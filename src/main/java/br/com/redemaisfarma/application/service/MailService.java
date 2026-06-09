/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.lang.Nullable
 *  org.springframework.stereotype.Service
 *  org.springframework.web.util.HtmlUtils
 *  org.thymeleaf.context.Context
 *  org.thymeleaf.context.IContext
 *  org.thymeleaf.spring6.SpringTemplateEngine
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.email.adapter.MailSenderAdapter;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class MailService {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private final MailSenderAdapter adapter;
    private final SpringTemplateEngine templateEngine;

    public void sendHtml(String to, String subject, String htmlBody, @Nullable List<String> bcc) {
        log.debug("[mail] preparando envio HTML: '{}' -> {}", (Object)subject, (Object)to);
        this.adapter.send(to, subject, htmlBody, bcc);
    }

    public void sendText(String to, String subject, String text, @Nullable List<String> bcc) {
        String html = "<pre style=\"white-space:pre-wrap;font-family:inherit;\">" + HtmlUtils.htmlEscape((String)text) + "</pre>";
        this.sendHtml(to, subject, html, bcc);
    }

    public void sendTemplate(String to, String subject, String template, Map<String, Object> model, @Nullable List<String> bcc) {
        Context ctx = buildContext(model);
        String html = this.templateEngine.process(template, (IContext)ctx);
        this.sendHtml(to, subject, html, bcc);
    }

    public String renderTemplate(String template, Map<String, Object> model) {
        Context ctx = buildContext(model);
        return this.templateEngine.process(template, (IContext)ctx);
    }

    private Context buildContext(Map<String, Object> model) {
        Context ctx = new Context();
        if (model != null) {
            model.forEach(ctx::setVariable);
        }
        return ctx;
    }

    @Generated
    public MailService(MailSenderAdapter adapter, SpringTemplateEngine templateEngine) {
        this.adapter = adapter;
        this.templateEngine = templateEngine;
    }
}
