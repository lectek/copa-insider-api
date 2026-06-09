ALTER TABLE fiscal_print_station
  ADD COLUMN api_key_hash VARCHAR(255) NULL AFTER notes,
  ADD COLUMN api_key_last_rotated_at DATETIME(6) NULL AFTER api_key_hash;

CREATE INDEX idx_fiscal_print_station_api_key_rotated
  ON fiscal_print_station (api_key_last_rotated_at);
