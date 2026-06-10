package com.currency.exchangeRate.config;

import com.currency.exchangeRate.ClientFeign.NbpFeClient;
import com.currency.exchangeRate.ServiceFeign.CurrencyProovider;
import com.currency.exchangeRate.ServiceFeign.NbpCurrencyService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableCaching
public class NbpCurrencyTestConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("exchangeRates", "exchangeRateHistory");
    }

    @Bean
    public NbpFeClient nbpFeClient() {
        return mock(NbpFeClient.class);
    }
    @Bean
    public CurrencyProovider currencyProvider(NbpFeClient nbpClient) {
        return new NbpCurrencyService(nbpClient);  // без ApplicationContext
    }
}
