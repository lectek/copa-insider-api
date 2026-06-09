package br.com.redemaisfarma.adapters.inbound.web.controller.auth;

import br.com.redemaisfarma.application.core.exception.CpfDuplicadoException;
import br.com.redemaisfarma.application.core.exception.EmailDuplicadoException;
import br.com.redemaisfarma.application.dto.request.CadastroClienteRequestDTO;
import br.com.redemaisfarma.application.port.inbound.RegistrationAppService;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthRegistrationController {

    /**
     * Expected amount of digits for a normalized CPF.
     */
    private static final int CPF_LENGTH = 11;

    /**
     * Minimum password size accepted by registration.
     */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Maximum password size accepted by registration.
     */
    private static final int MAX_PASSWORD_LENGTH = 128;

    /**
     * Default OTP channel when none is explicitly selected.
     */
    private static final String DEFAULT_OTP_CHANNEL = "email";

    /**
     * Application service for customer registration.
     */
    private final RegistrationAppService registrationAppService;

    /**
     * OTP service used to validate verification tokens.
     */
    private final OtpServicePort otpService;

    /**
     * Creates controller with registration and OTP dependencies.
     *
     * @param registrationService registration application service
     * @param otpPort otp service
     */
    public AuthRegistrationController(
            final RegistrationAppService registrationService,
            final OtpServicePort otpPort
    ) {
        this.registrationAppService = registrationService;
        this.otpService = otpPort;
    }

    /**
     * Trims all incoming string fields from registration form.
     *
     * @param binder web data binder
     */
    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(
                String.class,
                new StringTrimmerEditor(true)
        );
    }

    /**
     * Renders customer signup form.
     *
     * @param model view model
     * @return signup page
     */
    @GetMapping("/cliente/cadastro")
    public String cadastroForm(final Model model) {
        if (!model.containsAttribute("cadastro")) {
            model.addAttribute("cadastro", new CadastroClienteForm());
        }
        return "pages/auth/cadastro-cliente";
    }

    /**
     * Processes customer signup form.
     *
     * @param form signup form
     * @param br binding result
     * @return signup page or login redirect
     */
    @PostMapping("/cliente/cadastro")
    public String processar(
            @Valid @ModelAttribute("cadastro") final CadastroClienteForm form,
            final BindingResult br
    ) {
        if (hasText(form.getWebsite())) {
            br.reject("bot", "Operacao nao permitida.");
            return "pages/auth/cadastro-cliente";
        }

        form.setCpf(normalizeCpf(form.getCpf()));
        form.setTelefone(normalizePhone(form.getTelefone()));

        if (
                hasText(form.getSenha())
                        && !form.getSenha().equals(form.getConfirmarSenha())
        ) {
            br.rejectValue(
                    "confirmarSenha",
                    "mismatch",
                    "As senhas nao conferem."
            );
        }

        if (br.hasErrors()) {
            return "pages/auth/cadastro-cliente";
        }

        if (!hasText(form.getOtpToken())) {
            br.reject(
                    "otp.obrigatorio",
                    "Confirme o codigo de verificacao antes de criar a conta."
            );
            return "pages/auth/cadastro-cliente";
        }

        final String otpDestino = resolveOtpDestino(form);
        if (
                !hasText(otpDestino)
                        || !otpService.consumeTokenForDestino(
                                form.getOtpToken(),
                                otpDestino
                        )
        ) {
            br.reject(
                    "otp.invalido",
                    "Codigo de verificacao invalido ou expirado."
            );
            return "pages/auth/cadastro-cliente";
        }

        try {
            registrationAppService.cadastrarNovoCliente(toRequest(form));
            return "redirect:/auth/login?from=cadastro_ok";
        } catch (final EmailDuplicadoException ex) {
            br.rejectValue("email", "duplicado", "E-mail ja cadastrado.");
            return "pages/auth/cadastro-cliente";
        } catch (final CpfDuplicadoException ex) {
            br.rejectValue("cpf", "duplicado", "CPF ja cadastrado.");
            return "pages/auth/cadastro-cliente";
        } catch (final Exception ex) {
            br.reject("erro", "Nao foi possivel concluir o cadastro agora.");
            return "pages/auth/cadastro-cliente";
        }
    }

    private static boolean hasText(final String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String normalizeCpf(final String cpf) {
        if (!hasText(cpf)) {
            return cpf;
        }

        final String onlyDigits = cpf.replaceAll("\\D", "");
        if (onlyDigits.length() == CPF_LENGTH) {
            return onlyDigits;
        }
        return cpf;
    }

    private static String normalizePhone(final String phone) {
        if (!hasText(phone)) {
            return phone;
        }

        final String cleaned = phone.replaceAll("[^+()\\d\\s-]", "");
        return cleaned.trim();
    }

    private static String resolveOtpDestino(final CadastroClienteForm form) {
        if (form == null) {
            return null;
        }

        final String canal = hasText(form.getCanalOtp())
                ? form.getCanalOtp().trim().toLowerCase()
                : DEFAULT_OTP_CHANNEL;
        if ("sms".equals(canal)) {
            return normalizePhone(form.getTelefone());
        }
        return form.getEmail();
    }

    private static CadastroClienteRequestDTO toRequest(
            final CadastroClienteForm form
    ) {
        final CadastroClienteRequestDTO dto = new CadastroClienteRequestDTO();
        dto.setNome(form.getNome());
        dto.setEmail(form.getEmail());
        dto.setCpf(form.getCpf());
        dto.setTelefone(form.getTelefone());
        dto.setDataDeNascimento(form.getDataDeNascimento());
        dto.setSenha(form.getSenha());
        dto.setConfirmarSenha(form.getConfirmarSenha());
        dto.setCanalOtp(form.getCanalOtp());
        dto.setOtpDeliveryId(form.getOtpDeliveryId());
        dto.setOtpToken(form.getOtpToken());
        dto.setWebsite(form.getWebsite());
        return dto;
    }

    /**
     * Form object used by customer signup page.
     */
    @Getter
    @Setter
    public static final class CadastroClienteForm {

        /**
         * Customer full name.
         */
        @NotBlank(message = "Informe seu nome completo.")
        private String nome;

        /**
         * Customer email.
         */
        @Email(message = "E-mail invalido.")
        @NotBlank(message = "Informe um e-mail valido.")
        private String email;

        /**
         * Customer CPF.
         */
        @NotBlank(message = "Informe o CPF.")
        @Pattern(
                regexp = "^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$",
                message = "CPF invalido. Use 11 digitos com ou sem mascara."
        )
        private String cpf;

        /**
         * Customer phone number.
         */
        @Pattern(
                regexp = "^$|^[+()\\d\\s-]{8,20}$",
                message = "Telefone invalido."
        )
        private String telefone;

        /**
         * Customer birth date.
         */
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate dataDeNascimento;

        /**
         * Customer password.
         */
        @Size(
                min = MIN_PASSWORD_LENGTH,
                max = MAX_PASSWORD_LENGTH,
                message = "A senha deve ter entre 8 e 128 caracteres."
        )
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])"
                        + "(?=.*[^A-Za-z0-9]).{8,128}$",
                message = "A senha precisa de maiuscula, minuscula, numero"
                        + " e caractere especial."
        )
        private String senha;

        /**
         * Password confirmation.
         */
        @NotBlank(message = "Confirme sua senha.")
        private String confirmarSenha;

        /**
         * OTP channel selected by user.
         */
        private String canalOtp;

        /**
         * OTP delivery tracking id.
         */
        private String otpDeliveryId;

        /**
         * OTP token validated in frontend.
         */
        private String otpToken;

        /**
         * Hidden honeypot field. Must stay empty.
         */
        private String website;
    }
}
