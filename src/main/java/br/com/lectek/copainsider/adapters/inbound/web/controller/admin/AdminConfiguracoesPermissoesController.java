package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/permissoes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesPermissoesController {

    /**
     * Settings key for permission matrix payload.
     */
    private static final String KEY_PERMISSOES = "permissoes.matriz";

    /**
     * Settings service dependency.
     */
    private final AppSettingService settings;

    /**
     * JSON mapper used to serialize and deserialize the matrix.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates controller with settings dependencies.
     *
     * @param settingsService settings service
     * @param mapper json mapper
     */
    public AdminConfiguracoesPermissoesController(
            final AppSettingService settingsService,
            final ObjectMapper mapper
    ) {
        this.settings = settingsService;
        this.objectMapper = mapper;
    }

    /**
     * Renders permissions settings form.
     *
     * @param model view model
     * @return permissions settings page
     */
    @GetMapping
    public String form(final Model model) {
        final PermissoesForm form = new PermissoesForm();
        form.setRecursos(loadRecursos());
        model.addAttribute("form", form);
        return "pages/admin/configuracoes/permissoes";
    }

    /**
     * Persists permissions matrix.
     *
     * @param form form payload
     * @param ra redirect attributes
     * @return redirect to permissions settings page
     */
    @PostMapping
    public String salvar(
            @ModelAttribute("form") final PermissoesForm form,
            final RedirectAttributes ra
    ) {
        final List<RecursoPermissao> recursos = form.getRecursos() == null
                ? List.of()
                : form.getRecursos();
        try {
            final String json = objectMapper.writeValueAsString(recursos);
            settings.upsert(
                    KEY_PERMISSOES,
                    json,
                    "Matriz de permissoes por perfil"
            );
            ra.addFlashAttribute("success", "Permissoes atualizadas.");
        } catch (final Exception ex) {
            ra.addFlashAttribute("error", "Falha ao salvar permissoes.");
        }
        return "redirect:/admin/configuracoes/permissoes";
    }

    private List<RecursoPermissao> loadRecursos() {
        final String raw = settings.getOrDefault(KEY_PERMISSOES, "");
        if (raw != null && !raw.isBlank()) {
            try {
                final TypeReference<List<RecursoPermissao>> typeRef =
                        new TypeReference<List<RecursoPermissao>>() { };
                final List<RecursoPermissao> list = objectMapper.readValue(
                        raw,
                        typeRef
                );
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (final Exception ignored) {
                return defaultRecursos();
            }
        }
        return defaultRecursos();
    }

    private List<RecursoPermissao> defaultRecursos() {
        final List<RecursoPermissao> list = new ArrayList<>();
        list.add(recurso("pedidos", "Pedidos", true, true, true));
        list.add(recurso("produtos", "Produtos", true, true, false));
        list.add(recurso("clientes", "Clientes", true, true, true));
        list.add(recurso("financeiro", "Financeiro", true, false, false));
        list.add(recurso("relatorios", "Relatorios", true, false, false));
        return list;
    }

    private RecursoPermissao recurso(
            final String id,
            final String nome,
            final boolean admin,
            final boolean farmaceutico,
            final boolean caixa
    ) {
        return new RecursoPermissao(id, nome, admin, farmaceutico, caixa);
    }

    /**
     * Form payload for permission matrix page.
     */
    @Getter
    @Setter
    public static final class PermissoesForm {

        /**
         * Permission matrix items.
         */
        private List<RecursoPermissao> recursos;
    }

    /**
     * Permission matrix item.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = "id")
    public static final class RecursoPermissao {

        /**
         * Resource identifier.
         */
        private String id;

        /**
         * Display name.
         */
        private String nome;

        /**
         * Admin role flag.
         */
        private boolean admin;

        /**
         * Pharmacist role flag.
         */
        private boolean farmaceutico;

        /**
         * Cashier role flag.
         */
        private boolean caixa;
    }
}
