![](img\20210411152251.png)

1. spring 程序是如何启动的
2. spring是如何加载配置文件到应用程序的
3. 掌握核心接口BeanDefinitionReader
4. 掌握核心接口BeanFactory
5. 彻底弄懂spring的refresh方法
6. BeanPostProcessor接口的作用及实现
7. BeanFactoryPostProcessor接口的作用及实现
8. Spring Bean有没有必要实现Aware接口
9. 彻底理解bean的生命周期
10. 循环依赖问题
11. factoryBean接口的作用
12. bean的初始化都经历了什么
13. cglib和jdk动态代理的机制
14. aop是如何处理
15. 如何回答spring相关问题



### 谈一下spring 下面这个图

![](img\20210411220600.png)

---

![](img\20210411220600.png)





### Aware 接口

bean按照使用者：

- 自定义对象
- 容器内置对象

Aware  接口就是为了 帮助自定义对象获取容器对象

例如：

- BeanFactoryAware 接口获取 BeanFactory
- ApplicationContextAware 接口获取 ApplicationContext





### wrapper 包装类

直接：bean.setPropertyValue("name","覃杰鹏");

![](img\20210411221100.png)





### BeanFactory和FactoryBean的区别

BeanFactory：IOC容器应遵守的的最基本的接口，例如XmlBeanFactory，ApplicationContext都是附加了某种功能的BeanFactory

FactoryBean：实例化Bean的接口-工厂模式



### FactoryBean接口的作用

如果bean都是通过反射去创建实例化bean，那么在某些情况下，xml中<bean>需要配置大量信息

Spring 为此提供了一个 org.springframework.beans.factory.FactoryBean 工厂类接口，用户可以

通过实现该接口定制实例化 bean 的逻辑

当调用getBean("car")时，Spring通过反射机制发现CarFactoryBean实现了FactoryBean的接口

这时Spring容器就调用接口方法CarFactoryBean#getObject()方法返回

如果希望获取CarFactoryBean的实例

需要在使用getBean(beanName)方法时在beanName前显示的加上"&"前缀：如getBean("&car");

```java
@Data
public class Car {
    private int maxSpeed;
    private String brand;
    private double price;
}

public class CarFactoryBean implements FactoryBean<Car> {

    private String carInfo;

    @Override
    public Car getObject() throws Exception {
        Car car = new Car();
        String[] infos = carInfo.split(",");
        car.setBrand(infos[0]);
        car.setMaxSpeed(Integer.valueOf(infos[1]));
        car.setPrice(Double.valueOf(infos[2]));
        return car;
    }

    @Override
    public Class<?> getObjectType() {
        return Car.class;
    }
}

```

```xml
<bean id="car"class="com.qin.factorybean.CarFactoryBean" P:carInfo="法拉利,400,2000000"/>
```





### 面试谈谈Spring

spring 是一个管理bean容器的框架

核心：ioc 管理容器 ,aop 面向切面编程

ioc：把对象的创建、初始化、销毁交给spring来管理，主要通过DI依赖注入，set注入，构造器注入，和接口注入（现已淘汰）

aop：采用的动态代理对bean进行增强，主要通过jdk动态代理和cglib动态代理

​	

Spring 管理bean过程

bean信息加载到BeanDefinition

1. 定义bean的信息 xml  properties 或者Yaml
2. 通过BeanDefinitionReader接口加载，解析bean信息到 BeanDefinition里
3. BeanDefinition 里面的信息需要修改或者增加
4. 例如：${} 占位符的替换，还有Configuration配置增强，Component ，ComponentScan，Import，ImportResource
5. Spring 使用BeanFactoryPostProcessor接口进行扩展
6. 这时候得到一个完整的 BeanDefinition



反射构造器实例化对象--之前有一个扩展--实例化扩展器

InstantiationAwareBeanPostProcessor 接口 

调用代码：resolveBeforeInstantiation（）

AOP 切面信息就在这里定义，advises 放到ConcurrentHashMap里面缓存

正常情况下 InstantiationAwareBeanPostProcessor 返回 null  

TargetSource很少用



通过BeanDefinition反射构造器实例化对象 

1. 内部通过 BeanWrapperImpl实现的
2. Constructor<?>[] rawCandidates= beanClass.getDeclaredConstructors(); 
3. BeanUtils.instantiateClass   里面 ctor.newInstance(argsWithDefaultValues)



实例化对象完成添加三级缓存



接着属性赋值 populateBean --帕谱雷 

1. autowire by name 根据名称赋值
2. autowire by type 根据类型赋值



接着初始化--扩展 通过BeanPostProcessor实现

1. 实现aware接口，获取spring内置对象

只有3个BeanName ，BeanClassLoader，BeanFactory

其他的在new ApplicationContextAwareProcessor里面

```java
Spring 容器 refresh
prepareBeanFactory 
beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
```

2. 扩展 BeanPostProcessor 前置方法

例如：javax.validation.Validator.validate 属性值的验证

3. 执行初始化方法

两个：

实现InitializingBean接口的afterPropertiesSet方法 

BeanDefinition 的 InitMethod 属性，解析xml的init-method

注意：

@PostConstruct是通过BeanPostProcessor实现的

4. 扩展 BeanPostProcessor 后置方法 --aop 增强就是在这里扩展的

```
postProcessAfterInitialization
getAdvicesAndAdvisorsForBean
wrapIfNecessary 
getAdvicesAndAdvisorsForBean 里面判断当前类是否需要被增强
findEligibleAdvisors
findAdvisorsThatCanApply
AopUtils.findAdvisorsThatCanApply
canApply
Pointcut.ClassFilter 类过滤
Pointcut.MethodMatcher 方法匹配
```





### Spring 三级缓存

DefaultSingletonBeanRegistry三个属性：

```java
//一级缓存--成品的bean
singletonObjects = new ConcurrentHashMap<>(256);
//二级缓存--半成品的bean--没有属性赋值
earlySingletonObjects = new ConcurrentHashMap<>(16);
//三级缓存--缓存一个接口--调用getEarlyBeanReference--aop动态代理在这里覆盖原来的成品对象
Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16); 
```

**一级缓存有什么问题？**

1个map里面既有完整的已经ready的bean，也有不完整的，尚未设置field的bean

有其他线程去这个map里获取bean来用怎么办？

**二级缓存有什么问题？**

正常二级缓存没有什么问题，但在aop增强会报异常

因为循环依赖B 取到的值是原A的对象，不是增强后的对象 

**三级缓存，怎么解决这个问题？**

通过一个函数式表达式去提前获取调用AOP增强后置处理器，存到缓存里

后面回到A处理增强的时候，如果已经提前处理了，就不会再次增强

**过程：**

1. 创建A对象，此时，属性什么的全是null，可以理解为，只是new了，field还没设置
2. 添加到第三级缓存；（单例且开启循环依赖）加进去的，只是个factory，只有循环依赖的时候，才会发挥作用
3. 填充属性；循环依赖情况下，A/B循环依赖。假设当前为A，那么此时填充A的属性的时候，会去：new B；
4. 填充B的field，发现field里有一个是A类型，然后就去getBean("A")，然后走到第三级缓存
5. 拿到了A的ObjectFactory，然后调用ObjectFactory
6. A的ObjectFactory里调用AOP的后置处理器类: getEarlyBeanReference，拿到代理后的proxy A（假设此处有切面满足，则要创建代理，否则返回）
7. 然后proxy A 放到二级缓存里
8. 经过上面的步骤后，B里面，field已经填充ok，其中，且填充的field是代理后的A , proxy A
9. 接着 B 继续其他的后续处理
10. B处理完成后，回到当前的origin A（原始A）的field中
11. 接着对A进行后置处理，此时调用aop后置处理器的，此时前面已经调用过来不会再去调用wrapIfNecessary，所以这里直接返回原始A，即 origin A
12. 如果AOP后还是和原来的一样则被替换 为 proxy A

```java
// 创建A对象
Object beanA = instanceWrapper.getWrappedInstance()
// 添加A对象到三级缓存
addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
// 属性赋值--关联到B--B去创建
populateBeanA() ---> new ObjectB() 
// 创建B对象
Object beanB = instanceWrapper.getWrappedInstance()
// 添加B对象到三级缓存    
addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
// 属性赋值--关联到A
populateBeanB() ---> getBean("A") 
// getBena(A)  从 AOP的后置处理器获取到 proxy A 放到二级缓存里
// 同时缓存原始对象到到 earlyProxyReferences 用于判断下次是否再次调用增强
protected Object getSingleton(String beanName, boolean allowEarlyReference) {
    // Quick check for existing instance without full singleton lock
    Object singletonObject = this.singletonObjects.get(beanName);
    if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
        singletonObject = this.earlySingletonObjects.get(beanName);
        if (singletonObject == null && allowEarlyReference) {
            synchronized (this.singletonObjects) {
                // Consistent creation of early reference within full singleton lock
                singletonObject = this.singletonObjects.get(beanName);
                if (singletonObject == null) {
                    singletonObject = this.earlySingletonObjects.get(beanName);
                    if (singletonObject == null) {
                        ObjectFactory<?> singletonFactory = this.singletonFactories.get(beanName);
                        if (singletonFactory != null) {
                            //  这里提前调用 AOP的后置处理器类: getEarlyBeanReference，拿到代理后的proxy A
                            singletonObject = singletonFactory.getObject();
                            //  将代理 proxy A 放到二级缓存
                            this.earlySingletonObjects.put(beanName, singletonObject);
                            this.singletonFactories.remove(beanName);
                        }
                    }
                }
            }
        }
    }
    return singletonObject;
}
// B里面拿到 proxy A ，属性赋值完成--将自己放到一级缓存里
protected void addSingleton(String beanName, Object singletonObject) {
    synchronized (this.singletonObjects) {
        this.singletonObjects.put(beanName, singletonObject);
        this.singletonFactories.remove(beanName);
        this.earlySingletonObjects.remove(beanName);
        this.registeredSingletons.add(beanName);
    }
}
// 回到当前的origin A（原始A）的field中
// 因为earlyProxyReferences里已经缓存有证明已经经过代理增强
// 不需要再次AOP增强--直接返回原始A对象

// 从二级缓存里找到 proxy A 
Object earlySingletonReference = getSingleton(beanName, false);
// 如果A还是原始对象则替换为增强对象
if (exposedObject == bean) {
    exposedObject = earlySingletonReference;
}
```







### Spring boot 自动配置原理

@import +@Configuration+ Spring spi （@EnableAutoConfigrution）

自动配置类由各个start提供，使用Configuration +@Bean自定义配置类，放到META_INF/spring.factories

使用spring spi 扫描META_INF/spring.factories的配置类

使用@import 导入自动配置类

在Spring容器refresh里面执行BeanFactoryPostProcessor里面（  invokeBeanFactoryPostProcessors ）

将Configuration的信息放到BeanDefinition里面（ ConfigurationClassPostProcessor ）

1. Spring boot 自身@SpringBootApplication这个注解在 ConfigurationClassPostProcessor 里被扫描到
2. 然后执行@SpringBootApplication的注解
3. 这个注解 EnableAutoConfiguration 去找 META-INF/spring.factories 里面的 EnableAutoConfiguration.class
4. 然后通过import将约定好的自动配置类放到 BeanDefinition里面
5. 最后等待实例化 BeanDefinition





### Spring boot 创建web服务器的地方

在 onRefresh 刷新子容器里面调用 webServlet 容器 （ServletWebServerApplicationContext）

里创建 createWebServer



### Spring boot jar启动原理

spring-boot 通过打包插件 将变成有标准结构的可执行的jar

结构：

Main-Class: org.springframework.boot.loader.JarLauncher
Start-Class: com.example.demo.DemoApplication

JarLauncher：自定义了类加载器，改变类加载，将jar包中的jar包导入

接着调用 Start-Class 应用的main方法

如果是war包，则是 WarLauncher



### Spring mvc 流程

1. 通过handleMap 找到相应的处理器
2. 通过处理器找到对应的controller
3. 执行完成后，返回modeAndView
4. 找到相应的视图处理器
5. 处理完成返回前端





### Spring 用到的设计模式

单例：spring 通过加锁去控制单例的创建，还有饱汉模式，直接创建的

工厂模式：FactoryBean 的形式获取bean

代理模式：动态代理jdk,cglib

观察者模式：Spring 里面的监听器

模板模式：jdbcTemplate

















