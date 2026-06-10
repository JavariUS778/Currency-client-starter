package com.currency.exchangeRate.ClientFeign;

import com.currency.exchangeRate.FeConfig.CbrFeConfig;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@FeignClient(
        name = "cbr-client",
        url = "${currency.client.urls.cbr}",
        configuration = CbrFeConfig.class
)
public interface CbrFeClient {

    @PostMapping(value = "/DailyInfoWebServ/DailyInfo.asmx",
            consumes = "text/xml",
            produces = "text/xml")
    @Headers("SOAPAction: http://web.cbr.ru/GetCursOnDate")
    String getCursOnDate(@RequestBody String soapEnvelope);

    @PostMapping(value = "/DailyInfoWebServ/DailyInfo.asmx",
            consumes = "text/xml",
            produces = "text/xml")
    @Headers("SOAPAction: http://web.cbr.ru/GetCursDynamic")
    String getCursDynamic(@RequestBody String soapEnvelope);
}
