package com.currency.exchangeRate.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "currency.client")
public class CurrencyClientProperties {

    private Provider provider ;


    private boolean enabled = true;

    private String baseCurrency = "USD";

    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(10);

    @NestedConfigurationProperty
    private ProviderConfig providerConfig = new ProviderConfig();

}
