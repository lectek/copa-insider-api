package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.inbound.web.dto.ImageGenRequestDTO;
import br.com.lectek.copainsider.adapters.outbound.messaging.ProductImageRequestedEvent;
import br.com.lectek.copainsider.application.port.inbound.ImageStudioUseCase;
import br.com.lectek.copainsider.application.port.outbound.ProductImageJobRepository;
import br.com.lectek.copainsider.application.port.outbound.ProdutoRepositoryPort;
import br.com.lectek.copainsider.domain.ai.ProductPromptFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductImageJobService {

    private static final Logger log = LoggerFactory.getLogger(ProductImageJobService.class);
    private static final String PRESET = "packshot";

    private final ProdutoRepositoryPort produtos;
    private final ProductImageJobRepository jobs;
    private final ImageStudioUseCase imageStudio;
    private final ProductPromptFactory promptFactory;
    private final AiGeneratedImageStorageService generatedImageStorageService;

    public ProductImageJobService(ProdutoRepositoryPort produtos,
                                  ProductImageJobRepository jobs,
                                  ImageStudioUseCase imageStudio,
                                  ProductPromptFactory promptFactory,
                                  AiGeneratedImageStorageService generatedImageStorageService) {
        this.produtos = produtos;
        this.jobs = jobs;
        this.imageStudio = imageStudio;
        this.promptFactory = promptFactory;
        this.generatedImageStorageService = generatedImageStorageService;
    }

    @Transactional
    public void process(ProductImageRequestedEvent evt) {
        Long productId = evt.productId();
        Optional<ProdutoRepositoryPort.ProdutoDTO> opt = produtos.findById(productId);
        if (opt.isEmpty()) {
            log.warn("Produto {} não encontrado. Ignorando job.", productId);
            return;
        }
        var p = mergePromptData(opt.get(), evt);

        if (p.imagem() != null && !p.imagem().isBlank()) {
            log.debug("Produto {} já possui imagem. Ignorando geração.", p.id());
            return;
        }

        Map<String, Object> vars = promptFactory.varsFromProduto(p);
        String fingerprint = promptFactory.fingerprint(PRESET, vars);
        ProductImageJobRepository.Job job = jobs.createQueued(p.id(), fingerprint);

        try {
            jobs.markRunning(job.id());

            String prompt = promptFactory.promptForProduto(p);
            ImageGenRequestDTO req = new ImageGenRequestDTO(PRESET, prompt, vars, null, true, true);

            ImageStudioUseCase.GeneratedImageResult generatedImage = imageStudio.generateSync(req);
            String persistedPngUrl = persistAndLinkGeneratedImage(p.id(), generatedImage);
            jobs.markDone(job.id(), persistedPngUrl);

            log.info("Imagem gerada com sucesso para produto {} -> {}", p.id(), persistedPngUrl);
        } catch (Exception e) {
            jobs.markError(job.id(), e.getMessage());
            log.error("Falha ao gerar imagem para produto {}: {}", p.id(), e.getMessage(), e);
        }
    }

    @Transactional
    public void regenerateForced(Long productId) {
        var p = produtos.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + productId));

        Map<String, Object> vars = promptFactory.varsFromProduto(p);
        String fingerprint = promptFactory.fingerprint(PRESET, vars);
        ProductImageJobRepository.Job job = jobs.createQueued(p.id(), fingerprint);

        try {
            jobs.markRunning(job.id());
            ImageGenRequestDTO req = new ImageGenRequestDTO(PRESET, promptFactory.promptForProduto(p), vars, null, true, true);

            ImageStudioUseCase.GeneratedImageResult generatedImage = imageStudio.generateSync(req);
            String persistedPngUrl = persistAndLinkGeneratedImage(p.id(), generatedImage);
            jobs.markDone(job.id(), persistedPngUrl);

            log.info("[FORCED] Imagem regenerada para produto {} -> {}", p.id(), persistedPngUrl);
        } catch (Exception e) {
            jobs.markError(job.id(), e.getMessage());
            log.error("[FORCED] Falha ao regenerar imagem para produto {}: {}", p.id(), e.getMessage(), e);
        }
    }

    @Transactional
    public Optional<String> ensureImageLinkedFromLastSuccessfulJob(final Long productId) {
        if (productId == null) {
            return Optional.empty();
        }

        final Optional<ProdutoRepositoryPort.ProdutoDTO> produtoOpt = produtos.findById(productId);
        if (produtoOpt.isEmpty()) {
            return Optional.empty();
        }

        final String imagemAtual = trimToNull(produtoOpt.get().imagem());
        if (imagemAtual != null) {
            return Optional.of(imagemAtual);
        }

        final Optional<ProductImageJobRepository.Job> lastJobOpt = jobs.findLastByProduct(productId);
        if (lastJobOpt.isEmpty()) {
            return Optional.empty();
        }

        final ProductImageJobRepository.Job lastJob = lastJobOpt.get();
        if (lastJob.status() != ProductImageJobRepository.Status.DONE) {
            return Optional.empty();
        }

        final String resultUrl = trimToNull(lastJob.resultUrl());
        if (resultUrl == null) {
            return Optional.empty();
        }

        produtos.updateImagem(productId, resultUrl);
        return produtos.findById(productId)
                .map(ProdutoRepositoryPort.ProdutoDTO::imagem)
                .map(ProductImageJobService::trimToNull)
                .or(() -> Optional.of(resultUrl));
    }

    private String persistAndLinkGeneratedImage(
            final Long productId,
            final ImageStudioUseCase.GeneratedImageResult generatedImage
    ) throws IOException {
        final String persistedPngUrl = generatedImageStorageService.persistGeneratedImageWithFallback(
                productId,
                generatedImage
        );
        try {
            produtos.updateImagem(productId, persistedPngUrl);
            return persistedPngUrl;
        } catch (Exception ex) {
            generatedImageStorageService.deletePersistedProductImage(persistedPngUrl);
            throw ex;
        }
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProdutoRepositoryPort.ProdutoDTO mergePromptData(
            final ProdutoRepositoryPort.ProdutoDTO produto,
            final ProductImageRequestedEvent event
    ) {
        if (produto == null || event == null) {
            return produto;
        }
        return new ProdutoRepositoryPort.ProdutoDTO(
                produto.id(),
                preferNonBlank(produto.nome(), event.nome()),
                produto.descricao(),
                preferNonBlank(produto.categoria(), event.categoria()),
                produto.codigoBarras(),
                preferNonBlank(produto.fabricante(), event.marca()),
                produto.imagem()
        );
    }

    private String preferNonBlank(final String currentValue, final String fallbackValue) {
        final String current = trimToNull(currentValue);
        if (current != null) {
            return current;
        }
        return trimToNull(fallbackValue);
    }
}
