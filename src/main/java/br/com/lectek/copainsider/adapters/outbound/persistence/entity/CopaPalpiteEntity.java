package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "copa_palpite",
       uniqueConstraints = @UniqueConstraint(name = "uk_palpite_email_partida",
                                              columnNames = {"email", "partida_id"}))
public class CopaPalpiteEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "partida_id", nullable = false)
    private Long partidaId;

    @Column(name = "gols_casa", nullable = false)
    private int golsCasa;

    @Column(name = "gols_visitante", nullable = false)
    private int golsVisitante;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public CopaPalpiteEntity() {}

    public CopaPalpiteEntity(String email, Long partidaId) {
        this.email     = email.toLowerCase();
        this.partidaId = partidaId;
        this.criadoEm  = LocalDateTime.now();
    }

    public Long getId()             { return id; }
    public String getEmail()        { return email; }
    public Long getPartidaId()      { return partidaId; }
    public int getGolsCasa()        { return golsCasa; }
    public int getGolsVisitante()   { return golsVisitante; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    public void setGolsCasa(int v)      { this.golsCasa = v; }
    public void setGolsVisitante(int v) { this.golsVisitante = v; }
}
