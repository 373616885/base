package com.qin.demo.clazz.loader;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MyClassLoader extends ClassLoader {

	public Class<?> defineMyClass(String name, byte[] b, int off, int len) {
		return super.defineClass(name, b, off, len);
	}
	
	/**
	 * 这里证明字节码可以通过字节码加载成class对象的能力
	 */
	public static void main(String[] args) throws Exception {
		
		Path path = Paths.get(Hello.class.getResource("Hello.class").toURI());
		
		//Path path = Paths.get("Programmer.class");
		System.out.println(path);
		byte[] b = Files.readAllBytes(path);//默认
		MyClassLoader loader = new MyClassLoader();
		Class clazz = loader.defineMyClass(null, b, 0, b.length);
		// 测试加载是否成功，打印class 对象的名称
		System.out.println(clazz.getCanonicalName());
		// 实例化一个Hello对象
		Object o = clazz.newInstance();

		// 调用Hello的say方法
		clazz.getMethod("say", null).invoke(o, null);
	}

}
