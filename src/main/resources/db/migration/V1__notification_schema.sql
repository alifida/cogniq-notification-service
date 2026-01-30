-- Notification templates: HTML/Markdown content with placeholders (e.g. {{name}}, {{amount}})
CREATE TABLE IF NOT EXISTS notification_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(64) NOT NULL UNIQUE,
    subject VARCHAR(512) NOT NULL,
    body_html TEXT,
    body_plain TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS idx_notification_template_code ON notification_templates (code);

-- Notification log: history of sent alerts for dashboard inbox and audit
CREATE TABLE IF NOT EXISTS notification_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id UUID NOT NULL,
    recipient_id UUID NOT NULL,
    template_code VARCHAR(64) NOT NULL,
    channel VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    subject VARCHAR(512),
    body_preview VARCHAR(1024),
    sent_at TIMESTAMP,
    error_message VARCHAR(2048),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_notification_log_org_id ON notification_log (org_id);
CREATE INDEX IF NOT EXISTS idx_notification_log_recipient_id ON notification_log (recipient_id);
CREATE INDEX IF NOT EXISTS idx_notification_log_created_at ON notification_log (created_at);
