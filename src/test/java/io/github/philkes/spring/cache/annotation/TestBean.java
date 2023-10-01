package io.github.philkes.spring.cache.annotation;

public class TestBean {

    private final SomeService someService;

    public TestBean(SomeService someService) {
        this.someService = someService;
    }

    @CacheableAutoRefreshed(cacheNames = "someCache", fixedDelayString = "${test.cache.fixed.delay}")
    public String fetchSomeData(String parameter){
        return someService.fetchData(parameter);
    }
}
