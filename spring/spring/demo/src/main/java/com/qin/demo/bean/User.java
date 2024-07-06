package com.qin.demo.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Date;

public class User {
    private String name;
    private Integer age;
    private Boolean old;

    private User(Integer age) {
        this.age = age;
    }

    public User(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    User(String name) {
        System.out.println("user:" + name);
        this.name = name;
    }

    protected User(Boolean old) {
        System.out.println("user:" + old);
        this.old = old;
    }

    public static void main(String[] args) {
        Constructor[] c = User.class.getConstructors();
        for (Constructor constructor : c) {
            Parameter[]  p = constructor.getParameters();
            for (Parameter parameter : p) {
                System.out.println(constructor + " "+ parameter );
            }
        }
    }
}
