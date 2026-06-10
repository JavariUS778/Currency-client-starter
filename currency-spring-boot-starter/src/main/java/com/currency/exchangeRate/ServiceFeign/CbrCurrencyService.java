package com.currency.exchangeRate.ServiceFeign;

import com.currency.exchangeRate.ClientFeign.CbrFeClient;
import com.currency.exchangeRate.ModelFeign.CbrSoapEnvelopeBuilder;
import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.cache.CacheMetricsExporter;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
import com.currency.exchangeRate.utility.DateUtil;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



@Slf4j
public class CbrCurrencyService implements CurrencyProovider {
    @Lazy
    @Autowired
    private CurrencyProovider self;

    @Autowired
    private CacheMetricsExporter cacheMetrics;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CbrFeClient cbrClient;
    private final CbrSoapEnvelopeBuilder envelopeBuilder;

    public CbrCurrencyService(CbrFeClient cbrClient) {
        this.cbrClient = cbrClient;
        this.envelopeBuilder = new CbrSoapEnvelopeBuilder();
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "'all-' + #date.toString()")
    public List<ExchangeRateFeign> getAllRates(LocalDate date) {
        validateDate(date);

        cacheMetrics.recordMiss("GetAllRates");

        LocalDate businessDay = DateUtil.getLastBusinessDay(date);

        try {

            String request = envelopeBuilder.buildGetCursOnDateRequest(businessDay);

            String response = cbrClient.getCursOnDate(request);

            if (response == null || response.isBlank()) {
                throw new StarterException(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
            }

            List<ExchangeRateFeign> rates = envelopeBuilder.parseGetCursOnDateResponse(response);


            if (rates == null || rates.isEmpty()) {
                throw new StarterException(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
            }
            log.info("SOAP GetCursOnDate: date={}", businessDay);
            log.info("SOAP Request: {}", request);
            log.info("SOAP Response status: {}", response != null ? "OK" : "NULL");
            if (response != null) {
                log.info("SOAP Response: {}", response.substring(0, Math.min(500, response.length())));
            }
            return rates;
        }catch (StarterException e) {
                throw e;
        } catch (FeignException.NotFound e) {
            log.warn("Курсы ЦБ РФ не найдены для даты: {}", date, e);
            throw new StarterException(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
        } catch (FeignException.ServiceUnavailable e) {
            log.error("API ЦБ РФ недоступен", e);
            throw new StarterException(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
        } catch (FeignException.TooManyRequests e) {
            log.error("Превышен лимит запросов к API ЦБ РФ", e);
            throw new StarterException(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
        } catch (RetryableException e) {
            log.error("Ошибка сети при обращении к API ЦБ РФ", e);
            throw new StarterException(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
        } catch (FeignException e) {
            log.error("Ошибка Feign при запросе курсов ЦБ РФ: status={}", e.status(), e);
            throw new StarterException(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении курсов ЦБ РФ", e);
            throw new StarterException(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
        }

    }

    @Override
    @Cacheable(value = "exchangeRates", key = "#date.toString() + '-' + #code")
    public ExchangeRateFeign getRateByCode(LocalDate date, String code) {

        validateDate(date);
        validateCurrencyCode(code);

        cacheMetrics.recordMiss("GetRateByCode");

        try {
            List<ExchangeRateFeign> allRates = self.getAllRates(date);

            return allRates.stream()
                    .filter(rate -> matchesCode(rate, code))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.warn("Валюта с кодом {} не найдена на дату {}", code, date);
                        return new StarterException(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
                    });

        } catch (StarterException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при поиске курса ЦБ РФ {} на {}", code, date, e);
            throw new StarterException(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
        }
    }


    @Override
    @Cacheable(value = "exchangeRates", key = "'batch-' + #date.toString() + '-' + T(String).join(',', #codes.stream().sorted().toList())")
    public List<ExchangeRateFeign> getRatesByCodes(LocalDate date, List<String> codes) {
        validateDate(date);

        cacheMetrics.recordMiss("GetRatesByCodes");

        if (codes == null || codes.isEmpty()) {
            log.warn("Пустой список кодов валют");
            return List.of();
        }
        List<ExchangeRateFeign> allRates = getAllRates(date);

        List<ExchangeRateFeign> result = new ArrayList<>();
        List<String> failedCodes = new ArrayList<>();

        for (String code : codes) {
            try {
                validateCurrencyCode(code);
                allRates.stream()
                        .filter(r -> r.getCharCode().equals(code))
                        .findFirst()
                        .ifPresent(result::add);

            } catch (StarterException e) {
                log.warn("Валюта {} не найдена на дату {}", code, date);
                failedCodes.add(code);
            } catch (Exception e) {
                log.error("Ошибка при получении курса для {}: {}", code, e.getMessage());
                failedCodes.add(code);
            }
        }

        if (!failedCodes.isEmpty() && result.isEmpty()) {
            throw new StarterException(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
        }

        if (!failedCodes.isEmpty()) {
            log.warn("Частичный успех. Не найдены валюты: {}",
                    String.join(", ", failedCodes));
        }

        return result;
    }

    @Override
    @Cacheable(value = "exchangeRateHistory", key = "#from + '-' + #to + '-' + #code")
    public List<ExchangeRateFeign> getRateHistory(LocalDate from, LocalDate to, String code) {
        validateDate(from);
        validateDate(to);
        validateCurrencyCode(code);
        validateDateRange(from, to, 365);

        cacheMetrics.recordMiss("GetRateHistory");

        try {
            String internalCode = resolveInternalCode(code);

            log.info("SOAP GetCursDynamic: from={}, to={}, valutaCode={}", from, to, internalCode);

            String request = envelopeBuilder.buildGetCursDynamicRequest(from, to, internalCode);

            log.info("SOAP Request: {}", request);

            String response = cbrClient.getCursDynamic(request);

            log.info("SOAP Response: {}", response.substring(0, Math.min(500, response.length())));

            if (response == null || response.isBlank()) {
                throw new StarterException(StarterError.HISTORY_OF_CURRENCY_IS_NOT_FOUND);
            }

            List<ExchangeRateFeign> history = envelopeBuilder.parseGetCursDynamicResponse(response);

            if (history == null || history.isEmpty()) {
                throw new StarterException(StarterError.HISTORY_OF_CURRENCY_IS_NOT_FOUND);
            }

            return history;

        } catch (FeignException.NotFound e) {
            log.warn("История курса ЦБ РФ не найдена: код={}, период={}/{}", code, from, to, e);
            throw new StarterException(StarterError.HISTORY_OF_CURRENCY_IS_NOT_FOUND);
        } catch (FeignException.BadRequest e) {
            log.error("Некорректный запрос истории ЦБ РФ: код={}, период={}/{}", code, from, to, e);

            if (e.getMessage() != null && e.getMessage().contains("Limit exceeded")) {
                throw new StarterException(StarterError.INVALID_DATE_RANGE);
            }

            throw new StarterException(StarterError.INVALID_CURRENCY_CODE);
        } catch (FeignException.ServiceUnavailable e) {
            log.error("API ЦБ РФ недоступен при запросе истории", e);
            throw new StarterException(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
        } catch (RetryableException e) {
            log.error("Ошибка сети при запросе истории ЦБ РФ {} за {}-{}", code, from, to, e);
            throw new StarterException(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
        } catch (FeignException e) {
            log.error("Ошибка Feign при запросе истории ЦБ РФ: status={}", e.status(), e);
            throw new StarterException(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
        } catch (StarterException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении истории ЦБ РФ", e);
            throw new StarterException(StarterError.CBR_CLIENT_IS_NOT_AVAILABLE);
        }
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "'exchange-' + #date.toString() + '-' + #firstCode + '-' + #secondCode")
    public BigDecimal getExchangeRate(LocalDate date, String  firstCode, String secondCode) {

        validateDate(date);
        validateCurrencyCode(firstCode);
        validateCurrencyCode(secondCode);

        cacheMetrics.recordMiss("GetExchangeRate");

        List<ExchangeRateFeign> allRates= self.getAllRates(date);


        ExchangeRateFeign firstRate = findByCode(allRates,firstCode);
        ExchangeRateFeign secondRate = findByCode(allRates,secondCode);

        return firstRate.getRate()
                .divide(secondRate.getRate(),3, RoundingMode.HALF_UP);
    }

    private ExchangeRateFeign findByCode(List<ExchangeRateFeign> rates,String code){
        return rates.stream()
                .filter(s -> code.equalsIgnoreCase(s.getCharCode()))
                .findFirst()
                .orElseThrow(() -> new StarterException(StarterError.CURRENCY_BY_CODE_NOT_FOUND));
    }

    private boolean matchesCode(ExchangeRateFeign rate, String code) {
        if (code == null) return false;
        return code.equalsIgnoreCase(rate.getCharCode())
                || code.equalsIgnoreCase(rate.getNumCode());
    }

    private String resolveInternalCode(String code) {
        if (code.startsWith("R")) {
            return code;
        }

        String internalCode = CBR_CODE_MAP.get(code.toUpperCase());
        if (internalCode != null) {
            return internalCode;
        }

        throw new StarterException(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new StarterException(StarterError.INVALID_DATE_FORMAT);
        }

        if (date.isAfter(LocalDate.now())) {
            throw new StarterException(StarterError.FUTURE_DATE_ERROR);
        }

        if (date.isBefore(LocalDate.of(1992, 7, 1))) {
            throw new StarterException(StarterError.MINIMAL_DATE_ERROR);
        }
    }

    private void validateCurrencyCode(String code) {
        if (code == null || code.isBlank()) {
            throw new StarterException(StarterError.INVALID_CURRENCY_CODE);
        }

        if (!code.matches("^[a-zA-Z]{3}$") && !code.matches("^[a-zA-Z]\\d+$") && !code.matches("^\\d+$")) {
            throw new StarterException(StarterError.INVALID_CURRENCY_CODE_FORMAT);
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to, int maxDays) {
        if (from.isAfter(to)) {
            throw new StarterException(StarterError.INVALID_DATE_RANGE_FORMAT);
        }

        if (to.toEpochDay() - from.toEpochDay() > maxDays) {
            throw new StarterException(StarterError.INVALID_DATE_RANGE);
        }
    }

    @Override
    @CacheEvict(value = {"exchangeRates", "exchangeRateHistory"}, allEntries = true)
    public void clearCache() {
        log.info("Кеш ЦБ РФ очищен");
    }

    @Override
    public String getProviderName(){
        return "CBR";
    }

    private static final Map<String, String> CBR_CODE_MAP = Map.ofEntries(
            Map.entry("USD", "R01235"),
            Map.entry("EUR", "R01239"),
            Map.entry("GBP", "R01035"),
            Map.entry("CNY", "R01375"),
            Map.entry("JPY", "R01820"),
            Map.entry("CHF", "R01775"),
            Map.entry("TRY", "R01700"),
            Map.entry("INR", "R01270"),
            Map.entry("CAD", "R01350"),
            Map.entry("AUD", "R01010"),
            Map.entry("UAH", "R01720"),
            Map.entry("KZT", "R01335"),
            Map.entry("BYN", "R01090"),
            Map.entry("PLN", "R01565"),
            Map.entry("CZK", "R01760"),
            Map.entry("SEK", "R01770"),
            Map.entry("NOK", "R01535"),
            Map.entry("DKK", "R01215"),
            Map.entry("BGN", "R01100"),
            Map.entry("RON", "R01585"),
            Map.entry("HUF", "R01135")
    );

}