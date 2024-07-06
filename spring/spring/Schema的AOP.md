###  Spring中的自动代理方式来实现AOP，基于`Schema`配置文件方式 

##### 目标对象

```java
public interface Animal {
	void sayHello(String name,int age);
	void sayException(String name, int age);
}

public class Cat implements Animal {
	@Override
	public void sayHello(String name, int age) {
		System.out.println("--调用被增强方法");
	}
	@Override
	public void sayException(String name, int age) {
		System.out.println("==抛出异常：" + 1 / 0);
	}
}
```

##### 切面类 ：

需要注意 环绕增强 如果捕获了异常 后置异常增强 就无效了

```java
import org.aspectj.lang.ProceedingJoinPoint;

public class CatAspect {

	/**
	 * 前置增强
	 */
	public void beforeAdvice(String name, int age) {
		System.out.println("==前置增强，name：" + name + "，age：" + age);
	}

	/**
	 * 后置异常增强
	 */
	public void afterExceptionAdvice(String name, int age) {
		System.out.println("==后置异常增强，name：" + name + "，age：" + age);
	}

	/**
	 * 后置返回增强
	 */
	public void afterReturningAdvice(String name, int age) {
		System.out.println("==后置返回增强，name：" + name + "，age：" + age);
	}

	/**
	 * 后置最终增强
	 */
	public void afterAdvice(String name, int age) {
		System.out.println("==后置最终增强，name：" + name + "，age：" + age);
	}

	/**
	 * 环绕增强
	 */
	public Object roundAdvice(ProceedingJoinPoint p, String name, int age) {
		System.out.println("==环绕增强开始，name：" + name + "，age：" + age);
		Object o = null;
		try {
			o = p.proceed();
			Object[] args = p.getArgs();
			if (null != args) {
				for (int i = 0; i < args.length; i++) {
					System.out.println("==环绕增强参数值：" + args[i]);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println("==环绕增强结束，name：" + name + "，age：" + age);
		return o;
	}

}
```

##### xml 配置文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/aop
       http://www.springframework.org/schema/aop/spring-aop.xsd">

    <!-- 目标对象 -->
    <bean id="cat" class="com.qin.demo.proxy.Cat"/>

    <!-- 切面类 -->
    <bean id="catAspect" class="com.qin.demo.proxy.CatAspect"/>

    <aop:config proxy-target-class="true">
        <!-- 切入点-->
        <aop:pointcut id="pointcut" expression="execution(* com.qin.demo.proxy.Cat.*(..)) and args(name,age)"/>
        <!-- 切面 -->
        <aop:aspect ref="catAspect" order="0">
            <!--前置增强，在切入点选择的方法之前执行-->
            <aop:before method="beforeAdvice" pointcut-ref="pointcut" arg-names="name,age"/>
            <!--后置异常增强，在切入点选择的方法抛出异常时执行-->
            <aop:after-throwing method="afterExceptionAdvice" pointcut-ref="pointcut" arg-names="name,age"/>
            <!--后置返回增强，在切入点选择的方法正常返回时执行-->
            <aop:after-returning method="afterReturningAdvice" pointcut-ref="pointcut" arg-names="name,age"/>
            <!--后置最终增强，在切入点选择的方法返回时执行，不管是正常返回还是抛出异常都执行-->
            <aop:after method="afterAdvice" pointcut-ref="pointcut" arg-names="name,age"/>
            <!--
                环绕增强，环绕着在切入点选择的连接点处的方法所执行的通知，可以决定目标方法是否执行，
                什么时候执行，执行时是否需要替换方法参数，执行完毕是否需要替换返回值
              -->
            <aop:around method="roundAdvice" pointcut-ref="pointcut" arg-names="p,name,age"/>

        </aop:aspect>

    </aop:config>

</beans>
```

 ##### 测试类 

```java
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MyTest {

    @Test
    public void test1() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-aop.xml");
        Cat cat = ctx.getBean("cat", Cat.class);
        cat.sayHello("美美", 3);
    }
    @Test
    public void test2() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("spring-aop.xml");
        Cat cat = ctx.getBean("cat", Cat.class);
        cat.sayException("美美", 3);
    }
}
```

### 引介增强

```java
public interface Introduce {
    /**
     * 目标类对象引入新的接口
     */
    void sayIntroduce();
}
public class IntroduceImpl implements Introduce {
    /**
     * Spring 允许为目标类对象引入新的接口。
     * 使用：
     * 	引入
     * 	1、types-matching：匹配需要引入接口的目标对象的AspectJ语法类型表达式。
     * 	2、implement-interface：定义需要引入的接口。
     * 	3、default-impl和delegate-ref：定义引入接口的默认实现，二者选一，
     * 	  default-impl是接口的默认实现类全限定名，而delegate-ref是默认的实现的委托Bean名。
     * 注意 <aop:declare-parents> 放在 <aop:aspect/> 里面
     *     <aop:aspect/> 又在 <aop:config/> 里面
     * <aop:declare-parents types-matching="com.lyc.cn.v2.day06.Cat"
     * 				 implement-interface="com.lyc.cn.v2.day06.IIntroduce"
     * 				 default-impl="com.lyc.cn.v2.day06.IntroduceImpl"/>
     */
    @Override
    public void sayIntroduce() {
        System.out.println("引入新的接口");
    }
}
```

##### xml 配置文件

```xml
<!-- 目标对象 -->
    <bean id="cat" class="com.qin.demo.proxy.Cat"/>

    <!-- 切面类 -->
    <bean id="catAspect" class="com.qin.demo.proxy.CatAspect"/>

    <aop:config proxy-target-class="true">
        <!-- 切入点-->
        <aop:pointcut id="pointcut" expression="execution(* com.qin.demo.proxy.Cat.*(..)) and args(name,age)"/>
        <!-- 切面 -->
        <aop:aspect ref="catAspect" order="0">
            <!--前置增强，在切入点选择的方法之前执行-->
            <aop:before method="beforeAdvice" pointcut-ref="pointcut" arg-names="name,age"/>
            <!--为目标类对象引入新的接口 -->
            <aop:declare-parents types-matching="com.qin.demo.proxy.Cat"
                                 implement-interface="com.qin.demo.proxy.introduce.Introduce"
                                 default-impl="com.qin.demo.proxy.introduce.IntroduceImpl"/>

        </aop:aspect>
    </aop:config>
```

