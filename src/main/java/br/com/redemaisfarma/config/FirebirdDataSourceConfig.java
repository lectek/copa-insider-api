// src/main/java/br/com/redemaisfarma/config/FirebirdDataSourceConfig.java
package br.com.redemaisfarma.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.*;            // <- importa @Lazy
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@Conditional(FirebirdDataSourceConfig.FirebirdPropsPresent.class)
@ConditionalOnProperty(name = "legacy.sync.enabled", havingValue = "true", matchIfMissing = false)
@EnableJpaRepositories(
        basePackages = "br.com.redemaisfarma.adapters.outbound.legacy.repository",
        entityManagerFactoryRef = "firebirdEntityManagerFactory",
        transactionManagerRef = "firebirdTransactionManager"
)
public class FirebirdDataSourceConfig implements EnvironmentAware {

    private Environment env;
    @Override public void setEnvironment(@NonNull Environment environment) { this.env = environment; }

    @Bean(name = "firebirdDataSource")
    @Lazy // ← só cria/conecta quando for realmente usado
    public DataSource firebirdDataSource() {
        String jdbcUrl = firstNonBlank(
                env.getProperty("spring.datasource.firebird.jdbc-url"),
                env.getProperty("spring.datasource.firebird.url")
        );
        if (isBlank(jdbcUrl)) {
            throw new IllegalStateException("Firebird habilitado mas sem URL (spring.datasource.firebird.[jdbc-url|url]).");
        }

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(jdbcUrl);
        cfg.setUsername(env.getProperty("spring.datasource.firebird.username", "SYSDBA"));
        cfg.setPassword(env.getProperty("spring.datasource.firebird.password", "masterkey"));
        cfg.setDriverClassName(env.getProperty("spring.datasource.firebird.driver-class-name", "org.firebirdsql.jdbc.FBDriver"));

        // --- HARDENING: não falhar no startup se o .fdb não estiver acessível
        cfg.setInitializationFailTimeout(-1);   // não derruba a aplicação no start
        cfg.setValidationTimeout(5000);
        cfg.setMaximumPoolSize(Integer.parseInt(env.getProperty("spring.datasource.firebird.hikari.maximum-pool-size", "5")));
        cfg.setMinimumIdle(Integer.parseInt(env.getProperty("spring.datasource.firebird.hikari.minimum-idle", "0")));
        cfg.setIdleTimeout(Long.parseLong(env.getProperty("spring.datasource.firebird.hikari.idle-timeout", "60000")));
        cfg.setConnectionTimeout(Long.parseLong(env.getProperty("spring.datasource.firebird.hikari.connection-timeout", "10000")));

        return new HikariDataSource(cfg);
        // Opcional (mais lazy ainda):
        // return new LazyConnectionDataSourceProxy(new HikariDataSource(cfg));
    }

    @Bean(name = "firebirdEntityManagerFactory")
    @Lazy // ← adia criação/boot do EMF
    public LocalContainerEntityManagerFactoryBean firebirdEntityManagerFactory(
            @Qualifier("firebirdDataSource") DataSource dataSource) {

        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.dialect", "org.hibernate.community.dialect.FirebirdDialect");
        jpaProps.put("hibernate.show_sql", "false");
        jpaProps.put("hibernate.format_sql", "false");
        jpaProps.put("hibernate.jdbc.time_zone", "UTC");
        jpaProps.put("hibernate.boot.allow_jdbc_metadata_access", "false");
        // opcional (garantir que nada tente criar/alterar schema legado):
        // jpaProps.put("hibernate.hbm2ddl.auto", "none");

        var vendor = new HibernateJpaVendorAdapter();

        var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(vendor);
        emf.setJpaPropertyMap(jpaProps);
        emf.setPackagesToScan("br.com.redemaisfarma.adapters.outbound.legacy.entity");
        emf.setPersistenceUnitName("firebirdPU");
        return emf;
    }

    @Bean(name = "firebirdTransactionManager")
    @Lazy // ← só cria TM quando usar o EMF
    public PlatformTransactionManager firebirdTransactionManager(
            @Qualifier("firebirdEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name = "jdbcTemplateFirebird")
    @Lazy
    public JdbcTemplate jdbcTemplateFirebird(@Qualifier("firebirdDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }

    // --------- helpers & condição custom ----------
    static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    static String firstNonBlank(String... vals) { if (vals == null) return null; for (String v : vals) if (!isBlank(v)) return v; return null; }

    /** Condição custom para aceitar URL em 'url' OU 'jdbc-url'. */
    public static class FirebirdPropsPresent implements Condition {
        @Override public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
            Environment env = ctx.getEnvironment();
            if (!"true".equalsIgnoreCase(env.getProperty("legacy.sync.enabled", "false"))) return false;
            String url = env.getProperty("spring.datasource.firebird.url");
            String jdbc = env.getProperty("spring.datasource.firebird.jdbc-url");
            return !isBlank(url) || !isBlank(jdbc);
        }
    }
}
