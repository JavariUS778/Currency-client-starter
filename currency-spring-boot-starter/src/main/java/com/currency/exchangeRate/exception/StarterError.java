package com.currency.exchangeRate.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public enum StarterError {


    INVALID_DATE_FORMAT("INVALID_DATE_FORMAT", "Date is not can be null", 505),
    FUTURE_DATE_ERROR("FUTURE_DATE_ERROR", "Date must be present or past ", 505),
    MINIMAL_DATE_ERROR("MINIMAL_DATE_ERROR", "Date must be 2002-01-02 or later ", 505),
    INVALID_CURRENCY_CODE("INVALID_CURRENCY_CODE", "Currency code is not can be null", 505),
    INVALID_CURRENCY_CODE_FORMAT("INVALID_CURRENCY_CODE_FORMAT", "Currency code must be of three characters", 505),
    INVALID_CURRENCY_FORMAT("INVALID_CURRENCY_FORMAT", "Currency code must contain only letters", 505),
    INVALID_DATE_RANGE_FORMAT("INVALID_DATE_RANGE_FORMAT", "The start date is later than the end date", 505),
    INVALID_DATE_RANGE("INVALID_DATE_RANGE", "The date range cannot be longer than 93 days", 505),
    CURRENCY_BY_DATE_NOT_FOUND("CURRENCY_BY_DATE_NOT_FOUND", "Currency by this date is not found", 404),
    CURRENCY_BY_CODE_NOT_FOUND("CURRENCY_BY_CODE_NOT_FOUND", "Currency by this code is not found", 404),
    NBP_CLIENT_IS_NOT_AVAILABLE("NBP_CLIENT_IS_NOT_AVAILABLE", "NBP client is not available.Try again soon", 503),
    NBRB_CLIENT_IS_NOT_AVAILABLE("NBRB_CLIENT_IS_NOT_AVAILABLE", "NBRB client is not available.Try again soon", 503),
    CBR_CLIENT_IS_NOT_AVAILABLE("CBR_CLIENT_IS_NOT_AVAILABLE", "CBR client is not available.Try again soon", 503),
    HISTORY_OF_CURRENCY_IS_NOT_FOUND("HISTORY_OF_CURRENCY_IS_NOT_FOUND", "Currency history of this date range is not found", 404),
    CURRENCY_IS_NOT_FOUND("CURRENCY_IS_NOT_FOUND", "Currency is not found", 404),
    NUMBER_OF_ATTEMPTS_HAS_BEEN_EXCEEDED("NUMBER_OF_ATTEMPTS_HAS_BEEN_EXCEEDED", "The number of attempts has been exceeded", 429),
    OPERATION_WAS_INTERRUPTED("OPERATION_WAS_INTERRUPTED", "The operation was interupted", 505);

    private final String code;
    private final String message;
    private final int status;
}
