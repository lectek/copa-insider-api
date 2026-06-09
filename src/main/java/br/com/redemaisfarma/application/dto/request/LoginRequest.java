/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonAlias
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonIgnoreProperties
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  com.fasterxml.jackson.annotation.JsonProperty$Access
 *  com.fasterxml.jackson.core.JsonParser
 *  com.fasterxml.jackson.core.JsonToken
 *  com.fasterxml.jackson.databind.DeserializationContext
 *  com.fasterxml.jackson.databind.JsonDeserializer
 *  com.fasterxml.jackson.databind.annotation.JsonDeserialize
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.media.Schema$AccessMode
 *  io.swagger.v3.oas.annotations.media.Schema$RequiredMode
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Schema(name="LoginRequest", description="Dados para autentica\u00e7\u00e3o de usu\u00e1rio")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class LoginRequest
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID \u00fanico da requisi\u00e7\u00e3o (UUID) gerado pelo servidor", example="3fa85f64-5717-4562-b3fc-2c963f66afa6", accessMode=Schema.AccessMode.READ_ONLY)
    @JsonProperty(value="requestId", access=JsonProperty.Access.READ_ONLY)
    private UUID requestId;
    @Schema(description="Identificador de login: e-mail, CPF ou username", example="admin@redemaisfarma.local", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{login.usuario.notBlank}")
    @Size(max=150, message="{login.usuario.size}")
    @Pattern(regexp="^(?:[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}|\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|[A-Za-z0-9._-]{3,150})$", message="{login.usuario.emailOrCpf.pattern}")
    @JsonProperty(value="usuario")
    @JsonAlias(value={"email", "username", "login", "identificador"})
    private @NotBlank(message="{login.usuario.notBlank}") @Size(max=150, message="{login.usuario.size}") @Pattern(regexp="^(?:[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}|\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|[A-Za-z0-9._-]{3,150})$", message="{login.usuario.emailOrCpf.pattern}") String usuario;
    @Schema(description="Senha do usu\u00e1rio", format="password", requiredMode=Schema.RequiredMode.REQUIRED, accessMode=Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message="{login.senha.notBlank}")
    @Size(min=8, max=128, message="{login.senha.size}")
    @Pattern(regexp="^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,128}$", message="{login.senha.pattern}")
    @JsonProperty(value="senha", access=JsonProperty.Access.WRITE_ONLY)
    @JsonAlias(value={"password"})
    private @NotBlank(message="{login.senha.notBlank}") @Size(min=8, max=128, message="{login.senha.size}") @Pattern(regexp="^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,128}$", message="{login.senha.pattern}") String senha;
    @Schema(description="Se deve lembrar sess\u00e3o (persistente)", example="false")
    @JsonProperty(value="lembrarMe")
    private Boolean lembrarMe = Boolean.FALSE;
    @Schema(description="C\u00f3digo OTP para multifator (6 d\u00edgitos)", example="123456")
    @Pattern(regexp="\\d{6}", message="{login.otp.pattern}")
    @JsonProperty(value="otpCode")
    private @Pattern(regexp="\\d{6}", message="{login.otp.pattern}") String otpCode;
    @Schema(description="Informa\u00e7\u00f5es do dispositivo de origem")
    @JsonProperty(value="deviceInfo")
    @JsonDeserialize(using=LoginRequestDeviceInfoDeserializer.class)
    private DeviceInfo deviceInfo;
    @Schema(description="Endere\u00e7o IP do cliente (IPv4 ou IPv6)", example="192.168.0.1")
    @Size(max=45, message="{login.ip.size}")
    @Pattern(regexp="^(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|([0-9a-fA-F:]+))$", message="{login.ip.pattern}")
    @JsonProperty(value="ipAddress")
    private @Size(max=45, message="{login.ip.size}") @Pattern(regexp="^(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|([0-9a-fA-F:]+))$", message="{login.ip.pattern}") String ipAddress;
    @Schema(description="Data/hora da requisi\u00e7\u00e3o (servidor preenche)", type="string", format="date-time", example="2025-07-04T12:00:00", accessMode=Schema.AccessMode.READ_ONLY)
    @PastOrPresent(message="{login.requestTime.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="requestTime", access=JsonProperty.Access.READ_ONLY)
    private @PastOrPresent(message="{login.requestTime.pastOrPresent}") LocalDateTime requestTime;
    @Schema(description="ID do tenant (multi-inquilino)", example="rede-mais-farma", requiredMode=Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{login.tenantId.notBlank}")
    @Size(max=100, message="{login.tenantId.size}")
    @JsonProperty(value="tenantId")
    private @NotBlank(message="{login.tenantId.notBlank}") @Size(max=100, message="{login.tenantId.size}") String tenantId = "rede-mais-farma";

    public LoginRequest() {
    }

    public LoginRequest(UUID requestId, String usuario, String senha, Boolean lembrarMe, String otpCode, DeviceInfo deviceInfo, String ipAddress, LocalDateTime requestTime, String tenantId) {
        this.requestId = requestId;
        this.usuario = usuario;
        this.senha = senha;
        this.lembrarMe = lembrarMe;
        this.otpCode = otpCode;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.requestTime = requestTime;
        this.tenantId = tenantId == null || tenantId.isBlank() ? "rede-mais-farma" : tenantId;
    }

    public UUID getRequestId() {
        return this.requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getUsuario() {
        return this.usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Boolean getLembrarMe() {
        return this.lembrarMe;
    }

    public void setLembrarMe(Boolean lembrarMe) {
        this.lembrarMe = lembrarMe;
    }

    public String getOtpCode() {
        return this.otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public DeviceInfo getDeviceInfo() {
        return this.deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getRequestTime() {
        return this.requestTime;
    }

    public void setRequestTime(LocalDateTime requestTime) {
        this.requestTime = requestTime;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId == null || tenantId.isBlank() ? "rede-mais-farma" : tenantId;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoginRequest)) {
            return false;
        }
        LoginRequest that = (LoginRequest)o;
        return Objects.equals(this.requestId, that.requestId) && Objects.equals(this.usuario, that.usuario) && Objects.equals(this.lembrarMe, that.lembrarMe) && Objects.equals(this.otpCode, that.otpCode) && Objects.equals(this.deviceInfo, that.deviceInfo) && Objects.equals(this.ipAddress, that.ipAddress) && Objects.equals(this.requestTime, that.requestTime) && Objects.equals(this.tenantId, that.tenantId);
    }

    public int hashCode() {
        return Objects.hash(this.requestId, this.usuario, this.lembrarMe, this.otpCode, this.deviceInfo, this.ipAddress, this.requestTime, this.tenantId);
    }

    public String toString() {
        return "LoginRequest{requestId=" + String.valueOf(this.requestId) + ", usuario='" + this.usuario + "', lembrarMe=" + this.lembrarMe + ", otpCode='******', deviceInfo=" + (this.deviceInfo == null ? null : "{deviceId=%s, deviceName=%s, deviceType=%s, os=%s, userAgent=%s}".formatted(this.deviceInfo.getDeviceId(), this.deviceInfo.getDeviceName(), this.deviceInfo.getDeviceType(), this.deviceInfo.getOs(), this.deviceInfo.getUserAgent())) + ", ipAddress='" + this.ipAddress + "', requestTime=" + String.valueOf(this.requestTime) + ", tenantId='" + this.tenantId + "'}";
    }

    @JsonInclude(value=JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown=true)
    @Schema(name="DeviceInfo", description="Metadados do dispositivo/cliente")
    public static class DeviceInfo
    implements Serializable {
        private static final long serialVersionUID = 1L;
        @Schema(example="dev-local")
        @Size(max=100)
        @JsonProperty(value="deviceId")
        private @Size(max=100) String deviceId;
        @Schema(example="PC-Alex")
        @Size(max=150)
        @JsonProperty(value="deviceName")
        private @Size(max=150) String deviceName;
        @Schema(example="DESKTOP")
        @Size(max=50)
        @JsonProperty(value="deviceType")
        private @Size(max=50) String deviceType;
        @Schema(example="Windows 11")
        @Size(max=100)
        @JsonProperty(value="os")
        private @Size(max=100) String os;
        @Schema(example="curl/Windows")
        @Size(max=200)
        @JsonProperty(value="userAgent")
        private @Size(max=200) String userAgent;

        public DeviceInfo() {
        }

        public DeviceInfo(String userAgent) {
            this.userAgent = userAgent;
        }

        public String getDeviceId() {
            return this.deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceName() {
            return this.deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDeviceType() {
            return this.deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public String getOs() {
            return this.os;
        }

        public void setOs(String os) {
            this.os = os;
        }

        public String getUserAgent() {
            return this.userAgent;
        }

        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }
    }

    public static class LoginRequestDeviceInfoDeserializer
    extends JsonDeserializer<DeviceInfo> {
        public DeviceInfo deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken t = p.currentToken();
            if (t == JsonToken.VALUE_STRING) {
                return new DeviceInfo(p.getValueAsString());
            }
            if (t == JsonToken.START_OBJECT) {
                return (DeviceInfo)p.readValueAs(DeviceInfo.class);
            }
            return null;
        }
    }
}
