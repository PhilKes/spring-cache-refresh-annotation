package io.github.philkes.spring.cache.annotation;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

import static io.github.philkes.spring.cache.interceptor.ParametersKeyGenerator.PARAMETERS_KEY_GENERATOR_BEAN;

/**
 * Meta-annotation of {@link Cacheable} Annotation indicating that the result of invoking a method (or all methods
 * in a class) can be cached and that the cache for the annotated methods are automatically refreshed.
 *
 * <p>Each time an advised method is invoked, the default {@link Cacheable} behaviour is executed, and the return value cached.
 * Furthermore the cached executions of the methods are refreshed at a given interval/point in time by executing the method
 * with the same parameters and storing the up-to-date return value in the cache again. The time when the cache should
 * automatically be refreshed is specified with either {@link #fixedDelay()}, {@link #fixedDelayString()}, {@link #fixedRate()},
 * {@link #fixedRateString()}, {@link #cron()} annotation parameters, just like for the default {@link Scheduled} annotation.
 *
 * <p>While the cache is auto-refreshed, the old cached value is still present in the cache.</p>
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Reflective
@Cacheable(keyGenerator = PARAMETERS_KEY_GENERATOR_BEAN)
public @interface CacheableAutoRefreshed {

    /**
     * Alias for {@link Cacheable#value()} .
     * <p>Defaults to empty array.
     */
    @AliasFor(annotation = Cacheable.class)
    String[] value() default {};

    /**
     * Alias for {@link Cacheable#cacheNames()} .
     * <p>Defaults to empty array.
     */
    @AliasFor(annotation = Cacheable.class)
    String[] cacheNames() default {};

    /**
     * Alias for {@link Cacheable#condition()}.
     * <p>Defaults to empty string.
     */
    @AliasFor(annotation = Cacheable.class)
    String condition() default "";

    /**
     * Alias for {@link Cacheable#sync()}.
     * <p>Defaults to false.
     */
    @AliasFor(annotation = Cacheable.class)
    boolean sync() default false;

    /**
     * Alias for {@link Cacheable#cacheManager()}.
     * <p>Defaults to empty string.
     */
    @AliasFor(annotation = Cacheable.class)
    String cacheManager() default "";

    /**
     * Alias for {@link Cacheable#cacheResolver()}.
     * <p>Defaults to empty string.
     */
    @AliasFor(annotation = Cacheable.class)
    String cacheResolver() default "";

    /**
     * Defaults to empty string.
     *
     * @see Scheduled#cron()
     */
    String cron() default "";

    /**
     * <p>Defaults to empty string.</p>
     * @see Scheduled#zone() 
     */
    String zone() default "";

    /**
     * Defaults to {@code -1}.
     * @see Scheduled#fixedDelay()
     */
    long fixedDelay() default -1;

    /**
     * Defaults to empty string.
     *
     * @see Scheduled#fixedDelayString()
     */
    String fixedDelayString() default "";

    /**
     * Defaults to {@code -1}.
     *
     * @see Scheduled#fixedRate()
     */
    long fixedRate() default -1;

    /**
     * Defaults to empty string.
     *
     * @see Scheduled#fixedRateString()
     */
    String fixedRateString() default "";

    /**
     * Defaults to {@code -1}.
     *
     * @see Scheduled#initialDelay() ()
     */
    long initialDelay() default -1;

    /**
     * Defaults to empty string.
     *
     * @see Scheduled#initialDelayString()
     */
    String initialDelayString() default "";

    /**
     * Defaults to {@link TimeUnit#MILLISECONDS}.
     *
     * @see Scheduled#timeUnit()
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
