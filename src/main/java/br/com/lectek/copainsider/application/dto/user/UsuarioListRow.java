// src/main/java/br/com/lectek/copainsider/application/dto/user/UsuarioListRow.java
package br.com.lectek.copainsider.application.dto.user;
public record UsuarioListRow(Long id, String nome, String email, String papel, boolean ativo) {}
