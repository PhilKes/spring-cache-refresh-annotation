package io.github.philkes.spring.cache.annotation;

/**
 * Example Service mocking fetching some data from external source
 */
public class SomeService {

    public String fetchData(String parameter) {
        return "data: %s".formatted(parameter);
    }

    public String fetchData() {
        return "data";
    }
}
