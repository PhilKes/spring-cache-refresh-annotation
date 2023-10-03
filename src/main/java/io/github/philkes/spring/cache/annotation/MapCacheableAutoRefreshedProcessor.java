package io.github.philkes.spring.cache.annotation;

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.philkes.spring.cache.interceptor.CacheRefresher;
import io.github.philkes.spring.cache.interceptor.ParametersKey;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link CacheableAutoRefreshedProcessor} when a default {@link Map} cache is used, e.g. {@link org.springframework.cache.concurrent.ConcurrentMapCache}
 *
 * @see CacheableAutoRefreshedProcessor
 */
public class MapCacheableAutoRefreshedProcessor extends CacheableAutoRefreshedProcessor<Map<Object, Object>> {
    public MapCacheableAutoRefreshedProcessor(CacheManager cacheManager) {
        super(cacheManager);
    }

    /**
     * Build {@link CacheRefresher} for {@link Map} cache.
     * The cache refresh is executed synchronously for each cache-entry.
     */
    @Override
    protected CacheRefresher<Map<Object, Object>> createCacheRefresher(CacheManager cacheManager, String[] cacheNames, Object bean, Method method) {
        Map<String, Map<Object, Object>> caches  = Arrays.stream(cacheNames).collect(Collectors.toMap(cacheName -> cacheName, cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null && cache.getNativeCache() != null && cache.getNativeCache() instanceof Map<?, ?> mapCache) {
                return (Map<Object, Object>) mapCache;
            }
            return null;
        }));
        return new CacheRefresher<>(caches, bean, method){
            @Override
            public void refreshCache(Map<Object, Object> cache, String cacheName) {
                // TODO parallelize?
                for (Object keyObject : cache.keySet()) {
                    if (!(keyObject instanceof ParametersKey)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Found cache-key in cache '%s' that is not of type 'ParametersKey': '%s', skipping refresh."
                                    .formatted(cacheName, keyObject.toString()));
                        }
                        continue;
                    }
                    ParametersKey key = (ParametersKey) keyObject;
                    try {
                        cache.put(key, method.invoke(bean, key.getParams()));
                    } catch (Exception e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Refresh for cache-entry with key '%s' for cache '%s' threw an exception while invoking bean-method '%s#%s'. The old cached value is retained for this entry."
                                    .formatted(key, cacheName, bean.getClass().getSimpleName(), method.toGenericString()), e);
                        }
                        continue;
                    }

                    if (logger.isTraceEnabled()) {
                        logger.trace("Refreshed cache-entry with key '%s' for cache '%s'.".formatted(key.toString(), cacheName));
                    }
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Refreshed all (%d) entries in cache '%s'.".formatted(cache.keySet().size(), cacheName));
                }

            }
        };
    }
}
