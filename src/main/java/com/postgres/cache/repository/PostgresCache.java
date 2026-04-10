package com.postgres.cache.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import org.springframework.cache.Cache;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.cache.support.SimpleValueWrapper;

public class PostgresCache implements Cache {

	private final String name;
	private final CacheRepository repository;
	private final ObjectMapper mapper;
	private final Duration ttl;

	public PostgresCache(String name, CacheRepository repository, ObjectMapper mapper, Duration ttl) {
		this.name = name;
		this.repository = repository;
		this.mapper = mapper;
		this.ttl = ttl;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getNativeCache() {
		return repository;
	}

	@Override
	public ValueWrapper get(Object key) {
		return repository.findByCacheKeyAndCacheName(key.toString(), name)
			.filter(e -> e.getExpiresAt().isAfter(LocalDateTime.now()))
			.map(e -> {
				Object value = deserialize(e.getCacheValue(), e.getCacheType());
				return value != null ? new SimpleValueWrapper(value) : null;
			})
			.orElse(null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Class<T> type) {
		ValueWrapper wrapper = get(key);
		return (wrapper != null) ? (T) wrapper.get() : null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Object key, Callable<T> valueLoader) {
		ValueWrapper wrapper = get(key);
		if (wrapper != null) return (T) wrapper.get();

		try {
			T value = valueLoader.call();
			put(key, value);
			return value;
		} catch (Exception ex) {
			throw new ValueRetrievalException(key, valueLoader, ex);
		}
	}

	@Override
	public void put(Object key, Object value) {
		if (value == null) return;

		String jsonValue = serialize(value);
		String className = value.getClass().getName();
		LocalDateTime expiry = LocalDateTime.now().plus(ttl);

		repository.upsertCache(key.toString(), name, className, jsonValue, expiry);
	}

	@Override
	public void evict(Object key) {
		repository.deleteByCacheKeyAndCacheName(key.toString(), name);
	}

	@Override
	public void clear() {
		repository.deleteByCacheName(name);
	}

	private String serialize(Object value) {
		try {
			return mapper.writeValueAsString(value);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException("Falha ao serializar objeto para cache", ex);
		}
	}

	private Object deserialize(String json, String className) {
		try {
			Class<?> clazz = Class.forName(className);
			return mapper.readValue(json, clazz);
		} catch (ClassNotFoundException e) {
			return null;
		} catch (Exception ex) {
			return null;
		}
	}

}
