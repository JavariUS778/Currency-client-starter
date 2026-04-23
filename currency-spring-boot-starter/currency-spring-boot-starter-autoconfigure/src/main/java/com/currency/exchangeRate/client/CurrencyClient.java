package com.currency.exchangeRate.client;

import com.currency.exchangeRate.model.CurrencyRate;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CurrencyClient {


    Mono<CurrencyRate> getCurrentRate(String currencyCode);

    Mono<List<CurrencyRate>> getCurrentRates(List<String> currencyCodes);

    Mono<List<CurrencyRate>> getAllRates();
}
