package br.com.lectek.copainsider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

@SpringBootApplication(scanBasePackages = {"br.com.lectek.copainsider"})
@ConfigurationPropertiesScan
@EntityScan(basePackages = {"br.com.lectek.copainsider"})
@EnableScheduling
public class CopaInsiderApplication {
    private static final Logger logger = LoggerFactory.getLogger(CopaInsiderApplication.class);

    public static void main(String[] args) {
        try {
            logger.info("Application starting... Copa Insider");
            SpringApplication app = new SpringApplication(CopaInsiderApplication.class);
            app.setBannerMode(Banner.Mode.OFF);

            boolean hasActiveProfile =
                    System.getProperty("spring.profiles.active") != null
                    || System.getenv("SPRING_PROFILES_ACTIVE") != null;

            if (!hasActiveProfile) {
                app.setDefaultProperties(Map.of("spring.profiles.default", "dev"));
            }

            ConfigurableEnvironment env = app.run(args).getEnvironment();
            String port = env.getProperty("server.port", "8080");
            String[] profiles = env.getActiveProfiles();
            String profileList = String.join(",", profiles);
            logger.info("Active profiles: {}", profileList.isEmpty() ? "dev (default)" : profileList);
            logger.info("Application started — http://localhost:{}", port);

            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> logger.info("Application shutting down CopaInsider safely..."))
            );
        } catch (Exception e) {
            logger.error("Error starting application Copa Insider:", e);
            System.exit(1);
        }
    }
}
