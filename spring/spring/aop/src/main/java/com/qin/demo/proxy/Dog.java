package com.qin.demo.proxy;

public class  Dog implements Animal {

    private Dog dog;

    public Dog getDog() {
        return dog;
    }

    public void setDog(Dog dog) {
        this.dog = dog;
    }

    @Override
    public void sayHello(String name, int age) {
        System.out.println("==名字：" + name + " 年龄：" + age);
    }

    @Override
    public void sayException(String name, int age) {
        System.out.println("==名字：" + name + " 年龄：" + age);
        System.out.println("==抛出异常：" + 1 / 0);
    }

    public void sayHelloDog() {
        System.out.println("我是一只狗。。。");
    }
}
