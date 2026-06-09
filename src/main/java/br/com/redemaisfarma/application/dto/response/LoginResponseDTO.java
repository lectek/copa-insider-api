/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.Future
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotEmpty
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name="LoginResponseDTO", description="Dados retornados ap\u00f3s login bem-sucedido")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class LoginResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="Token de acesso JWT", example="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{loginResponse.accessToken.notBlank}")
    @JsonProperty(value="accessToken")
    private @NotBlank(message="{loginResponse.accessToken.notBlank}") String accessToken;
    @Schema(description="Token de refresh JWT", example="dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{loginResponse.refreshToken.notBlank}")
    @JsonProperty(value="refreshToken")
    private @NotBlank(message="{loginResponse.refreshToken.notBlank}") String refreshToken;
    @Schema(description="Tipo de usu\u00e1rio", example="CLIENTE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues={"CLIENTE", "ATENDENTE", "ADMIN"})
    @NotNull(message="{loginResponse.userType.notNull}")
    @JsonProperty(value="userType")
    private @NotNull(message="{loginResponse.userType.notNull}") UserType userType;
    @Schema(description="ID do usu\u00e1rio autenticado", example="12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{loginResponse.userId.notNull}")
    @JsonProperty(value="userId")
    private @NotNull(message="{loginResponse.userId.notNull}") Long userId;
    @Schema(description="Nome completo do usu\u00e1rio", example="Jo\u00e3o da Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{loginResponse.fullName.notBlank}")
    @Size(max=100, message="{loginResponse.fullName.size}")
    @JsonProperty(value="fullName")
    private @NotBlank(message="{loginResponse.fullName.notBlank}") @Size(max=100, message="{loginResponse.fullName.size}") String fullName;
    @Schema(description="E-mail do usu\u00e1rio", example="joao@redemaisfarma.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{loginResponse.email.notBlank}")
    @Email(message="{loginResponse.email.valid}")
    @JsonProperty(value="email")
    private @NotBlank(message="{loginResponse.email.notBlank}") @Email(message="{loginResponse.email.valid}") String email;
    @Schema(description="Lista de permiss\u00f5es do usu\u00e1rio", example="[\"READ_ORDERS\",\"WRITE_ORDERS\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message="{loginResponse.permissions.notEmpty}")
    @JsonProperty(value="permissions")
    private @NotEmpty(message="{loginResponse.permissions.notEmpty}") List<@NotBlank(message="{loginResponse.permission.notBlank}") String> permissions;
    @Schema(description="Timestamp de expira\u00e7\u00e3o do token de acesso", type="string", format="date-time", example="2025-07-04T15:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{loginResponse.expiresAt.notNull}")
    @Future(message="{loginResponse.expiresAt.future}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="expiresAt")
    private @NotNull(message="{loginResponse.expiresAt.notNull}") @Future(message="{loginResponse.expiresAt.future}") LocalDateTime expiresAt;
    @Schema(description="Timestamp do \u00faltimo login", type="string", format="date-time", example="2025-07-03T18:45:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="lastLoginAt")
    private LocalDateTime lastLoginAt;
    @Schema(description="Status da conta do usu\u00e1rio", example="ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues={"ACTIVE", "SUSPENDED", "INACTIVE"})
    @NotNull(message="{loginResponse.accountStatus.notNull}")
    @JsonProperty(value="accountStatus")
    private @NotNull(message="{loginResponse.accountStatus.notNull}") AccountStatus accountStatus;
    @Schema(description="Mensagem de boas-vindas", example="Bem-vindo \u00e0 RedeMaisFarma, Jo\u00e3o!")
    @JsonProperty(value="welcomeMessage")
    private String welcomeMessage;
    @Schema(description="ID do tenant (multi-inquilino)", example="redemaisfarma-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{loginResponse.tenantId.notBlank}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{loginResponse.tenantId.notBlank}") String tenantId;
    @Schema(description="Token de rastreamento (UUID)", example="6fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String accessToken, String refreshToken, UserType userType, Long userId, String fullName, String email, List<String> permissions, LocalDateTime expiresAt, LocalDateTime lastLoginAt, AccountStatus accountStatus, String welcomeMessage, String tenantId, UUID traceId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userType = userType;
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.permissions = permissions;
        this.expiresAt = expiresAt;
        this.lastLoginAt = lastLoginAt;
        this.accountStatus = accountStatus;
        this.welcomeMessage = welcomeMessage;
        this.tenantId = tenantId;
        this.traceId = traceId;
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

    public UserType getUserType() {
        return this.userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getLastLoginAt() {
        return this.lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public AccountStatus getAccountStatus() {
        return this.accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getWelcomeMessage() {
        return this.welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoginResponseDTO)) {
            return false;
        }
        LoginResponseDTO that = (LoginResponseDTO)o;
        return Objects.equals(this.accessToken, that.accessToken) && Objects.equals(this.refreshToken, that.refreshToken) && this.userType == that.userType && Objects.equals(this.userId, that.userId) && Objects.equals(this.fullName, that.fullName) && Objects.equals(this.email, that.email) && Objects.equals(this.permissions, that.permissions) && Objects.equals(this.expiresAt, that.expiresAt) && Objects.equals(this.lastLoginAt, that.lastLoginAt) && this.accountStatus == that.accountStatus && Objects.equals(this.welcomeMessage, that.welcomeMessage) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.accessToken, this.refreshToken, this.userType, this.userId, this.fullName, this.email, this.permissions, this.expiresAt, this.lastLoginAt, this.accountStatus, this.welcomeMessage, this.tenantId, this.traceId});
    }

    public String toString() {
        return "LoginResponseDTO{accessToken='[PROTECTED]', refreshToken='[PROTECTED]', userType=" + String.valueOf((Object)this.userType) + ", userId=" + this.userId + ", fullName='" + this.fullName + "', email='" + this.email + "', permissions=" + String.valueOf(this.permissions) + ", expiresAt=" + String.valueOf(this.expiresAt) + ", lastLoginAt=" + String.valueOf(this.lastLoginAt) + ", accountStatus=" + String.valueOf((Object)this.accountStatus) + ", welcomeMessage='" + this.welcomeMessage + "', tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + "}";
    }

    public static enum UserType {
        CLIENTE,
        ATENDENTE,
        ADMIN;

    }

    public static enum AccountStatus {
        ACTIVE,
        SUSPENDED,
        INACTIVE;

    }
}


