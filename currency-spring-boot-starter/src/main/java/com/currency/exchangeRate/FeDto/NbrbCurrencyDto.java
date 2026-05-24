package com.currency.exchangeRate.FeDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NbrbCurrencyDto {

    @JsonProperty("Cur_ID")
    private Integer curId;

    @JsonProperty("Cur_ParentID")
    private Integer curParentId;

    @JsonProperty("Cur_Code")
    private String curCode;

    @JsonProperty("Cur_Abbreviation")
    private String curAbbreviation;

    @JsonProperty("Cur_Name")
    private String curName;

    @JsonProperty("Cur_Name_Bel")
    private String curNameBel;

    @JsonProperty("Cur_Name_Eng")
    private String curNameEng;

    @JsonProperty("Cur_QuotName")
    private String curQuotName;

    @JsonProperty("Cur_QuotName_Bel")
    private String curQuotNameBel;

    @JsonProperty("Cur_QuotName_Eng")
    private String curQuotNameEng;

    @JsonProperty("Cur_NameMulti")
    private String curNameMulti;

    @JsonProperty("Cur_Name_BelMulti")
    private String curNameBelMulti;

    @JsonProperty("Cur_Name_EngMulti")
    private String curNameEngMulti;

    @JsonProperty("Cur_Scale")
    private Integer curScale;

    @JsonProperty("Cur_Periodicity")
    private Integer curPeriodicity;

    @JsonProperty("Cur_DateStart")
    private LocalDate curDateStart;

    @JsonProperty("Cur_DateEnd")
    private LocalDate curDateEnd;
}
