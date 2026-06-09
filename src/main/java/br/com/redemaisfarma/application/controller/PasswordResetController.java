/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.beans.propertyeditors.StringTrimmerEditor
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.validation.BindingResult
 *  org.springframework.web.bind.WebDataBinder
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.InitBinder
 *  org.springframework.web.bind.annotation.ModelAttribute
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 */
package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.dto.request.ForgotPasswordRequest;
import br.com.redemaisfarma.application.dto.request.ResetPasswordRequest;
import br.com.redemaisfarma.application.service.PasswordResetService;
import jakarta.validation.Valid;
import java.beans.PropertyEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value={"/cliente/auth"})
public class PasswordResetController {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);
    private final PasswordResetService service;

    public PasswordResetController(PasswordResetService service) {
        this.service = service;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, (PropertyEditor)new StringTrimmerEditor(true));
    }

    @GetMapping(value={"/esqueci-senha"})
    public String forgotForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", (Object)new ForgotPasswordRequest());
        }
        return "pages/cliente/esqueci-senha";
    }

    @PostMapping(value={"/esqueci-senha"})
    public String forgotSubmit(@Valid @ModelAttribute(value="form") ForgotPasswordRequest form, BindingResult br, Model model) {
        if (br.hasErrors()) {
            return "pages/cliente/esqueci-senha";
        }
        try {
            this.service.solicitarResetPorEmailOuCpf(form.getEmailOuCpf());
            log.info("Solicita\u00e7\u00e3o de reset recebida para identificador='{}' (se existir).", (Object)form.getEmailOuCpf());
        }
        catch (Exception ex) {
            log.warn("Falha ao solicitar reset para '{}': {}", (Object)form.getEmailOuCpf(), (Object)ex.getMessage());
        }
        model.addAttribute("infoMessage", (Object)"Se encontrarmos sua conta, enviaremos um link de redefini\u00e7\u00e3o para o e-mail cadastrado.");
        model.addAttribute("form", (Object)new ForgotPasswordRequest());
        return "pages/cliente/esqueci-senha";
    }

    @GetMapping(value={"/resetar-senha"})
    public String resetForm(@RequestParam(value="token") String token, Model model) {
        boolean valido = this.service.validarToken(token).isPresent();
        if (!valido) {
            model.addAttribute("errorMessage", (Object)"Link inv\u00e1lido ou expirado. Solicite novamente.");
            return "pages/cliente/resetar-senha";
        }
        ResetPasswordRequest form = new ResetPasswordRequest();
        form.setToken(token);
        model.addAttribute("form", (Object)form);
        return "pages/cliente/resetar-senha";
    }

    @PostMapping(value={"/resetar-senha"})
    public String resetSubmit(@Valid @ModelAttribute(value="form") ResetPasswordRequest form, BindingResult br, Model model) {
        boolean ok;
        if (form.getNovaSenha() != null && form.getConfirmarSenha() != null && !form.getNovaSenha().equals(form.getConfirmarSenha())) {
            br.rejectValue("confirmarSenha", "mismatch", "As senhas n\u00e3o conferem.");
        }
        if (br.hasErrors()) {
            return "pages/cliente/resetar-senha";
        }
        try {
            ok = this.service.aplicarNovaSenha(form.getToken(), form.getNovaSenha());
        }
        catch (Exception ex) {
            log.error("Erro ao aplicar nova senha via token: {}", (Object)ex.getMessage(), (Object)ex);
            ok = false;
        }
        if (!ok) {
            model.addAttribute("errorMessage", (Object)"Link inv\u00e1lido ou expirado. Solicite novamente.");
            return "pages/cliente/resetar-senha";
        }
        model.addAttribute("successMessage", (Object)"Senha redefinida com sucesso! Voc\u00ea j\u00e1 pode entrar.");
        return "pages/cliente/resetar-senha";
    }
}

