package com.currency.exchangeRate.ServiceFeign;

import com.currency.exchangeRate.ClientFeign.NbrbFeClient;
import com.currency.exchangeRate.FeDto.NbrbCurrencyDto;
import com.currency.exchangeRate.FeDto.NbrbRateDto;
import com.currency.exchangeRate.FeDto.NbrbShortDto;
import com.currency.exchangeRate.ModelFeign.ExchangeRateFeign;
import com.currency.exchangeRate.exception.StarterError;
import com.currency.exchangeRate.exception.StarterException;
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
import java.util.stream.Collectors;

@Slf4j
public class NbrbCurrencyService implements CurrencyProovider {

    @Lazy
    @Autowired
    private CurrencyProovider self;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final NbrbFeClient nbrbClient;

    public NbrbCurrencyService(NbrbFeClient nbrbClient) {
        this.nbrbClient = nbrbClient;
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "'all-' + #date.toString()")
    public List<ExchangeRateFeign> getAllRates(LocalDate date) {
        validateDate(date);

        try {
            String dateStr = date.format(DATE_FORMAT);
            List<NbrbRateDto> rates = nbrbClient.getRates(dateStr, 0);

            if (rates == null || rates.isEmpty()) {
                throw new StarterException(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
            }

            return mapToExchangeRates(rates);

        } catch (FeignException.NotFound e) {
            log.warn("Курсы НБРБ не найдены для даты: {}", date, e);
            throw new StarterException(StarterError.CURRENCY_BY_DATE_NOT_FOUND);
        } catch (FeignException.ServiceUnavailable e) {
            log.error("API НБРБ недоступен", e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (FeignException.TooManyRequests e) {
            log.error("Превышен лимит запросов к API НБРБ", e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (RetryableException e) {
            log.error("Ошибка сети при обращении к API НБРБ", e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (FeignException e) {
            log.error("Ошибка Feign при запросе курсов НБРБ: status={}", e.status(), e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (StarterException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении курсов НБРБ", e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        }
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "#date.toString() + '-' + #code")
    public ExchangeRateFeign getRateByCode(LocalDate date, String code) {
        validateDate(date);
        validateCurrencyCode(code);

        try {
            try {
                Integer curId = Integer.parseInt(code);
                NbrbRateDto rate = nbrbClient.getRateByIdAndDate(curId, date.format(DATE_FORMAT));

                if (rate == null) {
                    throw new StarterException(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
                }

                return mapToExchangeRate(rate);

            } catch (NumberFormatException e) {
                NbrbRateDto rate = nbrbClient.getRateByIsoCodeAndDate(
                        code.toUpperCase(),
                        2,
                        date.format(DATE_FORMAT)
                );

                if (rate == null) {
                    throw new StarterException(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
                }

                return mapToExchangeRate(rate);
            }

        } catch (FeignException.NotFound e) {
            log.warn("Курс НБРБ не найден: код={}, дата={}", code, date, e);
            throw new StarterException(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
        } catch (FeignException.BadRequest e) {
            log.error("Некорректный запрос НБРБ: код={}, дата={}", code, date, e);
            throw new StarterException(StarterError.INVALID_CURRENCY_CODE);
        } catch (FeignException.ServiceUnavailable e) {
            log.error("API НБРБ недоступен", e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (RetryableException e) {
            log.error("Ошибка сети при запросе курса НБРБ {} на {}", code, date, e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (FeignException e) {
            log.error("Ошибка Feign при запросе курса НБРБ {}: status={}", code, e.status(), e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (StarterException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении курса НБРБ {} на {}", code, date, e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        }
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "'batch-' + #date.toString() + '-' + T(String).join(',', #codes.stream().sorted().toList())")
    public List<ExchangeRateFeign> getRatesByCodes(LocalDate date, List<String> codes) {
        validateDate(date);

        if (codes == null || codes.isEmpty()) {
            log.warn("Пустой список кодов валют");
            return List.of();
        }
        List<ExchangeRateFeign> allRates = self.getAllRates(date);

        List<ExchangeRateFeign> result = new ArrayList<>();
        List<String> failedCodes = new ArrayList<>();

        for (String code : codes) {
            try {
                validateCurrencyCode(code);
                allRates.stream()
                        .filter(r -> r.getCharCode().equalsIgnoreCase(code))
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

        try {
            Integer curId = resolveCurId(code);

            List<NbrbShortDto> dynamics = nbrbClient.getRateDynamics(
                    curId,
                    from.format(DATE_FORMAT),
                    to.format(DATE_FORMAT)
            );

            if (dynamics == null || dynamics.isEmpty()) {
                throw new StarterException(StarterError.HISTORY_OF_CURRENCY_IS_NOT_FOUND);
            }

            return dynamics.stream()
                    .map(d -> {
                        ExchangeRateFeign rate = new ExchangeRateFeign();
                        rate.setCharCode(code);
                        rate.setRate(d.getCurOfficialRate());
                        rate.setDate(LocalDate.parse(d.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
                        return rate;
                    })
                    .collect(Collectors.toList());

        } catch (FeignException.NotFound e) {
            log.warn("История курса НБРБ не найдена: код={}, период={}/{}", code, from, to, e);
            throw new StarterException(StarterError.HISTORY_OF_CURRENCY_IS_NOT_FOUND);
        } catch (FeignException.BadRequest e) {
            log.error("Некорректный запрос истории НБРБ: код={}, период={}/{}", code, from, to, e);

            if (e.getMessage() != null && e.getMessage().contains("Limit exceeded")) {
                throw new StarterException(StarterError.INVALID_DATE_RANGE);
            }

            throw new StarterException(StarterError.INVALID_CURRENCY_CODE);
        } catch (FeignException.ServiceUnavailable e) {
            log.error("API НБРБ недоступен при запросе истории", e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (RetryableException e) {
            log.error("Ошибка сети при запросе истории НБРБ {} за {}-{}", code, from, to, e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (FeignException e) {
            log.error("Ошибка Feign при запросе истории НБРБ: status={}", e.status(), e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (StarterException e) {
            throw e;
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении истории НБРБ", e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        }
    }

    public List<NbrbCurrencyDto> getCurrencies() {
        try {
            List<NbrbCurrencyDto> currencies = nbrbClient.getAllCurrencies();

            if (currencies == null || currencies.isEmpty()) {
                throw new StarterException(StarterError.CURRENCY_IS_NOT_FOUND);
            }

            return currencies;

        } catch (FeignException.ServiceUnavailable e) {
            log.error("API НБРБ недоступен при получении справочника", e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (RetryableException e) {
            log.error("Ошибка сети при получении справочника НБРБ", e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (FeignException e) {
            log.error("Ошибка Feign при получении справочника НБРБ: status={}", e.status(), e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при получении справочника НБРБ", e);
            throw new StarterException(StarterError.NBRB_CLIENT_IS_NOT_AVAILABLE);
        }
    }

    @Override
    @Cacheable(value = "exchangeRates", key = "'exchange-' + #date.toString() + '-' + #firstCode + '-' + #secondCode")
    public BigDecimal getExchangeRate(LocalDate date,String  firstCode, String secondCode) {

        validateDate(date);
        validateCurrencyCode(firstCode);
        validateCurrencyCode(secondCode);

        List<ExchangeRateFeign> allRates = getAllRatesDirect(date);


        ExchangeRateFeign firstRate = findByCode(allRates,firstCode);
        ExchangeRateFeign secondRate = findByCode(allRates,secondCode);

        return firstRate.getRate()
                .divide(secondRate.getRate(),3, RoundingMode.HALF_UP);
    }

    private List<ExchangeRateFeign> getAllRatesDirect(LocalDate date) {
        String dateStr = date.format(DATE_FORMAT);
        List<NbrbRateDto> rates = nbrbClient.getRates(dateStr, 0);
        return mapToExchangeRates(rates);
    }

    private ExchangeRateFeign findByCode(List<ExchangeRateFeign> rates,String code){
        return rates.stream()
                .filter(s -> code.equalsIgnoreCase(s.getCharCode()))
                .findFirst()
                .orElseThrow(() -> new StarterException(StarterError.CURRENCY_BY_CODE_NOT_FOUND));
    }

    public NbrbCurrencyDto findCurrencyByAbbreviation(String abbreviation) {
        validateCurrencyCode(abbreviation);

        List<NbrbCurrencyDto> currencies = getCurrencies();
        LocalDate today = LocalDate.now();


        return currencies.stream()
                .filter(c -> abbreviation.equalsIgnoreCase(c.getCurAbbreviation()))
                .filter(c -> c.getCurDateEnd() == null || c.getCurDateEnd().isAfter(today))
                .findFirst()
                .orElseGet(() ->
                        currencies.stream()
                                .filter(c -> abbreviation.equalsIgnoreCase(c.getCurAbbreviation()))
                                .findFirst()
                                .orElseThrow(() -> {
                                    log.warn("Валюта не найдена по коду: {}", abbreviation);
                                    return new StarterException(StarterError.CURRENCY_BY_CODE_NOT_FOUND);
                                })
                );
    }

    private List<ExchangeRateFeign> mapToExchangeRates(List<NbrbRateDto> dtos) {
        return dtos.stream()
                .map(this::mapToExchangeRate)
                .collect(Collectors.toList());
    }

    private ExchangeRateFeign mapToExchangeRate(NbrbRateDto dto) {
        ExchangeRateFeign rate = new ExchangeRateFeign();
        rate.setCharCode(dto.getCurAbbreviation());
        rate.setName(dto.getCurName());
        rate.setNominal(dto.getCurScale());
        rate.setDate(LocalDate.parse(dto.getDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        BigDecimal ratePerUnit = dto.getCurOfficialRate()
                .divide(BigDecimal.valueOf(dto.getCurScale()), 6, RoundingMode.HALF_UP);
        rate.setRate(ratePerUnit);

        return rate;
    }

    private Integer resolveCurId(String code) {
        try {
            return Integer.parseInt(code);
        } catch (NumberFormatException e) {
            return findCurrencyByAbbreviation(code).getCurId();
        }
    }



    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new StarterException(StarterError.INVALID_DATE_FORMAT);
        }

        if (date.isAfter(LocalDate.now())) {
            throw new StarterException(StarterError.FUTURE_DATE_ERROR);
        }

        if (date.isBefore(LocalDate.of(2000, 1, 1))) {
            throw new StarterException(StarterError.MINIMAL_DATE_ERROR);
        }
    }

    private void validateCurrencyCode(String code) {
        if (code == null || code.isBlank()) {
            throw new StarterException(StarterError.INVALID_CURRENCY_CODE);
        }

        if (code.length() != 3 && !code.matches("^\\d+$")) {

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

    @CacheEvict(value = "exchangeRates", allEntries = true)
    public void clearCache() {
        log.info("Кеш курсов очищен");
    }

    @Override
    public String getProviderName(){
        return "NBRB";
    }
}