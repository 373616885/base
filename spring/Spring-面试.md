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



反射构造器实例化对象--之前有一个扩展--实例化扩展器InstantiationAwareBeanPostProcessor 接口 

调用代码：resolveBeforeInstantiation（）

处理AOP 切面信息的Bean---AnnotationAwareAspectJAutoProxyCreator 就是在这里处理，

AnnotationAwareAspectJAutoProxyCreator 处理得到的 advises 放到ConcurrentHashMap里面缓存



正常Bean 是不实现 InstantiationAwareBeanPostProcessor 的

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

4. 扩展 BeanPostProcessor 后置方法 -- aop 增强就是在这里扩展的 AbstractAdvisorAutoProxyCreator.postProcessAfterInitialization

在AbstractAdvisorAutoProxyCreator.postProcessAfterInitialization里面判断当前Bean是否需要增强

ClassFilter 类过滤

MethodMatcher 方法匹配

```
postProcessAfterInitialization
wrapIfNecessary 
getAdvicesAndAdvisorsForBean 里面判断当前类是否需要被增强
findEligibleAdvisors
findAdvisorsThatCanApply
AopUtils.findAdvisorsThatCanApply
canApply
Pointcut.ClassFilter 类过滤
Pointcut.MethodMatcher 方法匹配
```

最后得到一个完整的Bean



### BeanFactory 和  ApplicationContext 区别

BeanFactory 是bean工厂

ApplicationContext 是上下文

ApplicationContext 里面包含 BeanFactory ，是ApplicationContext 的一个属性

spring boot 的 ApplicationContext 默认 是 SpringApplication里面创建的AnnotationConfigServletWebServerApplicationContext



### BeanFactoryPostProcessors 执行逻辑 

BeanDefinitionRegistryPostProcessor 接口给 beanFactory 添加 BeanDefinition 和修改的机会

```java
public static void invokeBeanFactoryPostProcessors(
      ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

   Set<String> processedBeans = new HashSet<>();
   
   if (beanFactory instanceof BeanDefinitionRegistry) {
      BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
      List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
      List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
	  
      //先执行AnnotationConfigServletWebServerApplicationContext本身的beanFactoryPostProcessor
      //就是防止没有初始化AnnotationConfigServletWebServerApplicationContext里面的internalConfigurationAnnotationProcessor
      //就是ConfigurationClassPostProcessor 
      for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
         if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
            BeanDefinitionRegistryPostProcessor registryProcessor =
                  (BeanDefinitionRegistryPostProcessor) postProcessor;
            registryProcessor.postProcessBeanDefinitionRegistry(registry);
            registryProcessors.add(registryProcessor);
         }
         else {
            regularPostProcessors.add(postProcessor);
         }
      }

      // Do not initialize FactoryBeans here: We need to leave all regular beans
      // uninitialized to let the bean factory post-processors apply to them!
      // Separate between BeanDefinitionRegistryPostProcessors that implement
      // PriorityOrdered, Ordered, and the rest.
      List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

      // First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
      // 首先拿到 ConfigurationClassPostProcessor
      String[] postProcessorNames =
            beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
      for (String ppName : postProcessorNames) {
          //ConfigurationClassPostProcessor实现了PriorityOrdered
         if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
            //对ConfigurationClassPostProcessor进行创建
            //对实现了PriorityOrdered的BeanDefinitionRegistryPostProcessor实例化
            currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
            processedBeans.add(ppName);
         }
      }
      //排序
      sortPostProcessors(currentRegistryProcessors, beanFactory);
      registryProcessors.addAll(currentRegistryProcessors);
      //执行BeanDefinitionRegistryPostProcessor接口
      //得到@Configuration里面的很多Bean信息包括@SpringBootApplication里面的
      invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
      currentRegistryProcessors.clear();

      // Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
      //再次去拿BeanDefinitionRegistryPostProcessor接口信息
      postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
      for (String ppName : postProcessorNames) {
         if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
            //实例化BeanDefinitionRegistryPostProcessor接口有Ordered 的 Bean
            currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
            processedBeans.add(ppName);
         }
      }
      //排序
      sortPostProcessors(currentRegistryProcessors, beanFactory);
      registryProcessors.addAll(currentRegistryProcessors);
      //执行ConfigurationClassPostProcessor得到的BeanDefinitionRegistryPostProcessor接口有Ordered 的 Bean
      invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
      currentRegistryProcessors.clear();

      // Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
      boolean reiterate = true;
      while (reiterate) {
         reiterate = false;
         postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
         for (String ppName : postProcessorNames) {
            if (!processedBeans.contains(ppName)) {
               //实例化没有order的BeanDefinitionRegistryPostProcessor接口Bean
               currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
               processedBeans.add(ppName);
               reiterate = true;
            }
         }
         //排序
         sortPostProcessors(currentRegistryProcessors, beanFactory);
         registryProcessors.addAll(currentRegistryProcessors);
         //执行没有PriorityOrdered, order的BeanDefinitionRegistryPostProcessor
         invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry, beanFactory.getApplicationStartup());
         currentRegistryProcessors.clear();
      }

      // Now, invoke the postProcessBeanFactory callback of all processors handled so far.
      invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
      invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
   }

   else {
      // Invoke factory processors registered with the context instance.
      invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
   }

   // 这里可能又得到新的BeanFactoryPostProcessor：
   // 执行没有PriorityOrdered, order的BeanDefinitionRegistryPostProcessor添加了
   // 下面就是按顺序执行
   // 到这里为止：已经不可能添加 BeanDefinition 了
   String[] postProcessorNames =
         beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

   //一路都是执行BeanFactoryPostProcessor方法
    
   List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
   List<String> orderedPostProcessorNames = new ArrayList<>();
   List<String> nonOrderedPostProcessorNames = new ArrayList<>();
   for (String ppName : postProcessorNames) {
      if (processedBeans.contains(ppName)) {
         // skip - already processed in first phase above
      }
      else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
         priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
      }
      else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
         orderedPostProcessorNames.add(ppName);
      }
      else {
         nonOrderedPostProcessorNames.add(ppName);
      }
   }

   // First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
   sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
   invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

   // Next, invoke the BeanFactoryPostProcessors that implement Ordered.
   List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>(orderedPostProcessorNames.size());
   for (String postProcessorName : orderedPostProcessorNames) {
      orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
   }
   sortPostProcessors(orderedPostProcessors, beanFactory);
   invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

   // Finally, invoke all other BeanFactoryPostProcessors.
   List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>(nonOrderedPostProcessorNames.size());
   for (String postProcessorName : nonOrderedPostProcessorNames) {
      nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
   }
   invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

   // Clear cached merged bean definitions since the post-processors might have
   // modified the original metadata, e.g. replacing placeholders in values...
   beanFactory.clearMetadataCache();
}
```



### @Configuration 如何加载的

第一步：

Web服务器在 context = createApplicationContext();

AnnotationConfigServletWebServerApplicationContext 初始化的时候

将名称为： org.springframework.context.annotation.internalConfigurationAnnotationProcessor 的 BeanDefinitionRegistryPostProcessor 的处理器 ConfigurationClassPostProcessor 注册到 BeanDefinitionRegistry

就是ServletWeb额外添加的 ConfigurationClassPostProcessor 的 BeanRegistryPostProcessor



第二步：

在执行ConfigurationClassPostProcessor的postProcessBeanFactory的时候，解析得到顶级的启动类

application = com.qin.spring.Application 

这里会解析所有的配置信息 都是@SpringBootApplication 导入（很多自动配置的信息都是它导入的）

我们自定义的 @Configuration 也是 @SpringBootApplication 导入的

这里我们就得到 @Configuration 的所有 BeanDefinitions 信息



第三步： 

onRefresh(); 创建 createWebServer



第四步：

finishBeanFactoryInitialization 创建对象通过之前得到的 BeanDefinitions 



第五步：

getBean() 得到对象





### Spring AOP

AOP 还有一只 引介增强 很少用到就是给代理对象引入新的接口



第一步：

AspectJAutoProxyBeanDefinitionParser.parse() 处理XML的AOP信息 

AnnotationAwareAspectJAutoProxyCreator 处理注解的AOP信息（这是一个BeanPostProcessor处理器）



默认情况下：

Spring boot 通过 AopAutoConfiguration 将 实现了InstantiationAwareBeanPostProcessor 接口 

的BeanPostProcessor处理器AnnotationAwareAspectJAutoProxyCreator注册到BeanDefinitionRegistry里面

接着通过BeanDefinition在 resolveBeforeInstantiation 实例化AnnotationAwareAspectJAutoProxyCreator对象



AnnotationAwareAspectJAutoProxyCreator 是在前置BeanPostProcessor创建 

AnnotationAwareAspectJAutoProxyCreator 内部把需要所有的Bean放都进去 advises 一个缓存 Map<beanName, Boolean>

这个Map缓存着那个Bean需要被增强



第三步： 

当一个Bean 初始化完成后，调用 AnnotationAwareAspectJAutoProxyCreator 的 后置处理器的时候

会在里面判断当前类是否需要被增强

里面通过：Pointcut.ClassFilter 类过滤   Pointcut.MethodMatcher 方法匹配

```java
postProcessAfterInitialization
wrapIfNecessary 
getAdvicesAndAdvisorsForBean 里面判断当前类是否需要被增强
findEligibleAdvisors
findAdvisorsThatCanApply
AopUtils.findAdvisorsThatCanApply
canApply
Pointcut.ClassFilter 类过滤
Pointcut.MethodMatcher 方法匹配
```





### Srping 事务 

Bean对象创建的时候，已经通过AOP的AnnotationAwareAspectJAutoProxyCreator 增强了

创建对象的时候：发现符合BeanFactoryTransactionAttributeSourceAdvisor 这个Advisor，就给对象织入TransactionInterceptor

调用增强的方法：就是下面的 TransactionInterceptor.invoke 



@EnableTransactionManagement  

默认：ProxyTransactionManagementConfiguration

里面：BeanFactoryTransactionAttributeSourceAdvisor 这个Advisor

这个Advisor： 

TransactionInterceptor： 一个MethodInterceptor环绕增强 

AnnotationTransactionAttributeSource：用来获取类、接口、方法上的事物注解属性 



执行方法的时候调用 TransactionInterceptor 的 invoke 方法

最终调用的是 TransactionAspectSupport.invokeWithinTransaction

1. 通过AnnotationTransactionAttributeSource拿到事务注解属性 
2. TransactionManager 默认 JdbcTransactionManager
3. TransactionInfo txInfo =  创建事物（如果需要的话的，根据事物传播特性而定）并将信息放到 ThreadLocal上
4. retVal = invocation.proceedWithInvocation();  //执行目标类的方法
5. 失败 completeTransactionAfterThrowing  //事物回滚--结束
6. 最终 cleanupTransactionInfo 清除 ThreadLocal上的事务信息 
7. commitTransactionAfterReturning 提交事务



实现事务传播原理：

Spring 事务信息存在TheadLocal中的，所以永远一个线程只能一个事务



#### 每次都创建新的 TransactionInfo

- createTransactionIfNecessary  获取  TransactionInfo

  - 每次都创建新的 TransactionStatus

  - 在事务管理器里面 判断

  - ThreadLocal<Map<Object, Object>> resources 中 查看是否有 newConnectionHolder

  -  ThreadLocal<Boolean> actualTransactionActive 是否等于 true

    - 没有存在事务 ：

    - new 一个  ConnectionHolder 放到 ThreadLocal<Map<Object, Object>> resources  中

    - ```java
      TransactionSynchronizationManager.bindResource(); 
      ```

    - ThreadLocal<Boolean> actualTransactionActive  =  true

    - TransactionStatus . newTransaction = true

    - 已存在事务：PROPAGATION_REQUIRES_NEW 和 PROPAGATION_REQUIRED这两隔离级别

    - 拿到上一个事务的 ConnectionHolder 放到  TransactionStatus 中

    - TransactionStatus . newTransaction = false

    

    - 绑定当前创建的 TransactionInfo 到 ThreadLocal<TransactionInfo>  transactionInfoHolder

    - ```java
      ThreadLocal<TransactionInfo> transactionInfoHolder
      ```

    

    

- 总结：每次都创建新的 TransactionInfo 和 TransactionStatus 
- 区别，里面的属性 newTransaction  是否等于 true
- 上一个事务的信息 TransactionInfo 放到  oldTransactionInfo 里面

 

结构

```java
TransactionInfo.TransactionStatus
   --> DefaultTransactionStatus 
    		属性：newTransaction = false
    		属性：DataSourceTransactionObject
    				newConnectionHolder = false
    
ThreadLocal<Boolean> actualTransactionActive //是否已存在事务    
ThreadLocal<Map<Object, Object>> resources // newConnectionHolder 数据库 Connection

ThreadLocal<TransactionInfo> transactionInfoHolder //存储当前事务信息
```



#### 执行目标方法 

```java
retVal = invocation.proceedWithInvocation();
```



#### catch 异常

捕获到继续往上拋，后面 commitTransactionAfterReturning 没机会执行

```java
completeTransactionAfterThrowing(txInfo, ex);

//异常条件符合
txInfo.transactionAttribute.rollbackOn(ex)
//回滚
txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());

//接着继续往上拋
```



### finally 

```java
cleanupTransactionInfo(txInfo);
//将当前的 txInfo 变成之前 old  txinfo
transactionInfoHolder.set(this.oldTransactionInfo);
```



#### 正常执行（catch 没有捕获到继续往上拋）

```java
// 提交事务
commitTransactionAfterReturning(txInfo); 
```



### Spring 三级缓存

Spring 2.6 之后默认就不支持循环依赖了

需要收到开启

```properties
spring.main.allow-circular-references = true
```



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
6. A的ObjectFactory里调用AOP的后置处理器类: getEarlyBeanReference（里面调用wrapIfNecessary只做aop的增强处理），拿到代理后的proxy A（假设此处有切面满足，则要创建代理，否则返回）
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

新版：META_INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports

里面配置 AutoConfiguration配置类

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

流程：

1. spring-boot 通过提供了一个spring boot-maven-plugin用于程序打包成一个有标准结构的可执行的jar
2. 这个标准结构的可执行的jar，包含了应用依赖的jar和spring boot loader相关的类
3. java -jar 会去找jar中的 MANIFEST.MF文件，在里面找到 Main-Class 对应的 JarLauncher
4. JarLauncher 自定义了类加载器 LaunchedURLClassLoader，改变类加载，将jar包中 BOOT-INF/classes/  和  BOOT-INF/lib/ 导入
5. 接着调用 Start-Class 应用的main方法

代码：

```java

package org.springframework.boot.loader;

public class JarLauncher extends ExecutableArchiveLauncher {
    
    private static final String DEFAULT_CLASSPATH_INDEX_LOCATION = "BOOT-INF/classpath.idx";
    static final Archive.EntryFilter NESTED_ARCHIVE_ENTRY_FILTER = (entry) -> {
        return entry.isDirectory() ? entry.getName().equals("BOOT-INF/classes/") : entry.getName().startsWith("BOOT-INF/lib/");
    };
    
	.........
	
    public static void main(String[] args) throws Exception {
        // ExecutableArchiveLauncher里面launch
        (new JarLauncher()).launch(args);
    }
}

```

ExecutableArchiveLauncher里面launch和getMainClass

```java
protected void launch(String[] args) throws Exception {
    if (!this.isExploded()) {
        //注册url处理器
        JarFile.registerUrlProtocolHandler();
    }
	//创建类加载器
    ClassLoader classLoader = this.createClassLoader(this.getClassPathArchivesIterator());
    
    String jarMode = System.getProperty("jarmode");
    //拿到 Start-Class
    String launchClass = jarMode != null && !jarMode.isEmpty() ? "org.springframework.boot.loader.jarmode.JarModeLauncher" : this.getMainClass();
    //执行 Start-Class：的main	 
    this.launch(args, launchClass, classLoader);
}

protected String getMainClass() throws Exception {
        Manifest manifest = this.archive.getManifest();
        String mainClass = null;
        if (manifest != null) {
            mainClass = manifest.getMainAttributes().getValue("Start-Class");
        }

        if (mainClass == null) {
            throw new IllegalStateException("No 'Start-Class' manifest entry specified in " + this);
        } else {
            return mainClass;
        }
    }
```



从JarLauncher的main方法进入launch（），其中做了三件事：

1. 注册url处理器
2. 创建类加载器（该步骤会去扫描BOOT-INF下的classes和lib下的jar包）
3. 执行启动类的main方法(该步骤将执行由@SpringbootApplication标注的类的main方法)

#### 注册协议处理器

简单的。首先取出”java.protocol.handler.pkgs"环境变量的值，然将"org.springframework.boot.loader"路径追加到其中（通过一简单字符串就能定位到唯一资源）



将org.springframework.boot.loader包设置到 java.protocol.handler.pkgs中，启动时就能加载到springboot在loader下定义的自定义协议解析器



其实在java中也可以通过设置 JVM 启动参数 -D java.protocol.handler.pkgs 来设置 URLStreamHandler（自定义协议处理器时可以通过继承它实现） 实现类的包路径

但这麻烦没有，所以spring帮你设置了





### Spring mvc 流程

1. 通过handleMap 找到相应的处理器
2. 通过处理器找到对应的controller
3. 执行完成后，返回modeAndView
4. 找到相应的视图处理器
5. 处理完成返回前端





### Spring 用到的设计模式

单例：spring 默认的Bean就是单例

工厂模式：FactoryBean 的形式获取bean，名称默认是&开头

代理模式：动态代理jdk,cglib

策略模式：一个接口，多种实现，例如：Resource 接口，多种实现 URLResource，ClassPathResource

观察者模式：Spring 里面的监听器，ApplicationEvent，ApplicationListener.onApplicationEvent，ApplicationPublisher

模板模式：jdbcTemplate，RestTemplate , RedisTemplate

















