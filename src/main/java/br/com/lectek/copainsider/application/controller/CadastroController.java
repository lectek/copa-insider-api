package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.core.exception.CpfDuplicadoException;
import br.com.lectek.copainsider.application.core.exception.EmailDuplicadoException;
import br.com.lectek.copainsider.application.dto.request.CadastroClienteRequestDTO;
import br.com.lectek.copainsider.application.port.inbound.RegistrationAppService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cadastro")
public class CadastroController {

    private static final String REDIRECT_CADASTRO = "redirect:/cadastro";
    private static final String ERROR_MESSAGE = "errorMessage";

    private final RegistrationAppService registrationService;

    public CadastroController(RegistrationAppService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public String form() {
        return "pages/auth/cadastro";
    }

    @PostMapping
    public String submit(
            @ModelAttribute CadastroClienteRequestDTO dto,
            RedirectAttributes ra) {

        // Senhas não coincidem
        if (dto.getSenha() == null || !dto.getSenha().equals(dto.getConfirmarSenha())) {
            ra.addFlashAttribute(ERROR_MESSAGE, "As senhas não coincidem.");
            return REDIRECT_CADASTRO;
        }

        // CPF placeholder único — Copa Insider não exige CPF no registo
        if (dto.getCpf() == null || dto.getCpf().isBlank()) {
            long ts = System.currentTimeMillis();
            dto.setCpf(String.valueOf(ts).substring(2, 13)); // 11 dígitos
        }

        try {
            registrationService.cadastrarNovoCliente(dto);
            ra.addFlashAttribute("successMessage", "Conta criada! Faça login para entrar.");
            return "redirect:/auth/login";
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
    }
}
