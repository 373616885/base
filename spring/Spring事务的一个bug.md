### Spring事务的一个bug



最新版 Spring boot 2.7.3 还有这个bug



Spring  6.0.0.M3 才修复这个bug



@Transactional 注解里面的 rollbackFor 是 AgeException.class

如果抛异常AgeExceptionOver18 也会造成回滚



原因：异常匹配使用了contains 去判断

```java
private int getDepth(Class<?> exceptionType, int depth) {
   //
   if (exceptionType.getName().contains(this.exceptionPattern)) {
      // Found it!
      return depth;
   }
   // If we've gone as far as we can go and haven't found it...
   if (exceptionType == Throwable.class) {
      return -1;
   }
   return getDepth(exceptionType.getSuperclass(), depth + 1);
}
```

为什么要用`contains()` 方法呢？这其实是历史考量

在 XML 配置文件中，用户通常指定自定义异常类型的简单名称，而不是全路径类名

xml 配置示例：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <tx:advice id="tx" transaction-manager="txManager">
        <tx:attributes><!--注意：下面的name支持通配符的写法！并且只需给出方法名，而无需给类名-->
            <tx:method name="transfer" isolation="DEFAULT"
                       propagation="REQUIRED" timeout="-1" read-only="false" rollback-for="AgeException"/>
        </tx:attributes>
    </tx:advice>
</beans>
```

在 xml 配置中，关于 rollback-for 属性 , 通常定义 simple name 就是 AgeException

而 fully qualified class name 就是 com.qin.exception.AgeException



异常内部类也会有这种情况

```java
static class AgeException extends Exception{
    static class AgeExceptionOver18 extends Exception {

    }
}
```

内部类的时候抛出异常是这样的

throw new AgeException.AgeOver18Exception();





同时主要使用 rollbackForClassName 的时候会有这中情况



### 总结

- 不同包中的相同命名的异常类，会被意外匹配上。比如，example.client.WebException 和 example.server.WebException 都会与 “WebException” 模式匹配。
- 在同一个包中有类似命名的异常，这里说的相似是指当一个给定的异常名称是以另一个异常的名称开头时。例如：example.BusinessException 和 example.BusinessExceptionWithDetails 都与 “example.BusinessException”模式匹配。
- 嵌套异常，也就是当一个异常类被声明在另一个异常类里面的时候。例如：example.BusinessException 和 example.BusinessException$NestedException 都会与 “example.BusinessException” 匹配上。



### 解决

阿里巴巴 Java 开发手册在命名风格里面也特意提到了这一点，且是强制要求

对于异常类，要求必须以 Exception 结尾

7. 【强制】抽象类命名使用 Abstract 或 Base 开头；异常类命名使用 Exception 结尾；测试类

命名以它要测试的类的名称开始，以 Test 结尾。







