/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.EnumType
 *  jakarta.persistence.Enumerated
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.Index
 *  jakarta.persistence.PrePersist
 *  jakarta.persistence.PreUpdate
 *  jakarta.persistence.Table
 *  jakarta.persistence.Version
 */
package br.com.redemaisfarma.adapters.outbound.persistence.entity;
import br.com.redemaisfarma.domain.support.BarcodeNormalizer;
import br.com.redemaisfarma.domain.sync.SyncStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Entity
@Table(name="produto", indexes={@Index(name="idx_produto_codigo_barras", columnList="codigo_barras"), @Index(name="idx_produto_legacy_id", columnList="legacy_id")})
public class ProdutoEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="nome", nullable=false)
    private String nome;
    @Column(name="descricao")
    private String descricao;
    @Column(name="preco_venda", precision=15, scale=2)
    private BigDecimal precoVenda;
    @Column(name="preco_custo", precision=15, scale=2)
    private BigDecimal precoCusto;
    @Column(name="preco_promocional", precision=15, scale=2)
    private BigDecimal precoPromocional;
    @Column(name="imagem")
    private String imagem;
    @Column(name="imagem_webp")
    private String imagemWebp;
    @Column(name="imagens_adicionais", columnDefinition="TEXT")
    private String imagensAdicionais;
    @Column(name="categoria", nullable=false)
    private String categoria;
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private ProdutoCategoriaEntity categoriaRef;
    @Enumerated(value=EnumType.STRING)
    @Column(name="tarja_medicacao", length=32)
    private TarjaMedicacao tarjaMedicacao;
    @Column(name="exige_receita", nullable=false)
    private Boolean exigeReceita;
    @Column(name="codigo_barras")
    private String codigoBarras;
    @Enumerated(value=EnumType.STRING)
    @Column(name="metodo_leitura_codigo_barras", length=32)
    private MetodoLeituraCodigoBarras metodoLeituraCodigoBarras;
    @Column(name="codigo_original")
    private Long codigoOriginal;
    @Column(name="unidade")
    private String unidade;
    @Column(name="estoque")
    private Integer estoque;
    @Column(name="alerta_estoque_limite")
    private Integer alertaEstoqueLimite;
    @Column(name="disponivel")
    private Boolean disponivel;
    @Column(name="fabricante")
    private String fabricante;
    @Column(name="fiscal_ncm", length=8)
    private String fiscalNcm;
    @Column(name="fiscal_cest", length=7)
    private String fiscalCest;
    @Column(name="fiscal_cfop", length=4)
    private String fiscalCfop;
    @Column(name="fiscal_origem")
    private Integer fiscalOrigem;
    @Column(name="fiscal_icms_cst", length=3)
    private String fiscalIcmsCst;
    @Column(name="fiscal_csosn", length=3)
    private String fiscalCsosn;
    @Column(name="fiscal_pis_cst", length=2)
    private String fiscalPisCst;
    @Column(name="fiscal_cofins_cst", length=2)
    private String fiscalCofinsCst;
    @Column(name="data_cadastro")
    private LocalDate dataCadastro;
    @Column(name="data_importacao")
    private LocalDateTime dataImportacao;
    @Column(name="publicado_em")
    private LocalDateTime publicadoEm;
    @Column(name="despublicado_em")
    private LocalDateTime despublicadoEm;
    @Column(name="ordem_carrossel")
    private Integer ordemCarrossel;
    @Column(name="destaque_carrossel")
    private Boolean destaqueCarrossel;
    @Column(name="legacy_id")
    private Long legacyId;
    @Column(name="id_produto_externo")
    private Long idProdutoExterno;
    @Column(name="tenant_id")
    private Long tenantId;
    @Column(name="hash_legado", nullable=false, length=64)
    private String hashLegado;
    @Column(name="status_sync")
    private String statusSync;
    @Column(name="desconto_percentual")
    private Integer descontoPercentual;
    @Enumerated(value=EnumType.STRING)
    @Column(name="status")
    private ProdutoStatus status;
    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;
    @Column(name="updated_at", nullable=false)
    private LocalDateTime updatedAt;
    @Column(name="validador")
    private String validador;
    @Version
    @Column(name="version", nullable=false)
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
        this.normalizeTextFields();
        if (this.statusSync == null) {
            this.statusSync = SyncStatus.SINCRONIZADO;
        }
        if (this.status == null) {
            this.status = ProdutoStatus.IMPORTADO;
        }
        this.tarjaMedicacao = null;
        this.exigeReceita = Boolean.FALSE;
        if (this.metodoLeituraCodigoBarras == null) {
            this.metodoLeituraCodigoBarras = MetodoLeituraCodigoBarras.DESCONHECIDO;
        }
        this.normalizeImageGalleryState();
        this.normalizeFiscalFields();
        this.garantirHashLegado();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.normalizeTextFields();
        this.tarjaMedicacao = null;
        this.exigeReceita = Boolean.FALSE;
        this.normalizeImageGalleryState();
        this.normalizeFiscalFields();
        this.garantirHashLegado();
    }

    private void normalizeTextFields() {
        this.nome = ProdutoEntity.normalizePlainText(this.nome);
        this.descricao = ProdutoEntity.normalizePlainText(this.descricao);
        this.categoria = ProdutoEntity.normalizePlainText(this.categoria);
        this.fabricante = ProdutoEntity.normalizePlainText(this.fabricante);
        this.unidade = ProdutoEntity.normalizePlainText(this.unidade);
        this.statusSync = ProdutoEntity.normalizeStatusSync(this.statusSync);
        this.validador = ProdutoEntity.normalizePlainText(this.validador);

        if (ProdutoEntity.isGenericProductName(this.nome) && ProdutoEntity.isMeaningfulText(this.descricao)) {
            this.nome = ProdutoEntity.truncate(this.descricao, 255);
        }
        if (this.nome == null) {
            this.nome = "Produto";
        } else {
            this.nome = ProdutoEntity.truncate(this.nome, 255);
        }

        if (!ProdutoEntity.isMeaningfulText(this.descricao)) {
            this.descricao = ProdutoEntity.truncate(this.nome, 1000);
        } else {
            this.descricao = ProdutoEntity.truncate(this.descricao, 1000);
        }

        if (this.categoria == null) {
            this.categoria = "Sem Categoria";
        } else {
            this.categoria = ProdutoEntity.truncate(this.categoria, 255);
        }

        if (ProdutoEntity.looksLikeNullLiteral(this.fabricante)) {
            this.fabricante = null;
        } else {
            this.fabricante = ProdutoEntity.truncate(this.fabricante, 128);
        }

        if (ProdutoEntity.looksLikeNullLiteral(this.unidade)) {
            this.unidade = null;
        }
    }

    private void normalizeImageGalleryState() {
        List<String> imagens = this.getImagensProduto();
        if (imagens.isEmpty()) {
            this.imagem = null;
            this.imagensAdicionais = null;
            return;
        }
        this.imagem = imagens.getFirst();
        this.imagensAdicionais = ProdutoEntity.serializeAdditionalImages(imagens.subList(1, imagens.size()));
    }

    private void normalizeFiscalFields() {
        this.fiscalNcm = ProdutoEntity.onlyDigitsWithMaxLength(this.fiscalNcm, 8);
        this.fiscalCest = ProdutoEntity.onlyDigitsWithMaxLength(this.fiscalCest, 7);
        this.fiscalCfop = ProdutoEntity.onlyDigitsWithMaxLength(this.fiscalCfop, 4);
        this.fiscalIcmsCst = ProdutoEntity.onlyDigitsWithMaxLength(this.fiscalIcmsCst, 3);
        this.fiscalCsosn = ProdutoEntity.onlyDigitsWithMaxLength(this.fiscalCsosn, 3);
        this.fiscalPisCst = ProdutoEntity.onlyDigitsWithMaxLength(this.fiscalPisCst, 2);
        this.fiscalCofinsCst = ProdutoEntity.onlyDigitsWithMaxLength(this.fiscalCofinsCst, 2);
        if (this.fiscalOrigem != null && (this.fiscalOrigem < 0 || this.fiscalOrigem > 8)) {
            this.fiscalOrigem = null;
        }
    }

    private void garantirHashLegado() {
        if (this.hashLegado == null || this.hashLegado.isBlank()) {
            String base = (this.legacyId != null ? this.legacyId.toString() : "") + "|" + (this.codigoBarras != null ? this.codigoBarras : "") + "|" + (this.nome != null ? this.nome : "");
            this.hashLegado = ProdutoEntity.sha256Hex(base);
        }
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(d.length * 2);
            byte[] byArray = d;
            int n = d.length;
            int n2 = 0;
            while (n2 < n) {
                byte b = byArray[n2];
                sb.append(String.format("%02x", b));
                ++n2;
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new IllegalStateException("Falha ao computar hash_legado", e);
        }
    }

    private static String normalizePlainText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value
                .replace('\u00a0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeStatusSync(String value) {
        String normalized = normalizePlainText(value);
        if (normalized == null) {
            return null;
        }
        return switch (normalized.toUpperCase(Locale.ROOT)) {
            case "SINCRONIZADO", "SYNCED", "SUCCESS", "SUCESSO" -> SyncStatus.SINCRONIZADO;
            case "PENDENTE", "PENDING", "AGUARDANDO_CODIGO_BARRAS" -> SyncStatus.PENDENTE;
            case "ERRO", "ERROR", "FALHA", "FAILED" -> SyncStatus.ERRO;
            default -> normalized;
        };
    }

    private static boolean isMeaningfulText(String value) {
        return normalizePlainText(value) != null;
    }

    private static boolean looksLikeNullLiteral(String value) {
        String normalized = normalizePlainText(value);
        return normalized != null && "null".equalsIgnoreCase(normalized);
    }

    private static boolean isGenericProductName(String value) {
        String normalized = normalizePlainText(value);
        if (normalized == null) {
            return true;
        }
        String lowered = normalized.toLowerCase(Locale.ROOT);
        return lowered.equals("produto")
                || lowered.equals("sem nome")
                || lowered.equals("novo produto")
                || lowered.equals("produto do estoque fisico");
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPrecoVenda() {
        return this.precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public BigDecimal getPreco() {
        if (this.precoPromocional != null
                && this.precoVenda != null
                && this.precoPromocional.compareTo(this.precoVenda) < 0) {
            return this.precoPromocional;
        }
        return this.precoVenda;
    }

    public void setPreco(BigDecimal preco) {
        this.precoVenda = preco;
    }

    public BigDecimal getPrecoCusto() {
        return this.precoCusto;
    }

    public void setPrecoCusto(BigDecimal precoCusto) {
        this.precoCusto = precoCusto;
    }

    public BigDecimal getPrecoPromocional() {
        return this.precoPromocional;
    }

    public void setPrecoPromocional(BigDecimal precoPromocional) {
        this.precoPromocional = precoPromocional;
    }

    public BigDecimal getPrecoDe() {
        BigDecimal precoAtual = this.getPreco();
        if (this.precoVenda == null || precoAtual == null) {
            return null;
        }
        return this.precoVenda.compareTo(precoAtual) > 0 ? this.precoVenda : null;
    }

    public String getImagem() {
        return ProdutoEntity.normalizeImagemUrl(this.imagem);
    }

    public void setImagem(String imagem) {
        this.imagem = ProdutoEntity.normalizeImagemUrl(imagem);
    }

    public String getImagemWebp() {
        return this.imagemWebp;
    }

    public void setImagemWebp(String imagemWebp) {
        this.imagemWebp = imagemWebp;
    }

    public String getImagensAdicionais() {
        return this.imagensAdicionais;
    }

    public void setImagensAdicionais(String imagensAdicionais) {
        this.imagensAdicionais = ProdutoEntity.serializeAdditionalImages(
                ProdutoEntity.parseStoredAdditionalImages(imagensAdicionais)
        );
    }

    public List<String> getImagensProduto() {
        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        String primary = ProdutoEntity.normalizeImagemUrl(this.imagem);
        if (primary != null) {
            ordered.add(primary);
        }
        ordered.addAll(ProdutoEntity.parseStoredAdditionalImages(this.imagensAdicionais));
        return List.copyOf(ordered);
    }

    public void setImagensProduto(List<String> imagens) {
        List<String> normalizadas = ProdutoEntity.normalizeImageList(imagens);
        if (normalizadas.isEmpty()) {
            this.imagem = null;
            this.imagensAdicionais = null;
            return;
        }
        this.imagem = normalizadas.getFirst();
        this.imagensAdicionais = ProdutoEntity.serializeAdditionalImages(
                normalizadas.subList(1, normalizadas.size())
        );
    }

    public void addImagemProduto(String imagem) {
        String normalizada = ProdutoEntity.normalizeImagemUrl(imagem);
        if (normalizada == null) {
            return;
        }
        List<String> imagens = new ArrayList<>(this.getImagensProduto());
        if (!imagens.contains(normalizada)) {
            imagens.add(normalizada);
            this.setImagensProduto(imagens);
        }
    }

    public void definirImagemPrincipal(String imagem) {
        String normalizada = ProdutoEntity.normalizeImagemUrl(imagem);
        if (normalizada == null) {
            return;
        }
        List<String> imagens = new ArrayList<>(this.getImagensProduto());
        imagens.remove(normalizada);
        imagens.addFirst(normalizada);
        this.setImagensProduto(imagens);
    }

    private static String normalizeImagemUrl(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        String normalized = trimmed.replace('\\', '/');
        String lower = normalized.toLowerCase(Locale.ROOT);
        if (lower.contains("backend write error")
                || lower.contains("varnish cache server")
                || lower.contains("<html")
                || lower.contains("<body")) {
            return null;
        }

        if (normalized.contains("\n") || normalized.contains("\r")) {
            return null;
        }

        if (normalized.contains("..") || normalized.matches("^[a-zA-Z]:/.*")) {
            return null;
        }
        if (normalized.startsWith("/")) {
            return normalized;
        }
        if (normalized.startsWith("http://")
                || normalized.startsWith("https://")
                || normalized.startsWith("//")) {
            return normalized;
        }
        if (lower.startsWith("media/") || lower.startsWith("img/")
                || lower.startsWith("images/") || lower.startsWith("assets/")) {
            return "/" + normalized;
        }
        return "/media/products/" + normalized.replaceFirst("^/+", "");
    }

    private static List<String> normalizeImageList(List<String> imagens) {
        if (imagens == null || imagens.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String imagem : imagens) {
            String normalizada = ProdutoEntity.normalizeImagemUrl(imagem);
            if (normalizada != null) {
                unique.add(normalizada);
            }
        }
        return List.copyOf(unique);
    }

    private static List<String> parseStoredAdditionalImages(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        List<String> imagens = new ArrayList<>();
        for (String line : raw.split("\\R+")) {
            String normalizada = ProdutoEntity.normalizeImagemUrl(line);
            if (normalizada != null) {
                imagens.add(normalizada);
            }
        }
        return ProdutoEntity.normalizeImageList(imagens);
    }

    private static String serializeAdditionalImages(List<String> imagens) {
        List<String> normalizadas = ProdutoEntity.normalizeImageList(imagens);
        if (normalizadas.isEmpty()) {
            return null;
        }
        return String.join("\n", normalizadas);
    }

    private static String onlyDigitsWithMaxLength(String raw, int maxLength) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() > maxLength) {
            return digits.substring(0, maxLength);
        }
        return digits;
    }

    public String getCategoria() {
        return this.categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public ProdutoCategoriaEntity getCategoriaRef() {
        return this.categoriaRef;
    }

    public void setCategoriaRef(ProdutoCategoriaEntity categoriaRef) {
        this.categoriaRef = categoriaRef;
    }

    public TarjaMedicacao getTarjaMedicacao() {
        return this.tarjaMedicacao;
    }

    public void setTarjaMedicacao(TarjaMedicacao tarjaMedicacao) {
        this.tarjaMedicacao = tarjaMedicacao;
    }

    public Boolean getExigeReceita() {
        return this.exigeReceita;
    }

    public void setExigeReceita(Boolean exigeReceita) {
        this.exigeReceita = exigeReceita;
    }

    public String getCodigoBarras() {
        String explicitBarcode = ProdutoEntity.normalizeBarcode(this.codigoBarras);
        if (ProdutoEntity.isSupportedBarcode(explicitBarcode)) {
            return explicitBarcode;
        }
        String fallbackBarcode = ProdutoEntity.normalizeStoredFallbackBarcode(this.codigoOriginal);
        if (ProdutoEntity.isSupportedBarcode(fallbackBarcode)) {
            return fallbackBarcode;
        }
        return explicitBarcode;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = ProdutoEntity.normalizeBarcode(codigoBarras);
    }

    public void preserveCodigoOriginalBarcode(String codigoBarras) {
        String normalized = ProdutoEntity.normalizeBarcode(codigoBarras);
        if (!ProdutoEntity.isSupportedBarcode(normalized)) {
            return;
        }
        String visibleBarcode = this.getCodigoBarras();
        if (visibleBarcode != null && !visibleBarcode.equals(normalized)) {
            return;
        }
        Long parsedBarcode = ProdutoEntity.parseBarcodeAsLong(normalized);
        if (parsedBarcode != null) {
            this.codigoOriginal = parsedBarcode;
        }
    }

    public MetodoLeituraCodigoBarras getMetodoLeituraCodigoBarras() {
        return this.metodoLeituraCodigoBarras;
    }

    public void setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras metodoLeituraCodigoBarras) {
        this.metodoLeituraCodigoBarras = metodoLeituraCodigoBarras;
    }

    public Long getCodigoOriginal() {
        return this.codigoOriginal;
    }

    public void setCodigoOriginal(Long codigoOriginal) {
        this.codigoOriginal = codigoOriginal;
    }

    public Long getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    private static String normalizeBarcode(String value) {
        return BarcodeNormalizer.normalizeOrNull(value);
    }

    private static String normalizeStoredFallbackBarcode(Long codigoOriginal) {
        if (codigoOriginal == null) {
            return null;
        }
        String digits = ProdutoEntity.normalizeBarcode(codigoOriginal.toString());
        if (digits == null) {
            return null;
        }
        if (ProdutoEntity.isSupportedBarcode(digits)) {
            return digits;
        }
        if (digits.length() == 7 || digits.length() == 11) {
            return "0" + digits;
        }
        return digits;
    }

    private static boolean isSupportedBarcode(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.length() == 8 || value.length() >= 12 && value.length() <= 14;
    }

    private static Long parseBarcodeAsLong(String value) {
        if (!ProdutoEntity.isSupportedBarcode(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public String getUnidade() {
        return this.unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public Integer getEstoque() {
        return this.estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Integer getAlertaEstoqueLimite() {
        return this.alertaEstoqueLimite;
    }

    public void setAlertaEstoqueLimite(Integer alertaEstoqueLimite) {
        this.alertaEstoqueLimite = alertaEstoqueLimite;
    }

    public Boolean getDisponivel() {
        return this.disponivel;
    }

    public void setDisponivel(Boolean disponivel) {
        this.disponivel = disponivel;
    }

    public String getFabricante() {
        return this.fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getFiscalNcm() {
        return this.fiscalNcm;
    }

    public void setFiscalNcm(String fiscalNcm) {
        this.fiscalNcm = fiscalNcm;
    }

    public String getFiscalCest() {
        return this.fiscalCest;
    }

    public void setFiscalCest(String fiscalCest) {
        this.fiscalCest = fiscalCest;
    }

    public String getFiscalCfop() {
        return this.fiscalCfop;
    }

    public void setFiscalCfop(String fiscalCfop) {
        this.fiscalCfop = fiscalCfop;
    }

    public Integer getFiscalOrigem() {
        return this.fiscalOrigem;
    }

    public void setFiscalOrigem(Integer fiscalOrigem) {
        this.fiscalOrigem = fiscalOrigem;
    }

    public String getFiscalIcmsCst() {
        return this.fiscalIcmsCst;
    }

    public void setFiscalIcmsCst(String fiscalIcmsCst) {
        this.fiscalIcmsCst = fiscalIcmsCst;
    }

    public String getFiscalCsosn() {
        return this.fiscalCsosn;
    }

    public void setFiscalCsosn(String fiscalCsosn) {
        this.fiscalCsosn = fiscalCsosn;
    }

    public String getFiscalPisCst() {
        return this.fiscalPisCst;
    }

    public void setFiscalPisCst(String fiscalPisCst) {
        this.fiscalPisCst = fiscalPisCst;
    }

    public String getFiscalCofinsCst() {
        return this.fiscalCofinsCst;
    }

    public void setFiscalCofinsCst(String fiscalCofinsCst) {
        this.fiscalCofinsCst = fiscalCofinsCst;
    }

    public LocalDate getDataCadastro() {
        return this.dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataImportacao() {
        return this.dataImportacao;
    }

    public void setDataImportacao(LocalDateTime dataImportacao) {
        this.dataImportacao = dataImportacao;
    }

    public LocalDateTime getPublicadoEm() {
        return this.publicadoEm;
    }

    public void setPublicadoEm(LocalDateTime publicadoEm) {
        this.publicadoEm = publicadoEm;
    }

    public LocalDateTime getDespublicadoEm() {
        return this.despublicadoEm;
    }

    public void setDespublicadoEm(LocalDateTime despublicadoEm) {
        this.despublicadoEm = despublicadoEm;
    }

    public Integer getOrdemCarrossel() {
        return this.ordemCarrossel;
    }

    public void setOrdemCarrossel(Integer ordemCarrossel) {
        this.ordemCarrossel = ordemCarrossel;
    }

    public Boolean getDestaqueCarrossel() {
        return this.destaqueCarrossel;
    }

    public void setDestaqueCarrossel(Boolean destaqueCarrossel) {
        this.destaqueCarrossel = destaqueCarrossel;
    }

    public Long getLegacyId() {
        return this.legacyId;
    }

    public void setLegacyId(Long legacyId) {
        this.legacyId = legacyId;
    }

    public Long getIdProdutoExterno() {
        return this.idProdutoExterno;
    }

    public void setIdProdutoExterno(Long idProdutoExterno) {
        this.idProdutoExterno = idProdutoExterno;
    }

    public String getHashLegado() {
        return this.hashLegado;
    }

    public void setHashLegado(String hashLegado) {
        this.hashLegado = hashLegado;
    }

    public String getStatusSync() {
        return this.statusSync;
    }

    public void setStatusSync(String statusSync) {
        this.statusSync = statusSync;
    }

    public Integer getDescontoPercentual() {
        return this.descontoPercentual;
    }

    public void setDescontoPercentual(Integer descontoPercentual) {
        this.descontoPercentual = descontoPercentual;
    }

    public ProdutoStatus getStatus() {
        return this.status;
    }

    public void setStatus(ProdutoStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getValidador() {
        return this.validador;
    }

    public void setValidador(String validador) {
        this.validador = validador;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProdutoEntity)) {
            return false;
        }
        ProdutoEntity that = (ProdutoEntity)o;
        return Objects.equals(this.id, that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }
}
