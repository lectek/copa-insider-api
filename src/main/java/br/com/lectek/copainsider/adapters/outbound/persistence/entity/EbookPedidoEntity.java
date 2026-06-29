package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import br.com.lectek.copainsider.domain.enums.EbookStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
    name = "ebook_pedido",
    indexes = {
        @Index(name = "idx_ebook_email",  columnList = "comprador_email"),
        @Index(name = "idx_ebook_status", columnList = "status"),
        @Index(name = "idx_ebook_token",  columnList = "download_token")
    }
)
public class EbookPedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String transacao;

    @Column(name = "comprador_email", nullable = false, length = 200)
    private String compradorEmail;

    @Column(name = "comprador_nome", nullable = false, length = 200)
    private String compradorNome;

    @Column(name = "selecao_code", length = 10)
    private String selecaoCode;

    @Column(name = "selecao_nome", length = 100)
    private String selecaoNome;

    @Column(length = 10)
    private String idioma;

    @Column(name = "pdf_caminho", length = 500)
    private String pdfCaminho;

    @Column(name = "download_token", unique = true, length = 100)
    private String downloadToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EbookStatus status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "gerado_em")
    private Instant geradoEm;

    public EbookPedidoEntity() {}

    public EbookPedidoEntity(String transacao, String compradorEmail, String compradorNome, EbookStatus status) {
        this.transacao = transacao;
        this.compradorEmail = compradorEmail;
        this.compradorNome = compradorNome;
        this.status = status;
        this.criadoEm = Instant.now();
    }

    public void definirSelecao(String selecaoCode, String selecaoNome, String idioma) {
        this.selecaoCode = selecaoCode;
        this.selecaoNome = selecaoNome;
        this.idioma = idioma;
        if (this.status == EbookStatus.AGUARDANDO_SELECAO) {
            this.status = EbookStatus.PENDENTE;
        }
    }

    public void marcarGerando() {
        this.status = EbookStatus.GERANDO;
    }

    public void marcarPronto(String pdfCaminho, String downloadToken) {
        this.pdfCaminho = pdfCaminho;
        this.downloadToken = downloadToken;
        this.status = EbookStatus.PRONTO;
        this.geradoEm = Instant.now();
    }

    public void marcarErro() {
        this.status = EbookStatus.ERRO;
    }

    public Long getId()                 { return id; }
    public String getTransacao()        { return transacao; }
    public String getCompradorEmail()   { return compradorEmail; }
    public String getCompradorNome()    { return compradorNome; }
    public String getSelecaoCode()      { return selecaoCode; }
    public String getSelecaoNome()      { return selecaoNome; }
    public String getIdioma()           { return idioma; }
    public String getPdfCaminho()       { return pdfCaminho; }
    public String getDownloadToken()    { return downloadToken; }
    public EbookStatus getStatus()      { return status; }
    public Instant getCriadoEm()        { return criadoEm; }
    public Instant getGeradoEm()        { return geradoEm; }
}
