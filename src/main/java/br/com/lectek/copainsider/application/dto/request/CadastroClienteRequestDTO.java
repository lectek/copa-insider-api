package br.com.lectek.copainsider.application.dto.request;

import java.time.LocalDate;

public class CadastroClienteRequestDTO {

    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private LocalDate dataDeNascimento;
    private String senha;
    private String confirmarSenha;
    private String canalOtp;
    private String otpDeliveryId;
    private String otpToken;
    private String website;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDate getDataDeNascimento() {
        return dataDeNascimento;
    }

    public void setDataDeNascimento(LocalDate dataDeNascimento) {
        this.dataDeNascimento = dataDeNascimento;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmarSenha() {
        return confirmarSenha;
    }

    public void setConfirmarSenha(String confirmarSenha) {
        this.confirmarSenha = confirmarSenha;
    }

    public String getCanalOtp() {
        return canalOtp;
    }

    public void setCanalOtp(String canalOtp) {
        this.canalOtp = canalOtp;
    }

    public String getOtpDeliveryId() {
        return otpDeliveryId;
    }

    public void setOtpDeliveryId(String otpDeliveryId) {
        this.otpDeliveryId = otpDeliveryId;
    }

    public String getOtpToken() {
        return otpToken;
    }

    public void setOtpToken(String otpToken) {
        this.otpToken = otpToken;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
