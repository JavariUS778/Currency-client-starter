package com.currency.exchangeRate.FeDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NbrbShortDto {


    @JsonProperty("Cur_ID")
    private Integer curId;

    @JsonProperty("Date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String date;

    @JsonProperty("Cur_OfficialRate")
    private BigDecimal curOfficialRate;

}
