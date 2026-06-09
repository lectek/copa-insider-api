package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
        value = "/api/auth/email-claim",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Validated
public class AuthEmailClaimController {

    /**
     * OTP service used for delivery and verification flows.
     */
    private final OtpServicePort otpService;

    /**
     * Repository used to verify existing users by e-mail.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Creates controller with required dependencies.
     *
     * @param otpPort otp service port
     * @param repository user repository
     */
    public AuthEmailClaimController(
            final OtpServicePort otpPort,
            final UsuarioRepository repository
    ) {
        this.otpService = otpPort;
        this.usuarioRepository = repository;
    }

    /**
     * Starts OTP flow for e-mail claim.
     *
     * @param request request payload
     * @return start response
     */
    @PostMapping(value = "/start", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StartRes> start(
            @RequestBody @Valid final StartReq request
    ) {
        final OtpServicePort.StartResult startResult = otpService.start(
                "email",
                request.email(),
                request.previousDeliveryId()
        );
        final boolean exists = usuarioRepository.existsByEmailIgnoreCase(
                request.email()
        );
        final StartRes body = new StartRes(
                startResult.deliveryId(),
                startResult.maskedDestino(),
                startResult.cooldownSec(),
                startResult.ttlSeconds(),
                exists,
                startResult.demoCode()
        );
        return ResponseEntity.ok(body);
    }

    /**
     * Verifies OTP code and returns claim token.
     *
     * @param request verify payload
     * @return claim token response or validation error body
     */
    @PostMapping(value = "/verify", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> verify(
            @RequestBody @Valid final VerifyReq request
    ) {
        try {
            final String token = otpService.verify(
                    request.deliveryId(),
                    request.code()
            );
            final boolean exists = usuarioRepository.existsByEmailIgnoreCase(
                    request.email()
            );
            return ResponseEntity.ok(new VerifyRes(token, exists));
        } catch (OtpServicePort.OtpException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "reason",
                    ex.reason(),
                    "message",
                    ex.getMessage()
            ));
        }
    }

    /**
     * Start request payload.
     *
     * @param email user e-mail
     * @param previousDeliveryId previous delivery id
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StartReq(
            @NotBlank @Email String email,
            @JsonAlias({"previous_delivery_id", "previousDeliveryId"})
            String previousDeliveryId
    ) {
    }

    /**
     * Start response payload.
     *
     * @param deliveryId delivery id
     * @param maskedDestino masked destination
     * @param cooldownSec cooldown seconds
     * @param ttlSeconds expiration in seconds
     * @param userExists whether user already exists
     * @param demoCode debug/demo code
     */
    public record StartRes(
            String deliveryId,
            String maskedDestino,
            int cooldownSec,
            int ttlSeconds,
            boolean userExists,
            String demoCode
    ) {
    }

    /**
     * Verify request payload.
     *
     * @param deliveryId delivery id
     * @param code otp code
     * @param email user e-mail
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VerifyReq(
            @JsonAlias({"delivery_id", "deliveryId"})
            @NotBlank String deliveryId,
            @NotBlank String code,
            @NotBlank @Email String email
    ) {
    }

    /**
     * Verify response payload.
     *
     * @param token claim token
     * @param userExists whether user exists
     */
    public record VerifyRes(String token, boolean userExists) {
    }
}
