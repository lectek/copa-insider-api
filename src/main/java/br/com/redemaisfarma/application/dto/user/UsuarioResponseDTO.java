// src/main/java/br/com/redemaisfarma/application/dto/user/UsuarioResponseDTO.java
package br.com.redemaisfarma.application.dto.user;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String cpf,
        String papel,   // derivado de roles (um “principal”)
        boolean ativo
) {}
