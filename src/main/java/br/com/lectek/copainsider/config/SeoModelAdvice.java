package br.com.lectek.copainsider.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injeta atributos globais de SEO em todos os modelos Thymeleaf.
 */
@ControllerAdvice
public class SeoModelAdvice {

    @Value("${app.web.base-url:https://copainsider.com}")
    private String baseUrl;

    @ModelAttribute("baseUrl")
    public String baseUrl() {
        return baseUrl;
    }

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
