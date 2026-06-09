package br.com.lectek.copainsider.adapters.inbound.web.api.publico;

import br.com.lectek.copainsider.adapters.inbound.web.ProdutoCardDTO;
import br.com.lectek.copainsider.application.core.produto.ProdutoVitrineService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Generated;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Deprecated(since = "2026-03", forRemoval = false)
@RestController
@Validated
@RequestMapping("/api/public/vitrine")
public class VitrineApiController {

    private static final String SUCCESSOR = "/api/public/produtos/destaques";
    private final ProdutoVitrineService vitrine;

    @Deprecated(since = "2026-03", forRemoval = false)
    @GetMapping("/destaques")
    public ResponseEntity<List<ProdutoCardDTO>> destaques(
            @RequestParam(name = "limit", defaultValue = "12") @Min(1) @Max(50) int limit
    ) {
        List<ProdutoCardDTO> body = this.vitrine.listarDestaques(limit).stream()
                .map(ProdutoCardDTO::from)
                .toList();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5L, TimeUnit.MINUTES).cachePublic())
                .header("Deprecation", "true")
                .header("Sunset", "Tue, 30 Sep 2026 23:59:59 GMT")
                .header(HttpHeaders.LINK, "<" + SUCCESSOR + ">; rel=\"successor-version\"")
                .body(body);
    }

    @Deprecated(since = "2026-03", forRemoval = false)
    @Generated
    public VitrineApiController(ProdutoVitrineService vitrine) {
        this.vitrine = vitrine;
    }
}
