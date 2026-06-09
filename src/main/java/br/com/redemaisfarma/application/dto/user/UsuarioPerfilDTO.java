// src/main/java/br/com/redemaisfarma/application/dto/user/UsuarioPerfilDTO.java
package br.com.redemaisfarma.application.dto.user;
import java.time.LocalDateTime;
public record UsuarioPerfilDTO(
        Long id, String nome, String email, String cpf,
        String papel, boolean ativo, LocalDateTime ultimoAcesso
) {}
