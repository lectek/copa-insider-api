package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.lectek.copainsider.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.service.validation.ProductSourcePolicy;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import br.com.lectek.copainsider.domain.sync.SyncStatus;
import br.com.lectek.copainsider.domain.support.BarcodeNormalizer;
import br.com.lectek.copainsider.domain.support.ProdutoHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogoVendaDisponivelService {

    private static final int UNIDADE_MAX_LEN = 20;
    private static final String CATEGORIA_LOCAL = "Catalogo local";
    private static final String CATEGORIA_ESTOQUE_FISICO = "Estoque fisico";

    private final ProdutoRepository produtoRepository;
    private final ProductCategoryBindingService categoryBindingService;
    private final ResourceLoader resourceLoader;
    private final ObjectProvider<ProdutoLegacyRepository> legacyRepositoryProvider;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    @Value("${catalogo-venda.local.resource:classpath:catalogo/catalogo-venda-local.tsv}")
    private String resourceLocation;

    @Transactional(transactionManager = "mysqlTransactionManager")
    public ImportacaoResumo sincronizarCatalogoDisponivel() {
        List<CatalogoVendaItem> itens = this.loadItens();
        if (itens.isEmpty()) {
            log.warn("[catalogo-venda] nenhum item encontrado no recurso {}", this.resourceLocation);
            return new ImportacaoResumo(0, 0, 0, 0, 0);
        }

        Map<Long, CatalogoVendaItem> porLegacyId = new LinkedHashMap<>();
        Set<Long> legacyIdsSemBarcode = new HashSet<>();
        for (CatalogoVendaItem item : itens) {
            porLegacyId.put(item.legacyId(), item);
            if (item.legacyId() != null && BarcodeNormalizer.normalizeOrNull(item.codigoBarras()) == null) {
                legacyIdsSemBarcode.add(item.legacyId());
            }
        }
        Map<Long, String> legacyBarcodes = this.loadLegacyBarcodes(legacyIdsSemBarcode);

        int inseridos = 0;
        int atualizados = 0;
        int inalterados = 0;
        List<ProdutoEntity> lote = new ArrayList<>(porLegacyId.size());

        for (CatalogoVendaItem item : porLegacyId.values()) {
            TargetResolution resolution = this.resolveProduto(item, legacyBarcodes);
            if (resolution.skip()) {
                inalterados++;
                continue;
            }
            ProdutoEntity entity = resolution.entity();
            boolean novo = entity.getId() == null;
            Snapshot before = novo ? null : Snapshot.from(entity);

            this.applyItem(entity, item, legacyBarcodes);
            Snapshot after = Snapshot.from(entity);
            if (!novo && Objects.equals(before, after)) {
                inalterados++;
                continue;
            }

            lote.add(entity);
            if (novo) {
                inseridos++;
            } else {
                atualizados++;
            }
        }

        if (!lote.isEmpty()) {
            this.categoryBindingService.bindAll(lote);
            this.produtoRepository.saveAll(lote);
        }

        int desativados = this.desativarItensForaDoPdf(porLegacyId.keySet());
        log.info("[catalogo-venda] sincronizacao concluida | lidos={} inseridos={} atualizados={} inalterados={} desativados={}",
                porLegacyId.size(), inseridos, atualizados, inalterados, desativados);
        return new ImportacaoResumo(porLegacyId.size(), inseridos, atualizados, inalterados, desativados);
    }

    private TargetResolution resolveProduto(CatalogoVendaItem item, Map<Long, String> legacyBarcodes) {
        Long tenantId = this.resolveTenantId();
        List<ProdutoEntity> byLegacyId = tenantId == null
                ? this.produtoRepository.findAllByLegacyIdOrderByIdAsc(item.legacyId())
                : this.produtoRepository.findAllByTenantIdAndLegacyIdOrderByIdAsc(tenantId, item.legacyId());
        if (!byLegacyId.isEmpty()) {
            ProdutoEntity managed = byLegacyId.stream()
                    .filter(ProductSourcePolicy::isManagedByCatalogSync)
                    .findFirst()
                    .orElse(null);
            if (managed != null) {
                return TargetResolution.use(managed);
            }
            ProdutoEntity protectedOwner = byLegacyId.stream()
                    .filter(ProductSourcePolicy::isProtectedFromCatalogSync)
                    .findFirst()
                    .orElse(null);
            if (protectedOwner != null) {
                log.warn(
                        "[catalogo-venda] item ignorado: legacyId={} pertence a produto protegido id={} origem={}",
                        item.legacyId(),
                        protectedOwner.getId(),
                        protectedOwner.getMetodoLeituraCodigoBarras()
                );
                return TargetResolution.skipItem();
            }
        }

        String barcode = this.resolvePreferredBarcode(item, null, legacyBarcodes);
        if (barcode != null) {
            ProdutoEntity byBarcode = tenantId == null
                    ? this.produtoRepository.findByAnyCodigo(barcode).orElse(null)
                    : this.produtoRepository.findByAnyCodigo(tenantId, barcode).orElse(null);
            if (byBarcode == null) {
                ProdutoEntity entity = new ProdutoEntity();
                entity.setTenantId(tenantId);
                return TargetResolution.use(entity);
            }
            if (ProductSourcePolicy.isProtectedFromCatalogSync(byBarcode)) {
                log.warn(
                        "[catalogo-venda] item ignorado: codigoBarras={} pertence a produto protegido id={} origem={}",
                        barcode,
                        byBarcode.getId(),
                        byBarcode.getMetodoLeituraCodigoBarras()
                );
                return TargetResolution.skipItem();
            }
            return TargetResolution.use(byBarcode);
        }
        ProdutoEntity entity = new ProdutoEntity();
        entity.setTenantId(tenantId);
        return TargetResolution.use(entity);
    }

    private void applyItem(ProdutoEntity entity, CatalogoVendaItem item, Map<Long, String> legacyBarcodes) {
        String barcode = this.resolveBarcodeForEntity(entity, item, legacyBarcodes);
        boolean barcodeValido = this.hasPublishableBarcode(barcode);
        String nome = truncate(item.nome(), 255);
        String descricao = truncate(firstNonBlank(item.descricao(), item.nome()), 1000);
        String fabricante = truncate(item.fabricante(), 128);
        BigDecimal precoVenda = item.precoVenda().setScale(2, RoundingMode.HALF_UP);
        int estoque = this.resolveEstoque(entity, item);
        boolean publicavel = barcodeValido && estoque > 0;

        entity.setLegacyId(item.legacyId());
        if (entity.getTenantId() == null) {
            entity.setTenantId(this.resolveTenantId());
        }
        if (barcode != null) {
            entity.setCodigoBarras(barcode);
        } else if (entity.getId() == null) {
            entity.setCodigoBarras(null);
        }
        if (barcode == null) {
            entity.preserveCodigoOriginalBarcode(item.codigoBarras());
        }
        entity.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        entity.setNome(nome);
        entity.setDescricao(descricao);
        entity.setUnidade(truncate(firstNonBlank(item.descricao(), entity.getUnidade()), UNIDADE_MAX_LEN));
        entity.setFabricante(fabricante);
        entity.setCategoria(this.resolveCategoria(entity.getCategoria()));
        entity.setPrecoVenda(precoVenda);
        entity.setPrecoPromocional(null);
        if (entity.getPrecoCusto() == null || entity.getPrecoCusto().compareTo(BigDecimal.ZERO) <= 0) {
            entity.setPrecoCusto(precoVenda);
        }
        entity.setEstoque(estoque);
        entity.setDisponivel(publicavel);
        entity.setStatus(publicavel ? ProdutoStatus.PUBLICADO : ProdutoStatus.IMPORTADO);
        entity.setStatusSync(publicavel
                ? SyncStatus.SINCRONIZADO
                : SyncStatus.PENDENTE);
        if (publicavel) {
            entity.setDespublicadoEm(null);
        } else {
            entity.setPublicadoEm(null);
            entity.setDespublicadoEm(LocalDateTime.now());
        }
        if (publicavel && entity.getPublicadoEm() == null) {
            entity.setPublicadoEm(LocalDateTime.now());
        }
        if (entity.getDataCadastro() == null) {
            entity.setDataCadastro(LocalDate.now());
        }
        entity.setDataImportacao(LocalDateTime.now());
        entity.setHashLegado(ProdutoHashUtil.buildHash(
                barcode == null ? "" : barcode,
                nome,
                descricao,
                precoVenda,
                item.legacyId()
        ));
    }

    private String resolveBarcodeForEntity(ProdutoEntity entity,
                                           CatalogoVendaItem item,
                                           Map<Long, String> legacyBarcodes) {
        String barcode = this.resolvePreferredBarcode(item, entity, legacyBarcodes);
        if (barcode == null) {
            return null;
        }

        Long tenantId = this.resolveTenantId();
        ProdutoEntity owner = tenantId == null
                ? this.produtoRepository.findByAnyCodigo(barcode).orElse(null)
                : this.produtoRepository.findByAnyCodigo(tenantId, barcode).orElse(null);
        if (owner == null) {
            return barcode;
        }

        if (entity.getId() != null && owner.getId() != null && entity.getId().equals(owner.getId())) {
            return barcode;
        }

        return BarcodeNormalizer.normalizeOrNull(entity.getCodigoBarras());
    }

    private String resolvePreferredBarcode(CatalogoVendaItem item,
                                           ProdutoEntity entity,
                                           Map<Long, String> legacyBarcodes) {
        String barcode = BarcodeNormalizer.normalizeOrNull(item.codigoBarras());
        if (barcode != null) {
            return barcode;
        }

        String currentBarcode = entity == null ? null : BarcodeNormalizer.normalizeOrNull(entity.getCodigoBarras());
        if (currentBarcode != null) {
            return currentBarcode;
        }

        return legacyBarcodes.get(item.legacyId());
    }

    private Map<Long, String> loadLegacyBarcodes(Set<Long> legacyIds) {
        if (legacyIds == null || legacyIds.isEmpty()) {
            return Map.of();
        }

        ProdutoLegacyRepository legacyRepository = this.legacyRepositoryProvider.getIfAvailable();
        if (legacyRepository == null) {
            return Map.of();
        }

        List<Integer> ids = legacyIds.stream()
                .filter(Objects::nonNull)
                .filter(id -> id > 0 && id <= Integer.MAX_VALUE)
                .map(Long::intValue)
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }

        LinkedHashMap<Long, String> legacyBarcodes = new LinkedHashMap<>();
        for (ProdutoLegacyEntity legacyEntity : legacyRepository.findAllById(ids)) {
            if (legacyEntity == null || legacyEntity.getId() == null) {
                continue;
            }
            String barcode = BarcodeNormalizer.normalizeOrNull(legacyEntity.getCodigoBarras());
            if (barcode != null) {
                legacyBarcodes.putIfAbsent(legacyEntity.getId().longValue(), barcode);
            }
        }
        return legacyBarcodes;
    }

    private boolean hasPublishableBarcode(final String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return false;
        }
        final int length = barcode.length();
        return length == 8 || (length >= 12 && length <= 14);
    }

    private int resolveEstoque(ProdutoEntity entity, CatalogoVendaItem item) {
        if (item.estoque() != null) {
            return Math.max(0, item.estoque());
        }
        if (entity.getEstoque() != null) {
            return Math.max(0, entity.getEstoque());
        }
        return 0;
    }

    private String resolveCategoria(String categoriaAtual) {
        String categoria = normalize(categoriaAtual);
        if (!categoria.isBlank() && !categoria.equalsIgnoreCase(CATEGORIA_ESTOQUE_FISICO)) {
            return categoria;
        }
        return CATEGORIA_LOCAL;
    }

    private int desativarItensForaDoPdf(Set<Long> legacyIdsAtivos) {
        List<ProdutoEntity> gerenciados = this.produtoRepository
                .findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        if (gerenciados.isEmpty()) {
            return 0;
        }

        List<ProdutoEntity> toSave = new ArrayList<>();
        for (ProdutoEntity entity : gerenciados) {
            if (entity.getLegacyId() != null && legacyIdsAtivos.contains(entity.getLegacyId())) {
                continue;
            }

            boolean alreadyInactive = Boolean.FALSE.equals(entity.getDisponivel())
                    && (entity.getEstoque() == null || entity.getEstoque() <= 0)
                    && entity.getStatus() == ProdutoStatus.IMPORTADO
                    && entity.getDespublicadoEm() != null;
            if (alreadyInactive) {
                continue;
            }

            entity.setDisponivel(Boolean.FALSE);
            entity.setEstoque(0);
            entity.setStatus(ProdutoStatus.IMPORTADO);
            entity.setDespublicadoEm(LocalDateTime.now());
            toSave.add(entity);
        }

        if (!toSave.isEmpty()) {
            this.categoryBindingService.bindAll(toSave);
            this.produtoRepository.saveAll(toSave);
        }
        return toSave.size();
    }

    private List<CatalogoVendaItem> loadItens() {
        Resource resource = this.resourceLoader.getResource(this.resourceLocation);
        if (!resource.exists()) {
            log.warn("[catalogo-venda] recurso nao encontrado: {}", this.resourceLocation);
            return List.of();
        }

        List<CatalogoVendaItem> itens = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                CatalogoVendaItem item = this.parseLine(line);
                if (item != null) {
                    itens.add(item);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao ler catalogo disponivel: " + this.resourceLocation, ex);
        }
        return itens;
    }

    private CatalogoVendaItem parseLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split("\t", -1);
        if (parts.length < 7) {
            return null;
        }

        Long legacyId = parseLong(parts[0]);
        BigDecimal precoVenda = parseMoney(parts[5]);
        if (legacyId == null || precoVenda == null || precoVenda.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        return new CatalogoVendaItem(
                legacyId,
                normalize(parts[1]),
                firstNonBlank(parts[2], "Produto"),
                normalize(parts[3]),
                normalize(parts[4]),
                precoVenda,
                parseInteger(parts[6])
        );
    }

    private static Long parseLong(String raw) {
        String normalized = normalize(raw).replaceAll("[^0-9]", "");
        if (normalized.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Integer parseInteger(String raw) {
        String normalized = normalize(raw).replaceAll("[^0-9-]", "");
        if (normalized.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static BigDecimal parseMoney(String raw) {
        String normalized = normalize(raw).replace(",", ".");
        if (normalized.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            String normalized = normalize(value);
            if (!normalized.isBlank()) {
                return normalized;
            }
        }
        return "";
    }

    private static String truncate(String value, int maxLen) {
        String normalized = normalize(value);
        if (normalized.length() <= maxLen) {
            return normalized;
        }
        return normalized.substring(0, maxLen);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace('\uFEFF', ' ').replaceAll("\\s+", " ").trim();
    }

    private record CatalogoVendaItem(
            Long legacyId,
            String codigoBarras,
            String nome,
            String descricao,
            String fabricante,
            BigDecimal precoVenda,
            Integer estoque
    ) {
    }

    private record Snapshot(
            Long legacyId,
            String codigoBarras,
            String nome,
            String descricao,
            String categoria,
            String fabricante,
            String unidade,
            BigDecimal precoVenda,
            BigDecimal precoPromocional,
            Integer estoque,
            Boolean disponivel,
            ProdutoStatus status,
            MetodoLeituraCodigoBarras metodoLeituraCodigoBarras
    ) {
        static Snapshot from(ProdutoEntity entity) {
            return new Snapshot(
                    entity.getLegacyId(),
                    BarcodeNormalizer.normalizeOrNull(entity.getCodigoBarras()),
                    normalize(entity.getNome()),
                    normalize(entity.getDescricao()),
                    normalize(entity.getCategoria()),
                    normalize(entity.getFabricante()),
                    normalize(entity.getUnidade()),
                    entity.getPrecoVenda(),
                    entity.getPrecoPromocional(),
                    entity.getEstoque(),
                    entity.getDisponivel(),
                    entity.getStatus(),
                    entity.getMetodoLeituraCodigoBarras()
            );
        }
    }

    private record TargetResolution(ProdutoEntity entity, boolean skip) {

        static TargetResolution use(ProdutoEntity entity) {
            return new TargetResolution(entity, false);
        }

        static TargetResolution skipItem() {
            return new TargetResolution(null, true);
        }
    }

    private Long resolveTenantId() {
        return this.tenantResolverService == null ? null : this.tenantResolverService.resolveDefaultTenantId();
    }

    public record ImportacaoResumo(
            int lidos,
            int inseridos,
            int atualizados,
            int inalterados,
            int desativados
    ) {
    }
}
