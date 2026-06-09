package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.lectek.copainsider.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.core.exception.ImportInProgressException;
import br.com.lectek.copainsider.application.core.media.ImageStorageService;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.core.tenant.TenantFeature;
import br.com.lectek.copainsider.application.core.tenant.TenantFeatureGateService;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import br.com.lectek.copainsider.application.service.CatalogoVendaDisponivelService;
import br.com.lectek.copainsider.application.service.EstoqueFisicoCsvService;
import br.com.lectek.copainsider.application.service.EstoqueFisicoImportService;
import br.com.lectek.copainsider.application.service.ProductImageJobService;
import br.com.lectek.copainsider.application.service.ProductCategoryBindingService;
import br.com.lectek.copainsider.application.service.ProdutoAdminService;
import br.com.lectek.copainsider.application.service.SincronizacaoCatalogoService;
import br.com.lectek.copainsider.application.service.validation.ProductSourcePolicy;
import br.com.lectek.copainsider.domain.support.BarcodeNormalizer;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import jakarta.servlet.http.HttpSession;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin/produtos")
public class ProdutoAdminPageController {

    private static final Logger log = LoggerFactory.getLogger(ProdutoAdminPageController.class);
    private static final String CATEGORIA_CATALOGO_LOCAL = "Catalogo local";
    private static final String CATEGORIA_CATALOGO_NACIONAL = "Catalogo nacional";
    private static final String CATEGORIA_ESTOQUE_FISICO = "Estoque fisico";
    private static final String MODEL_ATTR_CATEGORIAS = "categorias";
    private static final String FLASH_SUCCESS = "success";
    private static final String FLASH_WARNING = "warning";
    private static final String FLASH_ERROR = "error";
    private static final String REDIRECT_PREFIX = "redirect:";
    private static final String PATH_PRODUTOS = "/admin/produtos";
    private static final String REDIRECT_PRODUTOS = REDIRECT_PREFIX + PATH_PRODUTOS;
    private static final String REDIRECT_PRODUTOS_BASE = REDIRECT_PREFIX + "/admin/produtos/";
    private static final String REDIRECT_PRODUTOS_NOVO = REDIRECT_PREFIX + "/admin/produtos/novo";
    private static final String PATH_ESTOQUE = "/admin/estoque";
    private static final String PATH_PRODUTOS_NAO_PRONTOS_TODOS = PATH_PRODUTOS + "/nao-prontos/todos";
    private static final String SUFFIX_EDITAR = "/editar";
    private static final int MAX_PENDING_ALL_LIMIT = 200_000;
    private static final String KEY_ALERTA_ESTOQUE_ENABLED = "app.estoque.alerta.enabled";
    private static final String KEY_ALERTA_ESTOQUE_LIMITE = "app.estoque.alerta.limite";
    private static final String UI_PRODUTOS_TEXT_PREFIX = "ui.admin.produtos.text.";
    private static final String UI_PRODUTOS_COLOR_PREFIX = "ui.admin.produtos.color.";
    private static final int DEFAULT_ALERTA_ESTOQUE_LIMITE = 2;
    private static final int MIN_ALERTA_ESTOQUE_LIMITE = 2;
    private static final int MAX_ALERTA_ESTOQUE_LIMITE = 100_000;
    private static final String SESSION_CATALOGO_PDF_PREVIEW = "admin.catalogo.pdf.preview";
    private static final Pattern BARCODE_PATTERN = Pattern.compile("\\b(?:\\d{8}|\\d{12,14})\\b");
    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\b\\d{1,5}\\b");
    private static final Pattern MONEY_PATTERN = Pattern.compile("(\\d{1,6}[\\.,]\\d{2})");
    private static final Pattern CATALOGO_PDF_MONEY_PATTERN = Pattern.compile("R\\$\\s*(\\d{1,6}[\\.,]\\d{2})");
    private static final Pattern CATALOGO_PDF_NCM_PATTERN = Pattern.compile("\\b\\d{8}\\b");
    private static final Pattern CATALOGO_PDF_BARCODE_LEGACY_PATTERN =
            Pattern.compile("^\\s*(\\d{8}|\\d{12,14})\\s+(\\d{1,6})\\s*$");
    private static final Pattern LETTER_PATTERN = Pattern.compile("\\p{L}");
    private static final String ORIGEM_CATALOGO_NACIONAL = "CATALOGO_NACIONAL";
    private static final String ORIGEM_CATALOGO_LOCAL = "CATALOGO_LOCAL";

    private final ProdutoAdminService adminService;
    private final ProdutoRepository produtoRepository;
    private final ProdutoCategoriaRepository categoriaRepository;
    private final ImageStorageService imageStorageService;
    private final ProductCategoryBindingService categoryBindingService;
    private final ProductImageJobService productImageJobService;
    private final AppSettingService appSettingService;
    private final ObjectProvider<CatalogoVendaDisponivelService> catalogoVendaDisponivelServiceProvider;
    private final ObjectProvider<EstoqueFisicoCsvService> estoqueFisicoCsvServiceProvider;
    private final ObjectProvider<EstoqueFisicoImportService> estoqueImportServiceProvider;
    private final ObjectProvider<ProdutoLegacyRepository> legacyRepositoryProvider;
    private final ObjectProvider<SincronizacaoCatalogoService> catalogSyncProvider;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;
    @Autowired(required = false)
    private TenantFeatureGateService tenantFeatureGateService;

    @GetMapping
    public String list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "estoque", required = false) String estoque,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            Model model
    ) {
        int alertaEstoqueLimite = this.resolveEstoqueBaixoLimite();
        Page<ProdutoEntity> page = adminService.buscarPagina(
                q,
                categoria,
                status,
                estoque,
                alertaEstoqueLimite,
                pageable
        );
        model.addAttribute("page", page);
        model.addAttribute("produtos", page.getContent());
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("categoria", categoria == null ? "" : categoria);
        model.addAttribute("statusFiltro", this.normalizeFilterParam(status));
        model.addAttribute("estoqueFiltro", this.normalizeFilterParam(estoque));
        model.addAttribute("ui", this.resolveProdutoListUi());
        model.addAttribute("resumoProdutos", this.resolveProductListSummary(alertaEstoqueLimite));
        model.addAttribute(MODEL_ATTR_CATEGORIAS, this.resolveCategorias());
        model.addAttribute("legacySyncEnabled", this.catalogSyncProvider.getIfAvailable() != null);
        this.populateEstoqueAlerta(model);
        return "pages/admin/produtos/lista";
    }

    @GetMapping("/lista")
    public String redirectList(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "estoque", required = false) String estoque
    ) {
        String termo = this.normalize(q);
        String categoriaFiltro = this.normalize(categoria);
        String statusFiltro = this.normalizeFilterParam(status);
        String estoqueFiltro = this.normalizeFilterParam(estoque);
        if (termo.isBlank()
                && categoriaFiltro.isBlank()
                && statusFiltro.isBlank()
                && estoqueFiltro.isBlank()) {
            return REDIRECT_PRODUTOS;
        }

        StringBuilder target = new StringBuilder(REDIRECT_PRODUTOS);
        String separator = "?";
        separator = this.appendQueryParam(target, separator, "q", termo);
        separator = this.appendQueryParam(target, separator, "categoria", categoriaFiltro);
        separator = this.appendQueryParam(target, separator, "status", statusFiltro);
        this.appendQueryParam(target, separator, "estoque", estoqueFiltro);
        return target.toString();
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (!this.produtoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        this.produtoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/excluir")
    @SuppressWarnings("java:S3516")
    public String deleteFromPage(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes
    ) {
        if (!this.produtoRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute(FLASH_WARNING, "Produto nao encontrado para exclusao.");
            return REDIRECT_PRODUTOS;
        }
        try {
            this.produtoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute(FLASH_SUCCESS, "Produto excluido com sucesso.");
        } catch (RuntimeException ex) {
            log.warn("[admin-produto] falha ao excluir produto id={}", id, ex);
            redirectAttributes.addFlashAttribute(FLASH_ERROR, "Nao foi possivel excluir o produto.");
        }
        return REDIRECT_PRODUTOS;
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> export(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "estoque", required = false) String estoque,
            @RequestParam(name = "limit", defaultValue = "2000") int limit
    ) {
        int safeLimit = Math.clamp(limit, 1, 50_000);
        int alertaEstoqueLimite = this.resolveEstoqueBaixoLimite();
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        String filename = "produtos-" + ts + ".csv";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        // lambda sem casts para preservar o tipo genérico
        StreamingResponseBody body = os -> {
            try {
                adminService.writeCsv(
                        os,
                        q,
                        categoria,
                        status,
                        estoque,
                        alertaEstoqueLimite,
                        safeLimit
                );
            } catch (IOException e) { // se writeCsv lancar checked
                throw new UncheckedIOException("Falha ao gerar CSV", e);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }

    @PostMapping(consumes = "multipart/form-data")
    @SuppressWarnings({"java:S3776", "java:S6541", "java:S4684"})
    public String criarProduto(
            @ModelAttribute("produto") ProdutoEntity produto,
            @RequestParam(value = "imagemFile", required = false) MultipartFile[] imagemFiles,
            RedirectAttributes ra
    ) {
        String nome = this.normalize(produto.getNome());
        if (nome.isBlank()) {
            return REDIRECT_PRODUTOS_NOVO + "?erro=nome";
        }
        produto.setNome(nome);

        String categoria = this.normalize(produto.getCategoria());
        if (categoria.isBlank()) {
            categoria = "Sem Categoria";
        }
        produto.setCategoria(categoria);
        this.applyMedicacaoRules(produto);

        if (produto.getLegacyId() != null && produto.getLegacyId() <= 0) {
            produto.setLegacyId(null);
        }

        if (produto.getLegacyId() != null) {
            Long tenantId = this.resolveTenantId();
            Optional<ProdutoEntity> existente = tenantId == null
                    ? produtoRepository.findByLegacyId(produto.getLegacyId())
                    : produtoRepository.findByTenantIdAndLegacyId(tenantId, produto.getLegacyId());
            if (existente.isPresent()) {
                ra.addFlashAttribute("info", "Produto ja existe no catalogo. Abrindo edicao.");
                return REDIRECT_PRODUTOS_BASE + existente.get().getId() + SUFFIX_EDITAR;
            }
        }

        String codigoBarras = this.normalize(produto.getCodigoBarras());
        if (codigoBarras.isBlank()) {
            produto.setCodigoBarras(null);
        } else {
            String barcodeNormalizado = this.normalizeBarcode(codigoBarras);
            Long tenantId = this.resolveTenantId();
            Optional<ProdutoEntity> existentePorCodigo = tenantId == null
                    ? produtoRepository.findByAnyCodigo(barcodeNormalizado)
                    : produtoRepository.findByAnyCodigo(tenantId, barcodeNormalizado);
            if (existentePorCodigo.isPresent()) {
                ra.addFlashAttribute("info", "Codigo de barras ja cadastrado. Abrindo edicao.");
                return REDIRECT_PRODUTOS_BASE + existentePorCodigo.get().getId() + SUFFIX_EDITAR;
            }
            produto.setCodigoBarras(barcodeNormalizado);
        }

        log.debug("[admin-produto] criar | nome='{}', categoria='{}', codigoBarras='{}'",
                produto.getNome(), produto.getCategoria(), produto.getCodigoBarras());

        List<MultipartFile> arquivosImagem = new ArrayList<>();
        if (imagemFiles != null) {
            for (MultipartFile imagemFile : imagemFiles) {
                if (imagemFile != null && !imagemFile.isEmpty()) {
                    arquivosImagem.add(imagemFile);
                }
            }
        }

        if (arquivosImagem.isEmpty()) {
            return REDIRECT_PRODUTOS_NOVO + "?erro=imagem";
        }

        produto.setId(null);
        if (produto.getTenantId() == null) {
            produto.setTenantId(this.resolveTenantId());
        }
        if (produto.getDisponivel() == null) {
            produto.setDisponivel(Boolean.TRUE);
        }
        if (produto.getDataCadastro() == null) {
            produto.setDataCadastro(LocalDate.now());
        }
        if (produto.getMetodoLeituraCodigoBarras() == null) {
            produto.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);
        }
        produto.setImagem(null);
        produto.setStatus(ProdutoStatus.IMPORTADO);
        produto.setPublicadoEm(null);
        this.categoryBindingService.bind(produto);

        ProdutoEntity salvo;
        try {
            salvo = produtoRepository.save(produto);
        } catch (DataIntegrityViolationException ex) {
            log.warn("[admin-produto] conflito integridade ao salvar | nome='{}', categoria='{}', codigoBarras='{}'",
                    produto.getNome(), produto.getCategoria(), produto.getCodigoBarras(), ex);
            return REDIRECT_PRODUTOS_NOVO + "?erro=integridade";
        }

        List<String> imageUrls = new ArrayList<>();
        try {
            for (MultipartFile imagemFile : arquivosImagem) {
                String imageUrl = imageStorageService.saveProductImage(salvo.getId(), imagemFile);
                imageUrls.add(imageUrl);
            }
            salvo.setImagensProduto(imageUrls);
            salvo.setStatus(ProdutoStatus.IMPORTADO);
            salvo.setPublicadoEm(null);
            produtoRepository.save(salvo);
        } catch (IOException | RuntimeException ex) {
            cleanupProductImages(imageUrls);
            log.warn("[admin-produto] falha upload imagem id={} nome='{}': {}", salvo.getId(), salvo.getNome(), ex.getMessage());
            produtoRepository.deleteById(salvo.getId());
            return REDIRECT_PRODUTOS_NOVO + "?erro=imagem_upload";
        }

        ra.addFlashAttribute(FLASH_SUCCESS, "Produto criado em IMPORTADO. Valide e publique na edicao.");
        return REDIRECT_PRODUTOS_BASE + salvo.getId() + SUFFIX_EDITAR;
    }

    @GetMapping("/novo")
    public String novoProdutoPage(
            @RequestParam(name = "legacyId", required = false) Long legacyId,
            @RequestParam(name = "nome", required = false) String nome,
            @RequestParam(name = "descricao", required = false) String descricao,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "codigoBarras", required = false) String codigoBarras,
            @RequestParam(name = "estoque", required = false) Integer estoque,
            @RequestParam(name = "fabricante", required = false) String fabricante,
            @RequestParam(name = "unidade", required = false) String unidade,
            @RequestParam(name = "origem", required = false) String origem,
            @RequestParam(name = "mostrarPreviewCatalogoPdf", required = false) Boolean mostrarPreviewCatalogoPdf,
            HttpSession session,
            Model model
    ) {
        ProdutoEntity produto = this.buildPrefilledNovoProduto(
                legacyId,
                nome,
                descricao,
                categoria,
                codigoBarras,
                estoque,
                fabricante,
                unidade,
                origem
        );
        model.addAttribute("produto", produto);
        model.addAttribute("produtoPrefill",
                legacyId != null
                        || StringUtils.hasText(nome)
                        || StringUtils.hasText(codigoBarras));
        model.addAttribute(MODEL_ATTR_CATEGORIAS, this.resolveCategorias());
        model.addAttribute("legacySyncEnabled", this.catalogSyncProvider.getIfAvailable() != null);
        List<CatalogoPdfPreviewItem> previewItems = this.getCatalogoPdfPreview(session);
        model.addAttribute("catalogoPdfPreviewItems", previewItems);
        model.addAttribute("catalogoPdfPreviewTotal", previewItems.size());
        model.addAttribute("mostrarPreviewCatalogoPdf", Boolean.TRUE.equals(mostrarPreviewCatalogoPdf) || !previewItems.isEmpty());
        this.populateEstoqueAlerta(model);
        return "pages/admin/produtos/form";
    }

    @PostMapping("/alerta-estoque")
    public String atualizarAlertaEstoque(@RequestParam(name = "limite", required = false) Integer limite,
                                         @RequestParam(name = "redirect", required = false) String redirect,
                                         RedirectAttributes ra) {
        int safeLimit = limite == null
                ? DEFAULT_ALERTA_ESTOQUE_LIMITE
                : Math.clamp(
                        limite,
                        MIN_ALERTA_ESTOQUE_LIMITE,
                        MAX_ALERTA_ESTOQUE_LIMITE
                );
        this.appSettingService.upsert(
                KEY_ALERTA_ESTOQUE_LIMITE,
                String.valueOf(safeLimit),
                "Limite de estoque baixo para aviso ao admin"
        );
        ra.addFlashAttribute(
                FLASH_SUCCESS,
                "Limite de alerta de estoque atualizado para "
                        + safeLimit
                        + ". A regra agora considera apenas saldo acima de 0 e abaixo desse limite."
        );
        return REDIRECT_PREFIX + this.resolveRedirectPath(redirect);
    }

    @GetMapping(value = "/busca-rapida", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @SuppressWarnings({"java:S3776", "java:S135"})
    public List<ProdutoLookupItem> buscaRapida(
            @RequestParam("q") String q,
            @RequestParam(name = "limit", defaultValue = "8") int limit
    ) {
        String termo = this.normalize(q);
        if (termo.isBlank()) {
            return List.of();
        }

        int safeLimit = Math.clamp(limit, 1, 20);
        Pageable pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.ASC, "nome"));

        List<ProdutoLookupItem> itens = new ArrayList<>(safeLimit);
        this.produtoRepository.searchPageByCategoria(termo, null, pageable)
                .getContent()
                .stream()
                .map(ProdutoLookupItem::from)
                .forEach(itens::add);

        if (itens.size() < safeLimit) {
            ProdutoLegacyRepository legacyRepository = this.legacyRepositoryProvider.getIfAvailable();
            if (legacyRepository != null) {
                List<ProdutoLegacyEntity> legacyMatches = this.buscarNoCatalogoNacional(legacyRepository, termo, safeLimit);
                for (ProdutoLegacyEntity legacy : legacyMatches) {
                    if (itens.size() >= safeLimit) {
                        break;
                    }

                    ProdutoLookupItem mapped = ProdutoLookupItem.fromLegacy(legacy);
                    Long tenantId = this.resolveTenantId();
                    if (mapped.legacyId() != null && this.produtoRepository.existsByLegacyId(tenantId, mapped.legacyId())) {
                        continue;
                    }
                    if (StringUtils.hasText(mapped.codigoBarras())
                            && this.produtoRepository.existsByAnyCodigo(tenantId, mapped.codigoBarras())) {
                        continue;
                    }

                    boolean duplicateByLegacy = mapped.legacyId() != null && itens.stream()
                            .anyMatch(existing -> mapped.legacyId().equals(existing.legacyId()));
                    boolean duplicateByBarcode = StringUtils.hasText(mapped.codigoBarras()) && itens.stream()
                            .anyMatch(existing -> mapped.codigoBarras().equals(existing.codigoBarras()));

                    if (duplicateByLegacy || duplicateByBarcode) {
                        continue;
                    }
                    itens.add(mapped);
                }
            }
        }

        if (itens.isEmpty()) {
            Pageable sugestoesPage = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "id"));
            Long tenantId = this.resolveTenantId();
            (tenantId == null
                    ? this.produtoRepository.findAll(sugestoesPage)
                    : this.produtoRepository.findByTenantId(tenantId, sugestoesPage))
                    .getContent()
                    .stream()
                    .map(ProdutoLookupItem::from)
                    .forEach(itens::add);
        }

        return itens.stream()
                .sorted(Comparator.comparing(item -> this.normalize(item.nome())))
                .limit(safeLimit)
                .toList();
    }

    @GetMapping(value = "/nao-prontos", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ProdutoLookupPage listarNaoProntos(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size
    ) {
        String termo = this.normalizeQuery(q);
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 1, 40);
        NaoProntosSlice slice = this.fetchNaoProntosSlice(termo, safePage, safeSize);
        return new ProdutoLookupPage(slice.items(), safePage, safeSize, slice.total(), slice.hasNext());
    }

    @GetMapping("/nao-prontos/todos")
    public String listarNaoProntosTodos(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size,
            Model model
    ) {
        CatalogoSlice slice = this.fetchCatalogoSlice(q, page, size);
        model.addAttribute("pendingItems", slice.items());
        model.addAttribute("pendingTotal", slice.total());
        model.addAttribute("listedCount", slice.items().size());
        model.addAttribute("pageNumber", slice.page());
        model.addAttribute("pageSize", slice.size());
        model.addAttribute("totalPages", slice.totalPages());
        model.addAttribute("hasPrev", slice.hasPrev());
        model.addAttribute("hasNext", slice.hasNext());
        model.addAttribute("q", q == null ? "" : q.trim());
        return "pages/admin/produtos/nao-prontos-todos";
    }

    @PostMapping("/nao-prontos/publicar-aptos")
    @SuppressWarnings("java:S3776")
    public String publicarNaoProntosAptosEmLote(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "validador", required = false) String validador,
            RedirectAttributes ra
    ) {
        List<ProdutoLookupItem> pendentes = this.resolveNaoProntosFromDatabase(q, MAX_PENDING_ALL_LIMIT, false);
        if (pendentes.isEmpty()) {
            ra.addFlashAttribute("info", "Nenhum produto pendente encontrado para o filtro informado.");
            return this.redirectNaoProntosTodos(q);
        }

        List<Long> ids = pendentes.stream()
                .map(ProdutoLookupItem::id)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (ids.isEmpty()) {
            ra.addFlashAttribute(FLASH_WARNING, "Nenhum item do filtro possui cadastro no catalogo para publicacao.");
            return this.redirectNaoProntosTodos(q);
        }

        List<ProdutoEntity> entidades = this.produtoRepository.findAllById(ids);
        if (entidades.isEmpty()) {
            ra.addFlashAttribute(FLASH_WARNING, "Nao foi possivel localizar os produtos pendentes no catalogo.");
            return this.redirectNaoProntosTodos(q);
        }

        String responsavel = StringUtils.hasText(validador) ? validador.trim() : "admin-lote";
        LocalDateTime now = LocalDateTime.now();

        int bloqueados = 0;
        int origemNaoPublicavel = 0;
        int semCodigoBarras = 0;
        int semImagem = 0;
        int semPreco = 0;
        int semEstoque = 0;
        List<ProdutoEntity> paraPublicar = new ArrayList<>();

        for (ProdutoEntity entity : entidades) {
            this.vincularImagemGeradaSeNecessario(entity);
            boolean pronto = true;

            if (!this.temOrigemPublicavel(entity)) {
                origemNaoPublicavel++;
                pronto = false;
            }
            if (!this.temCodigoBarrasValido(entity)) {
                semCodigoBarras++;
                pronto = false;
            }
            if (!this.temImagem(entity)) {
                semImagem++;
                pronto = false;
            }
            if (!this.temPrecoPositivo(entity)) {
                semPreco++;
                pronto = false;
            }
            if (!this.temEstoquePositivo(entity)) {
                semEstoque++;
                pronto = false;
            }

            if (!pronto) {
                bloqueados++;
                continue;
            }

            entity.setDisponivel(Boolean.TRUE);
            entity.setStatus(ProdutoStatus.PUBLICADO);
            entity.setValidador(responsavel);
            entity.setPublicadoEm(now);
            entity.setDespublicadoEm(null);
            entity.setUpdatedAt(now);
            paraPublicar.add(entity);
        }

        if (!paraPublicar.isEmpty()) {
            this.produtoRepository.saveAll(paraPublicar);
            ra.addFlashAttribute(FLASH_SUCCESS,
                    "Publicacao em lote concluida: " + paraPublicar.size() + " produto(s) publicado(s).");
        } else {
            ra.addFlashAttribute(FLASH_WARNING,
                    "Nenhum produto apto para publicar. Corrija origem, codigo de barras, imagem, preco e estoque dos pendentes.");
        }

        if (bloqueados > 0) {
            ra.addFlashAttribute("info",
                    "Bloqueios encontrados em " + bloqueados + " item(ns): origem nao publicavel " + origemNaoPublicavel
                            + ", sem codigo de barras " + semCodigoBarras
                            + ", sem imagem " + semImagem
                            + ", sem preco " + semPreco
                            + ", sem estoque " + semEstoque + ".");
        }

        return this.redirectNaoProntosTodos(q);
    }

    @PostMapping("/sincronizar-estoque")
    @SuppressWarnings("java:S3516")
    public String sincronizarEstoqueFisico(RedirectAttributes ra) {
        SincronizacaoCatalogoService syncService = this.catalogSyncProvider.getIfAvailable();
        if (syncService == null) {
            ra.addFlashAttribute(FLASH_WARNING, "Sincronizacao do catalogo nacional indisponivel. Ative legacy.sync.enabled e configure Firebird.");
            return REDIRECT_PRODUTOS;
        }

        try {
            SincronizacaoCatalogoService.ResumoSync resumo = syncService.sincronizarTudo();
            ra.addFlashAttribute(FLASH_SUCCESS,
                    "Sincronizacao do catalogo nacional concluida. Lidos: " + resumo.lidos()
                            + ", inseridos: " + resumo.inseridos()
                            + ", atualizados: " + resumo.atualizados()
                            + ", ignorados: " + resumo.ignorados()
                            + ", erros: " + resumo.erros() + ".");
        } catch (Exception ex) {
            log.error("[admin-produto] falha ao sincronizar catalogo nacional", ex);
            ra.addFlashAttribute(FLASH_ERROR, "Falha ao sincronizar catalogo nacional. Verifique as configuracoes do legado.");
        }
        return REDIRECT_PRODUTOS;
    }

    @PostMapping("/sincronizar-catalogo-venda-local")
    @SuppressWarnings("java:S3516")
    public String sincronizarCatalogoVendaLocal(@RequestParam(name = "redirect", required = false) String redirect,
                                                RedirectAttributes ra) {
        CatalogoVendaDisponivelService service = this.catalogoVendaDisponivelServiceProvider.getIfAvailable();
        if (service == null) {
            ra.addFlashAttribute(FLASH_WARNING, "Importacao do PDF local indisponivel no ambiente atual.");
            return REDIRECT_PREFIX + this.resolveRedirectPath(redirect);
        }

        try {
            CatalogoVendaDisponivelService.ImportacaoResumo resumo = service.sincronizarCatalogoDisponivel();
            ra.addFlashAttribute(FLASH_SUCCESS,
                    "Catalogo local atualizado. Lidos: " + resumo.lidos()
                            + ", inseridos: " + resumo.inseridos()
                            + ", atualizados: " + resumo.atualizados()
                            + ", inalterados: " + resumo.inalterados()
                            + ", desativados: " + resumo.desativados() + ".");
        } catch (Exception ex) {
            log.error("[admin-produto] falha ao sincronizar catalogo de venda local", ex);
            ra.addFlashAttribute(FLASH_ERROR, "Falha ao importar o PDF local com produtos disponiveis para venda.");
        }
        return REDIRECT_PREFIX + this.resolveRedirectPath(redirect);
    }

    @PostMapping("/importar-estoque-fisico")
    @SuppressWarnings("java:S3516")
    public String importarEstoqueFisico(
            @RequestParam(name = "arquivoCsv", required = false) MultipartFile arquivoCsv,
            RedirectAttributes ra
    ) {
        EstoqueFisicoImportService importService = this.estoqueImportServiceProvider.getIfAvailable();
        if (importService == null) {
            ra.addFlashAttribute(FLASH_WARNING, "Importacao CSV indisponivel no ambiente atual.");
            return REDIRECT_PRODUTOS_NOVO;
        }

        try {
            boolean uploaded = arquivoCsv != null && !arquivoCsv.isEmpty();
            EstoqueFisicoImportService.ImportacaoResumo resumo;
            if (uploaded) {
                MultipartFile csvUpload = Objects.requireNonNull(arquivoCsv, "arquivoCsv nao pode ser nulo quando uploaded=true");
                resumo = importService.importarTodosComoNaoDisponiveis(csvUpload.getInputStream());
            } else {
                resumo = importService.importarTodosComoNaoDisponiveis();
            }
            ra.addFlashAttribute(FLASH_SUCCESS,
                    "Importacao concluida" + (uploaded ? " via upload" : "") + ": lidos " + resumo.lidos()
                            + ", inseridos " + resumo.inseridos()
                            + ", atualizados " + resumo.atualizados()
                            + ", ignorados " + resumo.ignorados()
                            + ", erros " + resumo.erros() + ".");
        } catch (ImportInProgressException ex) {
            log.warn("[admin-produto] importacao de estoque fisico recusada por concorrencia");
            ra.addFlashAttribute(FLASH_WARNING, ex.getMessage());
        } catch (Exception ex) {
            log.error("[admin-produto] falha ao importar estoque fisico CSV", ex);
            ra.addFlashAttribute(FLASH_ERROR, "Falha ao importar estoque fisico. Verifique o arquivo CSV.");
        }
        return REDIRECT_PRODUTOS_NOVO;
    }

    @PostMapping("/importar-catalogo-pdf/preview")
    public String importarCatalogoPdfPreview(
            @RequestParam(name = "arquivoPdf", required = false) MultipartFile arquivoPdf,
            RedirectAttributes ra,
            HttpSession session
    ) {
        if (arquivoPdf == null || arquivoPdf.isEmpty()) {
            ra.addFlashAttribute(FLASH_WARNING, "Selecione um arquivo PDF para leitura do catalogo.");
            return REDIRECT_PRODUTOS_NOVO;
        }

        try {
            List<CatalogoPdfPreviewItem> itens = this.parseCatalogoPdf(arquivoPdf);
            if (itens.isEmpty()) {
                session.removeAttribute(SESSION_CATALOGO_PDF_PREVIEW);
                ra.addFlashAttribute(FLASH_WARNING, "Nao foi possivel extrair itens validos do PDF. Verifique o layout enviado pelo distribuidor.");
                return REDIRECT_PRODUTOS_NOVO;
            }

            session.setAttribute(SESSION_CATALOGO_PDF_PREVIEW, new ArrayList<>(itens));
            ra.addFlashAttribute(FLASH_SUCCESS, "Leitura do PDF concluida: " + itens.size() + " item(ns) pronto(s) para conferencia.");
        } catch (Exception ex) {
            log.error("[admin-produto] falha ao ler PDF de catalogo", ex);
            session.removeAttribute(SESSION_CATALOGO_PDF_PREVIEW);
            ra.addFlashAttribute(FLASH_ERROR, "Falha ao ler o PDF do catalogo. Confirme se o arquivo nao esta corrompido.");
        }

        return REDIRECT_PRODUTOS_NOVO + "?mostrarPreviewCatalogoPdf=true";
    }

    @PostMapping("/importar-catalogo-pdf/confirmar")
    @SuppressWarnings({"java:S3776", "java:S6541"})
    public String confirmarCatalogoPdf(
            RedirectAttributes ra,
            HttpSession session
    ) {
        List<CatalogoPdfPreviewItem> itens = this.getCatalogoPdfPreview(session);
        if (itens.isEmpty()) {
            ra.addFlashAttribute(FLASH_WARNING, "Nenhum item de PDF para confirmar. Faça a leitura do arquivo primeiro.");
            return REDIRECT_PRODUTOS_NOVO;
        }

        int lidos = itens.size();
        int inseridos = 0;
        int atualizados = 0;
        int ignorados = 0;
        LocalDateTime now = LocalDateTime.now();
        List<ProdutoEntity> lote = new ArrayList<>();
        Map<Long, ProdutoEntity> cacheLegacy = new HashMap<>();
        Map<String, ProdutoEntity> cacheCodigo = new HashMap<>();

        for (CatalogoPdfPreviewItem item : itens) {
            if (item.quantidade() <= 0) {
                ignorados++;
                continue;
            }

            ProdutoEntity entity = this.resolveProdutoParaCatalogoPdf(item, cacheLegacy, cacheCodigo);
            boolean novo = entity.getId() == null;
            Integer estoqueAtual = entity.getEstoque() == null ? 0 : Math.max(0, entity.getEstoque());
            entity.setEstoque(estoqueAtual + item.quantidade());

            if (novo) {
                entity.setNome(this.normalize(item.nome()).isBlank() ? "Produto importado do catalogo PDF" : item.nome());
                entity.setDescricao(entity.getNome());
                entity.setCategoria(CATEGORIA_CATALOGO_LOCAL);
                entity.setLegacyId(item.legacyId());
                entity.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
                if (!this.normalize(item.codigoBarras()).isBlank()) {
                    entity.setCodigoBarras(item.codigoBarras());
                }
                if (item.precoVenda() != null && item.precoVenda().compareTo(BigDecimal.ZERO) > 0) {
                    entity.setPrecoVenda(item.precoVenda());
                    entity.setPrecoCusto(item.precoVenda());
                } else if (entity.getPrecoVenda() == null) {
                    entity.setPrecoVenda(BigDecimal.ZERO);
                    entity.setPrecoCusto(BigDecimal.ZERO);
                }
                inseridos++;
            } else {
                if (item.precoVenda() != null
                        && item.precoVenda().compareTo(BigDecimal.ZERO) > 0
                        && (entity.getPrecoVenda() == null || entity.getPrecoVenda().compareTo(BigDecimal.ZERO) <= 0)) {
                    entity.setPrecoVenda(item.precoVenda());
                    if (entity.getPrecoCusto() == null || entity.getPrecoCusto().compareTo(BigDecimal.ZERO) <= 0) {
                        entity.setPrecoCusto(item.precoVenda());
                    }
                }
                atualizados++;
            }

            this.applyCatalogoPdfVendaMetadata(entity, item, novo);
            if (this.podePublicarProdutoDoCatalogoPdf(entity)) {
                entity.setDisponivel(Boolean.TRUE);
                entity.setStatus(ProdutoStatus.PUBLICADO);
                entity.setPublicadoEm(entity.getPublicadoEm() == null ? now : entity.getPublicadoEm());
                entity.setDespublicadoEm(null);
            } else {
                entity.setDisponivel(Boolean.FALSE);
                entity.setStatus(ProdutoStatus.IMPORTADO);
                entity.setDespublicadoEm(now);
            }
            entity.setDataImportacao(now);
            entity.setUpdatedAt(now);
            lote.add(entity);
        }

        if (!lote.isEmpty()) {
            this.categoryBindingService.bindAll(lote);
            this.produtoRepository.saveAll(lote);
        }
        session.removeAttribute(SESSION_CATALOGO_PDF_PREVIEW);
        ra.addFlashAttribute(
                FLASH_SUCCESS,
                "Importacao confirmada: lidos " + lidos
                        + ", novos " + inseridos
                        + ", publicados/atualizados " + atualizados
                        + ", ignorados " + ignorados + "."
        );
        return REDIRECT_PRODUTOS_NOVO;
    }

    private void applyCatalogoPdfVendaMetadata(
            ProdutoEntity entity,
            CatalogoPdfPreviewItem item,
            boolean novo
    ) {
        if (entity.getLegacyId() == null && item.legacyId() != null) {
            entity.setLegacyId(item.legacyId());
        }

        boolean managedByCatalogSync = novo || ProductSourcePolicy.isManagedByCatalogSync(entity);
        if (managedByCatalogSync) {
            entity.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        }

        String codigoBarras = this.normalizeBarcode(item.codigoBarras());
        if (!codigoBarras.isBlank()
                && (managedByCatalogSync || this.normalize(entity.getCodigoBarras()).isBlank())) {
            entity.setCodigoBarras(codigoBarras);
        }
    }

    @PostMapping("/importar-catalogo-pdf/limpar")
    public String limparCatalogoPdfPreview(HttpSession session, RedirectAttributes ra) {
        session.removeAttribute(SESSION_CATALOGO_PDF_PREVIEW);
        ra.addFlashAttribute(FLASH_SUCCESS, "Pre-visualizacao do PDF limpa.");
        return REDIRECT_PRODUTOS_NOVO;
    }

    @GetMapping("/form")
    public String redirectFormToNovo() {
        return REDIRECT_PRODUTOS_NOVO;
    }

    @PostMapping("/{id}/validar")
    @ResponseBody
    public ResponseEntity<String> validarProdutoFluxo(
            @PathVariable("id") final Long id,
            @RequestParam("validador") final String validador
    ) {
        Long tenantId = this.resolveTenantId();
        return (tenantId == null
                ? this.produtoRepository.findById(id)
                : this.produtoRepository.findByScopedId(tenantId, id))
                .map(entity -> {
                    if (entity.getStatus() == ProdutoStatus.PUBLICADO) {
                        return ResponseEntity
                                .status(org.springframework.http.HttpStatus.CONFLICT)
                                .body("Produto publicado nao pode voltar para VALIDADO.");
                    }
                    entity.setStatus(ProdutoStatus.VALIDADO);
                    entity.setValidador(this.resolveValidadorNome(validador));
                    entity.setUpdatedAt(LocalDateTime.now());
                    this.produtoRepository.save(entity);
                    return ResponseEntity.ok("VALIDADO");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/publicar")
    @ResponseBody
    public ResponseEntity<String> publicarProdutoFluxo(
            @PathVariable("id") final Long id,
            @RequestParam("validador") final String validador
    ) {
        Long tenantId = this.resolveTenantId();
        return (tenantId == null
                ? this.produtoRepository.findById(id)
                : this.produtoRepository.findByScopedId(tenantId, id))
                .map(entity -> {
                    if (entity.getStatus() != ProdutoStatus.VALIDADO) {
                        return ResponseEntity
                                .status(org.springframework.http.HttpStatus.CONFLICT)
                                .body("Produto deve estar VALIDADO antes de PUBLICAR.");
                    }
                    String motivoBloqueio = this.resolveMotivoBloqueioPublicacao(entity);
                    if (motivoBloqueio != null) {
                        return ResponseEntity
                                .status(org.springframework.http.HttpStatus.CONFLICT)
                                .body(motivoBloqueio);
                    }
                    entity.setDisponivel(Boolean.TRUE);
                    entity.setStatus(ProdutoStatus.PUBLICADO);
                    entity.setValidador(this.resolveValidadorNome(validador));
                    entity.setPublicadoEm(LocalDateTime.now());
                    entity.setDespublicadoEm(null);
                    entity.setUpdatedAt(LocalDateTime.now());
                    this.produtoRepository.save(entity);
                    return ResponseEntity.ok("PUBLICADO");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private String resolveValidadorNome(final String validador) {
        return StringUtils.hasText(validador) ? validador.trim() : "admin-web";
    }

    @GetMapping("/{id}/editar")
    public String editarProdutoPage(@PathVariable("id") Long id, Model model) {
        model.addAttribute("produtoId", id);
        Long tenantId = this.resolveTenantId();
        (tenantId == null
                ? this.produtoRepository.findById(id)
                : this.produtoRepository.findByScopedId(tenantId, id))
                .ifPresent(produto -> model.addAttribute("produto", produto));
        model.addAttribute(MODEL_ATTR_CATEGORIAS, this.resolveCategorias());
        this.populateEstoqueAlerta(model);
        return "pages/admin/produtos/editar";
    }

    private List<String> resolveCategorias() {
        List<String> categorias = this.categoriaRepository.findAllNomes();
        if (categorias == null || categorias.isEmpty()) {
            return List.of("Sem Categoria");
        }
        return categorias;
    }

    private void applyMedicacaoRules(ProdutoEntity produto) {
        if (this.isReceitaControladaEnabled()) {
            return;
        }
        produto.setTarjaMedicacao(null);
        produto.setExigeReceita(Boolean.FALSE);
    }

    private boolean isReceitaControladaEnabled() {
        if (this.tenantFeatureGateService == null) {
            return false;
        }
        return this.tenantFeatureGateService.isEnabledForCurrentTenant(
                TenantFeature.MOD_RECEITA_CONTROLADA,
                false
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeBarcode(String value) {
        return BarcodeNormalizer.normalize(value);
    }

    private String normalizeQuery(String value) {
        String termo = this.normalize(value);
        return termo.isBlank() ? null : termo;
    }

    private String normalizeFilterParam(String value) {
        String normalized = this.normalize(value);
        return normalized.isBlank() ? "" : normalized.toUpperCase(Locale.ROOT);
    }

    private String appendQueryParam(StringBuilder target,
                                    String separator,
                                    String key,
                                    String value) {
        if (!StringUtils.hasText(value)) {
            return separator;
        }
        target.append(separator)
                .append(key)
                .append("=")
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        return "&";
    }

    private int resolveEstoqueBaixoLimite() {
        return Math.max(
                MIN_ALERTA_ESTOQUE_LIMITE,
                this.appSettingService.getInt(
                        KEY_ALERTA_ESTOQUE_LIMITE,
                        DEFAULT_ALERTA_ESTOQUE_LIMITE
                )
        );
    }

    private void populateEstoqueAlerta(Model model) {
        boolean alertaAtivo = this.appSettingService.getBoolean(KEY_ALERTA_ESTOQUE_ENABLED, true);
        int limite = this.resolveEstoqueBaixoLimite();
        long qtdBaixo = this.produtoRepository.countComEstoqueBaixo(limite);

        model.addAttribute("alertaEstoqueAtivo", alertaAtivo);
        model.addAttribute("alertaEstoqueLimite", limite);
        model.addAttribute("alertaEstoqueBaixoTotal", qtdBaixo);
    }

    private ProductListSummary resolveProductListSummary(int alertaEstoqueLimite) {
        return new ProductListSummary(
                this.produtoRepository.countSemEstoque(),
                this.produtoRepository.countComEstoqueBaixo(alertaEstoqueLimite),
                this.resolveProdutosVendaveisTotal()
        );
    }

    private long resolveProdutosVendaveisTotal() {
        Long tenantId = this.resolveTenantId();
        if (tenantId == null) {
            return this.produtoRepository.countPubliclySellable(ProdutoRepository.PUBLIC_ALLOWED_SOURCES);
        }
        return this.produtoRepository.countPubliclySellable(tenantId, ProdutoRepository.PUBLIC_ALLOWED_SOURCES);
    }

    private ProdutoListUiConfig resolveProdutoListUi() {
        LinkedHashMap<String, String> defaultTexts = new LinkedHashMap<>();
        defaultTexts.put("title", "Produtos");
        defaultTexts.put("subtitle", "Gerencie catalogo, estoque e imagens com leitura rapida e menos ruido visual.");
        defaultTexts.put("summary.zero", "Backlog sem estoque");
        defaultTexts.put("summary.low", "Baixo estoque");
        defaultTexts.put("summary.available", "Vendaveis");
        defaultTexts.put("summary.zeroHint", "Catalogo importado sem saldo; nao e prioridade de venda");
        defaultTexts.put("summary.lowHint", "Itens com saldo que precisam de reposicao");
        defaultTexts.put("summary.availableHint", "Com estoque, preco e codigo validos no site");
        defaultTexts.put("alert.title", "Alerta de estoque baixo");
        defaultTexts.put("alert.description", "Ajuste o limite que separa os produtos em atencao.");
        defaultTexts.put("alert.limitLabel", "Limite para alerta");
        defaultTexts.put("alert.saveButton", "Salvar limite");
        defaultTexts.put("searchPlaceholder", "Buscar por nome, categoria, SKU ou codigo de barras");
        defaultTexts.put("filter.categoryLabel", "Categoria");
        defaultTexts.put("filter.statusLabel", "Status");
        defaultTexts.put("filter.stockLabel", "Estoque");
        defaultTexts.put("filter.submit", "Filtrar");
        defaultTexts.put("filter.clear", "Limpar");
        defaultTexts.put("filter.tip", "Dica: com o leitor conectado, basta bipar o codigo de barras nesta tela para abrir a busca.");
        defaultTexts.put("action.syncCatalog", "Sincronizar catalogo nacional");
        defaultTexts.put("action.syncPdf", "Atualizar disponiveis do PDF");
        defaultTexts.put("action.photosQueue", "Fila de fotos");
        defaultTexts.put("action.export", "Exportar CSV");
        defaultTexts.put("action.settings", "Configurar textos e cores");
        defaultTexts.put("action.new", "Novo produto");
        defaultTexts.put("table.product", "Produto");
        defaultTexts.put("table.price", "Preco");
        defaultTexts.put("table.stock", "Estoque");
        defaultTexts.put("table.category", "Categoria");
        defaultTexts.put("table.status", "Status");
        defaultTexts.put("table.ai", "IA");
        defaultTexts.put("table.actions", "Acoes");
        defaultTexts.put("status.available", "Disponivel");
        defaultTexts.put("status.unavailable", "Indisponivel");
        defaultTexts.put("stock.empty", "Sem estoque");
        defaultTexts.put("stock.low", "Baixo");
        defaultTexts.put("stock.normal", "Normal");
        defaultTexts.put("stock.unit", "un");
        defaultTexts.put("stock.threshold", "limite");
        defaultTexts.put("image.none", "Sem imagem");
        defaultTexts.put("image.preview", "Ver");
        defaultTexts.put("image.generate", "Gerar IA");
        defaultTexts.put("image.regenerate", "Regenerar");
        defaultTexts.put("row.edit", "Editar");
        defaultTexts.put("row.more", "Mais");
        defaultTexts.put("row.delete", "Excluir");
        defaultTexts.put("row.code", "Codigo");
        defaultTexts.put("empty", "Nenhum produto encontrado para os filtros informados.");

        LinkedHashMap<String, String> defaultColors = new LinkedHashMap<>();
        defaultColors.put("gradient.from", "#0f172a");
        defaultColors.put("gradient.mid", "#1d4ed8");
        defaultColors.put("gradient.to", "#dc2626");
        defaultColors.put("stock.empty", "#dc2626");
        defaultColors.put("stock.low", "#f59e0b");
        defaultColors.put("stock.normal", "#16a34a");

        Set<String> keys = new HashSet<>();
        defaultTexts.keySet().forEach(key -> keys.add(UI_PRODUTOS_TEXT_PREFIX + key));
        defaultColors.keySet().forEach(key -> keys.add(UI_PRODUTOS_COLOR_PREFIX + key));
        Map<String, String> persisted = this.appSettingService.getAllByKeys(keys);

        return new ProdutoListUiConfig(
                this.resolveUiValues(defaultTexts, persisted, UI_PRODUTOS_TEXT_PREFIX),
                this.resolveUiValues(defaultColors, persisted, UI_PRODUTOS_COLOR_PREFIX),
                "/admin/settings?q=ui.admin.produtos"
        );
    }

    private Map<String, String> resolveUiValues(Map<String, String> defaults,
                                                Map<String, String> persisted,
                                                String prefix) {
        LinkedHashMap<String, String> resolved = new LinkedHashMap<>();
        defaults.forEach((key, defaultValue) -> {
            String value = persisted == null ? null : persisted.get(prefix + key);
            String resolvedValue = defaultValue;
            if (value != null) {
                String trimmedValue = value.trim();
                if (!trimmedValue.isEmpty()) {
                    resolvedValue = trimmedValue;
                }
            }
            resolved.put(key, resolvedValue);
        });
        return resolved;
    }

    @SuppressWarnings("java:S107")
    private ProdutoEntity buildPrefilledNovoProduto(
            Long legacyId,
            String nome,
            String descricao,
            String categoria,
            String codigoBarras,
            Integer estoque,
            String fabricante,
            String unidade,
            String origem
    ) {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setTenantId(this.resolveTenantId());
        produto.setLegacyId(legacyId);
        produto.setNome(this.normalize(nome));
        produto.setDescricao(this.normalize(descricao));
        produto.setCodigoBarras(this.normalizeBarcode(codigoBarras));
        produto.setEstoque(estoque == null ? null : Math.max(estoque, 0));
        produto.setFabricante(this.normalize(fabricante));
        produto.setUnidade(this.normalize(unidade));
        produto.setMetodoLeituraCodigoBarras(this.resolveMetodoLeituraOrigem(origem));

        if (!ORIGEM_CATALOGO_NACIONAL.equalsIgnoreCase(this.normalize(origem))) {
            produto.setCategoria(this.normalize(categoria));
        }

        if (produto.getEstoque() != null) {
            produto.setDisponivel(produto.getEstoque() > 0);
        }

        this.applyMedicacaoRules(produto);
        return produto;
    }

    private MetodoLeituraCodigoBarras resolveMetodoLeituraOrigem(String origem) {
        String safeOrigem = this.normalize(origem).toUpperCase(Locale.ROOT);
        return switch (safeOrigem) {
            case "ESTOQUE_FISICO", "CATALOGO_PENDENTE", ORIGEM_CATALOGO_NACIONAL -> MetodoLeituraCodigoBarras.MANUAL;
            case ORIGEM_CATALOGO_LOCAL -> MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA;
            default -> MetodoLeituraCodigoBarras.MANUAL;
        };
    }

    private String resolveRedirectPath(String redirect) {
        String path = this.normalize(redirect);
        if (!StringUtils.hasText(path)) {
            return PATH_PRODUTOS;
        }
        if (!path.startsWith(PATH_PRODUTOS) && !path.startsWith(PATH_ESTOQUE)) {
            return PATH_PRODUTOS;
        }
        return path;
    }

    private String redirectNaoProntosTodos(String q) {
        String termo = this.normalize(q);
        if (termo.isBlank()) {
            return REDIRECT_PREFIX + PATH_PRODUTOS_NAO_PRONTOS_TODOS;
        }
        String encoded = URLEncoder.encode(termo, StandardCharsets.UTF_8);
        return REDIRECT_PREFIX + PATH_PRODUTOS_NAO_PRONTOS_TODOS + "?q=" + encoded;
    }

    private boolean temImagem(ProdutoEntity entity) {
        return entity != null && StringUtils.hasText(entity.getImagem());
    }

    private void vincularImagemGeradaSeNecessario(final ProdutoEntity entity) {
        if (entity == null || entity.getId() == null || this.temImagem(entity)) {
            return;
        }
        try {
            this.productImageJobService
                    .ensureImageLinkedFromLastSuccessfulJob(entity.getId())
                    .ifPresent(entity::setImagem);
        } catch (RuntimeException ex) {
            log.warn("[admin-produto] falha ao vincular imagem IA do produto {}: {}",
                    entity.getId(),
                    ex.getMessage());
        }
    }

    private boolean temPrecoPositivo(ProdutoEntity entity) {
        return entity != null
                && entity.getPrecoVenda() != null
                && entity.getPrecoVenda().compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean temEstoquePositivo(ProdutoEntity entity) {
        return entity != null
                && entity.getEstoque() != null
                && entity.getEstoque() > 0;
    }

    private boolean temCodigoBarrasValido(ProdutoEntity entity) {
        if (entity == null) {
            return false;
        }
        String normalized = BarcodeNormalizer.normalizeOrNull(entity.getCodigoBarras());
        if (normalized == null) {
            return false;
        }
        int length = normalized.length();
        return length == 8 || (length >= 12 && length <= 14);
    }

    private boolean temOrigemPublicavel(ProdutoEntity entity) {
        return ProductSourcePolicy.isLocalSellableSource(entity);
    }

    private boolean podePublicarProdutoDoCatalogoPdf(ProdutoEntity entity) {
        return this.temOrigemPublicavel(entity)
                && this.temCodigoBarrasValido(entity)
                && this.temPrecoPositivo(entity)
                && this.temEstoquePositivo(entity);
    }

    private String resolveMotivoBloqueioPublicacao(ProdutoEntity entity) {
        if (!this.temOrigemPublicavel(entity)) {
            return "Produto precisa ser salvo como cadastro local antes de publicar.";
        }
        if (!this.temCodigoBarrasValido(entity)) {
            return "Produto precisa ter codigo de barras valido antes de publicar.";
        }
        if (!this.temImagem(entity)) {
            return "Produto precisa ter imagem antes de publicar.";
        }
        if (!this.temPrecoPositivo(entity)) {
            return "Produto precisa ter preco de venda positivo antes de publicar.";
        }
        if (!this.temEstoquePositivo(entity)) {
            return "Produto precisa ter estoque positivo antes de publicar.";
        }
        return null;
    }

    private List<ProdutoLookupItem> resolveNaoProntosFromDatabase(String q, int limit) {
        return this.resolveNaoProntosFromDatabase(q, limit, true);
    }

    @SuppressWarnings({"java:S3776", "java:S135"})
    private List<ProdutoLookupItem> resolveNaoProntosFromDatabase(String q, int limit, boolean fallbackToAllWhenEmpty) {
        String termo = this.normalizeQuery(q);
        int safeLimit = Math.clamp(limit, 1, MAX_PENDING_ALL_LIMIT);
        List<ProdutoLookupItem> itens = new ArrayList<>(Math.min(safeLimit, 2_000));
        Set<String> seen = new HashSet<>();
        int page = 0;

        while (itens.size() < safeLimit) {
            int pageSize = Math.min(1000, safeLimit - itens.size());
            Page<ProdutoEntity> result = this.fetchNaoProntosPage(termo, page, pageSize);
            if (result.isEmpty()) {
                break;
            }

            int addedOnPage = 0;
            for (ProdutoEntity entity : result.getContent()) {
                if (entity == null) {
                    continue;
                }
                if (itens.size() >= safeLimit) {
                    break;
                }
                ProdutoLookupItem mapped = this.buildPendingFromDatabase(entity);
                String key = this.pendingUniqueKey(mapped);
                if (!seen.add(key)) {
                    continue;
                }
                itens.add(mapped);
                addedOnPage++;
            }

            if (!result.hasNext()) {
                break;
            }
            if (addedOnPage == 0) {
                break;
            }
            page++;
        }

        List<ProdutoLookupItem> merged = this.mergeNaoProntosWithCsv(termo, itens, safeLimit);
        if (fallbackToAllWhenEmpty && merged.isEmpty() && StringUtils.hasText(termo)) {
            return this.resolveNaoProntosFromDatabase(null, safeLimit, false);
        }
        return merged;
    }

    private NaoProntosSlice fetchNaoProntosSlice(String termo, int page, int size) {
        List<ProdutoLookupItem> allItems = this.resolveNaoProntosFromDatabase(termo, MAX_PENDING_ALL_LIMIT);
        if (allItems.isEmpty()) {
            return new NaoProntosSlice(List.of(), 0, false);
        }

        int from = page * size;
        if (from >= allItems.size()) {
            return new NaoProntosSlice(List.of(), allItems.size(), false);
        }
        int to = Math.min(from + size, allItems.size());
        List<ProdutoLookupItem> content = allItems.subList(from, to);
        return new NaoProntosSlice(content, allItems.size(), to < allItems.size());
    }

    private CatalogoSlice fetchCatalogoSlice(String q, int page, int size) {
        String termo = this.normalizeQuery(q);
        int safePage = Math.max(page, 0);
        int safeSize = Math.clamp(size, 10, 200);
        Page<ProdutoEntity> result = this.fetchCatalogoPage(termo, safePage, safeSize);

        // Mantem a listagem util para o admin mesmo quando o filtro nao retorna itens.
        // Nesse caso exibe a primeira pagina do catalogo completo.
        if (StringUtils.hasText(termo) && result.getTotalElements() == 0L) {
            result = this.fetchCatalogoPage(null, 0, safeSize);
        }

        List<ProdutoLookupItem> items = result.getContent()
                .stream()
                .map(ProdutoLookupItem::from)
                .toList();

        return new CatalogoSlice(
                items,
                result.getTotalElements(),
                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.hasPrevious(),
                result.hasNext()
        );
    }

    private Page<ProdutoEntity> fetchCatalogoPage(String termo, int page, int size) {
        PageRequest req = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        return this.produtoRepository.searchPageByCategoria(termo, null, req);
    }

    private Page<ProdutoEntity> fetchNaoProntosPage(String termo, int page, int size) {
        PageRequest req = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<ProdutoEntity> byCategoria = this.produtoRepository.searchNaoDisponiveisByCategoria(
                termo,
                CATEGORIA_ESTOQUE_FISICO,
                req
        );
        if (byCategoria.hasContent()) {
            return byCategoria;
        }
        return this.produtoRepository.searchNaoDisponiveis(termo, req);
    }

    private ProdutoLookupItem buildPendingFromDatabase(ProdutoEntity produto) {
        return new ProdutoLookupItem(
                produto.getId(),
                produto.getLegacyId(),
                "CATALOGO_PENDENTE",
                produto.getNome(),
                StringUtils.hasText(produto.getDescricao()) ? produto.getDescricao() : produto.getNome(),
                StringUtils.hasText(produto.getCategoria()) ? produto.getCategoria() : CATEGORIA_ESTOQUE_FISICO,
                produto.getCodigoBarras(),
                produto.getPrecoVenda(),
                produto.getPrecoPromocional(),
                produto.getEstoque(),
                produto.getFabricante(),
                produto.getUnidade()
        );
    }

    @SuppressWarnings({"java:S3776", "java:S135"})
    private List<ProdutoLookupItem> mergeNaoProntosWithCsv(String termo,
                                                           List<ProdutoLookupItem> bancoItens,
                                                           int limit) {
        int safeLimit = Math.max(1, limit);
        if (bancoItens.size() >= safeLimit) {
            return bancoItens;
        }

        EstoqueFisicoCsvService csvService = this.estoqueFisicoCsvServiceProvider.getIfAvailable();
        if (csvService == null) {
            return bancoItens;
        }

        List<EstoqueFisicoCsvService.EstoqueItem> csvItems = csvService.search(termo == null ? "" : termo);
        if (csvItems.isEmpty()) {
            return bancoItens;
        }

        List<ProdutoLookupItem> merged = new ArrayList<>(Math.min(safeLimit, bancoItens.size() + csvItems.size()));
        merged.addAll(bancoItens);

        Set<Long> legacyIds = new HashSet<>();
        Set<String> barcodes = new HashSet<>();
        for (ProdutoLookupItem item : bancoItens) {
            if (item == null) {
                continue;
            }
            if (item.legacyId() != null) {
                legacyIds.add(item.legacyId());
            }
            String normalizedBarcode = this.normalizeBarcode(item.codigoBarras());
            if (StringUtils.hasText(normalizedBarcode)) {
                barcodes.add(normalizedBarcode);
            }
        }

        for (EstoqueFisicoCsvService.EstoqueItem csvItem : csvItems) {
            if (merged.size() >= safeLimit || csvItem == null) {
                break;
            }

            Long csvLegacyId = csvItem.legacyId();
            String csvBarcode = this.normalizeBarcode(csvItem.codigoBarras());
            boolean duplicateByLegacy = csvLegacyId != null && legacyIds.contains(csvLegacyId);
            boolean duplicateByBarcode = StringUtils.hasText(csvBarcode) && barcodes.contains(csvBarcode);
            if (duplicateByLegacy || duplicateByBarcode) {
                continue;
            }

            merged.add(this.buildPendingFromCsv(csvItem));
            if (csvLegacyId != null) {
                legacyIds.add(csvLegacyId);
            }
            if (StringUtils.hasText(csvBarcode)) {
                barcodes.add(csvBarcode);
            }
        }

        return merged;
    }

    private ProdutoLookupItem buildPendingFromCsv(EstoqueFisicoCsvService.EstoqueItem csvItem) {
        String nome = StringUtils.hasText(csvItem.nome()) ? csvItem.nome() : "Produto do estoque fisico";
        Integer estoque = csvItem.estoque() == null ? 0 : Math.max(0, csvItem.estoque());

        return new ProdutoLookupItem(
                null,
                csvItem.legacyId(),
                "ESTOQUE_FISICO",
                nome,
                nome,
                CATEGORIA_ESTOQUE_FISICO,
                csvItem.codigoBarras(),
                csvItem.precoVenda(),
                null,
                estoque,
                csvItem.fabricante(),
                null
        );
    }

    private String pendingUniqueKey(ProdutoLookupItem item) {
        if (item.id() != null) {
            return "ID:" + item.id();
        }
        if (item.legacyId() != null) {
            return "L:" + item.legacyId();
        }
        String normalizedBarcode = this.normalizeBarcode(item.codigoBarras());
        if (StringUtils.hasText(normalizedBarcode)) {
            return "B:" + normalizedBarcode;
        }
        return "N:" + this.normalize(item.nome()).toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings({"java:S3776", "java:S135"})
    private List<ProdutoLegacyEntity> buscarNoCatalogoNacional(
            ProdutoLegacyRepository legacyRepository,
            String termo,
            int limite
    ) {
        List<ProdutoLegacyEntity> itens = new ArrayList<>();
        String termoLimpo = this.normalize(termo);
        if (termoLimpo.isBlank()) {
            return itens;
        }

        String termoDigits = termoLimpo.replaceAll("\\D+", "");
        if (!termoDigits.isBlank() && termoDigits.length() >= 8) {
            legacyRepository.findByCodigoBarras(termoDigits).ifPresent(itens::add);
        }

        for (ProdutoLegacyEntity entity : legacyRepository.findByNomeContainingIgnoreCase(termoLimpo)) {
            if (entity == null || entity.getId() == null) {
                continue;
            }
            itens.add(entity);
            if (itens.size() >= Math.max(limite * 2, 25)) {
                break;
            }
        }

        LinkedHashMap<Integer, ProdutoLegacyEntity> dedupe = new LinkedHashMap<>();
        for (ProdutoLegacyEntity item : itens) {
            if (item != null && item.getId() != null) {
                dedupe.putIfAbsent(item.getId(), item);
            }
        }
        return dedupe.values().stream().limit(Math.max(limite, 1)).toList();
    }

    public static record ProdutoLookupItem(
            Long id,
            Long legacyId,
            String origem,
            String nome,
            String descricao,
            String categoria,
            String codigoBarras,
            BigDecimal precoVenda,
            BigDecimal precoPromocional,
            Integer estoque,
            String fabricante,
            String unidade
    ) {
        static ProdutoLookupItem from(ProdutoEntity p) {
            String origem = p.getMetodoLeituraCodigoBarras() == MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA
                    ? ORIGEM_CATALOGO_LOCAL
                    : "CATALOGO";
            String categoria = p.getCategoria();
            if (origem.equals(ORIGEM_CATALOGO_LOCAL) && !StringUtils.hasText(categoria)) {
                categoria = CATEGORIA_CATALOGO_LOCAL;
            }
            return new ProdutoLookupItem(
                    p.getId(),
                    p.getLegacyId(),
                    origem,
                    p.getNome(),
                    p.getDescricao(),
                    categoria,
                    p.getCodigoBarras(),
                    p.getPrecoVenda(),
                    p.getPrecoPromocional(),
                    p.getEstoque(),
                    p.getFabricante(),
                    p.getUnidade()
            );
        }

        static ProdutoLookupItem fromLegacy(ProdutoLegacyEntity p) {
            String codigo = p.getCodigoBarras() == null ? "" : p.getCodigoBarras().replaceAll("\\D+", "");
            String nome = p.getNome() == null || p.getNome().isBlank() ? "Produto do catalogo nacional" : p.getNome().trim();
            String descricao = p.getApresentacao() == null ? "" : p.getApresentacao().trim();
            String unidade = p.getApresentacao() == null ? "" : p.getApresentacao().trim();

            return new ProdutoLookupItem(
                    null,
                    p.getId() == null ? null : p.getId().longValue(),
                    ORIGEM_CATALOGO_NACIONAL,
                    nome,
                    descricao,
                    CATEGORIA_CATALOGO_NACIONAL,
                    codigo,
                    p.getPrecoVenda(),
                    p.getPrecoPromocao(),
                    null,
                    null,
                    unidade
            );
        }
    }

    public static record ProdutoLookupPage(
            List<ProdutoLookupItem> items,
            int page,
            int size,
            int total,
            boolean hasNext
    ) {
    }

    private record NaoProntosSlice(
            List<ProdutoLookupItem> items,
            int total,
            boolean hasNext
    ) {
    }

    private record CatalogoSlice(
            List<ProdutoLookupItem> items,
            long total,
            int page,
            int size,
            int totalPages,
            boolean hasPrev,
            boolean hasNext
    ) {
    }

    public record ProductListSummary(
            long semEstoque,
            long estoqueBaixo,
            long disponiveis
    ) {
    }

    public record ProdutoListUiConfig(
            Map<String, String> texts,
            Map<String, String> colors,
            String settingsHref
    ) {
    }

    @SuppressWarnings("java:S3776")
    private ProdutoEntity resolveProdutoParaCatalogoPdf(
            CatalogoPdfPreviewItem item,
            Map<Long, ProdutoEntity> cacheLegacy,
            Map<String, ProdutoEntity> cacheCodigo
    ) {
        String codigo = this.normalize(item.codigoBarras());
        Long tenantId = this.resolveTenantId();
        if (!codigo.isBlank()) {
            ProdutoEntity cachedByCodigo = cacheCodigo.get(codigo);
            if (cachedByCodigo != null) {
                return cachedByCodigo;
            }
            Optional<ProdutoEntity> byCodigo = tenantId == null
                    ? this.produtoRepository.findByAnyCodigo(codigo)
                    : this.produtoRepository.findByAnyCodigo(tenantId, codigo);
            if (byCodigo.isPresent()) {
                ProdutoEntity found = byCodigo.get();
                cacheCodigo.put(codigo, found);
                if (found.getLegacyId() != null) {
                    cacheLegacy.putIfAbsent(found.getLegacyId(), found);
                }
                return found;
            }
        }

        if (item.legacyId() != null) {
            ProdutoEntity cachedByLegacy = cacheLegacy.get(item.legacyId());
            if (cachedByLegacy != null) {
                return cachedByLegacy;
            }
            Optional<ProdutoEntity> byLegacy = tenantId == null
                    ? this.produtoRepository.findByLegacyId(item.legacyId())
                    : this.produtoRepository.findByTenantIdAndLegacyId(tenantId, item.legacyId());
            if (byLegacy.isPresent()) {
                ProdutoEntity found = byLegacy.get();
                cacheLegacy.put(item.legacyId(), found);
                String foundCodigo = this.normalize(found.getCodigoBarras());
                if (!foundCodigo.isBlank()) {
                    cacheCodigo.putIfAbsent(foundCodigo, found);
                }
                return found;
            }
        }

        ProdutoEntity entity = new ProdutoEntity();
        entity.setTenantId(tenantId);
        return entity;
    }

    @SuppressWarnings("unchecked")
    private List<CatalogoPdfPreviewItem> getCatalogoPdfPreview(HttpSession session) {
        if (session == null) {
            return List.of();
        }
        Object raw = session.getAttribute(SESSION_CATALOGO_PDF_PREVIEW);
        if (raw instanceof List<?> list) {
            return (List<CatalogoPdfPreviewItem>) list;
        }
        return List.of();
    }

    private List<CatalogoPdfPreviewItem> parseCatalogoPdf(MultipartFile arquivoPdf) throws IOException {
        byte[] bytes = arquivoPdf.getBytes();
        try (PdfReader reader = new PdfReader(bytes)) {
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            List<String> rawLines = new ArrayList<>();
            int pages = reader.getNumberOfPages();
            for (int page = 1; page <= pages; page++) {
                String pageText = extractor.getTextFromPage(page);
                String[] lines = pageText.split("\\R");
                for (String rawLine : lines) {
                    rawLines.add(rawLine);
                }
            }
            return this.parseCatalogoPdfLines(rawLines);
        }
    }

    private List<CatalogoPdfPreviewItem> parseCatalogoPdfLines(List<String> rawLines) {
        LinkedHashMap<String, CatalogoPdfPreviewItem> dedupe = new LinkedHashMap<>();
        StringBuilder pendingName = new StringBuilder();
        String pendingBarcode = "";
        Long pendingLegacyId = null;
        boolean insideTable = false;

        for (String rawLine : rawLines) {
            String line = this.normalizeCatalogoPdfLine(rawLine);
            String upper = line.toUpperCase(Locale.ROOT);
            if (this.isCatalogoPdfHeaderLine(upper)) {
                insideTable = true;
                pendingName.setLength(0);
                pendingBarcode = "";
                pendingLegacyId = null;
                continue;
            }
            if (this.isCatalogoPdfFooterLine(upper)) {
                pendingName.setLength(0);
                pendingBarcode = "";
                pendingLegacyId = null;
                continue;
            }
            if (!insideTable || line.isBlank()) {
                continue;
            }

            Matcher barcodeLegacyMatcher = CATALOGO_PDF_BARCODE_LEGACY_PATTERN.matcher(line);
            if (barcodeLegacyMatcher.matches()) {
                String normalizedBarcode = this.normalizeBarcode(barcodeLegacyMatcher.group(1));
                if (!normalizedBarcode.isBlank()) {
                    pendingBarcode = normalizedBarcode;
                    pendingLegacyId = Long.parseLong(barcodeLegacyMatcher.group(2));
                    pendingName.setLength(0);
                }
                continue;
            }

            CatalogoPdfPreviewItem itemFromBlock = this.parseCatalogoPdfDataLine(
                    line,
                    pendingBarcode,
                    pendingLegacyId,
                    pendingName.toString()
            );
            if (itemFromBlock != null) {
                this.putCatalogoPdfItem(dedupe, itemFromBlock);
                pendingName.setLength(0);
                pendingBarcode = "";
                pendingLegacyId = null;
                continue;
            }

            CatalogoPdfPreviewItem item = this.parseCatalogoPdfDataLine(line, pendingName.toString());
            if (item != null) {
                this.putCatalogoPdfItem(dedupe, item);
                pendingName.setLength(0);
                pendingBarcode = "";
                pendingLegacyId = null;
                continue;
            }

            String description = this.extractCatalogoPdfDescription(rawLine);
            if (!description.isBlank()) {
                if (this.hasCatalogoPdfColumnGap(rawLine)) {
                    pendingName.setLength(0);
                } else if (pendingName.isEmpty() && this.normalize(pendingBarcode).isBlank()) {
                    continue;
                } else {
                    pendingName.append(' ');
                }
                pendingName.append(description);
            }
        }

        return dedupe.values().stream().limit(10_000).toList();
    }

    private void putCatalogoPdfItem(LinkedHashMap<String, CatalogoPdfPreviewItem> dedupe, CatalogoPdfPreviewItem item) {
        String key = this.buildCatalogoPdfKey(item);
        CatalogoPdfPreviewItem previous = dedupe.get(key);
        if (previous == null) {
            dedupe.put(key, item);
        } else {
            dedupe.put(key, previous.withQuantidade(previous.quantidade() + item.quantidade()));
        }
    }

    private String buildCatalogoPdfKey(CatalogoPdfPreviewItem item) {
        if (!this.normalize(item.codigoBarras()).isBlank()) {
            return "B:" + item.codigoBarras();
        }
        if (item.legacyId() != null) {
            return "L:" + item.legacyId();
        }
        return "N:" + this.normalize(item.nome()).toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings({"java:S3776", "java:S6541", "java:S135"})
    private CatalogoPdfPreviewItem parseCatalogoPdfLine(String rawLine) {
        return this.parseCatalogoPdfDataLine(this.normalizeCatalogoPdfLine(rawLine), "");
    }

    private CatalogoPdfPreviewItem parseCatalogoPdfDataLine(String line, String pendingName) {
        Matcher barcodeMatcher = BARCODE_PATTERN.matcher(line);
        String barcode = "";
        int barcodeStart = -1;
        int barcodeEnd = -1;
        while (barcodeMatcher.find()) {
            String candidate = barcodeMatcher.group();
            String normalized = this.normalizeBarcode(candidate);
            if (!normalized.isBlank()) {
                barcode = normalized;
                barcodeStart = barcodeMatcher.start();
                barcodeEnd = barcodeMatcher.end();
                break;
            }
        }
        if (barcode.isBlank()) {
            return null;
        }

        Long legacyId = null;
        Matcher integerMatcher = INTEGER_PATTERN.matcher(line.substring(barcodeEnd));
        if (integerMatcher.find()) {
            String firstInteger = integerMatcher.group();
            try {
                legacyId = Long.parseLong(firstInteger);
            } catch (NumberFormatException ignored) {
                legacyId = null;
            }
        }
        if (legacyId == null) {
            return null;
        }

        int afterLegacyStart = barcodeEnd + integerMatcher.end();
        Matcher ncmMatcher = CATALOGO_PDF_NCM_PATTERN.matcher(line);
        if (!ncmMatcher.find(afterLegacyStart)) {
            return null;
        }

        int quantidade = 0;
        Matcher qtdMatcher = INTEGER_PATTERN.matcher(line);
        if (qtdMatcher.find(ncmMatcher.end())) {
            try {
                quantidade = Integer.parseInt(qtdMatcher.group());
            } catch (NumberFormatException ex) {
                quantidade = 0;
            }
        }
        if (quantidade <= 0 || quantidade > 50_000) {
            return null;
        }

        BigDecimal precoVenda = null;
        Matcher moneyMatcher = CATALOGO_PDF_MONEY_PATTERN.matcher(line);
        while (moneyMatcher.find()) {
            String moneyRaw = moneyMatcher.group(1);
            String normalized = moneyRaw.replace(".", "").replace(",", ".");
            try {
                BigDecimal money = new BigDecimal(normalized);
                if (money.compareTo(BigDecimal.ZERO) > 0) {
                    precoVenda = money;
                    break;
                }
            } catch (NumberFormatException ignored) {
                precoVenda = null;
            }
        }

        boolean hasPendingName = !this.normalize(pendingName).isBlank();
        String nome = this.normalize(pendingName);
        if (!hasPendingName) {
            nome = line;
            if (barcodeStart >= 0 && barcodeEnd > barcodeStart) {
                nome = (line.substring(0, barcodeStart) + " " + line.substring(barcodeEnd)).trim();
            }
        }
        if (hasPendingName) {
            nome = nome.replaceAll("\\s+", " ").trim();
        } else {
            nome = nome.replaceAll("\\b\\d{1,8}\\b", " ")
                    .replaceAll("R\\$\\s*\\d{1,6}[\\.,]\\d{2}", " ")
                    .replaceAll("\\bST\\b|\\b\\d{1,2}%\\b", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
        }
        if (nome.isBlank() || nome.length() < 3) {
            nome = "Produto importado do PDF";
        }

        return new CatalogoPdfPreviewItem(
                legacyId,
                barcode,
                nome,
                quantidade,
                precoVenda
        );
    }

    private CatalogoPdfPreviewItem parseCatalogoPdfDataLine(
            String line,
            String pendingBarcode,
            Long pendingLegacyId,
            String pendingName
    ) {
        if (this.normalize(pendingBarcode).isBlank() || pendingLegacyId == null) {
            return null;
        }

        Matcher ncmMatcher = CATALOGO_PDF_NCM_PATTERN.matcher(line);
        if (!ncmMatcher.find()) {
            return null;
        }

        int quantidade = 0;
        Matcher qtdMatcher = INTEGER_PATTERN.matcher(line);
        if (qtdMatcher.find(ncmMatcher.end())) {
            try {
                quantidade = Integer.parseInt(qtdMatcher.group());
            } catch (NumberFormatException ex) {
                quantidade = 0;
            }
        }
        if (quantidade <= 0 || quantidade > 50_000) {
            return null;
        }

        BigDecimal precoVenda = this.extractCatalogoPdfFirstPositiveMoney(line);
        String nome = this.normalizeCatalogoPdfName(pendingName);
        if (nome.isBlank() || nome.length() < 3) {
            nome = "Produto importado do PDF";
        }

        return new CatalogoPdfPreviewItem(
                pendingLegacyId,
                pendingBarcode,
                nome,
                quantidade,
                precoVenda
        );
    }

    private BigDecimal extractCatalogoPdfFirstPositiveMoney(String line) {
        Matcher moneyMatcher = CATALOGO_PDF_MONEY_PATTERN.matcher(line);
        while (moneyMatcher.find()) {
            String moneyRaw = moneyMatcher.group(1);
            String normalized = moneyRaw.replace(".", "").replace(",", ".");
            try {
                BigDecimal money = new BigDecimal(normalized);
                if (money.compareTo(BigDecimal.ZERO) > 0) {
                    return money;
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String normalizeCatalogoPdfName(String value) {
        return this.normalize(value)
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String normalizeCatalogoPdfLine(String rawLine) {
        return this.normalize(rawLine).replace('\u00a0', ' ').replaceAll("\\s+", " ").trim();
    }

    private boolean isCatalogoPdfHeaderLine(String upperLine) {
        return upperLine.contains("BARRAS") && upperLine.contains("PRODUTO");
    }

    private boolean isCatalogoPdfFooterLine(String upperLine) {
        return upperLine.startsWith("SUB-TOTAL")
                || upperLine.startsWith("SUBTOTAL")
                || upperLine.startsWith("TOTAL")
                || upperLine.startsWith("GERADO POR")
                || upperLine.matches("\\d{2}/\\d{2}/\\d{4}.*\\b\\d+\\s+DE\\s+\\d+\\b");
    }

    private String extractCatalogoPdfDescription(String rawLine) {
        String original = rawLine == null ? "" : rawLine.replace('\u00a0', ' ');
        String line = this.normalizeCatalogoPdfLine(original);
        String upper = line.toUpperCase(Locale.ROOT);
        if (line.length() < 3
                || this.isCatalogoPdfHeaderLine(upper)
                || this.isCatalogoPdfFooterLine(upper)
                || (upper.contains("PROMO") && upper.contains("COMPRA"))
                || BARCODE_PATTERN.matcher(line).find()
                || CATALOGO_PDF_MONEY_PATTERN.matcher(line).find()
                || !LETTER_PATTERN.matcher(line).find()) {
            return "";
        }

        String[] blocks = original.trim().split("\\s{2,}");
        String candidate = blocks.length == 0 ? line : blocks[0];
        if (candidate.length() > 80) {
            candidate = candidate.substring(0, 80);
        }
        return candidate.replaceAll("\\s+", " ").trim();
    }

    private boolean hasCatalogoPdfColumnGap(String rawLine) {
        String original = rawLine == null ? "" : rawLine.replace('\u00a0', ' ');
        return original.trim().split("\\s{2,}").length > 1;
    }

    private record CatalogoPdfPreviewItem(
            Long legacyId,
            String codigoBarras,
            String nome,
            int quantidade,
            BigDecimal precoVenda
    ) implements Serializable {
        CatalogoPdfPreviewItem withQuantidade(int novaQuantidade) {
            return new CatalogoPdfPreviewItem(this.legacyId, this.codigoBarras, this.nome, novaQuantidade, this.precoVenda);
        }
    }

    private void cleanupProductImages(final List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        imageUrls.stream()
                .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                .forEach(imageStorageService::deleteProductImageByUrl);
    }

    private Long resolveTenantId() {
        return this.tenantResolverService == null ? null : this.tenantResolverService.resolveDefaultTenantId();
    }

    @Generated
    @SuppressWarnings("java:S107")
    public ProdutoAdminPageController(ProdutoAdminService adminService,
                                      ProdutoRepository produtoRepository,
                                      ProdutoCategoriaRepository categoriaRepository,
                                      ImageStorageService imageStorageService,
                                      ProductCategoryBindingService categoryBindingService,
                                      ProductImageJobService productImageJobService,
                                      AppSettingService appSettingService,
                                      ObjectProvider<CatalogoVendaDisponivelService> catalogoVendaDisponivelServiceProvider,
                                      ObjectProvider<EstoqueFisicoCsvService> estoqueFisicoCsvServiceProvider,
                                      ObjectProvider<EstoqueFisicoImportService> estoqueImportServiceProvider,
                                      ObjectProvider<ProdutoLegacyRepository> legacyRepositoryProvider,
                                      ObjectProvider<SincronizacaoCatalogoService> catalogSyncProvider) {
        this.adminService = adminService;
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.imageStorageService = imageStorageService;
        this.categoryBindingService = categoryBindingService;
        this.productImageJobService = productImageJobService;
        this.appSettingService = appSettingService;
        this.catalogoVendaDisponivelServiceProvider = catalogoVendaDisponivelServiceProvider;
        this.estoqueFisicoCsvServiceProvider = estoqueFisicoCsvServiceProvider;
        this.estoqueImportServiceProvider = estoqueImportServiceProvider;
        this.legacyRepositoryProvider = legacyRepositoryProvider;
        this.catalogSyncProvider = catalogSyncProvider;
    }
}
