/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Size
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.beans.propertyeditors.StringTrimmerEditor
 *  org.springframework.dao.DataIntegrityViolationException
 *  org.springframework.format.annotation.DateTimeFormat
 *  org.springframework.format.annotation.DateTimeFormat$ISO
 *  org.springframework.security.crypto.password.PasswordEncoder
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
 *  org.springframework.web.servlet.mvc.support.RedirectAttributes
 */
package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.service.ClienteService;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import br.com.redemaisfarma.domain.Cliente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.beans.PropertyEditor;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value={"/clientes"})
public class ClienteViewController {
    private final ClienteService clienteService;
    private final PasswordEncoder passwordEncoder;
    private final OtpServicePort otpService;
    @Value(value="${app.security.oauth2.enabled:true}")
    private boolean oauth2Enabled;
    @Value(value="${app.signup.relaxed:false}")
    private boolean relaxedSignup;

    public ClienteViewController(ClienteService clienteService, PasswordEncoder passwordEncoder, OtpServicePort otpService) {
        this.clienteService = clienteService;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, (PropertyEditor)new StringTrimmerEditor(true));
    }

    @GetMapping(value={"/cadastro"})
    public String showForm(Model model) {
        if (!model.containsAttribute("cliente")) {
            model.addAttribute("cliente", (Object)new ClienteForm());
        }
        model.addAttribute("oauth2Enabled", (Object)this.oauth2Enabled);
        model.addAttribute("relaxedSignup", (Object)this.relaxedSignup);
        return "pages/cliente/cadastro-cliente";
    }

    @PostMapping
    public String processForm(@Valid @ModelAttribute(value="cliente") ClienteForm dto, BindingResult binding, @RequestParam(value="otpToken", required=false) String otpToken, RedirectAttributes ra, Model model) {
        if (dto.getSenha() != null && dto.getConfirmarSenha() != null && !dto.getSenha().equals(dto.getConfirmarSenha())) {
            binding.rejectValue("confirmarSenha", "senha.nao.confere", "As senhas n\u00e3o conferem.");
        }
        if (binding.hasErrors() && this.relaxedSignup) {
            binding.getAllErrors().clear();
            if (dto.getNome() == null) {
                dto.setNome("Usu\u00e1rio");
            }
            if (dto.getEmail() == null) {
                dto.setEmail("dev@example.com");
            }
            if (dto.getSenha() == null) {
                dto.setSenha("Aa123456!");
            }
            if (dto.getConfirmarSenha() == null) {
                dto.setConfirmarSenha(dto.getSenha());
            }
            if (dto.getCpf() == null) {
                dto.setCpf("000.000.000-00");
            }
        }
        if (binding.hasErrors() && !this.relaxedSignup) {
            model.addAttribute("oauth2Enabled", (Object)this.oauth2Enabled);
            model.addAttribute("relaxedSignup", (Object)this.relaxedSignup);
            return "pages/cliente/cadastro-cliente";
        }
        if (!(this.relaxedSignup || otpToken != null && !otpToken.isBlank() && this.otpService.consumeToken(otpToken))) {
            binding.reject("otp.invalido", "C\u00f3digo de verifica\u00e7\u00e3o inv\u00e1lido ou expirado.");
            model.addAttribute("oauth2Enabled", (Object)this.oauth2Enabled);
            model.addAttribute("relaxedSignup", (Object)this.relaxedSignup);
            return "pages/cliente/cadastro-cliente";
        }
        String email = dto.getEmail() != null ? dto.getEmail().toLowerCase() : null;
        String cpfSomenteDigitos = dto.getCpf() != null ? dto.getCpf().replaceAll("\\D", "") : null;
        Cliente entity = new Cliente();
        entity.setNome(dto.getNome());
        entity.setEmail(email);
        entity.setTelefone(dto.getTelefone());
        entity.setCpf(cpfSomenteDigitos);
        entity.setDataDeNascimento(dto.getDataDeNascimento());
        entity.setAtivo(true);
        entity.setSenha(this.passwordEncoder.encode((CharSequence)dto.getSenha()));
        try {
            this.clienteService.create(entity);
        }
        catch (DataIntegrityViolationException dive) {
            binding.reject("duplicado", "E-mail ou CPF j\u00e1 cadastrado.");
            model.addAttribute("oauth2Enabled", (Object)this.oauth2Enabled);
            model.addAttribute("relaxedSignup", (Object)this.relaxedSignup);
            return "pages/cliente/cadastro-cliente";
        }
        ra.addFlashAttribute("infoMessage", (Object)"Conta criada com sucesso! Voc\u00ea j\u00e1 pode fazer login.");
        return "redirect:/login";
    }

    public static class ClienteForm {
        @NotBlank(message="Informe o nome.")
        @Size(max=100, message="Nome deve ter no m\u00e1ximo 100 caracteres.")
        private @NotBlank(message="Informe o nome.") @Size(max=100, message="Nome deve ter no m\u00e1ximo 100 caracteres.") String nome;
        @NotBlank(message="Informe o e-mail.")
        @Email(message="E-mail inv\u00e1lido.")
        @Size(max=150, message="E-mail deve ter no m\u00e1ximo 150 caracteres.")
        private @NotBlank(message="Informe o e-mail.") @Email(message="E-mail inv\u00e1lido.") @Size(max=150, message="E-mail deve ter no m\u00e1ximo 150 caracteres.") String email;
        @NotBlank(message="Informe a senha.")
        @Size(min=8, max=72, message="A senha deve ter entre 8 e 72 caracteres.")
        private @NotBlank(message="Informe a senha.") @Size(min=8, max=72, message="A senha deve ter entre 8 e 72 caracteres.") String senha;
        @NotBlank(message="Confirme a senha.")
        private @NotBlank(message="Confirme a senha.") String confirmarSenha;
        @Size(max=25, message="Telefone deve ter no m\u00e1ximo 25 caracteres.")
        private @Size(max=25, message="Telefone deve ter no m\u00e1ximo 25 caracteres.") String telefone;
        @NotBlank(message="Informe o CPF.")
        @Pattern(regexp="^(\\d{3}\\.?){3}-?\\d{2}$", message="CPF inv\u00e1lido. Ex: 123.456.789-09")
        private @NotBlank(message="Informe o CPF.") @Pattern(regexp="^(\\d{3}\\.?){3}-?\\d{2}$", message="CPF inv\u00e1lido. Ex: 123.456.789-09") String cpf;
        @DateTimeFormat(iso=DateTimeFormat.ISO.DATE)
        private LocalDate dataDeNascimento;

        public String getNome() {
            return this.nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getEmail() {
            return this.email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getSenha() {
            return this.senha;
        }

        public void setSenha(String senha) {
            this.senha = senha;
        }

        public String getConfirmarSenha() {
            return this.confirmarSenha;
        }

        public void setConfirmarSenha(String confirmarSenha) {
            this.confirmarSenha = confirmarSenha;
        }

        public String getTelefone() {
            return this.telefone;
        }

        public void setTelefone(String telefone) {
            this.telefone = telefone;
        }

        public String getCpf() {
            return this.cpf;
        }

        public void setCpf(String cpf) {
            this.cpf = cpf;
        }

        public LocalDate getDataDeNascimento() {
            return this.dataDeNascimento;
        }

        public void setDataDeNascimento(LocalDate dataDeNascimento) {
            this.dataDeNascimento = dataDeNascimento;
        }
    }
}

