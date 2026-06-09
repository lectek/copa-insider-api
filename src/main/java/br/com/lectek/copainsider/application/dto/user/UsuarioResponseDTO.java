// src/main/java/br/com/lectek/copainsider/application/dto/user/UsuarioResponseDTO.java
package br.com.lectek.copainsider.application.dto.user;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String cpf,
        String papel,   // derivado de roles (um “principal”)
        boolean ativo
) {}
