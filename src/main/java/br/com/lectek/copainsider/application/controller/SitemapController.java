package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.copa.PartidaVM;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class SitemapController {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String HOURLY  = "hourly";
    private static final String DAILY   = "daily";
    private static final String WEEKLY  = "weekly";
    private static final String MONTHLY = "monthly";

    private final Copa2026DataService copaData;
    private final String baseUrl;

    public SitemapController(Copa2026DataService copaData,
                             @Value("${app.web.base-url:https://copainsider.com}") String baseUrl) {
        this.copaData = copaData;
        this.baseUrl  = baseUrl;
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> sitemap() {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        // Páginas estáticas
        addUrl(sb, "",            DAILY,   "1.0");
        addUrl(sb, "/calendario", DAILY,   "0.9");
        addUrl(sb, "/selecoes",   WEEKLY,  "0.8");
        addUrl(sb, "/comparar",   WEEKLY,  "0.7");
        addUrl(sb, "/ranking",    DAILY,   "0.8");
        addUrl(sb, "/loja",       WEEKLY,  "0.9");
        addUrl(sb, "/doacao",     MONTHLY, "0.5");

        // Páginas dinâmicas por partida
        String today = LocalDateTime.now().format(ISO);
        for (PartidaVM p : copaData.listPartidas()) {
            addUrlWithDate(sb, "/jogo/"       + p.id() + "/sala",  HOURLY, "1.0", today);
            addUrlWithDate(sb, "/jogo/"       + p.id() + "/notas", HOURLY, "0.9", today);
            addUrl(sb, "/segunda-tela/"  + p.id(), HOURLY, "0.8");
            addUrl(sb, "/partida/"       + p.id(), DAILY,  "0.7");
        }

        sb.append("</urlset>");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(sb.toString());
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> robots() {
        String body = "User-agent: *\n" +
                      "Allow: /\n" +
                      "Disallow: /admin/\n" +
                      "Disallow: /api/\n" +
                      "Disallow: /actuator/\n" +
                      "Disallow: /auth/\n" +
                      "\n" +
                      "Sitemap: " + baseUrl + "/sitemap.xml\n";
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(body);
    }

    private void addUrl(StringBuilder sb, String path, String changefreq, String priority) {
        addUrlWithDate(sb, path, changefreq, priority, null);
    }

    private void addUrlWithDate(StringBuilder sb, String path, String changefreq,
                                String priority, String lastmod) {
        sb.append("  <url>\n");
        sb.append("    <loc>").append(baseUrl).append(path).append("</loc>\n");
        if (lastmod != null) sb.append("    <lastmod>").append(lastmod).append("</lastmod>\n");
        sb.append("    <changefreq>").append(changefreq).append("</changefreq>\n");
        sb.append("    <priority>").append(priority).append("</priority>\n");
        sb.append("  </url>\n");
    }
}
