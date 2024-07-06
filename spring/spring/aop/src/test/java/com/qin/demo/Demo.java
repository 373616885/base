package com.qin.demo;


import com.qin.demo.proxy.Animal;
import com.qin.demo.proxy.Cat;
import com.qin.demo.proxy.Dog;
import com.qin.demo.proxy.introduce.Introduce;
import com.qin.demo.proxy.method.*;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class Demo {

    @Test
    public void test1() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-aop.xml");
        Cat cat = ctx.getBean("cat", Cat.class);
        cat.sayHello("美美", 3);
    }


    @Test
    public void test2() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-aop.xml");
        Cat cat = ctx.getBean("cat", Cat.class);
        cat.sayException("美美", 3);
    }

    @Test
    public void test3() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-aop.xml");
        Introduce cat = ctx.getBean("cat", Introduce.class);
        cat.sayIntroduce();
    }

    @Test
    public void test4() {
        // 1、创建目标类、增强、切入点
        Animal animal = new Dog();
        MyMethodBeforeAdvice advice = new MyMethodBeforeAdvice();
        MyStaticMethodMatcherPointcutAdvisor advisor = new MyStaticMethodMatcherPointcutAdvisor();
        // 2、创建ProxyFactory并设置目标类、增强、切面
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(animal);
        // advisor为切面
        // 为切面类提供增强
        advisor.setAdvice(advice);
        // 切入点
        proxyFactory.addAdvisor(advisor);
        // 3、生成代理实例
        Dog proxyDog = (Dog) proxyFactory.getProxy();
        //proxyDog.sayHelloDog();
        System.out.println("\n\n");
        proxyDog.sayHello("qin",30);
    }

    @Test
    public void test5() {
        // 前置增强
        // 1、实例化bean和增强
        Animal dog = new Dog();
        MyMethodBeforeAdvice advice = new MyMethodBeforeAdvice();

        // 2、创建ProxyFactory并设置代理目标和增强
        ProxyFactory proxyFactory = new ProxyFactory();
        // 需要被织入横切关注点的对象
        proxyFactory.setTarget(dog);
        // advice 增强的类（前置增强 后置增强 环绕增强 的类）
        proxyFactory.addAdvice(advice);

        // 3、生成代理实例
        Animal proxyDog = (Animal) proxyFactory.getProxy();
        proxyDog.sayException("二哈", 3);
    }


    @Test
    public void test6() {
        // 后置增强
        // 1、实例化bean和增强
        Animal dog = new Dog();
        MyAfterReturningAdvice advice = new MyAfterReturningAdvice();

        // 2、创建ProxyFactory并设置代理目标和增强
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(dog);
        proxyFactory.addAdvice(advice);

        // 3、生成代理实例
        Animal proxyDog = (Animal) proxyFactory.getProxy();
        proxyDog.sayHello("二哈", 3);

    }

    @Test
    public void test7() {
        // 异常增强
        // 1、实例化bean和增强
        Animal dog = new Dog();
        MyThrowsAdvice advice = new MyThrowsAdvice();

        // 2、创建ProxyFactory并设置代理目标和增强
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(dog);
        proxyFactory.addAdvice(advice);

        // 3、生成代理实例
        Animal proxyDog = (Animal) proxyFactory.getProxy();
        proxyDog.sayException("二哈", 3);

    }

    @Test
    public void test8() {
        // 环绕增强
        // 1、实例化bean和增强
        Animal dog = new Dog();
        MyMethodInterceptor advice = new MyMethodInterceptor();

        // 2、创建ProxyFactory并设置代理目标和增强
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(dog);
        proxyFactory.addAdvice(advice);

        // 3、生成代理实例
        Animal proxyDog = (Animal) proxyFactory.getProxy();
        proxyDog.sayHello("二哈", 3);

    }


}
