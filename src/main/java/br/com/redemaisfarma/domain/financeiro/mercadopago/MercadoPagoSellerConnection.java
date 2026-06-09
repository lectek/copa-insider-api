package br.com.redemaisfarma.domain.financeiro.mercadopago;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(
        name = "mercadopago_seller_connection",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_mp_seller_connection_owner_reference",
                    columnNames = "owner_reference"
            )
        },
        indexes = {
            @Index(
                    name = "idx_mp_seller_connection_status",
                    columnList = "status"
            ),
            @Index(
                    name = "idx_mp_seller_connection_seller_user_id",
                    columnList = "seller_user_id"
            )
        }
)
public class MercadoPagoSellerConnection implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_reference", nullable = false, length = 120)
    private String ownerReference;

    @Column(name = "seller_user_id", nullable = false, length = 80)
    private String sellerUserId;

    @Column(name = "seller_nickname", length = 120)
    private String sellerNickname;

    @Column(name = "access_token", nullable = false, length = 512)
    private String accessToken;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Column(name = "public_key", length = 255)
    private String publicKey;

    @Column(name = "token_type", length = 40)
    private String tokenType;

    @Column(name = "scope", length = 255)
    private String scope;

    @Column(name = "live_mode", nullable = false)
    private boolean liveMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MercadoPagoConnectionStatus status =
            MercadoPagoConnectionStatus.CONNECTED;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @Column(name = "connected_at", nullable = false)
    private OffsetDateTime connectedAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "last_webhook_received_at")
    private OffsetDateTime lastWebhookReceivedAt;

    @Column(name = "last_webhook_payment_id", length = 80)
    private String lastWebhookPaymentId;

    @Lob
    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(final Long idValue) {
        id = idValue;
    }

    public String getOwnerReference() {
        return ownerReference;
    }

    public void setOwnerReference(final String ownerReferenceValue) {
        ownerReference = ownerReferenceValue;
    }

    public String getSellerUserId() {
        return sellerUserId;
    }

    public void setSellerUserId(final String sellerUserIdValue) {
        sellerUserId = sellerUserIdValue;
    }

    public String getSellerNickname() {
        return sellerNickname;
    }

    public void setSellerNickname(final String sellerNicknameValue) {
        sellerNickname = sellerNicknameValue;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(final String accessTokenValue) {
        accessToken = accessTokenValue;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(final String refreshTokenValue) {
        refreshToken = refreshTokenValue;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(final String publicKeyValue) {
        publicKey = publicKeyValue;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(final String tokenTypeValue) {
        tokenType = tokenTypeValue;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(final String scopeValue) {
        scope = scopeValue;
    }

    public boolean isLiveMode() {
        return liveMode;
    }

    public void setLiveMode(final boolean liveModeValue) {
        liveMode = liveModeValue;
    }

    public MercadoPagoConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(final MercadoPagoConnectionStatus statusValue) {
        status = statusValue;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(final OffsetDateTime expiresAtValue) {
        expiresAt = expiresAtValue;
    }

    public OffsetDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(final OffsetDateTime connectedAtValue) {
        connectedAt = connectedAtValue;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(final OffsetDateTime revokedAtValue) {
        revokedAt = revokedAtValue;
    }

    public OffsetDateTime getLastWebhookReceivedAt() {
        return lastWebhookReceivedAt;
    }

    public void setLastWebhookReceivedAt(
            final OffsetDateTime lastWebhookReceivedAtValue
    ) {
        lastWebhookReceivedAt = lastWebhookReceivedAtValue;
    }

    public String getLastWebhookPaymentId() {
        return lastWebhookPaymentId;
    }

    public void setLastWebhookPaymentId(final String lastWebhookPaymentIdValue) {
        lastWebhookPaymentId = lastWebhookPaymentIdValue;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(final String rawResponseValue) {
        rawResponse = rawResponseValue;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MercadoPagoSellerConnection that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
