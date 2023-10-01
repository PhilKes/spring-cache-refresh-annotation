package io.github.philkes.spring.cache.annotation;

public class CacheRefreshedBean {
    private int counter = 0;

    public String getMessageWithCounter(String msg) {
        return "%s (%d)".formatted(msg, counter++);
    }
}
