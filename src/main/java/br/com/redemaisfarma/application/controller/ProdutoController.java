package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.service.ProdutoService;
import br.com.redemaisfarma.domain.Produto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@Slf4j
@Validated
@Deprecated(since = "2026-03", forRemoval = false)
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/app/produtos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "App - Produtos (LEGADO)", description = "Legado: prefira /api/admin/produtos para operacoes administrativas")
public class ProdutoController {

    private final ProdutoService produtoService;

    @Operation(summary = "Criar produto")
    @Deprecated(since = "2026-03", forRemoval = false)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("hasAuthority('APP:PRODUTO:CRIAR') or hasRole('ADMIN')")
    public ResponseEntity<Produto> create(@Valid @RequestBody Produto produto) {
        Produto salvo = produtoService.create(produto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();

        log.info("Produto id={} criado.", salvo.getId());
        return ResponseEntity.created(location).body(salvo);
    }

    @Operation(summary = "Listar produtos")
    @Deprecated(since = "2026-03", forRemoval = false)
    @GetMapping
    // @PreAuthorize("hasAuthority('APP:PRODUTO:LER') or hasRole('ADMIN')")
    public ResponseEntity<List<Produto>> list() {
        List<Produto> produtos = produtoService.list();
        return ResponseEntity.ok(produtos);
    }

    @Operation(summary = "Buscar produto por ID")
    @Deprecated(since = "2026-03", forRemoval = false)
    @GetMapping("/{id}")
    // @PreAuthorize("hasAuthority('APP:PRODUTO:LER') or hasRole('ADMIN')")
    public ResponseEntity<Produto> findById(@PathVariable("id") Long id) {
        Produto produto = produtoService.findById(id);
        return ResponseEntity.ok(produto);
    }

    @Operation(summary = "Atualizar produto")
    @Deprecated(since = "2026-03", forRemoval = false)
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("hasAuthority('APP:PRODUTO:ATUALIZAR') or hasRole('ADMIN')")
    public ResponseEntity<Produto> update(@PathVariable("id") Long id, @Valid @RequestBody Produto produto) {
        // path param é a fonte de verdade
        produto.setId(id);
        Produto atualizado = produtoService.update(id, produto);
        log.info("Produto id={} atualizado.", id);
        return ResponseEntity.ok(atualizado);
    }

    @Operation(summary = "Excluir produto")
    @Deprecated(since = "2026-03", forRemoval = false)
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAuthority('APP:PRODUTO:EXCLUIR') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        produtoService.delete(id);
        log.info("Produto id={} removido.", id);
        return ResponseEntity.noContent().build();
    }
}
