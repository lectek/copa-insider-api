package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "copa_compra")
public class CopaCompraEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String transacao;

    @Column(nullable = false, length = 200)
    private String compradorEmail;

    @Column(nullable = false, length = 200)
    private String compradorNome;

    @Column(length = 200)
    private String produtoNome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(length = 10)
    private String moeda;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(nullable = false)
    private Instant criadoEm;

    @Column
    private boolean emailEnviado;

    @Column
    private Instant emailEnviadoEm;

    public CopaCompraEntity() {}

    public CopaCompraEntity(String transacao, String compradorEmail, String compradorNome,
                             String produtoNome, BigDecimal valor, String moeda, String status) {
        this.transacao = transacao;
        this.compradorEmail = compradorEmail;
        this.compradorNome = compradorNome;
        this.produtoNome = produtoNome;
        this.valor = valor;
        this.moeda = moeda;
        this.status = status;
        this.criadoEm = Instant.now();
        this.emailEnviado = false;
    }

    public Long getId() { return id; }
    public String getTransacao() { return transacao; }
    public String getCompradorEmail() { return compradorEmail; }
    public String getCompradorNome() { return compradorNome; }
    public String getProdutoNome() { return produtoNome; }
    public BigDecimal getValor() { return valor; }
    public String getMoeda() { return moeda; }
    public String getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
    public boolean isEmailEnviado() { return emailEnviado; }
    public Instant getEmailEnviadoEm() { return emailEnviadoEm; }

    public void marcarEmailEnviado() {
        this.emailEnviado = true;
        this.emailEnviadoEm = Instant.now();
    }
}
