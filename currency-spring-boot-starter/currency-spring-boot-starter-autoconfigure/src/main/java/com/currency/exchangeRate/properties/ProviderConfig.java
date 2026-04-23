package com.currency.exchangeRate.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProviderConfig {
    private String cbrUrl = "https://www.cbr.ru/DailyInfoWebServ/DailyInfo.asmx";
    private String nbpUrl = "https://api.nbp.pl/api";
    private String nbrbUrl = "https://api.nbrb.by/exrates/";
}
