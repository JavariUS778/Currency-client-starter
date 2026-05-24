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
public class NbpRateDto {

    @JsonProperty("table")
    private String table;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("code")
    private String code;

    @JsonProperty("rates")
    private List<RateValue> rates;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RateValue {

        @JsonProperty("no")
        private String no;

        @JsonProperty("effectiveDate")
        private LocalDate effectiveDate;

        @JsonProperty("tradingDate")
        private LocalDate tradingDate;

        @JsonProperty("mid")
        private Double mid;

        @JsonProperty("bid")
        private Double bid;

        @JsonProperty("ask")
        private Double ask;
    }
}
