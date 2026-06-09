package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.domain.support.BarcodeNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Service
public class EstoqueFisicoCsvService {

    private static final Logger log = LoggerFactory.getLogger(EstoqueFisicoCsvService.class);
    private final Path csvPath;

    private volatile long cachedLastModified = Long.MIN_VALUE;
    private volatile List<EstoqueItem> cachedItems = List.of();

    public EstoqueFisicoCsvService(
            @Value("${estoque-fisico.csv.path:Estoque Fisico.csv}") String csvPath
    ) {
        this.csvPath = Paths.get(csvPath);
    }

    public List<EstoqueItem> search(String query) {
        List<EstoqueItem> source = this.loadCached();
        String termo = normalize(query).toLowerCase(Locale.ROOT);
        if (termo.isBlank()) {
            return source;
        }
        return source.stream()
                .filter(item -> item.searchBlob().contains(termo))
                .toList();
    }

    public List<EstoqueItem> parseUploadedCsv(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.ISO_8859_1))) {
            return this.parseCsv(reader);
        }
    }

    private List<EstoqueItem> loadCached() {
        Path sourcePath = this.resolveSourcePath();
        long lastModified = this.resolveLastModified(sourcePath);
        if (lastModified == this.cachedLastModified) {
            return this.cachedItems;
        }
        synchronized (this) {
            Path currentPath = this.resolveSourcePath();
            long current = this.resolveLastModified(currentPath);
            if (current == this.cachedLastModified) {
                return this.cachedItems;
            }
            List<EstoqueItem> parsed = this.parseCsv(currentPath);
            this.cachedItems = parsed;
            this.cachedLastModified = current;
            log.info("[estoque-csv] cache atualizado: {} itens", parsed.size());
            return parsed;
        }
    }

    private Path resolveSourcePath() {
        if (!Files.isDirectory(this.csvPath)) {
            return this.csvPath;
        }

        try (Stream<Path> entries = Files.list(this.csvPath)) {
            return entries
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".csv"))
                    .sorted()
                    .findFirst()
                    .orElse(this.csvPath);
        } catch (IOException ex) {
            log.warn("[estoque-csv] nao foi possivel resolver arquivo dentro do diretorio {}", this.csvPath, ex);
            return this.csvPath;
        }
    }

    private long resolveLastModified(Path path) {
        try {
            if (Files.exists(path)) {
                return Files.getLastModifiedTime(path).toMillis();
            }
        } catch (IOException ex) {
            log.warn("[estoque-csv] nao foi possivel ler lastModified", ex);
        }
        return -1L;
    }

    private List<EstoqueItem> parseCsv(Path path) {
        if (!Files.exists(path)) {
            log.warn("[estoque-csv] arquivo nao encontrado: {}", path.toAbsolutePath());
            return List.of();
        }
        if (Files.isDirectory(path)) {
            log.warn("[estoque-csv] caminho configurado aponta para diretorio, sem arquivo CSV legivel: {}", path.toAbsolutePath());
            return List.of();
        }

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
            return this.parseCsv(reader);
        } catch (IOException ex) {
            log.error("[estoque-csv] falha ao ler arquivo {}", path.toAbsolutePath(), ex);
            return List.of();
        }
    }

    private List<EstoqueItem> parseCsv(BufferedReader reader) throws IOException {
        LinkedHashMap<String, EstoqueItem> dedupe = new LinkedHashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            EstoqueItem item = this.parseLine(line);
            if (item == null) {
                continue;
            }
            String key = this.buildKey(item);
            EstoqueItem previous = dedupe.get(key);
            if (previous == null || item.estoque() > previous.estoque()) {
                dedupe.put(key, item);
            }
        }
        return new ArrayList<>(dedupe.values());
    }

    private String buildKey(EstoqueItem item) {
        if (item.legacyId() != null) {
            return "L" + item.legacyId();
        }
        if (!item.codigoBarras().isBlank()) {
            return "B" + item.codigoBarras();
        }
        return "N" + item.nome().toLowerCase(Locale.ROOT);
    }

    private EstoqueItem parseLine(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] raw = line.split(";", -1);
        if (isHeaderOrSummary(raw)) {
            return null;
        }

        String nome = normalize(tokenAt(raw, 2));
        if (nome.length() < 3) {
            return null;
        }

        Long legacyId = parseLong(tokenAt(raw, 1));
        String codigoBarras = normalizeBarcode(tokenAt(raw, 0));
        if (legacyId == null && codigoBarras.isBlank()) {
            return null;
        }

        Integer estoque = parseInteger(tokenAt(raw, 6));
        if (estoque == null || estoque < 0) {
            estoque = 0;
        }

        String fabricante = normalize(tokenAt(raw, 3));
        BigDecimal precoTabela = parseMoney(tokenAt(raw, 7));
        BigDecimal precoVenda = parseMoney(tokenAt(raw, 8));

        String searchBlob = (
                nome + " " +
                (legacyId == null ? "" : legacyId) + " " +
                codigoBarras + " " +
                fabricante
        ).toLowerCase(Locale.ROOT);

        return new EstoqueItem(
                legacyId,
                codigoBarras,
                nome,
                fabricante,
                estoque,
                precoTabela,
                precoVenda,
                searchBlob
        );
    }

    private static boolean isHeaderOrSummary(String[] tokens) {
        String first = normalize(tokenAt(tokens, 0)).toUpperCase(Locale.ROOT);
        String second = normalize(tokenAt(tokens, 1)).toUpperCase(Locale.ROOT);
        String third = normalize(tokenAt(tokens, 2)).toUpperCase(Locale.ROOT);
        if (first.startsWith("SUB-TOTAL") || first.startsWith("GERADO POR")) {
            return true;
        }
        if (first.contains("COD") && first.contains("BARRAS")) {
            return true;
        }
        if (second.contains("CODIGO") && third.contains("PRODUTO")) {
            return true;
        }
        if (first.contains("PR.PROMO") || first.contains("LOCALIZA")) {
            return true;
        }
        return false;
    }

    private static String tokenAt(String[] tokens, int index) {
        if (tokens == null || index < 0 || index >= tokens.length) {
            return "";
        }
        return tokens[index];
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replace('\u00a0', ' ');
    }

    private static Integer parseInteger(String value) {
        BigDecimal parsed = parseLocalizedDecimal(value);
        if (parsed == null) {
            return null;
        }
        try {
            return parsed
                    .max(BigDecimal.ZERO)
                    .setScale(0, RoundingMode.DOWN)
                    .intValueExact();
        } catch (ArithmeticException ex) {
            return null;
        }
    }

    private static Long parseLong(String value) {
        String cleaned = normalize(value).replaceAll("[^0-9]", "");
        if (cleaned.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static BigDecimal parseMoney(String value) {
        BigDecimal parsed = parseLocalizedDecimal(
                normalize(value).replace("R$", "")
        );
        if (parsed == null) {
            return null;
        }
        return parsed;
    }

    private static BigDecimal parseLocalizedDecimal(String value) {
        String cleaned = normalize(value).replaceAll("[^0-9,.-]", "");
        if (cleaned.isBlank()) {
            return null;
        }
        int lastComma = cleaned.lastIndexOf(',');
        int lastDot = cleaned.lastIndexOf('.');
        if (lastComma >= 0 && lastDot >= 0) {
            if (lastComma > lastDot) {
                cleaned = cleaned.replace(".", "").replace(',', '.');
            } else {
                cleaned = cleaned.replace(",", "");
            }
        } else if (lastComma >= 0) {
            cleaned = cleaned.replace(',', '.');
        }
        try {
            return new BigDecimal(cleaned);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String normalizeBarcode(String value) {
        return BarcodeNormalizer.normalize(value);
    }

    public record EstoqueItem(
            Long legacyId,
            String codigoBarras,
            String nome,
            String fabricante,
            Integer estoque,
            BigDecimal precoTabela,
            BigDecimal precoVenda,
            String searchBlob
    ) {
    }
}
