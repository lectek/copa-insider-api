package br.com.lectek.copainsider.application.dto.response;

public class AlertItemDTO {
    private String tipo;
    private String mensagem;

    public AlertItemDTO() {
    }

    public AlertItemDTO(String tipo, String mensagem) {
        this.tipo = tipo;
        this.mensagem = mensagem;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
