package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.dto.request.EmailSuporteRequestDTO;
import br.com.redemaisfarma.application.service.SuporteMailService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(
        value = "/api/suporte",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class EmailSuporteController {

    /**
     * Service that sends support emails.
     */
    private final SuporteMailService service;

    /**
     * Sends support email request.
     *
     * @param request support request payload
     * @return operation summary
     */
    @PostMapping(value = "/email", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> enviar(
            @Valid @RequestBody final EmailSuporteRequestDTO request
    ) {
        final UUID trace = request.getTraceId() != null
                ? request.getTraceId()
                : UUID.randomUUID();
        if (request.getTraceId() == null) {
            request.setTraceId(trace);
        }

        log.info(
                "[suporte] recebida solicitacao | tenantId={}, assunto='{}', "
                        + "prioridade={}, categoria={}, traceId={}",
                request.getTenantId(),
                request.getAssunto(),
                request.getPrioridade(),
                request.getCategoria(),
                trace
        );

        service.enviar(request);

        return ResponseEntity.ok(
                Map.of(
                        "status", "sent",
                        "assunto", request.getAssunto(),
                        "prioridade", String.valueOf(request.getPrioridade()),
                        "categoria", String.valueOf(request.getCategoria()),
                        "tenantId", request.getTenantId(),
                        "traceId", trace.toString()
                )
        );
    }

    /**
     * Health endpoint for support API.
     *
     * @return plain text health response
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("suporte ok");
    }
}
