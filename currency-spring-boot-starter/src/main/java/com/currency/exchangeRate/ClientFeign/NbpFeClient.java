package com.currency.exchangeRate.ClientFeign;

import com.currency.exchangeRate.FeConfig.FeignConfig;
import com.currency.exchangeRate.FeDto.NbpRateDto;
import com.currency.exchangeRate.FeDto.NbpTableDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "nbp-client",
        url = "${currency.client.urls.nbp}",
        configuration = FeignConfig.class
)
public interface NbpFeClient {

    @GetMapping("/api/exchangerates/tables/{table}/{date}")
    List<NbpTableDto> getTableByDate(
            @PathVariable String table,
            @PathVariable String date
    );

    @GetMapping("/api/exchangerates/rates/{table}/{code}/{date}")
    NbpRateDto getRateByDate(
            @PathVariable String table,
            @PathVariable String code,
            @PathVariable String date
    );


    @GetMapping("/api/exchangerates/rates/{table}/{code}/{startDate}/{endDate}")
    NbpRateDto getRatesByDateRange(
            @PathVariable String table,
            @PathVariable String code,
            @PathVariable String startDate,
            @PathVariable String endDate
    );
}
