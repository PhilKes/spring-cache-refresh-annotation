package io.github.philkes.spring.cache;

import io.github.philkes.spring.cache.annotation.CacheableAutoRefreshedProcessor;
import io.github.philkes.spring.cache.annotation.CaffeineCacheableAutoRefreshedProcessor;
import io.github.philkes.spring.cache.annotation.MapCacheableAutoRefreshedProcessor;
import io.github.philkes.spring.cache.interceptor.ParametersKeyGenerator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import static io.github.philkes.spring.cache.annotation.CaffeineCacheableAutoRefreshedProcessor.CACHEABLE_AUTO_REFRESHED_PROCESSOR_BEAN;
import static io.github.philkes.spring.cache.interceptor.ParametersKeyGenerator.PARAMETERS_KEY_GENERATOR_BEAN;

/**
 * {@code @Configuration} class that registers a {@link CaffeineCacheableAutoRefreshedProcessor}
 * bean capable of processing the @{@link io.github.philkes.spring.cache.annotation.CacheableAutoRefreshed} annotation.
 *
 * <p>This configuration class is automatically imported.
 */
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class CacheableAutoRefreshedConfiguration {

    @Bean(name = PARAMETERS_KEY_GENERATOR_BEAN)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public KeyGenerator parametersKeyGenerator() {
        return new ParametersKeyGenerator();
    }


    @Bean(name = CACHEABLE_AUTO_REFRESHED_PROCESSOR_BEAN)
    public CacheableAutoRefreshedProcessor<?> caffeineCacheableAutoRefreshedAnnotationProcessor(CacheManager cacheManager) {
        if (cacheManager instanceof CaffeineCacheManager) {
            return new CaffeineCacheableAutoRefreshedProcessor(cacheManager);
        } else if (cacheManager instanceof ConcurrentMapCacheManager) {
            return new MapCacheableAutoRefreshedProcessor(cacheManager);
        } else {
            throw new IllegalArgumentException("CacheManager of type '%s' is not supported by spring-cache-refresh-annotation!".formatted(cacheManager.getClass().getSimpleName()));
        }
    }

}
