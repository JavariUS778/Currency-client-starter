package com.currency.exchangeRate.client;

import com.currency.exchangeRate.exception.ClientConnectionException;
import com.currency.exchangeRate.exception.CurrencyClientException;
import com.currency.exchangeRate.model.CurrencyRate;
import com.currency.exchangeRate.model.NbrbRateResponse;
import com.currency.exchangeRate.properties.CurrencyClientProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class NbrbApiClient implements CurrencyClient {

    private final WebClient webClient;

    public NbrbApiClient(CurrencyClientProperties properties) {
        String baseUrl = properties.getProviderConfig().getNbrbUrl();

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    @Override
    public Mono<CurrencyRate> getCurrentRate(String currencyCode) {

        if ("RUB".equalsIgnoreCase(currencyCode)) {
            return Mono.just(CurrencyRate.builder()
                    .provider("NBRB")
                    .currencyCode("RUB")
                    .currencyName("Российский рубль")
                    .rate(java.math.BigDecimal.ONE)
                    .nominal(1)
                    .date(LocalDate.now())
                    .build());
        }


        String uri = UriComponentsBuilder.fromPath("/rates/" + currencyCode.toUpperCase())
                .queryParam("parammode", 2)
                .build()
                .toUriString();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(NbrbRateResponse.class)
                .map(this::convertToCurrencyRate)
                .onErrorMap(WebClientRequestException.class,
                        e -> new ClientConnectionException("API НБРБ недоступен", e))
                .onErrorMap(e -> !(e instanceof CurrencyClientException),
                        e -> new CurrencyClientException("Ошибка получения курса из API НБРБ", e));
    }

    @Override
    public Mono<List<CurrencyRate>> getCurrentRates(List<String> currencyCodes) {
        return getAllRates()
                .map(rates -> rates.stream()
                        .filter(rate -> currencyCodes.contains(rate.getCurrencyCode()))
                        .collect(Collectors.toList()));
    }

    @Override
    public Mono<List<CurrencyRate>> getAllRates() {
        String uri = UriComponentsBuilder.fromPath("/rates")
                .queryParam("periodicity", 0)
                .build()
                .toUriString();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<NbrbRateResponse>>() {})
                .map(responses -> responses.stream()
                        .map(this::convertToCurrencyRate)
                        .collect(Collectors.toList()))
                .onErrorMap(WebClientRequestException.class,
                        e -> new ClientConnectionException("API НБРБ недоступен", e))
                .onErrorMap(e -> !(e instanceof CurrencyClientException),
                        e -> new CurrencyClientException("Ошибка получения списка курсов из API НБРБ", e));
    }

    private CurrencyRate convertToCurrencyRate(NbrbRateResponse response) {
        return CurrencyRate.builder()
                .provider("NBRB")
                .currencyCode(response.getCurAbbreviation())
                .currencyName(response.getCurName())
                .rate(response.getCurOfficialRate())
                .nominal(response.getCurScale())
                .date(response.getDate())
                .providerSpecificId(response.getCurId())
                .build();
    }
}