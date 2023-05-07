package com.qin.demo;

import com.qin.demo.bean.HelloApplicationContextAware;
import com.qin.demo.bean.MyBean;
import com.qin.demo.listener.NotifyEvent;
import org.springframework.context.ApplicationContext;

import java.time.format.DateTimeFormatter;

public class Context {
    public static void main(String[] args) {
        NotifyEvent event = new NotifyEvent("null");
        event.setContent("qinjp");
        event.setEmail("373616885@qq.com");

        ApplicationContext bf= new MyClassPathXmlApplicationContext("beanFactoryTest.xml");
        MyBean bean = bf.getBean("myBean",MyBean.class);
        System.out.println(bean.getTestStr());
        System.out.println(bean.getNameStr());
        System.out.println(bean.getLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        bf.publishEvent(event);
        HelloApplicationContextAware applicationContextAware = bf.getBean("helloApplicationContextAware", HelloApplicationContextAware.class);

        applicationContextAware.testAware();

    }
}
