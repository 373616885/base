package com.qin.demo.proxy.method;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class MyMethodInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("MyMethodInterceptor ==环绕增强开始");
        System.out.println("MyMethodInterceptor ==方法名：" + invocation.getMethod().getName());

        System.out.println("MyMethodInterceptor == 执行原始方法");
        Object proceed = invocation.proceed();

        System.out.println("MyMethodInterceptor ==环绕增强结束");
        return proceed;
    }
}
