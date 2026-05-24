package com.currency.exchangeRate.autoconfigure;

import com.currency.exchangeRate.ClientFeign.CbrFeClient;
import com.currency.exchangeRate.ClientFeign.NbpFeClient;
import com.currency.exchangeRate.ClientFeign.NbrbFeClient;
import com.currency.exchangeRate.ServiceFeign.CurrencyProovider;
import com.currency.exchangeRate.ServiceFeign.NbpCurrencyService;
import com.currency.exchangeRate.ServiceFeign.NbrbCurrencyService;
import com.currency.exchangeRate.ServiceFeign.*;
import com.currency.exchangeRate.exception.GracefulCacheErrorHandler;
import com.currency.exchangeRate.properties.CurrencyClientProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Client;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.time.Duration;

@AutoConfiguration
@EnableConfigurationProperties(CurrencyClientProperties.class)
@EnableFeignClients(basePackages = "com.currency.exchangeRate.ClientFeign")
@ConditionalOnProperty(prefix = "currency.client", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableCaching
public class CurrencyClientAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "currency.client.provider", havingValue = "CBR")
    public CurrencyProovider cbrProvider(CbrFeClient cbrClient) {
        return new CbrCurrencyService(cbrClient);
    }

    @Bean
    @ConditionalOnClass(RedisConnectionFactory.class)
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {


        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);


        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );

        RedisCacheConfiguration historyConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(24))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration("exchangeRateHistory", historyConfig)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "currency.client.cache.enabled", havingValue = "true")
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager concurrentMapCacheManager() {
        return new ConcurrentMapCacheManager("exchangeRates", "exchangeRateHistory");
    }

    @Bean
    @ConditionalOnProperty(name = "currency.client.provider", havingValue = "NBP")
    public CurrencyProovider nbpProvider(NbpFeClient nbpClient) {
        return new NbpCurrencyService(nbpClient);
    }

    @Bean
    @ConditionalOnProperty(name = "currency.client.provider", havingValue = "NBRB")
    public CurrencyProovider nbrbProvider(NbrbFeClient nbrbClient) {
        return new NbrbCurrencyService(nbrbClient);
    }

    @Bean
    public Client feignClient() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            }, new java.security.SecureRandom());

            HostnameVerifier allHostsValid = (hostname, session) -> true;

            return new Client.Default(
                    sslContext.getSocketFactory(),
                    allHostsValid
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new GracefulCacheErrorHandler();
    }
}