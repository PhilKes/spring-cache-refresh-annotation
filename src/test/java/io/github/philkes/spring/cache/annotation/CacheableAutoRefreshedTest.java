package io.github.philkes.spring.cache.annotation;

import org.awaitility.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
public abstract class CacheableAutoRefreshedTest {

    @SpyBean
    SomeService someService;

    @Autowired
    TestBean testBean;

    @Value("${test.cache.fixed.delay}")
    Integer fixedDelay;


    @Test
    void testCacheableAutoRefreshed() throws InterruptedException {
        // cache empty at start, so assert the method is never called by the scheduler
        await()
                .pollDelay(new Duration(fixedDelay * 2, TimeUnit.MILLISECONDS))
                .untilAsserted(() -> verify(someService, never()).fetchData(anyString()));

        for (int i = 0; i < 3; i++) {
            assertEquals("data", testBean.fetchSomeData());
        }
        verify(someService, times(1)).fetchData();

        // Wait until the cache should have been refreshed 3 times
        await()
                .atMost(new Duration(fixedDelay * 3l + 100, TimeUnit.MILLISECONDS))
                // assert fetchData() was called 8 times = 2 initial invoc. + 3 refreshes * 1 cach-entry
                .untilAsserted(() -> verify(someService, times(4)).fetchData());
        assertEquals("data", testBean.fetchSomeData());
    }

    @Test
    void testCacheableAutoRefreshedWithParams() throws InterruptedException {
        // cache empty at start, so assert the method is never called by the scheduler
        await()
                .atMost(new Duration(fixedDelay * 2, TimeUnit.MILLISECONDS))
                .untilAsserted(() -> verify(someService, never()).fetchData(anyString()));

        String msg = "test message";
        String msg2 = "test message2";

        for (int i = 0; i < 3; i++) {
            assertEquals("data: %s".formatted(msg), testBean.fetchSomeData(msg));
            assertEquals("data: %s".formatted(msg2), testBean.fetchSomeData(msg2));
        }
        verify(someService, times(2)).fetchData(anyString());

        // Wait until the cache should have been refreshed 3 times
        await()
                .atMost(new Duration(fixedDelay * 3l + 100, TimeUnit.MILLISECONDS))
                // assert fetchData() was called 8 times = 2 initial invoc. + 3 refreshes * 2 cach-entries
                .untilAsserted(() -> verify(someService, times(8)).fetchData(anyString()));
        assertEquals("data: %s".formatted(msg), testBean.fetchSomeData(msg));
        assertEquals("data: %s".formatted(msg2), testBean.fetchSomeData(msg2));
    }
}
