package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sala_voto_jogador",
       uniqueConstraints = @UniqueConstraint(name = "uk_voto",
               columnNames = {"jogo_id", "session_id", "jogador_slug"}))
public class SalaVotoJogadorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jogo_id", nullable = false)
    private Long jogoId;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "jogador_slug", nullable = false, length = 100)
    private String jogadorSlug;

    @Column(name = "selecao_slug", nullable = false, length = 100)
    private String selecaoSlug;

    @Column(nullable = false)
    private int nota;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Long getId() { return id; }
    public Long getJogoId() { return jogoId; }
    public String getSessionId() { return sessionId; }
    public String getJogadorSlug() { return jogadorSlug; }
    public String getSelecaoSlug() { return selecaoSlug; }
    public int getNota() { return nota; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    public void setJogoId(Long v) { this.jogoId = v; }
    public void setSessionId(String v) { this.sessionId = v; }
    public void setJogadorSlug(String v) { this.jogadorSlug = v; }
    public void setSelecaoSlug(String v) { this.selecaoSlug = v; }
    public void setNota(int v) { this.nota = v; }
    public void setCriadoEm(LocalDateTime v) { this.criadoEm = v; }
}
