package com.currency.exchangeRate.fixtures;

import com.currency.exchangeRate.FeDto.NbrbCurrencyDto;
import com.currency.exchangeRate.FeDto.NbrbRateDto;
import com.currency.exchangeRate.FeDto.NbrbShortDto;

import java.math.BigDecimal;

public class NbrbTestFixtures {

    public static NbrbRateDto usdRateDto() {
        NbrbRateDto dto = new NbrbRateDto();
        dto.setCurAbbreviation("USD");
        dto.setCurOfficialRate(new BigDecimal("3.85"));
        dto.setCurScale(1);
        dto.setCurName("Доллар США");
        dto.setDate("2026-05-25T00:00:00");
        return dto;
    }

    public static NbrbRateDto eurRateDto() {
        NbrbRateDto dto = new NbrbRateDto();
        dto.setCurAbbreviation("EUR");
        dto.setCurOfficialRate(new BigDecimal("4.20"));
        dto.setCurScale(1);
        dto.setCurName("Евро");
        dto.setDate("2026-05-25T00:00:00");
        return dto;
    }

    public static NbrbShortDto shortDto(String date, BigDecimal rate) {
        NbrbShortDto dto = new NbrbShortDto();
        dto.setDate(date + "T00:00:00");
        dto.setCurOfficialRate(rate);
        return dto;
    }

    public static NbrbCurrencyDto usdCurrencyDto() {
        NbrbCurrencyDto dto = new NbrbCurrencyDto();
        dto.setCurId(431);
        dto.setCurAbbreviation("USD");
        dto.setCurDateEnd(null);
        return dto;
    }
}
