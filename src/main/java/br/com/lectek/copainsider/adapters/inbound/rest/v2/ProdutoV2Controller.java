package br.com.lectek.copainsider.adapters.inbound.rest.v2;

import br.com.lectek.copainsider.application.dto.request.CadastroProdutoRequestDTO;
import br.com.lectek.copainsider.application.service.ProdutoService;
import br.com.lectek.copainsider.domain.Produto;
import jakarta.validation.Valid;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Deprecated(since = "2026-03", forRemoval = false)
@RestController("produtoV2Controller")
@RequestMapping(value = "/api/v2/produtos", produces = "application/json")
@Validated
public class ProdutoV2Controller {

    private final ProdutoService produtoService;

    @Deprecated(since = "2026-03", forRemoval = false)
    @Generated
    public ProdutoV2Controller(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    // ==========================================================
    // Criar novo produto
    // ==========================================================
    @Deprecated(since = "2026-03", forRemoval = false)
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Produto> create(@Valid @RequestBody CadastroProdutoRequestDTO dto) {
        Produto salvo = produtoService.create(toDomain(dto));
        URI location = URI.create("/api/v2/produtos/" + salvo.getId());
        return ResponseEntity.created(location).body(salvo);
    }

    // ==========================================================
    // Listar produtos
    // ==========================================================
    @Deprecated(since = "2026-03", forRemoval = false)
    @GetMapping
    public ResponseEntity<List<Produto>> list() {
        List<Produto> produtos = produtoService.list();
        return ResponseEntity.ok(produtos);
    }

    // ==========================================================
    // Buscar produto por ID
    // ==========================================================
    @Deprecated(since = "2026-03", forRemoval = false)
    @GetMapping("/{id}")
    public ResponseEntity<Produto> findById(@PathVariable("id") Long id) {
        Produto produto = produtoService.findById(id);
        return ResponseEntity.ok(produto);
    }

    // ==========================================================
    // Atualizar produto
    // ==========================================================
    @Deprecated(since = "2026-03", forRemoval = false)
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<Produto> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody CadastroProdutoRequestDTO dto
    ) {
        Produto atualizado = produtoService.update(id, toDomain(dto, id));
        return ResponseEntity.ok(atualizado);
    }

    // ==========================================================
    // Excluir produto
    // ==========================================================
    @Deprecated(since = "2026-03", forRemoval = false)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        produtoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Produto toDomain(CadastroProdutoRequestDTO dto) {
        return toDomain(dto, null);
    }

    private Produto toDomain(CadastroProdutoRequestDTO dto, Long id) {
        Produto domain = new Produto();
        domain.setId(id);
        domain.setNome(dto.getNome());
        domain.setDescricao(dto.getDescricao());
        domain.setPrecoVenda(dto.getPrecoVenda());
        domain.setCategoria(dto.getCategoria());
        if (id == null) {
            domain.setDataCadastro(LocalDateTime.now());
        }
        return domain;
    }
}
