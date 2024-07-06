package com.qin.demo.proxy;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;

public class AopCallbackFilterTest {
    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();
        // 设置目标类
        enhancer.setSuperclass(HelloServiceImpl.class);
        enhancer.setCallbackFilter(new AopCallbackFilter());
        enhancer.setCallbacks(new Callback[]{new AopProxy(), NoOp.INSTANCE});
        HelloServiceImpl dao = (HelloServiceImpl) enhancer.create();
        dao.say();

    }
}
