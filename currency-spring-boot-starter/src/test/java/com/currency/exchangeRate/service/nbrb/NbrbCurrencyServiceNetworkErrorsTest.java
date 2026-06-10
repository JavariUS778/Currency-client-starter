package com.currency.exchangeRate.service.nbrb;

import com.currency.exchangeRate.base.NbrbCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("Сетевые ошибки")
public class NbrbCurrencyServiceNetworkErrorsTest extends NbrbCurrencyServiceBaseTest {

    @Test
    @DisplayName("Должен выбросить исключение при ServiceUnavailable")
    void shouldThrowOn503() {
        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenThrow(feign.FeignException.ServiceUnavailable.class);

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("Должен выбросить исключение при TooManyRequests")
    void shouldThrowOn429() {
        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenThrow(feign.FeignException.TooManyRequests.class);

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("Должен выбросить исключение при RetryableException")
    void shouldThrowOnRetryable() {
        when(nbrbClient.getRates(anyString(), eq(0)))
                .thenThrow(feign.RetryableException.class);

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
    }
}
