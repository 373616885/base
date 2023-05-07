package com.qin.demo.listener;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;

public class UpdateBeanFactoryPostProcessor  implements BeanFactoryPostProcessor, Ordered {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        BeanDefinition bd = beanFactory.getBeanDefinition("myBean");
        MutablePropertyValues pv =  bd.getPropertyValues();
        if(pv.contains("testStr")){
            pv.addPropertyValue("testStr", "373616885");
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
