package com.qin.demo.jdk.proxy;

public class RealSubject implements Subject {

	@Override
	public void sayHello() {
		System.out.println("RealSubject: say hello");
	}

	@Override
	public void sayHi() {
		System.out.println("RealSubject: say hi");
	}

}
