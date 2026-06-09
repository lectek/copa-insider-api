package br.com.redemaisfarma.application.service.otp;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.adapters.outbound.sms.adapter.SmsSenderAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service("otpService")
@Profile({"dev", "test", "docker"})
public class OtpService implements OtpServicePort {
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    private static final Duration OTP_TTL = Duration.ofMinutes(10);
    private static final Duration TOKEN_TTL = Duration.ofMinutes(10);
    private static final int MAX_ATTEMPTS = 5;
    private static final int COOLDOWN_SEC = 60;

    private final SecureRandom rnd = new SecureRandom();

    private final Map<String, OtpEntry> deliveries = new ConcurrentHashMap<>();
    private final Map<String, TokenEntry> tokens = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastSendByKey = new ConcurrentHashMap<>();
    private final Map<String, String> lastDeliveryIdByKey = new ConcurrentHashMap<>();

    private final MailSenderAdapter mailer;
    private final SmsSenderAdapter smsSender;
    private final Environment env;

    public OtpService(MailSenderAdapter mailer, SmsSenderAdapter smsSender, Environment env) {
        this.mailer = mailer;
        this.smsSender = smsSender;
        this.env = env;
    }

    @Override
    public StartResult start(String canalStr, String destino, String previousDeliveryId) {
        final Canal canal = Canal.valueOf(canalStr.toLowerCase());
        final String normalizedDestino = normalizeDestino(canal, destino);
        final String key = key(canal, normalizedDestino);

        final Instant now = Instant.now();
        final Instant last = lastSendByKey.get(key);

        boolean bypassCooldown = false;
        if (previousDeliveryId != null && !previousDeliveryId.isBlank()) {
            final String lastDeliveryForKey = lastDeliveryIdByKey.get(key);
            bypassCooldown = previousDeliveryId.equals(lastDeliveryForKey);
        }

        if (!bypassCooldown && last != null) {
            long secSinceLast = Duration.between(last, now).getSeconds();
            if (secSinceLast < COOLDOWN_SEC) {
                throw new OtpException("cooldown",
                        "Aguarde " + (COOLDOWN_SEC - secSinceLast) + "s para reenviar.");
            }
        }

        final String code = generateCode6();
        final String deliveryId = UUID.randomUUID().toString();

        sendOtp(canal, normalizedDestino, code);

        final OtpEntry entry = new OtpEntry(canal.name(), normalizedDestino, code, now, now.plus(OTP_TTL), 0);
        deliveries.put(deliveryId, entry);
        lastSendByKey.put(key, now);
        lastDeliveryIdByKey.put(key, deliveryId);

        final String demoCode = isProd() ? null : code;
        return new StartResult(deliveryId, maskDestino(canal, normalizedDestino), COOLDOWN_SEC, (int) OTP_TTL.getSeconds(), demoCode);
    }

    @Override
    public String verify(String deliveryId, String codeRaw) {
        final OtpEntry e = deliveries.get(deliveryId);
        if (e == null) throw new OtpException("expired", "CÃ³digo expirado.");

        final Instant now = Instant.now();
        if (now.isAfter(e.expiresAt())) {
            deliveries.remove(deliveryId);
            throw new OtpException("expired", "CÃ³digo expirado.");
        }

        if (e.attempts() >= MAX_ATTEMPTS) {
            deliveries.remove(deliveryId);
            throw new OtpException("too_many_attempts", "Muitas tentativas. Solicite novo cÃ³digo.");
        }

        final String code = (codeRaw == null) ? "" : codeRaw.replaceAll("\\D", "");
        if (!Objects.equals(e.code(), code)) {
            deliveries.put(deliveryId, new OtpEntry(e.canal(), e.destino(), e.code(), e.createdAt(),
                    e.expiresAt(), e.attempts() + 1));
            throw new OtpException("invalid", "CÃ³digo incorreto.");
        }

        deliveries.remove(deliveryId);
        final String token = UUID.randomUUID().toString();
        tokens.put(token, new TokenEntry(deliveryId, e.destino(), now.plus(TOKEN_TTL)));
        return token;
    }

    @Override
    public boolean consumeToken(String token) {
        final TokenEntry te = tokens.remove(token);
        return te != null && Instant.now().isBefore(te.expiresAt());
    }

    @Override
    public boolean consumeTokenForDestino(String token, String destino) {
        if (token == null || token.isBlank()) return false;
        final TokenEntry te = tokens.remove(token);
        if (te == null) return false;
        if (Instant.now().isAfter(te.expiresAt())) return false;

        final String normalizedInput = normalizeByGuess(destino);
        final boolean isEmail = normalizedInput.contains("@");
        return isEmail
                ? normalizedInput.equalsIgnoreCase(te.destinoNormalizado())
                : normalizedInput.equals(te.destinoNormalizado());
    }

    private boolean isProd() {
        return Arrays.stream(env.getActiveProfiles()).anyMatch(p -> "prod".equalsIgnoreCase(p));
    }

    private String key(Canal canal, String destino) {
        return canal.name() + "|" + destino;
    }

    private String generateCode6() {
        final StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }

    private String normalizeDestino(Canal canal, String v) {
        if (v == null) return "";
        return (canal == Canal.email) ? v.trim() : v.replaceAll("\\D", "");
    }

    private String normalizeByGuess(String v) {
        if (v == null) return "";
        return v.contains("@") ? v.trim() : v.replaceAll("\\D", "");
    }

    private String maskDestino(Canal canal, String v) {
        if (canal == Canal.email) {
            final int at = v.indexOf('@');
            if (at <= 1) return "***";
            final String name = v.substring(0, at);
            final String domain = v.substring(at);
            final char first = name.charAt(0);
            final char last = name.charAt(Math.max(0, name.length() - 1));
            return first + "****" + last + domain;
        }
        final String d = v.replaceAll("\\D", "");
        if (d.length() < 4) return "****";
        return "****" + d.substring(d.length() - 4);
    }

    private void sendOtp(Canal canal, String destino, String code) {
        if (canal == Canal.sms) {
            sendSms(destino, code);
            return;
        }

        sendEmail(destino, code);
    }

    private void sendEmail(String destino, String code) {
        final String subject = "Seu codigo RedeMaisFarma";
        final String html = """
                <div style="font-family:system-ui,Segoe UI,Arial,sans-serif">
                  <h2>Confirme seu cadastro</h2>
                  <p>Use este codigo para verificar seu e-mail:</p>
                  <p style="font-size:24px;letter-spacing:6px"><b>%s</b></p>
                  <p>Ele expira em %d minutos.</p>
                  <hr/><small>Se nao foi voce, ignore este e-mail.</small>
                </div>
                """.formatted(code, OTP_TTL.toMinutes());
        try {
            String messageId = mailer.send(destino, subject, html, null);
            if (messageId == null || messageId.startsWith("noop")) {
                throw new IllegalStateException("Email delivery adapter is disabled.");
            }
            log.info("OTP email sent: to={} messageId={}", maskDestino(Canal.email, destino), messageId);
        } catch (Exception ex) {
            log.error("OTP email send failed: to={}", maskDestino(Canal.email, destino), ex);
            throw new OtpException("email_unavailable", "Nao foi possivel enviar o codigo por E-mail agora. Tente novamente.");
        }
    }

    private void sendSms(String destino, String code) {
        String message = "RedeMaisFarma: seu codigo de verificacao e " + code
                + ". Valido por " + OTP_TTL.toMinutes() + " minutos.";
        try {
            String providerId = smsSender.send(destino, message);
            if (providerId == null || providerId.isBlank()) {
                throw new IllegalStateException("SMS delivery provider returned empty id.");
            }
            log.info("OTP sms sent: to={} providerId={}", maskDestino(Canal.sms, destino), providerId);
        } catch (Exception ex) {
            log.error("OTP sms send failed: to={}", maskDestino(Canal.sms, destino), ex);
            throw new OtpException(
                    "sms_unavailable",
                    "Nao foi possivel enviar o codigo por SMS agora. Tente novamente ou selecione E-mail."
            );
        }
    }

    public enum Canal { email, sms }

    private record OtpEntry(String canal, String destino, String code,
                            Instant createdAt, Instant expiresAt, int attempts) { }

    private record TokenEntry(String deliveryId, String destinoNormalizado, Instant expiresAt) { }
}
