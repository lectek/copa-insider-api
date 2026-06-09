package br.com.lectek.copainsider.domain.ai;

import br.com.lectek.copainsider.application.port.outbound.ProdutoRepositoryPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.Normalizer;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ProductPromptFactory {

    private static final Pattern VOLUME_PATTERN = Pattern.compile("(\\d{2,3})\\s*ml");

    public Map<String, Object> varsFromProduto(final ProdutoRepositoryPort.ProdutoDTO produto) {
        return Map.of(
                "nome", this.safe(produto.nome()),
                "descricao", this.safe(produto.descricao()),
                "categoria", this.safe(produto.categoria()),
                "fabricante", this.safe(produto.fabricante()),
                "codigo", this.safe(produto.codigoBarras()),
                "tipoFisico", this.resolvePhysicalHint(produto),
                "cores", List.of("#0077FF", "#00CC88")
        );
    }

    public String promptForProduto(final ProdutoRepositoryPort.ProdutoDTO produto) {
        final StringBuilder builder = new StringBuilder(
                "Packshot fotorealista de ecommerce do produto real, fundo branco puro, "
                        + "luz de estudio suave e sombra discreta. "
                        + "Reproduza o tipo fisico exato do item e nao substitua por um objeto parecido."
        );
        this.appendField(builder, "Nome oficial", produto.nome());
        this.appendField(builder, "Descricao", produto.descricao());
        this.appendField(builder, "Categoria", produto.categoria());
        this.appendField(builder, "Fabricante", produto.fabricante());
        this.appendField(builder, "Codigo ou EAN", produto.codigoBarras());
        this.appendField(builder, "Referencia visual obrigatoria", this.resolvePhysicalHint(produto));
        builder.append(" Preserve formato fisico, material, tampa, rotulo, proporcoes, volume e acabamento da embalagem oficial.");
        builder.append(" Mostrar somente o produto em enquadramento de catalogo, sem pessoas, sem maos, sem objetos extras, sem mockup 3D, sem cenario, sem texto extra fora da embalagem oficial e sem marca d'agua.");
        return builder.toString();
    }

    public String fingerprint(final String preset, final Map<String, Object> vars) {
        final String input = preset + "::" + vars.toString();
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return ProductPromptFactory.toHex(digest);
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }

    private static String toHex(final byte[] bytes) {
        try (Formatter formatter = new Formatter()) {
            for (byte value : bytes) {
                formatter.format("%02x", value);
            }
            return formatter.toString();
        }
    }

    private String resolvePhysicalHint(final ProdutoRepositoryPort.ProdutoDTO produto) {
        final String source = String.join(
                " ",
                this.safe(produto.nome()),
                this.safe(produto.descricao()),
                this.safe(produto.categoria())
        ).trim();
        if (source.isBlank()) {
            return "";
        }

        final String normalized = normalizeForMatch(source);
        if (normalized.contains("coletor")) {
            return buildCollectorHint(normalized);
        }
        return "";
    }

    private String buildCollectorHint(final String normalizedSource) {
        final StringBuilder hint = new StringBuilder(
                "Coletor universal para exames ou laboratorio, pequeno frasco coletor cilindrico de plastico transparente ou translucido, boca larga e tampa rosqueavel"
        );

        final String lidColor = resolveCollectorLidColor(normalizedSource);
        if (lidColor != null) {
            hint.append(" na cor ").append(lidColor);
        }

        final String volume = resolveCollectorVolume(normalizedSource);
        if (volume != null) {
            hint.append(", capacidade aproximada de ").append(volume).append(" ml");
        }

        hint.append(", aparencia hospitalar real.");
        hint.append(" Nao transformar em copo domestico, pote cosmetico, garrafa ou embalagem generica.");
        return hint.toString();
    }

    private String resolveCollectorVolume(final String normalizedSource) {
        final Matcher matcher = VOLUME_PATTERN.matcher(normalizedSource);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String resolveCollectorLidColor(final String normalizedSource) {
        if (normalizedSource.contains("tampavermelh") || normalizedSource.contains("tampa vermelh")) {
            return "vermelha";
        }
        if (normalizedSource.contains("tampaazul") || normalizedSource.contains("tampa azul")) {
            return "azul";
        }
        if (normalizedSource.contains("tampacristal")
                || normalizedSource.contains("tampa cristal")
                || normalizedSource.contains(" cristal ")) {
            return "cristal ou transparente";
        }
        return null;
    }

    private String normalizeForMatch(final String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^a-zA-Z0-9]+", " ")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private String safe(final String value) {
        return value == null ? "" : value;
    }

    private void appendField(final StringBuilder builder, final String label, final String value) {
        final String safeValue = this.safe(value);
        if (!safeValue.isBlank()) {
            builder.append(' ').append(label).append(": ").append(safeValue).append('.');
        }
    }
}
