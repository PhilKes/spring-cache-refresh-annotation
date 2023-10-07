package io.github.philkes.spring.cache.annotation;

import io.github.philkes.spring.cache.interceptor.CacheRefresher;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.cache.CacheManager;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.philkes.spring.cache.interceptor.CacheRefresher.CACHE_REFRESH_METHOD;

/**
 * Bean post-processor that registers methods annotated with
 * {@link CacheableAutoRefreshed @CacheableAutoRefreshed} to be invoked by a
 * {@link TaskScheduler} according to the
 * "fixedRate", "fixedDelay", or "cron" expression provided via the annotation
 * and thereby refresh the annotated method's cache.
 *
 * <p>Autodetects any {@link SchedulingConfigurer} instances in the container,
 * allowing for customization of the scheduler to be used or for fine-grained
 * control over task registration (e.g. registration of {@link Trigger} tasks).
 * See the {@link EnableScheduling @EnableScheduling} javadocs for complete usage
 * details.
 *
 * @param <C> type of the used native-cache, e.g. {@link Map} or {@link com.github.benmanes.caffeine.cache.LoadingCache}
 * @see ScheduledAnnotationBeanPostProcessor
 */
public abstract class CacheableAutoRefreshedProcessor<C> extends ScheduledAnnotationBeanPostProcessor {

    public static final String CACHEABLE_AUTO_REFRESHED_PROCESSOR_BEAN = "io.github.philkes.spring.cache.annotation.internalCacheableAutoRefreshedAnnotationBeanPostProcessor";

    private final CacheManager cacheManager;

    protected final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

    protected CacheableAutoRefreshedProcessor(CacheManager cacheManager) {
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
                annotatedMethods.forEach((method, cacheableRefreshAnnotations) -> cacheableRefreshAnnotations.forEach(cacheableAutoRefreshed -> {
                            // Schedule cache refresher just like with the default @Scheduled annotation
                            CacheRefresher<C> cacheRefresher = createCacheRefresher(cacheManager, cacheableAutoRefreshed.value(), bean, method);
                            try {
                                processScheduled(toScheduled(cacheableAutoRefreshed), cacheRefresher.getClass().getMethod(CACHE_REFRESH_METHOD), cacheRefresher);
                            } catch (NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                        })
                );

                if (logger.isTraceEnabled()) {
                    logger.trace(annotatedMethods.size() + " @CacheableAutoRefreshed methods processed on bean '" + beanName +
                            "': " + annotatedMethods);
                }
            }
        }
        return bean;
    }

    protected abstract CacheRefresher<C> createCacheRefresher(CacheManager cacheManager, String[] cacheNames, Object bean, Method method);

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
}
