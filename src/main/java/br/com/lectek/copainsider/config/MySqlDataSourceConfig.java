package br.com.lectek.copainsider.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Primary
@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "br.com.lectek.copainsider.adapters.outbound.persistence",
                "br.com.lectek.copainsider.user.audit",
                "br.com.lectek.copainsider.domain.financeiro.config",
                "br.com.lectek.copainsider.domain.financeiro.mercadopago",
                "br.com.lectek.copainsider.domain.user"
        },
        entityManagerFactoryRef = "mysqlEntityManagerFactory",
        transactionManagerRef = "mysqlTransactionManager"
)
public class MySqlDataSourceConfig {

    private final GitProperties gitProperties;
    private final Environment environment;
    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlDataSourceConfig.class);
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");
    private static final Pattern INVISIBLE_CHAR_PATTERN = Pattern.compile("[\\uFEFF\\u00A0\\u200B\\u200C\\u200D\\u202F\\u2060]");
    private static final Pattern COMBINING_MARKS_PATTERN = Pattern.compile("\\p{M}+");
    private static final String EXPECTED_FORMAT_MESSAGE = "Formato esperado: mysql://usuario:senha@host:porta/dbname (uma linha, sem espacos e terminando com /dbname).";
    private static final Map<String, String> DATABASE_NAME_OVERRIDES = Map.of();

    public MySqlDataSourceConfig(@Autowired(required = false) GitProperties gitProperties,
                                 Environment environment) {
        this.gitProperties = gitProperties;
        this.environment = environment;
    }

    @Bean(name = {"dataSource", "mysqlDataSource"})
    @Primary
    public DataSource mysqlDataSource() {
        logStartupDiagnostics();
        ResolvedDatabaseUrl resolved = resolveDatabaseUrl();
        String sanitizedUrl = sanitizeUrl(resolved.rawValue());
        String maskedUrl = maskCredentials(sanitizedUrl);

        ParsedUrl parsed;
        try {
            parsed = parseDatabaseUrl(sanitizedUrl);
        } catch (RuntimeException ex) {
            logParseFailure(resolved, maskedUrl, ex);
            throw ex;
        }

        validateExpectedDatabase(parsed.database());
        logResolvedConfiguration(parsed, resolved.envKey());

        String resolvedUsername = parsed.username();
        if (isBlank(resolvedUsername)) {
            resolvedUsername = trimEnv("SPRING_DATASOURCE_USERNAME");
        }

        String resolvedPassword = parsed.password();
        if (resolvedPassword == null) {
            resolvedPassword = trimEnv("SPRING_DATASOURCE_PASSWORD");
        }

        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(parsed.jdbcUrl());
        cfg.setUsername(resolvedUsername);
        cfg.setPassword(resolvedPassword);
        cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Startup-friendly defaults for Railway.
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(8000);
        cfg.setValidationTimeout(3000);
        cfg.setInitializationFailTimeout(0);
        cfg.setKeepaliveTime(30000);
        cfg.setIdleTimeout(60000);
        cfg.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(cfg);
    }

    @Bean(name = {"mysqlEntityManagerFactory", "entityManagerFactory"})
    @Primary
    public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
            @Qualifier("dataSource") DataSource dataSource) {

        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        vendor.setGenerateDdl(false);

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(vendor);
        emf.setJpaPropertyMap(jpaProps());
        emf.setPackagesToScan("br.com.lectek.copainsider");
        emf.setPersistenceUnitName("mysqlPU");
        return emf;
    }

    @Bean(name = {"mysqlTransactionManager", "transactionManager"})
    @Primary
    public PlatformTransactionManager mysqlTransactionManager(
            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean(name = "jpaSharedEM_mysqlEntityManagerFactory")
    @Primary
    public EntityManager mysqlSharedEntityManager(
            @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory emf) {
        return SharedEntityManagerCreator.createSharedEntityManager(emf);
    }

    @Bean(name = "flyway")
    @Primary
    public Flyway flyway(@Qualifier("dataSource") DataSource dataSource) {
        boolean outOfOrder = Boolean.parseBoolean(
                System.getenv().getOrDefault("FLYWAY_OUT_OF_ORDER", "true"));
        LOGGER.info("Flyway config outOfOrder={}", outOfOrder);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration", "classpath:db/migration-mysql")
                .baselineOnMigrate(true)
                .outOfOrder(outOfOrder)
                .load();
        flyway.migrate();
        return flyway;
    }

    private Map<String, Object> jpaProps() {
        Map<String, Object> p = new HashMap<>();
        p.put("hibernate.hbm2ddl.auto", "none");
        p.put("hibernate.show_sql", "false");
        p.put("hibernate.format_sql", "true");
        p.put("hibernate.jdbc.time_zone", "UTC");
        return p;
    }

    private ResolvedDatabaseUrl resolveDatabaseUrl() {
        String springDatasourceUrl = trimEnv("SPRING_DATASOURCE_URL");
        if (!isBlank(springDatasourceUrl)) {
            return new ResolvedDatabaseUrl("SPRING_DATASOURCE_URL", springDatasourceUrl);
        }

        String mysqlUrl = trimEnv("MYSQL_URL");
        if (!isBlank(mysqlUrl)) {
            return new ResolvedDatabaseUrl("MYSQL_URL", mysqlUrl);
        }

        throw new IllegalStateException(
                "Nenhuma URL de banco foi definida. Defina SPRING_DATASOURCE_URL com a string de conexao MySQL.");
    }

    private boolean isProdProfileActive() {
        if (environment != null) {
            for (String profile : environment.getActiveProfiles()) {
                if ("prod".equalsIgnoreCase(profile)) {
                    return true;
                }
            }
        }
        return containsProdProfile(System.getenv("SPRING_PROFILES_ACTIVE"))
                || containsProdProfile(System.getProperty("spring.profiles.active"));
    }

    private void validateExpectedDatabase(String actualDatabase) {
        String expectedDatabase = normalizeDatabaseName(System.getenv("MYSQL_DATABASE"));
        if (!isBlank(expectedDatabase) && !expectedDatabase.equals(actualDatabase)) {
            throw new IllegalStateException("MYSQL_DATABASE diverge do nome do banco na URL resolvida.");
        }
    }

    private ParsedUrl parseDatabaseUrl(String rawUrl) {
        if (isBlank(rawUrl)) {
            throw new IllegalStateException("A URL do MySQL esta vazia. " + EXPECTED_FORMAT_MESSAGE);
        }

        String normalized = rawUrl.trim();
        if (hasWhitespace(normalized)) {
            throw new IllegalStateException("A URL do MySQL contem espacos ou quebras de linha. " + EXPECTED_FORMAT_MESSAGE);
        }

        if (normalized.startsWith("jdbc:")) {
            normalized = normalized.substring("jdbc:".length());
        }

        if (!normalized.startsWith("mysql://")) {
            throw new IllegalStateException("A URL do MySQL deve começar com mysql://. " + EXPECTED_FORMAT_MESSAGE);
        }

        ParsedUrl parsed = tryParseWithUri(normalized);
        if (parsed != null) {
            return parsed;
        }

        return parseManually(normalized);
    }

    private ParsedUrl tryParseWithUri(String rawUrl) {
        try {
            URI uri = URI.create(rawUrl);
            String host = uri.getHost();
            String path = uri.getPath();
            String db = (path != null && path.startsWith("/")) ? path.substring(1) : path;
            db = decodeComponent(db);
            if (isBlank(host) || isBlank(db)) {
                return null;
            }
            if (db.contains("/")) {
                throw new IllegalStateException("A URL do MySQL deve terminar com /dbname. " + EXPECTED_FORMAT_MESSAGE);
            }
            String normalizedDb = normalizeDatabaseName(db);
            if (isBlank(normalizedDb)) {
                return null;
            }

            int port = uri.getPort() > 0 ? uri.getPort() : 3306;
            String userInfo = uri.getUserInfo();
            String username = null;
            String password = null;
            if (!isBlank(userInfo)) {
                String[] parts = userInfo.split(":", 2);
                username = decodeComponent(parts[0]);
                if (parts.length > 1) {
                    password = decodeComponent(parts[1]);
                }
            }

            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + normalizedDb
                    + "?sslMode=REQUIRED&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            return new ParsedUrl(jdbcUrl, username, password, normalizedDb, host, port);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private ParsedUrl parseManually(String normalizedUrl) {
        String withoutScheme = normalizedUrl.substring("mysql://".length());
        int atIndex = withoutScheme.lastIndexOf('@');
        if (atIndex <= 0 || atIndex == withoutScheme.length() - 1) {
            throw new IllegalStateException("MYSQL_URL invalida (host e caminho devem estar presentes). " + EXPECTED_FORMAT_MESSAGE);
        }

        String userInfo = withoutScheme.substring(0, atIndex);
        String hostPart = withoutScheme.substring(atIndex + 1);

        int slashIndex = hostPart.indexOf('/');
        if (slashIndex <= 0 || slashIndex == hostPart.length() - 1) {
            throw new IllegalStateException("MYSQL_URL invalida (host e caminho devem estar presentes). " + EXPECTED_FORMAT_MESSAGE);
        }

        String hostPort = hostPart.substring(0, slashIndex);
        String dbPart = hostPart.substring(slashIndex + 1);
        int queryIndex = dbPart.indexOf('?');
        String dbSegment = queryIndex >= 0 ? dbPart.substring(0, queryIndex) : dbPart;
        String db = decodeComponent(dbSegment);

        String host;
        int port = 3306;
        int portIdx = hostPort.lastIndexOf(':');
        if (portIdx > 0 && portIdx < hostPort.length() - 1) {
            host = hostPort.substring(0, portIdx);
            try {
                port = Integer.parseInt(hostPort.substring(portIdx + 1));
            } catch (NumberFormatException ignored) {
                port = 3306;
            }
        } else {
            host = hostPort;
        }

        String username = null;
        String password = null;
        int userSep = userInfo.indexOf(':');
        if (userSep >= 0) {
            username = decodeComponent(userInfo.substring(0, userSep));
            password = decodeComponent(userInfo.substring(userSep + 1));
        } else if (!isBlank(userInfo)) {
            username = decodeComponent(userInfo);
        }

        if (isBlank(host) || isBlank(db)) {
            throw new IllegalStateException("MYSQL_URL invalida (host e caminho devem estar presentes). " + EXPECTED_FORMAT_MESSAGE);
        }

        if (db.contains("/")) {
            throw new IllegalStateException("A URL do MySQL deve terminar com /dbname. " + EXPECTED_FORMAT_MESSAGE);
        }

        String normalizedDb = normalizeDatabaseName(db);
        if (isBlank(normalizedDb)) {
            throw new IllegalStateException("MYSQL_URL invalida (nome do banco invalido). " + EXPECTED_FORMAT_MESSAGE);
        }

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + normalizedDb
                + "?sslMode=REQUIRED&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return new ParsedUrl(jdbcUrl, username, password, normalizedDb, host, port);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
    
    private static boolean hasWhitespace(String value) {
        return value != null && WHITESPACE_PATTERN.matcher(value).find();
    }

    private static boolean containsProdProfile(String rawProfiles) {
        if (isBlank(rawProfiles)) {
            return false;
        }
        for (String candidate : rawProfiles.split(",")) {
            if ("prod".equalsIgnoreCase(candidate.trim())) {
                return true;
            }
        }
        return false;
    }

    private static void logResolvedConfiguration(ParsedUrl parsed, String envKey) {
        LOGGER.info("MySQL config resolved source={} host={} port={} db={}",
                envKey != null ? envKey : "<unknown>",
                parsed.host(),
                parsed.port(),
                parsed.database());
    }

    private void logParseFailure(ResolvedDatabaseUrl resolved, String maskedUrl, RuntimeException ex) {
        String envKey = resolved != null ? resolved.envKey() : "<unknown>";
        LOGGER.error("Falha ao interpretar URL do MySQL (env={}, url={}, cause={}): {}",
                envKey,
                maskedUrl,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex);
    }

    private void logStartupDiagnostics() {
        LOGGER.info("Env presence prodProfile={}, SPRING_DATASOURCE_URL={}, MYSQL_URL={}",
                isProdProfileActive(),
                hasEnvValue("SPRING_DATASOURCE_URL"),
                hasEnvValue("MYSQL_URL"));
        String commit = null;
        if (gitProperties != null) {
            commit = gitProperties.getShortCommitId();
            if (isBlank(commit)) {
                commit = gitProperties.getCommitId();
            }
        }
        LOGGER.info("Build commit={}", commit != null ? commit : "<unknown>");
    }

    private static String trimEnv(String key) {
        String value = System.getenv(key);
        return value != null ? value.trim() : null;
    }

    private static boolean hasEnvValue(String key) {
        return !isBlank(trimEnv(key));
    }

    private static String sanitizeUrl(String rawUrl) {
        if (rawUrl == null) {
            return null;
        }
        return stripInvisibleAndQuotes(rawUrl.trim());
    }

    private static String sanitizeComponent(String component) {
        if (component == null) {
            return null;
        }
        return stripInvisibleAndQuotes(component.trim());
    }

    private static String stripInvisibleAndQuotes(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = INVISIBLE_CHAR_PATTERN.matcher(value).replaceAll("");
        return cleaned.replace("\"", "").replace("'", "");
    }

    private static String normalizeDatabaseName(String rawDb) {
        String sanitized = sanitizeComponent(rawDb);
        if (isBlank(sanitized)) {
            return null;
        }
        String normalized = Normalizer.normalize(sanitized, Normalizer.Form.NFKD);
        normalized = COMBINING_MARKS_PATTERN.matcher(normalized).replaceAll("");
        normalized = normalized.toLowerCase(Locale.ROOT);
        return DATABASE_NAME_OVERRIDES.getOrDefault(normalized, normalized);
    }

    private static String maskCredentials(String sanitizedUrl) {
        if (isBlank(sanitizedUrl)) {
            return "<masked>";
        }
        int schemeEnd = sanitizedUrl.indexOf("://");
        if (schemeEnd < 0) {
            return sanitizedUrl;
        }
        int atIndex = sanitizedUrl.indexOf('@', schemeEnd + 3);
        if (atIndex < 0) {
            return sanitizedUrl;
        }

        String prefix = sanitizedUrl.substring(0, schemeEnd + 3);
        String credentialSegment = sanitizedUrl.substring(schemeEnd + 3, atIndex);
        String hostSuffix = sanitizedUrl.substring(atIndex);
        int colonIndex = credentialSegment.indexOf(':');
        String maskedCredentials;
        if (colonIndex >= 0) {
            String username = credentialSegment.substring(0, colonIndex);
            maskedCredentials = username + ":***";
        } else if (!credentialSegment.isEmpty()) {
            maskedCredentials = credentialSegment + ":***";
        } else {
            maskedCredentials = "***";
        }

        return prefix + maskedCredentials + hostSuffix;
    }

    private static String decodeComponent(String component) {
        if (isBlank(component)) {
            return null;
        }
        try {
            return URLDecoder.decode(component, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return component;
        }
    }
    private record ParsedUrl(String jdbcUrl, String username, String password, String database, String host, int port) {}
    private record ResolvedDatabaseUrl(String envKey, String rawValue) {}
}
