INSERT INTO config(name, code, value, created_by, created_at, updated_by, updated_at)
VALUES ('Email Sender Name', 'EMAIL_SENDER_NAME', 'iSLDevs', 'system', now(), 'system', now());

INSERT INTO config(name, code, value, created_by, created_at, updated_by, updated_at)
VALUES ('Email Host', 'EMAIL_HOST', 'smtp.gmail.com', 'system', now(), 'system', now());

INSERT INTO config(name, code, value, created_by, created_at, updated_by, updated_at)
VALUES ('Email Port', 'EMAIL_PORT', '587', 'system', now(), 'system', now());

INSERT INTO config(name, code, value, created_by, created_at, updated_by, updated_at)
VALUES ('Email Username (Gmail)', 'EMAIL_USERNAME', 'sender@gmail.com', 'system', now(), 'system', now());

INSERT INTO config(name, code, value, created_by, created_at, updated_by, updated_at)
VALUES ('Email App Password (16-char Gmail App Password)', 'EMAIL_APP_PASSWORD', 'sender_password', 'system', now(), 'system', now());
