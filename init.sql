SET timezone = 'Europe/Berlin';

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    company VARCHAR(64) NOT NULL,
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
    last_login_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX idx_users_customer_id ON users(customer_id);
CREATE INDEX idx_users_email_address ON users(email_address);

CREATE TABLE customer_settings (
    id BIGSERIAL PRIMARY KEY REFERENCES customers(id) ON DELETE CASCADE,
    is_execution_enabled BOOLEAN NOT NULL,
    is_auto_reply_enabled BOOLEAN DEFAULT FALSE NOT NULL,
    support_agent_name VARCHAR(64) NOT NULL,
    crawl_frequency_in_hours INTEGER NOT NULL,
    last_crawl_at TIMESTAMP WITH TIME ZONE,
    next_crawl_at TIMESTAMP WITH TIME ZONE,
    email_html_template TEXT,
    mailbox_email_address VARCHAR(256) NOT NULL,
    mailbox_password_hash TEXT NOT NULL,
    imap_host VARCHAR(64) NOT NULL,
    smtp_host VARCHAR(64) NOT NULL,
    imap_port INTEGER NOT NULL,
    smtp_port INTEGER NOT NULL
);

CREATE TABLE rag_urls (
    id BIGSERIAL PRIMARY KEY,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    url TEXT NOT NULL,
    is_last_crawl_successful BOOLEAN NOT NULL
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
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE NOT NULL ,
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
CREATE INDEX idx_message_log_customer_id ON message_log(customer_id);
CREATE INDEX idx_message_log_category ON message_log(category);
CREATE INDEX idx_message_log_received_at ON message_log(received_at);
CREATE INDEX idx_message_log_processed_at ON message_log(processed_at);
CREATE INDEX idx_message_log_processing_time_in_seconds ON message_log(processing_time_in_seconds);

CREATE TABLE response_ratings (
    id BIGSERIAL PRIMARY KEY REFERENCES message_log(id) ON DELETE CASCADE,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE NOT NULL,
    rating INTEGER CHECK (rating BETWEEN 1 AND 5) NOT NULL,
    feedback TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX idx_response_ratings_rating ON response_ratings(rating);
CREATE INDEX idx_response_ratings_rated_at ON response_ratings(created_at);