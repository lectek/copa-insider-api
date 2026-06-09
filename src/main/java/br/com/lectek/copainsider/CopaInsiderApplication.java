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

import java.util.HashMap;
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
            boolean isRailway = isRunningOnRailway();

            Map<String, Object> defaults = new HashMap<>();
            if (!hasActiveProfile) {
                if (isRailway) {
                    defaults.put("spring.profiles.active", "prod");
                    app.setDefaultProperties(defaults);
                    logger.info("Railway environment detected; forcing prod profile.");
                } else {
                    defaults.put("spring.profiles.default", "dev");
                    app.setDefaultProperties(defaults);
                }
            } else if (isRailway) {
                logger.info("Railway environment detected; relying on SPRING_PROFILES_ACTIVE for profile selection.");
            }

            ConfigurableEnvironment env = app.run(args).getEnvironment();
            String port = env.getProperty("server.port", "8080");
            String[] profiles = env.getActiveProfiles();
            String profileList = String.join(",", profiles);
            logger.info("Active profiles: {}", profileList);
            if (profileList.isEmpty()) {
                logger.info("Profile source: {}", describeProfileSource(hasActiveProfile, isRailway));
            }

            logger.info("Application started.");
            logger.info("Base URL: http://localhost:{} (profiles: {})", port, profileList);
            logger.info("DataSource configuration initialized.");

            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> logger.info("Application shutting down CopaInsider safely..."))
            );
        } catch (Exception e) {
            logger.error("Error starting application Copa Insider:", e);
            System.exit(1);
        }
    }

    private static final String[] RAILWAY_INDICATOR_ENVS = {
            "RAILWAY_ENVIRONMENT_NAME",
            "RAILWAY_ENVIRONMENT",
            "RAILWAY_PROJECT_NAME",
            "RAILWAY_PROJECT_ID",
            "RAILWAY_SERVICE_NAME",
            "RAILWAY_SERVICE_ID",
            "RAILWAY_STATIC_URL",
            "RAILWAY_REGION",
            "RAILWAY_MYSQL_URL",
            "DATABASE_URL"
    };

    private static boolean isRunningOnRailway() {
        for (String env : RAILWAY_INDICATOR_ENVS) {
            if (isEnvPresent(env)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isEnvPresent(String envKey) {
        String value = System.getenv(envKey);
        return value != null && !value.trim().isEmpty();
    }

    private static String describeProfileSource(boolean hasActiveProfile, boolean isRailway) {
        String envValue = System.getenv("SPRING_PROFILES_ACTIVE");
        if (!isBlank(envValue)) {
            return "SPRING_PROFILES_ACTIVE";
        }
        String propValue = System.getProperty("spring.profiles.active");
        if (!isBlank(propValue)) {
            return "spring.profiles.active";
        }
        if (hasActiveProfile) {
            return "existing active profile";
        }
        return isRailway ? "default=prod" : "default=dev";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
