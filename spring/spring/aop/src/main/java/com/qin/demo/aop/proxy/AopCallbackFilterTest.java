package com.qin.demo.aop.proxy;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

public class AopCallbackFilterTest {
    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();
        // 设置目标类
        enhancer.setSuperclass(HelloServiceImpl.class);
        enhancer.setCallbackFilter(new AopCallbackFilter());
        // 根据 AopCallbackFilter accept 返回的 int 值执行者里调用顺序
        enhancer.setCallbacks(new Callback[]{new AopProxy(), NoOp.INSTANCE});
        HelloServiceImpl dao = (HelloServiceImpl) enhancer.create();
        dao.say();

    }
}
