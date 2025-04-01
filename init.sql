SET timezone = 'Europe/Berlin';

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    company VARCHAR(64) NOT NULL,
    street VARCHAR(64) NOT NULL,
    house_number VARCHAR(64) NOT NULL,
    postal_code VARCHAR(64) NOT NULL,
    city VARCHAR(64) NOT NULL,
    openai_api_key TEXT NOT NULL,
    source_of_contact VARCHAR(64),
    website_url TEXT,
    privacy_policy_url TEXT,
    cta_url TEXT
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    email_address VARCHAR(256) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    phone_number VARCHAR(64),
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
CREATE INDEX idx_users_email_address ON users(email_address);

CREATE TABLE settings (
    user_id BIGSERIAL PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    is_execution_enabled BOOLEAN NOT NULL,
    is_auto_reply_enabled BOOLEAN DEFAULT FALSE NOT NULL,
    is_response_rating_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    crawl_frequency_in_hours INTEGER DEFAULT 168 NOT NULL,
    last_crawl_at TIMESTAMP WITH TIME ZONE,
    next_crawl_at TIMESTAMP WITH TIME ZONE,
    mailbox_password TEXT NOT NULL,
    imap_host VARCHAR(64),
    smtp_host VARCHAR(64),
    imap_port INTEGER,
    smtp_port INTEGER
);

CREATE TABLE rag_urls (
    id BIGSERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    url TEXT NOT NULL,
    is_last_crawl_successful BOOLEAN
);
CREATE INDEX idx_rag_urls_customer_id ON rag_urls(customer_id);

CREATE TABLE blacklist (
    id BIGSERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    blacklisted_email_address VARCHAR(256) NOT NULL,
    UNIQUE (customer_id, blacklisted_email_address)
);
CREATE INDEX idx_blacklist_customer_id ON blacklist(customer_id);

CREATE TABLE message_categories (
    id BIGSERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    category VARCHAR(64) NOT NULL,
    is_reply BOOLEAN DEFAULT FALSE NOT NULL,
    is_function_call BOOLEAN DEFAULT FALSE NOT NULL,
    description TEXT NOT NULL,
    UNIQUE (customer_id, category)
);
CREATE INDEX idx_message_categories_customer_id ON message_categories(customer_id);

CREATE TABLE message_log (
    id BIGSERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    category VARCHAR(64) NOT NULL,
    language VARCHAR(64) NOT NULL,
    from_email_address VARCHAR(256),
    subject TEXT,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    processing_time_in_seconds INTEGER NOT NULL,
    llm_used VARCHAR(64) NOT NULL,
    input_tokens INTEGER NOT NULL,
    output_tokens INTEGER NOT NULL,
    total_tokens INTEGER NOT NULL
);
CREATE INDEX idx_message_log_user_id ON message_log(user_id);
CREATE INDEX idx_message_log_customer_id ON message_log(customer_id);
CREATE INDEX idx_message_log_category ON message_log(category);
CREATE INDEX idx_message_log_received_at ON message_log(received_at);
CREATE INDEX idx_message_log_processed_at ON message_log(processed_at);
CREATE INDEX idx_message_log_processing_time_in_seconds ON message_log(processing_time_in_seconds);

CREATE TABLE response_ratings (
    message_log_id BIGSERIAL PRIMARY KEY REFERENCES message_log(id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    isSatisfied BOOLEAN NULL,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5) NOT NULL,
    feedback TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX idx_response_ratings_user_id ON response_ratings(user_id);
CREATE INDEX idx_response_ratings_rating ON response_ratings(rating);
CREATE INDEX idx_response_ratings_rated_at ON response_ratings(created_at);

INSERT INTO customers (company, street, house_number, postal_code, city, openai_api_key)
VALUES ('FlowSuite', 'Straße', '69', '1337', 'München', 'apikey');

INSERT INTO customers (company, street, house_number, postal_code, city, openai_api_key)
VALUES ('Company', 'Street', '69', '1337', 'City', 'apikey');

INSERT INTO users (customer_id, first_name, last_name, email_address, password_hash, role, is_account_locked, is_account_enabled, is_subscribed_to_newsletter, verification_token, token_expires_at)
VALUES (1, 'Moritz', 'Schultz', 'moritz@flow-suite.de', '$2a$10$OTGphs2A9kBq/JCce5np.uZaIfGb1exhMRuJ4pBBOOoLWuoxlO72.', 'ADMIN', false, true, true, 'verif_token_1', NOW() + INTERVAL '30 minutes');

INSERT INTO users (customer_id, first_name, last_name, email_address, password_hash, role, is_account_locked, is_account_enabled, is_subscribed_to_newsletter, verification_token, token_expires_at)
VALUES (2, 'User', 'User', 'user', '$2a$10$OTGphs2A9kBq/JCce5np.uZaIfGb1exhMRuJ4pBBOOoLWuoxlO72.', 'USER', false, true, true, 'verif_token_2', NOW() + INTERVAL '30 minutes');

INSERT INTO settings (user_id, customer_id, is_execution_enabled, is_auto_reply_enabled, is_response_rating_enabled, crawl_frequency_in_hours, mailbox_password, imap_host, smtp_host, imap_port, smtp_port)
VALUES (1, 1,true, false, true, 168, 'encrypted_mailbox_password', 'imap.ionos.de', 'smtp.ionos.com', 993, 465);

INSERT INTO rag_urls (customer_id, url, is_last_crawl_successful)
VALUES (1, 'https://flow-suite.de', NULL);

INSERT INTO blacklist (customer_id, blacklisted_email_address)
VALUES (1, 'info@flow-suite.de');

INSERT INTO message_categories (customer_id, category, is_reply, is_function_call, description)
VALUES (1, 'Produkt', true, false, 'Fragen zum Produkt');

INSERT INTO message_log (user_id, customer_id, category, language, from_email_address, subject, received_at, processed_at, processing_time_in_seconds, llm_used, input_tokens, output_tokens, total_tokens)
VALUES (1, 1, 'Produkt', 'Deutsch', 'schultzmoritz@gmail.com', 'Was kann MailFlow', NOW(), NOW() + INTERVAL '30 seconds', 30, 'GPT-4', 1500, 1000, 2500);

INSERT INTO response_ratings (message_log_id, user_id, rating, feedback)
VALUES (1, 1, 5, 'Frage schnell beantwortet!');
