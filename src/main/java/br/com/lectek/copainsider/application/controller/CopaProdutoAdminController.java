package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaProdutoJPARepository;
import br.com.lectek.copainsider.application.copa.CopaProdutoAdminRequest;
import br.com.lectek.copainsider.application.copa.CopaProdutoVM;
import br.com.lectek.copainsider.application.service.HotmartSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Tag(name = "Admin — Copa Loja", description = "CRUD de produtos digitais da loja Copa Insider")
@RestController
@RequestMapping("/api/admin/copa-produtos")
public class CopaProdutoAdminController {

    private final CopaProdutoJPARepository repository;
    private final HotmartSyncService syncService;

    public CopaProdutoAdminController(CopaProdutoJPARepository repository,
                                      HotmartSyncService syncService) {
        this.repository = repository;
        this.syncService = syncService;
    }

    @Operation(summary = "Listar todos os produtos (incluindo inativos)")
    @GetMapping
    public List<CopaProdutoVM> listar() {
        return repository.findAll().stream().map(this::toVM).toList();
    }

    @Operation(summary = "Criar novo produto")
    @PostMapping
    public ResponseEntity<CopaProdutoVM> criar(@Valid @RequestBody CopaProdutoAdminRequest req) {
        if (repository.findBySlugAndAtivoTrue(req.slug()).isPresent()) {
            return ResponseEntity.status(409).build();
        }
        CopaProdutoEntity entity = fromRequest(new CopaProdutoEntity(), req);
        repository.save(entity);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(entity.getId()).toUri();
        return ResponseEntity.created(uri).body(toVM(entity));
    }

    @Operation(summary = "Atualizar produto (incluindo hotmart_url)")
    @PutMapping("/{id}")
    public ResponseEntity<CopaProdutoVM> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CopaProdutoAdminRequest req) {
        return repository.findById(id)
                .map(entity -> {
                    fromRequest(entity, req);
                    repository.save(entity);
                    return ResponseEntity.ok(toVM(entity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualizar somente a URL do Hotmart")
    @PatchMapping("/{id}/hotmart-url")
    public ResponseEntity<Void> atualizarHotmartUrl(
            @PathVariable Long id,
            @RequestParam String url) {
        return repository.findById(id)
                .map(entity -> {
                    entity.setHotmartUrl(url);
                    repository.save(entity);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Ativar / desativar produto")
    @PatchMapping("/{id}/ativo")
    public ResponseEntity<Void> toggleAtivo(
            @PathVariable Long id,
            @RequestParam boolean ativo) {
        return repository.findById(id)
                .map(entity -> {
                    entity.setAtivo(ativo);
                    repository.save(entity);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remover produto permanentemente")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Sincronizar produtos com Hotmart — importa novos e atualiza URLs existentes")
    @PostMapping("/sync-hotmart")
    public ResponseEntity<HotmartSyncService.SyncResult> syncHotmart() {
        HotmartSyncService.SyncResult result = syncService.sincronizar();
        return ResponseEntity.ok(result);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private CopaProdutoEntity fromRequest(CopaProdutoEntity e, CopaProdutoAdminRequest r) {
        e.setSlug(r.slug());
        e.setTipo(r.tipo());
        e.setPreco(r.preco());
        e.setNomePtBr(r.nomePtBr());
        e.setNomePtPt(r.nomePtPt());
        e.setNomeEn(r.nomeEn());
        e.setDescPtBr(r.descPtBr());
        e.setDescPtPt(r.descPtPt());
        e.setDescEn(r.descEn());
        e.setHotmartUrl(r.hotmartUrl());
        e.setImagemUrl(r.imagemUrl());
        e.setSlugTime1(r.slugTime1());
        e.setSlugTime2(r.slugTime2());
        e.setAtivo(r.ativo());
        e.setOrdem(r.ordem());
        return e;
    }

    private CopaProdutoVM toVM(CopaProdutoEntity p) {
        BigDecimal precoEur = p.getPreco().divide(BigDecimal.valueOf(6.20), 2, RoundingMode.HALF_UP);
        return new CopaProdutoVM(
                p.getSlug(), p.getTipo(), p.getPreco(), precoEur,
                p.getNomePtBr(), p.getDescPtBr(),
                p.getHotmartUrl(), p.getImagemUrl(),
                p.getSlugTime1(), p.getSlugTime2(),
                false, 1L, p.getSelecaoCode()
        );
    }
}
