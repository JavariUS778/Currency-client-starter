package com.currency.exchangeRate.FeDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NbpTableDto {


    @JsonProperty("table")
    private String table;

    @JsonProperty("no")
    private String no;

    @JsonProperty("tradingDate")
    private LocalDate tradingDate;

    @JsonProperty("effectiveDate")
    private LocalDate effectiveDate;

    @JsonProperty("rates")
    private List<NbpTableRate> rates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NbpTableRate {

        @JsonProperty("currency")
        private String currency;

        @JsonProperty("code")
        private String code;

        @JsonProperty("country")
        private String country;

        @JsonProperty("symbol")
        private Integer symbol;

        @JsonProperty("mid")
        private Double mid;

        @JsonProperty("bid")
        private Double bid;

        @JsonProperty("ask")
        private Double ask;
    }
}
