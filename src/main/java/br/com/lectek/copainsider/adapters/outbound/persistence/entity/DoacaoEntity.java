package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "doacao")
public class DoacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stripe_session_id", unique = true, length = 200)
    private String stripeSessionId;

    @Column(name = "doador_email", length = 200)
    private String doadorEmail;

    @Column(name = "doador_nome", length = 100)
    private String doadorNome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, length = 10)
    private String moeda = "EUR";

    @Column(nullable = false, length = 30)
    private String status = "PENDENTE";

    @Column(columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Long getId() { return id; }
    public String getStripeSessionId() { return stripeSessionId; }
    public String getDoadorEmail() { return doadorEmail; }
    public String getDoadorNome() { return doadorNome; }
    public BigDecimal getValor() { return valor; }
    public String getMoeda() { return moeda; }
    public String getStatus() { return status; }
    public String getMensagem() { return mensagem; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    public void setStripeSessionId(String v) { this.stripeSessionId = v; }
    public void setDoadorEmail(String v) { this.doadorEmail = v; }
    public void setDoadorNome(String v) { this.doadorNome = v; }
    public void setValor(BigDecimal v) { this.valor = v; }
    public void setMoeda(String v) { this.moeda = v; }
    public void setStatus(String v) { this.status = v; }
    public void setMensagem(String v) { this.mensagem = v; }
    public void setCriadoEm(LocalDateTime v) { this.criadoEm = v; }
}
