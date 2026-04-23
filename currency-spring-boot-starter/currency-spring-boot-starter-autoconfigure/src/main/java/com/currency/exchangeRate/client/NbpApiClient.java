package com.currency.exchangeRate.client;

import com.currency.exchangeRate.exception.ClientConnectionException;
import com.currency.exchangeRate.exception.CurrencyClientException;
import com.currency.exchangeRate.model.CurrencyRate;
import com.currency.exchangeRate.model.NbpRate;
import com.currency.exchangeRate.model.NbpRateResponse;
import com.currency.exchangeRate.model.NbpTableResponse;
import com.currency.exchangeRate.properties.CurrencyClientProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NbpApiClient implements CurrencyClient {

        private final WebClient webClient;
        private final ObjectMapper objectMapper;
        private final String baseUrl;

        public NbpApiClient(CurrencyClientProperties properties) {
            this.baseUrl = properties.getProviderConfig().getNbpUrl();
            this.objectMapper = new ObjectMapper();

            try {

                SslContext sslContext = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build();

                HttpClient httpClient = HttpClient.create()
                        .secure(sslSpec -> sslSpec.sslContext(sslContext));

                this.webClient = WebClient.builder()
                        .baseUrl(baseUrl)
                        .clientConnector(new ReactorClientHttpConnector(httpClient))
                        .build();
            } catch (SSLException e) {
                throw new RuntimeException("Ошибка настройки SSL", e);
            }
        }

    @Override
    public Mono<CurrencyRate> getCurrentRate(String currencyCode) {
        return webClient.get()
                .uri("/api/exchangerates/rates/A/" + currencyCode.toUpperCase() + "/")
                .retrieve()
                .bodyToMono(NbpRateResponse.class)
                .map(this::convertToCurrencyRate)
                .onErrorMap(WebClientRequestException.class,
                        e -> new ClientConnectionException("NBP API недоступен", e))
                .onErrorMap(e -> !(e instanceof CurrencyClientException),
                        e -> new CurrencyClientException("Ошибка получения курса из NBP API", e));
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
        return webClient.get()
                .uri("/api/exchangerates/tables/A/")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<NbpTableResponse>>() {})
                .map(this::parseTableResponse)
                .onErrorMap(WebClientRequestException.class,
                        e -> new ClientConnectionException("NBP API недоступен", e))
                .onErrorMap(e -> !(e instanceof CurrencyClientException),
                        e -> new CurrencyClientException("Ошибка получения списка курсов из NBP API", e));
    }

    private List<CurrencyRate> parseTableResponse(List<NbpTableResponse> tables) {
        if (tables == null || tables.isEmpty()) {
            return Collections.emptyList();
        }

        NbpTableResponse table = tables.get(0);

        return table.getRates().stream()
                .map(rate -> CurrencyRate.builder()
                        .provider("NBP")
                        .currencyCode(rate.getCode())
                        .currencyName(rate.getCurrency())
                        .rate(rate.getMid())
                        .nominal(1)
                        .date(LocalDate.now())
                        .build())
                .collect(Collectors.toList());
    }

    private CurrencyRate convertToCurrencyRate(NbpRateResponse response) {
        NbpRate rate = response.getRates().get(0);
        return CurrencyRate.builder()
                .provider("NBP")
                .currencyCode(response.getCode())
                .currencyName(response.getCurrency())
                .rate(rate.getMid())
                .nominal(1)
                .date(LocalDate.now())
                .build();
    }
    }

