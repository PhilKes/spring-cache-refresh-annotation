package io.github.philkes.spring.cache.annotation;

import io.github.philkes.spring.cache.interceptor.CacheRefresher;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.cache.CacheManager;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.philkes.spring.cache.interceptor.CacheRefresher.CACHE_REFRESH_METHOD;

public class CacheableAutoRefreshedAnnotationBeanPostProcessor extends ScheduledAnnotationBeanPostProcessor {

    public static final String CACHEABLE_AUTO_REFRESHED_PROCESSOR_BEAN = "io.github.philkes.spring.cache.annotation.internalCacheableAutoRefreshedAnnotationBeanPostProcessor";

    private final CacheManager cacheManager;

    protected final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    public CacheableAutoRefreshedAnnotationBeanPostProcessor(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // implementation taken from ScheduledAnnotationBeanPostProcessor
        if (bean instanceof AopInfrastructureBean || bean instanceof TaskScheduler ||
                bean instanceof ScheduledExecutorService) {
            // Ignore AOP infrastructure such as scoped proxies.
            return bean;
        }

        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        if (!this.nonAnnotatedClasses.contains(targetClass) &&
                AnnotationUtils.isCandidateClass(targetClass, CacheableAutoRefreshed.class)) {
            Map<Method, Set<CacheableAutoRefreshed>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                    (MethodIntrospector.MetadataLookup<Set<CacheableAutoRefreshed>>) method -> {
                        CacheableAutoRefreshed cacheableAutoRefreshedAnnotation = AnnotatedElementUtils.getMergedAnnotation(
                                method, CacheableAutoRefreshed.class);
                        return cacheableAutoRefreshedAnnotation == null ? null : Set.of(cacheableAutoRefreshedAnnotation);
                    });
            if (annotatedMethods.isEmpty()) {
                this.nonAnnotatedClasses.add(targetClass);
                if (logger.isTraceEnabled()) {
                    logger.trace("No @CacheableAutoRefreshed annotations found on bean class: " + targetClass);
                }
            } else {
                annotatedMethods.forEach((method, cacheableRefreshAnnotations) -> {
                            cacheableRefreshAnnotations.forEach(cacheableAutoRefreshed -> {
                                // Schedule cache refresher just like with the default @Scheduled annotation
                                CacheRefresher cacheRefresher = new CacheRefresher(cacheManager, bean, method, cacheableAutoRefreshed.value());
                                try {
                                    processScheduled(toScheduled(cacheableAutoRefreshed), cacheRefresher.getClass().getMethod(CACHE_REFRESH_METHOD), cacheRefresher);
                                } catch (NoSuchMethodException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                );

                if (logger.isTraceEnabled()) {
                    logger.trace(annotatedMethods.size() + " @CacheableAutoRefreshed methods processed on bean '" + beanName +
                            "': " + annotatedMethods);
                }
            }
        }
        return bean;
    }

    private static Scheduled toScheduled(CacheableAutoRefreshed cacheableAutoRefreshed) {
        return new Scheduled() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return cacheableAutoRefreshed.annotationType();
            }

            @Override
            public String cron() {
                return cacheableAutoRefreshed.cron();
            }

            @Override
            public String zone() {
                return cacheableAutoRefreshed.zone();
            }

            @Override
            public long fixedDelay() {
                return cacheableAutoRefreshed.fixedDelay();
            }

            @Override
            public String fixedDelayString() {
                return cacheableAutoRefreshed.fixedDelayString();
            }

            @Override
            public long fixedRate() {
                return cacheableAutoRefreshed.fixedRate();
            }

            @Override
            public String fixedRateString() {
                return cacheableAutoRefreshed.fixedRateString();
            }

            @Override
            public long initialDelay() {
                return cacheableAutoRefreshed.initialDelay();
            }

            @Override
            public String initialDelayString() {
                return cacheableAutoRefreshed.initialDelayString();
            }

            @Override
            public TimeUnit timeUnit() {
                return cacheableAutoRefreshed.timeUnit();
            }
        };
    }


    private static Duration toDuration(long value, TimeUnit timeUnit) {
        try {
            return Duration.of(value, timeUnit.toChronoUnit());
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Unsupported unit " + timeUnit + " for value \"" + value + "\": " + ex.getMessage());
        }
    }

    private static Duration toDuration(String value, TimeUnit timeUnit) {
        if (isDurationString(value)) {
            return Duration.parse(value);
        }
        return toDuration(Long.parseLong(value), timeUnit);
    }

    private static boolean isDurationString(String value) {
        return (value.length() > 1 && (isP(value.charAt(0)) || isP(value.charAt(1))));
    }

    private static boolean isP(char ch) {
        return (ch == 'P' || ch == 'p');
    }


}
