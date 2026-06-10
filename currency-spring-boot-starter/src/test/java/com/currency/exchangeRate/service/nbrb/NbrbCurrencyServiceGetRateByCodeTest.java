package com.currency.exchangeRate.service.nbrb;

import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.NbrbCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import com.currency.exchangeRate.fixtures.NbrbTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@DisplayName("getRateByCode")
public class NbrbCurrencyServiceGetRateByCodeTest extends NbrbCurrencyServiceBaseTest {
    @Test
    @DisplayName("Должен вернуть курс по буквенному коду (USD)")
    void shouldReturnRateByIsoCode() {

        when(nbrbClient.getRateByIsoCodeAndDate(anyString(), anyInt(), anyString()))
                .thenReturn(NbrbTestFixtures.usdRateDto());


        ExchangeRateFeign rate = service.getRateByCode(TODAY, "USD");


        assertThat(rate.getCharCode()).isEqualTo("USD");
        assertThat(rate.getRate()).isEqualByComparingTo(new BigDecimal("3.85"));
    }

    @Test
    @DisplayName("Должен вернуть курс по внутреннему коду (431)")
    void shouldReturnRateByCurId() {

        when(nbrbClient.getRateByIdAndDate(anyInt(), anyString()))
                .thenReturn(NbrbTestFixtures.usdRateDto());


        ExchangeRateFeign rate = service.getRateByCode(TODAY, "431");


        assertThat(rate.getCharCode()).isEqualTo("USD");
    }

    @Test
    @DisplayName("Должен выбросить исключение на некорректный код")
    void shouldThrowOnInvalidCode() {

        when(nbrbClient.getRateByIsoCodeAndDate(anyString(), anyInt(), anyString()))
                .thenThrow(feign.FeignException.BadRequest.class);


        assertThatThrownBy(() -> service.getRateByCode(TODAY, "???"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_CURRENCY_CODE);
    }
}
