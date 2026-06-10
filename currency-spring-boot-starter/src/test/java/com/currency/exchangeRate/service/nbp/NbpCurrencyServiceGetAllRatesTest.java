package com.currency.exchangeRate.service.nbp;

import com.currency.exchangeRate.FeDto.NbpTableDto;
import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.NbpCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import com.currency.exchangeRate.fixtures.NbpTestFixtures;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class NbpCurrencyServiceGetAllRatesTest extends NbpCurrencyServiceBaseTest {

    @BeforeEach
    void setUp() {
        cacheManager.getCache("exchangeRates").clear();
    }

    @Test
    @DisplayName("Должен вернуть список курсов")
    void shouldReturnRates() {
        when(nbpClient.getTableByDate(eq("a"), anyString()))
                .thenReturn(List.of(NbpTestFixtures.tableDto()));

        List<ExchangeRateFeign> rates = service.getAllRates(TODAY);

        assertThat(rates).hasSize(2);
        assertThat(rates).extracting("charCode").containsExactly("USD", "EUR");
    }

    @Test
    @DisplayName("Должен выбросить исключение при 404")
    void shouldThrowOnNotFound() {
        FeignException notFound = new FeignException.NotFound(
                "Not Found",
                MOCK_REQUEST,
                "404".getBytes(),
                Map.of()
        );

        when(nbpClient.getTableByDate(eq("a"), anyString()))
                .thenThrow(notFound);

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
    }

    @Test
    @DisplayName("Должен выбросить исключение при пустом ответе")
    void shouldThrowOnEmptyResponse() {
        when(nbpClient.getTableByDate(eq("a"), anyString()))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
    }

    @Test
    @DisplayName("Должен выбросить исключение при 503")
    void shouldThrowOnServiceUnavailable() {
        FeignException unavailable = new FeignException.ServiceUnavailable(
                "Service Unavailable",
                MOCK_REQUEST,
                "503".getBytes(),
                Map.of()
        );

        when(nbpClient.getTableByDate(eq("a"), anyString()))
                .thenThrow(unavailable);

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
    }
}