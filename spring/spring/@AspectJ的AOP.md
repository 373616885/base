### 基于@AspectJ的AOP

 **目标对象** 

```java
// 目标对象
public interface Animal {
	void sayHello();
}

public class Dog implements Animal {
	public void sayHello() {
		System.out.println("--被增强的方法");
	}
}
```

**引介**

```java
// 引介
public interface Introduce {
	void sayIntroduce();
}

public class IntroduceImpl implements Introduce {
	@Override
	public void sayIntroduce() {
		System.out.println("--引入新的接口");
	}
}
```

**切面** 

```java

import com.qin.demo.proxy.introduce.Introduce;
import com.qin.demo.proxy.introduce.IntroduceImpl;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

/**
 * 切面类
 */
@Aspect
public class DogAspect {

    /**
     * 例如：execution (* com.sample.service.impl..*.*(..)
     * 1、execution(): 表达式主体。
     * 2、第一个*号：表示返回类型，*号表示所有的类型。
     * 3、包名：表示需要拦截的包名，后面的两个点表示当前包和当前包的所有子包，
     * 即com.sample.service.impl包、子孙包下所有类的方法。
     * 4、第二个*号：表示类名，*号表示所有的类。
     * 5、*(..):最后这个星号表示方法名，*号表示所有的方法，后面括弧里面表示方法的参数，两个点表示任何参数。
     **/
    @Pointcut("execution(* com.qin.demo.proxy.Dog.*(..))")
    public void test() {
    }

    /**
     * test() 是切入点
     */
    @Before("test()")
    public void beforeTest() {
        System.out.println("==前置增强");
    }

    @After("test()")
    public void afterTest() {
        System.out.println("==后置最终增强");
    }

    @AfterThrowing("test()")
    public void afterThrowingTest() {
        System.out.println("==后置异常增强");
    }

    @AfterReturning("test()")
    public void afterReturningTest() {
        System.out.println("==后置返回增强");
    }

    @Around("test()")
    public Object aroundTest(ProceedingJoinPoint p) {
        System.out.println("==环绕增强开始");
        Object o = null;
        try {
            o = p.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("==环绕增强结束");
        return o;
    }

    @DeclareParents(value = "com.qin.demo.proxy.Dog", defaultImpl = IntroduceImpl.class)
    private Introduce introduce;
}
```

**XML 配置文件**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/aop
       http://www.springframework.org/schema/aop/spring-aop.xsd">
	
    <!--
        1、proxy-target-class
            如果被代理的目标对象至少实现了一个接口，则会使用JDK动态代理，所有实现该目标类实现的接口都将被代理
            如果该目标对象没有实现任何接口，则创建CGLIB动态代理。
            但是可以通过proxy-target-class属性强制指定使用CGLIB代理，
        2、expose-proxy
            解决目标对象内部的自我调用无法实施切面增强的问题
		3、这两个属性 可以单独 在 <aop:config /> 里面设置 
			<aop:config proxy-target-class="true"/>
			
    -->
    <aop:aspectj-autoproxy proxy-target-class="true">
        <!-- 指定@Aspect类，支持正则表达式，符合该表达式的切面类才会被应用-->
        <aop:include name="dogAspect"/>
    </aop:aspectj-autoproxy>

    <!-- 目标对象 -->
    <bean id="dog" class="com.qin.demo.proxy.Dog"/>
    <!--切面-->
    <bean id="dogAspect" class="com.qin.demo.proxy.DogAspect"/>
</beans>
```

```java
package com.qin.doc.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class AspectConfig {

//   @Pointcut("execution(public * com.qin.doc.web..*.*(..))")
//    public void pointcut(){}

    // 定义切点Pointcut  自行写入对应的controller包路径
//    @Pointcut("execution(* com.qin.doc.web..*.*(..))")
//    public void pointcut3() {
//    }

    // @annotation 在方法上
//    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.PostMapping)")
//    public void pointcut1() {
//    }
	 // @within 在类上
//    @Pointcut("@within(org.springframework.stereotype.Controller) || @within(org.springframework.web.bind.annotation.RestController)")
//    public void pointcut2() {
//    }

    @Pointcut("pointcut3() || pointcut1() || pointcut2() ")
    public void pointcut() {
    }


    @Around("pointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        HttpServletRequest request = sra.getRequest();

        String url = request.getRequestURL().toString();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        //这里可以获取到get请求的参数和其他信息
        log.info("请求开始, 各个参数, url: {}, method: {}, uri: {}, params: {}", url, method, uri, queryString);
        //重点 这里就是获取@RequestBody参数的关键  调试的情况下 可以看到o变量已经获取到了请求的参数
        Object[] o = pjp.getArgs();

        // result的值就是被拦截方法的返回值
        Object result = pjp.proceed();
        System.out.println(o);
        return result;
    }

}

```

