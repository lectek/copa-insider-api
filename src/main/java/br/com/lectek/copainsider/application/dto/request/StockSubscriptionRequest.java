package br.com.lectek.copainsider.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class StockSubscriptionRequest {
    @Email(message = "{stockSubscription.email.valid}")
    @NotBlank(message = "{stockSubscription.email.notBlank}")
    private String email;

    private String nome;

    public StockSubscriptionRequest() {
    }

    public StockSubscriptionRequest(String email, String nome) {
        this.email = email;
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
