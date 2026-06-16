package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.port.inbound.RegistrationAppService;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/verificar-email")
public class VerificarEmailController {

    private static final Logger log = LoggerFactory.getLogger(VerificarEmailController.class);

    private static final String SESS_DELIVERY_ID  = "pendingVerifDeliveryId";
    private static final String SESS_EMAIL         = "pendingVerifEmail";
    private static final String SESS_MASKED        = "pendingVerifMasked";
    private static final String REDIRECT_CADASTRO  = "redirect:/cadastro";
    private static final String ERROR_MSG          = "errorMessage";

    private final OtpServicePort          otpService;
    private final RegistrationAppService  registrationService;

    public VerificarEmailController(OtpServicePort otpService, RegistrationAppService registrationService) {
        this.otpService          = otpService;
        this.registrationService = registrationService;
    }

    @GetMapping
    public String form(HttpSession session, Model model) {
        if (session.getAttribute(SESS_DELIVERY_ID) == null) {
            return REDIRECT_CADASTRO;
        }
        model.addAttribute("maskedEmail", session.getAttribute(SESS_MASKED));
        return "pages/auth/verificar-email";
    }

    @PostMapping
    public String verify(@RequestParam String code,
                         HttpSession session,
                         RedirectAttributes ra) {

        String deliveryId = (String) session.getAttribute(SESS_DELIVERY_ID);
        String email      = (String) session.getAttribute(SESS_EMAIL);

        if (deliveryId == null || email == null) {
            ra.addFlashAttribute(ERROR_MSG, "Sessão expirada. Faça o cadastro novamente.");
            return REDIRECT_CADASTRO;
        }

        try {
            otpService.verify(deliveryId, code);
            registrationService.ativarEmailVerificado(email);
            clearSession(session);
            ra.addFlashAttribute("successMessage", "E-mail verificado! Faça login para entrar.");
            return "redirect:/auth/login";

        } catch (OtpServicePort.OtpException e) {
            switch (e.reason()) {
                case "expired", "too_many_attempts" -> {
                    clearSession(session);
                    ra.addFlashAttribute(ERROR_MSG, "Código expirado ou bloqueado. Faça o cadastro novamente.");
                    return REDIRECT_CADASTRO;
                }
                default -> {
                    ra.addFlashAttribute(ERROR_MSG, "Código incorreto. Tente novamente.");
                    return "redirect:/verificar-email";
                }
            }
        }
    }

    @PostMapping("/reenviar")
    public String reenviar(HttpSession session, RedirectAttributes ra) {
        String email      = (String) session.getAttribute(SESS_EMAIL);
        String deliveryId = (String) session.getAttribute(SESS_DELIVERY_ID);

        if (email == null || deliveryId == null) {
            ra.addFlashAttribute(ERROR_MSG, "Sessão expirada. Faz o cadastro novamente.");
            return REDIRECT_CADASTRO;
        }

        try {
            OtpServicePort.StartResult otp = otpService.start("email", email, deliveryId);
            session.setAttribute(SESS_DELIVERY_ID, otp.deliveryId());
            session.setAttribute(SESS_MASKED, otp.maskedDestino());
            if (otp.demoCode() != null) {
                ra.addFlashAttribute("demoCode", otp.demoCode());
            }
            ra.addFlashAttribute("successMessage", "Novo código enviado!");
            log.info("[otp-reenvio] reenviado para {}", email);
        } catch (OtpServicePort.OtpException e) {
            if ("cooldown".equals(e.reason())) {
                ra.addFlashAttribute(ERROR_MSG, e.getMessage());
            } else {
                ra.addFlashAttribute(ERROR_MSG, "Não foi possível reenviar. Tenta novamente.");
            }
        }
        return "redirect:/verificar-email";
    }

    private void clearSession(HttpSession session) {
        session.removeAttribute(SESS_DELIVERY_ID);
        session.removeAttribute(SESS_EMAIL);
        session.removeAttribute(SESS_MASKED);
    }
}
