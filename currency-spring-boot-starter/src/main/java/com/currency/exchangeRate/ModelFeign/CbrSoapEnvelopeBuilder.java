package com.currency.exchangeRate.ModelFeign;


import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class CbrSoapEnvelopeBuilder {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");



    private static final String GET_CURS_ON_DATE_TEMPLATE =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <GetCursOnDate xmlns="http://web.cbr.ru/">
                  <On_date>%s</On_date>
                </GetCursOnDate>
              </soap:Body>
            </soap:Envelope>
            """;

    private static final String GET_CURS_DYNAMIC_TEMPLATE =
            """
        <?xml version="1.0" encoding="utf-8"?>
        <soap:Envelope
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:xsd="http://www.w3.org/2001/XMLSchema"
            xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
            <GetCursDynamic xmlns="http://web.cbr.ru/">
              <FromDate>%s</FromDate>
              <ToDate>%s</ToDate>
              <ValutaCode>%s</ValutaCode>
            </GetCursDynamic>
          </soap:Body>
        </soap:Envelope>
            """;


    public String buildGetCursOnDateRequest(LocalDate date) {
        String formattedDate = date.format(DATE_FORMATTER);
        return String.format(GET_CURS_ON_DATE_TEMPLATE, formattedDate);
    }

    public String buildGetCursDynamicRequest(LocalDate from, LocalDate to, String valutaCode) {
        return String.format(
                GET_CURS_DYNAMIC_TEMPLATE,
                from.format(DATE_FORMATTER),
                to.format(DATE_FORMATTER),
                valutaCode
        );
    }




    public List<ExchangeRateFeign> parseGetCursOnDateResponse(String soapResponse) {
        List<ExchangeRateFeign> rates = new ArrayList<>();

        try {
            Document document = parseXml(soapResponse);
            NodeList valuteNodes = document.getElementsByTagName("ValuteCursOnDate");

            for (int i = 0; i < valuteNodes.getLength(); i++) {
                Element element = (Element) valuteNodes.item(i);
                rates.add(mapToExchangeRate(element));
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга курсов ЦБ", e);
        }

        return rates;
    }

    public List<ExchangeRateFeign> parseGetCursDynamicResponse(String soapResponse) {
        List<ExchangeRateFeign> rates = new ArrayList<>();

        try {
            Document document = parseXml(soapResponse);
            NodeList valuteNodes = document.getElementsByTagName("ValuteCursDynamic");

            for (int i = 0; i < valuteNodes.getLength(); i++) {
                Element element = (Element) valuteNodes.item(i);
                String vcode = getElementText(element, "Vcode");
                String vchCode = getElementText(element, "VchCode");
                log.info("Vcode: {}, VchCode: {}", vcode, vchCode);
                ExchangeRateFeign rate = new ExchangeRateFeign();
                rate.setCharCode(getElementText(element,"Vchcode"));
                rate.setRate(new BigDecimal(getElementText(element, "Vcurs")));
                rate.setNominal(Integer.parseInt(getElementText(element, "Vnom")));
                rate.setName(CBR_CODE_MAP.getOrDefault(getElementText(element,"Vcode"),getElementText(element,"Vchcode") ));
                rate.setNumCode(getElementText(element, "Vcode"));
                rate.setCharCode(CBR_CODE_MAP.getOrDefault(getElementText(element,"Vcode"),getElementText(element,"Vchcode")));
                String dateStr = getElementText(element, "CursDate");
                if (!dateStr.isEmpty()) {
                    rate.setDate(OffsetDateTime.parse(dateStr).toLocalDate());
                }

                rates.add(rate);
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка парсинга динамики курсов ЦБ", e);
        }

        return rates;
    }



    private Document parseXml(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xml.getBytes()));
    }

    private ExchangeRateFeign mapToExchangeRate(Element element) {
        ExchangeRateFeign rate = new ExchangeRateFeign();
        rate.setName(getElementText(element, "Vname"));
        rate.setNominal(Integer.parseInt(getElementText(element, "Vnom")));
        rate.setRate(new BigDecimal(getElementText(element, "Vcurs")));
        rate.setNumCode(getElementText(element, "Vcode"));
        rate.setCharCode(getElementText(element, "VchCode"));
        rate.setUnitRate(new BigDecimal(getElementText(element, "VunitRate")));
        return rate;
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";


    }
    private static final Map<String, String> CBR_CODE_MAP = Map.ofEntries(
            Map.entry("R01235", "USD"),
            Map.entry("R01239", "EUR"),
            Map.entry("R01035", "GBP"),
            Map.entry("R01375", "CNY"),
            Map.entry("R01820", "JPY"),
            Map.entry("R01775", "CHF"),
            Map.entry("R01700", "TRY"),
            Map.entry("R01270", "INR"),
            Map.entry("R01350", "CAD"),
            Map.entry("R01010", "AUD"),
            Map.entry("R01720", "UAH"),
            Map.entry("R01335", "KZT"),
            Map.entry("R01090", "BYN"),
            Map.entry("R01565", "PLN"),
            Map.entry("R01760", "CZK"),
            Map.entry("R01770", "SEK"),
            Map.entry("R01535", "NOK"),
            Map.entry("R01215", "DKK"),
            Map.entry("R01100", "BGN"),
            Map.entry("R01585", "RON"),
            Map.entry("R01135", "HUF")
    );
}