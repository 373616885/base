package com.qin.demo.proxy.method;

import org.springframework.aop.ThrowsAdvice;

import java.lang.reflect.Method;

public class MyThrowsAdvice implements ThrowsAdvice {

    /**
     * 异常增强
     */
    public void afterThrowing(Method method, Object[] args, Object target, Exception ex) {
        System.out.println("MyThrowsAdvice ==异常增强");
        System.out.println("MyThrowsAdvice ==方法名：" + method.getName());
        if (null != args && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("MyThrowsAdvice ==第" + (i + 1) + "参数：" + args[i]);
            }
        }
        System.out.println("MyThrowsAdvice ==目标类信息：" + target.toString());
        System.out.println("MyThrowsAdvice ==异常信息：" + ex.toString());
    }
}
