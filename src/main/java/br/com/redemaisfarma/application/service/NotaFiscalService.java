/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.lowagie.text.Document
 *  com.lowagie.text.Element
 *  com.lowagie.text.Font
 *  com.lowagie.text.PageSize
 *  com.lowagie.text.Paragraph
 *  com.lowagie.text.pdf.PdfWriter
 *  jakarta.mail.internet.MimeMessage
 *  org.springframework.core.io.ByteArrayResource
 *  org.springframework.core.io.InputStreamSource
 *  org.springframework.mail.javamail.JavaMailSender
 *  org.springframework.mail.javamail.MimeMessageHelper
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.NotaFiscalConfirmacaoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.NotaFiscalConfirmacaoRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotaFiscalService {
    private final NotaFiscalConfirmacaoRepository repo;
    private final JavaMailSender mail;

    public NotaFiscalService(NotaFiscalConfirmacaoRepository repo, JavaMailSender mail) {
        this.repo = repo;
        this.mail = mail;
    }

    @Transactional
    public Long confirmar(String nome, String preferencia, String email) {
        byte[] pdf = NotaFiscalService.gerarPdf(nome, preferencia, email);
        NotaFiscalConfirmacaoEntity e = new NotaFiscalConfirmacaoEntity();
        e.setNome(nome);
        e.setPreferencia(preferencia);
        e.setEmail(email);
        e.setPdf(pdf);
        e = (NotaFiscalConfirmacaoEntity)this.repo.save(e);
        this.enviarEmail(email, "Confirma\u00e7\u00e3o de Nota Fiscal", "Ol\u00e1 " + nome + ", segue sua confirma\u00e7\u00e3o em anexo.", pdf, "confirmacao-nota-fiscal.pdf");
        return e.getId();
    }

    private static byte[] gerarPdf(String nome, String preferencia, String email) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 36.0f, 36.0f, 36.0f, 36.0f);
            PdfWriter.getInstance((Document)doc, (OutputStream)out);
            doc.open();
            Font h1 = new Font(1, 16.0f, 1);
            Font p = new Font(1, 12.0f);
            doc.add((Element)new Paragraph("Confirma\u00e7\u00e3o de Nota Fiscal", h1));
            doc.add((Element)new Paragraph(" ", p));
            doc.add((Element)new Paragraph("Nome: " + nome, p));
            doc.add((Element)new Paragraph("Prefer\u00eancia: " + preferencia, p));
            doc.add((Element)new Paragraph("E-mail: " + email, p));
            doc.add((Element)new Paragraph("Gerado automaticamente pelo sistema.", p));
            doc.close();
            return out.toByteArray();
        }
        catch (Exception ex) {
            throw new IllegalStateException("Falha ao gerar PDF", ex);
        }
    }

    private void enviarEmail(String to, String subject, String text, byte[] attachment, String filename) {
        try {
            MimeMessage msg = this.mail.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            helper.addAttachment(filename, (InputStreamSource)new ByteArrayResource(attachment));
            this.mail.send(msg);
        }
        catch (Exception e) {
            throw new IllegalStateException("Falha ao enviar e-mail", e);
        }
    }
}

