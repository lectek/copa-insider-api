/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.mail.internet.InternetAddress
 *  jakarta.mail.internet.MimeMessage
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.lang.Nullable
 *  org.springframework.mail.javamail.JavaMailSender
 *  org.springframework.mail.javamail.MimeMessageHelper
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.adapters.outbound.email.adapter.impl;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import br.com.redemaisfarma.adapters.outbound.email.config.AppMailProperties;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SmtpMailSenderAdapter
implements MailSenderAdapter {
    private static final Logger log = LoggerFactory.getLogger(SmtpMailSenderAdapter.class);
    private final JavaMailSender mailSender;
    private final AppMailProperties props;

    public SmtpMailSenderAdapter(JavaMailSender mailSender, AppMailProperties props) {
        this.mailSender = mailSender;
        this.props = props;
    }

    @Override
    public String send(String to, String subject, String htmlBody, @Nullable List<String> bcc) {
        if (!this.props.isEnabled()) {
            log.info("[mail] envio DESLIGADO (app.mail.enabled=false). '{}' -> {}", (Object)subject, (Object)to);
            return "noop-disabled";
        }
        try {
            MimeMessage msg = this.mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, 3, StandardCharsets.UTF_8.name());
            helper.setValidateAddresses(true);
            helper.setTo(to);
            this.setFromSmart(helper, this.props.getFrom());
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            msg.setSentDate(new Date());
            ArrayList<String> allBcc = new ArrayList<String>();
            if (bcc != null && !bcc.isEmpty()) {
                allBcc.addAll(bcc);
            }
            if (this.props.getBcc() != null && !this.props.getBcc().isEmpty()) {
                allBcc.addAll(this.props.getBcc());
            }
            if (!allBcc.isEmpty()) {
                helper.setBcc(allBcc.toArray(new String[0]));
            }
            this.mailSender.send(msg);
            return msg.getMessageID() != null ? msg.getMessageID() : "<none>";
        }
        catch (Exception e) {
            log.error("[mail] falha ao enviar '{}'", (Object)subject, (Object)e);
            throw new RuntimeException("Falha ao enviar e-mail", e);
        }
    }

    private void setFromSmart(MimeMessageHelper helper, String fromRaw) {
        try {
            InternetAddress[] parsed = InternetAddress.parse((String)fromRaw, (boolean)true);
            if (parsed.length > 0) {
                InternetAddress ia = parsed[0];
                if (ia.getPersonal() != null && !ia.getPersonal().isBlank()) {
                    helper.setFrom(ia.getAddress(), ia.getPersonal());
                } else {
                    helper.setFrom(ia.getAddress());
                }
                return;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            helper.setFrom(fromRaw);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

