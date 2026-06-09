package br.com.redemaisfarma.application.core.media;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ImageStorageServiceBeanTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void shouldCreateImageStorageServiceBean() {
        contextRunner.run(context -> {
            assertThat(context.getStartupFailure()).isNull();
            assertThat(context.getBeansOfType(ImageStorageProperties.class)).hasSize(1);
            assertThat(context.getBeansOfType(ImageStorageService.class)).hasSize(1);
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ImageStorageProperties.class)
    @ComponentScan(basePackageClasses = ImageStorageService.class)
    static class TestConfig {
    }
}
