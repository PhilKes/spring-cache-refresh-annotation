package io.github.philkes.spring.cache;

import io.github.philkes.spring.cache.annotation.CacheableAutoRefreshedAnnotationBeanPostProcessor;
import io.github.philkes.spring.cache.interceptor.ParametersKeyGenerator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

import static io.github.philkes.spring.cache.annotation.CacheableAutoRefreshedAnnotationBeanPostProcessor.CACHEABLE_AUTO_REFRESHED_PROCESSOR_BEAN;
import static io.github.philkes.spring.cache.interceptor.ParametersKeyGenerator.PARAMETERS_KEY_GENERATOR_BEAN;

// TODO add conditional if CacheManager exists
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
