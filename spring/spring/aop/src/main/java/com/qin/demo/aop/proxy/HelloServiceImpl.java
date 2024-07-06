package com.qin.demo.aop.proxy;

public class HelloServiceImpl {

	public String say() {
		System.out.println("hello everyone");
		return "HelloServiceImpl的say返回值";
	}

}
