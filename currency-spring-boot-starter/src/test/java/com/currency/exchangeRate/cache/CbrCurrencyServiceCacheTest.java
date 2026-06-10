package com.currency.exchangeRate.cache;

import com.currency.exchangeRate.base.CbrCurrencyServiceBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("CbrCurrencyServiceCacheTest")
public class CbrCurrencyServiceCacheTest extends CbrCurrencyServiceBaseTest {

    @BeforeEach
    void setUp() {
        cacheManager.getCache("exchangeRates").clear();
        cacheManager.getCache("exchangeRateHistory").clear();
    }

    @Test
    @DisplayName("Повторный вызов не должен вызывать API")
    void shouldNotCallApiOnSecondCall() {
        String soapResponse = """
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
                            <VunitRate>75.50</VunitRate>
                          </ValuteCursOnDate>
                        </ValuteData>
                      </GetCursOnDateResult>
                    </GetCursOnDateResponse>
                  </soap:Body>
                </soap:Envelope>
                """;

        when(cbrClient.getCursOnDate(anyString())).thenReturn(soapResponse);

        service.getAllRates(TODAY);
        service.getAllRates(TODAY);

        verify(cbrClient, times(1)).getCursOnDate(anyString());
    }

    @Test
    @DisplayName("После очистки кеша API вызывается заново")
    void shouldCallApiAfterCacheEvict() {
        String soapResponse = """
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
                            <VunitRate>75.50</VunitRate>
                          </ValuteCursOnDate>
                        </ValuteData>
                      </GetCursOnDateResult>
                    </GetCursOnDateResponse>
                  </soap:Body>
                </soap:Envelope>
                """;

        when(cbrClient.getCursOnDate(anyString())).thenReturn(soapResponse);

        service.getAllRates(TODAY);
        service.clearCache();
        service.getAllRates(TODAY);

        verify(cbrClient, times(2)).getCursOnDate(anyString());
    }
}
