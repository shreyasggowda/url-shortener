CREATE TABLE click_events (
    id BIGSERIAL PRIMARY KEY,
    url_id BIGINT NOT NULL REFERENCES urls(id) ON DELETE CASCADE,
    clicked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    referrer_source VARCHAR(254),
    device_type VARCHAR(16) NOT NULL,
    visitor_hash VARCHAR(64)
);
CREATE INDEX idx_click_events_url_time ON click_events(url_id, clicked_at DESC);
CREATE INDEX idx_click_events_url_visitor ON click_events(url_id, visitor_hash);
