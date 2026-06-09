package br.com.redemaisfarma.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class FlywayMigrationReporterTest {

    @Mock
    private Flyway flyway;

    @Mock
    private Environment environment;

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUpLogger() {
        logger = (Logger) LoggerFactory.getLogger(FlywayMigrationReporter.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDownLogger() {
        logger.detachAppender(listAppender);
        listAppender.stop();
    }

    @Test
    void shouldLogDebugWhenNotProdAndNoPendingMigrations() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {"dev"});
        MigrationInfoService info = buildInfo(1, 0, "20260111");
        when(flyway.info()).thenReturn(info);

        FlywayMigrationReporter reporter = new FlywayMigrationReporter(flyway, environment);
        reporter.report();

        assertOnlyEvent(Level.DEBUG, "Flyway initialized. applied=1, pending=0, currentVersion=20260111");
    }

    @Test
    void shouldLogInfoWhenProdEvenWithoutPending() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {"prod"});
        MigrationInfoService info = buildInfo(3, 0, "20260112");
        when(flyway.info()).thenReturn(info);

        FlywayMigrationReporter reporter = new FlywayMigrationReporter(flyway, environment);
        reporter.report();

        assertOnlyEvent(Level.INFO, "Flyway initialized. applied=3, pending=0, currentVersion=20260112");
    }

    @Test
    void shouldLogInfoWhenPendingMigrationsExist() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {"qa"});
        MigrationInfoService info = buildInfo(2, 2, "20260110");
        when(flyway.info()).thenReturn(info);

        FlywayMigrationReporter reporter = new FlywayMigrationReporter(flyway, environment);
        reporter.report();

        assertOnlyEvent(Level.INFO, "Flyway initialized. applied=2, pending=2, currentVersion=20260110");
    }

    private MigrationInfoService buildInfo(int applied, int pending, String version) {
        MigrationInfoService info = org.mockito.Mockito.mock(MigrationInfoService.class);
        MigrationInfo current = org.mockito.Mockito.mock(MigrationInfo.class);
        when(current.getVersion()).thenReturn(MigrationVersion.fromVersion(version));
        when(info.current()).thenReturn(current);
        when(info.applied()).thenReturn(new MigrationInfo[applied]);
        when(info.pending()).thenReturn(new MigrationInfo[pending]);
        return info;
    }

    private void assertOnlyEvent(Level level, String message) {
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent event = listAppender.list.get(0);
        assertThat(event.getLevel()).isEqualTo(level);
        assertThat(event.getFormattedMessage()).isEqualTo(message);
    }
}
