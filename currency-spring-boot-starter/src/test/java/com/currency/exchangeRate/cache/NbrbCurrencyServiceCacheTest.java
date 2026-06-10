package com.currency.exchangeRate.cache;

import com.currency.exchangeRate.base.NbrbCurrencyServiceBaseTest;
import com.currency.exchangeRate.fixtures.NbrbTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@DisplayName("Кеширование")
class NbrbCurrencyServiceCacheTest extends NbrbCurrencyServiceBaseTest {

    @Test
    @DisplayName("Повторный вызов не должен вызывать API")
    void shouldNotCallApiOnSecondCall() {

        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenReturn(List.of(NbrbTestFixtures.usdRateDto()));


        service.getAllRates(TODAY);
        service.getAllRates(TODAY);


        verify(nbrbClient, times(1)).getRates(anyString(), eq(0));
    }

    @Test
    @DisplayName("После очистки кеша API вызывается заново")
    void shouldCallApiAfterCacheEvict() {

        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenReturn(List.of(NbrbTestFixtures.usdRateDto()));


        service.getAllRates(TODAY);
        service.clearCache();
        service.getAllRates(TODAY);


        verify(nbrbClient, times(2)).getRates(anyString(), eq(0));
    }

    @Test
    @DisplayName("Разные ключи кеша для разных дат")
    void shouldCacheByDate() {

        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenReturn(List.of(NbrbTestFixtures.usdRateDto()));


        service.getAllRates(TODAY);
        service.getAllRates(TODAY.plusDays(1));


        verify(nbrbClient, times(2)).getRates(anyString(), eq(0));
    }
}