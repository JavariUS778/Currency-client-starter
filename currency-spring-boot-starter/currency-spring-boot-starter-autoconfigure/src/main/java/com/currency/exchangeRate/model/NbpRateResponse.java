package com.currency.exchangeRate.model;

import lombok.Data;

import java.util.List;

@Data
public class NbpRateResponse {
    private String table;
    private String currency;
    private String code;
    private List<NbpRate> rates;
}
