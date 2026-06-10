package com.currency.exchangeRate.service.cbr;

import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.CbrCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
@DisplayName("GetHistoryRates")
public class CbrCurrencyServiceGetHistoryRatesTest extends CbrCurrencyServiceBaseTest {

    @BeforeEach
    void setUp() {
        cacheManager.getCache("exchangeRates").clear();
        cacheManager.getCache("exchangeRateHistory").clear();
    }

    @Test
    @DisplayName("Должен вернуть историю курсов")
    void shouldReturnHistory() {
        String currenciesResponse = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <GetCursOnDateResponse>
                      <GetCursOnDateResult>
                        <ValuteData>
                          <ValuteCursOnDate>
                            <VchCode>USD</VchCode>
                            <Vcurs>75.50</Vcurs>
                            <Vnom>1</Vnom>
                            <Vcode>R01235</Vcode>
                          </ValuteCursOnDate>
                        </ValuteData>
                      </GetCursOnDateResult>
                    </GetCursOnDateResponse>
                  </soap:Body>
                </soap:Envelope>
                """;

        String historyResponse = """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <GetCursDynamicResponse>
                      <GetCursDynamicResult>
                        <ValuteData>
                          <ValuteCursDynamic>
                            <CursDate>2026-06-01</CursDate>
                            <Vcode>R01235</Vcode>
                            <Vnom>1</Vnom>
                            <Vcurs>75.50</Vcurs>
                          </ValuteCursDynamic>
                          <ValuteCursDynamic>
                            <CursDate>2026-06-02</CursDate>
                            <Vcode>R01235</Vcode>
                            <Vnom>1</Vnom>
                            <Vcurs>75.60</Vcurs>
                          </ValuteCursDynamic>
                        </ValuteData>
                      </GetCursDynamicResult>
                    </GetCursDynamicResponse>
                  </soap:Body>
                </soap:Envelope>
                """;

        when(cbrClient.getCursOnDate(anyString())).thenReturn(currenciesResponse);
        when(cbrClient.getCursDynamic(anyString())).thenReturn(historyResponse);

        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 2);
        List<ExchangeRateFeign> history = service.getRateHistory(from, to, "USD");

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getRate()).isEqualByComparingTo(new BigDecimal("75.50"));
        assertThat(history.get(1).getRate()).isEqualByComparingTo(new BigDecimal("75.60"));
    }

    @Test
    @DisplayName("Должен выбросить исключение при неверном диапазоне дат")
    void shouldThrowOnInvalidDateRange() {
        assertThatThrownBy(() -> service.getRateHistory(TODAY, TODAY.minusDays(1), "USD"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_DATE_RANGE_FORMAT);
    }

    @Test
    @DisplayName("Должен выбросить исключение при превышении 365 дней")
    void shouldThrowOnTooLargeRange() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2026, 6, 8); // > 365 дней

        assertThatThrownBy(() -> service.getRateHistory(from, to, "USD"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_DATE_RANGE);
    }
}
