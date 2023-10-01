package io.github.philkes.spring.cache.interceptor;

import org.springframework.cache.interceptor.SimpleKey;

/**
 * Extension of {@link SimpleKey} to expose method invocation's parameters
 */
public class ParametersKey extends SimpleKey {

    private final Object[] params;
    public ParametersKey(Object... params) {
        super(params);
        this.params = params;
    }

    public Object[] getParams() {
        return params;
    }
}
