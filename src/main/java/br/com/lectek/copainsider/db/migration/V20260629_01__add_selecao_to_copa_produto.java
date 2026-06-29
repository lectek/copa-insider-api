package br.com.lectek.copainsider.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class V20260629_01__add_selecao_to_copa_produto extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement st = context.getConnection().createStatement()) {
            addColumnIfMissing(st, "selecao_code",   "VARCHAR(10) NULL AFTER slug_time2");
            addColumnIfMissing(st, "idioma_default", "VARCHAR(10) NULL AFTER selecao_code");
        }
    }

    private void addColumnIfMissing(Statement st, String column, String definition) throws Exception {
        String check = "SELECT COUNT(*) FROM information_schema.columns " +
                       "WHERE table_schema = DATABASE() AND table_name = 'copa_produto' AND column_name = '" + column + "'";
        try (ResultSet rs = st.executeQuery(check)) {
            if (rs.next() && rs.getInt(1) == 0) {
                st.execute("ALTER TABLE copa_produto ADD COLUMN " + column + " " + definition);
            }
        }
    }
}
