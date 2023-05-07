package com.qin.demo.aop.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class AopProxy implements MethodInterceptor {

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		System.out.println("Before Method Invoke");
		Object result = proxy.invokeSuper(obj, args);
		System.out.println("result: "+ result);
		System.out.println("After Method Invoke");
		return result;
	}

}
