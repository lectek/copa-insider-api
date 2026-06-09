package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.application.service.NotaFiscalService;
import br.com.redemaisfarma.application.service.fiscal.PedidoFiscalSnapshotService;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintChannel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/nota-fiscal")
@RequiredArgsConstructor
public class ConfirmaNotaFiscalApiController {

    private final NotaFiscalService service;
    private final PedidoFiscalSnapshotService pedidoFiscalSnapshotService;
    private final PedidoRepository pedidoRepository;

    @PostMapping("/confirmacao")
    public ResponseEntity<Void> confirmarNota(@Valid @RequestBody PreferenciaNotaDTO dto) {
        Long id = service.confirmar(dto.nome(), dto.preferencia(), dto.email());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @PostMapping("/pedidos/{pedidoId}/preferencias")
    public ResponseEntity<PreferenciaFiscalResponseDTO> confirmarPreferenciaPedido(
            @PathVariable("pedidoId") final Long pedidoId,
            @Valid @RequestBody final PreferenciaFiscalPedidoDTO dto
    ) {
        final PedidoEntity pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido nao encontrado."
                ));
        pedidoFiscalSnapshotService.captureIfMissing(
                pedido,
                "CLIENTE_FISCAL_PREFERENCIAS"
        );
        final var snapshot = pedidoFiscalSnapshotService.updateDeliveryPreferences(
                pedidoId,
                new PedidoFiscalSnapshotService.UpdateDeliveryPreferencesRequest(
                        dto.printChannel(),
                        Boolean.TRUE.equals(dto.emailRequested()),
                        dto.email()
                )
        );
        return ResponseEntity.ok(
                new PreferenciaFiscalResponseDTO(
                        snapshot.getPedido().getId(),
                        snapshot.getPrintChannel(),
                        snapshot.isEmailDeliveryRequested(),
                        snapshot.getEmailDeliveryAddress()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        // LinkedHashMap para manter a ordem previsível
        Map<String, String> errors = new LinkedHashMap<>();

        // Erros de campo
        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            String message = err.getDefaultMessage();
            errors.put(err.getField(), message != null ? message : "Campo inválido");
        }

        // Erros globais (sem campo específico), se existirem
        ex.getBindingResult().getGlobalErrors().forEach(objectError -> {
            String message = objectError.getDefaultMessage();
            errors.put(objectError.getObjectName(), message != null ? message : "Requisição inválida");
        });

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            final IllegalArgumentException ex
    ) {
        return ResponseEntity.badRequest().body(
                Map.of("message", ex.getMessage())
        );
    }

    // DTO em record (Java 21) — imutável e compatível com Jackson/Spring Boot 3
    public record PreferenciaNotaDTO(
            @NotBlank(message = "Nome é obrigatório")
            String nome,

            @NotBlank(message = "Preferência é obrigatória")
            String preferencia,

            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "E-mail deve ser válido")
            String email
    ) { }

    public record PreferenciaFiscalPedidoDTO(
            FiscalPrintChannel printChannel,
            @NotNull(message = "Informe se deseja receber por e-mail")
            Boolean emailRequested,
            @Email(message = "E-mail deve ser valido")
            String email
    ) { }

    public record PreferenciaFiscalResponseDTO(
            Long pedidoId,
            FiscalPrintChannel printChannel,
            boolean emailRequested,
            String email
    ) { }
}
