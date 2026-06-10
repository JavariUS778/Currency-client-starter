package com.currency.exchangeRate.service.cbr;

import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.CbrCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("getRateByCode")
public class CbrCurrencyServiceGetRateByCodeTest extends CbrCurrencyServiceBaseTest {

    @BeforeEach
    void setUp() {
        cacheManager.getCache("exchangeRates").clear();
        cacheManager.getCache("exchangeRateHistory").clear();
    }

    @Test
    @DisplayName("Должен вернуть курс по буквенному коду")
    void shouldReturnRateByCode() {
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
            </ValuteData>
          </GetCursOnDateResult>
        </GetCursOnDateResponse>
      </soap:Body>
    </soap:Envelope>
    """;

        when(cbrClient.getCursOnDate(anyString())).thenReturn(soapResponse);

        ExchangeRateFeign rate = service.getRateByCode(TODAY, "USD");

        assertThat(rate.getCharCode()).isEqualTo("USD");
        assertThat(rate.getRate()).isEqualByComparingTo(new BigDecimal("75.50"));
    }

    @Test
    @DisplayName("Должен выбросить исключение, если валюта не найдена")
    void shouldThrowWhenCurrencyNotFound() {
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
            </ValuteData>
          </GetCursOnDateResult>
        </GetCursOnDateResponse>
      </soap:Body>
    </soap:Envelope>
    """;

        when(cbrClient.getCursOnDate(anyString())).thenReturn(soapResponse);

        assertThatThrownBy(() -> service.getRateByCode(TODAY, "EUR"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
    }
}
