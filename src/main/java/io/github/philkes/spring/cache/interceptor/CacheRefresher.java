package io.github.philkes.spring.cache.interceptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.TargetSource;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;

public class CacheRefresher {

    public static final String CACHE_REFRESH_METHOD = "refreshCache";

    private final Log logger = LogFactory.getLog(getClass());

    private final CacheManager cacheManager;
    private final Object bean;
    private final Method method;
    private final String[] cacheNames;

    public CacheRefresher(CacheManager cacheManager, Object bean, Method method, String[] cacheNames) {
        this.cacheManager = cacheManager;
        this.bean = bean;
        this.method = method;
        this.cacheNames = cacheNames;
    }


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

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

    public String[] getCacheNames() {
        return cacheNames;
    }

    public Object getTargetObject() {
        String name = bean.getClass().getName();
        if (name.toLowerCase().contains("cglib")) {
            return extractTargetObject(bean);
        }
        return bean;
    }

    private Object extractTargetObject(Object proxied) {
        try {
            return findSpringTargetSource(proxied).getTarget();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private TargetSource findSpringTargetSource(Object proxied) {
        Method[] methods = proxied.getClass().getDeclaredMethods();
        Method targetSourceMethod = findTargetSourceMethod(methods);
        targetSourceMethod.setAccessible(true);
        try {
            return (TargetSource) targetSourceMethod.invoke(proxied);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Method findTargetSourceMethod(Method[] methods) {
        for (Method method : methods) {
            if (method.getName().endsWith("getTargetSource")) {
                return method;
            }
        }
        throw new IllegalStateException(
                "Could not find target source method on proxied object ["
                        + bean.getClass() + "]");
    }
}
