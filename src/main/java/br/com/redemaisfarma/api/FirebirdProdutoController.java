/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package br.com.redemaisfarma.api;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/firebird"})
@ConditionalOnProperty(prefix="app.sync.legacy", name={"enabled"}, havingValue="true")
public class FirebirdProdutoController {
    private final ProdutoLegacyRepository repo;

    public FirebirdProdutoController(ProdutoLegacyRepository repo) {
        this.repo = repo;
    }

    @GetMapping(value={"/produtos/codigo-barras/{ean}"})
    public ProdutoLegacyEntity porEan(@PathVariable("ean") String ean) {
        return this.repo.findByCodigoBarras(ean).orElse(null);
    }

    @GetMapping(value={"/produtos/busca"})
    public List<ProdutoLegacyEntity> porNome(@RequestParam String q) {
        return this.repo.findByNomeContainingIgnoreCase(q);
    }

    @GetMapping(value={"/produtos/estoque-positivo"})
    public List<ProdutoLegacyEntity> estoquePositivo(@RequestParam(defaultValue="0") BigDecimal minimo) {
        return this.repo.findBySaldoGreaterThan(minimo);
    }
}

