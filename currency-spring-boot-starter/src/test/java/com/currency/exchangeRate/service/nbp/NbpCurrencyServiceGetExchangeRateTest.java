package com.currency.exchangeRate.service.nbp;

import com.currency.exchangeRate.base.NbpCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import com.currency.exchangeRate.fixtures.NbpTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("getExchangeRate")
public class NbpCurrencyServiceGetExchangeRateTest extends NbpCurrencyServiceBaseTest {
    @Test
    @DisplayName("Должен вычислить кросс-курс")
    void shouldCalculateCrossRate() {
        when(nbpClient.getTableByDate(eq("a"), anyString()))
                .thenReturn(List.of(NbpTestFixtures.tableDto()));

        BigDecimal rate = service.getExchangeRate(TODAY, "USD", "EUR");

        assertThat(rate).isEqualByComparingTo(new BigDecimal("0.917"));
    }

    @Test
    @DisplayName("Должен выбросить исключение, если валюта не найдена")
    void shouldThrowWhenCurrencyNotFound() {
        when(nbpClient.getTableByDate(eq("a"), anyString()))
                .thenReturn(List.of(NbpTestFixtures.tableDto()));

        assertThatThrownBy(() -> service.getExchangeRate(TODAY, "USD", "GBP"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
    }
}
