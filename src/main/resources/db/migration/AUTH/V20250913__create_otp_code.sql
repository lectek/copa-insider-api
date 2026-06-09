CREATE TABLE IF NOT EXISTS otp_code (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  delivery_id VARCHAR(128) NOT NULL UNIQUE,
  destination VARCHAR(255) NOT NULL,
  code_hash VARCHAR(255) NOT NULL,
  salt VARCHAR(64) NOT NULL,
  ttl_seconds INT NOT NULL,
  attempts INT NOT NULL DEFAULT 0,
  max_attempts INT NOT NULL DEFAULT 5,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING|VERIFIED|EXPIRED|BLOCKED
  INDEX idx_otp_destination (destination),
  INDEX idx_otp_expires (expires_at)
);
