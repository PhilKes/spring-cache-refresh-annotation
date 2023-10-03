package io.github.philkes.spring.cache.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Bean that defines non-args method {@link #refreshCaches()} that can be scheduled by Spring to refresh all entries of
 * a given cache
 */
public abstract class CacheRefresher<NativeCache> {

    public static final String CACHE_REFRESH_METHOD = "refreshCaches";

    private final Log logger = LogFactory.getLog(getClass());

    private final Map<String, NativeCache> caches;
    /**
     * Bean on which the {@link #method} should be invoked on
     */
    private final Object bean;
    /**
     * Method that was cached
     */
    private final Method method;

    public CacheRefresher(Map<String, NativeCache> caches, Object bean, Method method) {
        this.caches = caches;
        this.bean = bean;
        this.method = method;
    }

    /**
     * Invokes the cached method with the same parameters again and stores the new return value in the cache again.
     * The cache stays stable during executing of this method.
     */
    public void refreshCaches() {
        if (caches.keySet().isEmpty()) {
            if (logger.isTraceEnabled()) {
                logger.trace("Not refreshing cache of bean method '%s#%s' since it is empty.".formatted(bean.getClass().getSimpleName(), method.toGenericString()));
            }
            return;
        }
        for (Map.Entry<String, NativeCache> cacheEntry : caches.entrySet()) {
            String cacheName = cacheEntry.getKey();
            NativeCache nativeCache = cacheEntry.getValue();
            if (nativeCache == null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Cache '%s' does not exist, skipping refresh.".formatted(cacheName));
                }
                continue;
            }
            refreshCache(nativeCache, cacheName);
        }
    }

    public abstract void refreshCache(NativeCache nativeCache, String cacheName);


}
