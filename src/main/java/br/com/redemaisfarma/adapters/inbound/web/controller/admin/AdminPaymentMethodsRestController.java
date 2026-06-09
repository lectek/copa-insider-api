package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.service.CustomPaymentMethodService;
import br.com.redemaisfarma.application.service.CustomPaymentMethodService.CustomPaymentMethod;
import br.com.redemaisfarma.application.service.CustomPaymentMethodService.CustomPaymentMethodInput;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/pagamentos/metodos")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminPaymentMethodsRestController {

    private final CustomPaymentMethodService customMethods;

    public AdminPaymentMethodsRestController(
            final CustomPaymentMethodService customPaymentMethodService
    ) {
        this.customMethods = customPaymentMethodService;
    }

    @GetMapping
    public List<PaymentMethodResponse> list() {
        return customMethods.list().stream()
                .map(PaymentMethodResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<PaymentMethodResponse> create(
            @Valid @RequestBody final PaymentMethodRequest request
    ) {
        try {
            final CustomPaymentMethod created = customMethods.create(
                    request.toInput()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(PaymentMethodResponse.from(created));
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public PaymentMethodResponse update(
            @PathVariable("id") final String id,
            @Valid @RequestBody final PaymentMethodRequest request
    ) {
        try {
            final CustomPaymentMethod updated = customMethods.update(
                    id,
                    request.toInput()
            );
            return PaymentMethodResponse.from(updated);
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final String id) {
        try {
            customMethods.delete(id);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    public record PaymentMethodRequest(
            @NotBlank @Size(max = 120) String nome,
            @Size(max = 20)
            @Pattern(
                    regexp = "^(?i)(offline|online|custom|pos)?$",
                    message = "Tipo invalido. Use offline, online, custom ou pos."
            )
            String tipo,
            @DecimalMin(value = "0.00") BigDecimal taxa,
            Boolean ativo
    ) {
        private CustomPaymentMethodInput toInput() {
            return new CustomPaymentMethodInput(nome, tipo, taxa, ativo);
        }
    }

    public record PaymentMethodResponse(
            String id,
            String nome,
            String tipo,
            BigDecimal taxa,
            boolean ativo,
            String value
    ) {
        private static PaymentMethodResponse from(final CustomPaymentMethod m) {
            return new PaymentMethodResponse(
                    m.getId(),
                    m.getNome(),
                    m.getTipo(),
                    m.getTaxa(),
                    m.isAtivo(),
                    "custom:" + m.getId()
            );
        }
    }
}
