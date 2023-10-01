package io.github.philkes.spring.cache.annotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {ThymeleafAutoConfiguration.class})
@EnableCaching
@EnableScheduling
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @Bean
    public CacheRefreshedBean beanWithCacheablRefresh(){
        return new CacheRefreshedBean();
    }
    @Bean
    public TestBean testBean(CacheRefreshedBean beanWithCacheablRefresh){
        return new TestBean(beanWithCacheablRefresh);
    }

}
