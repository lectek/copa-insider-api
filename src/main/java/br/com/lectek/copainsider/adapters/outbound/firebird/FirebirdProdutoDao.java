/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.jdbc.core.JdbcTemplate
 *  org.springframework.jdbc.core.RowMapper
 *  org.springframework.stereotype.Repository
 */
package br.com.lectek.copainsider.adapters.outbound.firebird;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "legacy.sync", name = "enabled", havingValue = "true", matchIfMissing = false)
public class FirebirdProdutoDao {
    private final JdbcTemplate fb;
    private static final RowMapper<FbProduto> MAPPER = (rs, i) -> new FbProduto(rs.getInt("PRODUTO_ID"), rs.getString("PRODUTO"), rs.getString("APRESENTACAO"), rs.getString("COD_BARRAS"), rs.getBigDecimal("PROD_PRVENDA"), rs.getBigDecimal("PROD_PRPROMOCAO"), FirebirdProdutoDao.safeInt(rs, "PROD_SALDO"), FirebirdProdutoDao.safeTs(rs, "LASTUPDATE"));

    private static Integer safeInt(ResultSet rs, String c) throws SQLException {
        int v = rs.getInt(c);
        return rs.wasNull() ? null : Integer.valueOf(v);
    }

    private static LocalDateTime safeTs(ResultSet rs, String c) throws SQLException {
        Timestamp t = rs.getTimestamp(c);
        return t == null ? null : t.toLocalDateTime();
    }

    public FirebirdProdutoDao(JdbcTemplate firebirdJdbcTemplate) {
        this.fb = firebirdJdbcTemplate;
    }

    public List<FbProduto> fetchAtualizados(LocalDateTime since, int limit) {
        String sql = "    SELECT FIRST ?\n           p.PRODUTO_ID, p.PRODUTO, p.APRESENTACAO, p.COD_BARRAS,\n           p.PROD_PRVENDA, p.PROD_PRPROMOCAO, p.PROD_SALDO, p.LASTUPDATE\n    FROM   PRODUTOS p\n    WHERE  (p.LASTUPDATE IS NULL OR p.LASTUPDATE > ?)\n       AND p.PRODUTO IS NOT NULL\n       AND p.PROD_PRVENDA > 0\n    ORDER  BY COALESCE(p.LASTUPDATE, TIMESTAMP '1900-01-01') ASC\n";
        return this.fb.query(sql, MAPPER, new Object[]{limit, since});
    }

    public record FbProduto(Integer produtoId, String produto, String apresentacao, String codBarras, BigDecimal precoVenda, BigDecimal precoPromocao, Integer estoque, LocalDateTime lastUpdate) {
    }
}
