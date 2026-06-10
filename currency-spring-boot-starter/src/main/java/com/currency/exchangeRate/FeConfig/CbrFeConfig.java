package com.currency.exchangeRate.FeConfig;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class CbrFeConfig {

    @Bean
    public RequestInterceptor cbrInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("User-Agent", "Mozilla/5.0");
            requestTemplate.header("Accept", "text/xml");
        };
    }
}
