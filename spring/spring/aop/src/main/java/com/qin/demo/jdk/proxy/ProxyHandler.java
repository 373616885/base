package com.qin.demo.jdk.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyHandler implements InvocationHandler {

	private Object obj;

	public ProxyHandler(Object obj) {
		this.obj = obj;
	}

	public ProxyHandler() {
	}

	// 返回代理对象简化客户端调用
	public Object bind(Object obj) {
		this.obj = obj;
		return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("之前处理");
		// method 通过接口反射得到得方法
		Object result = method.invoke(obj, args);
		System.out.println("之后处理");
		return result;
	}

}
