package com.currency.exchangeRate.config;

import com.currency.exchangeRate.ClientFeign.NbrbFeClient;
import com.currency.exchangeRate.ServiceFeign.CurrencyProovider;
import com.currency.exchangeRate.ServiceFeign.NbrbCurrencyService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;


import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableCaching
public class NbrbCurrencyTestConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("exchangeRates", "exchangeRateHistory");
    }

    @Bean
    public NbrbFeClient nbrbFeClient() {
        return mock(NbrbFeClient.class);
    }


    @Bean
    public CurrencyProovider currencyProvider(NbrbFeClient nbrbClient) {
        return new NbrbCurrencyService(nbrbClient);
    }

}
