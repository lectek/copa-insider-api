package br.com.lectek.copainsider.adapters.inbound.web.security;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.domain.user.Role;
import br.com.lectek.copainsider.domain.user.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GoogleOAuth2UserService
        implements org.springframework.security.oauth2.client.userinfo.OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuth2UserService.class);

    private static final String ROLE_CLIENTE = "ROLE_CLIENTE";
    private static final String ATTR_EMAIL   = "email";

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UsuarioRepository usuarioRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public GoogleOAuth2UserService(UsuarioRepository usuarioRepo,
                                   RoleRepository roleRepo,
                                   PasswordEncoder encoder) {
        this.usuarioRepo = usuarioRepo;
        this.roleRepo    = roleRepo;
        this.encoder     = encoder;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User googleUser = delegate.loadUser(request);

        String email = googleUser.getAttribute(ATTR_EMAIL);
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("google_no_email");
        }
        email = email.trim().toLowerCase();

        final String finalEmail = email;
        UsuarioEntity usuario = usuarioRepo.findByEmailIgnoreCase(finalEmail)
                .map(existing -> {
                    if (!existing.isEmailVerificado()) {
                        existing.setEmailVerificado(true);
                        log.info("[oauth2] email activado via Google: {}", finalEmail);
                    }
                    if (existing.getRoles() == null || existing.getRoles().isEmpty()) {
                        existing.addRole(resolveClienteRole());
                        log.info("[oauth2] ROLE_CLIENTE adicionado a utilizador existente sem roles: {}", finalEmail);
                        usuarioRepo.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> {
                    String nome = googleUser.getAttribute("name");
                    if (nome == null || nome.isBlank()) nome = finalEmail;
                    UsuarioEntity u = new UsuarioEntity();
                    u.setEmail(finalEmail);
                    u.setNome(nome.trim());
                    u.setSenha(encoder.encode(UUID.randomUUID().toString()));
                    u.setEmailVerificado(true);
                    u.addRole(resolveClienteRole());
                    log.info("[oauth2] novo utilizador via Google: {}", finalEmail);
                    return usuarioRepo.save(u);
                });

        List<GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(r -> {
                    String nome = r.getNome();
                    return new SimpleGrantedAuthority(nome.startsWith("ROLE_") ? nome : "ROLE_" + nome);
                })
                .collect(Collectors.toList());

        return new DefaultOAuth2User(authorities, googleUser.getAttributes(), ATTR_EMAIL);
    }

    private Role resolveClienteRole() {
        return roleRepo.findByNome(ROLE_CLIENTE)
                .or(() -> roleRepo.findByNome("CLIENTE"))
                .or(() -> roleRepo.findByNome("ROLE_USER"))
                .or(() -> roleRepo.findByNome("USER"))
                .orElseGet(() -> roleRepo.save(Role.of(ROLE_CLIENTE)));
    }
}
