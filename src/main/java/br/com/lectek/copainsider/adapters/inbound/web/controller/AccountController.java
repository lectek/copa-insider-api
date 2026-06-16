package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CustomerEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaAcessoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CustomerRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.core.account.UserAccountService;
import br.com.lectek.copainsider.application.core.media.ImageStorageService;
import br.com.lectek.copainsider.application.dto.request.ChangePasswordRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente")
public class AccountController {

    /**
     * Max avatar size in bytes (2 MB).
     */
    private static final long AVATAR_MAX_BYTES = 2L * 1024L * 1024L;

    /**
     * Model attribute key for active menu state.
     */
    private static final String ACTIVE_ATTR = "active";

    /**
     * Model attribute key for form payload.
     */
    private static final String FORM_ATTR = "form";

    /**
     * Flash attribute key for info message.
     */
    private static final String INFO_MESSAGE_ATTR = "infoMessage";

    /**
     * Flash attribute key for error message.
     */
    private static final String ERROR_MESSAGE_ATTR = "errorMessage";

    /**
     * Redirect path for account data page.
     */
    private static final String REDIRECT_DADOS = "redirect:/cliente/dados";

    /**
     * Redirect path for password page.
     */
    private static final String REDIRECT_SENHA = "redirect:/cliente/senha";

    /**
     * View path for password page.
     */
    private static final String VIEW_SENHA = "pages/cliente/alterar-senha";

    /**
     * View path for account page.
     */
    private static final String VIEW_CONTA = "pages/cliente/conta";

    /**
     * View path for account data page.
     */
    private static final String VIEW_DADOS = "pages/cliente/dados";

    /**
     * Supported avatar MIME type.
     */
    private static final String IMAGE_JPEG = "image/jpeg";

    /**
     * Supported avatar MIME type.
     */
    private static final String IMAGE_PNG = "image/png";

    /**
     * Supported avatar MIME type.
     */
    private static final String IMAGE_WEBP = "image/webp";

    /**
     * Max allowed length for full name.
     */
    private static final int NOME_MAX_LEN = 120;

    /**
     * Max allowed length for email.
     */
    private static final int EMAIL_MAX_LEN = 150;

    /**
     * Max allowed length for phone.
     */
    private static final int TELEFONE_MAX_LEN = 25;

    /**
     * Max allowed length for address.
     */
    private static final int ENDERECO_MAX_LEN = 200;

    /**
     * User account domain service.
     */
    private final UserAccountService accountService;

    /**
     * User repository.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Customer repository.
     */
    private final CustomerRepository customerRepository;

    /**
     * Service used to store avatars.
     */
    private final ImageStorageService imageStorageService;

    /**
     * Copa access repository for checking purchased content.
     */
    private final CopaAcessoJPARepository acessoRepository;

    /**
     * Creates the controller with required dependencies.
     *
     * @param accountServiceValue account service
     * @param usuarioRepositoryValue user repository
     * @param customerRepositoryValue customer repository
     * @param imageStorageServiceValue image storage service
     * @param acessoRepositoryValue copa access repository
     */
    public AccountController(
            final UserAccountService accountServiceValue,
            final UsuarioRepository usuarioRepositoryValue,
            final CustomerRepository customerRepositoryValue,
            final ImageStorageService imageStorageServiceValue,
            final CopaAcessoJPARepository acessoRepositoryValue
    ) {
        this.accountService = accountServiceValue;
        this.usuarioRepository = usuarioRepositoryValue;
        this.customerRepository = customerRepositoryValue;
        this.imageStorageService = imageStorageServiceValue;
        this.acessoRepository = acessoRepositoryValue;
    }

    /**
     * Shows the page to update password.
     *
     * @param model view model
     * @return password view
     */
    @GetMapping("/senha")
    public String formAlterarSenha(
            final Model model,
            final Authentication auth
    ) {
        model.addAttribute(ACTIVE_ATTR, "conta");
        if (!model.containsAttribute(FORM_ATTR)) {
            model.addAttribute(FORM_ATTR, new ChangePasswordRequest());
        }
        final String recoveryEmail = resolveRecoveryEmail(auth);
        model.addAttribute("passwordRecoveryEmail", recoveryEmail);
        model.addAttribute("passwordRecoveryMaskedEmail", maskEmail(recoveryEmail));
        model.addAttribute(
                "passwordRecoveryAvailable",
                recoveryEmail != null && !recoveryEmail.isBlank()
        );
        return VIEW_SENHA;
    }

    /**
     * Shows the account overview.
     *
     * @param model view model
     * @param auth authenticated principal
     * @return account view
     */
    @GetMapping("/conta")
    public String conta(final Model model, final Authentication auth) {
        model.addAttribute(ACTIVE_ATTR, "conta");
        final String email = extrairEmail(auth);
        if (email != null) {
            final int total = acessoRepository.findByEmailIgnoreCase(email).size();
            model.addAttribute("temAcessos", total > 0);
            model.addAttribute("totalAcessos", total);
        } else {
            model.addAttribute("temAcessos", false);
            model.addAttribute("totalAcessos", 0);
        }
        final UsuarioEntity usuario = localizarUsuario(auth).orElse(null);
        if (usuario != null) {
            model.addAttribute("avatarUrl", usuario.getAvatarUrl());
            model.addAttribute("avatarNome", usuario.getNome());
            return VIEW_CONTA;
        }
        localizarCustomer(auth).ifPresent(customer -> {
            model.addAttribute("avatarUrl", customer.getAvatarUrl());
            model.addAttribute("avatarNome", customer.getNome());
        });
        return VIEW_CONTA;
    }

    /**
     * Shows the profile data page.
     *
     * @param model view model
     * @param auth authenticated principal
     * @return profile data view
     */
    @GetMapping("/dados")
    public String dados(final Model model, final Authentication auth) {
        model.addAttribute(ACTIVE_ATTR, "dados");
        final UsuarioEntity usuario = localizarUsuario(auth).orElse(null);
        final CustomerEntity customer = localizarCustomer(auth).orElse(null);
        if (usuario != null) {
            model.addAttribute("avatarUrl", usuario.getAvatarUrl());
            model.addAttribute("avatarNome", usuario.getNome());
        } else if (customer != null) {
            model.addAttribute("avatarUrl", customer.getAvatarUrl());
            model.addAttribute("avatarNome", customer.getNome());
        }
        if (!model.containsAttribute(FORM_ATTR)) {
            final ClienteDadosForm form = new ClienteDadosForm();
            if (usuario != null) {
                form.setNome(usuario.getNome());
                form.setEmail(usuario.getEmail());
                form.setCpf(usuario.getCpf());
                form.setTelefone(usuario.getTelefone());
                form.setEndereco(usuario.getEndereco());
            } else if (customer != null) {
                form.setNome(customer.getNome());
                form.setEmail(customer.getEmail());
            }
            model.addAttribute(FORM_ATTR, form);
        }
        return VIEW_DADOS;
    }

    /**
     * Persists updated profile data.
     *
     * @param form form payload
     * @param result binding result
     * @param auth authenticated principal
     * @param ra redirect attributes
     * @return redirect to profile data page
     */
    @PostMapping("/dados")
    public String atualizarDados(
            @Valid @ModelAttribute(FORM_ATTR) final ClienteDadosForm form,
            final BindingResult result,
            final Authentication auth,
            final RedirectAttributes ra
    ) {
        final Optional<UsuarioEntity> usuarioOpt = localizarUsuario(auth);
        if (usuarioOpt.isEmpty()) {
            ra.addFlashAttribute(ERROR_MESSAGE_ATTR, "Usuario nao encontrado.");
            return REDIRECT_DADOS;
        }

        final UsuarioEntity usuario = usuarioOpt.get();
        final String emailNormalizado = normalizarEmail(form.getEmail());

        if (emailNormalizado == null || emailNormalizado.isBlank()) {
            result.rejectValue(
                    "email",
                    "email.invalid",
                    "Informe um e-mail valido."
            );
        } else if (!emailNormalizado.equalsIgnoreCase(usuario.getEmail())) {
            usuarioRepository
                    .findByEmailIgnoreCase(emailNormalizado)
                    .ifPresent(existente -> {
                        if (!existente.getId().equals(usuario.getId())) {
                            result.rejectValue(
                                    "email",
                                    "email.duplicado",
                                    "E-mail ja cadastrado."
                            );
                        }
                    });
        }

        if (result.hasErrors()) {
            ra.addFlashAttribute(
                    "org.springframework.validation.BindingResult.form",
                    result
            );
            ra.addFlashAttribute(FORM_ATTR, form);
            return REDIRECT_DADOS;
        }

        usuario.setNome(form.getNome());
        usuario.setEmail(emailNormalizado);
        usuario.setTelefone(normalizarTelefone(form.getTelefone()));
        usuario.setEndereco(normalizarEndereco(form.getEndereco()));
        usuarioRepository.save(usuario);
        ra.addFlashAttribute(
                INFO_MESSAGE_ATTR,
                "Dados atualizados com sucesso."
        );
        return REDIRECT_DADOS;
    }

    /**
     * Changes account password.
     *
     * @param form form payload
     * @param result binding result
     * @param auth authenticated principal
     * @param ra redirect attributes
     * @return redirect to password page
     */
    @PostMapping("/senha")
    public String alterarSenha(
            @Valid @ModelAttribute(FORM_ATTR) final ChangePasswordRequest form,
            final BindingResult result,
            final Authentication auth,
            final RedirectAttributes ra
    ) {
        if (!form.getNovaSenha().equals(form.getConfirmarNovaSenha())) {
            result.rejectValue(
                    "confirmarNovaSenha",
                    "password.confirm.mismatch",
                    "As senhas nao conferem."
            );
        }
        if (result.hasErrors()) {
            ra.addFlashAttribute(
                    "org.springframework.validation.BindingResult.form",
                    result
            );
            ra.addFlashAttribute(FORM_ATTR, form);
            return REDIRECT_SENHA;
        }
        final Pair<Long, String> user = extrairUsuario(auth);
        try {
            accountService.changePassword(
                    user.getFirst(),
                    form.getSenhaAtual(),
                    form.getNovaSenha()
            );
            ra.addFlashAttribute(
                    INFO_MESSAGE_ATTR,
                    "Senha alterada com sucesso."
            );
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute(ERROR_MESSAGE_ATTR, ex.getMessage());
        }
        return REDIRECT_SENHA;
    }

    /**
     * Updates account avatar.
     *
     * @param file uploaded file
     * @param auth authenticated principal
     * @param ra redirect attributes
     * @return redirect to profile data page
     */
    @PostMapping("/avatar")
    public String atualizarAvatar(
            @RequestParam("file") final MultipartFile file,
            final Authentication auth,
            final RedirectAttributes ra
    ) {
        final Optional<UsuarioEntity> usuarioOpt = localizarUsuario(auth);
        final Optional<CustomerEntity> customerOpt = localizarCustomer(auth);
        if (usuarioOpt.isEmpty() && customerOpt.isEmpty()) {
            ra.addFlashAttribute(ERROR_MESSAGE_ATTR, "Usuario nao encontrado.");
            return REDIRECT_DADOS;
        }
        if (file == null || file.isEmpty()) {
            ra.addFlashAttribute(
                    ERROR_MESSAGE_ATTR,
                    "Selecione uma imagem para enviar."
            );
            return REDIRECT_DADOS;
        }
        if (file.getSize() > AVATAR_MAX_BYTES) {
            ra.addFlashAttribute(
                    ERROR_MESSAGE_ATTR,
                    "Imagem acima de 2MB. Reduza o tamanho e tente novamente."
            );
            return REDIRECT_DADOS;
        }
        final String contentType = normalizarContentType(file.getContentType());
        if (!isImageContentTypeValido(contentType)) {
            ra.addFlashAttribute(
                    ERROR_MESSAGE_ATTR,
                    "Formato invalido. Use PNG, JPG ou WEBP."
            );
            return REDIRECT_DADOS;
        }

        final Long ownerId = usuarioOpt.map(UsuarioEntity::getId).orElseGet(
                () -> customerOpt.map(CustomerEntity::getId).orElse(null)
        );
        try {
            final String previousUsuarioAvatar = usuarioOpt.map(UsuarioEntity::getAvatarUrl).orElse(null);
            final String previousCustomerAvatar = customerOpt.map(CustomerEntity::getAvatarUrl).orElse(null);
            final String url = imageStorageService.saveUserAvatar(ownerId, file);
            try {
                usuarioOpt.ifPresent(usuario -> {
                    usuario.setAvatarUrl(url);
                    usuarioRepository.save(usuario);
                });
                customerOpt.ifPresent(customer -> {
                    customer.setAvatarUrl(url);
                    customerRepository.save(customer);
                });
            } catch (RuntimeException ex) {
                usuarioOpt.ifPresent(usuario -> restoreUserAvatar(usuario, previousUsuarioAvatar));
                customerOpt.ifPresent(customer -> restoreCustomerAvatar(customer, previousCustomerAvatar));
                imageStorageService.deleteUserAvatarByUrl(url);
                throw ex;
            }
            ra.addFlashAttribute(
                    INFO_MESSAGE_ATTR,
                    "Foto atualizada com sucesso."
            );
        } catch (IOException | RuntimeException ex) {
            ra.addFlashAttribute(ERROR_MESSAGE_ATTR, ex.getMessage());
        }
        return REDIRECT_DADOS;
    }

    /**
     * Extracts current user id and email.
     *
     * @param auth authenticated principal
     * @return pair with user id and email
     */
    private Pair<Long, String> extrairUsuario(final Authentication auth) {
        final UsuarioEntity usuario = localizarUsuario(auth).orElseThrow(
                () -> new IllegalArgumentException("Usuario nao encontrado.")
        );
        return Pair.of(usuario.getId(), usuario.getEmail());
    }

    /**
     * Resolves the domain user from authentication details.
     *
     * @param auth authenticated principal
     * @return optional user
     */
    private Optional<UsuarioEntity> localizarUsuario(
            final Authentication auth
    ) {
        if (auth == null || auth.getName() == null) {
            return Optional.empty();
        }
        final Optional<UsuarioEntity> byIdentity = usuarioRepository
                .findByEmailOrCpf(auth.getName());
        if (byIdentity.isPresent()) {
            return byIdentity;
        }
        final String email = extrairEmail(auth);
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Resolves the social customer from authentication details.
     *
     * @param auth authenticated principal
     * @return optional customer
     */
    private Optional<CustomerEntity> localizarCustomer(
            final Authentication auth
    ) {
        final String email = extrairEmail(auth);
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return customerRepository.findByEmail(email);
    }

    /**
     * Normalizes email value.
     *
     * @param email raw email
     * @return normalized email
     */
    private String normalizarEmail(final String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    /**
     * Normalizes phone value.
     *
     * @param telefone raw phone
     * @return normalized phone
     */
    private String normalizarTelefone(final String telefone) {
        if (telefone == null) {
            return null;
        }
        final String trimmed = telefone.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    /**
     * Normalizes address value.
     *
     * @param endereco raw address
     * @return normalized address
     */
    private String normalizarEndereco(final String endereco) {
        if (endereco == null) {
            return null;
        }
        final String trimmed = endereco.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    /**
     * Tries to extract an email from authentication principal.
     *
     * @param auth authenticated principal
     * @return normalized email when available
     */
    private String extrairEmail(final Authentication auth) {
        if (auth == null) {
            return null;
        }
        if (auth.getName() != null && auth.getName().contains("@")) {
            return normalizarEmail(auth.getName());
        }
        final Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            final String username = userDetails.getUsername();
            return username != null && username.contains("@")
                    ? normalizarEmail(username)
                    : null;
        }
        if (principal instanceof OAuth2User oauth2User) {
            final Object email = oauth2User.getAttributes().get("email");
            if (email != null && !email.toString().isBlank()) {
                return normalizarEmail(email.toString());
            }
        }
        return null;
    }

    private String resolveRecoveryEmail(final Authentication auth) {
        final Optional<UsuarioEntity> usuario = localizarUsuario(auth);
        if (usuario.isPresent()) {
            return normalizarEmail(usuario.get().getEmail());
        }
        final Optional<CustomerEntity> customer = localizarCustomer(auth);
        if (customer.isPresent()) {
            return normalizarEmail(customer.get().getEmail());
        }
        return extrairEmail(auth);
    }

    private String maskEmail(final String email) {
        final String normalized = normalizarEmail(email);
        if (normalized == null || normalized.isBlank() || !normalized.contains("@")) {
            return "";
        }
        final String[] parts = normalized.split("@", 2);
        final String localPart = parts[0];
        final String domainPart = parts[1];
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***@" + domainPart;
        }
        return localPart.substring(0, 2) + "***@" + domainPart;
    }

    /**
     * Normalizes content type to lower-case text.
     *
     * @param contentType raw content type
     * @return normalized content type
     */
    private String normalizarContentType(final String contentType) {
        if (contentType == null) {
            return "";
        }
        return contentType.trim().toLowerCase();
    }

    /**
     * Checks if avatar content type is supported.
     *
     * @param contentType normalized content type
     * @return true when supported
     */
    private boolean isImageContentTypeValido(final String contentType) {
        return IMAGE_JPEG.equals(contentType)
                || IMAGE_PNG.equals(contentType)
                || IMAGE_WEBP.equals(contentType);
    }

    private void restoreUserAvatar(final UsuarioEntity usuario, final String previousAvatarUrl) {
        try {
            usuario.setAvatarUrl(previousAvatarUrl);
            usuarioRepository.save(usuario);
        } catch (RuntimeException ignored) {
            // Melhor esforço para evitar avatar apontando para arquivo removido.
        }
    }

    private void restoreCustomerAvatar(final CustomerEntity customer, final String previousAvatarUrl) {
        try {
            customer.setAvatarUrl(previousAvatarUrl);
            customerRepository.save(customer);
        } catch (RuntimeException ignored) {
            // Melhor esforço para evitar avatar apontando para arquivo removido.
        }
    }

    /**
     * Form payload used in profile data page.
     */
    @Getter
    @Setter
    public static class ClienteDadosForm {

        /**
         * User full name.
         */
        @NotBlank(message = "Informe seu nome.")
        @Size(
                max = NOME_MAX_LEN,
                message = "O nome deve ter no maximo 120 caracteres."
        )
        private String nome;

        /**
         * User email.
         */
        @NotBlank(message = "Informe seu e-mail.")
        @Email(message = "E-mail invalido.")
        @Size(
                max = EMAIL_MAX_LEN,
                message = "O e-mail deve ter no maximo 150 caracteres."
        )
        private String email;

        /**
         * User CPF.
         */
        private String cpf;

        /**
         * User phone.
         */
        @Size(
                max = TELEFONE_MAX_LEN,
                message = "O telefone deve ter no maximo 25 caracteres."
        )
        private String telefone;

        /**
         * User address.
         */
        @Size(
                max = ENDERECO_MAX_LEN,
                message = "O endereco deve ter no maximo 200 caracteres."
        )
        private String endereco;
    }
}
