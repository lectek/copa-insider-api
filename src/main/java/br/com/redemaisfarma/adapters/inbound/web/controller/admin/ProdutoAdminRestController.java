package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import com.fasterxml.jackson.databind.JsonNode;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.core.tenant.TenantFeature;
import br.com.redemaisfarma.application.core.tenant.TenantFeatureGateService;
import br.com.redemaisfarma.application.core.tenant.TenantResolverService;
import br.com.redemaisfarma.application.core.media.ImageStorageService;
import br.com.redemaisfarma.application.dto.request.AdminProdutoRequestDTO;
import br.com.redemaisfarma.application.dto.response.ProdutoResponseDTO;
import br.com.redemaisfarma.application.mapper.ProdutoMapper;
import br.com.redemaisfarma.application.service.ProductCategoryBindingService;
import br.com.redemaisfarma.domain.Produto;
import br.com.redemaisfarma.domain.support.BarcodeNormalizer;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/produtos")
public class ProdutoAdminRestController {

    /**
     * Product prefix used when deriving public UUID from entity id.
     */
    private static final String PUBLIC_ID_PREFIX = "produto:";

    /**
     * Number of years used for synthetic validity in response.
     */
    private static final int DEFAULT_VALIDADE_YEARS = 1;

    /**
     * Conflict message when trying to validate an already published product.
     */
    private static final String VALIDAR_STATUS_CONFLICT =
            "Produto publicado nao pode voltar para VALIDADO.";

    /**
     * Conflict message when trying to publish a non-validated product.
     */
    private static final String PUBLICAR_STATUS_CONFLICT =
            "Produto deve estar VALIDADO antes de PUBLICAR.";

    /**
     * Validation message for duplicate names.
     */
    private static final String DUPLICATE_NAME_MESSAGE =
            "Produto com este nome já existe – pesquise antes de criar.";

    /**
     * Repository used for product persistence.
     */
    private final ProdutoJpaRepository repo;

    /**
     * Service used for image upload.
     */
    private final ImageStorageService imageStorageService;
    private final ProductCategoryBindingService categoryBindingService;
    private final Validator validator;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;
    @Autowired(required = false)
    private TenantFeatureGateService tenantFeatureGateService;

    /**
     * Creates controller with dependencies.
     *
     * @param repository product repository
     * @param storageService image storage service
     */
    public ProdutoAdminRestController(
            final ProdutoJpaRepository repository,
            final ImageStorageService storageService,
            final ProductCategoryBindingService categoryBindingService,
            final Validator validator
    ) {
        this.repo = repository;
        this.imageStorageService = storageService;
        this.categoryBindingService = categoryBindingService;
        this.validator = validator;
    }

    /**
     * Lists products using optional query and pagination.
     *
     * @param q optional text query
     * @param pageable page request
     * @return page of product responses
     */
    @GetMapping
    public Page<ProdutoResponseDTO> list(
            @RequestParam(name = "q", required = false) final String q,
            final Pageable pageable
    ) {
        final Long tenantId = resolveTenantId();
        final Page<ProdutoEntity> page = tenantId == null
                ? repo.searchPage(q, pageable)
                : repo.searchPage(tenantId, q, pageable);
        return page.map(this::toResponse);
    }

    /**
     * Gets one product by id.
     *
     * @param id product id
     * @return product payload or 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> get(
            @PathVariable("id") final Long id
    ) {
        final Long tenantId = resolveTenantId();
        final var produto = tenantId == null ? repo.findById(id) : repo.findByScopedId(tenantId, id);
        return produto
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new product.
     *
     * @param dto request payload
     * @return created product response
     */
    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> create(
            @RequestBody final JsonNode body
    ) {
        final ParsedAdminProdutoRequest request = parseRequest(body);
        final AdminProdutoRequestDTO dto = request.dto();
        validateRequest(dto);
        primePrimaryImageFromGallery(dto);
        ensureImageWhenActive(dto);
        ensureBarcodeWhenActive(dto);
        ensureStockWhenActive(dto);
        ensureUniqueName(dto, null);

        final ProdutoEntity entity = ProdutoMapper.toEntity(toDomain(dto));
        entity.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.API);
        entity.setStatus(ProdutoStatus.IMPORTADO);
        entity.setDataImportacao(LocalDateTime.now());
        applyMedicacaoRules(entity);
        applyFiscalRules(entity, dto);
        entity.setAlertaEstoqueLimite(sanitizeAlertaEstoqueLimite(dto.getAlertaEstoqueLimite()));
        final Long tenantId = resolveTenantId();
        if (tenantId != null) {
            entity.setTenantId(tenantId);
        }
        applyImageGallery(entity, request, null);
        categoryBindingService.bind(entity);
        ensureImageWhenActive(entity);
        ensureBarcodeWhenActive(entity);
        ensureStockWhenActive(entity);

        final ProdutoEntity salvo = repo.save(entity);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(salvo));
    }

    /**
     * Updates one product.
     *
     * @param id product id
     * @param dto request payload
     * @return updated product response or 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> update(
            @PathVariable("id") final Long id,
            @RequestBody final JsonNode body
    ) {
        final ParsedAdminProdutoRequest request = parseRequest(body);
        final AdminProdutoRequestDTO dto = request.dto();
        validateRequest(dto);
        primePrimaryImageFromGallery(dto);
        ensureImageWhenActive(dto);
        ensureBarcodeWhenActive(dto);
        ensureStockWhenActive(dto);
        ensureUniqueName(dto, id);

        final Long tenantId = resolveTenantId();
        final var atualOpt = tenantId == null ? repo.findById(id) : repo.findByScopedId(tenantId, id);
        return atualOpt
                .map(atual -> {
                    final String imagemAnterior = atual.getImagem();
                    final Produto src = toDomain(dto);
                    ProdutoMapper.updateEntity(atual, src);
                    if (dto.getCodigoBarras() != null
                            && !dto.getCodigoBarras().isBlank()) {
                        atual.setMetodoLeituraCodigoBarras(
                                MetodoLeituraCodigoBarras.API
                        );
                    }
                    applyMedicacaoRules(atual);
                    applyFiscalRules(atual, dto);
                    atual.setAlertaEstoqueLimite(sanitizeAlertaEstoqueLimite(dto.getAlertaEstoqueLimite()));
                    applyImageGallery(atual, request, imagemAnterior);
                    categoryBindingService.bind(atual);
                    ensureImageWhenActive(atual);
                    ensureBarcodeWhenActive(atual);
                    ensureStockWhenActive(atual);
                    atual.setUpdatedAt(LocalDateTime.now());
                    final ProdutoEntity salvo = repo.save(atual);
                    return ResponseEntity.ok(toResponse(salvo));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes one product.
     *
     * @param id product id
     * @return empty response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final Long id) {
        final Long tenantId = resolveTenantId();
        if (tenantId == null) {
            if (!repo.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            repo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        final var opt = repo.findByScopedId(tenantId, id);
        if (opt.isPresent()) {
            repo.delete(opt.get());
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Uploads image for one product.
     *
     * @param id product id
     * @param file image file
     * @return image URL or error
     */
    @PostMapping(value = "/{id}/imagem", consumes = "multipart/form-data")
    public ResponseEntity<Object> uploadImage(
            @PathVariable("id") final Long id,
            @RequestParam("file") final MultipartFile file
    ) {
        final Long tenantId = resolveTenantId();
        final var produto = tenantId == null ? repo.findById(id) : repo.findByScopedId(tenantId, id);
        return produto
                .<ResponseEntity<Object>>map(entity -> {
                    if (file == null || file.isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body((Object) "Arquivo vazio");
                    }
                    String imageUrl = null;
                    try {
                        imageUrl = imageStorageService.saveProductImage(id, file);
                        entity.addImagemProduto(imageUrl);
                        entity.definirImagemPrincipal(imageUrl);
                        entity.setUpdatedAt(LocalDateTime.now());
                        final ProdutoEntity salvo = repo.save(entity);
                        return ResponseEntity.ok((Object) toResponse(salvo));
                    } catch (IOException ex) {
                        cleanupUploadedProductImage(imageUrl);
                        return ResponseEntity.badRequest()
                                .body((Object) ex.getMessage());
                    } catch (RuntimeException ex) {
                        cleanupUploadedProductImage(imageUrl);
                        throw ex;
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }

    @PostMapping(value = "/{id}/imagens", consumes = "multipart/form-data")
    public ResponseEntity<Object> uploadImages(
            @PathVariable("id") final Long id,
            @RequestParam("file") final List<MultipartFile> files
    ) {
        return repo.findById(id)
                .<ResponseEntity<Object>>map(entity -> {
                    final List<MultipartFile> validFiles = files == null
                            ? List.of()
                            : files.stream()
                                    .filter(file -> file != null && !file.isEmpty())
                                    .toList();
                    if (validFiles.isEmpty()) {
                        return ResponseEntity.badRequest().body((Object) "Arquivo vazio");
                    }
                    final boolean hasPrimaryImage = entity.getImagem() != null
                            && !entity.getImagem().isBlank();
                    final List<String> uploadedUrls = new ArrayList<>();
                    try {
                        for (MultipartFile file : validFiles) {
                            final String imageUrl = imageStorageService
                                    .saveProductImage(id, file);
                            entity.addImagemProduto(imageUrl);
                            uploadedUrls.add(imageUrl);
                        }
                        if (!hasPrimaryImage && !uploadedUrls.isEmpty()) {
                            entity.definirImagemPrincipal(uploadedUrls.getFirst());
                        }
                        entity.setUpdatedAt(LocalDateTime.now());
                        final ProdutoEntity salvo = repo.save(entity);
                        return ResponseEntity.ok((Object) toResponse(salvo));
                    } catch (IOException ex) {
                        cleanupUploadedProductImages(uploadedUrls);
                        return ResponseEntity.badRequest().body((Object) ex.getMessage());
                    } catch (RuntimeException ex) {
                        cleanupUploadedProductImages(uploadedUrls);
                        throw ex;
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Marks product as validated.
     *
     * @param id product id
     * @param validador validator name
     * @return updated product response or 404
     */
    @PostMapping("/{id}/validar")
    public ResponseEntity<ProdutoResponseDTO> validar(
            @PathVariable("id") final Long id,
            @RequestParam("validador") final String validador
    ) {
        return repo.findById(id)
                .map(entity -> {
                    if (entity.getStatus() == ProdutoStatus.PUBLICADO) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                VALIDAR_STATUS_CONFLICT
                        );
                    }
                    entity.setStatus(ProdutoStatus.VALIDADO);
                    entity.setValidador(validador);
                    entity.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(toResponse(repo.save(entity)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Publishes product when it is already validated.
     *
     * @param id product id
     * @param validador validator name
     * @return updated product response or 404
     */
    @PostMapping("/{id}/publicar")
    public ResponseEntity<ProdutoResponseDTO> publicar(
            @PathVariable("id") final Long id,
            @RequestParam("validador") final String validador
    ) {
        return repo.findById(id)
                .map(entity -> {
                    if (entity.getStatus() != ProdutoStatus.VALIDADO) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                PUBLICAR_STATUS_CONFLICT
                        );
                    }
                    ensureBarcodeWhenPublishing(entity);
                    ensureStockWhenPublishing(entity);
                    entity.setStatus(ProdutoStatus.PUBLICADO);
                    entity.setDisponivel(Boolean.TRUE);
                    entity.setValidador(validador);
                    entity.setPublicadoEm(LocalDateTime.now());
                    entity.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(toResponse(repo.save(entity)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Converts request DTO to domain object.
     *
     * @param dto request payload
     * @return domain object
     */
    private Produto toDomain(final AdminProdutoRequestDTO dto) {
        final Produto domain = new Produto();
        domain.setId(null);
        domain.setNome(dto.getNome());
        domain.setDescricao(dto.getDescricao());
        domain.setPrecoVenda(dto.getPreco());
        domain.setImagem(dto.getImagem());
        domain.setCategoria(dto.getCategoria());
        domain.setCodigoBarras(dto.getCodigoBarras());
        domain.setPrecoCusto(null);
        domain.setEstoque(dto.getEstoque());
        domain.setDisponivel(dto.getAtivo());
        domain.setFabricante(null);
        domain.setCodigoOriginal(null);
        domain.setUnidade(null);
        domain.setDataCadastro(LocalDateTime.now());
        return domain;
    }

    /**
     * Converts entity to response DTO.
     *
     * @param entity product entity
     * @return response DTO
     */
    private ProdutoResponseDTO toResponse(final ProdutoEntity entity) {
        final ProdutoResponseDTO dto = new ProdutoResponseDTO();

        final UUID publicId = entity.getId() != null
                ? UUID.nameUUIDFromBytes(
                        (PUBLIC_ID_PREFIX + entity.getId()).getBytes()
                )
                : UUID.randomUUID();

        dto.setEntityId(entity.getId());
        dto.setId(publicId);
        dto.setNome(nvl(entity.getNome(), "Produto"));
        dto.setDescricao(nvl(entity.getDescricao(), ""));
        dto.setPreco(entity.getPrecoVenda());
        dto.setImagem(entity.getImagem());
        dto.setImagens(entity.getImagensProduto());
        dto.setCategoria(entity.getCategoria());
        dto.setEstoqueAtual(entity.getEstoque());
        dto.setAlertaEstoqueLimite(entity.getAlertaEstoqueLimite());
        dto.setValidade(LocalDate.now().plusYears(DEFAULT_VALIDADE_YEARS));
        dto.setCodigoBarras(entity.getCodigoBarras());
        dto.setMarca(entity.getFabricante());
        dto.setFornecedor(null);
        dto.setQuantidadeVendida(null);
        dto.setDataCadastro(
                entity.getDataCadastro() != null
                        ? entity.getDataCadastro().atStartOfDay()
                        : null
        );
        dto.setDataAtualizacao(entity.getUpdatedAt());
        final boolean produtoDestaque =
                Boolean.TRUE.equals(entity.getDestaqueCarrossel());
        dto.setProdutoDestaque(produtoDestaque);
        dto.setProdutoRecomendadoIA(Boolean.FALSE);
        dto.setProdutoControlado(Boolean.TRUE.equals(entity.getExigeReceita()));
        dto.setAvaliacaoMedia(null);
        dto.setTags(null);

        final boolean ativo = Boolean.TRUE.equals(entity.getDisponivel())
                && entity.getPrecoVenda() != null
                && entity.getPrecoVenda().signum() > 0
                && entity.getEstoque() != null
                && entity.getEstoque() > 0;

        dto.setSituacao(
                ativo
                        ? ProdutoResponseDTO.SituacaoProduto.ATIVO
                        : ProdutoResponseDTO.SituacaoProduto.ESGOTADO
        );

        return dto;
    }

    private void applyMedicacaoRules(final ProdutoEntity entity) {
        if (isReceitaControladaEnabled()) {
            return;
        }
        entity.setTarjaMedicacao(null);
        entity.setExigeReceita(Boolean.FALSE);
    }

    private boolean isReceitaControladaEnabled() {
        if (tenantFeatureGateService == null) {
            return false;
        }
        return tenantFeatureGateService.isEnabledForCurrentTenant(
                TenantFeature.MOD_RECEITA_CONTROLADA,
                false
        );
    }

    private void applyFiscalRules(
            final ProdutoEntity entity,
            final AdminProdutoRequestDTO dto
    ) {
        entity.setFiscalNcm(normalizeDigits(dto.getFiscalNcm()));
        entity.setFiscalCest(normalizeDigits(dto.getFiscalCest()));
        entity.setFiscalCfop(normalizeDigits(dto.getFiscalCfop()));
        entity.setFiscalOrigem(dto.getFiscalOrigem());
        entity.setFiscalIcmsCst(normalizeDigits(dto.getFiscalIcmsCst()));
        entity.setFiscalCsosn(normalizeDigits(dto.getFiscalCsosn()));
        entity.setFiscalPisCst(normalizeDigits(dto.getFiscalPisCst()));
        entity.setFiscalCofinsCst(normalizeDigits(dto.getFiscalCofinsCst()));
    }

    /**
     * Validates image requirement for active products.
     *
     * @param dto request payload
     */
    private void ensureImageWhenActive(final AdminProdutoRequestDTO dto) {
        if (Boolean.TRUE.equals(dto.getAtivo())
                && (dto.getImagem() == null || dto.getImagem().isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Imagem obrigatória para produtos disponibilizados na web."
            );
        }
    }

    private void ensureBarcodeWhenActive(final AdminProdutoRequestDTO dto) {
        if (Boolean.TRUE.equals(dto.getAtivo())
                && !hasValidBarcode(dto.getCodigoBarras())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Codigo de barras obrigatorio para produtos disponibilizados na web."
            );
        }
    }

    private void ensureStockWhenActive(final AdminProdutoRequestDTO dto) {
        if (Boolean.TRUE.equals(dto.getAtivo()) && !hasPositiveStock(dto.getEstoque())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estoque positivo obrigatorio para produtos disponibilizados na web."
            );
        }
    }

    /**
     * Validates unique product name in repository.
     *
     * @param dto request payload
     * @param currentId current product id for update scenario
     */
    private void ensureUniqueName(
            final AdminProdutoRequestDTO dto,
            final Long currentId
    ) {
        if (dto.getNome() == null) {
            return;
        }
        final String nome = dto.getNome().trim();
        if (nome.isBlank()) {
            return;
        }
        repo.findByNomeIgnoreCase(nome)
                .filter(existing -> currentId == null
                        || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            DUPLICATE_NAME_MESSAGE
                    );
                });
    }

    private Integer sanitizeAlertaEstoqueLimite(final Integer rawValue) {
        if (rawValue == null) {
            return null;
        }
        return Math.clamp(rawValue, 2, 100_000);
    }

    /**
     * Null-safe string helper.
     *
     * @param value source value
     * @param def default value
     * @return normalized value
     */
    private static String nvl(final String value, final String def) {
        return (value == null || value.isBlank()) ? def : value;
    }

    private static String normalizeDigits(final String value) {
        if (value == null) {
            return null;
        }
        final String digits = value.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }

    private void validateRequest(final AdminProdutoRequestDTO dto) {
        final LinkedHashSet<ConstraintViolation<?>> violations = new LinkedHashSet<>();
        violations.addAll(validator.validate(dto));
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void primePrimaryImageFromGallery(final AdminProdutoRequestDTO dto) {
        if (dto == null) {
            return;
        }
        if (dto.getImagem() != null && !dto.getImagem().isBlank()) {
            return;
        }
        if (dto.getImagens() == null || dto.getImagens().isEmpty()) {
            return;
        }
        dto.setImagem(dto.getImagens().getFirst());
    }

    private ParsedAdminProdutoRequest parseRequest(final JsonNode body) {
        final AdminProdutoRequestDTO dto = new AdminProdutoRequestDTO();
        dto.setNome(readText(body, "nome"));
        dto.setDescricao(readText(body, "descricao"));
        dto.setPreco(readBigDecimal(body, "preco", "precoVenda"));
        dto.setImagem(readText(body, "imagem", "imagemUrl"));
        dto.setImagens(readTextList(body, "imagens"));
        dto.setCategoria(readText(body, "categoria"));
        dto.setCodigoBarras(readText(body, "codigoBarras"));
        dto.setEstoque(readInteger(body, "estoque", "estoqueAtual"));
        dto.setAlertaEstoqueLimite(readInteger(body, "alertaEstoqueLimite"));
        dto.setTarjaMedicacao(readText(body, "tarjaMedicacao"));
        dto.setExigeReceita(Boolean.TRUE.equals(readBoolean(body, "exigeReceita")));
        dto.setFiscalNcm(readText(body, "fiscalNcm"));
        dto.setFiscalCest(readText(body, "fiscalCest"));
        dto.setFiscalCfop(readText(body, "fiscalCfop"));
        dto.setFiscalOrigem(readInteger(body, "fiscalOrigem"));
        dto.setFiscalIcmsCst(readText(body, "fiscalIcmsCst"));
        dto.setFiscalCsosn(readText(body, "fiscalCsosn"));
        dto.setFiscalPisCst(readText(body, "fiscalPisCst"));
        dto.setFiscalCofinsCst(readText(body, "fiscalCofinsCst"));
        dto.setAtivo(Boolean.TRUE.equals(readBoolean(body, "ativo", "disponivel")));
        return new ParsedAdminProdutoRequest(dto, hasField(body, "imagens"));
    }

    private void applyImageGallery(
            final ProdutoEntity entity,
            final ParsedAdminProdutoRequest request,
            final String imagemAnterior
    ) {
        if (request.imagesProvided()) {
            final List<String> imagens = new ArrayList<>();
            final String imagemPrincipal = trimToNull(request.dto().getImagem());
            if (imagemPrincipal != null) {
                imagens.add(imagemPrincipal);
            }
            if (request.dto().getImagens() != null) {
                imagens.addAll(request.dto().getImagens());
            }
            entity.setImagensProduto(imagens);
            return;
        }

        final String imagemPrincipal = trimToNull(request.dto().getImagem());
        if (imagemPrincipal == null) {
            return;
        }
        if (imagemAnterior != null && !imagemAnterior.equals(imagemPrincipal)) {
            entity.addImagemProduto(imagemAnterior);
        }
        entity.addImagemProduto(imagemPrincipal);
        entity.definirImagemPrincipal(imagemPrincipal);
    }

    private void ensureImageWhenActive(final ProdutoEntity entity) {
        if (Boolean.TRUE.equals(entity.getDisponivel())
                && entity.getImagensProduto().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Imagem obrigatÃ³ria para produtos disponibilizados na web."
            );
        }
    }

    private void ensureBarcodeWhenActive(final ProdutoEntity entity) {
        if (Boolean.TRUE.equals(entity.getDisponivel())
                && !hasValidBarcode(entity.getCodigoBarras())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Codigo de barras obrigatorio para produtos disponibilizados na web."
            );
        }
    }

    private void ensureStockWhenActive(final ProdutoEntity entity) {
        if (Boolean.TRUE.equals(entity.getDisponivel()) && !hasPositiveStock(entity.getEstoque())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estoque positivo obrigatorio para produtos disponibilizados na web."
            );
        }
    }

    private void ensureBarcodeWhenPublishing(final ProdutoEntity entity) {
        if (!hasValidBarcode(entity.getCodigoBarras())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Produto precisa ter codigo de barras valido antes de publicar."
            );
        }
    }

    private void ensureStockWhenPublishing(final ProdutoEntity entity) {
        if (!hasPositiveStock(entity.getEstoque())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Produto precisa ter estoque positivo antes de publicar."
            );
        }
    }

    private boolean hasPositiveStock(final Integer estoque) {
        return estoque != null && estoque > 0;
    }

    private boolean hasValidBarcode(final String codigoBarras) {
        final String normalized = BarcodeNormalizer.normalizeOrNull(codigoBarras);
        if (normalized == null) {
            return false;
        }
        final int length = normalized.length();
        return length == 8 || (length >= 12 && length <= 14);
    }

    private static boolean hasField(final JsonNode body, final String fieldName) {
        return body != null && body.isObject() && body.has(fieldName);
    }

    private static JsonNode firstPresent(final JsonNode body, final String... fieldNames) {
        if (body == null || !body.isObject()) {
            return null;
        }
        for (String fieldName : fieldNames) {
            if (body.has(fieldName)) {
                return body.get(fieldName);
            }
        }
        return null;
    }

    private static String readText(final JsonNode body, final String... fieldNames) {
        final JsonNode node = firstPresent(body, fieldNames);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return trimToNull(node.textValue());
        }
        if (node.isNumber() || node.isBoolean()) {
            return trimToNull(node.asText());
        }
        return null;
    }

    private static List<String> readTextList(final JsonNode body, final String fieldName) {
        final JsonNode node = firstPresent(body, fieldName);
        if (node == null || node.isNull()) {
            return List.of();
        }
        final List<String> imagens = new ArrayList<>();
        if (node.isArray()) {
            node.forEach(item -> {
                final String value = item == null || item.isNull()
                        ? null
                        : trimToNull(item.asText());
                if (value != null) {
                    imagens.add(value);
                }
            });
            return imagens;
        }
        final String value = readText(body, fieldName);
        if (value != null) {
            imagens.add(value);
        }
        return imagens;
    }

    private static Integer readInteger(final JsonNode body, final String... fieldNames) {
        final BigDecimal decimal = readBigDecimal(body, fieldNames);
        if (decimal == null) {
            return null;
        }
        final BigDecimal normalized = decimal.stripTrailingZeros();
        if (normalized.scale() > 0) {
            return null;
        }
        try {
            return normalized.intValueExact();
        } catch (ArithmeticException ex) {
            return null;
        }
    }

    private static BigDecimal readBigDecimal(final JsonNode body, final String... fieldNames) {
        final JsonNode node = firstPresent(body, fieldNames);
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.decimalValue();
        }
        if (!node.isTextual()) {
            return null;
        }
        final String raw = trimToNull(node.textValue());
        if (raw == null) {
            return null;
        }
        final String monetary = raw.replace("R$", "").replace(" ", "");
        final String normalized = monetary.contains(",") && monetary.contains(".")
                ? monetary.replace(".", "").replace(",", ".")
                : monetary.replace(",", ".");
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Boolean readBoolean(final JsonNode body, final String... fieldNames) {
        final JsonNode node = firstPresent(body, fieldNames);
        if (node == null || node.isNull()) {
            return false;
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        if (node.isNumber()) {
            return node.intValue() != 0;
        }
        if (!node.isTextual()) {
            throw invalidField(fieldNames[0]);
        }
        final String value = trimToNull(node.textValue());
        if (value == null) {
            return false;
        }
        return switch (value.toLowerCase()) {
            case "true", "1", "sim", "yes" -> true;
            case "false", "0", "nao", "não", "no" -> false;
            default -> throw invalidField(fieldNames[0]);
        };
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static ResponseStatusException invalidField(final String fieldName) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Campo '" + fieldName + "' invalido."
        );
    }

    private void cleanupUploadedProductImages(final List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }
        imageUrls.stream()
                .filter(imageUrl -> imageUrl != null && !imageUrl.isBlank())
                .forEach(imageStorageService::deleteProductImageByUrl);
    }

    private void cleanupUploadedProductImage(final String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        imageStorageService.deleteProductImageByUrl(imageUrl);
    }

    private record ParsedAdminProdutoRequest(
            AdminProdutoRequestDTO dto,
            boolean imagesProvided
    ) {
    }
}
