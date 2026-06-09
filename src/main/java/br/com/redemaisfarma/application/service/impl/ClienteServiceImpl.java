package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.application.service.ClienteService;
import br.com.redemaisfarma.domain.Cliente;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Cliente findById(@NotNull Long id) {
        ClienteEntity entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cliente não encontrado: id=" + id));
        return toDomain(entity);
    }

    @Override
    public List<Cliente> list() {
        return repository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Cliente create(Cliente cliente) {
        if (cliente == null) throw new IllegalArgumentException("Payload de cliente é obrigatório.");

        String email = normalizeEmail(cliente.getEmail());
        String cpf   = onlyDigits(cliente.getCpf());

        if (email != null && repository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        if (cpf != null && repository.existsByCpf(cpf)) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        ClienteEntity e = toEntity(cliente);
        e.setId(null);
        e.setEmail(email);
        e.setCpf(cpf);

        if (hasText(cliente.getSenha())) {
            e.setSenha(passwordEncoder.encode(cliente.getSenha()));
        }

        ClienteEntity salvo = repository.save(e);
        return toDomain(salvo);
    }

    @Override
    @Transactional
    public Cliente update(@NotNull Long id, Cliente cliente) {
        if (cliente == null) throw new IllegalArgumentException("Payload de cliente é obrigatório.");

        ClienteEntity original = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cliente não encontrado: id=" + id));

        String novoEmail = normalizeEmail(cliente.getEmail());
        String novoCpf   = onlyDigits(cliente.getCpf());

        if (novoEmail != null && !novoEmail.equalsIgnoreCase(safe(original.getEmail()))) {
            repository.findByEmailIgnoreCase(novoEmail).ifPresent(existente -> {
                if (!existente.getId().equals(id)) throw new IllegalArgumentException("E-mail já cadastrado.");
            });
            original.setEmail(novoEmail);
        }

        if (novoCpf != null && !novoCpf.equals(safe(original.getCpf()))) {
            repository.findByCpf(novoCpf).ifPresent(existente -> {
                if (!existente.getId().equals(id)) throw new IllegalArgumentException("CPF já cadastrado.");
            });
            original.setCpf(novoCpf);
        }

        if (cliente.getNome() != null) original.setNome(cliente.getNome());
        if (cliente.getTelefone() != null) original.setTelefone(cliente.getTelefone());
        if (cliente.getDataDeNascimento() != null) original.setDataDeNascimento(cliente.getDataDeNascimento());
        original.setAtivo(cliente.isAtivo());

        if (hasText(cliente.getSenha()) && !passwordEncoder.matches(cliente.getSenha(), safe(original.getSenha()))) {
            original.setSenha(passwordEncoder.encode(cliente.getSenha()));
        }

        ClienteEntity salvo = repository.save(original);
        return toDomain(salvo);
    }

    @Override
    @Transactional
    public void delete(@NotNull Long id) {
        if (!repository.existsById(id)) throw new NoSuchElementException("Cliente não encontrado: id=" + id);
        repository.deleteById(id);
    }

    /* ======================== BUSCAS AVANÇADAS (compatíveis) ======================== */

    @Override
    public Page<Cliente> search(String q, String cpf, String telefone, Pageable pageable) {
        // Prioridade objetiva: CPF > email (q com '@') > telefone > nome (q)
        String cpfDigits = onlyDigits(cpf);
        if (cpfDigits != null) {
            return repository.findByCpf(cpfDigits)
                    .map(this::toDomain)
                    .map(dto -> singlePage(dto, pageable))
                    .orElse(Page.empty(pageable));
        }

        String qNorm = safeLower(q);
        if (qNorm != null && qNorm.contains("@")) { // tratar como e-mail
            return repository.findByEmailIgnoreCase(qNorm)
                    .map(this::toDomain)
                    .map(dto -> singlePage(dto, pageable))
                    .orElse(Page.empty(pageable));
        }

        String telDigits = onlyDigits(telefone);
        if (telDigits != null) {
            // Fallback em memória (repo não tem findByTelefone)
            List<ClienteEntity> all = repository.findAll();
            List<Cliente> filtrados = all.stream()
                    .filter(e -> {
                        String t = onlyDigits(e.getTelefone());
                        return t != null && t.contains(telDigits);
                    })
                    .map(this::toDomain)
                    .collect(Collectors.toList());
            return toPaged(filtrados, pageable);
        }

        if (qNorm != null && !qNorm.isBlank()) {
            // Primeiro tenta pelo "first by nome contains"
            Optional<ClienteEntity> first = repository.findFirstByNomeContainingIgnoreCase(qNorm);
            if (first.isPresent()) {
                // Para não retornar só 1, fazemos fallback em memória para trazer mais nomes/email contendo q
                List<ClienteEntity> all = repository.findAll();
                List<Cliente> filtrados = all.stream()
                        .filter(e -> containsIgnoreCase(e.getNome(), qNorm) || containsIgnoreCase(e.getEmail(), qNorm))
                        .map(this::toDomain)
                        .collect(Collectors.toList());
                return toPaged(filtrados, pageable);
            } else {
                return Page.empty(pageable);
            }
        }

        // Sem filtros: devolve tudo paginado (em memória)
        List<Cliente> todos = repository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
        return toPaged(todos, pageable);
    }

    @Override
    public Optional<Cliente> findByCpf(String cpf) {
        String digits = onlyDigits(cpf);
        return repository.findByCpf(digits).map(this::toDomain);
    }

    @Override
    public Optional<Cliente> findByTelefone(String telefone) {
        String digits = onlyDigits(telefone);
        if (digits == null) return Optional.empty();

        // Fallback em memória (repo não expõe por telefone)
        return repository.findAll().stream()
                .filter(e -> {
                    String t = onlyDigits(e.getTelefone());
                    return t != null && t.equals(digits);
                })
                .findFirst()
                .map(this::toDomain);
    }

    /* ======================== HELPERS ======================== */

    private Page<Cliente> singlePage(Cliente dto, Pageable pageable) {
        return new PageImpl<>(List.of(dto), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort()), 1);
    }

    private Page<Cliente> toPaged(List<Cliente> source, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), source.size());
        if (start > end) return new PageImpl<>(Collections.emptyList(), pageable, source.size());
        return new PageImpl<>(source.subList(start, end), pageable, source.size());
    }

    private boolean containsIgnoreCase(String value, String qLower) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(qLower);
        }

    private Cliente toDomain(ClienteEntity e) {
        if (e == null) return null;
        Cliente d = new Cliente();
        d.setId(e.getId());
        d.setNome(e.getNome());
        d.setEmail(e.getEmail());
        d.setTelefone(e.getTelefone());
        d.setCpf(e.getCpf());
        d.setSenha(e.getSenha());
        d.setDataDeNascimento(e.getDataDeNascimento());
        d.setAtivo(e.isAtivo());
        return d;
    }

    private ClienteEntity toEntity(Cliente d) {
        if (d == null) return null;
        ClienteEntity e = new ClienteEntity();
        e.setId(d.getId());
        e.setNome(d.getNome());
        e.setEmail(d.getEmail());
        e.setTelefone(d.getTelefone());
        e.setCpf(d.getCpf());
        e.setSenha(d.getSenha());
        e.setDataDeNascimento(d.getDataDeNascimento());
        e.setAtivo(d.isAtivo());
        return e;
    }

    private String normalizeEmail(String email) {
        if (!hasText(email)) return null;
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String onlyDigits(String v) {
        if (v == null) return null;
        String s = v.replaceAll("\\D", "");
        return s.isEmpty() ? null : s;
    }

    private boolean hasText(String v) { return v != null && !v.trim().isEmpty(); }

    private String safe(String v) { return v == null ? "" : v; }

    private String safeLower(String v) {
        return (v == null || v.isBlank()) ? null : v.trim().toLowerCase(Locale.ROOT);
    }
}
