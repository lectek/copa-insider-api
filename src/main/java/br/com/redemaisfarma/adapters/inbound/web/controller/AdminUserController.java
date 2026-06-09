package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.dto.user.UsuarioFormDTO;
import br.com.redemaisfarma.application.service.user.UserService;
import java.util.Collections;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUserController {

    /**
     * Service responsible for admin user operations.
     */
    private final UserService userService;

    /**
     * Creates controller with user service dependency.
     *
     * @param service user service
     */
    public AdminUserController(final UserService service) {
        this.userService = service;
    }

    /**
     * Renders users list with optional filters.
     *
     * @param q free-text query
     * @param papel role filter
     * @param model view model
     * @return users list page
     */
    @GetMapping
    public String listar(
            @RequestParam(required = false) final String q,
            @RequestParam(required = false) final String papel,
            final Model model
    ) {
        model.addAttribute("usuarios", userService.listar(q, papel));
        return "pages/admin/usuarios/lista";
    }

    /**
     * Renders new user form.
     *
     * @param model view model
     * @return user form page
     */
    @GetMapping("/novo")
    public String novo(final Model model) {
        model.addAttribute(
                "usuario",
                new UsuarioFormDTO(
                        null,
                        "",
                        "",
                        "",
                        "",
                        true
                )
        );
        return "pages/admin/usuarios/form";
    }

    /**
     * Renders edit form for an existing user.
     *
     * @param id user id
     * @param model view model
     * @return user form page
     */
    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable("id") final Long id,
            final Model model
    ) {
        model.addAttribute("usuario", userService.buscarForm(id));
        return "pages/admin/usuarios/form";
    }

    /**
     * Persists a user form submission.
     *
     * @param usuario user form payload
     * @return redirect to users list
     */
    @PostMapping
    public String salvar(
            @ModelAttribute @Valid final UsuarioFormDTO usuario
    ) {
        userService.salvar(usuario);
        return "redirect:/admin/usuarios";
    }

    /**
     * Renders user profile page.
     *
     * @param id user id
     * @param model view model
     * @return user profile page
     */
    @GetMapping("/{id}")
    public String perfil(
            @PathVariable("id") final Long id,
            final Model model
    ) {
        model.addAttribute("u", userService.buscarPerfil(id));
        model.addAttribute("logs", Collections.emptyList());
        return "pages/admin/usuarios/perfil";
    }
}
