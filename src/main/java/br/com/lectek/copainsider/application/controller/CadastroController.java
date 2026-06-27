package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.core.exception.CpfDuplicadoException;
import br.com.lectek.copainsider.application.core.exception.EmailDuplicadoException;
import br.com.lectek.copainsider.application.dto.request.CadastroClienteRequestDTO;
import br.com.lectek.copainsider.application.port.inbound.RegistrationAppService;
import br.com.lectek.copainsider.application.service.CopaAcessoService;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cadastro")
public class CadastroController {

    private static final Logger log = LoggerFactory.getLogger(CadastroController.class);
    private static final String REDIRECT_CADASTRO = "redirect:/cadastro";
    private static final String ERROR_MESSAGE = "errorMessage";

    @Value("${app.security.oauth2.enabled:true}")
    private boolean oauth2Enabled;

    private final RegistrationAppService registrationService;
    private final OtpServicePort         otpService;
    private final CopaAcessoService      acessoService;

    public CadastroController(RegistrationAppService registrationService,
                               OtpServicePort otpService,
                               CopaAcessoService acessoService) {
        this.registrationService = registrationService;
        this.otpService          = otpService;
        this.acessoService       = acessoService;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("oauth2Enabled", oauth2Enabled);
        return "pages/auth/cadastro";
    }

    @PostMapping
    public String submit(
            @ModelAttribute CadastroClienteRequestDTO dto,
            HttpSession session,
            RedirectAttributes ra) {

        if (dto.getSenha() == null || !dto.getSenha().equals(dto.getConfirmarSenha())) {
            ra.addFlashAttribute(ERROR_MESSAGE, "As senhas não coincidem.");
            return REDIRECT_CADASTRO;
        }

        if (dto.getCpf() == null || dto.getCpf().isBlank()) {
            long ts = System.currentTimeMillis();
            dto.setCpf(String.valueOf(ts).substring(2, 13));
        }

        try {
            registrationService.cadastrarNovoCliente(dto);
        } catch (EmailDuplicadoException e) {
            ra.addFlashAttribute(ERROR_MESSAGE, "Este e-mail já está em uso.");
            return REDIRECT_CADASTRO;
        } catch (CpfDuplicadoException e) {
            ra.addFlashAttribute(ERROR_MESSAGE, "CPF já cadastrado. Tente fazer login.");
            return REDIRECT_CADASTRO;
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute(ERROR_MESSAGE, e.getMessage());
            return REDIRECT_CADASTRO;
        }

        String email = dto.getEmail().trim().toLowerCase();

        // Tentar resgatar código de acesso, se fornecido
        String codigo = dto.getCodigoAcesso();
        if (codigo != null && !codigo.isBlank()) {
            try {
                var resultado = acessoService.resgatar(email, codigo.trim());
                log.info("[cadastro] resgate código '{}' para {} → {}", codigo, email, resultado);
            } catch (Exception ex) {
                log.warn("[cadastro] falha ao resgatar código '{}' para {}: {}", codigo, email, ex.getMessage());
            }
        }

        // OTP de verificação de email
        try {
            OtpServicePort.StartResult otp = otpService.start("email", email, null);
            session.setAttribute("pendingVerifEmail", email);
            session.setAttribute("pendingVerifDeliveryId", otp.deliveryId());
            session.setAttribute("pendingVerifMasked", otp.maskedDestino());
            if (otp.demoCode() != null) {
                ra.addFlashAttribute("demoCode", otp.demoCode());
            }
            return "redirect:/verificar-email";
        } catch (Exception ex) {
            log.warn("OTP indisponível para {}; ativando conta sem verificação", email);
            registrationService.ativarEmailVerificado(email);
            ra.addFlashAttribute("successMessage", "Conta criada! Faça login para entrar.");
            return "redirect:/auth/login";
        }
    }
}
