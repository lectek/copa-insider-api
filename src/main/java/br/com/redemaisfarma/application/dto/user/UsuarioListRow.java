// src/main/java/br/com/redemaisfarma/application/dto/user/UsuarioListRow.java
package br.com.redemaisfarma.application.dto.user;
public record UsuarioListRow(Long id, String nome, String email, String papel, boolean ativo) {}
