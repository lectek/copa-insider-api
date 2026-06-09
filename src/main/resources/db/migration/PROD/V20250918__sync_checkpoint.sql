CREATE TABLE IF NOT EXISTS sync_checkpoint (
  id          VARCHAR(64) PRIMARY KEY,
  last_update DATETIME(6) NULL,
  updated_at  DATETIME(6) NOT NULL
               DEFAULT CURRENT_TIMESTAMP(6)
               ON UPDATE CURRENT_TIMESTAMP(6)
);

INSERT IGNORE INTO sync_checkpoint (id, last_update)
VALUES ('firebird_produtos', NULL);
