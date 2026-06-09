/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.mail.Session
 *  jakarta.mail.internet.MimeMessage
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.lang.NonNull
 *  org.springframework.lang.Nullable
 *  org.springframework.mail.MailException
 *  org.springframework.mail.SimpleMailMessage
 *  org.springframework.mail.javamail.JavaMailSender
 */
package br.com.redemaisfarma.config;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@ConditionalOnProperty(prefix="app.mail", name={"noop"}, havingValue="true")
public class MailSupportConfig {
    @Bean
    @ConditionalOnMissingBean(value={JavaMailSender.class})
    public JavaMailSender noOpJavaMailSender() {
        return new NoOpJavaMailSender();
    }

    static class NoOpJavaMailSender
    implements JavaMailSender {
        private static final Logger log = LoggerFactory.getLogger(NoOpJavaMailSender.class);

        NoOpJavaMailSender() {
        }

        @NonNull
        public MimeMessage createMimeMessage() {
            return new MimeMessage(Session.getInstance((Properties)new Properties()));
        }

        @NonNull
        public MimeMessage createMimeMessage(@NonNull InputStream contentStream) throws MailException {
            try {
                return new MimeMessage(Session.getInstance((Properties)new Properties()), contentStream);
            }
            catch (Exception e) {
                throw new MailException("Falha ao criar MimeMessage (no-op) a partir do InputStream", e){
                    private static final long serialVersionUID = 1L;
                };
            }
        }

        public void send(@NonNull MimeMessage mimeMessage) throws MailException {
            log.debug("[MAIL NO-OP] MimeMessage suprimida (teste/dev). Assunto: {}", (Object)this.safeSubject(mimeMessage));
        }

        public void send(MimeMessage ... mimeMessages) throws MailException {
            for (MimeMessage m : mimeMessages) {
                this.send(m);
            }
        }

        public void send(@NonNull SimpleMailMessage simpleMessage) throws MailException {
            log.debug("[MAIL NO-OP] SimpleMailMessage suprimida (teste/dev). Assunto: {}", (Object)(simpleMessage != null ? simpleMessage.getSubject() : "<null>"));
        }

        public void send(SimpleMailMessage ... simpleMessages) throws MailException {
            for (SimpleMailMessage m : simpleMessages) {
                this.send(m);
            }
        }

        private String safeSubject(@Nullable MimeMessage msg) {
            if (msg == null) {
                return "<null>";
            }
            try {
                return msg.getSubject();
            }
            catch (Exception e) {
                return "<desconhecido>";
            }
        }
    }
}

