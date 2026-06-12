package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sala_chat_mensagem")
public class SalaChatMensagemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "jogo_id", nullable = false)
    private Long jogoId;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "autor_nome", nullable = false, length = 80)
    private String autorNome;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(nullable = false, length = 20)
    private String tipo = "TEXTO";

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Long getId() { return id; }
    public Long getJogoId() { return jogoId; }
    public String getSessionId() { return sessionId; }
    public String getAutorNome() { return autorNome; }
    public String getMensagem() { return mensagem; }
    public String getTipo() { return tipo; }
    public boolean isAtivo() { return ativo; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    public void setJogoId(Long v) { this.jogoId = v; }
    public void setSessionId(String v) { this.sessionId = v; }
    public void setAutorNome(String v) { this.autorNome = v; }
    public void setMensagem(String v) { this.mensagem = v; }
    public void setTipo(String v) { this.tipo = v; }
    public void setAtivo(boolean v) { this.ativo = v; }
    public void setCriadoEm(LocalDateTime v) { this.criadoEm = v; }
}
