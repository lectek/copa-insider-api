package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.port.inbound.RegistrationAppService;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import jakarta.servlet.http.HttpSession;
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

    private final OtpServicePort otpService;
    private final RegistrationAppService registrationService;

    public VerificarEmailController(OtpServicePort otpService, RegistrationAppService registrationService) {
        this.otpService = otpService;
        this.registrationService = registrationService;
    }

    @GetMapping
    public String form(HttpSession session, Model model) {
        if (session.getAttribute("pendingVerifDeliveryId") == null) {
            return "redirect:/cadastro";
        }
        model.addAttribute("maskedEmail", session.getAttribute("pendingVerifMasked"));
        return "pages/auth/verificar-email";
    }

    @PostMapping
    public String verify(@RequestParam String code,
                         HttpSession session,
                         RedirectAttributes ra) {

        String deliveryId = (String) session.getAttribute("pendingVerifDeliveryId");
        String email      = (String) session.getAttribute("pendingVerifEmail");

        if (deliveryId == null || email == null) {
            ra.addFlashAttribute("errorMessage", "Sessão expirada. Faça o cadastro novamente.");
            return "redirect:/cadastro";
        }

        try {
            otpService.verify(deliveryId, code);
            registrationService.ativarEmailVerificado(email);
            session.removeAttribute("pendingVerifDeliveryId");
            session.removeAttribute("pendingVerifEmail");
            session.removeAttribute("pendingVerifMasked");
            ra.addFlashAttribute("successMessage", "E-mail verificado! Faça login para entrar.");
            return "redirect:/auth/login";

        } catch (OtpServicePort.OtpException e) {
            switch (e.reason()) {
                case "expired", "too_many_attempts" -> {
                    session.removeAttribute("pendingVerifDeliveryId");
                    session.removeAttribute("pendingVerifEmail");
                    session.removeAttribute("pendingVerifMasked");
                    ra.addFlashAttribute("errorMessage",
                            "Código expirado ou bloqueado. Faça o cadastro novamente.");
                    return "redirect:/cadastro";
                }
                default -> {
                    ra.addFlashAttribute("errorMessage", "Código incorreto. Tente novamente.");
                    return "redirect:/verificar-email";
                }
            }
        }
    }
}
