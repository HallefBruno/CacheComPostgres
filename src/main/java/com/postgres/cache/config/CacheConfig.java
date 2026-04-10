package com.postgres.cache.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.postgres.cache.repository.CacheRepository;
import com.postgres.cache.repository.PostgresCache;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {
	
	private static final int TEMPO_MAXIMO = 1;
	
    private final CacheRepository repository;

    public CacheConfig(CacheRepository repository) {
        this.repository = repository;
    }

	@Bean
    public ObjectMapper cacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public CacheManager cacheManager(ObjectMapper mapper) {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new PostgresCache("produtos", repository, mapper, Duration.ofMinutes(TEMPO_MAXIMO))
        ));
        return cacheManager;
    }

    //@Scheduled(cron = "0 */10 * * * *")
	@Scheduled(cron = "0 */1 * * * *")
	@Transactional
    public void cleanCache() {
        this.repository.deleteExpired(LocalDateTime.now());
    }
}
