package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.inbound.web.request.RegisterRequest;
import br.com.redemaisfarma.application.core.account.UserAccountService;
import br.com.redemaisfarma.application.dto.request.LoginRequest;
import br.com.redemaisfarma.application.dto.response.AuthResponse;
import br.com.redemaisfarma.application.dto.response.LoginResponseDTO;
import br.com.redemaisfarma.application.service.AuthService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    /**
     * Generic bad credentials message.
     */
    private static final String MSG_INVALID_CREDENTIALS =
            "Credenciais invalidas.";

    /**
     * Login failed message.
     */
    private static final String MSG_USER_OR_PASSWORD_INVALID =
            "Usuario ou senha invalidos.";

    /**
     * Generic auth internal error message.
     */
    private static final String MSG_INTERNAL_AUTH_ERROR =
            "Erro interno ao autenticar.";

    /**
     * Default tenant id when absent.
     */
    private static final String DEFAULT_TENANT_ID = "rede-mais-farma";

    /**
     * Welcome message returned after successful login.
     */
    private static final String WELCOME_MESSAGE = "Bem-vindo!";

    /**
     * Authentication service.
     */
    private final AuthService authService;

    /**
     * User account service for registration.
     */
    private final UserAccountService accounts;

    /**
     * Creates controller with required dependencies.
     *
     * @param authServiceValue auth service
     * @param accountServiceValue account service
     */
    public AuthApiController(
            final AuthService authServiceValue,
            final UserAccountService accountServiceValue
    ) {
        this.authService = authServiceValue;
        this.accounts = accountServiceValue;
    }

    /**
     * Authenticates a user and returns token payload.
     *
     * @param req login request
     * @return login response
     */
    @PostMapping(
            path = "/login",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<?> login(@Valid @RequestBody final LoginRequest req) {
        try {
            final String usuario = safeTrim(req.getUsuario());
            final String senha = safeTrim(req.getSenha());
            if (!StringUtils.hasText(usuario) || !StringUtils.hasText(senha)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap(
                                "error",
                                MSG_INVALID_CREDENTIALS
                        ));
            }
            final AuthResponse auth = authService.authenticate(usuario, senha);
            return ResponseEntity.ok(buildLoginResponse(auth));
        } catch (AuthenticationException ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap(
                            "error",
                            MSG_USER_OR_PASSWORD_INVALID
                    ));
        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap(
                            "error",
                            MSG_INTERNAL_AUTH_ERROR
                    ));
        }
    }

    /**
     * Registers a new account and immediately authenticates it.
     *
     * @param req registration request
     * @return authentication payload
     */
    @PostMapping(
            path = "/register",
            consumes = "application/json",
            produces = "application/json"
    )
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(
            @Valid @RequestBody final RegisterRequest req
    ) {
        accounts.register(req.name(), req.email(), req.cpf(), req.password());
        return authService.authenticate(req.email(), req.password());
    }

    /**
     * Builds login DTO using authenticated response payload.
     *
     * @param auth authenticated response
     * @return login response DTO
     */
    private LoginResponseDTO buildLoginResponse(final AuthResponse auth) {
        return new LoginResponseDTO(
                auth.getAccessToken(),
                auth.getRefreshToken(),
                mapUserType(auth.getRoles()),
                auth.getUserId(),
                auth.getUsername(),
                auth.getEmail(),
                safeList(auth.getRoles()),
                auth.getExpiresAt(),
                LocalDateTime.now(),
                LoginResponseDTO.AccountStatus.ACTIVE,
                WELCOME_MESSAGE,
                auth.getTenantId() != null
                        ? auth.getTenantId()
                        : DEFAULT_TENANT_ID,
                auth.getTraceId() != null
                        ? auth.getTraceId()
                        : UUID.randomUUID()
        );
    }

    /**
     * Trims a string value safely.
     *
     * @param value source value
     * @return trimmed value or null
     */
    private static String safeTrim(final String value) {
        return value == null ? null : value.trim();
    }

    /**
     * Returns an empty list when roles are absent.
     *
     * @param roles roles list
     * @return non-null roles list
     */
    private static List<String> safeList(final List<String> roles) {
        return roles == null ? Collections.emptyList() : roles;
    }

    /**
     * Resolves user type based on role names.
     *
     * @param roles roles list
     * @return resolved user type
     */
    private static LoginResponseDTO.UserType mapUserType(
            final List<String> roles
    ) {
        if (roles == null || roles.isEmpty()) {
            return LoginResponseDTO.UserType.CLIENTE;
        }
        for (String r : roles) {
            if (r == null) {
                continue;
            }
            final String role = r.toUpperCase(Locale.ROOT);
            if (role.contains("ROLE_ADMIN")) {
                return LoginResponseDTO.UserType.ADMIN;
            }
            if (role.contains("ROLE_ATENDENTE")) {
                return LoginResponseDTO.UserType.ATENDENTE;
            }
        }
        return LoginResponseDTO.UserType.CLIENTE;
    }
}
