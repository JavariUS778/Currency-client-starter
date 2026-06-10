package com.currency.exchangeRate.service.cbr;

import com.currency.exchangeRate.base.CbrCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import feign.FeignException;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("NetworkErrors")
public class CbrCurrencyServiceNetworkErrorsTest extends CbrCurrencyServiceBaseTest {

    private static final Request MOCK_REQUEST = Request.create(
            Request.HttpMethod.POST,
            "https://www.cbr.ru/DailyInfoWebServ/DailyInfo.asmx",
            Map.of(),
            null, StandardCharsets.UTF_8, null
    );

    @BeforeEach
    void setUp() {
        cacheManager.getCache("exchangeRates").clear();
        cacheManager.getCache("exchangeRateHistory").clear();
    }

    @Test
    @DisplayName("Должен выбросить исключение при RetryableException")
    void shouldThrowOnRetryable() {
        when(cbrClient.getCursOnDate(anyString()))
                .thenThrow(new RetryableException(
                        500, "Retryable",
                        Request.HttpMethod.POST, (Long) null, MOCK_REQUEST
                ));

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("Должен выбросить исключение при TooManyRequests")
    void shouldThrowOnTooManyRequests() {
        FeignException error = new FeignException.TooManyRequests(
                "Too Many Requests",
                MOCK_REQUEST,
                "429".getBytes(),
                Map.of()
        );
        when(cbrClient.getCursOnDate(anyString())).thenThrow(error);

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
    }
}
