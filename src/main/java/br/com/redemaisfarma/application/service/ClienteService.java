package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.domain.Cliente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

/**
 * Porta da aplicação para operações de Cliente.
 * Implementações devem lançar exceção de "não encontrado" quando aplicável
 * (ex.: IllegalArgumentException, EntityNotFoundException ou custom).
 */
@Validated
public interface ClienteService {

    // ---- CRUD básico ----
    Cliente findById(@NotNull Long id);

    List<Cliente> list();

    Cliente create(@Valid @NotNull Cliente cliente);

    Cliente update(@NotNull Long id, @Valid @NotNull Cliente cliente);

    void delete(@NotNull Long id);

    // ---- Busca avançada ----
    /**
     * Busca paginada por clientes.
     * @param q        termo livre (nome/email) – opcional
     * @param cpf      CPF (somente dígitos ou formatado) – opcional
     * @param telefone Telefone (somente dígitos ou formatado) – opcional
     */
    Page<Cliente> search(@Nullable String q,
                         @Nullable String cpf,
                         @Nullable String telefone,
                         @ParameterObject Pageable pageable);

    /** Atalho para busca por CPF. */
    Optional<Cliente> findByCpf(@NotNull String cpf);

    /** Atalho para busca por telefone. */
    Optional<Cliente> findByTelefone(@NotNull String telefone);

    // ---- nomes legados (mantidos por compatibilidade) ----
    @Deprecated
    default Cliente buscarPorId(Long id) { return this.findById(id); }

    @Deprecated
    default List<Cliente> listarTodos() { return this.list(); }

    @Deprecated
    default Cliente salvar(Cliente cliente) { return this.create(cliente); }

    @Deprecated
    default Cliente atualizar(Long id, Cliente cliente) { return this.update(id, cliente); }

    @Deprecated
    default void deletar(Long id) { this.delete(id); }
}

