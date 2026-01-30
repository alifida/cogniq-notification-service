-- Seed common templates (placeholders: {{name}}, {{amount}}, {{credits}}, {{link}}, etc.)
INSERT INTO notification_templates (id, code, subject, body_html, body_plain, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'VERIFICATION_CODE', 'Your CogniQ verification code', '<p>Hi {{name}},</p><p>Your verification code is: <strong>{{code}}</strong></p><p>Valid for 10 minutes.</p>', 'Hi {{name}}, Your verification code is: {{code}}. Valid for 10 minutes.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'PASSWORD_RESET', 'Reset your CogniQ password', '<p>Hi {{name}},</p><p><a href="{{link}}">Click here to reset your password</a>. Link expires in 1 hour.</p>', 'Hi {{name}}, Reset your password: {{link}}. Link expires in 1 hour.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'BILLING_SUCCESS', 'Payment received – CogniQ', '<p>Hi {{name}},</p><p>We received your payment of {{amount}}. Credits added: {{credits}}.</p><p>Thank you for your subscription.</p>', 'Hi {{name}}, We received your payment of {{amount}}. Credits added: {{credits}}. Thank you.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'CREDIT_LOW', 'Credits running low', '<p>Hi {{name}},</p><p>Your credit balance is below 10%. Consider topping up to avoid interruption.</p>', 'Hi {{name}}, Your credit balance is below 10%. Consider topping up.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'TRAINING_STARTED', 'Training started', '<p>ML model training has been initiated for your dataset.</p>', 'ML model training has been initiated.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'TRAINING_FINISHED', 'Model ready', '<p>Hi {{name}},</p><p>Your model is ready. View accuracy metrics in the dashboard.</p>', 'Hi {{name}}, Your model is ready. View accuracy metrics in the dashboard.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), 'LARGE_CSV_PROCESSED', 'Large file ready', '<p>Your file ({{fileName}}) has been processed and is ready for ML.</p>', 'Your file ({{fileName}}) has been processed and is ready for ML.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;
