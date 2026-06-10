package br.com.lectek.copainsider.adapters.inbound.web;

import br.com.lectek.copainsider.application.core.media.ImageStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
@EnableConfigurationProperties(ImageStorageProperties.class)
public class WebResourcesConfig implements WebMvcConfigurer {

    private static final String FALLBACK_MEDIA_LOCATION = "file:media/";
    private final ImageStorageProperties imageStorageProperties;

    public WebResourcesConfig(ImageStorageProperties imageStorageProperties) {
        this.imageStorageProperties = imageStorageProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/img/");
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/static/images/");
        registry.addResourceHandler("/animations/**").addResourceLocations("classpath:/static/animations/");
        registry.addResourceHandler("/media/**").addResourceLocations(resolveMediaLocations());
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/static/favicon.ico");
        registry.addResourceHandler("/index.html").addResourceLocations("classpath:/static/index.html");
    }

    private String[] resolveMediaLocations() {
        Set<String> locations = new LinkedHashSet<>();
        addMediaParentLocation(locations, this.imageStorageProperties.getDir());
        addMediaParentLocation(locations, this.imageStorageProperties.getUserDir());
        locations.add(FALLBACK_MEDIA_LOCATION);
        return locations.toArray(String[]::new);
    }

    private static void addMediaParentLocation(Set<String> locations, String configuredDir) {
        if (configuredDir == null || configuredDir.isBlank()) {
            return;
        }
        Path dir = Paths.get(configuredDir).toAbsolutePath().normalize();
        Path parent = dir.getParent();
        if (parent == null) {
            return;
        }

        String normalizedPath = parent.toString().replace('\\', '/');
        if (!normalizedPath.endsWith("/")) {
            normalizedPath = normalizedPath + "/";
        }
        locations.add("file:" + normalizedPath);
    }
}
