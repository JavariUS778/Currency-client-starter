package com.currency.exchangeRate.service.nbrb;

import com.currency.exchangeRate.base.NbrbCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import com.currency.exchangeRate.fixtures.NbrbTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@DisplayName("getExchangeRate")
class NbrbCurrencyServiceGetExchangeRateTest extends NbrbCurrencyServiceBaseTest {

    @Test
    @DisplayName("Должен вычислить кросс-курс USD/EUR (3.85 / 4.20 = 0.917)")
    void shouldCalculateUsdToEur() {

        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenReturn(List.of(
                        NbrbTestFixtures.usdRateDto(),
                        NbrbTestFixtures.eurRateDto()
                ));


        BigDecimal rate = service.getExchangeRate(TODAY, "USD", "EUR");


        assertThat(rate).isEqualByComparingTo(new BigDecimal("0.917"));
    }

    @Test
    @DisplayName("Должен вычислить кросс-курс EUR/USD (4.20 / 3.85 = 1.091)")
    void shouldCalculateEurToUsd() {

        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenReturn(List.of(
                        NbrbTestFixtures.usdRateDto(),
                        NbrbTestFixtures.eurRateDto()
                ));


        BigDecimal rate = service.getExchangeRate(TODAY, "EUR", "USD");


        assertThat(rate).isEqualByComparingTo(new BigDecimal("1.091"));
    }

    @Test
    @DisplayName("Должен вернуть 1 для одинаковых валют")
    void shouldReturnOneForSameCurrency() {

        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenReturn(List.of(NbrbTestFixtures.usdRateDto()));


        BigDecimal rate = service.getExchangeRate(TODAY, "USD", "USD");


        assertThat(rate).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    @DisplayName("Должен выбросить исключение, если первая валюта не найдена")
    void shouldThrowWhenFirstCurrencyNotFound() {

        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenReturn(List.of(NbrbTestFixtures.usdRateDto()));


        assertThatThrownBy(() -> service.getExchangeRate(TODAY, "EUR", "USD"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
    }

    @Test
    @DisplayName("Должен выбросить исключение, если вторая валюта не найдена")
    void shouldThrowWhenSecondCurrencyNotFound() {

        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenReturn(List.of(NbrbTestFixtures.usdRateDto()));


        assertThatThrownBy(() -> service.getExchangeRate(TODAY, "USD", "EUR"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
    }

    @Test
    @DisplayName("Должен выбросить исключение при пустом коде первой валюты")
    void shouldThrowOnEmptyFirstCode() {

        assertThatThrownBy(() -> service.getExchangeRate(TODAY, "", "EUR"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_CURRENCY_CODE);
    }

    @Test
    @DisplayName("Должен выбросить исключение при пустом коде второй валюты")
    void shouldThrowOnEmptySecondCode() {

        assertThatThrownBy(() -> service.getExchangeRate(TODAY, "USD", ""))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_CURRENCY_CODE);
    }
}