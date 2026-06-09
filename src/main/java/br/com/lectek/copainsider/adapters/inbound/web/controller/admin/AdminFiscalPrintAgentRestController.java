package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.inbound.web.security.FiscalPrintAgentApiKeyFilter;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalPrintJobEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalPrintStationEntity;
import br.com.lectek.copainsider.application.service.fiscal.FiscalPrintQueueService;
import br.com.lectek.copainsider.application.service.fiscal.FiscalPrintStationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/fiscal/impressao/agente")
@PreAuthorize("hasAnyRole('ADMIN','CAIXA','PRINT_AGENT')")
@Validated
public class AdminFiscalPrintAgentRestController {

    private final FiscalPrintQueueService fiscalPrintQueueService;
    private final FiscalPrintStationService fiscalPrintStationService;

    public AdminFiscalPrintAgentRestController(
            final FiscalPrintQueueService fiscalPrintQueueServiceValue,
            final FiscalPrintStationService fiscalPrintStationServiceValue
    ) {
        this.fiscalPrintQueueService = fiscalPrintQueueServiceValue;
        this.fiscalPrintStationService = fiscalPrintStationServiceValue;
    }

    @PostMapping("/estacoes/{code}/heartbeat")
    public StationHeartbeatResponse heartbeat(
            @PathVariable("code") final String code
    ) {
        try {
            final FiscalPrintStationEntity station =
                    fiscalPrintStationService.recordHeartbeat(code);
            return new StationHeartbeatResponse(
                    station.getId(),
                    station.getCode(),
                    station.getDisplayName(),
                    station.isActive(),
                    station.getLastHeartbeatAt()
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (java.util.NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @GetMapping("/estacoes/{code}/proximo-job")
    public NextPrintJobResponse nextJob(
            @PathVariable("code") final String code,
            final Authentication authentication
    ) {
        try {
            final Optional<FiscalPrintJobEntity> job =
                    fiscalPrintQueueService.claimNextReadyJob(
                            code,
                            actor(authentication)
                    );
            if (job.isEmpty()) {
                return new NextPrintJobResponse(null);
            }
            return new NextPrintJobResponse(AgentJobPayload.from(job.get()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (java.util.NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PostMapping("/jobs/{jobId}/concluido")
    public AgentJobPayload markPrinted(
            @PathVariable("jobId") final Long jobId,
            @RequestHeader(
                    name = FiscalPrintAgentApiKeyFilter.HEADER_STATION_CODE,
                    required = false
            )
            final String stationCode,
            final Authentication authentication
    ) {
        try {
            return AgentJobPayload.from(
                    isPrintAgent(authentication)
                            ? fiscalPrintQueueService.markPrintedForStation(
                                    jobId,
                                    stationCode,
                                    actor(authentication)
                            )
                            : fiscalPrintQueueService.markPrinted(
                                    jobId,
                                    actor(authentication)
                            )
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (java.util.NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @PostMapping("/jobs/{jobId}/falhou")
    public AgentJobPayload markFailed(
            @PathVariable("jobId") final Long jobId,
            @Valid @RequestBody final PrintFailureRequest request,
            @RequestHeader(
                    name = FiscalPrintAgentApiKeyFilter.HEADER_STATION_CODE,
                    required = false
            )
            final String stationCode,
            final Authentication authentication
    ) {
        try {
            return AgentJobPayload.from(
                    isPrintAgent(authentication)
                            ? fiscalPrintQueueService.markFailedForStation(
                                    jobId,
                                    stationCode,
                                    actor(authentication),
                                    request.message()
                            )
                            : fiscalPrintQueueService.markFailed(
                                    jobId,
                                    actor(authentication),
                                    request.message()
                            )
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (java.util.NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    private String actor(final Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "PRINT_AGENT";
        }
        final String value = authentication.getName().trim();
        return value.isBlank() ? "PRINT_AGENT" : value;
    }

    private boolean isPrintAgent(final Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_PRINT_AGENT"::equals);
    }

    public record StationHeartbeatResponse(
            Long id,
            String code,
            String displayName,
            boolean active,
            LocalDateTime lastHeartbeatAt
    ) {
    }

    public record NextPrintJobResponse(
            AgentJobPayload job
    ) {
    }

    public record PrintFailureRequest(
            @NotBlank
            @Size(max = 500)
            String message
    ) {
    }

    public record AgentJobPayload(
            Long jobId,
            Long pedidoId,
            Long fiscalDocumentId,
            String jobType,
            String printChannel,
            String stationCode,
            String stationName,
            Integer copies,
            String accessKey,
            String danfeUrl,
            String documentStatus,
            LocalDateTime updatedAt
    ) {
        static AgentJobPayload from(final FiscalPrintJobEntity job) {
            return new AgentJobPayload(
                    job.getId(),
                    job.getPedido() == null ? null : job.getPedido().getId(),
                    job.getFiscalDocument() == null
                            ? null
                            : job.getFiscalDocument().getId(),
                    job.getJobType() == null ? null : job.getJobType().name(),
                    job.getPrintChannel() == null
                            ? null
                            : job.getPrintChannel().name(),
                    job.getStation() == null ? null : job.getStation().getCode(),
                    job.getStation() == null
                            ? null
                            : job.getStation().getDisplayName(),
                    job.getCopies(),
                    job.getFiscalDocument() == null
                            ? null
                            : job.getFiscalDocument().getAccessKey(),
                    job.getFiscalDocument() == null
                            ? null
                            : job.getFiscalDocument().getDanfeStoragePath(),
                    job.getFiscalDocument() == null
                            ? null
                            : job.getFiscalDocument().getStatus() == null
                                    ? null
                                    : job.getFiscalDocument().getStatus().name(),
                    job.getUpdatedAt()
            );
        }
    }
}
