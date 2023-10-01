package io.github.philkes.spring.cache.interceptor;

import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;

/**
 * Cache-key generator that uses {@link ParametersKey}s
 */
public class ParametersKeyGenerator implements KeyGenerator {
    public static final String PARAMETERS_KEY_GENERATOR_BEAN = "io.github.philkes.spring.cache.interceptor.internalParametersKeyGenerator";

    @Override
    public Object generate(Object target, Method method, Object... params) {
        return generateKey(params);
    }

    /**
     * Generate a key based on the specified parameters.
     */
    public static Object generateKey(Object... params) {
        return new ParametersKey(params);
    }
}
