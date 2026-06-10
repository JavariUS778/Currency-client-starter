package com.currency.exchangeRate.ServiceFeign;

import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface CurrencyProovider {

    List<ExchangeRateFeign> getAllRates(LocalDate date);

    ExchangeRateFeign getRateByCode(LocalDate date, String code);

    List<ExchangeRateFeign> getRatesByCodes(LocalDate date, List<String> codes);

    List<ExchangeRateFeign> getRateHistory(LocalDate from, LocalDate to, String code);

    BigDecimal getExchangeRate(LocalDate date, String firstCode, String secondCode);

    void clearCache();

    String getProviderName();
}
