# Spring Cache-refresh Annotation

This Spring library provides the [CacheableAutoRefreshed](./src/main/java/io/github/philkes/spring/cache/annotation/CacheableAutoRefreshed.java) annotation which extends the functionality of Spring's [Cacheable](https://www.baeldung.com/spring-cache-tutorial#1-cacheable) annotation, to automatically refresh the cache at a specified point/interval in time.

## Usage
1. Scheduling has to be enabled (see [Enable Support for Scheduling](https://www.baeldung.com/spring-scheduled-tasks#enable-support-for-scheduling))
2. Caching has to be enabled (see [Enable Caching](https://www.baeldung.com/spring-cache-tutorial#enable-caching))
3. Add the dependency to your `pom.xml`:
    ```xml
    <dependency>
        <groupId>io.github.philkes</groupId>
        <artifactId>spring-cache-refresh-annotation</artifactId>
        <version>1.0.0</version>
    </dependency>
    ```
   _Note: The needed Spring Beans are autoconfigured by [CacheableAutoRefreshedConfiguration](./src/main/java/io/github/philkes/spring/cache/CacheableAutoRefreshedConfiguration.java)_
4. Annotate a method with the `@CacheableAutoRefreshed` annotation (see e.g. [TestBean](./src/test/java/io/github/philkes/spring/cache/annotation/TestBean.java)):
   ```java
    @CacheableAutoRefreshed(cacheNames = "someCache", fixedDelay = 10000)
    public String fetchSomeData(String parameter) {
        return someService.fetchData(parameter);
    }
   ```
   In this example `fetchSomeData` return values will be cached with the default behaviour of `@Cacheable` and the cached values will be refreshed every 10 seconds.

   The cache stays stable even while refreshing.

### Supported Caches
* Spring's [Simple Caching](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.caching.provider.simple) with `ConcurrentHashMap`
* [Caffeine](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.caching.provider.caffeine)
* TODO: Redis
* TODO: JCache

### Configuration
To configure the caching itself, the `@CacheableAutoRefresh` provides the same annotation parameters as `@Cacheable` (see [Spring/Cacheable](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/cache/annotation/Cacheable.html)) as well as all the parameters from the `@Scheduled` annotation (see [Spring/Scheduled](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html))


## Dependencies
- Built with Java 17
- [Spring-Framework](https://docs.spring.io/spring-framework/reference/overview.html) for `spring-context`, `spring-boot-autoconfigure`
- [Mockito](https://site.mockito.org/) for mocking tests
- [Awaitility](https://github.com/awaitility/awaitility) for testing spring scheduled tasks


This project is licensed under the terms of the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.txt).
