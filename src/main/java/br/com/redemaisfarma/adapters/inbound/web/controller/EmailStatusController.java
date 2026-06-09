package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.inbound.web.dto.EmailDeliveryResponse;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailDelivery;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailDeliveryRepository;
import java.net.URI;
import java.util.Optional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/email")
public class EmailStatusController {

    /**
     * Maximum accepted page size.
     */
    private static final int MAX_PAGE_SIZE = 200;

    /**
     * Repository for email delivery status records.
     */
    private final EmailDeliveryRepository repo;

    /**
     * Creates controller with repository dependency.
     *
     * @param repository email delivery repository
     */
    public EmailStatusController(final EmailDeliveryRepository repository) {
        this.repo = repository;
    }

    /**
     * Retrieves a status record by id.
     *
     * @param id email delivery id
     * @return status record response when found
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmailDeliveryResponse> get(
            @PathVariable("id") final Long id
    ) {
        return repo.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Searches email status records using optional filters.
     *
     * @param destination destination email filter
     * @param status status filter
     * @param purpose purpose filter
     * @param page page number
     * @param size page size
     * @return paged status records
     */
    @GetMapping
    public ResponseEntity<Page<EmailDeliveryResponse>> search(
            @RequestParam(required = false) final String destination,
            @RequestParam(required = false) final String status,
            @RequestParam(required = false) final String purpose,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size
    ) {
        final PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(size, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        final EmailDelivery probe = new EmailDelivery();
        if (destination != null && !destination.isBlank()) {
            probe.setDestination(destination);
        }
        if (status != null && !status.isBlank()) {
            probe.setStatus(status);
        }
        if (purpose != null && !purpose.isBlank()) {
            probe.setPurpose(purpose);
        }

        final ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.EXACT);

        final Page<EmailDelivery> pageResult = repo.findAll(
                Example.of(probe, matcher),
                pageable
        );
        final Page<EmailDeliveryResponse> mapped = pageResult.map(
                this::toResponse
        );
        return ResponseEntity.ok(mapped);
    }

    /**
     * Retrieves the raw JSON payload for an email status record.
     *
     * @param id email delivery id
     * @return raw payload when found
     */
    @GetMapping(
            value = "/{id}/raw",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> raw(@PathVariable("id") final Long id) {
        return repo.findById(id)
                .map(EmailDelivery::getPayloadJson)
                .map(json -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(json))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Resets a failed status entry to pending.
     *
     * @param id email delivery id
     * @return accepted when retry is scheduled
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<?> retry(@PathVariable("id") final Long id) {
        final Optional<EmailDelivery> opt = repo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        final EmailDelivery e = opt.get();
        if (!"FAILED".equalsIgnoreCase(e.getStatus())) {
            return ResponseEntity.unprocessableEntity()
                    .body(
                            "Somente registros com status=FAILED"
                                    + " podem ser reprocessados."
                    );
        }

        e.setStatus("PENDING");
        e.setLastError(null);
        e.setAttempts(0);
        repo.save(e);

        return ResponseEntity.accepted()
                .location(URI.create("/api/email/" + e.getId()))
                .build();
    }

    private EmailDeliveryResponse toResponse(final EmailDelivery e) {
        return new EmailDeliveryResponse(
                e.getId(),
                e.getPurpose(),
                e.getDestination(),
                e.getProvider(),
                e.getStatus(),
                e.getAttempts(),
                e.getMessageId(),
                e.getLastError(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
