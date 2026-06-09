package br.com.lectek.copainsider.adapters.inbound.rest.v2;

import br.com.lectek.copainsider.application.service.ClienteService;
import br.com.lectek.copainsider.domain.Cliente;
import jakarta.validation.Valid;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController("clienteV2Controller")
@RequestMapping("/api/v2/clientes")
@Validated
public class ClienteV2Controller {

    private final ClienteService clienteService;

    @Generated
    public ClienteV2Controller(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // ==================================================
    // 1. Criar novo cliente
    // ==================================================
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<Cliente> criar(@Valid @RequestBody Cliente cliente) {
        Cliente salvo = clienteService.create(cliente);
        URI location = URI.create("/api/v2/clientes/" + salvo.getId());
        return ResponseEntity.created(location).body(salvo);
    }

    // ==================================================
    // 2. Buscar cliente por ID
    // ==================================================
    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable("id") Long id) {
        Cliente cliente = clienteService.findById(id);
        return ResponseEntity.ok(cliente);
    }

    // ==================================================
    // 3. Listar todos os clientes
    // ==================================================
    @GetMapping(produces = "application/json")
    public ResponseEntity<List<Cliente>> listar() {
        List<Cliente> clientes = clienteService.list();
        return ResponseEntity.ok(clientes);
    }

    // ==================================================
    // 4. Atualizar cliente existente
    // ==================================================
    @PutMapping(value = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Cliente> atualizar(@PathVariable("id") Long id, @Valid @RequestBody Cliente cliente) {
        Cliente atualizado = clienteService.update(id, cliente);
        return ResponseEntity.ok(atualizado);
    }

    // ==================================================
    // 5. Deletar cliente
    // ==================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable("id") Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
