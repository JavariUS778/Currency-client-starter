package com.currency.exchangeRate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
@Slf4j
public class GracefulCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Ошибка чтения из кеша '{}' по ключу '{}': {}. Кеш будет перезаписан.",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.error("Ошибка записи в кеш '{}' по ключу '{}': {}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.error("Ошибка удаления из кеша '{}' по ключу '{}': {}",
                cache.getName(), key, exception.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.error("Ошибка очистки кеша '{}': {}", cache.getName(), exception.getMessage());
    }

}
