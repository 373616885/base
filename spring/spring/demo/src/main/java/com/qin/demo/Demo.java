package com.qin.demo;

import com.qin.demo.bean.Car;
import com.qin.demo.bean.MyBean;
import com.qin.demo.bean.MyTestBean;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class Demo {

    public static void main(String[] args) {

        Resource resource = new ClassPathResource("beanFactory.xml");
        XmlBeanFactory beanFactory = new XmlBeanFactory(resource);
        //beanFactory.setAllowCircularReferences(false);
        MyTestBean myBean = beanFactory.getBean("myTestBean",MyTestBean.class);
        System.out.println(myBean.getTestStr());
        MyBean bean = beanFactory.getBean("myBean",MyBean.class);
        System.out.println(bean.getTestStr());
        Car car = beanFactory.getBean("car",Car.class);
        System.out.println(car.toString());
//        HelloBeanFactoryAware hello = beanFactory.getBean("helloBeanFactoryAware",HelloBeanFactoryAware.class);
//        hello.testAware();

        ThreadMXBean mxBean =  ManagementFactory.getThreadMXBean();
        long[] ids = mxBean.getAllThreadIds();
        for (long id : ids) {
            ThreadInfo info = mxBean.getThreadInfo(id);
            System.out.println(info.getThreadId() + "   " + info.getThreadName());
        }
    }

}
