package com.qin.demo.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

// 需对该类进行Bean的实例化
public class LearnListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        //判断spring容器是否加载完成
        if (event.getApplicationContext().getParent() == null) {
            // 打印容器中出事Bean的数量
            System.out.println("监听器获得容器中初始化Bean数量：" + event.getApplicationContext().getBeanDefinitionCount());
        }
    }

}
