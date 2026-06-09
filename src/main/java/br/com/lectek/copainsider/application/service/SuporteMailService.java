package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.lectek.copainsider.adapters.outbound.email.config.AppMailProperties;
import br.com.lectek.copainsider.application.dto.request.EmailSuporteRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Validated
@Service
@RequiredArgsConstructor
public class SuporteMailService {

    private final TemplateEngine templateEngine;
    private final MailSenderAdapter sender;
    private final AppMailProperties mailProps;

    @Value("${app.company.support-email:no-reply@local.copainsider}")
    private String suporteEmail;

    @Value("${app.company.name:Rede Mais Farma}")
    private String companyName;

    @Value("${app.company.site:https://local.copainsider}")
    private String companySite;

    @Value("${mail.template.suporte:pages/mail/suporte}")
    private String suporteTemplate;

    /**
     * Envia o e-mail de suporte renderizando o template HTML.
     */
    public void enviar(@Valid @NotNull EmailSuporteRequestDTO req) {
        // ---- contexto Thymeleaf
        Context ctx = new Context(Locale.of("pt", "BR"));
        Map<String, Object> company = Map.of(
                "name", Optional.ofNullable(companyName).orElse("Rede Mais Farma"),
                "site", Optional.ofNullable(companySite).orElse("https://local.copainsider")
        );
        ctx.setVariables(Map.of(
                "req", req,
                "cssUrl", mailProps.getCssUrl(),
                "company", company
        ));

        String html = templateEngine.process(suporteTemplate, ctx);

        // ---- BCC (string única com separadores diversos -> lista única limpinha)
        List<String> bcc = parseBcc(req.getBcc());

        // ---- assunto (com fallbacks)
        String assunto = buildAssunto(req);

        String tenantId = Optional.ofNullable(req.getTenantId()).orElse("-");
        String traceId  = Optional.ofNullable(req.getTraceId()).map(UUID::toString).orElse("-");

        try {
            log.info("[suporte] Enviando e-mail | to={}, assunto='{}', bcc={}, tenantId={}, traceId={}",
                    suporteEmail, assunto, bcc, tenantId, traceId);

            // Caso o seu MailSenderAdapter suporte anexos futuramente:
            // sender.send(suporteEmail, assunto, html, bcc, req.getAnexos());
            sender.send(suporteEmail, assunto, html, bcc);

            log.info("[suporte] E-mail enviado | to={}, assunto='{}'", suporteEmail, assunto);
        } catch (Exception ex) {
            log.error("[suporte] Falha ao enviar e-mail | to={}, assunto='{}', tenantId={}, traceId={}, erro={}",
                    suporteEmail, assunto, tenantId, traceId, ex.getMessage(), ex);
            throw ex; // deixamos o RestExceptionTranslator transformar em 5xx
        }
    }

    // ----------------- helpers -----------------

    private List<String> parseBcc(String raw) {
        if (raw == null || raw.isBlank()) return Collections.emptyList();
        // aceita vírgula, ponto e vírgula ou espaços múltiplos como separadores
        return Stream.of(raw.split("[,;\\s]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toUnmodifiableList());
    }

    private String buildAssunto(EmailSuporteRequestDTO req) {
        String cat = Optional.ofNullable(req.getCategoria()).map(Enum::name).orElse("OUTROS");
        String assuntoReq = Optional.ofNullable(req.getAssunto()).filter(s -> !s.isBlank()).orElse("Sem assunto");
        return "[SUPORTE] " + cat + " | " + assuntoReq;
    }
}
