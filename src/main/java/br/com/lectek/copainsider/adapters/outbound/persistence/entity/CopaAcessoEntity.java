package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "copa_acesso",
       uniqueConstraints = @UniqueConstraint(name = "uk_email_produto", columnNames = {"email", "produto_slug"}))
public class CopaAcessoEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "produto_slug", nullable = false, length = 100)
    private String produtoSlug;

    @Column(name = "transacao", length = 100)
    private String transacao;

    @Column(name = "concedido_em", nullable = false)
    private LocalDateTime concedidoEm;

    public CopaAcessoEntity() {}

    public CopaAcessoEntity(String email, String produtoSlug, String transacao) {
        this.email       = email.toLowerCase();
        this.produtoSlug = produtoSlug;
        this.transacao   = transacao;
        this.concedidoEm = LocalDateTime.now();
    }

    public Long getId()               { return id; }
    public String getEmail()          { return email; }
    public String getProdutoSlug()    { return produtoSlug; }
    public String getTransacao()      { return transacao; }
    public LocalDateTime getConcedidoEm() { return concedidoEm; }
}
