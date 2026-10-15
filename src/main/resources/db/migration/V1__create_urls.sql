CREATE SEQUENCE url_id_seq START WITH 1 INCREMENT BY 1;
CREATE TABLE urls (
    id BIGINT PRIMARY KEY DEFAULT nextval('url_id_seq'),
    short_code VARCHAR(10) UNIQUE NOT NULL,
    long_url TEXT NOT NULL,
    api_key_owner VARCHAR(100) NOT NULL,
    click_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
CREATE INDEX idx_short_code ON urls(short_code);
