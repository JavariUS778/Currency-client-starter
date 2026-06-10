package com.currency.exchangeRate.service.nbp;

import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.NbpCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import com.currency.exchangeRate.fixtures.NbpTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("getRateByCode")
public class NbpCurrencyServiceGetRateByCodeTest extends NbpCurrencyServiceBaseTest {

    @Test
    @DisplayName("Должен вернуть курс по коду")
    void shouldReturnRate() {
        when(nbpClient.getRateByDate(eq("a"), eq("usd"), anyString()))
                .thenReturn(NbpTestFixtures.rateDtoUsd());

        ExchangeRateFeign rate = service.getRateByCode(TODAY, "USD");

        assertThat(rate.getCharCode()).isEqualTo("USD");
        assertThat(rate.getRate()).isEqualByComparingTo(new BigDecimal("3.85"));
    }

    @Test
    @DisplayName("Должен выбросить исключение при 404")
    void shouldThrowOnNotFound() {
        cacheManager.getCache("exchangeRates").clear();
        when(nbpClient.getRateByDate(eq("a"), eq("usd"), anyString()))
                .thenThrow(feign.FeignException.NotFound.class);

        assertThatThrownBy(() -> service.getRateByCode(TODAY, "USD"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
    }

    @Test
    @DisplayName("Должен выбросить исключение при 400")
    void shouldThrowOnBadRequest() {
        when(nbpClient.getRateByDate(eq("a"), eq("xxx"), anyString()))
                .thenThrow(feign.FeignException.BadRequest.class);

        assertThatThrownBy(() -> service.getRateByCode(TODAY, "XXX"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_CURRENCY_CODE);
    }
}
