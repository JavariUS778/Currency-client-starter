package com.currency.exchangeRate.fixtures;

import com.currency.exchangeRate.FeDto.NbpRateDto;
import com.currency.exchangeRate.FeDto.NbpTableDto;

import java.time.LocalDate;
import java.util.List;

public class NbpTestFixtures {

    public static NbpTableDto tableDto() {
        NbpTableDto dto = new NbpTableDto();
        dto.setTable("A");
        dto.setNo("100/A/NBP/2026");
        dto.setEffectiveDate(LocalDate.of(2026, 5, 26));
        dto.setRates(List.of(tableRateUsd(), tableRateEur()));
        return dto;
    }

    public static NbpTableDto.NbpTableRate tableRateUsd() {
        NbpTableDto.NbpTableRate rate = new NbpTableDto.NbpTableRate();
        rate.setCurrency("dolar amerykański");
        rate.setCode("USD");
        rate.setMid(3.85);
        return rate;
    }

    public static NbpTableDto.NbpTableRate tableRateEur() {
        NbpTableDto.NbpTableRate rate = new NbpTableDto.NbpTableRate();
        rate.setCurrency("euro");
        rate.setCode("EUR");
        rate.setMid(4.20);
        return rate;
    }



    public static NbpRateDto rateDtoUsd() {
        NbpRateDto dto = new NbpRateDto();
        dto.setTable("A");
        dto.setCurrency("dolar amerykański");
        dto.setCode("USD");
        dto.setRates(List.of(rateValue(LocalDate.of(2026, 5, 26), 3.85)));
        return dto;
    }

    public static NbpRateDto rateDtoEur() {
        NbpRateDto dto = new NbpRateDto();
        dto.setTable("A");
        dto.setCurrency("euro");
        dto.setCode("EUR");
        dto.setRates(List.of(rateValue(LocalDate.of(2026, 5, 26), 4.20)));
        return dto;
    }

    public static NbpRateDto.RateValue rateValue(LocalDate date, double mid) {
        NbpRateDto.RateValue value = new NbpRateDto.RateValue();
        value.setNo("100/A/NBP/2026");
        value.setEffectiveDate(date);
        value.setMid(mid);
        return value;
    }



    public static NbpTableDto.NbpTableRate tableRateUsdBidAsk() {
        NbpTableDto.NbpTableRate rate = new NbpTableDto.NbpTableRate();
        rate.setCurrency("dolar amerykański");
        rate.setCode("USD");
        rate.setBid(3.80);
        rate.setAsk(3.90);
        return rate;
    }

    public static NbpRateDto.RateValue rateValueBidAsk(LocalDate date, double bid, double ask) {
        NbpRateDto.RateValue value = new NbpRateDto.RateValue();
        value.setNo("50/C/NBP/2026");
        value.setEffectiveDate(date);
        value.setBid(bid);
        value.setAsk(ask);
        return value;
    }
}