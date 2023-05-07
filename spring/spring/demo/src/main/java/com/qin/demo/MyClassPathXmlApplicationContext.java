package com.qin.demo;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MyClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {

    public MyClassPathXmlApplicationContext(String configLocation) throws BeansException {
        super(configLocation);
    }

    @Override
    protected void initPropertySources() {
        // 默认会添加系统的环境变量和系统环境
        // 添加必要元素
        getEnvironment().setRequiredProperties("JAVA_HOME");
    }

//    @Override
//    protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
//        super.setAllowBeanDefinitionOverriding(false);
//        super.setAllowCircularReferences(true);
//        super.customizeBeanFactory(beanFactory);
//    }
}
