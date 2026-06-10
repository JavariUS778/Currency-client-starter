package com.currency.exchangeRate.service.nbrb;

import com.currency.exchangeRate.FeDto.NbrbCurrencyDto;
import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.NbrbCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import com.currency.exchangeRate.fixtures.NbrbTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@DisplayName("getRateHistory")
class NbrbCurrencyServiceGetRateHistoryTest extends NbrbCurrencyServiceBaseTest {

    private static final LocalDate FROM = LocalDate.of(2026, 5, 1);
    private static final LocalDate TO = LocalDate.of(2026, 5, 25);

    private NbrbCurrencyDto usdCurrency;

    @BeforeEach
    void setUp() {

        usdCurrency = NbrbTestFixtures.usdCurrencyDto();
    }

    @Test
    @DisplayName("Должен вернуть историю курсов за период")
    void shouldReturnRateHistory() {

        when(nbrbClient.getAllCurrencies())
                .thenReturn(List.of(usdCurrency));

        when(nbrbClient.getRateDynamics(eq(431), anyString(), anyString()))
                .thenReturn(List.of(
                        NbrbTestFixtures.shortDto("2026-05-01", new BigDecimal("3.80")),
                        NbrbTestFixtures.shortDto("2026-05-02", new BigDecimal("3.82")),
                        NbrbTestFixtures.shortDto("2026-05-03", new BigDecimal("3.85"))
                ));


        List<ExchangeRateFeign> history = service.getRateHistory(FROM, TO, "USD");


        assertThat(history).hasSize(3);
        assertThat(history.get(0).getDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(history.get(0).getRate()).isEqualByComparingTo(new BigDecimal("3.80"));
        assertThat(history.get(1).getDate()).isEqualTo(LocalDate.of(2026, 5, 2));
        assertThat(history.get(1).getRate()).isEqualByComparingTo(new BigDecimal("3.82"));
        assertThat(history.get(2).getDate()).isEqualTo(LocalDate.of(2026, 5, 3));
        assertThat(history.get(2).getRate()).isEqualByComparingTo(new BigDecimal("3.85"));
    }

    @Test
    @DisplayName("Должен работать с внутренним кодом (431)")
    void shouldWorkWithCurId() {

        when(nbrbClient.getRateDynamics(eq(431), anyString(), anyString()))
                .thenReturn(List.of(
                        NbrbTestFixtures.shortDto("2026-05-01", new BigDecimal("3.80"))
                ));


        List<ExchangeRateFeign> history = service.getRateHistory(FROM, TO, "431");


        assertThat(history).hasSize(1);
    }

    @Test
    @DisplayName("Должен выбросить исключение при пустой истории")
    void shouldThrowOnEmptyHistory() {

        when(nbrbClient.getAllCurrencies())
                .thenReturn(List.of(usdCurrency));


        when(nbrbClient.getRateDynamics(eq(431), anyString(), anyString()))
                .thenReturn(List.of());


        assertThatThrownBy(() -> service.getRateHistory(FROM, TO, "USD"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.HISTORY_OF_CURRENCY_IS_NOT_FOUND);
    }

    @Test
    @DisplayName("Должен выбросить исключение при 404 от API")
    void shouldThrowOnApiNotFound() {

        when(nbrbClient.getAllCurrencies())
                .thenReturn(List.of(usdCurrency));

        when(nbrbClient.getRateDynamics(anyInt(), anyString(), anyString()))
                .thenThrow(feign.FeignException.NotFound.class);

        assertThatThrownBy(() -> service.getRateHistory(FROM, TO, "USD"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.HISTORY_OF_CURRENCY_IS_NOT_FOUND);
    }

    @Test
    @DisplayName("Должен выбросить исключение при неверном диапазоне дат (from > to)")
    void shouldThrowOnInvalidDateRange() {

        assertThatThrownBy(() -> service.getRateHistory(TO, FROM, "USD"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_DATE_RANGE_FORMAT);
    }

    @Test
    @DisplayName("Должен выбросить исключение при превышении 365 дней")
    void shouldThrowOnTooLargeDateRange() {

        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 2); // 367 дней

        assertThatThrownBy(() -> service.getRateHistory(from, to, "USD"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_DATE_RANGE);
    }

    @Test
    @DisplayName("Должен выбросить исключение, если валюта не найдена в справочнике")
    void shouldThrowWhenCurrencyNotFound() {

        when(nbrbClient.getAllCurrencies())
                .thenReturn(List.of(usdCurrency));


        assertThatThrownBy(() -> service.getRateHistory(FROM, TO, "XYZ"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
    }
}