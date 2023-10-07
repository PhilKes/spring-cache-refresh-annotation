package io.github.philkes.spring.cache.annotation;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Policy;
import io.github.philkes.spring.cache.interceptor.CacheRefresher;
import io.github.philkes.spring.cache.interceptor.ParametersKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link CacheableAutoRefreshedProcessor} when a {@link CaffeineCacheManager} is used for caching
 *
 * @see CacheableAutoRefreshedProcessor
 */
public class CaffeineCacheableAutoRefreshedProcessor extends CacheableAutoRefreshedProcessor<LoadingCache<Object, Object>> {

    public CaffeineCacheableAutoRefreshedProcessor(CacheManager cacheManager) {
        super(cacheManager);
    }

    private LoadingCache<Object, Object> fromCache(com.github.benmanes.caffeine.cache.Cache<Object, Object> cache, CacheLoader<Object, Object> cacheLoader) {
        Policy<Object, Object> policy = cache.policy();
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        policy.refreshAfterWrite().ifPresent(refreshAfterWrite -> builder.refreshAfterWrite(refreshAfterWrite.getRefreshesAfter()));
        policy.expireAfterAccess().ifPresent(expireAfterAcces -> builder.expireAfterAccess(expireAfterAcces.getExpiresAfter()));
        policy.expireAfterWrite().ifPresent(expireAfterWrite -> builder.expireAfterWrite(expireAfterWrite.getExpiresAfter()));
        if (policy.isRecordingStats()) {
            builder.recordStats();
        }
        return builder.build(cacheLoader);
    }


    /**
     * Build {@link CacheRefresher} for Caffeine cache.
     * The internal caffeine caches are customised for manual asynchronous refreshes via {@link LoadingCache#refreshAll(Iterable)}
     */
    @Override
    protected CacheRefresher<LoadingCache<Object, @Nullable Object>> createCacheRefresher(CacheManager cacheManager, String[] cacheNames, Object bean, Method method) {
        Map<String, LoadingCache<Object, @Nullable Object>> caches = Arrays.stream(cacheNames).collect(Collectors.toMap(cacheName -> cacheName, cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache caffeineCache) {
                LoadingCache<Object, Object> customCache = fromCache(caffeineCache.getNativeCache(), key -> method.invoke(bean, ((ParametersKey) key).getParams()));
                ((CaffeineCacheManager) cacheManager).registerCustomCache(cacheName, customCache);
                return customCache;
            } else {
                throw new RuntimeException("Cache '%s' is not of type '%s'!".formatted(cacheName, CaffeineCache.class.getSimpleName()));
            }
        }));

        return new CacheRefresher<>(caches, bean, method) {
            @Override
            public void refreshCache(LoadingCache<Object, Object> cache, String cacheName) {
                cache.refreshAll(cache.asMap().keySet()).thenAccept(updatedCache -> {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Refreshed all (%d) entries in cache '%s'.".formatted(updatedCache.size(), cacheName));
                    }
                });
            }
        };
    }
}
