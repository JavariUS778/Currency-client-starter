package com.currency.exchangeRate.ServiceFeign;

import com.currency.exchangeRate.ClientFeign.NbpFeClient;
import com.currency.exchangeRate.FeDto.NbpRateDto;
import com.currency.exchangeRate.FeDto.NbpTableDto;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class NbpCurrencyService implements CurrencyProovider{

    @Lazy
    @Autowired
    private ApplicationContext context;

    @Autowired
    private CacheMetricsExporter cacheMetrics;

    private CurrencyProovider self() {
        return context.getBean(CurrencyProovider.class);
    }

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");


    private final NbpFeClient nbpClient;
    public NbpCurrencyService( NbpFeClient nbpClient) {
        this.nbpClient = nbpClient;
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "'all-' + #date.toString()")
    public List<ExchangeRateFeign> getAllRates(LocalDate date) {

        validateDate(date);


        cacheMetrics.recordMiss("GetAllRates");


        LocalDate businessDay = DateUtil.getLastBusinessDay(date);
        try {

            List<NbpTableDto> tables = nbpClient.getTableByDate("a", businessDay.format(DATE_FORMAT));

            if (tables == null || tables.isEmpty()) {
                throw new StarterException(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
            }

            return mapTableToExchangeRates(tables);

        } catch (StarterException e) {
            throw e;
        } catch (FeignException.NotFound e) {
            log.warn("Таблица курсов не найдена для даты: {}", date, e);
            throw new StarterException(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
        } catch (FeignException.ServiceUnavailable e) {
            log.error("API NBP недоступен", e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        } catch (FeignException.TooManyRequests e) {
            log.error("Превышен лимит запросов к API NBP", e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        } catch (RetryableException e) {
            log.error("Ошибка сети при обращении к API NBP", e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        } catch (FeignException e) {
            log.error("Ошибка Feign при запросе курсов: status={}", e.status(), e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении курсов", e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        }
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "#date.toString() + '-' + #code")
    public ExchangeRateFeign getRateByCode(LocalDate date, String code) {

        validateDate(date);
        validateCurrencyCode(code);

        cacheMetrics.recordMiss("GetRateByCode");


        LocalDate businessDay = DateUtil.getLastBusinessDay(date);

        try {
            NbpRateDto response = nbpClient.getRateByDate(
                    "a", code.toLowerCase(), businessDay.format(DATE_FORMAT)
            );

            if (response == null || response.getRates() == null || response.getRates().isEmpty()) {
                throw new StarterException(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
            }

            return mapRateToExchangeRate(response);

        } catch (FeignException.NotFound e) {
            log.warn("Курс не найден: код={}, дата={}", code, date, e);
            throw new StarterException(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
        } catch (FeignException.BadRequest e) {
            log.error("Некорректный запрос: код={}, дата={}", code, date, e);
            throw new StarterException(StarterError.INVALID_CURRENCY_CODE);
        } catch (FeignException.ServiceUnavailable e) {
            log.error("API NBP недоступен", e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        } catch (RetryableException e) {
            log.error("Ошибка сети при запросе курса {} на {}", code, date, e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        } catch (FeignException e) {
            log.error("Ошибка Feign при запросе курса {}: status={}", code, e.status(), e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении курса {} на {}", code, date, e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        }
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "'batch-' + #date.toString() + '-' + T(String).join(',', #codes.stream().sorted().toList())")
    public List<ExchangeRateFeign> getRatesByCodes(LocalDate date, List<String> codes) {

        validateDate(date);

        cacheMetrics.recordMiss("GetRatesByCodes");
        cacheMetrics.recordHit("GetRatesByCodes");

        if (codes == null || codes.isEmpty()) {
            log.warn("Пустой список кодов валют");
            return List.of();
        }
        List<ExchangeRateFeign> allRates = self().getAllRates(date);

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
        validateDateRange(from, to);

        cacheMetrics.recordMiss("GetRatesHistory");


        try {
            NbpRateDto response = nbpClient.getRatesByDateRange(
                    "a", code.toLowerCase(),
                    from.format(DATE_FORMAT),
                    to.format(DATE_FORMAT)
            );

            if (response == null || response.getRates() == null || response.getRates().isEmpty()) {
                throw new StarterException(StarterError.HISTORY_OF_CURRENCY_IS_NOT_FOUND);
            }

            return mapRateSeriesToExchangeRates(response);

        } catch (FeignException.NotFound e) {
            log.warn("История курса не найдена: код={}, период={}/{}", code, from, to, e);
            throw new StarterException(StarterError.HISTORY_OF_CURRENCY_IS_NOT_FOUND);
        } catch (FeignException.BadRequest e) {
            log.error("Некорректный запрос истории: код={}, период={}/{}", code, from, to, e);


            if (e.getMessage() != null && e.getMessage().contains("Limit exceeded")) {
                throw new StarterException(StarterError.INVALID_DATE_RANGE);
            }

            throw new StarterException(StarterError.INVALID_CURRENCY_CODE);
        } catch (FeignException.ServiceUnavailable e) {
            log.error("API NBP недоступен при запросе истории", e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        } catch (RetryableException e) {
            log.error("Ошибка сети при запросе истории курса {} за {}-{}", code, from, to, e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        } catch (FeignException e) {
            log.error("Ошибка Feign при запросе истории: status={}", e.status(), e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении истории курса", e);
            throw new StarterException(StarterError.NBP_CLIENT_IS_NOT_AVAILABLE);
        }
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "'exchange-' + #date.toString() + '-' + #firstCode + '-' + #secondCode")
    public BigDecimal getExchangeRate(LocalDate date,String  firstCode, String secondCode) {

        validateDate(date);
        validateCurrencyCode(firstCode);
        validateCurrencyCode(secondCode);

        cacheMetrics.recordMiss("GetExchangeRates");


        List<ExchangeRateFeign> allRates= self().getAllRates(date);


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
    private List<ExchangeRateFeign> mapTableToExchangeRates(List<NbpTableDto> tables) {
        if (tables == null || tables.isEmpty()) {
            return List.of();
        }

        NbpTableDto table = tables.get(0);

        if (table.getRates() == null || table.getRates().isEmpty()) {
            log.warn("Таблица {} от {} не содержит курсов", table.getNo(), table.getEffectiveDate());
            return List.of();
        }

        return table.getRates().stream()
                .map(r -> {
                    ExchangeRateFeign rate = new ExchangeRateFeign();
                    rate.setCharCode(r.getCode());
                    rate.setName(r.getCurrency());
                    rate.setNominal(1);
                    rate.setRate(r.getMid() != null ?
                            BigDecimal.valueOf(r.getMid()) : null);
                    rate.setDate(table.getEffectiveDate());
                    return rate;
                })
                .collect(Collectors.toList());
    }

    private ExchangeRateFeign mapRateToExchangeRate(NbpRateDto dto) {
        if (dto == null || dto.getRates() == null || dto.getRates().isEmpty()) {
            return null;
        }

        NbpRateDto.RateValue rateValue = dto.getRates().get(0);

        ExchangeRateFeign rate = new ExchangeRateFeign();
        rate.setCharCode(dto.getCode());
        rate.setName(dto.getCurrency());
        rate.setNominal(1);
        rate.setRate(rateValue.getMid() != null ?
                BigDecimal.valueOf(rateValue.getMid()) : null);
        rate.setDate(rateValue.getEffectiveDate());
        return rate;
    }

    private List<ExchangeRateFeign> mapRateSeriesToExchangeRates(NbpRateDto dto) {
        if (dto == null || dto.getRates() == null) {
            return List.of();
        }

        return dto.getRates().stream()
                .map(r -> {
                    ExchangeRateFeign rate = new ExchangeRateFeign();
                    rate.setCharCode(dto.getCode());
                    rate.setName(dto.getCurrency());
                    rate.setNominal(1);
                    rate.setRate(r.getMid() != null ?
                            BigDecimal.valueOf(r.getMid()) : null);
                    rate.setDate(r.getEffectiveDate());
                    return rate;
                })
                .collect(Collectors.toList());
    }

    private void validateDate(LocalDate date) throws StarterException {
        if (date == null) {
            throw new StarterException(StarterError.INVALID_DATE_FORMAT);
        }

        if (date.isAfter(LocalDate.now())) {
            throw new StarterException(StarterError.FUTURE_DATE_ERROR);
        }

        if (date.isBefore(LocalDate.of(2002, 1, 2))) {
            throw new StarterException(StarterError.MINIMAL_DATE_ERROR);
        }
    }

    private void validateCurrencyCode(String code) {
        if (code == null || code.isBlank()) {
            throw new StarterException(StarterError.INVALID_CURRENCY_CODE);
        }

        if (code.length() != 3) {
            throw new StarterException(StarterError.INVALID_CURRENCY_CODE_FORMAT);
        }

        if (!code.matches("^[a-zA-Z]{3}$")) {
            throw new StarterException(StarterError.INVALID_CURRENCY_CODE);
        }
}

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new StarterException(StarterError.INVALID_DATE_RANGE_FORMAT);
        }

        if (to.toEpochDay() - from.toEpochDay() > 93) {
            throw new StarterException(StarterError.INVALID_DATE_RANGE);
        }
    }

    @Override
    @CacheEvict(value = {"exchangeRates", "exchangeRateHistory"}, allEntries = true)
    public void clearCache() {
        log.info("Кеш NBP очищен");
    }

    @Override
    public String getProviderName(){
        return "NBP";
    }
}
