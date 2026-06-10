package com.currency.exchangeRate.cache;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Component
public class CacheMetricsExporter {

    private final MeterRegistry registry;
    private final Map<String, Counter> hitCounters = new HashMap<>();
    private final Map<String, Counter> missCounters = new HashMap<>();

    public void recordHit(String cacheName) {
        getOrCreateHitCounter(cacheName).increment();
    }

    public void recordMiss(String cacheName) {
        getOrCreateMissCounter(cacheName).increment();
    }

    private Counter getOrCreateHitCounter(String cacheName) {
        return hitCounters.computeIfAbsent(cacheName, name ->
                Counter.builder("currency.cache.hits")
                        .description("Попадания в кеш")
                        .tag("cache", name)
                        .register(registry)
        );
    }

    private Counter getOrCreateMissCounter(String cacheName) {
        return missCounters.computeIfAbsent(cacheName, name ->
                Counter.builder("currency.cache.misses")
                        .description("Промахи кеша")
                        .tag("cache", name)
                        .register(registry)
        );
    }
}
