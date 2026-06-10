package com.currency.exchangeRate.base;

import com.currency.exchangeRate.ClientFeign.NbpFeClient;
import com.currency.exchangeRate.ServiceFeign.CurrencyProovider;
import com.currency.exchangeRate.config.NbpCurrencyTestConfig;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {NbpCurrencyTestConfig.class})
@ActiveProfiles("test")
public class NbpCurrencyServiceBaseTest {

    @Autowired
    protected CacheManager cacheManager;

    @Autowired
    protected CurrencyProovider service;

    @MockitoBean
    protected NbpFeClient nbpClient;

    protected static final LocalDate TODAY = LocalDate.of(2026, 5, 26);

    protected static final Request MOCK_REQUEST = Request.create(
            Request.HttpMethod.GET,
            "http://localhost/mock",
            Map.of(),
            null, StandardCharsets.UTF_8, null
    );

}
