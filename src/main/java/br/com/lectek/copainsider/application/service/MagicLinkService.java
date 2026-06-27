package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PasswordResetTokenEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PasswordResetTokenRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Generates and validates one-time magic-login links.
 *
 * Reuses the password_reset_token table — the token is marked as used
 * immediately after the user authenticates, preventing replay.
 */
@Service
public class MagicLinkService {

    private static final Logger log = LoggerFactory.getLogger(MagicLinkService.class);
    private static final int TOKEN_TTL_MINUTES = 15;

    private final UsuarioRepository          usuarioRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final MailSenderAdapter          mail;
    private final SecureRandom               random = new SecureRandom();
    private final String                     baseUrl;

    public MagicLinkService(UsuarioRepository usuarioRepo,
                             PasswordResetTokenRepository tokenRepo,
                             MailSenderAdapter mail,
                             @Value("${app.web.base-url:http://localhost:18090}") String baseUrl) {
        this.usuarioRepo = usuarioRepo;
        this.tokenRepo   = tokenRepo;
        this.mail        = mail;
        this.baseUrl     = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    /**
     * Generates a magic-login token for the given email and sends the link.
     * Does nothing if the email is not found (silent — prevents enumeration).
     */
    @Transactional
    public void solicitar(String email) {
        if (email == null || email.isBlank()) return;
        String normalized = email.trim().toLowerCase();

        Optional<UsuarioEntity> opt = usuarioRepo.findByEmailIgnoreCase(normalized);
        if (opt.isEmpty()) {
            log.debug("[magic-link] e-mail '{}' não encontrado — silencioso.", normalized);
            return;
        }

        UsuarioEntity user = opt.get();
        tokenRepo.deleteByUsuarioId(user.getId());

        String token = generateToken();
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setToken(token);
        entity.setUsuarioId(user.getId());
        entity.setExpiraEm(LocalDateTime.now().plusMinutes(TOKEN_TTL_MINUTES));
        tokenRepo.save(entity);

        String link = baseUrl + "/auth/magic-link?token=" + token;
        mail.send(user.getEmail(), "O teu link de acesso — Copa Insider", buildHtml(user.getNome(), link), null);
        log.info("[magic-link] link emitido para userId={} (expira em {} min).", user.getId(), TOKEN_TTL_MINUTES);
    }

    /**
     * Validates token and returns the associated user.
     * Marks the token as used to prevent replay.
     */
    @Transactional
    public Optional<UsuarioEntity> consumir(String token) {
        return tokenRepo.findByToken(token)
                .filter(t -> !t.isUsado())
                .filter(t -> t.getExpiraEm().isAfter(LocalDateTime.now()))
                .flatMap(t -> {
                    t.setUsado(true);
                    t.setUsadoEm(LocalDateTime.now());
                    tokenRepo.save(t);
                    return usuarioRepo.findById(t.getUsuarioId());
                });
    }

    private String generateToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String buildHtml(String nome, String link) {
        return """
               <!doctype html>
               <html lang="pt-BR">
               <head>
                 <meta charset="UTF-8">
                 <title>Acesso Copa Insider</title>
                 <style>
                   body{font-family:Arial,Helvetica,sans-serif;line-height:1.6;margin:0;padding:24px;background:#f6f7f9;color:#222}
                   .card{max-width:520px;margin:0 auto;background:#fff;border:1px solid #e6e8eb;border-radius:12px;padding:28px}
                   .btn{display:inline-block;padding:13px 22px;border-radius:8px;text-decoration:none;font-weight:600}
                   .btn-gold{background:#e8b930;color:#0a0a0f}
                   .muted{color:#6b7280;font-size:12px;margin-top:16px}
                 </style>
               </head>
               <body>
                 <div class="card">
                   <p>Olá, <strong>%s</strong>!</p>
                   <p>Clica no botão abaixo para entrares na tua conta <strong>Copa Insider</strong>.<br>
                      O link é válido por <strong>%d minutos</strong> e de uso único.</p>
                   <p><a class="btn btn-gold" href="%s" target="_blank" rel="noopener">Entrar agora →</a></p>
                   <p class="muted">Se não pediste este link, podes ignorar este e-mail em segurança.</p>
                 </div>
               </body>
               </html>
               """.formatted(nome, TOKEN_TTL_MINUTES, link);
    }
}
