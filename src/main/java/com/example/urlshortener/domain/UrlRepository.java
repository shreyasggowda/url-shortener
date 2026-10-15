package com.example.urlshortener.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface UrlRepository extends JpaRepository<UrlMapping, Long> {
    @Query(value = "select nextval('url_id_seq')", nativeQuery = true)
    Long nextId();
    Optional<UrlMapping> findByShortCode(String shortCode);
    @Modifying
    @Query("update UrlMapping u set u.clickCount = u.clickCount + 1 where u.shortCode = :code")
    int incrementClickCount(@Param("code") String code);
}
