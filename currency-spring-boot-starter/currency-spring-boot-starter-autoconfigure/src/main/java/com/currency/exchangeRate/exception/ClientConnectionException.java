package com.currency.exchangeRate.exception;

import lombok.Getter;

@Getter
public class ClientConnectionException extends CurrencyClientException {

    private final String provider;
    private final String url;

    public ClientConnectionException(String provider, String url, Throwable cause) {
        super(String.format("Не удалось подключиться к API %s (%s): %s",
                provider, url, cause.getMessage()), cause);
        this.provider = provider;
        this.url = url;
    }

    public ClientConnectionException(String message, Throwable cause) {
        super(message, cause);
        this.provider = null;
        this.url = null;
    }

}