package com.currency.exchangeRate.service.nbp;

import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.NbpCurrencyServiceBaseTest;
import com.currency.exchangeRate.fixtures.NbpTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("getRatesByCodes")
public class NbpCurrencyServiceGetRatesByCodesTest extends NbpCurrencyServiceBaseTest {

    @Test
    @DisplayName("Должен вернуть курсы для нескольких валют")
    void shouldReturnMultipleRates() {
        when(nbpClient.getTableByDate(eq("a"), anyString()))
                .thenReturn(List.of(NbpTestFixtures.tableDto()));

        List<ExchangeRateFeign> rates = service.getRatesByCodes(TODAY, List.of("USD", "EUR"));

        assertThat(rates).hasSize(2);
        assertThat(rates).extracting("charCode").containsExactly("USD", "EUR");
    }

    @Test
    @DisplayName("Должен вернуть пустой список для пустых кодов")
    void shouldReturnEmptyForEmptyCodes() {
        List<ExchangeRateFeign> rates = service.getRatesByCodes(TODAY, Collections.emptyList());

        assertThat(rates).isEmpty();
    }
}
