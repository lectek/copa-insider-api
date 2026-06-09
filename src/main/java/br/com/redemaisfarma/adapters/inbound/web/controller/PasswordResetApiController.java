package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.service.PasswordResetService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetApiController {

    /**
     * Service responsible for password reset operations.
     */
    private final PasswordResetService resetService;

    /**
     * Creates controller with password reset service dependency.
     *
     * @param service password reset service
     */
    public PasswordResetApiController(final PasswordResetService service) {
        this.resetService = service;
    }

    /**
     * Triggers reset flow for the informed identifier.
     *
     * @param body reset request payload
     * @return generic success status
     */
    @PostMapping("/esqueci-senha")
    public ResponseEntity<ApiStatus> esqueciSenha(
            @RequestBody final EsqueciSenhaRequest body
    ) {
        final String identifier = body == null
                ? ""
                : safeTrim(body.emailOuCpf());
        if (!identifier.isEmpty()) {
            resetService.solicitarResetPorEmailOuCpf(identifier);
        }
        return ResponseEntity.ok(new ApiStatus("ok"));
    }

    /**
     * Validates a password reset token.
     *
     * @param token reset token
     * @return token validation result
     */
    @GetMapping("/validar-token")
    public ResponseEntity<ValidarTokenResponse> validarToken(
            @RequestParam("token") final String token
    ) {
        final boolean valido = resetService.validarToken(token).isPresent();
        return ResponseEntity.ok(new ValidarTokenResponse(valido));
    }

    /**
     * Applies a new password using a valid token.
     *
     * @param body reset payload with token and password
     * @return status response
     */
    @PostMapping("/resetar-senha")
    public ResponseEntity<ApiStatus> resetarSenha(
            @RequestBody final ResetarSenhaRequest body
    ) {
        if (
                body == null
                        || isBlank(body.token())
                        || isBlank(body.novaSenha())
        ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiStatus("dados_invalidos"));
        }

        final boolean updated = resetService.aplicarNovaSenha(
                body.token().trim(),
                body.novaSenha()
        );
        if (!updated) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiStatus("token_invalido_ou_expirado"));
        }

        return ResponseEntity.ok(new ApiStatus("alterada"));
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String safeTrim(final String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Request payload for forgot-password flow.
     *
     * @param emailOuCpf identifier informed by user
     */
    public record EsqueciSenhaRequest(@NotBlank String emailOuCpf) {
    }

    /**
     * Generic API status payload.
     *
     * @param status status value
     */
    public record ApiStatus(String status) {
    }

    /**
     * Token validation payload.
     *
     * @param valido indicates token validity
     */
    public record ValidarTokenResponse(boolean valido) {
    }

    /**
     * Request payload for password reset.
     *
     * @param token reset token
     * @param novaSenha new password value
     */
    public record ResetarSenhaRequest(
            @NotBlank String token,
            @NotBlank String novaSenha
    ) {
    }
}
