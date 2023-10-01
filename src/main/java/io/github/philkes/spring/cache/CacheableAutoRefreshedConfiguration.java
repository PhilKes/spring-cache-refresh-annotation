package io.github.philkes.spring.cache;

import io.github.philkes.spring.cache.annotation.CacheableAutoRefreshedAnnotationBeanPostProcessor;
import io.github.philkes.spring.cache.interceptor.ParametersKeyGenerator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import static io.github.philkes.spring.cache.annotation.CacheableAutoRefreshedAnnotationBeanPostProcessor.CACHEABLE_AUTO_REFRESHED_PROCESSOR_BEAN;
import static io.github.philkes.spring.cache.interceptor.ParametersKeyGenerator.PARAMETERS_KEY_GENERATOR_BEAN;

/**
 * {@code @Configuration} class that registers a {@link CacheableAutoRefreshedAnnotationBeanPostProcessor}
 * bean capable of processing the @{@link io.github.philkes.spring.cache.annotation.CacheableAutoRefreshed} annotation.
 *
 * <p>This configuration class is automatically imported.
 *
 */
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class CacheableAutoRefreshedConfiguration {

    @Bean(name = PARAMETERS_KEY_GENERATOR_BEAN)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public KeyGenerator parametersKeyGenerator(){
        return new ParametersKeyGenerator();
    }

    @Bean(name = CACHEABLE_AUTO_REFRESHED_PROCESSOR_BEAN)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public CacheableAutoRefreshedAnnotationBeanPostProcessor cacheableAutoRefreshedAnnotationProcessor(CacheManager cacheManager){
        return new CacheableAutoRefreshedAnnotationBeanPostProcessor(cacheManager);
    }
}
