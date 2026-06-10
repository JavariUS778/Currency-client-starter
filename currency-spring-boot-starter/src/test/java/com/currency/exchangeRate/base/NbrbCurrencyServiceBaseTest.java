package com.currency.exchangeRate.base;

import com.currency.exchangeRate.ClientFeign.NbrbFeClient;
import com.currency.exchangeRate.ServiceFeign.CurrencyProovider;
import com.currency.exchangeRate.config.NbrbCurrencyTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        NbrbCurrencyTestConfig.class
})
@ActiveProfiles("test")
public class NbrbCurrencyServiceBaseTest {

    @Autowired
    protected CacheManager cacheManager;

    @MockitoBean
    protected NbrbFeClient nbrbClient;

    @Autowired
    protected CurrencyProovider service;

    protected static final LocalDate TODAY = LocalDate.of(2026, 5, 25);

    @BeforeEach
    void clearCaches() {
        cacheManager.getCache("exchangeRates").clear();
        cacheManager.getCache("exchangeRateHistory").clear();
    }
}
