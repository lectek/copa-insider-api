CREATE TABLE IF NOT EXISTS product_image_job (
  id BIGINT NOT NULL AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL,           -- QUEUED|RUNNING|DONE|ERROR|SKIPPED
  result_url VARCHAR(512) NULL,
  error_msg VARCHAR(512) NULL,
  fingerprint VARCHAR(128) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_product_status (product_id, status),
  KEY idx_created (created_at)
);
