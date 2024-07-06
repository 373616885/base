package com.qin.demo.bean;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;

public class HelloApplicationContextAware implements ApplicationContextAware {

//    private EnvironmentAware environmentAware;
//
//    public void setEnvironmentAware(EnvironmentAware environmentAware) {
//        this.environmentAware = environmentAware;
//    }

    private MyBean myBean;

    public void setMyBean(MyBean myBean) {
        this.myBean = myBean;
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    public void testAware() {
        // 通过 hello 这个 bean id 从 beanFactory 获取实例
        Hello hello = applicationContext.getBean("hello", Hello.class);
        hello.say();
        System.out.println(myBean.getDateValue());
        //System.out.println(environmentAware.toString());
    }
}
