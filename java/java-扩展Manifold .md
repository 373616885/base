### java 扩展
https://zhuanlan.zhihu.com/p/607107073
https://github.com/manifold-systems/manifold

### 准备条件
需要安装 Manifold IDEA 的插件



pom 的 maven-compiler-plugin 中加入 annotationProcessorPaths

使用了 Lombok，也需要把 Lombok 也加入 annotationProcessorPaths

```xml
<?xml version="1.0" encoding="UTF-8"?>

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.qin.ext</groupId>
    <artifactId>manifold-ext</artifactId>
    <version>1.0</version>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>

        <!-- https://mvnrepository.com/artifact/org.apache.commons/commons-lang3 -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.12.0</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <!-- https://mvnrepository.com/artifact/systems.manifold/manifold-ext -->
        <dependency>
            <groupId>systems.manifold</groupId>
            <artifactId>manifold-ext</artifactId>
            <version>2023.1.3</version>
<!--            <scope>provided</scope>-->
        </dependency>

    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.10.1</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <encoding>UTF-8</encoding>
                    <compilerArgs>
                        <arg>-Xplugin:Manifold no-bootstrap</arg>
                    </compilerArgs>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>systems.manifold</groupId>
                            <artifactId>manifold-ext</artifactId>
                            <version>2023.1.3</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>1.18.22</version>
                        </path>
                    </annotationProcessorPaths>
                    <!--                    <excludes>-->
                    <!-- 去除指定的包，及其包下的类-->
                    <!--                        <exclude>**/demo/**</exclude>-->
                    <!-- 去除指定的类-->
                    <!--                        <exclude>**/UserControllerTest.java</exclude>-->
                    <!--                        <exclude>**/UserServiceTest.java</exclude>-->
                    <!--                    </excludes>-->
                </configuration>
            </plugin>

        </plugins>
    </build>

</project>

```



### 编写扩展方法

```java
package com.qin.ext.extensions.java.lang.String;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.apache.commons.lang3.StringUtils;

/**
 * String 的扩展方法
 */
@Extension
public final class StringExt {
    public static String[] sp(@This String str, char separator) {
        return StringUtils.split(str, separator);
    }

    // Add print() instance method to String via @This
    public static void print(@This String thiz) {
        System.out.println(thiz);
    }

    // Add lineSeparator() static method to String via @Extension
    @Extension
    public static String nanoTime() {
        return String.valueOf(System.nanoTime());
    }

    @Extension
    public static String currentTimeMillis() {
        return String.valueOf(System.currentTimeMillis());
    }
}

```



可以发现本质上还是工具类的静态方法，但是有一些要求：

1. 工具类需要使用 Manifold 的 `@Extension` 注解
2. 静态方法中，目标类型的参数，需要使用 `@This` 注解
3. 工具类所在的包名，需要以 **extensions.目标类型全限定类名** 结尾



### 使用

```java
package com.qin.ext;

import java.util.List;

/**
 * Hello world!
 */
public class ExtDemo {
    public static void main(String[] args) {
        String s = String.nanoTime();

        System.out.println(s);

        String aa = "abdsdabdadb";
        String[] as = aa.sp('a');
        for (String a : as) {
            System.out.println(a.toString());
        }
        System.out.println(as.toString());
        //SpringApplication.run(DemoApplication.class, args);
        Object o = new Object();
        Object[] objects = o.emptyArray();
        List<Object> objects1 = objects.toList();
    }
}

```



### 扩展静态方法

如果你给 `Object` 添加静态扩展方法，那么意味着你可以在任何地方直接访问到这个静态方法，而不需要 import —— 恭喜你，解锁了 “顶级函数”

```java
package com.qin.ext.java.lang.Object;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import java.util.Optional;

/**
 * Object 的扩展方法
 */
@Extension
public final class ObjectExt {
    public static Optional<@Self Object> asOpt(@This Object obj) {
        return Optional.ofNullable(obj);
    }
}
```



### 缺点

如果jar的编译范围是 provided

那么像数组的toString方法就不能在项目中使用

```xml
 <dependency>
     <groupId>systems.manifold</groupId>
     <artifactId>manifold-ext</artifactId>
     <version>2023.1.3</version>
     <!-- provided -->
     <scope>provided</scope>
</dependency>
```



`ManArrayExt` 的源码，发现 Manifold 专门提供了一个类 `manifold.rt.api.Array`，用来表示数组。

`ManArrayExt` 中为数组提供的扩展 的方法



### 与之相似 

Lombok有一个注解叫@ExtendMethod 也可以实现的