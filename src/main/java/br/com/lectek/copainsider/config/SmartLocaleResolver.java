package br.com.lectek.copainsider.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.time.Duration;
import java.util.Enumeration;
import java.util.Locale;

/**
 * Falls back to Accept-Language when no locale cookie is set.
 * Priority: cookie → Accept-Language → default (pt-BR).
 */
public class SmartLocaleResolver extends CookieLocaleResolver {

    private final String name;

    public SmartLocaleResolver(String cookieName) {
        super(cookieName);
        this.name = cookieName;
        setDefaultLocale(Locale.of("pt", "BR"));
        setCookieMaxAge(Duration.ofDays(30));
        setCookiePath("/");
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        if (hasCookieValue(request)) {
            return super.resolveLocale(request);
        }
        return detectFromAcceptLanguage(request);
    }

    private boolean hasCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return false;
        for (Cookie c : cookies) {
            if (name.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                return true;
            }
        }
        return false;
    }

    private Locale detectFromAcceptLanguage(HttpServletRequest request) {
        Enumeration<Locale> accepted = request.getLocales();
        while (accepted.hasMoreElements()) {
            Locale locale = accepted.nextElement();
            String lang = locale.getLanguage();
            String country = locale.getCountry();
            if ("pt".equals(lang) && "PT".equals(country)) return Locale.of("pt", "PT");
            if ("pt".equals(lang)) return Locale.of("pt", "BR");
            if ("en".equals(lang)) return Locale.ENGLISH;
        }
        return Locale.of("pt", "BR");
    }
}
