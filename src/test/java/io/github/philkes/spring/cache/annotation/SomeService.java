package io.github.philkes.spring.cache.annotation;

/**
 * Example Service mocking fetching some data from external source
 */
public class SomeService {
    private int counter = 0;

    public String fetchData(String parameter) {
        return "%s (%d)".formatted(parameter, counter++);
    }
}
