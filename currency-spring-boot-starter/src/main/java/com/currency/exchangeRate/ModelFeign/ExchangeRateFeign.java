package com.currency.exchangeRate.ModelFeign;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
public class ExchangeRateFeign {
    private String name;
    private int nominal;
    private BigDecimal rate;
    private String numCode;
    private String charCode;
    private BigDecimal unitRate;
    private LocalDate date;
}
