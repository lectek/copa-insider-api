package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PasswordResetTokenEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PasswordResetTokenRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderAdapter mailAdapter;
    private final SecureRandom random = new SecureRandom();
    private final String baseUrl;

    public PasswordResetService(UsuarioRepository usuarioRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                MailSenderAdapter mailAdapter,
                                @Value("${app.web.base-url:http://localhost:18090}") String baseUrl) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailAdapter = mailAdapter;
        this.baseUrl = normalizeBaseUrl(baseUrl);
    }

    @Transactional
    public void solicitarResetPorEmailOuCpf(String emailOuCpf) {
        tokenRepository.deleteByUsadoIsTrueOrExpiraEmBefore(LocalDateTime.now().minusDays(1));
        if (emailOuCpf == null || emailOuCpf.isBlank()) return;

        String raw = emailOuCpf.trim();
        String email = raw.contains("@") ? raw.toLowerCase() : null;
        String cpf   = raw.contains("@") ? null : raw.replaceAll("\\D", "");

        Optional<UsuarioEntity> opt = (email != null)
                ? usuarioRepository.findByEmailOrCpf(email)
                : usuarioRepository.findByEmailOrCpf(cpf);

        if (opt.isEmpty()) {
            log.debug("Solicitação de reset para '{}': usuário não encontrado (silencioso).", raw);
            return;
        }

        UsuarioEntity user = opt.get();
        tokenRepository.deleteByUsuarioId(user.getId());

        String token = gerarToken();
        PasswordResetTokenEntity prt = new PasswordResetTokenEntity();
        prt.setToken(token);
        prt.setUsuarioId(user.getId());
        prt.setExpiraEm(LocalDateTime.now().plusMinutes(30));
        tokenRepository.save(prt);

        String link = baseUrl + "/cliente/auth/resetar-senha?token=" + token;
        String assunto = "Redefinição de senha - RedeMaisFarma";
        String html = buildEmailHtml(user.getNome(), link);
        mailAdapter.send(user.getEmail(), assunto, html, null);

        log.info("Link de reset emitido para usuário id={} (expira em 30 min).", user.getId());
    }

    public Optional<UsuarioEntity> validarToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> !t.isUsado())
                .filter(t -> t.getExpiraEm().isAfter(LocalDateTime.now()))
                .flatMap(t -> usuarioRepository.findById(t.getUsuarioId()));
    }

    @Transactional
    public boolean aplicarNovaSenha(String token, String novaSenha) {
        Optional<PasswordResetTokenEntity> opt = tokenRepository.findByToken(token)
                .filter(t -> !t.isUsado())
                .filter(t -> t.getExpiraEm().isAfter(LocalDateTime.now()));

        if (opt.isEmpty()) return false;

        PasswordResetTokenEntity t2 = opt.get();
        UsuarioEntity u = usuarioRepository.findById(t2.getUsuarioId()).orElse(null);
        if (u == null) return false;

        u.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(u);

        t2.setUsado(true);
        t2.setUsadoEm(LocalDateTime.now());
        tokenRepository.save(t2);

        log.info("Senha redefinida para usuário id={} via token.", u.getId());
        return true;
    }

    private String gerarToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String normalizeBaseUrl(String url) {
        if (url == null || url.isBlank()) return "http://localhost:18090";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String buildEmailHtml(String nome, String link) {
        return """
               <!doctype html>
               <html lang="pt-BR">
               <head>
                 <meta charset="UTF-8">
                 <meta name="viewport" content="width=device-width,initial-scale=1">
                 <title>Redefinição de senha</title>
                 <style>
                   body{font-family:Arial,Helvetica,sans-serif;line-height:1.5;margin:0;padding:24px;background:#f6f7f9;color:#222}
                   .card{max-width:560px;margin:0 auto;background:#fff;border:1px solid #e6e8eb;border-radius:12px;padding:24px}
                   .btn{display:inline-block;padding:12px 18px;border-radius:8px;text-decoration:none}
                   .btn-primary{background:#2563eb;color:#fff}
                   .muted{color:#6b7280;font-size:12px}
                 </style>
               </head>
               <body>
                 <div class="card">
                   <p>Olá, <strong>%s</strong>!</p>
                   <p>Recebemos um pedido para redefinir sua senha na <strong>RedeMaisFarma</strong>.</p>
                   <p>Para continuar, clique no botão abaixo (o link é válido por <strong>30 minutos</strong>):</p>
                   <p><a class="btn btn-primary" href="%s" target="_blank" rel="noopener">Redefinir minha senha</a></p>
                   <p class="muted">Se você não solicitou, pode ignorar este e-mail.</p>
                 </div>
               </body>
               </html>
               """.formatted(nome, link);
    }
}
