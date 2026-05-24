package com.currency.exchangeRate.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
public class StarterException extends RuntimeException {
    private final StarterError error;
}
