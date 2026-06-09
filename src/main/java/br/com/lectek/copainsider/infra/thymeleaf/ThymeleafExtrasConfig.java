// src/main/java/br/com/lectek/copainsider/infra/thymeleaf/ThymeleafExtrasConfig.java
package br.com.lectek.copainsider.infra.thymeleaf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
public class ThymeleafExtrasConfig {
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}
