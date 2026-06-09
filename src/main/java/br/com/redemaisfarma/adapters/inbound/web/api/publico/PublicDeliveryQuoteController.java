package br.com.redemaisfarma.adapters.inbound.web.api.publico;

import br.com.redemaisfarma.application.service.delivery.DeliveryPricingService;
import br.com.redemaisfarma.application.view.DeliveryQuoteVM;
import jakarta.validation.constraints.Size;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/public/entrega")
public class PublicDeliveryQuoteController {

    private final DeliveryPricingService deliveryPricingService;

    public PublicDeliveryQuoteController(
            final DeliveryPricingService deliveryPricingServiceValue
    ) {
        this.deliveryPricingService = deliveryPricingServiceValue;
    }

    @GetMapping("/cotacao")
    public ResponseEntity<DeliveryQuoteVM> quote(
            @RequestParam(name = "endereco", required = false)
            @Size(max = 255) final String endereco
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(deliveryPricingService.quoteForAddress(endereco));
    }
}
