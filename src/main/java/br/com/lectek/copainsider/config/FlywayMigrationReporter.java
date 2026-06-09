package br.com.lectek.copainsider.config;

import java.util.Arrays;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(Flyway.class)
public class FlywayMigrationReporter {
    private static final Logger log = LoggerFactory.getLogger(FlywayMigrationReporter.class);

    private final Flyway flyway;
    private final Environment environment;

    public FlywayMigrationReporter(Flyway flyway, Environment environment) {
        this.flyway = flyway;
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void report() {
        var info = flyway.info();
        MigrationInfo current = info.current();
        int applied = info.applied().length;
        int pending = info.pending().length;
        String version = current != null && current.getVersion() != null ? current.getVersion().toString() : "<unknown>";
        boolean prod = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("prod"));
        if (!prod && pending == 0) {
            log.debug("Flyway initialized. applied={}, pending={}, currentVersion={}", applied, pending, version);
            return;
        }
        log.info("Flyway initialized. applied={}, pending={}, currentVersion={}", applied, pending, version);
    }
}
