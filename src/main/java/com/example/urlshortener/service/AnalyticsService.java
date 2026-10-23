package com.example.urlshortener.service;

import com.example.urlshortener.domain.AppUser;
import java.sql.Timestamp;
import java.time.*;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {
    private final NamedParameterJdbcTemplate jdbc;
    public AnalyticsService(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Dashboard dashboard(AppUser user, LocalDate from, LocalDate to) {
        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.minusDays(29) : from;
        if (start.isAfter(end)) throw new IllegalArgumentException("Start date must be before end date");
        Map<String, Object> p = Map.of("owner", user.getId(), "from", Timestamp.valueOf(start.atStartOfDay()), "to", Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
        long totalLinks = number("select count(*) from urls where owner_user_id = :owner", Map.of("owner", user.getId()));
        long totalClicks = number("select count(*) from click_events e join urls u on u.id=e.url_id where u.owner_user_id=:owner", Map.of("owner", user.getId()));
        long periodClicks = number("select count(*) from click_events e join urls u on u.id=e.url_id where u.owner_user_id=:owner and e.clicked_at >= :from and e.clicked_at < :to", p);
        List<Daily> daily = jdbc.query("select cast(e.clicked_at as date) day, count(*) clicks, count(distinct e.visitor_hash) unique_clicks from click_events e join urls u on u.id=e.url_id where u.owner_user_id=:owner and e.clicked_at >= :from and e.clicked_at < :to group by cast(e.clicked_at as date) order by day", p, (rs, row) -> new Daily(rs.getDate("day").toLocalDate(), rs.getLong("clicks"), rs.getLong("unique_clicks")));
        List<Breakdown> referrers = breakdown("select coalesce(e.referrer_source, 'Direct / unknown') label, count(*) clicks from click_events e join urls u on u.id=e.url_id where u.owner_user_id=:owner and e.clicked_at >= :from and e.clicked_at < :to group by 1 order by 2 desc limit 5", p);
        List<Breakdown> devices = breakdown("select e.device_type label, count(*) clicks from click_events e join urls u on u.id=e.url_id where u.owner_user_id=:owner and e.clicked_at >= :from and e.clicked_at < :to group by 1 order by 2 desc", p);
        List<Recent> recent = jdbc.query("select u.short_code, u.long_url, u.created_at, coalesce(max(e.clicked_at), u.created_at) recent_at from urls u left join click_events e on e.url_id=u.id where u.owner_user_id=:owner group by u.id order by recent_at desc limit 5", Map.of("owner", user.getId()), (rs, row) -> new Recent(rs.getString("short_code"), rs.getString("long_url"), rs.getTimestamp("created_at").toInstant(), rs.getTimestamp("recent_at").toInstant()));
        return new Dashboard(totalLinks, totalClicks, periodClicks, start, end, daily, referrers, devices, recent);
    }
    public LinkAnalytics link(AppUser user, String code, LocalDate from, LocalDate to) {
        LocalDate end = to == null ? LocalDate.now() : to; LocalDate start = from == null ? end.minusDays(29) : from;
        Map<String, Object> p = Map.of("owner", user.getId(), "code", code, "from", Timestamp.valueOf(start.atStartOfDay()), "to", Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
        long clicks = number("select count(*) from click_events e join urls u on u.id=e.url_id where u.owner_user_id=:owner and u.short_code=:code and e.clicked_at >= :from and e.clicked_at < :to", p);
        long unique = number("select count(distinct e.visitor_hash) from click_events e join urls u on u.id=e.url_id where u.owner_user_id=:owner and u.short_code=:code and e.clicked_at >= :from and e.clicked_at < :to", p);
        List<Daily> daily = jdbc.query("select cast(e.clicked_at as date) day, count(*) clicks, count(distinct e.visitor_hash) unique_clicks from click_events e join urls u on u.id=e.url_id where u.owner_user_id=:owner and u.short_code=:code and e.clicked_at >= :from and e.clicked_at < :to group by cast(e.clicked_at as date) order by day", p, (rs, row) -> new Daily(rs.getDate("day").toLocalDate(), rs.getLong("clicks"), rs.getLong("unique_clicks")));
        return new LinkAnalytics(clicks, unique, start, end, daily, breakdown("select coalesce(e.referrer_source, 'Direct / unknown') label, count(*) clicks from click_events e join urls u on u.id=e.url_id where u.owner_user_id=:owner and u.short_code=:code and e.clicked_at >= :from and e.clicked_at < :to group by 1 order by 2 desc limit 5", p), breakdown("select e.device_type label, count(*) clicks from click_events e join urls u on u.id=e.url_id where u.owner_user_id=:owner and u.short_code=:code and e.clicked_at >= :from and e.clicked_at < :to group by 1 order by 2 desc", p));
    }
    private long number(String sql, Map<String, ?> params) { Long value = jdbc.queryForObject(sql, params, Long.class); return value == null ? 0 : value; }
    private List<Breakdown> breakdown(String sql, Map<String, ?> p) { return jdbc.query(sql, p, (rs, row) -> new Breakdown(rs.getString("label"), rs.getLong("clicks"))); }
    public record Daily(LocalDate day, long clicks, long uniqueClicks) { }
    public record Breakdown(String label, long clicks) { }
    public record Recent(String shortCode, String longUrl, Instant createdAt, Instant recentAt) { }
    public record Dashboard(long totalLinks, long totalClicks, long clicksInPeriod, LocalDate from, LocalDate to, List<Daily> dailyClicks, List<Breakdown> referrers, List<Breakdown> devices, List<Recent> recentActivity) { }
    public record LinkAnalytics(long clicksInPeriod, long uniqueClicksInPeriod, LocalDate from, LocalDate to, List<Daily> dailyClicks, List<Breakdown> referrers, List<Breakdown> devices) { }
}
