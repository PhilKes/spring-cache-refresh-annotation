package io.github.philkes.spring.cache;

import io.github.philkes.spring.cache.annotation.CacheableAutoRefreshedProcessor;
import io.github.philkes.spring.cache.annotation.CaffeineCacheableAutoRefreshedProcessor;
import io.github.philkes.spring.cache.annotation.MapCacheableAutoRefreshedProcessor;
import io.github.philkes.spring.cache.interceptor.ParametersKeyGenerator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;

import static io.github.philkes.spring.cache.annotation.CaffeineCacheableAutoRefreshedProcessor.CACHEABLE_AUTO_REFRESHED_PROCESSOR_BEAN;
import static io.github.philkes.spring.cache.interceptor.ParametersKeyGenerator.PARAMETERS_KEY_GENERATOR_BEAN;

/**
 * Configuration class that registers the correct {@link CacheableAutoRefreshedProcessor} bean, based on the configured {@link CacheManager},
 *  capable of processing the @{@link io.github.philkes.spring.cache.annotation.CacheableAutoRefreshed} annotation.
 *
 * <p>This configuration class is automatically imported.
 */
@AutoConfiguration(after = CacheAutoConfiguration.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class CacheableAutoRefreshedConfiguration {

    @Bean(name = PARAMETERS_KEY_GENERATOR_BEAN)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public KeyGenerator parametersKeyGenerator() {
        return new ParametersKeyGenerator();
    }


    @ConditionalOnBean(CaffeineCacheManager.class)
    @Bean(name = CACHEABLE_AUTO_REFRESHED_PROCESSOR_BEAN)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CaffeineCacheableAutoRefreshedProcessor caffeineCacheableAutoRefreshedAnnotationProcessor(CaffeineCacheManager cacheManager) {
        return new CaffeineCacheableAutoRefreshedProcessor(cacheManager);
    }

    @ConditionalOnBean(ConcurrentMapCacheManager.class)
    @Bean(name = CACHEABLE_AUTO_REFRESHED_PROCESSOR_BEAN)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public MapCacheableAutoRefreshedProcessor mapCacheableAutoRefreshedAnnotationProcessor(CacheManager cacheManager) {
        return new MapCacheableAutoRefreshedProcessor(cacheManager);
    }

}
