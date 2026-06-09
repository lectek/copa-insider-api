package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.adapters.inbound.web.dto.ResetSenhaForm;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.application.service.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    /**
     * Service responsible for reset password flow.
     */
    private final PasswordResetService resetService;

    /**
     * Creates controller with password reset dependency.
     *
     * @param service password reset service
     */
    public AuthController(final PasswordResetService service) {
        this.resetService = service;
    }

    /**
     * Renders forgot-password form.
     *
     * @return forgot-password page
     */
    @GetMapping("/esqueci-senha")
    public String esqueciSenhaForm() {
        return "auth/esqueci-senha";
    }

    /**
     * Receives forgot-password submission and triggers reset flow.
     *
     * @param identificador identifier informed by user
     * @param ra redirect attributes
     * @return redirect to login page
     */
    @PostMapping("/esqueci-senha")
    public String esqueciSenhaSubmit(
            @RequestParam("identificador") final String identificador,
            final RedirectAttributes ra
    ) {
        final String identifier = safeTrim(identificador);
        if (!identifier.isEmpty()) {
            resetService.solicitarResetPorEmailOuCpf(identifier);
        }
        ra.addFlashAttribute(
                "infoMessage",
                "Se existir uma conta, enviaremos um e-mail com instrucoes."
        );
        return "redirect:/login";
    }

    /**
     * Renders reset-password form from token.
     *
     * @param token reset token
     * @param model view model
     * @return reset-password page
     */
    @GetMapping("/resetar-senha")
    public String resetarSenhaForm(
            @RequestParam("token") final String token,
            final Model model
    ) {
        final Optional<UsuarioEntity> userOpt =
                resetService.validarToken(token);
        if (userOpt.isEmpty()) {
            model.addAttribute("form", null);
            model.addAttribute("errorMessage", "Link invalido ou expirado.");
            return "auth/resetar-senha";
        }

        final ResetSenhaForm form = new ResetSenhaForm();
        form.setToken(token);
        model.addAttribute("form", form);
        return "auth/resetar-senha";
    }

    /**
     * Applies new password from MVC reset form.
     *
     * @param form reset form
     * @param br binding result
     * @param ra redirect attributes
     * @param model view model
     * @return reset page or login redirect
     */
    @PostMapping("/resetar-senha")
    public String resetarSenhaSubmit(
            @Valid @ModelAttribute("form") final ResetSenhaForm form,
            final BindingResult br,
            final RedirectAttributes ra,
            final Model model
    ) {
        if (br.hasErrors()) {
            return "auth/resetar-senha";
        }

        if (!form.getNovaSenha().equals(form.getConfirmarSenha())) {
            model.addAttribute("errorMessage", "As senhas nao coincidem.");
            return "auth/resetar-senha";
        }

        final boolean updated = resetService.aplicarNovaSenha(
                form.getToken(),
                form.getNovaSenha()
        );
        if (!updated) {
            model.addAttribute("form", null);
            model.addAttribute("errorMessage", "Link invalido ou expirado.");
            return "auth/resetar-senha";
        }

        ra.addFlashAttribute(
                "infoMessage",
                "Senha alterada com sucesso. Faca login novamente."
        );
        return "redirect:/login";
    }

    /**
     * Forgot-password API endpoint.
     *
     * @param body request payload
     * @return generic success status
     */
    @PostMapping(path = "/api/esqueci-senha")
    @ResponseBody
    public ResponseEntity<ApiStatus> apiEsqueciSenha(
            @Valid @RequestBody final EsqueciSenhaRequest body
    ) {
        final String identifier = safeTrim(body.emailOuCpf());
        if (!identifier.isEmpty()) {
            resetService.solicitarResetPorEmailOuCpf(identifier);
        }
        return ResponseEntity.ok(new ApiStatus("ok"));
    }

    /**
     * Token validation API endpoint.
     *
     * @param token reset token
     * @return token validation payload
     */
    @GetMapping(path = "/api/validar-token")
    @ResponseBody
    public ResponseEntity<ValidarTokenResponse> apiValidarToken(
            @RequestParam("token") final String token
    ) {
        final boolean valido = resetService.validarToken(token).isPresent();
        return ResponseEntity.ok(new ValidarTokenResponse(valido));
    }

    /**
     * Reset password API endpoint.
     *
     * @param body request payload
     * @return API status response
     */
    @PostMapping(path = "/api/resetar-senha")
    @ResponseBody
    public ResponseEntity<ApiStatus> apiResetarSenha(
            @Valid @RequestBody final ResetarSenhaRequest body
    ) {
        final boolean updated = resetService.aplicarNovaSenha(
                body.token(),
                body.novaSenha()
        );
        if (!updated) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiStatus("token_invalido_ou_expirado"));
        }
        return ResponseEntity.ok(new ApiStatus("alterada"));
    }

    private static String safeTrim(final String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Request payload for forgot-password endpoint.
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
     * Request payload for reset endpoint.
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
