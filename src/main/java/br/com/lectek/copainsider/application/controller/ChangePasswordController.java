/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.http.HttpServletRequest
 *  jakarta.servlet.http.HttpServletResponse
 *  jakarta.validation.Valid
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.annotation.AuthenticationPrincipal
 *  org.springframework.security.core.context.SecurityContextHolder
 *  org.springframework.security.core.userdetails.UserDetails
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
 *  org.springframework.stereotype.Controller
 *  org.springframework.ui.Model
 *  org.springframework.validation.BindingResult
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.ModelAttribute
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.servlet.mvc.support.RedirectAttributes
 */
package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.dto.request.ChangePasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value={"/auth"})
public class ChangePasswordController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping(value={"/mudar-senha"})
    public String form(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", (Object)new ChangePasswordRequest());
        }
        return "pages/cliente/mudar-senha";
    }

    @PostMapping(value={"/mudar-senha"})
    public String submit(@AuthenticationPrincipal UserDetails me, @Valid @ModelAttribute(value="form") ChangePasswordRequest form, BindingResult br, RedirectAttributes ra, HttpServletRequest req, HttpServletResponse res) {
        if (me == null) {
            ra.addFlashAttribute("loginError", (Object)"Sua sess\u00e3o expirou. Fa\u00e7a login novamente.");
            return "redirect:/login";
        }
        if (br.hasErrors()) {
            return "pages/cliente/mudar-senha";
        }
        String nova = ChangePasswordController.safe(form.getNovaSenha());
        String conf = ChangePasswordController.safe(form.getConfirmarNovaSenha());
        String atual = ChangePasswordController.safe(form.getSenhaAtual());
        if (!nova.equals(conf)) {
            br.rejectValue("confirmarNovaSenha", "mismatch", "As senhas n\u00e3o conferem.");
            return "pages/cliente/mudar-senha";
        }
        String login = ChangePasswordController.safe(me.getUsername());
        UsuarioEntity u = this.localizarPorEmailOuCpf(login).orElseThrow(() -> new IllegalStateException("Usu\u00e1rio n\u00e3o encontrado para: " + login));
        if (!this.passwordEncoder.matches((CharSequence)atual, u.getSenha())) {
            br.rejectValue("senhaAtual", "invalid", "Senha atual incorreta.");
            return "pages/cliente/mudar-senha";
        }
        if (this.passwordEncoder.matches((CharSequence)nova, u.getSenha())) {
            br.rejectValue("novaSenha", "reused", "A nova senha n\u00e3o pode ser igual \u00e0 senha atual.");
            return "pages/cliente/mudar-senha";
        }
        u.setSenha(this.passwordEncoder.encode((CharSequence)nova));
        this.usuarioRepository.save(u);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(req, res, auth);
        ra.addFlashAttribute("infoMessage", (Object)"Senha alterada com sucesso. Fa\u00e7a login novamente.");
        return "redirect:/login";
    }

    private Optional<UsuarioEntity> localizarPorEmailOuCpf(String login) {
        if (login == null || login.isBlank()) {
            return Optional.empty();
        }
        String ident = login.trim();
        if (ident.contains("@")) {
            return this.usuarioRepository.findByEmailIgnoreCase(ident.toLowerCase());
        }
        String cpfDigits = ident.replaceAll("\\D", "");
        if (cpfDigits.length() == 11) {
            return this.usuarioRepository.findByCpf(cpfDigits);
        }
        return this.usuarioRepository.findByEmailOrCpf(ident);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}

