/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.mail.MessagingException
 *  jakarta.mail.internet.MimeMessage
 *  org.springframework.mail.javamail.JavaMailSender
 *  org.springframework.mail.javamail.MimeMessageHelper
 *  org.springframework.stereotype.Component
 */
package br.com.lectek.copainsider.application.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {
    private final JavaMailSender mail;

    public EmailSender(JavaMailSender mail) {
        this.mail = mail;
    }

    public String sendHtml(String to, String from, String subject, String html) {
        try {
            MimeMessage msg = this.mail.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, 3, StandardCharsets.UTF_8.name());
            h.setTo(to);
            h.setFrom(from);
            h.setSubject(subject);
            h.setText(html, true);
            this.mail.send(msg);
            String[] hdr = msg.getHeader("Message-ID");
            return hdr != null && hdr.length > 0 ? hdr[0] : UUID.randomUUID().toString();
        }
        catch (MessagingException ex) {
            throw new RuntimeException("mail_send_failed: " + ex.getMessage(), ex);
        }
    }
}

