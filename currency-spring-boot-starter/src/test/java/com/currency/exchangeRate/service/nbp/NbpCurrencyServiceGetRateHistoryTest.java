package com.currency.exchangeRate.service.nbp;

import com.currency.exchangeRate.FeDto.NbpRateDto;
import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.base.NbpCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import com.currency.exchangeRate.fixtures.NbpTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("getRateHistory")
public class NbpCurrencyServiceGetRateHistoryTest extends NbpCurrencyServiceBaseTest {

    @Test
    @DisplayName("Должен вернуть историю курсов")
    void shouldReturnHistory() {
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 25);

        NbpRateDto dto = new NbpRateDto();
        dto.setCode("USD");
        dto.setCurrency("dolar amerykański");
        dto.setRates(List.of(
                NbpTestFixtures.rateValue(LocalDate.of(2026, 5, 1), 3.80),
                NbpTestFixtures.rateValue(LocalDate.of(2026, 5, 2), 3.82)
        ));

        when(nbpClient.getRatesByDateRange(eq("a"), eq("usd"), anyString(), anyString()))
                .thenReturn(dto);

        List<ExchangeRateFeign> history = service.getRateHistory(from, to, "USD");

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(history.get(0).getRate()).isEqualByComparingTo(new BigDecimal("3.80"));
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
    @DisplayName("Должен выбросить исключение при превышении 93 дней")
    void shouldThrowOnTooLargeRange() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 5, 26);

        assertThatThrownBy(() -> service.getRateHistory(from, to, "USD"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_DATE_RANGE);
    }
}
