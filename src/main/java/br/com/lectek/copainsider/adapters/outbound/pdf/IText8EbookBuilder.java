package br.com.lectek.copainsider.adapters.outbound.pdf;

import br.com.lectek.copainsider.application.service.EbookConteudo;
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
    private static final String SEP = "  --  ";

    public String construir(EbookConteudo conteudo, String token, String storageDir) throws IOException {
        File dir = new File(storageDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Nao foi possivel criar o diretorio: " + storageDir);
        }

        String filename = "guia-" + conteudo.selecaoCode().toLowerCase() + "-" + token + ".pdf";
        String caminho  = storageDir + File.separator + filename;

        DeviceRgb corPrimaria   = parseHex(conteudo.corPrimaria(),   0,   0, 128);
        DeviceRgb corSecundaria = parseHex(conteudo.corSecundaria(), 255, 215,  0);

        try (PdfWriter writer = new PdfWriter(caminho);
             PdfDocument pdf  = new PdfDocument(writer);
             Document doc     = new Document(pdf, PageSize.A4)) {

            doc.setMargins(60, 60, 60, 60);

            PdfFont fonteTitulo  = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fonteTexto   = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fonteItalico = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            adicionarCapa(doc, conteudo, fonteTitulo, fonteItalico, corPrimaria, corSecundaria);

            adicionarSecao(doc, "A Alma da Selecao",
                    conteudo.almaSeleção(), fonteTitulo, fonteTexto, corPrimaria);

            if (!conteudo.lendas().isEmpty()) {
                adicionarTituloSecao(doc, "As Lendas Imortais", fonteTitulo, corPrimaria);
                for (EbookConteudo.Lenda lenda : conteudo.lendas()) {
                    adicionarLenda(doc, lenda, fonteTitulo, fonteTexto, fonteItalico, corPrimaria);
                }
            }

            if (!conteudo.partidas().isEmpty()) {
                adicionarTituloSecao(doc, "As Partidas que Pararam o Mundo", fonteTitulo, corPrimaria);
                for (EbookConteudo.PartidaHistorica partida : conteudo.partidas()) {
                    adicionarPartida(doc, partida, fonteTitulo, fonteTexto, corPrimaria);
                }
            }

            adicionarSecao(doc, "A Historia nas Copas",
                    conteudo.historiaCopas(), fonteTitulo, fonteTexto, corPrimaria);

            if (!conteudo.guerreirosHoje().isEmpty()) {
                adicionarTituloSecao(doc, "Os Guerreiros de Hoje", fonteTitulo, corPrimaria);
                for (EbookConteudo.Guerreiro guerreiro : conteudo.guerreirosHoje()) {
                    adicionarGuerreiro(doc, guerreiro, fonteTitulo, fonteTexto, corPrimaria);
                }
            }

            adicionarSecao(doc, "A Copa 2026 que Esta por Vir",
                    conteudo.copa2026(), fonteTitulo, fonteTexto, corPrimaria);

            adicionarEmNumeros(doc, conteudo.emNumeros(), fonteTitulo, fonteTexto, corPrimaria, corSecundaria);

            adicionarSabiaque(doc, conteudo.sabiaque(), fonteTitulo, fonteItalico, corPrimaria);

            adicionarManifesto(doc, conteudo, fonteTitulo, fonteItalico, corPrimaria);
        }

        log.info("[pdf] ebook gerado — {}", caminho);
        return caminho;
    }

    // ── Capa ──────────────────────────────────────────────────────────────────

    private void adicionarCapa(Document doc, EbookConteudo c,
                                PdfFont fonteTitulo, PdfFont fonteItalico,
                                DeviceRgb corPrimaria, DeviceRgb corSecundaria) {
        doc.add(new Paragraph("\n\n\n\n"));

        doc.add(new Paragraph("GUIA DA SELECAO")
                .setFont(fonteTitulo).setFontSize(14)
                .setFontColor(corSecundaria)
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new Paragraph(c.selecaoNome() != null ? c.selecaoNome().toUpperCase() : "")
                .setFont(fonteTitulo).setFontSize(42)
                .setFontColor(corPrimaria)
                .setTextAlignment(TextAlignment.CENTER));

        if (c.apelido() != null && !c.apelido().isBlank()) {
            doc.add(new Paragraph(c.apelido())
                    .setFont(fonteItalico).setFontSize(18)
                    .setFontColor(new DeviceRgb(80, 80, 80))
                    .setTextAlignment(TextAlignment.CENTER));
        }

        doc.add(new Paragraph("\n\n"));

        if (c.fraseCapa() != null && !c.fraseCapa().isBlank()) {
            doc.add(new Paragraph("\"" + c.fraseCapa() + "\"")
                    .setFont(fonteItalico).setFontSize(16)
                    .setFontColor(corPrimaria)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        doc.add(new Paragraph("\n\n\n\n\n\n\n"));
        doc.add(new Paragraph("Copa Insider  --  Copa do Mundo 2026")
                .setFont(fonteItalico).setFontSize(10)
                .setFontColor(new DeviceRgb(120, 120, 120))
                .setTextAlignment(TextAlignment.CENTER));

        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
    }

    // ── Secções genéricas ──────────────────────────────────────────────────────

    private void adicionarSecao(Document doc, String titulo, String corpo,
                                 PdfFont fonteTitulo, PdfFont fonteTexto, DeviceRgb cor) {
        adicionarTituloSecao(doc, titulo, fonteTitulo, cor);
        if (corpo != null && !corpo.isBlank()) {
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

    // ── Lenda ─────────────────────────────────────────────────────────────────

    private void adicionarLenda(Document doc, EbookConteudo.Lenda lenda,
                                 PdfFont fonteTitulo, PdfFont fonteTexto, PdfFont fonteItalico,
                                 DeviceRgb cor) {
        if (lenda.nome().isBlank()) return;

        Paragraph nomePara = new Paragraph();
        nomePara.add(new Text(lenda.nome()).setFont(fonteTitulo).setFontSize(14).setFontColor(cor));
        if (!lenda.apelido().isBlank()) {
            nomePara.add(new Text(SEP + lenda.apelido())
                    .setFont(fonteItalico).setFontSize(12)
                    .setFontColor(new DeviceRgb(80, 80, 80)));
        }
        doc.add(nomePara.setMarginTop(16).setMarginBottom(6));

        if (!lenda.bioNarrativa().isBlank()) {
            doc.add(new Paragraph(lenda.bioNarrativa())
                    .setFont(fonteTexto).setFontSize(11)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMultipliedLeading(1.5f));
        }

        if (!lenda.legado().isBlank()) {
            doc.add(new Paragraph("\" " + lenda.legado() + " \"")
                    .setFont(fonteItalico).setFontSize(11)
                    .setFontColor(cor)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(6).setMarginBottom(20));
        }
    }

    // ── Partida histórica ─────────────────────────────────────────────────────

    private void adicionarPartida(Document doc, EbookConteudo.PartidaHistorica p,
                                   PdfFont fonteTitulo, PdfFont fonteTexto, DeviceRgb cor) {
        doc.add(new Paragraph(p.titulo())
                .setFont(fonteTitulo).setFontSize(13)
                .setFontColor(cor)
                .setMarginTop(16).setMarginBottom(4));

        doc.add(new Paragraph(p.adversario() + "  " + p.placar() + SEP + p.data())
                .setFont(fonteTitulo).setFontSize(10)
                .setFontColor(new DeviceRgb(100, 100, 100))
                .setMarginBottom(8));

        if (!p.narrativa().isBlank()) {
            doc.add(new Paragraph(p.narrativa())
                    .setFont(fonteTexto).setFontSize(11)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMultipliedLeading(1.5f)
                    .setMarginBottom(20));
        }
    }

    // ── Guerreiro ─────────────────────────────────────────────────────────────

    private void adicionarGuerreiro(Document doc, EbookConteudo.Guerreiro g,
                                     PdfFont fonteTitulo, PdfFont fonteTexto, DeviceRgb cor) {
        Paragraph titulo = new Paragraph();
        titulo.add(new Text(g.nome()).setFont(fonteTitulo).setFontSize(13).setFontColor(cor));
        if (g.clube() != null && !g.clube().isBlank()) {
            titulo.add(new Text(SEP + g.clube())
                    .setFont(fonteTexto).setFontSize(11)
                    .setFontColor(new DeviceRgb(80, 80, 80)));
        }
        doc.add(titulo.setMarginTop(14).setMarginBottom(4));

        if (!g.descricao().isBlank()) {
            doc.add(new Paragraph(g.descricao())
                    .setFont(fonteTexto).setFontSize(11)
                    .setTextAlignment(TextAlignment.JUSTIFIED)
                    .setMultipliedLeading(1.5f)
                    .setMarginBottom(14));
        }
    }

    // ── Em Números ────────────────────────────────────────────────────────────

    private void adicionarEmNumeros(Document doc, String emNumeros,
                                     PdfFont fonteTitulo, PdfFont fonteTexto,
                                     DeviceRgb corPrimaria, DeviceRgb corSecundaria) {
        if (emNumeros == null || emNumeros.isBlank()) return;
        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));

        String[] linhas = emNumeros.split("\n");
        if (linhas.length > 0) {
            doc.add(new Paragraph(linhas[0].toUpperCase())
                    .setFont(fonteTitulo).setFontSize(20)
                    .setFontColor(corPrimaria)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(24));
        }
        for (int i = 1; i < linhas.length; i++) {
            doc.add(new Paragraph(linhas[i])
                    .setFont(fonteTexto).setFontSize(13)
                    .setFontColor(corSecundaria)
                    .setMultipliedLeading(1.8f));
        }
    }

    // ── Sabia que ─────────────────────────────────────────────────────────────

    private void adicionarSabiaque(Document doc, String sabiaque,
                                    PdfFont fonteTitulo, PdfFont fonteItalico, DeviceRgb cor) {
        if (sabiaque == null || sabiaque.isBlank()) return;
        doc.add(new Paragraph("\n\n"));
        doc.add(new Paragraph("SABIA QUE...")
                .setFont(fonteTitulo).setFontSize(14)
                .setFontColor(cor)
                .setMarginBottom(10));
        doc.add(new Paragraph(sabiaque)
                .setFont(fonteItalico).setFontSize(12)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setMultipliedLeading(1.6f));
    }

    // ── Manifesto ─────────────────────────────────────────────────────────────

    private void adicionarManifesto(Document doc, EbookConteudo conteudo,
                                     PdfFont fonteTitulo, PdfFont fonteItalico, DeviceRgb corPrimaria) {
        doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
        doc.add(new Paragraph("MANIFESTO DO TORCEDOR")
                .setFont(fonteTitulo).setFontSize(18)
                .setFontColor(corPrimaria)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30));

        if (conteudo.manifestoTorcedor() != null && !conteudo.manifestoTorcedor().isBlank()) {
            doc.add(new Paragraph(conteudo.manifestoTorcedor())
                    .setFont(fonteItalico).setFontSize(13)
                    .setFontColor(corPrimaria)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMultipliedLeading(1.8f));
        }

        doc.add(new Paragraph("\n\n\n"));
        doc.add(new Paragraph("Copa Insider  --  Copa do Mundo 2026  --  " + conteudo.selecaoNome())
                .setFont(fonteItalico).setFontSize(9)
                .setFontColor(new DeviceRgb(150, 150, 150))
                .setTextAlignment(TextAlignment.CENTER));
    }

    // ── Utilitários ───────────────────────────────────────────────────────────

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
