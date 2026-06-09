package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.messaging.ProductImagePublisher;
import br.com.redemaisfarma.adapters.outbound.messaging.ProductImageRequestedEvent;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.core.tenant.TenantResolverService;
import br.com.redemaisfarma.application.port.outbound.ProductImageJobRepository;
import br.com.redemaisfarma.application.service.ProductImageJobService;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;
import java.util.List;

@Profile("!test")
@Controller
@RequestMapping
public class ProductImageAdminPageController {

    private static final Logger log = LoggerFactory.getLogger(
            ProductImageAdminPageController.class
    );

    private final ProdutoJpaRepository produtoRepo;
    private final ProductImagePublisher publisher;
    private final ProductImageJobService jobService;
    private final ProductImageJobRepository jobRepo;
    private final boolean kafkaEnabled;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    @Generated
    public ProductImageAdminPageController(
            final ProdutoJpaRepository produtoRepo,
            final ProductImagePublisher publisher,
            final ProductImageJobService jobService,
            final ProductImageJobRepository jobRepo,
            @Value("${kafka.enabled:false}") final boolean kafkaEnabled
    ) {
        this.produtoRepo = produtoRepo;
        this.publisher = publisher;
        this.jobService = jobService;
        this.jobRepo = jobRepo;
        this.kafkaEnabled = kafkaEnabled;
    }

    @GetMapping("/admin/imagens")
    public String pageProdutosSemImagem(
            @RequestParam(name = "q", required = false) final String q,
            @RequestParam(name = "size", defaultValue = "30") final int size,
            final Model model
    ) {
        final String termo = normalizeQuery(q);
        final int safeSize = Math.clamp(size, 10, 100);
        final PageRequest pageRequest = PageRequest.of(0, safeSize);
        final Long tenantId = resolveTenantId();
        final Page<ProdutoEntity> priorityPage = tenantId == null
                ? produtoRepo.searchSemMidiaComEstoque(termo, pageRequest)
                : produtoRepo.searchSemMidiaComEstoque(tenantId, termo, pageRequest);
        final Page<ProdutoEntity> backlogPage = tenantId == null
                ? produtoRepo.searchSemMidiaSemEstoque(termo, pageRequest)
                : produtoRepo.searchSemMidiaSemEstoque(tenantId, termo, pageRequest);
        final long priorityTotal = tenantId == null
                ? produtoRepo.countSemMidiaComEstoque(termo)
                : produtoRepo.countSemMidiaComEstoque(tenantId, termo);
        final long backlogTotal = tenantId == null
                ? produtoRepo.countSemMidiaSemEstoque(termo)
                : produtoRepo.countSemMidiaSemEstoque(tenantId, termo);
        model.addAttribute("priorityItems", priorityPage.getContent());
        model.addAttribute("backlogItems", backlogPage.getContent());
        model.addAttribute("priorityTotal", priorityTotal);
        model.addAttribute("backlogTotal", backlogTotal);
        model.addAttribute("totalSemImagem", priorityTotal + backlogTotal);
        model.addAttribute("priorityListed", priorityPage.getNumberOfElements());
        model.addAttribute("backlogListed", backlogPage.getNumberOfElements());
        model.addAttribute("pageSize", safeSize);
        model.addAttribute("q", q == null ? "" : q.trim());
        return "pages/admin/imagens/index";
    }

    @GetMapping("/api/admin/imagens/produtos/sem-imagem")
    @ResponseBody
    public Page<ProdutoEntity> apiProdutosSemImagem(final Pageable pageable) {
        return produtoRepo.findSemMidia(pageable);
    }

    @PostMapping({
            "/api/admin/imagens/{produtoId}/queue",
            "/admin/imagens/{produtoId}/queue"
    })
    @ResponseBody
    public ResponseEntity<?> queue(@PathVariable("produtoId") final String rawProdutoId) {
        final Long produtoId = parseProdutoId(rawProdutoId);
        if (produtoId == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("ID de produto invalido.");
        }

        final Long tenantId = resolveTenantId();
        final var produto = (tenantId == null
                ? produtoRepo.findById(produtoId)
                : produtoRepo.findByScopedId(tenantId, produtoId)).orElse(null);
        if (produto == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Produto nao encontrado.");
        }

        if (produto.getImagem() != null && !produto.getImagem().isBlank()) {
            return ResponseEntity.ok(new EnqueueResponse(
                    "JA_POSSUI_IMAGEM",
                    JobView.doneWithImage(produto.getId(), produto.getImagem())
            ));
        }

        final ProductImageRequestedEvent event = new ProductImageRequestedEvent(
                produto.getId(),
                produto.getNome(),
                produto.getFabricante(),
                produto.getCategoria(),
                String.valueOf(produto.getId())
        );

        boolean kafkaPublished = false;
        if (this.kafkaEnabled) {
            try {
                publisher.publish(event);
                kafkaPublished = true;
            } catch (RuntimeException ex) {
                log.warn(
                        "Falha ao publicar no Kafka para produto {}. Fallback sync sera usado. Erro={}",
                        produtoId,
                        ex.getMessage(),
                        ex
                );
            }
        }

        final String result;
        if (!this.kafkaEnabled) {
            result = "PROCESSADO_SYNC";
        } else if (kafkaPublished) {
            result = "PROCESSADO_SYNC_E_ENFILEIRADO";
        } else {
            result = "PROCESSADO_SYNC_FALLBACK";
        }
        return runSyncAndBuildResponse(produtoId, event, result);
    }

    @PostMapping({
            "/api/admin/imagens/{produtoId}/regenerate",
            "/admin/imagens/{produtoId}/regenerate"
    })
    @ResponseBody
    public ResponseEntity<?> regenerate(@PathVariable("produtoId") final String rawProdutoId) {
        final Long produtoId = parseProdutoId(rawProdutoId);
        if (produtoId == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("ID de produto invalido.");
        }

        try {
            jobService.regenerateForced(produtoId);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Produto nao encontrado.");
        } catch (RuntimeException ex) {
            log.error("Falha ao regenerar imagem para produto {}: {}", produtoId, ex.getMessage(), ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Falha ao processar solicitacao de imagem.");
        }

        final var lastJob = jobRepo.findLastByProduct(produtoId).orElse(null);
        return ResponseEntity.ok(new EnqueueResponse(
                "REGERADO",
                lastJob == null ? null : JobView.from(lastJob)
        ));
    }

    @GetMapping("/api/admin/imagens/jobs")
    @ResponseBody
    public List<JobView> jobs(
            @RequestParam(name = "status", required = false) final String status,
            @RequestParam(defaultValue = "20") final int limit,
            @RequestParam(defaultValue = "0") final int offset
    ) {
        final ProductImageJobRepository.Status st;
        if (status == null || status.isBlank()) {
            st = ProductImageJobRepository.Status.QUEUED;
        } else {
            st = ProductImageJobRepository.Status.valueOf(status.toUpperCase());
        }

        return jobRepo.findByStatus(
                        st,
                        Math.max(1, Math.min(limit, 1000)),
                        Math.max(0, offset))
                .stream()
                .map(JobView::from)
                .toList();
    }

    private ResponseEntity<EnqueueResponse> runSyncAndBuildResponse(
            final Long produtoId,
            final ProductImageRequestedEvent event,
            final String result
    ) {
        this.jobService.process(event);
        final var lastJob = jobRepo.findLastByProduct(produtoId).orElse(null);
        final JobView jobView = lastJob == null ? null : JobView.from(lastJob);
        final Long tenantId = resolveTenantId();
        final boolean productHasImage = (tenantId == null
                ? produtoRepo.findById(produtoId)
                : produtoRepo.findByScopedId(tenantId, produtoId))
                .map(produto -> produto.getImagem() != null
                        && !produto.getImagem().isBlank())
                .orElse(false);
        if (lastJob == null) {
            return ResponseEntity.accepted().body(new EnqueueResponse(result, null));
        }

        if (lastJob.status() == ProductImageJobRepository.Status.ERROR) {
            if (productHasImage) {
                return ResponseEntity.ok()
                        .body(new EnqueueResponse("PROCESSADO_SYNC_IMAGEM_EXISTENTE", jobView));
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new EnqueueResponse("ERRO_SYNC", jobView));
        }

        if (lastJob.status() != ProductImageJobRepository.Status.DONE) {
            return ResponseEntity.accepted().body(new EnqueueResponse(result, jobView));
        }

        return ResponseEntity.ok(new EnqueueResponse(result, jobView));
    }

    private static Long parseProdutoId(final String rawProdutoId) {
        if (rawProdutoId == null || rawProdutoId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(rawProdutoId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String normalizeQuery(final String rawValue) {
        if (rawValue == null) {
            return null;
        }
        final String value = rawValue.trim();
        return value.isBlank() ? null : value;
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }

    public record EnqueueResponse(String result, JobView lastJob) {
    }

    public record JobView(
            Long id,
            Long productId,
            String status,
            String resultUrl,
            String errorMsg,
            String fingerprint,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static JobView from(final ProductImageJobRepository.Job j) {
            return new JobView(
                    j.id(),
                    j.productId(),
                    j.status().name(),
                    j.resultUrl(),
                    j.errorMsg(),
                    j.fingerprint(),
                    j.createdAt(),
                    j.updatedAt()
            );
        }

        public static JobView doneWithImage(final Long productId, final String imageUrl) {
            return new JobView(
                    null,
                    productId,
                    ProductImageJobRepository.Status.DONE.name(),
                    imageUrl,
                    null,
                    null,
                    null,
                    null
            );
        }
    }
}
