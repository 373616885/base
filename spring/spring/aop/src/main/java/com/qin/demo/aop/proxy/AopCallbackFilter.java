package com.qin.demo.aop.proxy;

import net.sf.cglib.proxy.CallbackFilter;

import java.lang.reflect.Method;

public class AopCallbackFilter implements CallbackFilter {
    @Override
    public int accept(Method method) {
        if ("say".equals(method.getName())) {
            return 0;
        }
        return 1;
    }
}
