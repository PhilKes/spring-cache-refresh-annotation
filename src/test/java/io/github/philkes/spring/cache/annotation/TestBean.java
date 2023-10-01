package io.github.philkes.spring.cache.annotation;

public class TestBean {

    private final CacheRefreshedBean cacheRefreshedBean;

    public TestBean(CacheRefreshedBean cacheRefreshedBean) {
        this.cacheRefreshedBean = cacheRefreshedBean;
    }

    @CacheableAutoRefreshed(cacheNames = "testCache", fixedDelayString = "${test.cache.fixed.delay}")
    public String getStringFromCacheRefreshedBean(String msg){
        return cacheRefreshedBean.getMessageWithCounter(msg);
    }
}
