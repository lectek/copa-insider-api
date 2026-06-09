package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.application.dto.otp.OtpStartRequest;
import br.com.lectek.copainsider.application.dto.otp.OtpStartResponse;
import br.com.lectek.copainsider.application.dto.otp.OtpVerifyRequest;
import br.com.lectek.copainsider.application.dto.otp.OtpVerifyResponse;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/auth/otp", produces = "application/json")
@Validated
public class AuthOtpController {

    /**
     * Internal error reason code.
     */
    private static final String INTERNAL_ERROR_REASON = "internal_error";

    /**
     * Title used in internal error responses.
     */
    private static final String INTERNAL_ERROR_TITLE = "Erro interno";

    /**
     * Message used when OTP send fails unexpectedly.
     */
    private static final String START_INTERNAL_MESSAGE =
            "Nao foi possivel enviar o codigo agora. Tente novamente.";

    /**
     * Message used when OTP verify fails unexpectedly.
     */
    private static final String VERIFY_INTERNAL_MESSAGE =
            "Nao foi possivel verificar o codigo agora. Tente novamente.";

    /**
     * Logger instance.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(AuthOtpController.class);

    /**
     * OTP domain service.
     */
    private final OtpServicePort otp;

    /**
     * Creates controller with OTP service dependency.
     *
     * @param otpService OTP service
     */
    public AuthOtpController(final OtpServicePort otpService) {
        this.otp = otpService;
    }

    /**
     * Starts OTP flow and sends code using requested channel.
     *
     * @param req start request payload
     * @return OTP start response or problem details
     */
    @PostMapping(value = "/start", consumes = "application/json")
    public ResponseEntity<?> start(
            @Valid @RequestBody final OtpStartRequest req
    ) {
        try {
            final OtpServicePort.StartResult res = otp.start(
                    req.canal(),
                    req.destino(),
                    req.previousDeliveryId()
            );
            return ResponseEntity.ok(new OtpStartResponse(
                    res.deliveryId(),
                    res.maskedDestino(),
                    res.cooldownSec(),
                    res.ttlSeconds(),
                    res.demoCode()
            ));
        } catch (OtpServicePort.OtpException ex) {
            return ResponseEntity.badRequest().body(buildBadRequestProblem(
                    "OTP nao enviado",
                    ex.getMessage(),
                    ex.reason()
            ));
        } catch (Exception ex) {
            LOGGER.error("Falha inesperada em /otp/start", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildInternalProblem(START_INTERNAL_MESSAGE));
        }
    }

    /**
     * Verifies OTP code and returns authentication token.
     *
     * @param req verify request payload
     * @return OTP verify response or problem details
     */
    @PostMapping(value = "/verify", consumes = "application/json")
    public ResponseEntity<?> verify(
            @Valid @RequestBody final OtpVerifyRequest req
    ) {
        try {
            final String token = otp.verify(req.deliveryId(), req.code());
            return ResponseEntity.ok(new OtpVerifyResponse(token));
        } catch (OtpServicePort.OtpException ex) {
            return ResponseEntity.badRequest().body(buildBadRequestProblem(
                    "OTP invalido",
                    ex.getMessage(),
                    ex.reason()
            ));
        } catch (Exception ex) {
            LOGGER.error("Falha inesperada na verificacao de OTP", ex);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildInternalProblem(VERIFY_INTERNAL_MESSAGE));
        }
    }

    /**
     * Creates a problem payload for bad request responses.
     *
     * @param title problem title
     * @param detail problem detail
     * @param reason reason code
     * @return problem detail object
     */
    private ProblemDetail buildBadRequestProblem(
            final String title,
            final String detail,
            final String reason
    ) {
        final ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                detail
        );
        pd.setTitle(title);
        pd.setProperty("reason", reason);
        return pd;
    }

    /**
     * Creates a problem payload for internal server errors.
     *
     * @param detail problem detail
     * @return problem detail object
     */
    private ProblemDetail buildInternalProblem(final String detail) {
        final ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                detail
        );
        pd.setTitle(INTERNAL_ERROR_TITLE);
        pd.setProperty("reason", INTERNAL_ERROR_REASON);
        return pd;
    }
}
