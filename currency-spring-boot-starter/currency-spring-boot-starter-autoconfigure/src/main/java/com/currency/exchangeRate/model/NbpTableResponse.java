package com.currency.exchangeRate.model;

import lombok.Data;

import java.util.List;


@Data
public class NbpTableResponse {
    private String table;
    private String no;
    private String effectiveDate;
    private List<NbpRate> rates;
}
