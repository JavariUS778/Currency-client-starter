package com.currency.exchangeRate.service;

import com.currency.exchangeRate.client.CurrencyClient;
import com.currency.exchangeRate.model.CurrencyRate;
import com.currency.exchangeRate.properties.CurrencyClientProperties;
import com.currency.exchangeRate.properties.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyClientService {

    private final CurrencyClient apiClient;
    private final CurrencyClientProperties properties;

    public Mono<CurrencyRate> getRate(String currencyCode) {
        return apiClient.getCurrentRate(currencyCode);
    }

    public Mono<List<CurrencyRate>> getRates(List<String> currencyCodes) {
        return apiClient.getCurrentRates(currencyCodes);
    }

    public Mono<List<CurrencyRate>> getAllRates() {
        return apiClient.getAllRates();
    }

    public Mono<BigDecimal> convert(String fromCurrency, String toCurrency, BigDecimal amount) {

        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return Mono.just(amount);
        }


        if ("RUB".equalsIgnoreCase(fromCurrency)) {
            return getRate(toCurrency)
                    .map(toRate -> {
                        BigDecimal toNominal = BigDecimal.valueOf(toRate.getNominal());
                        return amount.multiply(toNominal)
                                .divide(toRate.getRate(), 2, RoundingMode.HALF_UP);
                    });
        }


        if ("RUB".equalsIgnoreCase(toCurrency)) {
            return getRate(fromCurrency)
                    .map(fromRate -> {
                        BigDecimal fromNominal = BigDecimal.valueOf(fromRate.getNominal());
                        return amount.multiply(fromRate.getRate())
                                .divide(fromNominal, 2, RoundingMode.HALF_UP);
                    });
        }


        return getRate(fromCurrency)
                .flatMap(fromRate ->
                        getRate(toCurrency)
                                .map(toRate -> {
                                    BigDecimal fromNominal = BigDecimal.valueOf(fromRate.getNominal());
                                    BigDecimal toNominal = BigDecimal.valueOf(toRate.getNominal());

                                    BigDecimal amountInBase = amount.multiply(fromRate.getRate())
                                            .divide(fromNominal, 10, RoundingMode.HALF_UP);

                                    return amountInBase.multiply(toNominal)
                                            .divide(toRate.getRate(), 2, RoundingMode.HALF_UP);
                                })
                );
    }
}