### SpringDevTools bug 或者注意事项



### 知识点

JVM 中表示两个class对象是否是同一个的必要条件

1. 类的完整名称一致，包括包名
2. 加载类的类加载器classLoader必须相同



### 背景

如果jar的静态变量，被程序同名覆盖的变量会导致，在jar包里获取不到的情况



jar包

```java
package com.qin;

public class MyTools1 {

    static {
        System.out.println(MyTools1.class.getClassLoader());
    }

    public static String value;


}



```



```java
package com.qin;

public class MyTools2 {

    static {
        System.out.println(MyTools2.class.getClassLoader());
    }
    public static void setValue(String value){
        MyTools1.value = value;
    }

    public static String getValue(){
        return  MyTools1.value;
    }
}
```



项目

```xml
导入上面的jar
<dependency>
    <groupId>com.qin</groupId>
    <artifactId>classloader</artifactId>
    <version>1.0</version>
</dependency>
```



```java
package com.qin;

public class MyTools1 {

    static {
        System.out.println(MyTools1.class.getClassLoader());
    }

    public static String value;

    // 这里去扩展MyTools1。。。。
}
```

```java
public static void main(String[] args) throws InterruptedException {
  	// 这里设置值
    MyTools1.value = "aaaa";
    //这里获取MyTools1.value的值获取不到
    System.out.println(MyTools2.getValue());
    System.out.println(Application.class.getClassLoader());
}
```



原因

JVM 中表示两个class对象是否是同一个的必要条件

1. 类的完整名称一致，包括包名
2. 加载类的类加载器classLoader必须相同



使用 spring-boot-devtools

应用使用的类加载器是 RestartClassLoader

而jar的类加载器是 AppClassLoader



两个类加载器不一致，所以使用的时候 在应用程序同名覆盖的变量去扩展的方式，会导致加载同名的类是不同的类对象

因为他们的类加载器不一样



















