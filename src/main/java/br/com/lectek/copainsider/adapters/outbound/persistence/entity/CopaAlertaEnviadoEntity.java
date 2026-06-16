package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "copa_alerta_enviado",
       uniqueConstraints = @UniqueConstraint(name = "uk_alerta_partida_email",
                                              columnNames = {"partida_id", "email"}))
public class CopaAlertaEnviadoEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partida_id", nullable = false)
    private Long partidaId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "enviado_em", nullable = false)
    private LocalDateTime enviadoEm;

    public CopaAlertaEnviadoEntity() {}

    public CopaAlertaEnviadoEntity(Long partidaId, String email) {
        this.partidaId = partidaId;
        this.email     = email.toLowerCase();
        this.enviadoEm = LocalDateTime.now();
    }

    public Long getId()             { return id; }
    public Long getPartidaId()      { return partidaId; }
    public String getEmail()        { return email; }
    public LocalDateTime getEnviadoEm() { return enviadoEm; }
}
