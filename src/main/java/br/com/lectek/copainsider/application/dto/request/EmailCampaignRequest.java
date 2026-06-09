package br.com.lectek.copainsider.application.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EmailCampaignRequest {
    private String nome;
    private String assunto;
    private String templateKey;
    private String segmento;
    private String segmentoDetalhado;
    private String categoria;
    private Integer recenciaDias;
    private BigDecimal ticketMinimo;
    private Boolean envioImediato;
    private LocalDateTime agendarPara;
    private String agendarTimezone;
    private String validationStatus;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public String getSegmento() {
        return segmento;
    }

    public void setSegmento(String segmento) {
        this.segmento = segmento;
    }

    public String getSegmentoDetalhado() {
        return segmentoDetalhado;
    }

    public void setSegmentoDetalhado(String segmentoDetalhado) {
        this.segmentoDetalhado = segmentoDetalhado;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getRecenciaDias() {
        return recenciaDias;
    }

    public void setRecenciaDias(Integer recenciaDias) {
        this.recenciaDias = recenciaDias;
    }

    public BigDecimal getTicketMinimo() {
        return ticketMinimo;
    }

    public void setTicketMinimo(BigDecimal ticketMinimo) {
        this.ticketMinimo = ticketMinimo;
    }

    public Boolean getEnvioImediato() {
        return envioImediato;
    }

    public void setEnvioImediato(Boolean envioImediato) {
        this.envioImediato = envioImediato;
    }

    public LocalDateTime getAgendarPara() {
        return agendarPara;
    }

    public void setAgendarPara(LocalDateTime agendarPara) {
        this.agendarPara = agendarPara;
    }

    public String getAgendarTimezone() {
        return agendarTimezone;
    }

    public void setAgendarTimezone(String agendarTimezone) {
        this.agendarTimezone = agendarTimezone;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }
}
