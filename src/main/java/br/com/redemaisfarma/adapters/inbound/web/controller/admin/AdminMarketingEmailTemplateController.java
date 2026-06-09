package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailTemplate;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailTemplateRepository;
import br.com.redemaisfarma.application.service.MailService;
import java.util.Comparator;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin - Templates de Email", description = "Templates e testes de e-mail para campanhas e notificações")
@Controller
@RequestMapping("/admin/marketing/emails")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMarketingEmailTemplateController {

    private final EmailTemplateRepository templateRepository;
    private final MailService mailService;

    public AdminMarketingEmailTemplateController(EmailTemplateRepository templateRepository, MailService mailService) {
        this.templateRepository = templateRepository;
        this.mailService = mailService;
    }

    @Operation(summary = "Lista os templates disponíveis de e-mail")
    @GetMapping("/templates")
    public String templates(Model model) {
        var templates = templateRepository.findAll();
        templates.sort(Comparator.comparing(EmailTemplate::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        model.addAttribute("templates", templates);
        return "pages/admin/marketing/emails/teamplates";
    }

    @Operation(summary = "Mostra o editor para criação/edição de template")
    @GetMapping("/editor")
    public String editor(@RequestParam(name = "id", required = false) Long id, Model model) {
        EmailTemplate template = null;
        if (id != null) {
            template = templateRepository.findById(id).orElse(null);
        }
        if (template == null) {
            template = new EmailTemplate();
        }
        model.addAttribute("template", template);
        return "pages/admin/marketing/emails/editor";
    }

    @Operation(summary = "Salva o template e envia teste opcional")
    @PostMapping("/editor")
    public String salvar(@ModelAttribute("template") EmailTemplate form,
                         @RequestParam(name = "acao", required = false) String acao,
                         @RequestParam(name = "testeDestino", required = false) String testeDestino,
                         RedirectAttributes ra) {
        if ("teste".equalsIgnoreCase(acao)) {
            if (testeDestino == null || testeDestino.isBlank()) {
                ra.addFlashAttribute("error", "Informe um email para teste.");
                return "redirect:/admin/marketing/emails/editor";
            }
            try {
                mailService.sendHtml(testeDestino.trim(), safe(form.getAssunto(), "Teste de email"), safe(form.getHtml(), ""), null);
                ra.addFlashAttribute("success", "Email de teste enviado.");
            } catch (Exception ex) {
                ra.addFlashAttribute("error", "Falha ao enviar teste.");
            }
            return "redirect:/admin/marketing/emails/editor";
        }

        EmailTemplate entity = form.getId() == null
                ? new EmailTemplate()
                : templateRepository.findById(form.getId()).orElse(new EmailTemplate());
        entity.setNome(safe(form.getNome(), "Template"));
        entity.setAssunto(safe(form.getAssunto(), ""));
        entity.setHtml(safe(form.getHtml(), ""));
        templateRepository.save(entity);
        ra.addFlashAttribute("success", "Template salvo.");
        return "redirect:/admin/marketing/emails/templates";
    }

    private String safe(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? fallback : trimmed;
    }
}
