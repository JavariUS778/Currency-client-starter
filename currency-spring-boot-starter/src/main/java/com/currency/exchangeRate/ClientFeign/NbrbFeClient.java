package com.currency.exchangeRate.ClientFeign;

import com.currency.exchangeRate.FeConfig.FeignConfig;
import com.currency.exchangeRate.FeDto.NbrbCurrencyDto;
import com.currency.exchangeRate.FeDto.NbrbRateDto;
import com.currency.exchangeRate.FeDto.NbrbShortDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(
        name = "nbrb-bank-client",
        url = "${currency.client.urls.nbrb}",
        configuration = FeignConfig.class
)
public interface NbrbFeClient {

    @GetMapping("/exrates/currencies")
    List<NbrbCurrencyDto> getAllCurrencies();

    @GetMapping("/exrates/rates")
    List<NbrbRateDto> getRates(
            @RequestParam(value = "ondate", required = false) String onDate,
            @RequestParam(value = "periodicity", required = false) Integer periodicity
    );

    @GetMapping("/exrates/rates/{curId}")
    NbrbRateDto getRateByIdAndDate(
            @PathVariable Integer curId,
            @RequestParam("ondate") String onDate
    );

    @GetMapping("/exrates/rates/{code}")
    NbrbRateDto getRateByIsoCodeAndDate(
            @PathVariable String code,
            @RequestParam("parammode") Integer paramMode,
            @RequestParam("ondate") String onDate
    );

    @GetMapping("/exrates/rates/dynamics/{curId}")
    List<NbrbShortDto> getRateDynamics(
            @PathVariable Integer curId,
            @RequestParam("startdate") String startDate,
            @RequestParam("enddate") String endDate
    );
}
