### AOP中 术语

-  Jointpoint  连接点 ： 需要在程序中插入横切关注点的扩展点，可能是类初始化、方法执行、方法调用、字段调用或处理异常 等等 。 Spring只支持方法执行连接点，在AOP中表示为“在哪里干” 

-  Pointcut   切入点  ： 连接点的集合 ，  Spring支持perl5正则表达式和AspectJ切入点模式，Spring默认使用AspectJ 语法，在AOP中表示为“在哪里干的集合” 

-  Advice  增强  ： 包括前置增强（before advice）、后置增强(after advice)、环绕增强（around advice）、异常增强 (throws Advice )

-  Aspect  切面  :   是增强、引入和切入点的组合  Spring中可以使用Schema和@AspectJ方式进行组织实现；在AOP中表示为“在哪干和干什么集合”； 

- inter-type declaration 引介增强 ：引介增强是一个比较特殊的增强，它不是在目标方法周围织入增强，而是为目标类创建新的方法或属性，所以引介增强的连接点是类级别的，而非方法级别的，Spring允许引入新的接口（必须对应一个实现）到所有被代理对象（目标对象）, 在AOP中表示为“干什么（引入什么）”；

- Target Object 目标对象：需要被织入横切关注点的对象，即该对象是切入点选择的对象，需要被增强的对象，从而也可称为“被增强对象”；由于Spring AOP 通过代理模式实现，从而这个对象永远是被代理对象，在AOP中表示为“对谁干”；

- AOP Proxy AOP代理 ：AOP框架使用代理模式创建的对象，从而实现在连接点处插入增强（即应用切面），就是通过代理来对目标对象应用切面。在Spring中，AOP代理可以用JDK动态代理或CGLIB代理实现，而通过拦截器模型应用切面。

- Weaving  织入 ：织入是一个过程，是将切面应用到目标对象从而创建出AOP代理对象的过程，织入可以在编译期、类装载期、运行期进行。

  



### 增强方式

 Spring aop，我们不会一开始就讲解基于`@AspectJ`或者基于`Schema 配置文件`的方式

 基于Advice接口 :

**MethodBeforeAdvice前置增强**

```java
/**
 * 
 */
import org.springframework.aop.MethodBeforeAdvice;
import java.lang.reflect.Method;

public class MyMethodBeforeAdvice implements MethodBeforeAdvice {

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("MyMethodBeforeAdvice ==前置增强");
        System.out.println("MyMethodBeforeAdvice ==方法名：" + method.getName());
        if (null != args && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("MyMethodBeforeAdvice ==第" + (i + 1) + "参数：" + args[i]);
            }
        }
        System.out.println("MyMethodBeforeAdvice ==目标类信息：" + target.toString());
    }

}

@Test
public void test5() {
    // 前置增强
    // 1、实例化bean和增强
    Animal dog = new Dog();
    MyMethodBeforeAdvice advice = new MyMethodBeforeAdvice();

    // 2、创建ProxyFactory并设置代理目标和增强
    ProxyFactory proxyFactory = new ProxyFactory();
    // 需要被织入横切关注点的对象
    proxyFactory.setTarget(dog);
    // advice 增强的类（前置增强 后置增强 环绕增强 的类）
    proxyFactory.addAdvice(advice);

    // 3、生成代理实例
    Animal proxyDog = (Animal) proxyFactory.getProxy();
    proxyDog.sayException("二哈", 3);
}

```

##### AfterReturningAdvice后置增强

```java
/**
 * 后置增强
 */
public class MyAfterReturningAdvice implements AfterReturningAdvice {
	
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
		System.out.println("==后置增强");
		System.out.println("==方法名：" + method.getName());
		if (null != args && args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				System.out.println("==第" + (i + 1) + "参数：" + args[i]);
			}
		}
		System.out.println("==目标类信息：" + target.toString());
	}
}

@Test
public void test6() {
    // 后置增强
    // 1、实例化bean和增强
    Animal dog = new Dog();
    MyAfterReturningAdvice advice = new MyAfterReturningAdvice();

    // 2、创建ProxyFactory并设置代理目标和增强
    ProxyFactory proxyFactory = new ProxyFactory();
    proxyFactory.setTarget(dog);
    proxyFactory.addAdvice(advice);

    // 3、生成代理实例
    Animal proxyDog = (Animal) proxyFactory.getProxy();
    proxyDog.sayHello("二哈", 3);

}
```

##### ThrowsAdvice异常增强

```java
import org.springframework.aop.ThrowsAdvice;

import java.lang.reflect.Method;

public class MyThrowsAdvice implements ThrowsAdvice {

    /**
     * 异常增强
     */
    public void afterThrowing(Method method, Object[] args, 
                              Object target, Exception ex) {
        System.out.println("MyThrowsAdvice ==异常增强");
        System.out.println("MyThrowsAdvice ==方法名：" + method.getName());
        if (null != args && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("MyThrowsAdvice ==第" + (i + 1) + "参数：" + args[i]);
            }
        }
        System.out.println("MyThrowsAdvice ==目标类信息：" + target.toString());
        System.out.println("MyThrowsAdvice ==异常信息：" + ex.toString());
    }
}

@Test
public void test7() {
    // 异常增强
    // 1、实例化bean和增强
    Animal dog = new Dog();
    MyThrowsAdvice advice = new MyThrowsAdvice();

    // 2、创建ProxyFactory并设置代理目标和增强
    ProxyFactory proxyFactory = new ProxyFactory();
    proxyFactory.setTarget(dog);
    proxyFactory.addAdvice(advice);

    // 3、生成代理实例
    Animal proxyDog = (Animal) proxyFactory.getProxy();
    proxyDog.sayException("二哈", 3);

}
```

##### MethodInterceptor环绕增强

```java
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class MyMethodInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("MyMethodInterceptor ==环绕增强开始");
        System.out.println("MyMethodInterceptor ==方法名：" + invocation.getMethod().getName());

        System.out.println("MyMethodInterceptor == 执行原始方法");
        Object proceed = invocation.proceed();

        System.out.println("MyMethodInterceptor ==环绕增强结束");
        return proceed;
    }
}

@Test
public void test8() {
    // 环绕增强
    // 1、实例化bean和增强
    Animal dog = new Dog();
    MyMethodInterceptor advice = new MyMethodInterceptor();

    // 2、创建ProxyFactory并设置代理目标和增强
    ProxyFactory proxyFactory = new ProxyFactory();
    proxyFactory.setTarget(dog);
    proxyFactory.addAdvice(advice);

    // 3、生成代理实例
    Animal proxyDog = (Animal) proxyFactory.getProxy();
    proxyDog.sayHello("二哈", 3);

}
```



**到这里增强的只是对应的类，没有指定方法，以上是对 目标对象 所有的方法都进行了增强**

###  切入点 @Pointcut 

通过切入点就可以有选择的将增强应用到目标类的方法上

```java

public interface Pointcut {

	/**
	 * 返回当前切点匹配的类
	 */
    // ClassFilter可以定位到具体的类上
	ClassFilter getClassFilter();

	/**
	 * 返回当前切点匹配的方法
	 */
    // MethodMatcher可以定位到具体的方法上
	MethodMatcher getMethodMatcher();

	// 总是匹配的实例
	Pointcut TRUE = TruePointcut.INSTANCE;

}


@FunctionalInterface
public interface ClassFilter {
	// 切入点应该应用于给定的候选目标类
	boolean matches(Class<?> clazz);
	// 总是匹配
	ClassFilter TRUE = TrueClassFilter.INSTANCE;

}

public interface MethodMatcher {
	/**
	 * 静态方法匹配判断
	 */
    boolean matches(Method method, @Nullable Class<?> targetClass);
	/**
	 * 判断静态方法匹配或动态方法匹配
	 * true：动态方法匹配
	 * false：静态方法匹配
	 */
	boolean isRuntime();
	/**
	 * 动态方法匹配判断
	 */
	boolean matches(Method method, @Nullable Class<?> targetClass, Object... args);
	// 总是匹配
	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;
}


```

Spring支持两种方法匹配器： 

- 静态方法匹模式：所谓静态方法匹配器，仅对方法名签名（包括方法名和入参类型及顺序）进行匹配。
- 动态方法匹配器：动态方法匹配器会在运行期方法检查入参的值。 静态匹配仅会判断一次，而动态匹配因为每次调用方法的入参可能不一样，所以每次调用方法都必须判断。

 Spring提供的切点类型 :

静态方法切点     org.springframework.aop.support.StaticMethodMatcherPointcut
静态方法切点的抽象基类，默认情况下匹配所有的类。最常用的两个子类NameMatchMethodPointcut和 AbstractRegexpMethodPointcut ， 前者提供简单字符串匹配方法签名，后者使用正则表达式匹配方法签名。

 动态方法切点     org.springframework.aop.support.DynamicMethodMatcherPointcut
动态方法切点的抽象基类，默认情况下匹配所有的类 

注解切点     org.springframework.aop.support.annotation.AnnotationMatchingPointcut

表达式切点     org.springframework.aop.support.ExpressionPointcut
提供了对AspectJ切点表达式语法的支持

流程切点     org.springframework.aop.support.ControlFlowPointcut
该切点是一个比较特殊的节点，它根据程序执行的堆栈信息查看目标方法是否由某一个方法直接或间接发起调用，一次来判断是否为匹配的链接点

复合切点    org.springframework.aop.support.ComposablePointcut
该类是为实现创建多个切点而提供的操作类



### 切面 Advisor ( @Aspect )

横切代码，又包含部分连接点信息（方法前、方法后主方位信息） -- @Pointcut 和 增强的 @Before 等

切面可以分为3类：一般切面、切点切面、引介切面 

一般切面Advisor ：

- org.springframework.aop.Advisor 代表一般切面 
- 仅包含一个Advice（增强） ,因为Advice包含了横切代码和连接点信息，所以Advice本身一个简单的切面
- 只不过它代表的横切的连接点是所有目标类的所有方法，因为这个横切面太宽泛，所以一般不会直接使用

切点切面PointcutAdvisor ：

-  org.springframework.aop.PointcutAdvisor ,代表具有切点的切面，

- 包括Advice和Pointcut两个类，这样就可以通过类、方法名以及方位等信息灵活的定义切面的连接点，

- 提供更具实用性的切面。PointcutAdvisor主要有6个具体的实现类 

  ```java
  1.DefaultPointcutAdvisor：最常用的切面类型，它可以通过任意Pointcut和Advice定义一个切面，唯一不支持的就是引介的切面类型，一般可以通过扩展该类实现自定义的切面
  2.NameMatchMethodPointcutAdvisor：通过该类可以定义按方法名定义切点的切面
  3.AspectJExpressionPointcutAdvisor：用于AspectJ切点表达式定义切点的切面
  4.StaticMethodMatcherPointcutAdvisor：静态方法匹配器切点定义的切面，默认情况下匹配所有的的目标类
  5.AspectJPointcutAdvisor：用于AspectJ语法定义切点的切面
  ```

 引介切面IntroductionAdvisor :

-  org.springframework.aop.IntroductionAdvisor代表引介切面，
-  引介切面是对应引介增强的特殊的切面，它应用于类层上面，所以引介切点使用ClassFilter进行定义。 



### 静态普通方法名匹配切面

切入点 通过ClassFilter可以定位到具体的类上，MethodMatcher可以定位到具体的方法上

切面 ：切入点 和 增强类的集合

```java
// 接口
public interface Animal {
	void sayHello();
}
// 接口实现类
public class Cat implements Animal {

	@Override
	public void sayHello() {
		System.out.println("我是Cat类的sayHello方法。。。");
	}
    
	public void sayHelloCat() {
		System.out.println("我是一只猫。。。");
	}

}
// 增强
public class MyMethodBeforeAdvice implements MethodBeforeAdvice {
	@Override
	public void before(Method method, Object[] args, Object target) throws Throwable {
		System.out.println("==前置增强");
		System.out.println("==方法名：" + method.getName());
		if (null != args && args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				System.out.println("==第" + (i + 1) + "参数：" + args[i]);
			}
		}
		System.out.println("==目标类信息：" + target.toString());
	}
}

```



**静态切面** 

```java

import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

import java.lang.reflect.Method;

/**
 * 静态普通方法名匹配切面
 */
public class MyStaticPointcutAdvisor extends StaticMethodMatcherPointcutAdvisor {

	private static String METHOD_NAME = "sayHello";

	/**
	 * 静态方法匹配判断，这里只有方法名为sayHello的，才能被匹配
	 */
	@Override
	public boolean matches(Method method, Class<?> targetClass) {
		return METHOD_NAME.equals(method.getName());
	}

	/**
	 * 覆盖getClassFilter，只匹配Dog类
	 */
	public ClassFilter getClassFilter() {
		return new ClassFilter() {
			@Override
			public boolean matches(Class<?> clazz) {
				return Dog.class.isAssignableFrom(clazz);
			}
		};
	}
}

// 使用
@Test
public void test1() {
    // 1、创建目标类、增强、切入点
    Animal animal = new Dog();
    MyMethodBeforeAdvice advice = new MyMethodBeforeAdvice();
    MyStaticPointcutAdvisor advisor = new MyStaticPointcutAdvisor();

    // 2、创建ProxyFactory并设置目标类、增强、切面
    ProxyFactory proxyFactory = new ProxyFactory();
    proxyFactory.setTarget(animal);
    // 为切面类提供增强
    advisor.setAdvice(advice);
    proxyFactory.addAdvisor(advisor);

    // 3、生成代理实例
    Dog proxyDog = (Dog) proxyFactory.getProxy();
    proxyDog.sayHelloDog();
    System.out.println("\n\n");
    proxyDog.sayHello();

}
```

​	









 









