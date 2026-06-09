package br.com.lectek.copainsider.application.service.impl;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.application.dto.response.RelatorioClienteLinhaDTO;
import br.com.lectek.copainsider.application.service.RelatorioClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioClienteServiceImpl implements RelatorioClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    public Page<RelatorioClienteLinhaDTO> listar(String q, String cpf, String telefone, Pageable pageable) {
        String qLower   = normalize(q);
        String cpfDigits = onlyDigits(cpf);
        String telDigits = onlyDigits(telefone);

        List<ClienteEntity> base = clienteRepository.findAll();

        List<RelatorioClienteLinhaDTO> filtrados = base.stream()
                .filter(c -> qLower == null
                        || contains(c.getNome(), qLower)
                        || contains(c.getEmail(), qLower))
                .filter(c -> cpfDigits == null || Objects.equals(onlyDigits(c.getCpf()), cpfDigits))
                .filter(c -> telDigits == null || Optional.ofNullable(onlyDigits(c.getTelefone()))
                        .map(t -> t.contains(telDigits)).orElse(false))
                .map(this::toLinha)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), filtrados.size());
        List<RelatorioClienteLinhaDTO> pageContent = (start > end) ? List.of() : filtrados.subList(start, end);

        return new PageImpl<>(pageContent, pageable, filtrados.size());
    }

    private RelatorioClienteLinhaDTO toLinha(ClienteEntity c) {
        // Por enquanto, qtdPedidos/valorTotal como 0 (sem agregação de pedidos)
        return new RelatorioClienteLinhaDTO(
                c.getId(),
                c.getNome(),
                c.getCpf(),
                c.getEmail(),
                c.getTelefone(),
                0L,
                BigDecimal.ZERO
        );
    }

    private static String normalize(String s) {
        return (s == null || s.isBlank()) ? null : s.trim().toLowerCase(Locale.ROOT);
    }
    private static String onlyDigits(String s) {
        if (s == null) return null;
        String d = s.replaceAll("\\D", "");
        return d.isEmpty() ? null : d;
    }
    private static boolean contains(String value, String qLower) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(qLower);
    }
}
