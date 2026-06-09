/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name="AuthResponse", description="Dados retornados ap\u00f3s autentica\u00e7\u00e3o")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class AuthResponse
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="Token de acesso JWT", example="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty(value="accessToken")
    private String accessToken;
    @Schema(description="Token de refresh JWT (pode n\u00e3o ser retornado)", example="dGhpcyBpcyBhIHJlZnJlc2g...")
    @JsonProperty(value="refreshToken")
    private String refreshToken;
    @Schema(description="ID do usu\u00e1rio autenticado", example="42")
    @JsonProperty(value="userId")
    private Long userId;
    @Schema(description="Username do usu\u00e1rio", example="joao.silva")
    @JsonProperty(value="username")
    private String username;
    @Schema(description="E-mail do usu\u00e1rio", example="joao.silva@farmacia.com")
    @JsonProperty(value="email")
    private String email;
    @Schema(description="Pap\u00e9is/permiss\u00f5es do usu\u00e1rio", example="[\"ROLE_USER\", \"ROLE_ADMIN\"]")
    @JsonProperty(value="roles")
    private List<String> roles;
    @Schema(description="Expira\u00e7\u00e3o do accessToken", type="string", format="date-time", example="2025-07-04T15:30:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="expiresAt")
    private LocalDateTime expiresAt;
    @Schema(description="ID do tenant (multi-inquilino)", example="redemaisfarma-001")
    @JsonProperty(value="tenantId")
    private String tenantId;
    @Schema(description="Token de correla\u00e7\u00e3o (UUID)", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @Schema(description="Timestamp de emiss\u00e3o do token", type="string", format="date-time", example="2025-07-04T14:00:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="issuedAt")
    private LocalDateTime issuedAt;

    public AuthResponse() {
    }

    public AuthResponse(String accessToken, String refreshToken, Long userId, String username, List<String> roles, LocalDateTime expiresAt, String tenantId, UUID traceId, LocalDateTime issuedAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.roles = roles;
        this.expiresAt = expiresAt;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.issuedAt = issuedAt;
        this.email = null;
    }

    public AuthResponse(String accessToken, String refreshToken, Long userId, String username, String email, List<String> roles, LocalDateTime expiresAt, String tenantId, UUID traceId, LocalDateTime issuedAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.expiresAt = expiresAt;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.issuedAt = issuedAt;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return this.roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTraceId() {
        return this.traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public LocalDateTime getIssuedAt() {
        return this.issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthResponse)) {
            return false;
        }
        AuthResponse that = (AuthResponse)o;
        return Objects.equals(this.accessToken, that.accessToken) && Objects.equals(this.refreshToken, that.refreshToken) && Objects.equals(this.userId, that.userId) && Objects.equals(this.username, that.username) && Objects.equals(this.email, that.email) && Objects.equals(this.roles, that.roles) && Objects.equals(this.expiresAt, that.expiresAt) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.issuedAt, that.issuedAt);
    }

    public int hashCode() {
        return Objects.hash(this.accessToken, this.refreshToken, this.userId, this.username, this.email, this.roles, this.expiresAt, this.tenantId, this.traceId, this.issuedAt);
    }

    public String toString() {
        return "AuthResponse{accessToken='[PROTECTED]', userId=" + this.userId + ", username='" + this.username + "', email='" + this.email + "', roles=" + String.valueOf(this.roles) + ", expiresAt=" + String.valueOf(this.expiresAt) + ", tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + ", issuedAt=" + String.valueOf(this.issuedAt) + "}";
    }
}

