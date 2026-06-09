-- Add metadata for campaign segmentation and scheduling
ALTER TABLE email_campaign
  ADD COLUMN segment_detail VARCHAR(255) NULL AFTER segment_json,
  ADD COLUMN scheduled_zone VARCHAR(64) NOT NULL DEFAULT 'UTC' AFTER scheduled_at,
  ADD COLUMN validation_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' AFTER scheduled_zone;
