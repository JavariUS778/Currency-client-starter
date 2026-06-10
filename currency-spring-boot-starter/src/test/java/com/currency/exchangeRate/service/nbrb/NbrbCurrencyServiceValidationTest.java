package com.currency.exchangeRate.service.nbrb;

import com.currency.exchangeRate.base.NbrbCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Валидация")
class NbrbCurrencyServiceValidationTest extends NbrbCurrencyServiceBaseTest {

    @Test
    @DisplayName("Должен выбросить исключение на будущую дату")
    void shouldThrowOnFutureDate() {
        assertThatThrownBy(() -> service.getAllRates(LocalDate.now().plusDays(1)))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.FUTURE_DATE_ERROR);
    }

    @Test
    @DisplayName("Должен выбросить исключение на слишком старую дату")
    void shouldThrowOnTooOldDate() {
        assertThatThrownBy(() -> service.getAllRates(LocalDate.of(1999, 1, 1)))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.MINIMAL_DATE_ERROR);
    }

    @Test
    @DisplayName("Должен выбросить исключение на пустой код валюты")
    void shouldThrowOnEmptyCode() {
        assertThatThrownBy(() -> service.getRateByCode(TODAY, ""))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_CURRENCY_CODE);
    }

    @Test
    @DisplayName("Должен выбросить исключение на null-код валюты")
    void shouldThrowOnNullCode() {
        assertThatThrownBy(() -> service.getRateByCode(TODAY, null))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_CURRENCY_CODE);
    }

    @Test
    @DisplayName("Должен выбросить исключение на диапазон дат (from > to)")
    void shouldThrowOnInvalidDateRange() {
        assertThatThrownBy(() -> service.getRateHistory(TODAY, TODAY.minusDays(1), "USD"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_DATE_RANGE_FORMAT);
    }

    @Test
    @DisplayName("Должен выбросить исключение на превышение максимального периода")
    void shouldThrowOnTooLargeRange() {
        assertThatThrownBy(() -> service.getRateHistory(TODAY.minusYears(2), TODAY, "USD"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_DATE_RANGE);
    }
}