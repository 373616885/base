package com.qin.netty.encode;

public record User(int age, String name) {
    public User(String name) {
        this(0, name);
    }
    public User(int age) {
        this(age, null);
    }
    public User() {
        this(0, null);
    }

    public static User of(){
        return new User(20);
    }

}
