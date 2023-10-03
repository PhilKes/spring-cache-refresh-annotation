package io.github.philkes.spring.cache.annotation.mapcache;

import io.github.philkes.spring.cache.annotation.SomeService;
import io.github.philkes.spring.cache.annotation.TestBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {ThymeleafAutoConfiguration.class})
@EnableCaching
@EnableScheduling
public class MapTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(MapTestApplication.class, args);
    }

    @Bean
    public SomeService someService(){
        return new SomeService();
    }
    @Bean
    public TestBean testBean(SomeService someService){
        return new TestBean(someService);
    }


}
