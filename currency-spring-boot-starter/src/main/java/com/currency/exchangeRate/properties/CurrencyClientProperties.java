package com.currency.exchangeRate.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "currency.client")
public class CurrencyClientProperties {

    private Provider provider = Provider.CBR;

    private boolean enabled = true;

    private String baseCurrency = "USD";

    private Duration connectTimeout = Duration.ofSeconds(5);

    private Duration readTimeout = Duration.ofSeconds(10);

    private int maxRetryAttempts = 3;

    private Duration retryDelay = Duration.ofSeconds(1);

    private Urls urls = new Urls();

    @Getter
    @Setter
    public static class Urls {
        private String cbr = "https://www.cbr.ru/";
        private String nbp = "https://api.nbp.pl/";
        private String nbrb = "https://api.nbrb.by/";
    }
}