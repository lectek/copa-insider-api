/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
 */
package br.com.redemaisfarma.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration(proxyBeanMethods=false)
@EnableMethodSecurity(prePostEnabled=false, securedEnabled=false, jsr250Enabled=false)
public class MethodSecurityConfig {
}

