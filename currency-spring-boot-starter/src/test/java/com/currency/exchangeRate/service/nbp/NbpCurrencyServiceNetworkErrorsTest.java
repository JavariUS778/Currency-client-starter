package com.currency.exchangeRate.service.nbp;

import com.currency.exchangeRate.base.NbpCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class NbpCurrencyServiceNetworkErrorsTest extends NbpCurrencyServiceBaseTest {

    @Test
    @DisplayName("Должен выбросить исключение при RetryableException")
    void shouldThrowOnRetryable() {
        when(nbpClient.getTableByDate(eq("a"), anyString()))
                .thenThrow(feign.RetryableException.class);

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("Должен выбросить исключение при TooManyRequests")
    void shouldThrowOnTooManyRequests() {
        when(nbpClient.getTableByDate(eq("a"), anyString()))
                .thenThrow(feign.FeignException.TooManyRequests.class);

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
    }
}

