package br.com.lectek.copainsider.application.copa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaAlertaEnviadoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaAcessoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaAlertaEnviadoJPARepository;
import br.com.lectek.copainsider.application.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertaJogoService {

    private static final Logger log = LoggerFactory.getLogger(AlertaJogoService.class);
    private static final DateTimeFormatter HORA_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final Copa2026DataService           copaData;
    private final CopaAcessoJPARepository       acessoRepo;
    private final CopaAlertaEnviadoJPARepository alertaRepo;
    private final MailService                   mailService;

    @Value("${app.web.base-url:https://allaboutworldcup2026.com}")
    private String baseUrl;

    public AlertaJogoService(Copa2026DataService copaData,
                              CopaAcessoJPARepository acessoRepo,
                              CopaAlertaEnviadoJPARepository alertaRepo,
                              MailService mailService) {
        this.copaData   = copaData;
        this.acessoRepo = acessoRepo;
        this.alertaRepo = alertaRepo;
        this.mailService = mailService;
    }

    @Scheduled(fixedRate = 300_000)
    public void verificarEEnviar() {
        try {
            LocalDateTime agora        = LocalDateTime.now();
            LocalDateTime janelaInicio = agora.plusMinutes(30);
            LocalDateTime janelaFim    = agora.plusMinutes(35);

            List<PartidaVM> jogos = copaData.listPartidas().stream()
                    .filter(p -> p.agendada()
                            && p.dataHora() != null
                            && !p.dataHora().isBefore(janelaInicio)
                            && p.dataHora().isBefore(janelaFim))
                    .toList();

            if (jogos.isEmpty()) return;

            List<String> emails = acessoRepo.findDistinctEmails();
            if (emails.isEmpty()) return;

            log.info("[alerta-jogo] {} jogo(s) em 30 min — {} destinatário(s)", jogos.size(), emails.size());

            for (PartidaVM jogo : jogos) {
                for (String email : emails) {
                    if (alertaRepo.existsByPartidaIdAndEmail(jogo.id(), email)) continue;
                    try {
                        enviarAlerta(email, jogo);
                        alertaRepo.save(new CopaAlertaEnviadoEntity(jogo.id(), email));
                    } catch (Exception ex) {
                        log.warn("[alerta-jogo] falha ao enviar para {}: {}", email, ex.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            log.error("[alerta-jogo] erro no scheduler: {}", ex.getMessage());
        }
    }

    private void enviarAlerta(String email, PartidaVM jogo) {
        String hora = jogo.dataHora() != null ? jogo.dataHora().format(HORA_FMT) : "";
        String assunto = jogo.bandeiraCasa() + " " + jogo.selecaoCasa()
                + " × " + jogo.selecaoVisitante() + " " + jogo.bandeiraVisitante()
                + " — começa às " + hora;

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("jogo",    jogo);
        ctx.put("baseUrl", baseUrl);
        ctx.put("chatUrl", baseUrl + "/jogo/" + jogo.id() + "/sala");
        ctx.put("hora",    hora);

        mailService.sendTemplate(email, assunto, "mail/alerta-jogo", ctx, null);
        log.info("[alerta-jogo] enviado para {} — {}", email, assunto);
    }
}
