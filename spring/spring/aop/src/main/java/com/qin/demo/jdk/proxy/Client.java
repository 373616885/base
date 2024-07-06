package com.qin.demo.jdk.proxy;

import java.io.FileOutputStream;
import java.lang.reflect.Proxy;

public class Client {
	public static void main(String[] args) {
		Subject realSubject = new RealSubject();

		// 我们要代理哪个真实对象，就将该对象传进去，最后是通过该真实对象来调用其方法的
		ProxyHandler handler = new ProxyHandler(realSubject);
		// Subject proxySubject = (Subject) handler.bind(realSubject);
		// System.out.println(proxySubject.getClass().getName());
		// proxySubject.say();

		/*
		 * 通过Proxy的newProxyInstance方法来创建我们的代理对象，我们来看看其三个参数 第一个参数
		 * handler.getClass().getClassLoader() ，我们这里使用handler这个类的ClassLoader对象来加载我们的代理对象
		 * 第二个参数realSubject.getClass().getInterfaces()，我们这里为代理对象提供的接口是真实对象所实行的接口，
		 * 表示我要代理的是该真实对象，这样我就能调用这组接口中的方法了 第三个参数handler， 我们这里将这个代理对象关联到了上方的
		 * InvocationHandler 这个对象上
		 */
		Subject subject = (Subject) Proxy.newProxyInstance(handler.getClass().getClassLoader(),
				realSubject.getClass().getInterfaces(), handler);
		System.out.println(subject.getClass().getName());
		subject.sayHi();
		subject.sayHello();
		// write proxySubject class binary data to file
		createProxyClassFile();

	}

	private static void createProxyClassFile() {
		String name = "ProxySubject";
		byte[] data = sun.misc.ProxyGenerator.generateProxyClass(name, new Class[] { Subject.class }, 1);
		try {
			FileOutputStream out = new FileOutputStream(name + ".class");
			out.write(data);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
