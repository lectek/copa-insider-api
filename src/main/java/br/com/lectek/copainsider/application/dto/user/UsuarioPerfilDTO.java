// src/main/java/br/com/lectek/copainsider/application/dto/user/UsuarioPerfilDTO.java
package br.com.lectek.copainsider.application.dto.user;
import java.time.LocalDateTime;
public record UsuarioPerfilDTO(
        Long id, String nome, String email, String cpf,
        String papel, boolean ativo, LocalDateTime ultimoAcesso
) {}
