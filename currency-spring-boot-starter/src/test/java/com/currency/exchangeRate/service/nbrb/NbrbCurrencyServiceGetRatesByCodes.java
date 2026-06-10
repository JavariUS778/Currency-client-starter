package com.currency.exchangeRate.service.nbrb;

import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.NbrbCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import com.currency.exchangeRate.fixtures.NbrbTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@DisplayName("getRatesByCodes")
class NbrbCurrencyServiceGetRatesByCodes extends NbrbCurrencyServiceBaseTest {

    @Test
    @DisplayName("Должен вернуть курсы для USD и EUR")
    void shouldReturnRatesByCodes() {

        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenReturn(List.of(
                        NbrbTestFixtures.usdRateDto(),
                        NbrbTestFixtures.eurRateDto()
                ));

        List<String> codes = List.of("USD", "EUR");


        List<ExchangeRateFeign> ratesByCodes = service.getRatesByCodes(TODAY, codes);


        assertThat(ratesByCodes).hasSize(2);
        assertThat(ratesByCodes.get(0).getCharCode()).isEqualTo("USD");
        assertThat(ratesByCodes.get(0).getRate()).isEqualByComparingTo(new BigDecimal("3.85"));
        assertThat(ratesByCodes.get(1).getCharCode()).isEqualTo("EUR");
        assertThat(ratesByCodes.get(1).getRate()).isEqualByComparingTo(new BigDecimal("4.20"));
    }

    @Test
    @DisplayName("Должен вернуть пустой список для пустых кодов")
    void shouldReturnEmptyForEmptyCodes() {

        List<ExchangeRateFeign> rates = service.getRatesByCodes(TODAY, Collections.emptyList());


        assertThat(rates).isEmpty();
    }

    @Test
    @DisplayName("Должен выбросить исключение, если API вернул 404")
    void shouldThrowWhenApiReturns404() {

        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenThrow(feign.FeignException.NotFound.class);

        List<String> codes = List.of("USD", "EUR");

        assertThatThrownBy(() -> service.getRatesByCodes(TODAY, codes))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
    }
}