package com.qin.demo.bean;

import com.qin.demo.bean.User;

public class StaticClass {

    public static User user = new User("qinjp");

    public static void methed(){
        System.out.println("static 方法");
    }

    static {
        System.out.println("static 代码块");
    }

    StaticClass () {
        System.out.println("构造器");
    }

}
