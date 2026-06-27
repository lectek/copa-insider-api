package br.com.lectek.copainsider.adapters.inbound.web.controller.auth;

import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaAcessoJPARepository;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticates a user who has already verified their e-mail via OTP.
 *
 * The client JS flow:
 *  1. POST /api/auth/email-claim/start  → receive deliveryId
 *  2. POST /api/auth/email-claim/verify → receive claimToken
 *  3. POST /auth/otp-login              → creates session, returns redirect URL
 *
 * This endpoint lives under /auth/ (not /api/) so that SecurityMvcConfig's
 * session management is active and the authenticated context persists.
 */
@RestController
@RequestMapping("/auth/otp-login")
@Validated
public class OtpLoginController {

    private static final String REDIRECT_ADMIN  = "/admin/dashboard";
    private static final String REDIRECT_COPA   = "/conta/acessos";
    private static final String REDIRECT_CLIENT = "/cliente/conta";

    private final OtpServicePort        otpService;
    private final UserDetailsService    userDetailsService;
    private final CopaAcessoJPARepository acessoRepo;

    public OtpLoginController(OtpServicePort otpService,
                               UserDetailsService userDetailsService,
                               @Lazy CopaAcessoJPARepository acessoRepo) {
        this.otpService         = otpService;
        this.userDetailsService = userDetailsService;
        this.acessoRepo         = acessoRepo;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq req,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {
        // 1. Consume OTP token — one-time use
        if (!otpService.consumeTokenForDestino(req.token(), req.email())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "reason", "token_invalido",
                    "message", "Código inválido ou expirado."));
        }

        // 2. Load user — raises UsernameNotFoundException if not found
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(req.email());
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "reason", "user_not_found",
                    "message", "Utilizador não encontrado."));
        }

        // 3. Guard: account must be active
        if (!userDetails.isEnabled()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "reason", "account_disabled",
                    "message", "Confirme o e-mail antes de entrar."));
        }
        if (!userDetails.isAccountNonLocked()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "reason", "account_locked",
                    "message", "Conta temporariamente bloqueada."));
        }

        // 4. Build authentication token
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

        // 5. Session fixation protection: invalidate old session, create new
        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.invalidate();
        }
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        new HttpSessionSecurityContextRepository().saveContext(ctx, request, response);

        // 6. Resolve post-login destination
        String email = userDetails.getUsername();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        String redirect;
        if (isAdmin) {
            redirect = REDIRECT_ADMIN;
        } else if (acessoRepo.existsByEmailIgnoreCase(email)) {
            redirect = REDIRECT_COPA;
        } else {
            redirect = REDIRECT_CLIENT;
        }

        return ResponseEntity.ok(Map.of("redirect", redirect));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record LoginReq(
            @NotBlank @Email String email,
            @NotBlank String token
    ) {}
}
