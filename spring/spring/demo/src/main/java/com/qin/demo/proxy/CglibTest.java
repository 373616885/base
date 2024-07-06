package com.qin.demo.proxy;

import java.io.IOException;

import org.objectweb.asm.ClassWriter;

import net.sf.cglib.core.DebuggingClassWriter;
import net.sf.cglib.core.DefaultGeneratorStrategy;
import net.sf.cglib.proxy.Enhancer;


public class CglibTest {
	public static void main(String[] args) throws Exception {
		AopProxy daoProxy = new AopProxy();
		// cglib 中加强器 用来创建动态代理
		Enhancer enhancer = new Enhancer();
		// 设置目标类
		enhancer.setSuperclass(HelloServiceImpl.class);
		// 设置拦截对象
		// 这里相当于是对于代理类上所有方法的调用，都会调用CallBack，而Callback则需要实行intercept()方法进行拦截
		enhancer.setCallback(daoProxy);
		// 生成代理类并返回一个实例
		HelloServiceImpl dao = (HelloServiceImpl) enhancer.create();
		System.out.println(dao.getClass().getName());
		System.out.println(dao.getClass().getSuperclass().getName());
		String result = dao.say();
		System.out.println(result);
		
		System.in.read();
		
	}

}
