package com.currency.exchangeRate.autoconfigure;

import com.currency.exchangeRate.client.CbrApiClient;
import com.currency.exchangeRate.client.CurrencyClient;
import com.currency.exchangeRate.client.NbpApiClient;
import com.currency.exchangeRate.client.NbrbApiClient;
import com.currency.exchangeRate.properties.CurrencyClientProperties;
import com.currency.exchangeRate.service.CurrencyClientService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
@EnableConfigurationProperties(CurrencyClientProperties.class)
@ConditionalOnProperty(prefix = "currency.client", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CurrencyClientAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "currency.client.provider", havingValue = "NBP", matchIfMissing = true)
    @ConditionalOnMissingBean
    public CurrencyClient nbpApiClient(CurrencyClientProperties properties) {
        return new NbpApiClient(properties);
    }

    @Bean
    @ConditionalOnProperty(name = "currency.client.provider", havingValue = "CBR", matchIfMissing = true)
    @ConditionalOnMissingBean
    public CurrencyClient cbrApiClient(CurrencyClientProperties properties) {
        return new CbrApiClient(properties);
    }

    @Bean
    @ConditionalOnProperty(name = "currency.client.provider", havingValue = "NBRB", matchIfMissing = true)
    @ConditionalOnMissingBean
    public CurrencyClient nbrbApiClient(CurrencyClientProperties properties) {
        return new NbrbApiClient(properties);
    }


    @Bean
    @ConditionalOnMissingBean
    public CurrencyClientService currencyService(CurrencyClient apiClient, CurrencyClientProperties properties) {
        return new CurrencyClientService(apiClient, properties);
    }
}