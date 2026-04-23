package com.currency.exchangeRate.client;

import com.currency.exchangeRate.exception.ClientConnectionException;
import com.currency.exchangeRate.exception.CurrencyClientException;
import com.currency.exchangeRate.model.CurrencyRate;
import com.currency.exchangeRate.properties.CurrencyClientProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CbrApiClient implements CurrencyClient {

    private final WebClient webClient;
    private final XPath xpath;

    public CbrApiClient(CurrencyClientProperties properties)  {
        String baseUrl = properties.getProviderConfig().getCbrUrl();


        try {
            SslContext sslContext = null;
            sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            SslContext finalSslContext = sslContext;
            HttpClient httpClient = HttpClient.create()
                    .secure(sslSpec -> sslSpec.sslContext(finalSslContext))
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                    .responseTimeout(Duration.ofSeconds(30));
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();

        XPathFactory xPathFactory = XPathFactory.newInstance();
        this.xpath = xPathFactory.newXPath();
    }

    @Override
    public Mono<CurrencyRate> getCurrentRate(String currencyCode) {
        String soapRequest = buildSoapRequest();

        return webClient.post()
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "http://web.cbr.ru/GetCursOnDate")
                .bodyValue(soapRequest)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> {
                    System.out.println("=== Response for " + currencyCode + " ===");
                    System.out.println(response.substring(0, Math.min(500, response.length())));
                })
                .map(response -> parseSoapResponse(response, currencyCode))
                .doOnSuccess(rate -> System.out.println("Successfully parsed rate for " + currencyCode))
                .doOnError(error -> System.err.println("Error for " + currencyCode + ": " + error.getClass().getSimpleName() + " - " + error.getMessage()))
                .onErrorMap(e -> new ClientConnectionException("ЦБ РФ API недоступен: " + e.getMessage(), e));
    }

    @Override
    public Mono<List<CurrencyRate>> getCurrentRates(List<String> currencyCodes) {
        String soapRequest = buildSoapRequest();
        return getAllRates()
                .map(rates -> rates.stream()
                        .filter(rate -> currencyCodes.contains(rate.getCurrencyCode()))
                        .toList());
    }

    @Override
    public Mono<List<CurrencyRate>> getAllRates() {
        String soapRequest = buildSoapRequest();

        return webClient.post()
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "http://web.cbr.ru/GetCursOnDate")
                .bodyValue(soapRequest)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseAllRatesResponse)
                .onErrorMap(e -> new ClientConnectionException("ЦБ РФ API недоступен", e));
    }

    private String buildSoapRequest() {
        LocalDate today = LocalDate.now();
        String date = today.format(DateTimeFormatter.ISO_DATE);

        return """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                               xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                               xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <GetCursOnDate xmlns="http://web.cbr.ru/">
                      <On_date>%s</On_date>
                    </GetCursOnDate>
                  </soap:Body>
                </soap:Envelope>
                """.formatted(date);
    }

    private CurrencyRate parseSoapResponse(String soapResponse, String currencyCode) {
        try {
            Document doc = loadDocument(soapResponse);


            String expression = "//*[local-name()='ValuteCursOnDate']";
            XPathExpression expr = xpath.compile(expression);
            NodeList valuteNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < valuteNodes.getLength(); i++) {
                Node valute = valuteNodes.item(i);

                String code = getNodeValue(valute, ".//*[local-name()='VchCode']");
                if (code == null || code.isEmpty()) {
                    code = getNodeValue(valute, ".//*[local-name()='Vcode']");
                }

                if (currencyCode.equalsIgnoreCase(code)) {
                    String rateStr = getNodeValue(valute, ".//*[local-name()='Vcurs']").replace(',', '.');
                    String nominalStr = getNodeValue(valute, ".//*[local-name()='Vnom']");
                    String name = getNodeValue(valute, ".//*[local-name()='Vname']");

                    return CurrencyRate.builder()
                            .provider("CBR")
                            .currencyCode(code)
                            .currencyName(name != null ? name.trim() : "")
                            .rate(new BigDecimal(rateStr))
                            .nominal(nominalStr != null && !nominalStr.isEmpty() ? Integer.parseInt(nominalStr) : 1)
                            .date(LocalDate.now())
                            .build();
                }
            }

            throw new CurrencyClientException("Валюта " + currencyCode + " не найдена в ответе ЦБ РФ");

        } catch (Exception e) {
            throw new CurrencyClientException("Ошибка парсинга SOAP ответа ЦБ РФ: " + e.getMessage(), e);
        }
    }

    private List<CurrencyRate> parseAllRatesResponse(String soapResponse) {
        List<CurrencyRate> rates = new ArrayList<>();

        try {
            Document doc = loadDocument(soapResponse);


            String expression = "//*[local-name()='ValuteCursOnDate']";
            XPathExpression expr = xpath.compile(expression);
            NodeList valuteNodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            for (int i = 0; i < valuteNodes.getLength(); i++) {
                Node valute = valuteNodes.item(i);

                String code = getNodeValue(valute, ".//*[local-name()='VchCode']");
                if (code == null || code.isEmpty()) {
                    code = getNodeValue(valute, ".//*[local-name()='Vcode']");
                }

                String rateStr = getNodeValue(valute, ".//*[local-name()='Vcurs']");
                if (rateStr == null || rateStr.isEmpty()) {
                    continue;
                }

                rateStr = rateStr.replace(',', '.');
                String nominalStr = getNodeValue(valute, ".//*[local-name()='Vnom']");
                String name = getNodeValue(valute, ".//*[local-name()='Vname']");


                rates.add(CurrencyRate.builder()
                        .provider("CBR")
                        .currencyCode(code)
                        .currencyName(name != null ? name.trim() : "")
                        .rate(new BigDecimal(rateStr))
                        .nominal(nominalStr != null && !nominalStr.isEmpty() ? Integer.parseInt(nominalStr) : 1)
                        .date(LocalDate.now())
                        .build());
            }

        } catch (Exception e) {
            throw new CurrencyClientException("Ошибка парсинга SOAP ответа ЦБ РФ: " + e.getMessage(), e);
        }

        return rates;
    }

    private Document loadDocument(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private String getNodeValue(Node parent, String xpathExpr) {
        try {
            XPathExpression expr = xpath.compile(xpathExpr);
            Node node = (Node) expr.evaluate(parent, XPathConstants.NODE);
            if (node != null) {
                String value = node.getTextContent();
                return value != null ? value.trim() : "";
            }
        } catch (Exception e) {
        }
        return "";
    }
}