package com.qin.demo.proxy.method;

import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

public class MyAfterReturningAdvice implements AfterReturningAdvice {
    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        System.out.println("AfterReturningAdvice ==后置增强");
        System.out.println("AfterReturningAdvice ==方法名：" + method.getName());
        if (null != args && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("AfterReturningAdvice ==第" + (i + 1) + "参数：" + args[i]);
            }
        }
        System.out.println("AfterReturningAdvice ==目标类信息：" + target.toString());
    }
}
