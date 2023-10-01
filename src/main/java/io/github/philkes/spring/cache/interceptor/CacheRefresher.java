package io.github.philkes.spring.cache.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;

/**
 * Bean that defines non-args method {@link #refreshCache()} that can be scheduled by Spring to refresh all entries of
 * a given cache
 */
public class CacheRefresher {

    public static final String CACHE_REFRESH_METHOD = "refreshCache";

    private final Log logger = LogFactory.getLog(getClass());

    private final CacheManager cacheManager;
    /**
     * Bean on which the {@link #method} should be invoked on
     */
    private final Object bean;
    /**
     * Method that was cached
     */
    private final Method method;
    /**
     * Names of the method invocation's caches
     */
    private final String[] cacheNames;

    public CacheRefresher(CacheManager cacheManager, Object bean, Method method, String[] cacheNames) {
        this.cacheManager = cacheManager;
        this.bean = bean;
        this.method = method;
        this.cacheNames = cacheNames;
    }

    /**
     * Invokes the cached method with the same parameters again and stores the new return value in the cache again.
     * The cache stays stable during executing of this method.
     */
    public void refreshCache() {
        if (cacheNames.length == 0) {
            if (logger.isTraceEnabled()) {
                logger.trace("Not refreshing cache of bean method '%s#%s' since it is empty.".formatted(bean.getClass().getSimpleName(), method.toGenericString()));
            }
            return;
        }
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Cache '%s' does not exist, skipping refresh.".formatted(cacheName));
                }
                continue;
            }
            // TODO generify for other cache implementations?
            if (cache instanceof ConcurrentMapCache concurrentMapCache) {
                ConcurrentMap<Object, Object> nativeCache = concurrentMapCache.getNativeCache();
                if (nativeCache.isEmpty()) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Cache '%s' is empty, skipping refresh.".formatted(cacheName));
                    }
                    continue;
                }
                // TODO parallelize?
                for (Object keyObject : nativeCache.keySet()) {
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
                            logger.debug("Refresh for cache-entry with key '%s' for cache '%s' threw an exception while invoking bean-method '%s#%s'"
                                    .formatted(key, cacheName, bean.getClass().getSimpleName(), method.toGenericString()), e);
                        }
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Refreshed cache-entry with key '%s' for cache '%s'.".formatted(key.toString(), cacheName));
                    }
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Refreshed all entries in cache '%s'.".formatted(cacheName));
                }
            }

        }
    }

}
