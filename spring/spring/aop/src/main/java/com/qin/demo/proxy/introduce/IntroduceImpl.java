package com.qin.demo.proxy.introduce;

public class IntroduceImpl implements Introduce {
    /**
     * Spring 允许为目标类对象引入新的接口。
     * 使用：
     * 	引入
     * 	1、types-matching：匹配需要引入接口的目标对象的AspectJ语法类型表达式。
     * 	2、implement-interface：定义需要引入的接口。
     * 	3、default-impl和delegate-ref：定义引入接口的默认实现，二者选一，
     * 	  default-impl是接口的默认实现类全限定名，而delegate-ref是默认的实现的委托Bean名。
     * <aop:declare-parents types-matching="com.lyc.cn.v2.day06.Cat"
     * 				 implement-interface="com.lyc.cn.v2.day06.IIntroduce"
     * 				 default-impl="com.lyc.cn.v2.day06.IntroduceImpl"/>
     */
    @Override
    public void sayIntroduce() {
        System.out.println("引入新的接口");
    }
}
