package com.currency.exchangeRate.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CurrencyRate {
    private String provider;
    private String currencyCode;
    private String currencyName;
    private BigDecimal rate;
    private int nominal = 1;
    private LocalDate date;
    private Integer providerSpecificId;


    private CurrencyRate(Builder builder) {
        this.provider = builder.provider;
        this.currencyCode = builder.currencyCode;
        this.currencyName = builder.currencyName;
        this.rate = builder.rate;
        this.nominal = builder.nominal;
        this.date = builder.date;
        this.providerSpecificId = builder.providerSpecificId;
    }

    public static Builder builder() {
        return new Builder();
    }



    public static class Builder {
        private String provider;
        private String currencyCode;
        private String currencyName;
        private BigDecimal rate;
        private int nominal = 1;
        private LocalDate date;
        private Integer providerSpecificId;

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public Builder currencyName(String currencyName) {
            this.currencyName = currencyName;
            return this;
        }

        public Builder rate(BigDecimal rate) {
            this.rate = rate;
            return this;
        }

        public Builder nominal(int nominal) {
            this.nominal = nominal;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder providerSpecificId(Integer id) {
            this.providerSpecificId = id;
            return this;
        }

        public CurrencyRate build() {
            return new CurrencyRate(this);
        }
    }
}