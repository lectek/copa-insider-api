package br.com.lectek.copainsider.domain.service.impl;

import br.com.lectek.copainsider.domain.service.EstoqueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
public class EstoqueServiceImpl implements EstoqueService {
    private static final Logger log = LoggerFactory.getLogger(EstoqueServiceImpl.class);
    private final JdbcTemplate jdbc;

    public EstoqueServiceImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean temDisponivel(Long produtoId, int quantidade) {
        if (produtoId == null) {
            return false;
        }
        if (quantidade <= 0) {
            return true;
        }
        Integer saldo = jdbc.query(
                "SELECT estoque FROM produto WHERE id = ?",
                ps -> ps.setLong(1, produtoId),
                rs -> rs.next() ? rs.getInt(1) : null
        );
        boolean ok = saldo != null && saldo >= quantidade;
        if (log.isDebugEnabled()) {
            log.debug("Estoque.temDisponivel produtoId={} solicitado={}, saldo={}, ok={}",
                    produtoId, quantidade, saldo, ok);
        }
        return ok;
    }

    @Override
    @Transactional
    public void baixar(Long produtoId, int quantidade, String motivo) {
        if (produtoId == null) {
            throw new IllegalArgumentException("Produto invalido para baixa de estoque.");
        }
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
        }
        int updated = jdbc.update(
                "UPDATE produto SET estoque = estoque - ? " +
                        "WHERE id = ? AND estoque >= ?",
                quantidade, produtoId, quantidade
        );
        if (updated == 0) {
            throw new IllegalStateException("Estoque insuficiente para produto " + produtoId + " (qtd=" + quantidade + ")");
        }
        registrarMovimento(produtoId, quantidade, "SAIDA", motivo);
        if (log.isDebugEnabled()) {
            log.debug("Estoque.baixar OK produtoId={} quantidade={} motivo={}", produtoId, quantidade, motivo);
        }
    }

    @Transactional
    public void ajustarSaldo(Long produtoId, int delta) {
        if (produtoId == null) {
            throw new IllegalArgumentException("Produto invalido para ajuste de estoque.");
        }
        if (delta == 0) return;

        String tipo = delta < 0 ? "SAIDA" : "ENTRADA";
        int abs = Math.abs(delta);

        int updated;
        if (delta < 0) {
            updated = jdbc.update(
                    "UPDATE produto SET estoque = estoque - ? WHERE id = ? AND estoque >= ?",
                    abs, produtoId, abs
            );
        } else {
            updated = jdbc.update(
                    "UPDATE produto SET estoque = estoque + ? WHERE id = ?",
                    abs, produtoId
            );
        }

        if (updated == 0) {
            throw new IllegalStateException("Ajuste de saldo falhou para produto " + produtoId + " (delta=" + delta + ")");
        }

        registrarMovimento(produtoId, abs, tipo, "AJUSTE");

        if (log.isDebugEnabled()) {
            log.debug("Estoque.ajustarSaldo OK produtoId={} delta={}", produtoId, delta);
        }
    }

    private void registrarMovimento(Long produtoId, int quantidade, String tipo, String motivo) {
        try {
            jdbc.update(
                    "INSERT INTO movimento_estoque (produto_id, quantidade, tipo, motivo) VALUES (?, ?, ?, ?)",
                    produtoId, quantidade, tipo, motivo
            );
        } catch (DataAccessException exWithMotivo) {
            try {
                jdbc.update(
                        "INSERT INTO movimento_estoque (produto_id, quantidade, tipo) VALUES (?, ?, ?)",
                        produtoId, quantidade, tipo
                );
            } catch (DataAccessException exWithoutMotivo) {
                log.error(
                        "Falha ao registrar movimento_estoque produtoId={} quantidade={} tipo={}. " +
                                "A venda foi mantida, mas sem trilha de movimento.",
                        produtoId, quantidade, tipo, exWithoutMotivo
                );
            }
        }
    }
}
