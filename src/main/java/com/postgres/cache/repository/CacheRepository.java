package com.postgres.cache.repository;

import com.postgres.cache.model.CacheEntity;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CacheRepository extends JpaRepository<CacheEntity, String> {

    Optional<CacheEntity> findByCacheKeyAndCacheName(String cacheKey, String cacheName);

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO public.cache_produto (cache_key, cache_name, cache_type, cache_value, expires_at)
        VALUES (:key, :name, :type, :value\\:\\:jsonb, :expires)
        ON CONFLICT (cache_key) 
        DO UPDATE SET cache_value = EXCLUDED.cache_value, cache_type = EXCLUDED.cache_type,  expires_at = EXCLUDED.expires_at
        """, nativeQuery = true)
    void upsertCache(String key, String name, String type, String value, LocalDateTime expires);

    @Transactional
    void deleteByCacheKeyAndCacheName(String key, String name);

    @Transactional
    void deleteByCacheName(String name);

    @Transactional
    @Modifying
    @Query("DELETE FROM CacheEntity e WHERE e.expiresAt < :now")
    void deleteExpired(LocalDateTime now);
}
