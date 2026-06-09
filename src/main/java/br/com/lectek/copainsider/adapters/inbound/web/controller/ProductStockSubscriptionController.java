package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProductStockSubscriptionEntity;
import br.com.lectek.copainsider.application.dto.request.StockSubscriptionRequest;
import br.com.lectek.copainsider.application.service.ProductStockSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/produtos/{produtoId}/stock")
@Tag(
        name = "App - Volta ao estoque",
        description = "Inscreve um e-mail para avisos de volta ao estoque"
)
public class ProductStockSubscriptionController {

    /**
     * Service for stock-subscription operations.
     */
    private final ProductStockSubscriptionService subscriptionService;

    /**
     * Creates the controller with required service.
     *
     * @param service stock-subscription service
     */
    public ProductStockSubscriptionController(
            final ProductStockSubscriptionService service
    ) {
        this.subscriptionService = service;
    }

    /**
     * Registers an e-mail for back-in-stock notifications.
     *
     * @param produtoId product identifier
     * @param request request payload
     * @return HTTP 201 with location of created subscription
     */
    @PostMapping("/subscribe")
    @Operation(summary = "Inscrever e-mail para aviso de estoque")
    public ResponseEntity<Void> subscribe(
            @PathVariable("produtoId") final Long produtoId,
            @Valid @RequestBody final StockSubscriptionRequest request
    ) {
        final ProductStockSubscriptionEntity subscription =
                subscriptionService.subscribe(
                produtoId,
                request.getEmail(),
                request.getNome()
        );
        final URI location = URI.create(
                "/api/public/produtos/" + produtoId + "/stock/subscriptions/"
                        + subscription.getId()
        );
        return ResponseEntity.created(location).build();
    }
}
