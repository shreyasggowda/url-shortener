package com.example.urlshortener.domain;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UrlRepository extends JpaRepository<UrlMapping, Long> {
    @Query(value = "select nextval('url_id_seq')", nativeQuery = true)
    Long nextId();
    Optional<UrlMapping> findByShortCode(String shortCode);
    @Query("""
        select u from UrlMapping u where u.owner.id = :ownerId
        and (:search is null or lower(u.shortCode) like lower(concat('%', :search, '%')) or lower(u.longUrl) like lower(concat('%', :search, '%')))
        and (:fromDate is null or u.createdAt >= :fromDate) and (:toDate is null or u.createdAt <= :toDate)
        and (:status = 'all' or (:status = 'active' and (u.expiresAt is null or u.expiresAt > CURRENT_TIMESTAMP)) or (:status = 'expired' and u.expiresAt <= CURRENT_TIMESTAMP))
        """)
    Page<UrlMapping> findOwnedLinks(@Param("ownerId") Long ownerId, @Param("search") String search, @Param("status") String status, @Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate, Pageable pageable);
    @Modifying
    @Query("update UrlMapping u set u.clickCount = u.clickCount + 1 where u.shortCode = :code")
    int incrementClickCount(@Param("code") String code);
}
