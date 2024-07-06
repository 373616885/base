### lookup-method 使用：

主要解决 **单例** 依赖 **多例** 的问题

问题：

```java
public class Main{

     private Man man;   

     //这里注入一个prototype的Man实例
     public void setMan(Man man) {          this.man= man;      }

     public Man getMan() {          return man;      }

}
```

```xml
 <!-- 这里声明一个多例的bean -->
<bean id="man" class="test.American" scope="prototype"/> 
<!-- 这里声明一个单例的bean -->
<bean id="main" class="test.Main">  
     <!-- 这里依赖一个多例的bean -->   
    <property name="man" ref="man"/>   
</bean>  

如果我为Main创建一个单例bean，那么当我注入Man实例的时候，man属性也会变成单例了，跟我预期的效果不一样

解决方法1：放弃依赖注入，使用传统工厂思想，通过ApplicationContext的getBean方法获取Man的实例。但是缺点比较明显，跟Spring框架耦合了。

解决方法2：如果我仍然想得到依赖注入带来的好处，那么可以使用Spring提供的lookup-method来注入。

```

使用 lookup-method :

```xml
<!-- 这里声明一个多例的bean -->
<bean id="man" class="test.American" scope="prototype"/>  

<!-- 这里声明一个单例的bean -->    
<bean id="main" class="test.Main"> 
    <!-- 这里依赖一个多例的bean --> 
    <lookup-method name="getMan" bean="man"/>   
</bean>  
```



原理：Spring会对bean指定的class做动态代理，代理<lookup-method/>标签中name属性所指定的方法，返回bean属性指定的bean实例对象

```xml
<bean class="beanClass">
    <lookup-method name="method" bean="non-singleton-bean"/>
</bean>
```

method：是beanClass中的一个方法，beanClass和method是不是抽象都无所谓，不会影响CGLIB的动态代理，根据项目实际需求去定义。

non-singleton-bean：指的是lookup-method中bean属性指向的必须是一个非单例模式的bean，当然如果不是也不会报错，只是每次得到的都是相同引用的bean（同一个实例），这样用lookup-method就没有意义了

```properties
另外对于method在代码中的签名有下面的标准：

<public|protected> [abstract] <return-type> theMethodName(no-arguments);

1.public|protected要求方法必须是可以被子类重写和调用的；

2.abstract可选，如果是抽象方法，CGLIB的动态代理类就会实现这个方法，如果不是抽象方法，就会覆盖这个方法，所以没什么影响；

3.return-type就是non-singleton-bean的类型咯，当然可以是它的父类或者接口。

4.no-arguments不允许有参数。
```



### replaced-method  使用：

主要作用就是替换方法体及其返回值，其实现也比较简单

**replace-method注入需实现MethodReplacer接口，并重写reimplement方法**

```java
public class OriginalDog {
    public void sayHello(String name) {
        System.out.println("Hello,I am a black dog, my name is " + name);
    }
}
```

```java
public class ReplaceDog implements MethodReplacer {
    @Override
    public Object reimplement(Object obj, Method method, Object[] args) throws Throwable {
        System.out.println("Hello, I am a white dog...");
        Arrays.stream(args).forEach(str -> System.out.println("参数:" + str));
        return obj;
    }
}
```

```xml
<!-- ====================replace-method属性注入==================== -->
<bean id="dogReplaceMethod" class="com.lyc.cn.v2.day01.method.replaceMethod.ReplaceDog"/>
<bean id="originalDogReplaceMethod" class="com.lyc.cn.v2.day01.method.replaceMethod.OriginalDog">
    <replaced-method name="sayHello" replacer="dogReplaceMethod">
        <arg-type match="java.lang.String"></arg-type>
    </replaced-method>
</bean>
```

