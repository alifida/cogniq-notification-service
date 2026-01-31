ALTER TABLE notification_log ADD COLUMN IF NOT EXISTS read_at TIMESTAMP;
CREATE INDEX IF NOT EXISTS idx_notification_log_read_at ON notification_log (recipient_id, read_at) WHERE read_at IS NULL;
