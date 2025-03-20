SET timezone = 'Europe/Berlin';

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    company VARCHAR(255) NOT NULL,
    source_of_contact VARCHAR(255),
    website_url VARCHAR(255),
    privacy_policy_url VARCHAR(255),
    cta_url VARCHAR(255)
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE,
    first_name VARCHAR(63) NOT NULL,
    last_name VARCHAR(63) NOT NULL,
    email_address VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(63),
    role VARCHAR(31) DEFAULT 'USER' NOT NULL,
    is_account_locked BOOLEAN NOT NULL,
    is_account_enabled BOOLEAN NOT NULL,
    is_subscribed_to_newsletter BOOLEAN NOT NULL,
    verification_token VARCHAR(255) UNIQUE,
    token_expires_at TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_users_customer_id ON users(customer_id);
CREATE INDEX idx_users_email_address ON users(email_address);

CREATE TABLE customer_settings (
    id BIGSERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE,
    is_execution_enabled BOOLEAN NOT NULL,
    is_auto_reply_enabled BOOLEAN DEFAULT FALSE NOT NULL,
    support_agent_name VARCHAR(63),
    crawl_frequency_in_hours INTEGER DEFAULT 168,
    last_crawl_at TIMESTAMP WITH TIME ZONE,
    next_crawl_at TIMESTAMP WITH TIME ZONE,
    email_html_template TEXT,
    mailbox_email_address VARCHAR(63) NOT NULL,
    mailbox_password_hash VARCHAR(255) NOT NULL,
    imap_host VARCHAR(255) NOT NULL,
    smtp_host VARCHAR(255) NOT NULL,
    imap_port INTEGER NOT NULL,
    smtp_port INTEGER NOT NULL,
    CONSTRAINT check_crawl_frequency CHECK (crawl_frequency_in_hours >= 24 AND crawl_frequency_in_hours <= 744)
);
CREATE INDEX idx_customer_settings_customer_id ON customer_settings(customer_id);

CREATE TABLE rag_urls (
    id BIGSERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    is_last_crawl_successful BOOLEAN NOT NULL
);
CREATE INDEX idx_rag_urls_customer_id ON rag_urls(customer_id);

CREATE TABLE blacklist (
    id BIGSERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE,
    blacklisted_email_address VARCHAR(255) NOT NULL,
    UNIQUE (customer_id, blacklisted_email_address)
);
CREATE INDEX idx_blacklist_customer_id ON blacklist(customer_id);

CREATE TABLE message_categories (
    id BIGSERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE,
    category VARCHAR(255) NOT NULL,
    is_reply BOOLEAN DEFAULT FALSE,
    is_function_call BOOLEAN DEFAULT FALSE,
    description TEXT NOT NULL,
    UNIQUE (customer_id, category)
);
CREATE INDEX idx_message_categories_customer_id ON message_categories(customer_id);

CREATE TABLE message_log (
    id BIGSERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE,
    category VARCHAR(255) NOT NULL,
    language VARCHAR(255) NOT NULL,
    from_email_address VARCHAR(255),
    subject TEXT,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processing_time_in_seconds INTEGER GENERATED ALWAYS AS (EXTRACT(EPOCH FROM (processed_at - received_at))) STORED,
    llm_used VARCHAR(255),
    input_tokens INTEGER,
    output_tokens INTEGER,
    total_tokens INTEGER
);
CREATE INDEX idx_message_log_customer_id ON message_log(customer_id);
CREATE INDEX idx_message_log_category ON message_log(category);
CREATE INDEX idx_message_log_received_at ON message_log(received_at);
CREATE INDEX idx_message_log_processed_at ON message_log(processed_at);
CREATE INDEX idx_message_log_processing_time_in_seconds ON message_log(processing_time_in_seconds);

CREATE TABLE response_ratings (
    id BIGSERIAL PRIMARY KEY,
    message_log_id INTEGER REFERENCES message_log(id) ON DELETE CASCADE,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5) NOT NULL,
    feedback TEXT,
    rated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_response_ratings_message_log_id ON response_ratings(message_log_id);
CREATE INDEX idx_response_ratings_rating ON response_ratings(rating);