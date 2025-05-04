CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    company VARCHAR(64) NOT NULL,
    street VARCHAR(64) NOT NULL,
    house_number VARCHAR(64) NOT NULL,
    postal_code VARCHAR(64) NOT NULL,
    city VARCHAR(64) NOT NULL,
    billing_email_address VARCHAR(64) NOT NULL UNIQUE ,
    openai_api_key_encrypted TEXT NOT NULL,
    source_of_contact VARCHAR(64),
    website_url TEXT,
    privacy_policy_url TEXT,
    cta_url TEXT,
    registration_token TEXT UNIQUE NOT NULL,
    is_test_version BOOLEAN,
    ionos_username VARCHAR(64),
    ionos_password_encrypted TEXT
);
CREATE INDEX idx_customers_registration_token ON customers(registration_token);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    first_name_encrypted TEXT NOT NULL,
    last_name_encrypted TEXT NOT NULL,
    email_address_hash TEXT NOT NULL UNIQUE,
    email_address_encrypted TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    phone_number_encrypted TEXT,
    position VARCHAR(64),
    role VARCHAR(16) DEFAULT 'USER' NOT NULL,
    is_account_locked BOOLEAN NOT NULL,
    is_account_enabled BOOLEAN NOT NULL,
    is_subscribed_to_newsletter BOOLEAN NOT NULL,
    verification_token TEXT UNIQUE NOT NULL,
    token_expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX idx_users_customer_id ON users(customer_id);
CREATE INDEX idx_users_email_address ON users(email_address_hash);
CREATE INDEX idx_users_verification_token ON users(verification_token);

CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    client_name VARCHAR(256) NOT NULL UNIQUE,
    client_secret_hash TEXT NOT NULL,
    scope TEXT NOT NULL
);

CREATE TABLE settings (
    user_id BIGSERIAL PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    customer_id BIGINT REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    is_execution_enabled BOOLEAN NOT NULL,
    is_auto_reply_enabled BOOLEAN DEFAULT FALSE NOT NULL,
    is_response_rating_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    crawl_frequency_in_hours INTEGER DEFAULT 168 NOT NULL,
    last_crawl_at TIMESTAMP WITH TIME ZONE,
    next_crawl_at TIMESTAMP WITH TIME ZONE,
    mailbox_password_hash TEXT NOT NULL,
    mailbox_password_encrypted TEXT NOT NULL,
    imap_host VARCHAR(64),
    smtp_host VARCHAR(64),
    imap_port INTEGER,
    smtp_port INTEGER
);

CREATE TABLE rag_urls (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    url TEXT NOT NULL,
    is_last_crawl_successful BOOLEAN
);
CREATE INDEX idx_rag_urls_customer_id ON rag_urls(customer_id);

CREATE TABLE blacklist (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    blacklisted_email_address_hash TEXT NOT NULL,
    blacklisted_email_address_encrypted TEXT NOT NULL,
    UNIQUE (user_id, blacklisted_email_address_hash)
);
CREATE INDEX idx_blacklist_customer_id ON blacklist(user_id);

CREATE TABLE message_categories (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    category VARCHAR(64) NOT NULL,
    is_reply BOOLEAN DEFAULT FALSE NOT NULL,
    is_function_call BOOLEAN DEFAULT FALSE NOT NULL,
    description TEXT NOT NULL,
    UNIQUE (customer_id, category)
);
CREATE INDEX idx_message_categories_customer_id ON message_categories(customer_id);

CREATE TABLE message_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    customer_id BIGINT REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    is_replied BOOLEAN NOT NULL,
    category VARCHAR(64) NOT NULL,
    language VARCHAR(64) NOT NULL,
    from_email_address_encrypted TEXT,
    subject TEXT,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    processing_time_in_seconds INTEGER NOT NULL,
    llm_used VARCHAR(64) NOT NULL,
    input_tokens INTEGER NOT NULL,
    output_tokens INTEGER NOT NULL,
    total_tokens INTEGER NOT NULL,
    token TEXT UNIQUE NOT NULL,
    token_expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_message_log_user_id ON message_log(user_id);
CREATE INDEX idx_message_log_customer_id ON message_log(customer_id);
CREATE INDEX idx_message_log_category ON message_log(category);
CREATE INDEX idx_message_log_received_at ON message_log(received_at);
CREATE INDEX idx_message_log_processed_at ON message_log(processed_at);
CREATE INDEX idx_message_log_processing_time_in_seconds ON message_log(processing_time_in_seconds);
CREATE INDEX idx_message_log_token ON message_log(token);

CREATE TABLE response_ratings (
    message_log_id BIGSERIAL PRIMARY KEY REFERENCES message_log(id) ON DELETE CASCADE,
    customer_id BIGINT REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    is_satisfied BOOLEAN NOT NULL,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5) NOT NULL,
    feedback TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX idx_response_ratings_message_log_id ON response_ratings(message_log_id);
CREATE INDEX idx_response_ratings_customer_id ON response_ratings(customer_id);
CREATE INDEX idx_response_ratings_user_id ON response_ratings(user_id);
CREATE INDEX idx_response_ratings_rating ON response_ratings(rating);
CREATE INDEX idx_response_ratings_rated_at ON response_ratings(created_at);

INSERT INTO customers (company, street, house_number, postal_code, city, billing_email_address, openai_api_key_encrypted, registration_token, is_test_version, ionos_username, ionos_password_encrypted)
VALUES ('FlowSuite', 'Straße', '69', '1337', 'München', 'rechnungen@flow-suite.de', 'R0p2fHYTSBAHIq5YEWzN1Jwnfar/IwvyqnPhw/AGjwliTfNO71WPHw==', 'secureToken1', true, 'test@flow-suite.de' , 'password'),
       ('Company', 'Street', '69', '1337', 'City', 'billing@example.de', 'R0p2fHYTSBAHIq5YEWzN1Jwnfar/IwvyqnPhw/AGjwliTfNO71WPHw==', 'secureToken2', true, 'test@flow-suite.de', 'password');

INSERT INTO users (customer_id, first_name_encrypted, last_name_encrypted, email_address_hash, email_address_encrypted, password_hash, role, is_account_locked, is_account_enabled, is_subscribed_to_newsletter, verification_token, token_expires_at)
VALUES (1, 'Uztmz8Fii79yN2SY6wg5md6Ek5RLeBzMGYlNlqYutLyj', 'Uztmz8Fii79yN2SY6wg5md6Ek5RLeBzMGYlNlqYutLyj', 'Cb6R4BLpHhVMebqauEd3TZrhfdkR8hFjvulTHYUfbNM=', 'DMX3vfIVH7vta9jAgOUbwEWGRTa5jFiv2yLi6BMnNv4d7hcfQFdMGnUCRcJPfA==', '$2a$10$t0Olv0N4TdmUfd9yG242i.znX.NN7c.a3AU9DadUg1ro0Xsc8jvom', 'ADMIN', false, true, true, 'token1', NOW() + INTERVAL '30 minutes'),
       (2, 'RtlBAwPz6EdINA4O51gu8uz0AuZ0UHE5FJPC26Xbquo=', 'RtlBAwPz6EdINA4O51gu8uz0AuZ0UHE5FJPC26Xbquo=', 'U8c45XuAqt5w5/4xkE6/vFfnLE5E3t1uNJJqoywHvUM=', 'g6jxJeir/fXKqxEOwUGuuyLraFFSFnW9Gn8/3QF/J9eS6ka9yyk55iLA5rwgcg==', '$2a$10$t0Olv0N4TdmUfd9yG242i.znX.NN7c.a3AU9DadUg1ro0Xsc8jvom', 'USER', false, true, true, 'token2', NOW() + INTERVAL '30 minutes');

INSERT INTO clients(client_name, client_secret_hash, scope)
VALUES ('mailbox-service', '$2a$10$4/8k4VN17iFXP4PD840vVOV.RvKwWQ.pFP9cjOPSqYHYmeWMk1wXe', 'CLIENT customers:list customers:read settings:read'),
       ('rag-service', '$2a$10$4/8k4VN17iFXP4PD840vVOV.RvKwWQ.pFP9cjOPSqYHYmeWMk1wXe', 'CLIENT customers:list customers:read settings:read'),
       ('llm-service', '$2a$10$4/8k4VN17iFXP4PD840vVOV.RvKwWQ.pFP9cjOPSqYHYmeWMk1wXe', 'CLIENT customers:list customers:read settings:read');


INSERT INTO settings (user_id, customer_id, is_execution_enabled, is_auto_reply_enabled, is_response_rating_enabled, crawl_frequency_in_hours, mailbox_password_hash, mailbox_password_encrypted, imap_host, smtp_host, imap_port, smtp_port)
VALUES (1, 1,true, false, true, 168, '4NtowY94nzPXZNY7emJFTPGycM6GlSugg6OwlOFQeDI=', '$2a$10$/fXalbMsPDJvqVAVo2YNYeEFWdKl67nIyM4.7DEsoy/ZXdWHkJRHm', 'imap.ionos.de', 'smtp.ionos.com', 993, 465),
       (2, 2,true, false, true, 168, '4NtowY94nzPXZNY7emJFTPGycM6GlSugg6OwlOFQeDI=', '$2a$10$/fXalbMsPDJvqVAVo2YNYeEFWdKl67nIyM4.7DEsoy/ZXdWHkJRHm', 'imap.ionos.de', 'smtp.ionos.com', 993, 465);

INSERT INTO rag_urls (customer_id, url, is_last_crawl_successful)
VALUES (1, 'https://www.flow-suite.de', NULL),
       (2, 'https://www.flow-suite.de', NULL);

INSERT INTO blacklist (user_id, blacklisted_email_address_hash, blacklisted_email_address_encrypted)
VALUES (1, 'PCwU0vnyGsBYrljsDMd3Kf5Lq/fhqG7VLMc/aCKR+fU=', 'ks5Bk+l9E29nDULdti6ihyz8ZFfqwvc8wfxiRL2d0HSvtVOkPJZ8g3zDnnFhJQ=='),
       (1, 'sOCDd3BNjIapxAdppHUn6OcqwPkmkw6XVjVwR0acfJ8=', 'qLwsA99/GkXAT56obP1sLJBg9sB5yGHWCxCsBmQjN3hs3lm86kXFaxNjAMMQx8mHtKsy'),
       (2, 'PCwU0vnyGsBYrljsDMd3Kf5Lq/fhqG7VLMc/aCKR+fU=', 'ks5Bk+l9E29nDULdti6ihyz8ZFfqwvc8wfxiRL2d0HSvtVOkPJZ8g3zDnnFhJQ==');

INSERT INTO message_categories (customer_id, category, is_reply, is_function_call, description)
VALUES (2, 'Produkt Frage', true, false, 'Allgemeine Fragen zum Produkt'),
       (2, 'Buchungsanfrage', true, true, 'Buchungsanfragen für ein Hotelzimmer'),
       (2, 'Support', false, false, 'Generelle Support Anfrage');

INSERT INTO message_log (user_id, customer_id, is_replied, category, language, from_email_address_encrypted,subject, received_at, processed_at, processing_time_in_seconds,llm_used, input_tokens, output_tokens, total_tokens, token, token_expires_at)
VALUES
-- 2024
(2, 2, true, 'Produkt Frage', 'Deutsch', 'user@example.com', 'Test', '2024-03-25T09:00:00+01:00', '2024-03-25T09:00:00+01:00', 40, 'gpt-4', 1600, 1200, 2800, 'token1', NOW() + INTERVAL '30 minutes'),
-- Week of March 25–31
(2, 2, true, 'Produkt Frage', 'Deutsch', 'user@example.com', 'Test', '2025-03-25T09:00:00+01:00', '2025-03-25T09:00:00+01:00', 40, 'gpt-4', 1600, 1200, 2800, 'token2', NOW() + INTERVAL '30 minutes'),
(2, 2, true, 'Buchungsanfrage', 'Deutsch', 'user@example.com', 'Test', '2025-03-27T14:30:00+01:00', '2025-03-27T14:30:00+01:00', 38, 'gpt-4', 1580, 1190, 2770, 'token3', NOW() + INTERVAL '30 minutes'),
(2, 2, false, 'Support', 'Deutsch', 'user@example.com', 'Test', '2025-03-29T10:45:00+01:00', '2025-03-29T10:45:00+01:00', 35, 'gpt-4', 1550, 1150, 2700, 'token4', NOW() + INTERVAL '30 minutes'),

-- Week of April 1–7
(2, 2, true, 'Produkt Frage', 'Deutsch', 'user@example.com', 'Test', '2025-04-01T08:00:00+02:00', '2025-04-01T08:00:00+02:00', 42, 'gpt-4', 1620, 1220, 2840, 'token5', NOW() + INTERVAL '30 minutes'),
(2, 2, true, 'Buchungsanfrage', 'Deutsch', 'user@example.com', 'Test', '2025-04-01T15:15:00+02:00', '2025-04-01T15:15:00+02:00', 39, 'gpt-4', 1590, 1190, 2780, 'token6', NOW() + INTERVAL '30 minutes'),
(2, 2, false, 'Support', 'Deutsch', 'user@example.com', 'Test', '2025-04-03T11:30:00+02:00', '2025-04-03T11:30:00+02:00', 44, 'gpt-4', 1630, 1240, 2870, 'token7', NOW() + INTERVAL '30 minutes'),

-- Week of April 8–14
(2, 2, true, 'Produkt Frage', 'Deutsch', 'user@example.com', 'Test', '2025-04-08T10:00:00+02:00', '2025-04-08T10:00:00+02:00', 37, 'gpt-4', 1570, 1170, 2740, 'token8', NOW() + INTERVAL '30 minutes'),
(2, 2, true, 'Buchungsanfrage', 'Deutsch', 'user@example.com', 'Test', '2025-04-09T13:45:00+02:00', '2025-04-09T13:45:00+02:00', 46, 'gpt-4', 1650, 1250, 2900, 'token9', NOW() + INTERVAL '30 minutes'),
(2, 2, false, 'Support', 'Deutsch', 'user@example.com', 'Test', '2025-04-10T09:30:00+02:00', '2025-04-10T09:30:00+02:00', 40, 'gpt-4', 1600, 1200, 2800, 'token10', NOW() + INTERVAL '30 minutes'),

-- Week of April 15–21
(2, 2, true, 'Produkt Frage', 'Deutsch', 'user@example.com', 'Test', '2025-04-15T09:00:00+02:00', '2025-04-15T09:00:00+02:00', 41, 'gpt-4', 1590, 1190, 2780, 'token11', NOW() + INTERVAL '30 minutes'),
(2, 2, true, 'Produkt Frage', 'Deutsch', 'user@example.com', 'Test', '2025-04-15T17:15:00+02:00', '2025-04-15T17:15:00+02:00', 39, 'gpt-4', 1580, 1180, 2760, 'token12', NOW() + INTERVAL '30 minutes'),
(2, 2, true, 'Buchungsanfrage', 'Deutsch', 'user@example.com', 'Test', '2025-04-17T14:30:00+02:00', '2025-04-17T14:30:00+02:00', 43, 'gpt-4', 1610, 1210, 2820, 'token13', NOW() + INTERVAL '30 minutes'),
(2, 2, false, 'Support', 'Deutsch', 'user@example.com', 'Test', '2025-04-19T11:45:00+02:00', '2025-04-19T11:45:00+02:00', 36, 'gpt-4', 1560, 1160, 2720, 'token14', NOW() + INTERVAL '30 minutes'),

-- April 22
(2, 2, true, 'Produkt Frage', 'Deutsch', 'user@example.com', 'Test', '2025-04-22T08:30:00+02:00', '2025-04-22T08:30:00+02:00', 42, 'gpt-4', 1620, 1220, 2840, 'token15', NOW() + INTERVAL '30 minutes'),
(2, 2, true, 'Produkt Frage', 'Deutsch', 'user@example.com', 'Test', '2025-04-22T16:30:00+02:00', '2025-04-22T16:30:00+02:00', 45, 'gpt-4', 1640, 1240, 2880, 'token16', NOW() + INTERVAL '30 minutes'),
(2, 2, true, 'Buchungsanfrage', 'Deutsch', 'user@example.com', 'Test', '2025-04-22T09:00:00+02:00', '2025-04-22T09:00:00+02:00', 41, 'gpt-4', 1600, 1200, 2800, 'token17', NOW() + INTERVAL '30 minutes'),
(2, 2, true, 'Buchungsanfrage', 'Deutsch', 'user@example.com', 'Test', '2025-04-22T17:00:00+02:00', '2025-04-22T17:00:00+02:00', 40, 'gpt-4', 1600, 1200, 2800, 'token18', NOW() + INTERVAL '30 minutes'),
(2, 2, false, 'Support', 'Deutsch', 'user@example.com', 'Test', '2025-04-22T10:00:00+02:00', '2025-04-22T10:00:00+02:00', 44, 'gpt-4', 1630, 1230, 2860, 'token19', NOW() + INTERVAL '30 minutes'),
(2, 2, false, 'Support', 'Deutsch', 'user@example.com', 'Test', '2025-04-22T18:00:00+02:00', '2025-04-22T18:00:00+02:00', 46, 'gpt-4', 1650, 1250, 2900, 'token20', NOW() + INTERVAL '30 minutes');

INSERT INTO response_ratings (message_log_id, customer_id, user_id, is_satisfied, rating, feedback)
VALUES (1, 2, 2, true, 4, 'Good'),
       (2, 2, 2, false, 2, 'Bad'),
       (7, 2, 2, false, 1, 'Very Bad');

INSERT INTO response_ratings (message_log_id, customer_id, user_id, is_satisfied, rating)
VALUES
       (3, 2, 2, false, 3),
       (4, 2, 2, true, 5),
       (5, 2, 2, false, 3),
       (6, 2, 2, true, 4),
       (8, 2, 2, false, 1),
       (9, 2, 2, true, 4);


