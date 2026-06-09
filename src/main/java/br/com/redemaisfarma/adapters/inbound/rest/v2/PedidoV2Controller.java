package br.com.redemaisfarma.adapters.inbound.rest.v2;

import br.com.redemaisfarma.application.service.PedidoService;
import br.com.redemaisfarma.domain.Pedido;
import jakarta.validation.Valid;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController("pedidoV2Controller")
@RequestMapping(value = "/api/v2/pedidos", produces = "application/json")
@Validated
public class PedidoV2Controller {

    private final PedidoService pedidoService;

    @Generated
    public PedidoV2Controller(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // =========================================
    // Criar
    // =========================================
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Pedido> criar(@Valid @RequestBody Pedido pedido) {
        Pedido salvo = pedidoService.create(pedido);
        URI location = URI.create("/api/v2/pedidos/" + salvo.getId());
        return ResponseEntity.created(location).body(salvo);
    }

    // =========================================
    // Buscar por ID
    // =========================================
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.of(pedidoService.findByIdOptional(id));
    }

    // =========================================
    // Listar
    // =========================================
    @GetMapping
    public ResponseEntity<List<Pedido>> listar() {
        return ResponseEntity.ok(pedidoService.list());
    }

    // =========================================
    // Atualizar
    // =========================================
    @PutMapping(value = "/{id}", consumes = "application/json")
    public ResponseEntity<Pedido> atualizar(@PathVariable("id") Long id, @Valid @RequestBody Pedido pedido) {
        Pedido atualizado = pedidoService.update(id, pedido);
        return ResponseEntity.ok(atualizado);
    }

    // =========================================
    // Deletar
    // =========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") Long id) {
        pedidoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
