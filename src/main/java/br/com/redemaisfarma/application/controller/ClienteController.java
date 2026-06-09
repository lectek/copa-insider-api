package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.application.service.ClienteService;
import br.com.redemaisfarma.domain.Cliente;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/app/clientes", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "App - Clientes", description = "CRUD e busca de clientes (camada de aplicação)")
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(summary = "Criar cliente")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("hasAuthority('APP:CLIENTE:CRIAR') or hasRole('ADMIN')")
    public ResponseEntity<Cliente> create(@Valid @RequestBody Cliente cliente) {
        log.debug("Criando cliente...");
        Cliente salvo = clienteService.create(cliente);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(salvo.getId())
                .toUri();

        log.info("Cliente id={} criado.", salvo.getId());
        return ResponseEntity.created(location).body(salvo);
    }

    @Operation(summary = "Listar/buscar clientes (paginado). Filtros: q, cpf, telefone")
    @GetMapping
    // @PreAuthorize("hasAuthority('APP:CLIENTE:LER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Cliente>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String telefone,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        Page<Cliente> page = clienteService.search(q, cpf, telefone, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Buscar cliente por ID")
    @GetMapping("/{id}")
    // @PreAuthorize("hasAuthority('APP:CLIENTE:LER') or hasRole('ADMIN')")
    public ResponseEntity<Cliente> findById(@PathVariable("id") Long id) {
        Cliente cliente = clienteService.findById(id);
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Buscar cliente por CPF (atalho)")
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<Cliente> findByCpf(@PathVariable("cpf") String cpf) {
        Optional<Cliente> opt = clienteService.findByCpf(cpf);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Buscar cliente por telefone (atalho)")
    @GetMapping("/telefone/{telefone}")
    public ResponseEntity<Cliente> findByTelefone(@PathVariable("telefone") String telefone) {
        Optional<Cliente> opt = clienteService.findByTelefone(telefone);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Atualizar cliente")
    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    // @PreAuthorize("hasAuthority('APP:CLIENTE:ATUALIZAR') or hasRole('ADMIN')")
    public ResponseEntity<Cliente> update(@PathVariable("id") Long id, @Valid @RequestBody Cliente cliente) {
        cliente.setId(id); // path param é a fonte de verdade
        Cliente atualizado = clienteService.update(id, cliente);
        log.info("Cliente id={} atualizado.", id);
        return ResponseEntity.ok(atualizado);
    }

    @Operation(summary = "Excluir cliente")
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAuthority('APP:CLIENTE:EXCLUIR') or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        clienteService.delete(id);
        log.info("Cliente id={} removido.", id);
        return ResponseEntity.noContent().build();
    }
}
