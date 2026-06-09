CREATE TABLE IF NOT EXISTS mercadopago_seller_connection (
  id                BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  owner_reference   VARCHAR(120) NOT NULL,
  seller_user_id    VARCHAR(80)  NOT NULL,
  seller_nickname   VARCHAR(120) NULL,
  access_token      VARCHAR(512) NOT NULL,
  refresh_token     VARCHAR(512) NULL,
  public_key        VARCHAR(255) NULL,
  token_type        VARCHAR(40)  NULL,
  scope             VARCHAR(255) NULL,
  live_mode         TINYINT(1)   NOT NULL DEFAULT 0,
  status            VARCHAR(30)  NOT NULL,
  expires_at        TIMESTAMP    NULL,
  connected_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  revoked_at        TIMESTAMP    NULL,
  raw_response      TEXT         NULL,
  created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_mp_seller_connection_owner_reference (owner_reference),
  KEY idx_mp_seller_connection_status (status),
  KEY idx_mp_seller_connection_seller_user_id (seller_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
