package com.currency.exchangeRate.service.nbp;

import com.currency.exchangeRate.base.NbpCurrencyServiceBaseTest;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DisplayName("Validation")
public class NbpCurrencyServiceValidationTest extends NbpCurrencyServiceBaseTest {

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
        assertThatThrownBy(() -> service.getAllRates(LocalDate.of(2000, 1, 1)))
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
    @DisplayName("Должен выбросить исключение на код длиной не 3")
    void shouldThrowOnInvalidCodeLength() {
        assertThatThrownBy(() -> service.getRateByCode(TODAY, "US"))
                .isInstanceOf(StarterException.class)
                .extracting("error")
                .isEqualTo(StarterError.INVALID_CURRENCY_CODE_FORMAT);
    }
}
