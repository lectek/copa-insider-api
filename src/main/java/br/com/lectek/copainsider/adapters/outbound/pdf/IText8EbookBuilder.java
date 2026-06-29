package br.com.lectek.copainsider.adapters.outbound.pdf;

import br.com.lectek.copainsider.adapters.outbound.selecao.SelecaoDataResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class IText8EbookBuilder {

    private static final Logger log = LoggerFactory.getLogger(IText8EbookBuilder.class);

    private final ObjectMapper objectMapper;

    public IText8EbookBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String construir(SelecaoDataResult dados, String conteudoJson, String token, String storageDir) throws IOException {
        File dir = new File(storageDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Não foi possível criar o diretório: " + storageDir);
        }

        String filename = "guia-" + dados.getSelecaoCode().toLowerCase() + "-" + token + ".pdf";
        String caminho = storageDir + File.separator + filename;

        JsonNode conteudo = parseConteudo(conteudoJson);
        DeviceRgb corPrimaria   = parseHex(dados.getCorPrimaria(),   0, 0, 128);
        DeviceRgb corSecundaria = parseHex(dados.getCorSecundaria(), 255, 215, 0);

        try (PdfWriter writer = new PdfWriter(caminho);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf, PageSize.A4)) {

            doc.setMargins(60, 60, 60, 60);

            PdfFont fonteTitulo  = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fonteTexto   = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fonteItalico = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            // ── CAPA ─────────────────────────────────────────────────────────
            adicionarCapa(doc, dados, conteudo, fonteTitulo, fonteItalico, corPrimaria, corSecundaria);

            // ── SECÇÃO 1 — A Alma da Seleção ─────────────────────────────────
            adicionarSecao(doc, "A Alma da Seleção",
                    texto(conteudo, "alma_selecao"),
                    fonteTitulo, fonteTexto, corPrimaria);

            // ── SECÇÃO 2 — As Lendas Imortais ────────────────────────────────
            adicionarTituloSecao(doc, "As Lendas Imortais", fonteTitulo, corPrimaria);
            if (conteudo.has("lendas") && conteudo.get("lendas").isArray()) {
                for (JsonNode lenda : conteudo.get("lendas")) {
                    adicionarLenda(doc, lenda, fonteTitulo, fonteTexto, fonteItalico, corPrimaria);
                }
            }

            // ── SECÇÃO 3 — Partidas que Pararam o Mundo ──────────────────────
            adicionarTituloSecao(doc, "As Partidas que Pararam o Mundo", fonteTitulo, corPrimaria);
            if (conteudo.has("partidas_historicas") && conteudo.get("partidas_historicas").isArray()) {
                for (JsonNode partida : conteudo.get("partidas_historicas")) {
                    adicionarPartida(doc, partida, fonteTitulo, fonteTexto, corPrimaria, corSecundaria);
                }
            }

            // ── SECÇÃO 4 — A História nas Copas ──────────────────────────────
            adicionarSecao(doc, "A História nas Copas",
                    texto(conteudo, "historia_copas"),
                    fonteTitulo, fonteTexto, corPrimaria);

            // ── SECÇÃO 5 — Os Guerreiros de Hoje ─────────────────────────────
            adicionarTituloSecao(doc, "Os Guerreiros de Hoje", fonteTitulo, corPrimaria);
            if (conteudo.has("guerreiros_hoje") && conteudo.get("guerreiros_hoje").isArray()) {
                for (JsonNode guerreiro : conteudo.get("guerreiros_hoje")) {
                    adicionarGuerreiro(doc, guerreiro, fonteTitulo, fonteTexto, corPrimaria);
                }
            }

            // ── SECÇÃO 6 — A Copa 2026 ────────────────────────────────────────
            adicionarSecao(doc, "A Copa 2026 que Está por Vir",
                    texto(conteudo, "copa_2026"),
                    fonteTitulo, fonteTexto, corPrimaria);

            // ── ENCERRAMENTO — Manifesto do Torcedor ─────────────────────────
            adicionarManifesto(doc, conteudo, dados, fonteTitulo, fonteItalico, corPrimaria, corSecundaria);
        }

        log.info("[pdf] ebook gerado — {}", caminho);
        return caminho;
    }

    // ── Métodos de construção de secções ─────────────────────────────────────

    private void adicionarCapa(Document doc, SelecaoDataResult dados, JsonNode conteudo,
                                PdfFont fonteTitulo, PdfFont fonteItalico,
                                DeviceRgb corPrimaria, DeviceRgb corSecundaria) {
        doc.add(new Paragraph("\n\n\n\n"));

        doc.add(new Paragraph("GUIA DA SELEÇÃO")
                .setFont(fonteTitulo).setFontSize(14)
                .setFontColor(corSecundaria)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(dados.getSelecaoNome() != null ? dados.getSelecaoNome().toUpperCase() : "")
                .setFont(fonteTitulo).setFontSize(42)
                .setFontColor(corPrimaria)
                .setTextAlignment(TextAlignment.CENTER));

        String apelido = dados.getApelido() != null ? dados.getApelido() : "";
        doc.add(new Paragraph(apelido)
                .setFont(fonteItalico).setFontSize(18)
                .setFontColor(new DeviceRgb(80, 80, 80))
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph("\n\n"));

        String fraseCapa = texto(conteudo, "frase_capa");
        if (!fraseCapa.isEmpty()) {
            doc.add(new Paragraph("\"" + fraseCapa + "\"")
                    .setFont(fonteItalico).setFontSize(16)
                    .setFontColor(corPrimaria)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        doc.add(new Paragraph("\n\n\n\n\n\n\n"));
        doc.add(new Paragraph("Copa Insider · Copa do Mundo 2026")
                .setFont(fonteItalico).setFontSize(10)
                .setFontColor(new DeviceRgb(120, 120, 120))
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
    }

    private void adicionarSecao(Document doc, String titulo, String corpo,
                                 PdfFont fonteTitulo, PdfFont fonteTexto, DeviceRgb cor) {
        adicionarTituloSecao(doc, titulo, fonteTitulo, cor);
        if (!corpo.isEmpty()) {
            doc.add(new Paragraph(corpo)
                    .setFont(fonteTexto).setFontSize(11)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMultipliedLeading(1.5f));
        }
        doc.add(new Paragraph("\n"));
    }

    private void adicionarTituloSecao(Document doc, String titulo, PdfFont fonteTitulo, DeviceRgb cor) {
        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        doc.add(new Paragraph(titulo.toUpperCase())
                .setFont(fonteTitulo).setFontSize(20)
                .setFontColor(cor)
                .setTextAlignment(TextAlignment.LEFT)
                .setMarginBottom(20));
    }

    private void adicionarLenda(Document doc, JsonNode lenda,
                                 PdfFont fonteTitulo, PdfFont fonteTexto, PdfFont fonteItalico,
                                 DeviceRgb cor) {
        String nome = texto(lenda, "nome");
        String apelido = texto(lenda, "apelido");
        String bio = texto(lenda, "bio_narrativa");
        String legado = texto(lenda, "legado");

        if (!nome.isEmpty()) {
            Paragraph nomePara = new Paragraph();
            nomePara.add(new Text(nome).setFont(fonteTitulo).setFontSize(14).setFontColor(cor));
            if (!apelido.isEmpty()) {
                nomePara.add(new Text("  —  " + apelido).setFont(fonteItalico).setFontSize(12)
                        .setFontColor(new DeviceRgb(80, 80, 80)));
            }
            doc.add(nomePara.setMarginTop(16).setMarginBottom(6));
        }

        if (!bio.isEmpty()) {
            doc.add(new Paragraph(bio)
                    .setFont(fonteTexto).setFontSize(11)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMultipliedLeading(1.5f));
        }

        if (!legado.isEmpty()) {
            doc.add(new Paragraph("\" " + legado + " \"")
                    .setFont(fonteItalico).setFontSize(11)
                    .setFontColor(cor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(6).setMarginBottom(20));
        }
    }

    private void adicionarPartida(Document doc, JsonNode partida,
                                   PdfFont fonteTitulo, PdfFont fonteTexto,
                                   DeviceRgb corPrimaria, DeviceRgb corSecundaria) {
        String titulo = texto(partida, "titulo");
        String adversario = texto(partida, "adversario");
        String placar = texto(partida, "placar");
        String data = texto(partida, "data");
        String narrativa = texto(partida, "narrativa");

        if (!titulo.isEmpty()) {
            doc.add(new Paragraph(titulo)
                    .setFont(fonteTitulo).setFontSize(13)
                    .setFontColor(corPrimaria)
                    .setMarginTop(16).setMarginBottom(4));
        }

        if (!adversario.isEmpty() || !placar.isEmpty()) {
            doc.add(new Paragraph(adversario + "  " + placar + "  " + data)
                    .setFont(fonteTitulo).setFontSize(10)
                    .setFontColor(new DeviceRgb(100, 100, 100))
                    .setMarginBottom(8));
        }

        if (!narrativa.isEmpty()) {
            doc.add(new Paragraph(narrativa)
                    .setFont(fonteTexto).setFontSize(11)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMultipliedLeading(1.5f)
                    .setMarginBottom(20));
        }
    }

    private void adicionarGuerreiro(Document doc, JsonNode guerreiro,
                                     PdfFont fonteTitulo, PdfFont fonteTexto, DeviceRgb cor) {
        String nome = texto(guerreiro, "nome");
        String descricao = texto(guerreiro, "descricao");

        if (!nome.isEmpty()) {
            doc.add(new Paragraph(nome)
                    .setFont(fonteTitulo).setFontSize(13)
                    .setFontColor(cor)
                    .setMarginTop(14).setMarginBottom(4));
        }
        if (!descricao.isEmpty()) {
            doc.add(new Paragraph(descricao)
                    .setFont(fonteTexto).setFontSize(11)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMultipliedLeading(1.5f)
                    .setMarginBottom(16));
        }
    }

    private void adicionarManifesto(Document doc, JsonNode conteudo, SelecaoDataResult dados,
                                     PdfFont fonteTitulo, PdfFont fonteItalico,
                                     DeviceRgb corPrimaria, DeviceRgb corSecundaria) {
        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        doc.add(new Paragraph("MANIFESTO DO TORCEDOR")
                .setFont(fonteTitulo).setFontSize(18)
                .setFontColor(corPrimaria)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30));

        String manifesto = texto(conteudo, "manifesto_torcedor");
        if (!manifesto.isEmpty()) {
            doc.add(new Paragraph(manifesto)
                    .setFont(fonteItalico).setFontSize(13)
                    .setFontColor(corPrimaria)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMultipliedLeading(1.8f));
        }

        doc.add(new Paragraph("\n\n\n"));
        doc.add(new Paragraph("Copa Insider · Copa do Mundo 2026 · " + dados.getSelecaoNome())
                .setFont(fonteItalico).setFontSize(9)
                .setFontColor(new DeviceRgb(150, 150, 150))
                .setTextAlignment(TextAlignment.CENTER));
    }

    // ── Utilitários ──────────────────────────────────────────────────────────

    private JsonNode parseConteudo(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.warn("[pdf] JSON de conteúdo inválido — usando nó vazio: {}", e.getMessage());
            return objectMapper.createObjectNode();
        }
    }

    private String texto(JsonNode node, String campo) {
        JsonNode n = node.get(campo);
        return (n != null && n.isTextual()) ? n.asText() : "";
    }

    private DeviceRgb parseHex(String hex, int rDef, int gDef, int bDef) {
        try {
            if (hex == null || hex.isBlank()) return new DeviceRgb(rDef, gDef, bDef);
            String h = hex.replace("#", "");
            int r = Integer.parseInt(h.substring(0, 2), 16);
            int g = Integer.parseInt(h.substring(2, 4), 16);
            int b = Integer.parseInt(h.substring(4, 6), 16);
            return new DeviceRgb(r, g, b);
        } catch (Exception e) {
            return new DeviceRgb(rDef, gDef, bDef);
        }
    }
}
