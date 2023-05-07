### 模块化优势

重构的了jre 将 rt.jar 进行了拆分



更好的访问控制



开发效率的提升



提升了加载速度



### 重构的了jre 将 rt.jar 进行了拆分

将 rt.jar 拆分成了 71个 jmod文件 在 jmods  下



### 更好的访问控制

你有一个jar对外的，你只想对某个package 可以对外 某些package 不想对外就可以使用 module

jar 包可以转成jmod文件放到jmods下

类文件和jar之间的一层

![](img\2022-09-13 101746.png)



### 开发效率的提升

rt.jar 变成71个模块，改动一个模块，不需要测试全部模块



### 提升了加载速度

本身是类加载的时候会去加载classpath下的所有文件，现在可以定向加载 module 引人的，不需要加载全部的classpath





![](img\2022-09-13 102722.png)



### 编译指令

```shell
javac -verbose --module-path target -d target\pojo pojo\src\*.java pojo\src\pojo\*.java

--module-path指定本次编译依赖的外部模块的搜索路径
-d指定编译目标文件存放的目录

-verbose打印详细日志便于我们观察编译的具体过程

```



### 缺点

需要使用者也必须有 module-info.java 这个文件

如何使用者没有 module-info.java 这个文件是可以无视的



### 使用中需要注意 

**maven-compiler-plugin 编译插件必须是新版的旧版的不行**

```xml
   <build>
        <plugins>
            <!-- java编译插件 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.10.1</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>
        </plugins>
    </build>
```



**依赖的 jar 必须都要 requires**

例如依赖了 hutool.all 这个jar module-info中就必须 requires hutool.all

```java
module com.qin.a {
    requires lombok;
    requires hutool.all;
    exports com.qin.user;
    exports com.qin.admin;
}
```







 







