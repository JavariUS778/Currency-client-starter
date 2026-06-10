package com.currency.exchangeRate.service.cbr;

import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.CbrCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("CbrCurrencyServiceGetAllRatesTest")
public class CbrCurrencyServiceGetAllRatesTest extends CbrCurrencyServiceBaseTest {
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
    @DisplayName("Должен вернуть список курсов")
    void shouldReturnRates() {
        String soapResponse = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <GetCursOnDateResponse xmlns="http://web.cbr.ru/">
          <GetCursOnDateResult>
            <ValuteData>
              <ValuteCursOnDate>
                <Vname>Доллар США</Vname>
                <Vnom>1</Vnom>
                <Vcurs>75.50</Vcurs>
                <Vcode>840</Vcode>
                <VchCode>USD</VchCode>
                <VunitRate>75.50</VunitRate>
              </ValuteCursOnDate>
              <ValuteCursOnDate>
                <Vname>Евро</Vname>
                <Vnom>1</Vnom>
                <Vcurs>80.25</Vcurs>
                <Vcode>978</Vcode>
                <VchCode>EUR</VchCode>
                <VunitRate>80.25</VunitRate>
              </ValuteCursOnDate>
            </ValuteData>
          </GetCursOnDateResult>
        </GetCursOnDateResponse>
      </soap:Body>
    </soap:Envelope>
    """;

        when(cbrClient.getCursOnDate(anyString())).thenReturn(soapResponse);

        List<ExchangeRateFeign> rates = service.getAllRates(TODAY);

        assertThat(rates).hasSize(2);
        assertThat(rates).extracting("charCode").containsExactly("USD", "EUR");
        assertThat(rates.get(0).getRate()).isEqualByComparingTo(new BigDecimal("75.50"));
        assertThat(rates.get(1).getRate()).isEqualByComparingTo(new BigDecimal("80.25"));
    }

    @Test
    @DisplayName("Должен выбросить исключение при пустом ответе")
    void shouldThrowOnEmptyResponse() {
        when(cbrClient.getCursOnDate(anyString())).thenReturn("");

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
    }

    @Test
    @DisplayName("Должен выбросить исключение при 500 от API")
    void shouldThrowOnInternalServerError() {
        FeignException error = new FeignException.InternalServerError(
                "Internal Server Error",
                MOCK_REQUEST,
                "500".getBytes(),
                Map.of()
        );
        when(cbrClient.getCursOnDate(anyString())).thenThrow(error);

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
    }

    @Test
    @DisplayName("Должен выбросить исключение при 503")
    void shouldThrowOnServiceUnavailable() {
        FeignException error = new FeignException.ServiceUnavailable(
                "Service Unavailable",
                MOCK_REQUEST,
                "503".getBytes(),
                Map.of()
        );
        when(cbrClient.getCursOnDate(anyString())).thenThrow(error);

        assertThatThrownBy(() -> service.getAllRates(TODAY))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
    }
}
