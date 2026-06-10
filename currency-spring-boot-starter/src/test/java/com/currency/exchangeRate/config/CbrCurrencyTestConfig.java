package com.currency.exchangeRate.config;

import com.currency.exchangeRate.ClientFeign.CbrFeClient;
import com.currency.exchangeRate.ModelFeign.CbrSoapEnvelopeBuilder;
import com.currency.exchangeRate.ServiceFeign.CbrCurrencyService;
import com.currency.exchangeRate.ServiceFeign.CurrencyProovider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableCaching
public class CbrCurrencyTestConfig {


    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("exchangeRates", "exchangeRateHistory");
    }

    @Bean
    public CbrFeClient cbrFeClient() {
        return mock(CbrFeClient.class);
    }

    @Bean
    public CbrSoapEnvelopeBuilder envelopeBuilder() {
        return new CbrSoapEnvelopeBuilder();
    }

    @Bean
    public CurrencyProovider currencyProvider(CbrFeClient cbrClient) {
        return new CbrCurrencyService(cbrClient);
    }
}
