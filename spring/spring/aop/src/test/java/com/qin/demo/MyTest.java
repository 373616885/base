package com.qin.demo;

import com.qin.demo.proxy.Animal;
import com.qin.demo.proxy.Dog;
import com.qin.demo.proxy.introduce.Introduce;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MyTest {

    @Test
    public void test1() {
        // 基于@AspectJ注解方式
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-aspect.xml");
        Dog dog =(Dog) ctx.getBean("dog");
        dog.sayHello("qinjp",30);
    }


    @Test
    public void test2() {
        // 引入
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-aspect.xml");
        // 注意：getBean获取的是dog
        Introduce introduce = ctx.getBean("dog", Introduce.class);
        introduce.sayIntroduce();
    }
}
