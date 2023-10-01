package io.github.philkes.spring.cache.annotation;

import org.awaitility.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CacheableAutoRefreshedAnnotationBeanPostProcessorTest {

    @SpyBean
    CacheRefreshedBean cacheRefreshedBean;

    @Autowired
    TestBean testBean;

    @Value("${test.cache.fixed.delay}")
    Integer fixedDelay;


    @Test
    void testCacheableAutoRefreshed() throws InterruptedException {
        // cache empty at start, so assert the method is never called by the scheduler
        await()
                .atMost(new Duration(fixedDelay * 2, TimeUnit.MILLISECONDS))
                .untilAsserted(() -> verify(cacheRefreshedBean, never()).getMessageWithCounter(anyString()));

        String msg = "test message";

        for (int i = 0; i < 3; i++) {
            assertEquals("%s (0)".formatted(msg), testBean.getStringFromCacheRefreshedBean(msg));
        }
        verify(cacheRefreshedBean, times(1)).getMessageWithCounter(anyString());

        // Wait until the cache should have been refreshed 3 times
        await()
                .atMost(new Duration(fixedDelay * 3l + 100, TimeUnit.MILLISECONDS))
                .untilAsserted(() -> verify(cacheRefreshedBean, times(4)).getMessageWithCounter(anyString()));
        assertEquals("%s (3)".formatted(msg), testBean.getStringFromCacheRefreshedBean(msg));
    }

}