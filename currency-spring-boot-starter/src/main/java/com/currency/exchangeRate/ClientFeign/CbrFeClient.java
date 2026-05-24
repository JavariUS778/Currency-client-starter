package com.currency.exchangeRate.ClientFeign;

import com.currency.exchangeRate.FeConfig.FeignConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
    name = "cbr-currency-client",
    url = "${currency.client.urls.cbr}",
    configuration = FeignConfig.class
        )
public interface CbrFeClient {

    @PostMapping(value = "/DailyInfoWebServ/DailyInfo.asmx",
            consumes = "text/xml",
            produces = "text/xml")
    String getCursOnDate (@RequestBody String SoapEnvelope);

    @PostMapping(value = "/DailyInfoWebServ/DailyInfo.asmx",
            consumes = "text/xml",
            produces = "text/xml")
    String getCursDynamic (@RequestBody String SoapEnvelope);
}
