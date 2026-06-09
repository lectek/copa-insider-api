package br.com.lectek.copainsider.application.dto.response;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Schema(name = "FiltroRelatorioResponseDTO", description = "Resultado do relatório com dados agregados e detalhes")
@JsonInclude(JsonInclude.Include.NON_EMPTY) // evita campos vazios no JSON
public class FiltroRelatorioResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description="ID único da geração do relatório", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("relatorioId")
    private UUID relatorioId;

    @Schema(description="Timestamp de geração do relatório", type="string", format="date-time", example="2025-07-04T16:30:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Fortaleza")
    @JsonProperty("dataGeracao")
    private LocalDateTime dataGeracao;

    @Schema(description="Username do solicitante", example="joao.silva")
    @JsonProperty("usuarioSolicitante")
    private String usuarioSolicitante;

    @Schema(description="ID do tenant (multi-inquilino)", example="copainsider-001")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description="Filtros aplicados (campo -> valor)", example="{\"dataInicial\":\"2025-01-01\",\"dataFinal\":\"2025-06-30\"}")
    @JsonProperty("filtrosAplicados")
    private Map<String, String> filtrosAplicados;

    @Schema(description="Tempo de processamento (ms)", example="1234")
    @JsonProperty("tempoProcessamentoMs")
    private Long tempoProcessamentoMs;

    @Schema(description="Métricas agregadas chave-valor", example="{\"totalVendas\":1000.50,\"numeroPedidos\":150}")
    @JsonProperty("metricasAgregadas")
    private Map<String, BigDecimal> metricasAgregadas;

    // alias de leitura pra compatibilidade com payloads antigos (Aggregadas)
    @JsonProperty("metricasAggregadas")
    @JsonAlias("metricasAggregadas")
    private void setMetricasAggregadasAlias(Map<String, BigDecimal> alias) {
        this.metricasAgregadas = alias;
    }

    @Schema(description="Informações de paginação do resultado")
    @JsonProperty("paginacao")
    private PaginacaoDTO paginacao;

    @Schema(description="Lista de critérios de ordenação", example="[\"dataVenda,DESC\"]")
    @JsonProperty("ordenacao")
    private List<String> ordenacao;

    // Listas específicas (use a(s) que fizer(em) sentido no endpoint)
    @Schema(description="Lista de vendas detalhadas")
    @JsonProperty("detalhesVendas")
    private List<VendaDTO> detalhesVendas;

    @Schema(description="Lista de produtos detalhados")
    @JsonProperty("detalhesProdutos")
    private List<ProdutoDTO> detalhesProdutos;

    @Schema(description="Lista de clientes detalhados")
    @JsonProperty("detalhesClientes")
    private List<ClienteResumoDTO> detalhesClientes;

    @Schema(description="Lista de atendentes detalhados")
    @JsonProperty("detalhesAtendentes")
    private List<AtendenteDTO> detalhesAtendentes;

    public FiltroRelatorioResponseDTO() {}

    // --- Getters/Setters (mantidos para compatibilidade) ---
    public UUID getRelatorioId() { return relatorioId; }
    public void setRelatorioId(UUID relatorioId) { this.relatorioId = relatorioId; }

    public LocalDateTime getDataGeracao() { return dataGeracao; }
    public void setDataGeracao(LocalDateTime dataGeracao) { this.dataGeracao = dataGeracao; }

    public String getUsuarioSolicitante() { return usuarioSolicitante; }
    public void setUsuarioSolicitante(String usuarioSolicitante) { this.usuarioSolicitante = usuarioSolicitante; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Map<String, String> getFiltrosAplicados() { return filtrosAplicados; }
    public void setFiltrosAplicados(Map<String, String> filtrosAplicados) { this.filtrosAplicados = filtrosAplicados; }

    public Long getTempoProcessamentoMs() { return tempoProcessamentoMs; }
    public void setTempoProcessamentoMs(Long tempoProcessamentoMs) { this.tempoProcessamentoMs = tempoProcessamentoMs; }

    public Map<String, BigDecimal> getMetricasAgregadas() { return metricasAgregadas; }
    public void setMetricasAgregadas(Map<String, BigDecimal> metricasAgregadas) { this.metricasAgregadas = metricasAgregadas; }

    public PaginacaoDTO getPaginacao() { return paginacao; }
    public void setPaginacao(PaginacaoDTO paginacao) { this.paginacao = paginacao; }

    public List<String> getOrdenacao() { return ordenacao; }
    public void setOrdenacao(List<String> ordenacao) { this.ordenacao = ordenacao; }

    public List<VendaDTO> getDetalhesVendas() { return detalhesVendas; }
    public void setDetalhesVendas(List<VendaDTO> detalhesVendas) { this.detalhesVendas = detalhesVendas; }

    public List<ProdutoDTO> getDetalhesProdutos() { return detalhesProdutos; }
    public void setDetalhesProdutos(List<ProdutoDTO> detalhesProdutos) { this.detalhesProdutos = detalhesProdutos; }

    public List<ClienteResumoDTO> getDetalhesClientes() { return detalhesClientes; }
    public void setDetalhesClientes(List<ClienteResumoDTO> detalhesClientes) { this.detalhesClientes = detalhesClientes; }

    public List<AtendenteDTO> getDetalhesAtendentes() { return detalhesAtendentes; }
    public void setDetalhesAtendentes(List<AtendenteDTO> detalhesAtendentes) { this.detalhesAtendentes = detalhesAtendentes; }

    // equals/hashCode/toString — (mantive os seus; pode gerar via IDE)

    // ---------- Tipos internos (mantidos e refinados) ----------
    @Schema(name="PaginacaoDTO", description="Dados de paginação dos resultados")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginacaoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description="Página atual (zero-based)", example="0")
        @JsonProperty("pagina")
        private Integer pagina;

        @Schema(description="Tamanho da página", example="50")
        @JsonProperty("tamanho")
        private Integer tamanho;

        @Schema(description="Total de páginas", example="20")
        @JsonProperty("totalPaginas")
        private Integer totalPaginas;

        @Schema(description="Total de registros", example="1000")
        @JsonProperty("totalRegistros")
        private Long totalRegistros;

        public PaginacaoDTO() {}
        public PaginacaoDTO(Integer pagina, Integer tamanho, Integer totalPaginas, Long totalRegistros) {
            this.pagina = pagina; this.tamanho = tamanho; this.totalPaginas = totalPaginas; this.totalRegistros = totalRegistros;
        }

        public Integer getPagina() { return pagina; }
        public void setPagina(Integer pagina) { this.pagina = pagina; }
        public Integer getTamanho() { return tamanho; }
        public void setTamanho(Integer tamanho) { this.tamanho = tamanho; }
        public Integer getTotalPaginas() { return totalPaginas; }
        public void setTotalPaginas(Integer totalPaginas) { this.totalPaginas = totalPaginas; }
        public Long getTotalRegistros() { return totalRegistros; }
        public void setTotalRegistros(Long totalRegistros) { this.totalRegistros = totalRegistros; }

        /** Factory util pra montar a partir de um Page<?>. */
        public static PaginacaoDTO fromPage(org.springframework.data.domain.Page<?> page) {
            return new PaginacaoDTO(page.getNumber(), page.getSize(), page.getTotalPages(), page.getTotalElements());
        }
    }

    @Schema(name="ProdutoDTO", description="Dados resumidos de cada produto")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProdutoDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(description="ID do produto", example="789")
        @JsonProperty("produtoId")
        private Long produtoId;
        @Schema(description="Nome do produto", example="Dipirona 500mg")
        @JsonProperty("nome")
        private String nome;
        @Schema(description="Categoria do produto", example="ANALGESICO")
        @JsonProperty("categoria")
        private String categoria;
        @Schema(description="Valor total vendido no período", example="1234.56")
        @JsonProperty("valorTotalVendido")
        private BigDecimal valorTotalVendido;

        public ProdutoDTO() {}
        public ProdutoDTO(Long produtoId, String nome, String categoria, BigDecimal valorTotalVendido) {
            this.produtoId = produtoId; this.nome = nome; this.categoria = categoria; this.valorTotalVendido = valorTotalVendido;
        }
        public Long getProdutoId() { return produtoId; }
        public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
        public BigDecimal getValorTotalVendido() { return valorTotalVendido; }
        public void setValorTotalVendido(BigDecimal valorTotalVendido) { this.valorTotalVendido = valorTotalVendido; }
    }

    @Schema(name="VendaDTO", description="Dados resumidos de cada venda")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VendaDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(description="ID da venda", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @JsonProperty("vendaId")
        private UUID vendaId;
        @Schema(description="Data da venda", type="string", format="date-time", example="2025-07-01T10:00:00")
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss", timezone = "America/Fortaleza")
        @JsonProperty("dataVenda")
        private LocalDateTime dataVenda;
        @Schema(description="Valor total da venda", example="200.00")
        @JsonProperty("valorVenda")
        private BigDecimal valorVenda;

        public VendaDTO() {}
        public VendaDTO(UUID vendaId, LocalDateTime dataVenda, BigDecimal valorVenda) {
            this.vendaId = vendaId; this.dataVenda = dataVenda; this.valorVenda = valorVenda;
        }
        public UUID getVendaId() { return vendaId; }
        public void setVendaId(UUID vendaId) { this.vendaId = vendaId; }
        public LocalDateTime getDataVenda() { return dataVenda; }
        public void setDataVenda(LocalDateTime dataVenda) { this.dataVenda = dataVenda; }
        public BigDecimal getValorVenda() { return valorVenda; }
        public void setValorVenda(BigDecimal valorVenda) { this.valorVenda = valorVenda; }
    }

    // --- Tipos referenciados (stubs) ---
    public static class ClienteResumoDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id; private String nome; private String cpf; private String email; private String telefone;
        private Long qtdPedidos; private BigDecimal valorTotal;
        public ClienteResumoDTO() {}
        public ClienteResumoDTO(Long id, String nome, String cpf, String email, String telefone, Long qtdPedidos, BigDecimal valorTotal){
            this.id=id; this.nome=nome; this.cpf=cpf; this.email=email; this.telefone=telefone; this.qtdPedidos=qtdPedidos; this.valorTotal=valorTotal;
        }
        // getters/setters…
        public Long getId(){return id;} public void setId(Long id){this.id=id;}
        public String getNome(){return nome;} public void setNome(String nome){this.nome=nome;}
        public String getCpf(){return cpf;} public void setCpf(String cpf){this.cpf=cpf;}
        public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
        public String getTelefone(){return telefone;} public void setTelefone(String telefone){this.telefone=telefone;}
        public Long getQtdPedidos(){return qtdPedidos;} public void setQtdPedidos(Long qtdPedidos){this.qtdPedidos=qtdPedidos;}
        public BigDecimal getValorTotal(){return valorTotal;} public void setValorTotal(BigDecimal valorTotal){this.valorTotal=valorTotal;}
    }

    public static class AtendenteDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long id; private String nome; private String matricula; private Long qtdAtendimentos;
        public AtendenteDTO() {}
        public AtendenteDTO(Long id, String nome, String matricula, Long qtdAtendimentos){
            this.id=id; this.nome=nome; this.matricula=matricula; this.qtdAtendimentos=qtdAtendimentos;
        }
        // getters/setters…
        public Long getId(){return id;} public void setId(Long id){this.id=id;}
        public String getNome(){return nome;} public void setNome(String nome){this.nome=nome;}
        public String getMatricula(){return matricula;} public void setMatricula(String matricula){this.matricula=matricula;}
        public Long getQtdAtendimentos(){return qtdAtendimentos;} public void setQtdAtendimentos(Long qtdAtendimentos){this.qtdAtendimentos=qtdAtendimentos;}
    }
}
