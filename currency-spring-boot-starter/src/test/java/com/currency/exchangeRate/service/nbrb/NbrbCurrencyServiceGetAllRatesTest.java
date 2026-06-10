package com.currency.exchangeRate.service.nbrb;

import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.NbrbCurrencyServiceBaseTest;
import com.currency.exchangeRate.fixtures.NbrbTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@DisplayName("getAllRates")
public class NbrbCurrencyServiceGetAllRatesTest extends NbrbCurrencyServiceBaseTest {


    @Test
    @DisplayName("Должен вернуть список курсов")
    void shouldReturnRates() {
        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenReturn(List.of(
                        NbrbTestFixtures.usdRateDto(),
                        NbrbTestFixtures.eurRateDto()
                ));

        List<ExchangeRateFeign> rates = service.getAllRates(TODAY);

        assertThat(rates).hasSize(2);
        assertThat(rates).extracting("charCode").containsExactly("USD", "EUR");
    }
}
