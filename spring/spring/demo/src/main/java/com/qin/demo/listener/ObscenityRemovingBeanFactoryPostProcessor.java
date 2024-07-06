package com.qin.demo.listener;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.util.StringValueResolver;

import java.util.HashSet;
import java.util.Set;

public class ObscenityRemovingBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

    private Set<String> obscentities = new HashSet<>();

    public void setObscentities(Set<String> obscentities) {
        this.obscentities = obscentities;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] names = beanFactory.getBeanDefinitionNames();
        for (String name : names) {
            BeanDefinition definition = beanFactory.getBeanDefinition(name);
            StringValueResolver resolver = strVal -> {
                System.out.println("***** " + strVal + " *****");
                if (this.obscentities.contains(strVal)) {
                    return "***** " + strVal + " *****";
                }
                return strVal;
            };
            BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(resolver);
            visitor.visitBeanDefinition(definition);
        }
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
