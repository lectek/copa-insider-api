package br.com.lectek.copainsider.adapters.outbound.legacy.adapter;

import br.com.lectek.copainsider.adapters.outbound.legacy.dto.LegacyProdutoDTO;
import br.com.lectek.copainsider.adapters.outbound.legacy.port.LegacyProdutoPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "legacy.sync.enabled", havingValue = "true")
public class LegacyProdutoJdbcAdapter implements LegacyProdutoPort {
    private final JdbcTemplate jdbc;
    private final String configuredLastUpdateColumn;

    public LegacyProdutoJdbcAdapter(@Qualifier("jdbcTemplateFirebird") JdbcTemplate jdbcTemplateFirebird,
                                    @Value("${legacy.produtos.last-update-column:}") String configuredLastUpdateColumn) {
        this.jdbc = jdbcTemplateFirebird;
        this.configuredLastUpdateColumn = configuredLastUpdateColumn;
    }

    @Override
    public List<LegacyProdutoDTO> fetchChangedSince(LocalDateTime since, int page, int size) {
        String lastCol = this.resolveLastUpdateColumn();
        int offset = Math.max(0, page) * Math.max(1, size);
        String sql = """
                SELECT FIRST %d SKIP %d
                       ID_PRODUTO,
                       PRODUTO,
                       APRESENTACAO,
                       COD_BARRAS,
                       PROD_PRVENDA,
                       PROD_PRPROMOCAO,
                       PROD_SALDO,
                       %s AS LAST_UPDATE
                FROM PRODUTOS
                WHERE %s >= ?
                ORDER BY %s
                """.formatted(size, offset, lastCol, lastCol, lastCol);

        return this.jdbc.query(
                sql,
                ps -> ps.setTimestamp(1, Timestamp.valueOf(since)),
                new LegacyProdutoMapper()
        );
    }

    private String resolveLastUpdateColumn() {
        List<String> candidates = (this.configuredLastUpdateColumn != null && !this.configuredLastUpdateColumn.isBlank())
                ? List.of(this.configuredLastUpdateColumn)
                : List.of("ULTIMA_ATUALIZACAO", "DT_ALTERACAO", "ULT_ATUALIZACAO", "UPDATED_AT");

        for (String col : candidates) {
            try {
                this.jdbc.query("SELECT " + col + " FROM PRODUTOS WHERE 1=0", rs -> {});
                return col;
            } catch (BadSqlGrammarException ignored) {
            }
        }
        throw new IllegalStateException("Não encontrei coluna de 'última atualização' na tabela PRODUTOS. Configure-a em legacy.produtos.last-update-column");
    }

    private static class LegacyProdutoMapper implements RowMapper<LegacyProdutoDTO> {
        @Override
        public LegacyProdutoDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            Timestamp ts = rs.getTimestamp("LAST_UPDATE");
            LocalDateTime lastUpdate = ts != null ? ts.toLocalDateTime() : null;
            return new LegacyProdutoDTO(
                    rs.getLong("ID_PRODUTO"),
                    rs.getString("PRODUTO"),
                    rs.getString("APRESENTACAO"),
                    rs.getString("COD_BARRAS"),
                    rs.getBigDecimal("PROD_PRVENDA"),
                    rs.getBigDecimal("PROD_PRPROMOCAO"),
                    rs.getInt("PROD_SALDO"),
                    lastUpdate
            );
        }
    }
}
