package com.currency.exchangeRate.cache;

import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.NbpCurrencyServiceBaseTest;
import com.currency.exchangeRate.fixtures.NbpTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;

import java.util.List;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class NbpCurrencyServiceCacheTest extends NbpCurrencyServiceBaseTest {

    @Test
    @DisplayName("Повторный вызов не должен вызывать API")
    void shouldCacheResult() {
        when(nbpClient.getTableByDate(eq("a"), anyString()))
                .thenReturn(List.of(NbpTestFixtures.tableDto()));

        List<ExchangeRateFeign> rates1 = service.getAllRates(TODAY);

        Cache cache = cacheManager.getCache("exchangeRates");
        assertThat(cache.get("all-" + TODAY)).isNotNull();

        List<ExchangeRateFeign> rates2 = service.getAllRates(TODAY);


        assertThat(rates1).isEqualTo(rates2);
    }

    @Test
    @DisplayName("После очистки кеша API вызывается заново")
    void shouldCallApiAfterCacheEvict() {
        when(nbpClient.getTableByDate(eq("a"), anyString()))
                .thenReturn(List.of(NbpTestFixtures.tableDto()));

        service.getAllRates(TODAY);
        service.clearCache();
        service.getAllRates(TODAY);

        verify(nbpClient, times(2)).getTableByDate(eq("a"), anyString());
    }
}
