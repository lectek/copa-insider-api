package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.service.PaymentTerminalService;
import br.com.redemaisfarma.application.service.PaymentTerminalService.TerminalConfig;
import br.com.redemaisfarma.application.service.PaymentTerminalService.TerminalConfigInput;
import br.com.redemaisfarma.application.service.PaymentTerminalService.TerminalPaymentRequest;
import br.com.redemaisfarma.application.service.PaymentTerminalService.TerminalPaymentResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/admin/pagamentos/terminal")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminPaymentTerminalRestController {

    private final PaymentTerminalService terminalService;

    public AdminPaymentTerminalRestController(
            final PaymentTerminalService paymentTerminalService
    ) {
        this.terminalService = paymentTerminalService;
    }

    @GetMapping("/config")
    public TerminalConfigResponse loadConfig() {
        return TerminalConfigResponse.from(terminalService.loadConfig());
    }

    @PutMapping("/config")
    public TerminalConfigResponse saveConfig(
            @Valid @RequestBody final TerminalConfigRequest request
    ) {
        try {
            final TerminalConfig saved = terminalService.saveConfig(request.toInput());
            return TerminalConfigResponse.from(saved);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @PostMapping("/test")
    public TerminalTestResponse testTerminal(
            @Valid @RequestBody final TerminalTestRequest request
    ) {
        try {
            final TerminalPaymentResult result = terminalService.authorize(
                    request.toPaymentRequest()
            );
            return TerminalTestResponse.from(result);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    public record TerminalConfigRequest(
            Boolean enabled,
            @Pattern(
                    regexp = "^(?i)(mock|webhook)?$",
                    message = "Modo invalido. Use mock ou webhook."
            )
            String mode,
            @Size(max = 80) String provider,
            @Size(max = 512) String endpointUrl,
            @Size(max = 120) String terminalId,
            @Size(max = 120) String merchantId,
            @Min(1000) @Max(30000) Integer timeoutMs,
            @Size(max = 255) String secret,
            Boolean clearSecret
    ) {
        private TerminalConfigInput toInput() {
            return new TerminalConfigInput(
                    enabled,
                    mode,
                    provider,
                    endpointUrl,
                    terminalId,
                    merchantId,
                    timeoutMs,
                    secret,
                    clearSecret
            );
        }
    }

    public record TerminalConfigResponse(
            boolean enabled,
            String mode,
            String provider,
            String endpointUrl,
            String terminalId,
            String merchantId,
            int timeoutMs,
            boolean secretConfigured
    ) {
        private static TerminalConfigResponse from(final TerminalConfig config) {
            return new TerminalConfigResponse(
                    config.enabled(),
                    config.mode(),
                    config.provider(),
                    config.endpointUrl(),
                    config.terminalId(),
                    config.merchantId(),
                    config.timeoutMs(),
                    config.secretConfigured()
            );
        }
    }

    public record TerminalTestRequest(
            @DecimalMin(value = "0.01") BigDecimal valor,
            @Pattern(
                    regexp = "^(?i)(credito|debito|cartao_credito|cartao_debito)?$",
                    message = "Metodo invalido para teste."
            )
            String metodo,
            @Size(max = 80) String referencia
    ) {
        private TerminalPaymentRequest toPaymentRequest() {
            final String paymentType = resolvePaymentType(metodo);
            final String reference = referencia == null || referencia.isBlank()
                    ? "ADMIN-TESTE"
                    : referencia.trim();
            return new TerminalPaymentRequest(
                    valor,
                    paymentType,
                    reference,
                    "ADMIN_TESTE",
                    Map.of("source", "admin-config")
            );
        }

        private static String resolvePaymentType(final String value) {
            if (value == null || value.isBlank()) {
                return "CARTAO_CREDITO";
            }
            final String normalized = value.trim().toLowerCase();
            return switch (normalized) {
                case "debito", "cartao_debito" -> "CARTAO_DEBITO";
                default -> "CARTAO_CREDITO";
            };
        }
    }

    public record TerminalTestResponse(
            boolean approved,
            String status,
            String transactionId,
            String message
    ) {
        private static TerminalTestResponse from(final TerminalPaymentResult result) {
            return new TerminalTestResponse(
                    result.approved(),
                    result.status(),
                    result.transactionId(),
                    result.message()
            );
        }
    }
}
