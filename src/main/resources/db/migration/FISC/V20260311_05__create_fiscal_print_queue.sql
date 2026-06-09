CREATE TABLE IF NOT EXISTS fiscal_print_station (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL,
  display_name VARCHAR(120) NOT NULL,
  printer_name VARCHAR(180) NULL,
  role VARCHAR(30) NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  last_heartbeat_at DATETIME(6) NULL,
  notes VARCHAR(255) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  UNIQUE KEY uk_fiscal_print_station_code (code),
  KEY idx_fiscal_print_station_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fiscal_print_job (
  id BIGINT NOT NULL AUTO_INCREMENT,
  fiscal_document_id BIGINT NOT NULL,
  pedido_id BIGINT NOT NULL,
  station_id BIGINT NULL,
  job_type VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL,
  print_channel VARCHAR(30) NOT NULL,
  priority INT NOT NULL DEFAULT 50,
  copies INT NOT NULL DEFAULT 1,
  source VARCHAR(40) NULL,
  scheduled_for DATETIME(6) NULL,
  started_at DATETIME(6) NULL,
  completed_at DATETIME(6) NULL,
  cancelled_at DATETIME(6) NULL,
  reprint_of_job_id BIGINT NULL,
  created_by VARCHAR(80) NULL,
  last_actor VARCHAR(80) NULL,
  cancel_reason VARCHAR(255) NULL,
  error_message VARCHAR(500) NULL,
  created_at DATETIME(6) NOT NULL,
  updated_at DATETIME(6) NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_fiscal_print_job_document (fiscal_document_id),
  KEY idx_fiscal_print_job_pedido (pedido_id),
  KEY idx_fiscal_print_job_status (status),
  KEY idx_fiscal_print_job_station (station_id),
  KEY idx_fiscal_print_job_type (job_type),
  CONSTRAINT fk_fiscal_print_job_document
    FOREIGN KEY (fiscal_document_id) REFERENCES fiscal_document (id),
  CONSTRAINT fk_fiscal_print_job_pedido
    FOREIGN KEY (pedido_id) REFERENCES pedido (id),
  CONSTRAINT fk_fiscal_print_job_station
    FOREIGN KEY (station_id) REFERENCES fiscal_print_station (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS fiscal_print_job_event (
  id BIGINT NOT NULL AUTO_INCREMENT,
  print_job_id BIGINT NOT NULL,
  event_type VARCHAR(30) NOT NULL,
  status_before VARCHAR(30) NULL,
  status_after VARCHAR(30) NULL,
  message VARCHAR(255) NULL,
  actor VARCHAR(80) NULL,
  created_at DATETIME(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_fiscal_print_job_event_job (print_job_id),
  KEY idx_fiscal_print_job_event_type (event_type),
  CONSTRAINT fk_fiscal_print_job_event_job
    FOREIGN KEY (print_job_id) REFERENCES fiscal_print_job (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
