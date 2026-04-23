package com.currency.exchangeRate.model;

import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Data
public class NbpRate {
    private String no;
    private String effectiveDate;
    private BigDecimal mid;
    private String currency;
    private String code;
}
