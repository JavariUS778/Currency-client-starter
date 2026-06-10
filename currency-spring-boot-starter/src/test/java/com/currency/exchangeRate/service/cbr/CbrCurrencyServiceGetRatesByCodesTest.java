package com.currency.exchangeRate.service.cbr;

import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.CbrCurrencyServiceBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;



import java.util.Collections;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;


import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("GetRatesByCodes")
public class CbrCurrencyServiceGetRatesByCodesTest extends CbrCurrencyServiceBaseTest {

    @BeforeEach
    void setUp() {
        cacheManager.getCache("exchangeRates").clear();
        cacheManager.getCache("exchangeRateHistory").clear();
    }

    @Test
    @DisplayName("Должен вернуть курсы для нескольких валют")
    void shouldReturnMultipleRates() {
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

        List<ExchangeRateFeign> rates = service.getRatesByCodes(TODAY, List.of("USD", "EUR"));

        assertThat(rates).hasSize(2);
        assertThat(rates).extracting("charCode").containsExactly("USD", "EUR");
    }

    @Test
    @DisplayName("Должен вернуть пустой список для пустых кодов")
    void shouldReturnEmptyForEmptyCodes() {
        List<ExchangeRateFeign> rates = service.getRatesByCodes(TODAY, Collections.emptyList());

        assertThat(rates).isEmpty();
    }
}
