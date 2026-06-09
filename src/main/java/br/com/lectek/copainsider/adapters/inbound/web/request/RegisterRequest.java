package br.com.lectek.copainsider.adapters.inbound.web.request;

import br.com.lectek.copainsider.adapters.inbound.web.validator.annotation.SenhaForte;
import br.com.lectek.copainsider.application.validation.annotation.EmailUnico;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @EmailUnico
    String email,
    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
    @SenhaForte
    String password,
    String name,
    String cpf
) {
    public String cpfNormalizado() {
        return this.cpf == null ? null : this.cpf.replaceAll("\\D", "");
    }
}
