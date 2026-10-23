ALTER TABLE urls ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE;
CREATE INDEX idx_urls_owner_created ON urls(owner_user_id, created_at DESC);
