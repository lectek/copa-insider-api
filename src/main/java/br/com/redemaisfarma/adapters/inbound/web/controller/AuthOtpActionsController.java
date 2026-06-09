package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.service.ClienteService;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import br.com.redemaisfarma.domain.Cliente;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/auth", produces = "application/json")
@Validated
public class AuthOtpActionsController {

    /**
     * Minimum password length.
     */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Maximum password length.
     */
    private static final int MAX_PASSWORD_LENGTH = 128;

    /**
     * OTP service dependency.
     */
    private final OtpServicePort otp;

    /**
     * User repository dependency.
     */
    private final UsuarioRepository usuarios;

    /**
     * Password encoder dependency.
     */
    private final PasswordEncoder encoder;

    /**
     * Customer service dependency.
     */
    private final ClienteService clienteService;

    /**
     * Creates controller with OTP-related dependencies.
     *
     * @param otpService otp service
     * @param usuarioRepository user repository
     * @param passwordEncoder password encoder
     * @param service customer service
     */
    public AuthOtpActionsController(
            final OtpServicePort otpService,
            final UsuarioRepository usuarioRepository,
            final PasswordEncoder passwordEncoder,
            final ClienteService service
    ) {
        this.otp = otpService;
        this.usuarios = usuarioRepository;
        this.encoder = passwordEncoder;
        this.clienteService = service;
    }

    /**
     * Resets password after OTP validation.
     *
     * @param req request payload
     * @return reset response payload
     */
    @PostMapping(path = "/password/reset-otp", consumes = "application/json")
    public ResponseEntity<?> resetByOtp(
            @RequestBody @Validated final ResetReq req
    ) {
        if (!otp.consumeTokenForDestino(req.token(), req.email())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message",
                    "Token invalido, expirado ou nao pertence a este e-mail."
            ));
        }

        final UsuarioEntity user = usuarios.findByEmailIgnoreCase(req.email())
                .orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message",
                    "Usuario nao encontrado para este e-mail."
            ));
        }

        user.setSenha(encoder.encode(req.novaSenha()));
        usuarios.save(user);
        return ResponseEntity.ok(Map.of("message", "Senha redefinida."));
    }

    /**
     * Completes registration after OTP validation.
     *
     * @param req request payload
     * @return registration response payload
     */
    @PostMapping(path = "/register/complete-otp", consumes = "application/json")
    public ResponseEntity<?> registerByOtp(
            @RequestBody @Validated final RegisterReq req
    ) {
        if (!otp.consumeTokenForDestino(req.token(), req.email())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message",
                    "Token invalido, expirado ou nao pertence a este e-mail."
            ));
        }

        if (usuarios.existsByEmailIgnoreCase(req.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "message",
                    "E-mail ja cadastrado."
            ));
        }

        final Cliente cliente = new Cliente();
        cliente.setNome(req.nome());
        cliente.setEmail(req.email().toLowerCase());
        cliente.setSenha(encoder.encode(req.senha()));
        cliente.setAtivo(true);
        cliente.setCpf(null);
        cliente.setTelefone(null);
        cliente.setDataDeNascimento(null);

        clienteService.create(cliente);
        return ResponseEntity.ok(Map.of(
                "message",
                "Conta criada com sucesso.",
                "email",
                req.email()
        ));
    }

    /**
     * Request payload for password reset by OTP.
     *
     * @param token otp token
     * @param email email destination
     * @param novaSenha new password
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResetReq(
            @NotBlank String token,
            @NotBlank @Email String email,
            @JsonAlias({"nova_senha", "novaSenha"})
            @NotBlank
            @Size(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH)
            String novaSenha
    ) {
    }

    /**
     * Request payload for registration completion by OTP.
     *
     * @param token otp token
     * @param email account email
     * @param nome account name
     * @param senha account password
     */
    public record RegisterReq(
            @NotBlank String token,
            @NotBlank @Email String email,
            @NotBlank String nome,
            @NotBlank
            @Size(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH)
            String senha
    ) {
    }
}
