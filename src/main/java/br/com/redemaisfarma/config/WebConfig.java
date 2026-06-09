/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.web.servlet.LocaleResolver
 *  org.springframework.web.servlet.i18n.CookieLocaleResolver
 */
package br.com.redemaisfarma.config;

import java.time.Duration;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

@Configuration
public class WebConfig {
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.of("pt", "BR"));
        resolver.setCookieMaxAge(Duration.ofDays(30L));
        resolver.setCookiePath("/");
        return resolver;
    }
}

