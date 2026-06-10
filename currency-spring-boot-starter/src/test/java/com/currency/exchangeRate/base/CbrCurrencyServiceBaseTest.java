package com.currency.exchangeRate.base;

import com.currency.exchangeRate.ClientFeign.CbrFeClient;
import com.currency.exchangeRate.ServiceFeign.CurrencyProovider;
import com.currency.exchangeRate.config.CbrCurrencyTestConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CbrCurrencyTestConfig.class})
@ActiveProfiles("test")
public class CbrCurrencyServiceBaseTest {
    @Autowired
    protected CurrencyProovider service;

    @Autowired
    protected CacheManager cacheManager;

    @MockitoBean
    protected CbrFeClient cbrClient;

    protected static final LocalDate TODAY = LocalDate.of(2026, 6, 8);
}
