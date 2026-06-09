package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailDelivery;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailDeliveryRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.otp.OtpCodeEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.otp.OtpCodeRepository;
import br.com.lectek.copainsider.domain.user.Role;
import br.com.lectek.copainsider.domain.user.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class DevProvisioningService {

    private static final SecureRandom RNG = new SecureRandom();

    private final OtpCodeRepository otpRepo;
    private final EmailDeliveryRepository emailDeliveryRepo;
    private final UsuarioJpaRepository usuarioRepo;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final ObjectMapper objectMapper;

    // Fallback: se app.web.base-url não estiver no application, usa APP_WEB_BASE_URL do ambiente.
    private final String baseUrl;

    public DevProvisioningService(
            OtpCodeRepository otpRepo,
            EmailDeliveryRepository emailDeliveryRepo,
            UsuarioJpaRepository usuarioRepo,
            RoleRepository roleRepository,
            PasswordEncoder encoder,
            ObjectMapper objectMapper,
            @Value("${app.web.base-url:${APP_WEB_BASE_URL:http://localhost:8080}}") String baseUrl
    ) {
        this.otpRepo = otpRepo;
        this.emailDeliveryRepo = emailDeliveryRepo;
        this.usuarioRepo = usuarioRepo;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    @Transactional
    public String start(String email) {
        String code = randomCode6();
        byte[] salt = randomSalt();
        String codeHash = hashCode(code, salt);

        String token = UUID.randomUUID().toString();
        String deliveryId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        int ttlSec = 600;

        // Salva OTP
        OtpCodeEntity otp = new OtpCodeEntity();
        otp.setDeliveryId(deliveryId);
        otp.setDestination(email);
        otp.setCodeHash(codeHash);
        otp.setSalt(Base64.getEncoder().encodeToString(salt));
        otp.setTtlSeconds(ttlSec);
        otp.setAttempts(0);
        otp.setMaxAttempts(3);
        otp.setCreatedAt(now);
        otp.setExpiresAt(now.plusSeconds(ttlSec));
        otp.setStatus("PENDING");
        otp.setVerificationToken(token);
        otpRepo.save(otp);

        // Monta email
        String link = baseUrl + "/api/dev/provision/verify?token=" + token;
        String subject = "Código para criar acesso DEV";
        String html = """
                <div style="font-family:Arial,sans-serif">
                  <h3>Criação de acesso DEV</h3>
                  <p>Seu código é <b style="font-size:18px">%s</b> (expira em %d min).</p>
                  <p>Ou clique: <a href="%s">%s</a></p>
                </div>
                """.formatted(code, ttlSec / 60, link, link);

        // Persiste trilha de envio
        EmailDelivery ed = new EmailDelivery();
        ed.setPurpose("DEV_CREATE_CODE");
        ed.setDestination(email);
        ed.setProvider("SMTP");
        ed.setStatus("PENDING");
        ed.setAttempts(0);
        ed.setMessageId(null);
        ed.setPayloadJson(buildPayloadJson(subject, html, link, deliveryId));
        ed.setCreatedAt(now); // importante se coluna é NOT NULL
        // Se existir campo "deliveryId" na entidade, descomente:
        // ed.setDeliveryId(deliveryId);
        emailDeliveryRepo.save(ed);

        return deliveryId;
    }

    @Transactional
    public void verifyByToken(String token, String optionalPassword, String cpfIfNew, String nomeIfNew) {
        OtpCodeEntity otp = otpRepo.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("token inválido"));

        Instant now = Instant.now();
        if (!"PENDING".equalsIgnoreCase(otp.getStatus()) || now.isAfter(otp.getExpiresAt())) {
            throw new IllegalStateException("código expirado ou já utilizado");
        }

        otp.setStatus("VERIFIED");
        otp.setVerifiedAt(now);
        otp.setConsumedAt(now);
        otpRepo.save(otp);

        String email = otp.getDestination();

        // ✅ Alinhado ao teu UsuarioJpaRepository: um único parâmetro "id"
        // procura por email (case-insensitive) OU por cpf = id
        Optional<UsuarioEntity> existing = usuarioRepo.findByEmailOrCpf(email);

        UsuarioEntity user = existing.orElseGet(() -> {
            if (cpfIfNew == null || cpfIfNew.isBlank()) {
                throw new IllegalArgumentException("CPF é obrigatório para criação do usuário.");
            }
            if (nomeIfNew == null || nomeIfNew.isBlank()) {
                throw new IllegalArgumentException("Nome é obrigatório para criação do usuário.");
            }
            UsuarioEntity u = new UsuarioEntity();
            u.setEmail(email);
            u.setCpf(cpfIfNew);
            u.setNome(nomeIfNew);
            return u;
        });

        if (optionalPassword != null && !optionalPassword.isBlank()) {
            user.setSenha(encoder.encode(optionalPassword));
        } else if (user.getSenha() == null || user.getSenha().isBlank()) {
            user.setSenha(encoder.encode(UUID.randomUUID().toString()));
        }

        user.addRole(resolveDeveloperRole());
        usuarioRepo.save(user);
    }

    private Role resolveDeveloperRole() {
        return List.of("ROLE_DEVELOPER", "DEVELOPER", "ROLE_DEV", "DEV")
                .stream()
                .map(roleRepository::findByNome)
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Nenhuma role de desenvolvedor encontrada na tabela roles."));
    }

    private static String randomCode6() {
        return String.format("%06d", RNG.nextInt(1_000_000));
    }

    private static byte[] randomSalt() {
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);
        return salt;
    }

    private static String hashCode(String code, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return Base64.getEncoder().encodeToString(md.digest(code.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("hash failure", e);
        }
    }

    private String buildPayloadJson(String subject, String html, String link, String deliveryId) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                    "subject", subject,
                    "html", html,
                    "link", link,
                    "deliveryId", deliveryId
            ));
        } catch (Exception ex) {
            throw new IllegalStateException("payload_json_build_failed", ex);
        }
    }
}
