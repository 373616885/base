### 命令

```
mvn dependency:analyze
```

仔细查看控制台输出的日志信息，特别关注以下几个部分：

```
[INFO] --- maven-dependency-plugin:2.8:analyze (default-cli) @ xxxproject ---
[WARNING] Used undeclared dependencies found:
[WARNING]    org.springframework:spring-beans:jar:4.0.0.RELEASE:compile
[WARNING]    org.springframework:spring-context:jar:4.0.0.RELEASE:compile
[WARNING] Unused declared dependencies found:
[WARNING]    com.alibaba:dubbo:jar:2.5.3:compile
[WARNING]    com.baidu.disconf:disconf-client:jar:2.6.32:compile
[WARNING]    org.mybatis:mybatis:jar:3.2.7:compile
[WARNING]    org.mybatis:mybatis-spring:jar:1.2.2:compile
[WARNING]    mysql:mysql-connector-java:jar:5.1.41:compile
[WARNING]    com.alibaba:druid:jar:1.0.9:compile
[WARNING]    com.github.sgroschupf:zkclient:jar:0.1:compile
[WARNING]    org.apache.zookeeper:zookeeper:jar:3.4.6:compile
[WARNING]    org.springframework:spring-jdbc:jar:4.0.0.RELEASE:compile
[WARNING]    org.slf4j:log4j-over-slf4j:jar:1.7.5:compile
[WARNING]    org.slf4j:jcl-over-slf4j:jar:1.7.5:runtime
[WARNING]    ch.qos.logback:logback-classic:jar:1.0.13:compile   
```



### Used undeclared dependencies found

实际使用了某个依赖包，但并未在pom.xml文件中显式声明

这些依赖包可能是通过其他依赖间接引入的

例如，假设你的项目在`pom.xml`中声明了对`A.jar`的依赖，但未声明对`B.jar`的依赖。而`A.jar`的依赖树中又包含了对B.jar的依赖。通过运行`mvn dependency:analyze`命令，如果输出如下警告：

```
mvn dependency:analyze
```

出现

```
[WARNING] Used undeclared dependencies found: B.jar
```

这意味着你的项目代码中实际上使用了`B.jar`中的类或接口。在这种情况下，你应该将`B.jar`添加到项目的`pom.xml`文件中，以确保依赖关系的正确声明



### **Unused declared dependencies found**

这一部分则是指你在项目的`pom.xml`文件中声明了某个依赖包，但在实际的项目代码中并未使用到它。这些依赖包可能是不必要的，可以考虑从`pom.xml`中移除。

然而，在删除这些依赖之前，请务必注意以下几点：

- “未使用”的定义仅限于`main/java`和test源代码目录，不包括配置文件或其他可能的扩展点。
- 在删除依赖之前，务必备份`pom.xml`文件，以防万一误删重要依赖导致的问题。
- Maven的依赖分析工具并非万能，有时可能会产生误报。因此，在删除任何依赖后，都应进行充分的测试以验证项目的稳定性





### 风险要注意

依赖分析工具的结果并非绝对准确，可能存在误判的情况

例如：某些工具可能无法识别某些特殊的使用场景（如注解处理器等）