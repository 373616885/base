### autowire

autowire 即自动注入的意思，通过使用 autowire 特性，我们就不用再显示的配置 bean 之间的依赖了

当 bean 配置中的 autowire = byName 时，Spring 会首先通过反射获取该 bean 所依赖 bean 的名字（beanName），然后再通过调用 BeanFactory.getName(beanName) 方法即可获取对应的依赖实例。

autowire = byName 原理大致就是这样，接下来我们来演示一下

```java
public class Service {

    private Dao mysqlDao;

    private Dao mongoDao;

    // 忽略 getter/setter

    @Override
    public String toString() {
        return super.toString() + "\n\t\t\t\t\t{" +
            "mysqlDao=" + mysqlDao +
            ", mongoDao=" + mongoDao +
            '}';
    }
}

public interface Dao {}
public class MySqlDao implements Dao {}
public class MongoDao implements Dao {}

```

```xml
<bean name="mongoDao" class="xyz.coolblog.autowire.MongoDao"/>
<bean name="mysqlDao" class="xyz.coolblog.autowire.MySqlDao"/>

<!-- 非自动注入，手动配置依赖 -->
<bean name="service-without-autowire" class="xyz.coolblog.autowire.Service" autowire="no">
    <property name="mysqlDao" ref="mysqlDao"/>
    <property name="mongoDao" ref="mongoDao"/>
</bean>

<!-- 通过设置 autowire 属性，我们就不需要像上面那样显式配置依赖了 -->
<bean name="service-with-autowire" class="xyz.coolblog.autowire.Service" autowire="byName"/>
```

```java
String configLocation = "application-autowire.xml";

ApplicationContext applicationContext = 
    new ClassPathXmlApplicationContext(configLocation);
System.out.println("service-without-autowire -> " + applicationContext.getBean("service-without-autowire"));

System.out.println("service-with-autowire -> " + applicationContext.getBean("service-with-autowire"));

```



**ApplicationListener** 

是Spring事件机制的一部分，与抽象类ApplicationEvent类配合来完成ApplicationContext的事件机制。

如果容器中存在ApplicationListener的Bean，当ApplicationContext调用publishEvent方法时，对应的Bean会被触发。这一过程是典型的观察者模式的实现

**ContextRefreshedEvent事件的监听** 

以Spring的内置事件ContextRefreshedEvent为例，当ApplicationContext被初始化或刷新时，会触发ContextRefreshedEvent事件，下面我们就实现一个ApplicationListener来监听此事件的发生。

```java
@Component // 需对该类进行Bean的实例化
public class LearnListener implements ApplicationListener<ContextRefreshedEvent> {
   @Override
   public void onApplicationEvent(ContextRefreshedEvent event) {
      // 打印容器中出事Bean的数量
      System.out.println("监听器获得容器中初始化Bean数量：" + 	event.getApplicationContext().getBeanDefinitionCount());
   }
}
```

**自定义事件及监听**

```java
public class NotifyEvent extends ApplicationEvent {

    private String email;

    private String content;

    public NotifyEvent(Object source) {
        super(source);
    }

    public NotifyEvent(Object source, String email, String content) {
        super(source);
        this.email = email;
        this.content = content;
    }
    // 省略getter/setter方法
}
```

定义监听器NotifyListener：

```java
@Component
public class NotifyListener implements ApplicationListener<NotifyEvent> {

    @Override
    public void onApplicationEvent(NotifyEvent event) {
        System.out.println("邮件地址：" + event.getEmail());
        System.out.println("邮件内容：" + event.getContent());
    }
}


```

单元测试类：

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class ListenerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void testListener() {

        NotifyEvent event = new NotifyEvent("object", "abc@qq.com", "This is the content");

        webApplicationContext.publishEvent(event);
    }
}
```



### ApplicationContext创建过程简析

```java
public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
    this(new String[] {configLocation}, true, null);
}

public ClassPathXmlApplicationContext(String[] configLocations, boolean refresh, @Nullable ApplicationContext parent) throws BeansException {
    // 调用父类构造器
    super(parent);
    // 设定配置文件路径
    // 此函数主要用于解析给定的路径数组，
    // 当然， 如果数组中包含特殊符号，如$｛var｝,
    // 那么 在 resolvePath 中会搜寻匹配的系统变量并替换
    setConfigLocations(configLocations);
    if (refresh) {
        // 扩展功能
        refresh();
    }
}

AbstractApplicationContext.refresh()

@Override
public void refresh() throws BeansException, IllegalStateException {
    synchronized (this.startupShutdownMonitor) {
        // Prepare this context for refreshing.
        // 1、准备刷新上下文环境
        // 例如对系统属性或者环境变量进行准备及验证
        prepareRefresh();

        // Tell the subclass to refresh the internal bean factory.
        // 2、obtainFreshBeanFactory->读取xml并初始化BeanFactory
        ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
		
        // 到这里Spring 已经完成对配置的解析
        // ---------------------------------//
        
        // Prepare the bean factory for use in this context.
        // 3、填充BeanFactory功能
        // ApplicationContext 在功能上的扩展也由此展开
        // 注册一些默认的属性编辑器
        prepareBeanFactory(beanFactory);

        try {
            // Allows post-processing of the bean factory in context subclasses.
            // 4、该方法是个空的模板方法 
            // 所以的bean definitions 已经被加载 ，在实例化之前给一个机会 处理一下 
            postProcessBeanFactory(beanFactory);

            // Invoke factory processors registered as beans in the context.
            // 5、调动工厂的处理器在注册 Bean到容器 之前 
            invokeBeanFactoryPostProcessors(beanFactory);

            // Register bean processors that intercept bean creation.
            // 上面是调用 BeanFactoryPostProcessors 
            // 这里是注册BeanPostProcessors 
            // 6、注册BeanPostProcessors
            registerBeanPostProcessors(beanFactory);

            // Initialize message source for this context.
            // 7、初始化Message资源
            initMessageSource();

            // Initialize event multicaster for this context.
            // 8、初始事件广播器
            initApplicationEventMulticaster();

            // Initialize other special beans in specific context subclasses.
            // 9、留给子类初始化其他Bean(空的模板方法)
            onRefresh();

            // Check for listener beans and register them.
            // 10、注册事件监听器
            registerListeners();

            // Instantiate all remaining (non-lazy-init) singletons.
            // 11、初始化其他的单例Bean(非延迟加载的)
            finishBeanFactoryInitialization(beanFactory);

            // Last step: publish corresponding event.
            // 12、完成刷新过程,通知生命周期处理器lifecycleProcessor刷新过程,
            //   同时发出ContextRefreshEvent通知
            finishRefresh();
            
        } catch (BeansException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Exception encountered during context initialization - " +
                            "cancelling refresh attempt: " + ex);
            }

            // Destroy already created singletons to avoid dangling resources.
            // 13、销毁已经创建的Bean
            destroyBeans();

            // Reset 'active' flag.
            // 14、重置容器激活标签
            cancelRefresh(ex);

            // Propagate exception to caller.
            throw ex;
        }

        finally {
            // Reset common introspection caches in Spring's core, since we
            // might not ever need metadata for singleton beans anymore...
            resetCommonCaches();
        }
    }
}
```



### prepareRefresh刷新上下文的准备工作

```java
/**
 * 准备刷新上下文环境，设置它的启动日期和活动标志，以及执行任何属性源的初始化。
 * Prepare this context for refreshing, setting its startup date and
 * active flag as well as performing any initialization of property sources.
 */
protected void prepareRefresh() {
    	// Switch to active.
        this.startupDate = System.currentTimeMillis();
        this.closed.set(false);
        this.active.set(true);

        if (logger.isInfoEnabled()) {
            logger.info("Refreshing " + this);
        }

        // Initialize any placeholder property sources in the context environment.
        // 在上下文环境中初始化任何占位符属性源。(空的方法,留给子类覆盖)
    	// spring 给用户的扩展 
    	/**
    	 * 继承自 ClassPathXmlApplicationContext 的 MyClassPathXmlApplicationContext
    	 * 重写 initPropertySources() 
    	 * 默认 getEnvironment() 获取到 StandardEnvironment
         * StandardEnvironment 里面有 systemProperties 和 systemEnvironment
    	 * 重写：getEnvironment().setRequiredProperties("java.version");
    	 * 那么：getEnvironment().validateRequiredProperties();就可以检测是否有对应得环境变量
    	 */
        initPropertySources();

        // Validate that all properties marked as required are resolvable:
        // see ConfigurablePropertyResolver#setRequiredProperties
        // 验证需要的属性文件是否都已放入环境中
    	// 查看 ConfigurablePropertyResolver#setRequiredProperties
        getEnvironment().validateRequiredProperties();
		
    
    	// earlyApplicationListeners，applicationListeners，earlyApplicationEvents 初始化
    	// earlyApplicationListeners 不等于空就需要请空 applicationListeners
    	// 把 earlyApplicationListeners 添加到 applicationListeners中
    	/** 
    	 * applicationListeners 和 ApplicationEvents 配合使用 
    	 * 观察者模式
    	 */
        // Store pre-refresh ApplicationListeners...
        if (this.earlyApplicationListeners == null) {
            this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
        }
        else {
            // Reset local application listeners to pre-refresh state.
            this.applicationListeners.clear();
            this.applicationListeners.addAll(this.earlyApplicationListeners);
        }

        // Allow for the collection of early ApplicationEvents,
        // to be published once the multicaster is available...
        this.earlyApplicationEvents = new LinkedHashSet<>();
}



/**
 * 添加容器必要的变量
 */
public void setRequiredProperties(String... requiredProperties) {
    for (String key : requiredProperties) {
        this.requiredProperties.add(key);
    }
}

/**
 * 校验元素是否存在
 */
public void validateRequiredProperties() {
    MissingRequiredPropertiesException ex = new MissingRequiredPropertiesException();
    for (String key : this.requiredProperties) {
        if (this.getProperty(key) == null) {
            ex.addMissingRequiredProperty(key);
        }
    }
    if (!ex.getMissingRequiredProperties().isEmpty()) {
        throw ex;
    }
}

/**
 * 功能：修改ClassPathXmlApplicationContext 
 */
public class MyClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {
	
    // 默认有系统变量 systemProperties （ file.encoding -> UTF-8 java.version -> 1.8.0_221）
    // 和系统环境 systemEnvironment （OS -> Windows_NT ，JAVA_HOME -> D:\Java\jdk1.8.0_221）
    
    @Override
    protected void initPropertySources() {
        // 默认 new StandardEnvironment() 
        getEnvironment().setRequiredProperties("java.version");
    }
}


```



### obtainFreshBeanFactory->读取xml并初始化BeanFactory

```java
// 获取新鲜的 BeanFactory
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
    // 刷新 BeanFactory 已经存在就销毁 然后 重建 放到this.beanFactory 里
    refreshBeanFactory();
    // 获取 this.beanFactory 的值
    ConfigurableListableBeanFactory beanFactory = getBeanFactory();
    if (logger.isDebugEnabled()) {
    	logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);
    }
    return beanFactory;
}


@Override
protected final void refreshBeanFactory() throws BeansException {
    // 1、如果BeanFactory的实例已经存在,则销毁并关闭
    if (hasBeanFactory()) {
        destroyBeans();
        closeBeanFactory();
    }
    // 2、重新创建BeanFactory
    try {
        // 创建 DefaultListableBeanFactory
        DefaultListableBeanFactory beanFactory = createBeanFactory();
        // 为了序列化指定ID，如果需要的话，让这个 BeanFactory 从 id 反序列到 BeanFactory 对象 
        // getId() = ObjectUtils.identityToString(this); 当前对象的toString
        // 放到 serializableFactories 里面
        beanFactory.setSerializationId(getId());
        // 定制 beanFactory， 设置相关属性， 
        // 1.包括是否允许覆盖同名称的不同定义的对象 和 2.循环依赖是否允许存在 
        // 设置＠Autowired 和自Qualifier 注解解析器
        // QualifierAnnotationAutowireCandidateResolver 
        customizeBeanFactory(beanFactory);
         // 加载BeanDefinition
        loadBeanDefinitions(beanFactory);
        synchronized (this.beanFactoryMonitor) {
            this.beanFactory = beanFactory;
        }
    }
    catch (IOException ex) {
        throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
    }
}

/**
 * 如果想设置 allowBeanDefinitionOverriding 和 allowCircularReferences
 * 使用子类覆 盖方法：
public class MyClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {

    @Override
    protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
        super.setAllowBeanDefinitionOverriding(false);
        super.setAllowCircularReferences(false);
        super.customizeBeanFactory(beanFactory);
    }
}
 * 
 */
protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
    if (this.allowBeanDefinitionOverriding != null) {
        beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
    }
    if (this.allowCircularReferences != null) {
        beanFactory.setAllowCircularReferences(this.allowCircularReferences);
    }
}


@Override
protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
    // Create a new XmlBeanDefinitionReader for the given BeanFactory.
    // 为指定 beanFactory 创建 XrnlBeanDefinitionReader 
    XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

    // Configure the bean definition reader with this context's
    // resource loading environment.
    // 对 beanDefinitionReader 进行环境变茧的设恒
    beanDefinitionReader.setEnvironment(this.getEnvironment());
    beanDefinitionReader.setResourceLoader(this);
    // 处理xml 配置文件的
    // EntityResolver 用法
	// SAX 应用程序需要实现自定义处理外部实体，则必须实现此接口并使用 setEntityResolver 方法向 SAX 驱动器注册一个实例
    // 对于解析一个XML,SAX首先读取该XML文档上的声明，根据声明去寻找相应的DTD定义，以便对文档进行一个验证
    // 默认的寻找规则，即通过网络（实现上就是声明的 DTD 的 URI 地址）来下载相应的 DTD 声明，并进行认证
    // EntityResolver 的作用是项目本身就可以提供一个如何寻找 DTD 声明的方法，即由程序来 实现寻找 DTD 声明的过程 
    // 比如我们将 DTD 文件放到项目中某处 ，在实现时直接将此文档读 取并返回给 SAX 即可。 这样就避免了通过网络来寻找相应的声明。 
    // EntityResolver 的接口方法声明 ： 
    // InputSource resolveEntity (String publicId, String systernid) 
    // 两个参数 publicId 和 systemId，并返回一个 inputSource 对象
    // DelegatingEntityResolver 类为 EntityResolver 的实现类
    // 里面的DTD BeansDtdResolver 类 解析
    // 里面的XSD PluggableSchemaResolver 类 解析
    beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

    // Allow a subclass to provide custom initialization of the reader,
    // then proceed with actually loading the bean definitions.
    // 对 BeanDefinitionReader 进行设置，可以被盖 
    initBeanDefinitionReader(beanDefinitionReader);
    // 真正读取配置文件的方法
    loadBeanDefinitions(beanDefinitionReader);
}
```



### 填充BeanFactory功能

```java
protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    
    // Tell the internal bean factory to use the context's class loader etc.
    // 设置类加载器
    beanFactory.setBeanClassLoader(getClassLoader());
    
    // 设置beanFactory的表达式语言处理器,主要用于解析依赖注入 bean 的时候
    // Spring3开始增加了对语言表达式的支持,默认可以使用#{bean.xxx}的形式来调用相关属性值
    // 调用：AbstractAutowireCapableBeanFactory 类的 applyPropertyValues 函数
    //  evaluate(typedStringValue); 
    // 和构造器解析值得时候都用到了：evaluateBeanDefinitionString 
    //if (argValue instanceof String) {
	//	argValue = this.beanFactory.evaluateBeanDefinitionString((String) argValue, mbd);
	//} 
    beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
    
    // 为beanFactory增加一个默认的propertyEditor,这个主要是对bean的属性等设置管理的一个工具
    // 这个是Resource 属性设置管理器
    beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

    // Configure the bean factory with context callbacks.
   	// 添加ApplicationContextAwareProcessor
    // 这BeanPostProcessor 个处理了Aware接口
    /**
     private void invokeAwareInterfaces(Object bean) {
		if (bean instanceof Aware) {
			if (bean instanceof EnvironmentAware) {
				((EnvironmentAware) bean).setEnvironment(this.applicationContext.getEnvironment());
			}
			if (bean instanceof EmbeddedValueResolverAware) {
				((EmbeddedValueResolverAware) bean).setEmbeddedValueResolver(this.embeddedValueResolver);
			}
			if (bean instanceof ResourceLoaderAware) {
				((ResourceLoaderAware) bean).setResourceLoader(this.applicationContext);
			}
			if (bean instanceof ApplicationEventPublisherAware) {
				((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(this.applicationContext);
			}
			if (bean instanceof MessageSourceAware) {
				((MessageSourceAware) bean).setMessageSource(this.applicationContext);
			}
			if (bean instanceof ApplicationContextAware) {
				((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
			}
		}
	}
	*/
    // 在spring实例后，会调用 initializeBean 初始化方法 initializeBean 
    // 里面会调用 invokeAwareMethods 方法 
    // 简单讲：就是实例化 Aware接口 实例的时候调用 上面的set方法 获取	一些对应的资源
    beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
    
    // 设置忽略自动装配的接口
    // 上面的 Aware 类已经不是普通的 bean 
    // 需要在 Spring 做 bean 的依赖注入的时候忽略它们。 
    // 而 ignoreDependencyInterface 的作用正是在此。
    
    // 实例化Aware接口的Bean就可以获取对应得资源 
    // 里面的EnvironmentAware,EmbeddedValueResolverAware，ResourceLoaderAware
    // ApplicationEventPublisherAware,MessageSourceAware,ApplicationContextAware
    // 6个属性不依赖注入
	/**

public class HelloApplicationContextAware implements ApplicationContextAware {
	// 这个不依赖注入
    private EnvironmentAware environmentAware;

    public void setEnvironmentAware(EnvironmentAware environmentAware) {
        this.environmentAware = environmentAware;
    }
	// 这个依赖注入
    private MyBean myBean;

    public void setMyBean(MyBean myBean) {
        this.myBean = myBean;
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    public void testAware() {
        // 通过 hello 这个 bean id 从 beanFactory 获取实例
        Hello hello = applicationContext.getBean("hello", Hello.class);
        hello.say();
        System.out.println(myBean.getDateValue());
        System.out.println(environmentAware.toString());
    }
}
	*/
    beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
    beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
    beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
    beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
    beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

    // BeanFactory interface not registered as resolvable type in a plain factory.
    // MessageSource registered (and found for autowiring) as a bean.
    // 设置几个自动装配的特殊规则
    // 注册依赖 
    // 当 bean 的属性注 入的时候， 
    // 一旦检测到属性为 BeanFactory 类型便会将 beanFactory 的实例注入进去
    // BeanFactory,ResourceLoader,ApplicationEventPublisher,ApplicationContext 
    // 上面4个作为Bean的属性时，直接注入
    beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
    beanFactory.registerResolvableDependency(ResourceLoader.class, this);
    beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
    beanFactory.registerResolvableDependency(ApplicationContext.class, this);
		
    // Register early post-processor for detecting inner beans as ApplicationListeners.
    // 添加ApplicationListener接口到AbstractApplicationContext.applicationListeners或者 
    // 注册检测到的 ApplicationListener 接口
    // 如果是将监听器 
    // 就放到applicationListeners或者ApplicationEventMulticaster.applicationListeners
    beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

    
    // Detect a LoadTimeWeaver and prepare for weaving, if found.
    // 增加对AspectJ的支持
    if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
        beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
        // Set a temporary ClassLoader for type matching.
        beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
    }

    
    // Register default environment beans.
    // 注册默认的系统环境bean
    // containsLocalBean 忽略父类的 bean 只看本容器的情况
    if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
        beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
    }
    if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
        beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
    }
    if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
        beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
    }
}
```



#### invokeBeanFactoryPostProcessors–>调用BeanFactoryPostProcessor

流程：

1. BeanDefinitionRegistry 类型的 beanFactory 必须先处理 BeanDefinitionRegistryPostProcessor 
2. 对于BeanDefinitionRegistryPostProcessor 类型 有自己定义的方法 postProcessBeanDefinitionRegistry，需要先调用 
3. 先处理BeanDefinitionRegistryPostProcessor 类型 的 实现了PriorityOrdered 接口的
4. 调用 BeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry
5. 然后处理BeanDefinitionRegistryPostProcessor 类型 的 实现了Ordered 接口的
6. 调用 BeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry
7. 处理剩余没有实现 PriorityOrdered 接口 和 Ordered 接口的 
8. 调用 BeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry
9. 到这里是 处理 BeanDefinitionRegistryPostProcessor.postProcessBeanDefinitionRegistry 方法的
10. 接着处理 BeanFactoryPostProcessor 类型的 postProcessBeanFactory 方法
11. 到这里 传递过来的 beanFactoryPostProcessors （通过AbstractApplicationContext 中的添加处理器方法 addBeanFactoryPostProcessor 进行添加 ）的都处理了
12. 接下来要处理通过配置文件注册的BeanFactoryPostProcessor
13. 处理过程会实例化 BeanFactoryPostProcessor 



```java
public static void invokeBeanFactoryPostProcessors(
			ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {

    // Invoke BeanDefinitionRegistryPostProcessors first, if any.
    // 首先处理 BeanDefinitionRegistryPostProcessor 接口的
    Set<String> processedBeans = new HashSet<>();
	
    
    // 这一段处理的 beanFactory 是 BeanDefinitionRegistry类型的
    
    // beanFactory是BeanDefinitionRegistry类型
    if (beanFactory instanceof BeanDefinitionRegistry) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        // 存储 BeanFactoryPostProcessor 
        List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
        // 存储 BeanDefinitionRegistryPostProcessor 
        List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();
		
        //对于 BeanDefinitionRegistryPostProcessor类型的 
        //在 BeanFactoryPostProcessor 的基础上还有自己定义的方法，需要先调用
        
        
        //循环手动注册的beanFactoryPostProcessors
        //传递的 beanFactoryPostProcessors 是 BeanDefinitionRegistryPostProcessor 类型的处理
        
        for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
            // 如果是BeanDefinitionRegistryPostProcessor的实例话,则调用其postProcessBeanDefinitionRegistry方法,对bean进行注册操作
            if (postProcessor instanceof BeanDefinitionRegistryPostProcessor) {
                BeanDefinitionRegistryPostProcessor registryProcessor =
                    (BeanDefinitionRegistryPostProcessor) postProcessor;
                 // 如果是BeanDefinitionRegistryPostProcessor类型,则直接调用其postProcessBeanDefinitionRegistry
                 // 对于BeanDefinitionRegistryPostProcessor 类型， 在 BeanFactoryPostProcessor 的基础上还有自己定义的方法，需要先调用
                registryProcessor.postProcessBeanDefinitionRegistry(registry);
                registryProcessors.add(registryProcessor);
            }
            else {
                 // 否则则将其当做普通的BeanFactoryPostProcessor处理,直接加入regularPostProcessors集合,以备后续处理
                regularPostProcessors.add(postProcessor);
            }
        }

        // Do not initialize FactoryBeans here: We need to leave all regular beans
        // uninitialized to let the bean factory post-processors apply to them!
        // Separate between BeanDefinitionRegistryPostProcessors that implement
        // PriorityOrdered, Ordered, and the rest.
        List<BeanDefinitionRegistryPostProcessor> currentRegistryProcessors = new ArrayList<>();

        // First, invoke the BeanDefinitionRegistryPostProcessors that implement PriorityOrdered.
        // 首先调用实现了PriorityOrdered(有限排序接口)的BeanDefinitionRegistryPostProcessors
        // 这里处理 beanFactory 里面的 BeanDefinitionRegistryPostProcessors
        String[] postProcessorNames =
            beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
        for (String ppName : postProcessorNames) {
            // 实现了PriorityOrdered(有限排序接口)的BeanDefinitionRegistryPostProcessors
            if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
                currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                processedBeans.add(ppName);
            }
        }
        // 排序
        sortPostProcessors(currentRegistryProcessors, beanFactory);
        // 加入registryProcessors集合
        registryProcessors.addAll(currentRegistryProcessors);
        // 和上面一样调用 postProcessBeanDefinitionRegistry方法
        invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
        // 处理完 实现PriorityOrdered(有限排序接口)的BeanDefinitionRegistryPostProcessors
        // 清空，接着处理 实现 Ordered 接口的
        currentRegistryProcessors.clear();

        // Next, invoke the BeanDefinitionRegistryPostProcessors that implement Ordered.
        // 其次,调用实现了Ordered(普通排序接口)的BeanDefinitionRegistryPostProcessors
        postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
        for (String ppName : postProcessorNames) {
            // 实现了Ordered(普通排序接口)的BeanDefinitionRegistryPostProcessors
            if (!processedBeans.contains(ppName) && beanFactory.isTypeMatch(ppName, Ordered.class)) {
                // 加入到currentRegistryProcessors中
                currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                processedBeans.add(ppName);
            }
        }
        // 排序
        sortPostProcessors(currentRegistryProcessors, beanFactory);
        // 加入registryProcessors集合
        registryProcessors.addAll(currentRegistryProcessors);
         // 调用所有实现了PriorityOrdered的的BeanDefinitionRegistryPostProcessors的postProcessBeanDefinitionRegistry方法,注册bean
         invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
        // 清空
        currentRegistryProcessors.clear();

        // Finally, invoke all other BeanDefinitionRegistryPostProcessors until no further ones appear.
        // 最后,处理其他的BeanDefinitionRegistryPostProcessors
        boolean reiterate = true;
        while (reiterate) {
            reiterate = false;
            postProcessorNames = beanFactory.getBeanNamesForType(BeanDefinitionRegistryPostProcessor.class, true, false);
            for (String ppName : postProcessorNames) {
                // 之前没有处理的 BeanDefinitionRegistryPostProcessors
                if (!processedBeans.contains(ppName)) {
                    currentRegistryProcessors.add(beanFactory.getBean(ppName, BeanDefinitionRegistryPostProcessor.class));
                    processedBeans.add(ppName);
                    reiterate = true;
                }
            }
            // 排序
            sortPostProcessors(currentRegistryProcessors, beanFactory);
            // 加入registryProcessors集合
            registryProcessors.addAll(currentRegistryProcessors);
            // 调用其他的BeanDefinitionRegistryPostProcessors的postProcessBeanDefinitionRegistry方法,注册bean
            invokeBeanDefinitionRegistryPostProcessors(currentRegistryProcessors, registry);
            currentRegistryProcessors.clear();
        }

        // Now, invoke the postProcessBeanFactory callback of all processors handled so far.
        // 调用所有BeanDefinitionRegistryPostProcessor(包括手动注册和通过配置文件注册)
        // 和BeanFactoryPostProcessor(只有手动注册)的回调函数-->postProcessBeanFactory
        // 这里才是真正调用 BeanFactoryPostProcessor的postProcessBeanFactory	
        invokeBeanFactoryPostProcessors(registryProcessors, beanFactory);
        invokeBeanFactoryPostProcessors(regularPostProcessors, beanFactory);
    }

    else {
        // Invoke factory processors registered with the context instance.
        // 如果不是BeanDefinitionRegistry的实例,那么直接调用其回调函数即可-->postProcessBeanFactory 
        invokeBeanFactoryPostProcessors(beanFactoryPostProcessors, beanFactory);
    }

    // Do not initialize FactoryBeans here: We need to leave all regular beans
    // uninitialized to let the bean factory post-processors apply to them!
     // 3、上面的代码已经处理完了所有的BeanDefinitionRegistryPostProcessors和手动注册的BeanFactoryPostProcessor
    // 接下来要处理通过配置文件注册的BeanFactoryPostProcessor
    // 首先获取所有的BeanFactoryPostProcessor
    // (注意:这里获取的集合会包含BeanDefinitionRegistryPostProcessors)
    String[] postProcessorNames =
     beanFactory.getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);

    // Separate between BeanFactoryPostProcessors that implement PriorityOrdered,
    // Ordered, and the rest.
    // 配置文件注册的BeanFactoryPostProcessor 实现 PriorityOrdered 接口的
    List<BeanFactoryPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
    // 配置文件注册的BeanFactoryPostProcessor 实现 Ordered 接口的
    List<String> orderedPostProcessorNames = new ArrayList<>();
    // 配置文件注册的BeanFactoryPostProcessor 没有实现 顺序接口的
    List<String> nonOrderedPostProcessorNames = new ArrayList<>();
    
    for (String ppName : postProcessorNames) {
        // 上面处理过就不处理
        if (processedBeans.contains(ppName)) {
            // skip - already processed in first phase above
        }
        else if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
            // 加入到PriorityOrdered有序处理器集合
            priorityOrderedPostProcessors.add(beanFactory.getBean(ppName, BeanFactoryPostProcessor.class));
        }
        else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
            // 加入到Ordered有序处理器集合
            orderedPostProcessorNames.add(ppName);
        }
        else {
            // 加入到无序处理器集合
            nonOrderedPostProcessorNames.add(ppName);
        }
    }

    // First, invoke the BeanFactoryPostProcessors that implement PriorityOrdered.
    // 首先调用实现了PriorityOrdered接口的处理器
    // 排序
    sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
    // 调用postProcessBeanFactory方法
    invokeBeanFactoryPostProcessors(priorityOrderedPostProcessors, beanFactory);

    // Next, invoke the BeanFactoryPostProcessors that implement Ordered.
    // 其次,调用实现了Ordered接口的处理器
    List<BeanFactoryPostProcessor> orderedPostProcessors = new ArrayList<>();
    for (String postProcessorName : orderedPostProcessorNames) {
        orderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
    }
    sortPostProcessors(orderedPostProcessors, beanFactory);
    invokeBeanFactoryPostProcessors(orderedPostProcessors, beanFactory);

    // Finally, invoke all other BeanFactoryPostProcessors.
    // 最后,调用无序处理器
    List<BeanFactoryPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
    for (String postProcessorName : nonOrderedPostProcessorNames) {
        nonOrderedPostProcessors.add(beanFactory.getBean(postProcessorName, BeanFactoryPostProcessor.class));
    }
    invokeBeanFactoryPostProcessors(nonOrderedPostProcessors, beanFactory);

    // Clear cached merged bean definitions since the post-processors might have
    // modified the original metadata, e.g. replacing placeholders in values...
    // 清理元数据
    beanFactory.clearMetadataCache();
}

```



对于 BeanDefinitionRegistry 的类型的处理类的处理主要包括以下内容。 

1. 对于硬编码注册的后处理器的处理，主要是通过 AbstractApplicationContext 中的添加处理器方法 addBeanFactoryPostProcessor 进行添加 

   ```java
   @Override
   public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor postProcessor) {
       Assert.notNull(postProcessor, "BeanFactoryPostProcessor must not be null");
       this.beanFactoryPostProcessors.add(postProcessor);
   }
   ```

   

2. 添加完成 存放在 beanFactoryPostProcessors 中

3. 处理BeanFactoryPostProcessor 是先检查 beanFactoryPostProcessors  有没有数据

4. BeanDefinitionRegistryPostProcessor 继承 BeanFactoryPostProcessor  

5. 不但有BeanFactoryPostProcessor  的特性 

6. 还有自己的 postProcessBeanDefinitionRegistry方法 需要调用

7. BeanDefinitionRegistryPostProcessor  只对 BeanDefinitionRegistry 类型有效

8. 如果 beanFactory 并不是 BeanDefinitionRegistry 类型 ，那么就可以忽略 BeanDefinitionRegistryPostProcessor  直接处理 BeanFactoryPostProcessor  

9.  Sping 并不保证读取的顺序 ，所以为了保证用户的调用顺序的要求，Spring 对于后处理器的调用支持按照 PriorityOrdered 或者 Ordered 的顺序调用

   

### 注册BeanPostProcessors 

​	上面是调用BeanFacotoryPostProcessors  这里是注册，真正调用的是在 bean 实例的时候 。

很多功能的 BeanFacotory 是不支持**后处理器的自动注册**的。 所以在调用的时候如果没有进行手动注册其实是不

能使用的。但是 在 ApplicationContext 中却添加了 自动注册功能。

```java
public static void registerBeanPostProcessors(
			ConfigurableListableBeanFactory beanFactory, AbstractApplicationContext applicationContext) {

		String[] postProcessorNames = beanFactory.getBeanNamesForType(BeanPostProcessor.class, true, false);

		// Register BeanPostProcessorChecker that logs an info message when
		// a bean is created during BeanPostProcessor instantiation, i.e. when
		// a bean is not eligible for getting processed by all BeanPostProcessors.
    	// BeanPostProcessorChecker 是一个普通的信息打印 ，
    	// 可能会有些情况， 当 Spring 的配置中的后处理器还没有被注册
    	// 就已经开始了 bean 的初始化时
    	// 便会打印出 BeanPostProcessorChecker 中设定的信息 
    	// 这里的 1 就是 BeanPostProcessorChecker 这个 BeanPostProcessor
    	int beanProcessorTargetCount = beanFactory.getBeanPostProcessorCount() + 1 + postProcessorNames.length;
    
    	// 1、注册一个BeanPostProcessorChecker,它是BeanPostProcessor的子类
   		// 用于在BeanPostProcessor实例化期间创建bean时记录信息消息，
        // 即当bean不符合由所有BeanPostProcessors处理的资格时。
		beanFactory.addBeanPostProcessor(new BeanPostProcessorChecker(beanFactory, beanProcessorTargetCount));

		// Separate between BeanPostProcessors that implement PriorityOrdered,
		// Ordered, and the rest.
    	// 存储 PriorityOrdered 类型 的 BeanPostProcessor
		List<BeanPostProcessor> priorityOrderedPostProcessors = new ArrayList<>();
   		// 存储 MergedBeanDefinitionPostProcessor 类型 的 BeanPostProcessor
		List<BeanPostProcessor> internalPostProcessors = new ArrayList<>();
    	// 存储 Ordered 类型 的 BeanPostProcessor
		List<String> orderedPostProcessorNames = new ArrayList<>();
    	// 存储 其他的 BeanPostProcessor
		List<String> nonOrderedPostProcessorNames = new ArrayList<>();
    	// 归类好
		for (String ppName : postProcessorNames) {
			if (beanFactory.isTypeMatch(ppName, PriorityOrdered.class)) {
				BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
				priorityOrderedPostProcessors.add(pp);
                // 这个没有重复调用
				// 因为在注册
                // 先删除   this.beanPostProcessors.remove(beanPostProcessor);
                // 然后添加 this.beanPostProcessors.add(beanPostProcessor);
                // equals 方法就得看自己的实现了
                // MergedBeanDefinitionPostProcessor 主要在 doCreateBean 
                // createBeanInstance 之后 populateBean 之前处理
                // 一个非常关键的类：AutowiredAnnotationBeanPostProcessor
                // @Autowired @Value 和 @Inject 这3 个注解
				if (pp instanceof MergedBeanDefinitionPostProcessor) {
					internalPostProcessors.add(pp);
				}
			} else if (beanFactory.isTypeMatch(ppName, Ordered.class)) {
				orderedPostProcessorNames.add(ppName);
			} else {
				nonOrderedPostProcessorNames.add(ppName);
			}
		}

		// First, register the BeanPostProcessors that implement PriorityOrdered.
		// 首先注册实现了 PriorityOrdered 的 BeanPostProcessors
    	sortPostProcessors(priorityOrderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, priorityOrderedPostProcessors);

		// Next, register the BeanPostProcessors that implement Ordered.
    	// 接着注册实现了 Ordered 的 BeanPostProcessors
		List<BeanPostProcessor> orderedPostProcessors = new ArrayList<>();
		for (String ppName : orderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			orderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		sortPostProcessors(orderedPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, orderedPostProcessors);

		// Now, register all regular BeanPostProcessors.
    	// 注册常规的 BeanPostProcessor 
		List<BeanPostProcessor> nonOrderedPostProcessors = new ArrayList<>();
		for (String ppName : nonOrderedPostProcessorNames) {
			BeanPostProcessor pp = beanFactory.getBean(ppName, BeanPostProcessor.class);
			nonOrderedPostProcessors.add(pp);
			if (pp instanceof MergedBeanDefinitionPostProcessor) {
				internalPostProcessors.add(pp);
			}
		}
		registerBeanPostProcessors(beanFactory, nonOrderedPostProcessors);

		// Finally, re-register all internal BeanPostProcessors.
    	// 注册 MergedBeanDefinitionPostProcessor 类型的 BeanPostProcessors
		sortPostProcessors(internalPostProcessors, beanFactory);
		registerBeanPostProcessors(beanFactory, internalPostProcessors);

		// Re-register post-processor for detecting inner beans as ApplicationListeners,
		// moving it to the end of the processor chain (for picking up proxies etc).
		// 重复注册ApplicationListenerDetector -- 为了将这个处理器移动末尾
    	beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(applicationContext));
    
	}
```



### 初始化Message资源 

该方法不是很重要，留在以后分析吧。。。

### 初始事件广播器

1. 如果用户自 定义了事件广播器 ，那么使用用户自定义的事件广播器
2. 如果用户没有自定义事件广播器，那么使用默认的 ApplicationEventMulticaster
3. ApplicationEventMulticaster 存储监昕器用的（实现了 ApplicationListener 接口的类）
4. 调用的时候会根据 getApplicationListeners(event, resolveDefaultEventType(event))  去找到对应得监听器

```java
protected void initApplicationEventMulticaster() {
    ConfigurableListableBeanFactory beanFactory = getBeanFactory();
    
    // 1、默认使用内置的事件广播器,如果有的话.
    // 我们可以在配置文件中配置Spring事件广播器或者自定义事件广播器
    // 例如: <bean id="applicationEventMulticaster" class="org.springframework.context.event.SimpleApplicationEventMulticaster"></bean>
    // APPLICATION_EVENT_MULTICASTER_BEAN_NAME是写死的
    if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
        this.applicationEventMulticaster =
            beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
        if (logger.isDebugEnabled()) {
            logger.debug("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
        }
    }
    else {
        // 2、否则,新建一个事件广播器,SimpleApplicationEventMulticaster是spring的默认事件广播器
        this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
        beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
        if (logger.isDebugEnabled()) {
            logger.debug("Unable to locate ApplicationEventMulticaster with name '" +
                         APPLICATION_EVENT_MULTICASTER_BEAN_NAME +
                         "': using default [" + this.applicationEventMulticaster + "]");
        }
    }
}
```

### onRefresh–>留给子类初始化其他Bean

该方法是个空的模板方法

### 注册事件监听器

```java
/**
 * Add beans that implement ApplicationListener as listeners.
 * Doesn't affect other listeners, which can be added without being beans.
 */
protected void registerListeners() {
    // Register statically specified listeners first.
    // 注册容量里的自带的 ApplicationListener
    for (ApplicationListener<?> listener : getApplicationListeners()) {
        // 广播器注册 监听器 
        getApplicationEventMulticaster().addApplicationListener(listener);
    }

    // Do not initialize FactoryBeans here: We need to leave all regular beans
    // uninitialized to let post-processors apply to them!
    // 其次,注册普通的事件监听器 -- 这里不初始化
    // 不初始化 通过 调用的时候（实例化）
    String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
    for (String listenerBeanName : listenerBeanNames) {
        getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
    }

    // Publish early application events now that we finally have a multicaster...
    // 如果有早期事件的话,在这里进行事件广播
    // 因为前期SimpleApplicationEventMulticaster尚未注册，无法发布事件，
    // 因此早期的事件会先存放在earlyApplicationEvents集合中，这里把它们取出来进行发布
    // 所以早期事件的发布时间节点是早于其他事件的
    Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
    // 早期事件广播器是一个Set<ApplicationEvent>集合,
    // 保存了无法发布的早期事件,当SimpleApplicationEventMulticaster
    // 创建完之后随即进行发布,同事也要将其保存的事件释放
    this.earlyApplicationEvents = null;
    if (earlyEventsToProcess != null) {
        for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
            getApplicationEventMulticaster().multicastEvent(earlyEvent);
        }
    }
}
```



### 初始化其他的单例Bean(非延迟加载的)

```java
/**
 * Finish the initialization of this context's bean factory,
 * initializing all remaining singleton beans.
 */
protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
    // Initialize conversion service for this context.
    // 判断有无ConversionService(bean属性类型转换服务接口),并初始化
    if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME) &&
        beanFactory.isTypeMatch(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)) {
        beanFactory.setConversionService(
            beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME, ConversionService.class));
    }

    // Register a default embedded value resolver if no bean post-processor
    // (such as a PropertyPlaceholderConfigurer bean) registered any before:
    // at this point, primarily for resolution in annotation attribute values.
    // 如果beanFactory中不包含EmbeddedValueResolver,则向其中添加一个EmbeddedValueResolver
    // EmbeddedValueResolver-->解析bean中的占位符和表达式 
    // mesHandler 就是一个解析占位符的
    /* 
    <bean id="mesHandler" class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="locations">
            <value>classpath:application.properties</value>
        </property>
    </bean>
    */
    if (!beanFactory.hasEmbeddedValueResolver()) {
        beanFactory.addEmbeddedValueResolver(strVal -> getEnvironment().resolvePlaceholders(strVal));
    }

    // Initialize LoadTimeWeaverAware beans early to allow for registering their transformers early.
    // 初始化LoadTimeWeaverAware类型的bean
    // LoadTimeWeaverAware-->加载Spring Bean时织入第三方模块,如AspectJ
    String[] weaverAwareNames = beanFactory.getBeanNamesForType(LoadTimeWeaverAware.class, false, false);
    for (String weaverAwareName : weaverAwareNames) {
        getBean(weaverAwareName);
    }

    // Stop using the temporary ClassLoader for type matching.
    // 释放临时类加载器
    beanFactory.setTempClassLoader(null);

    // Allow for caching all bean definition metadata, not expecting further changes.
    // 冻结缓存的BeanDefinition元数据
    // 注册的 bean 定义将不被修改或进行任何进一步的处理
    beanFactory.freezeConfiguration();

    // Instantiate all remaining (non-lazy-init) singletons.
    // 初始化其他的非延迟加载的单例bean
    beanFactory.preInstantiateSingletons();
}
```



**ConversionService** 的设置

之前提到了一种 String 转 Date 的方式

Spring 还提供了另一种转换方式。

示例来了解下 Converter 的使用方式。

1：自定义 Converter  

```java
public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    final DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate convert(String source) {
        return LocalDate.parse(source, formatter);
    }
}

```

2:  注册 Spring 容器  id="conversionService" 写死

```xml
<bean id="stringToLocalDateConverter" class="com.qin.demo.config.StringToLocalDateConverter"/>

<bean id="conversionService"
          class="org.springframework.context.support.ConversionServiceFactoryBean">
    <property name="converters">
        <list>
            <ref bean="stringToLocalDateConverter"/>
        </list>
    </property>
</bean>
```

3：使用

```java
// 使用
public static void main(String[] args) {
    DefaultConversionService convers1onService =new DefaultConversionService ();
    convers1onService.addConverter(new StringToLocalDateConverter ());
    String phoneNumberStr = "2019-09-10";
    LocalDate date = convers1onService.convert(phoneNumberStr,LocalDate.class);
    System.out.println(date.format(formatter));
}
```

**初始化非延迟加载**

ApplicationContext 实现的默认行为就是在启动时将所有单例 bean 提前进行实例化。

提前实例化意味着作为初始化过程的一部分

ApplicationContext 实例会创建并配置所有的单例 bean 通常情况下这是一件好事 ，因为这样在配置中的任何错误就会即刻被发现 （否则的话可能要花几个小时甚至几天）才会被发现

```java

	@Override
	public void preInstantiateSingletons() throws BeansException {
		if (logger.isDebugEnabled()) {
			logger.debug("Pre-instantiating singletons in " + this);
		}

		// Iterate over a copy to allow for init methods which in turn register new bean definitions.
		// While this may not be part of the regular factory bootstrap, it does otherwise work fine.
		List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

		// Trigger initialization of all non-lazy singleton beans...
        // 
		for (String beanName : beanNames) {
            
            // Bean定义公共的抽象类是AbstractBeanDefinition，
            //普通的Bean在Spring加载Bean定义的时候，实例化出来的是GenericBeanDefinition，
            //而Spring上下文包括实例化所有Bean用的AbstractBeanDefinition是RootBeanDefinition，
            //这时候就使用getMergedLocalBeanDefinition方法做了一次转化，
            //将非RootBeanDefinition转换为RootBeanDefinition以供后续操作。
            
			RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
            
            //（1）不是抽象的
			//（2）必须是单例的
			//（3）必须是非懒加载的
			if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
                
				if (isFactoryBean(beanName)) {
					Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
					if (bean instanceof FactoryBean) {
						final FactoryBean<?> factory = (FactoryBean<?>) bean;
                        // SmartFactoryBean eagerInit: Bean 是否需要立即加载的意思
                        // SmartFactoryBean基本不会用到
                        // FactoryBean接口的扩展接口。接口实现并不表示是否总是返回单独的实例对象，比如FactoryBean.isSingleton()实现返回false的情况并不清晰地表示每次返回的都是单独的实例对象
						//不实现这个扩展接口的简单FactoryBean的实现，FactoryBean.isSingleton()实现返回false总是简单地告诉我们每次返回的都是单独的实例对象，暴露出来的对象只能够通过命令访问
						//注意：这个接口是一个有特殊用途的接口，主要用于框架内部使用与Spring相关。通常，应用提供的FactoryBean接口实现应当只需要实现简单的FactoryBean接口即可，新方法应当加入到扩展接口中去
						boolean isEagerInit;
						if (System.getSecurityManager() != null && factory instanceof SmartFactoryBean) {
							isEagerInit = AccessController.doPrivileged((PrivilegedAction<Boolean>)
											((SmartFactoryBean<?>) factory)::isEagerInit,
									getAccessControlContext());
						}
						else {
							isEagerInit = (factory instanceof SmartFactoryBean &&
									((SmartFactoryBean<?>) factory).isEagerInit());
						}
						if (isEagerInit) {
							getBean(beanName);
						}
					}
				} else {
                    // 非 FactoryBean 初始化
					getBean(beanName);
				}
			}
		}

		// Trigger post-initialization callback for all applicable beans...
        // 实现该接口后，当所有单例 bean 都初始化完成以后， 
        // 容器会回调 SmartInitializingSingleton接口的方法 afterSingletonsInstantiated。
		// 主要应用场合就是在所有单例 bean 创建完成之后，可以在该回调中做一些事情
		for (String beanName : beanNames) {
			Object singletonInstance = getSingleton(beanName);
			if (singletonInstance instanceof SmartInitializingSingleton) {
				final SmartInitializingSingleton smartSingleton = (SmartInitializingSingleton) singletonInstance;
				if (System.getSecurityManager() != null) {
					AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
						smartSingleton.afterSingletonsInstantiated();
						return null;
					}, getAccessControlContext());
				}
				else {
					smartSingleton.afterSingletonsInstantiated();
				}
			}
		}
	}
```



### 完成刷新过程,通知生命周期处理器lifecycleProcessor刷新过程,同时发出ContextRefreshEvent通知

```java
protected void finishRefresh() {
    // Clear context-level resource caches (such as ASM metadata from scanning).
    // 清空资源缓存
    clearResourceCaches();

    // Initialize lifecycle processor for this context.
    // 初始化生命周期处理器
    initLifecycleProcessor();

    // Propagate refresh to lifecycle processor first.
    // 调用生命周期处理器的onRefresh方法
    getLifecycleProcessor().onRefresh();

    // Publish the final event.
    // 推送容器刷新事件
    publishEvent(new ContextRefreshedEvent(this));

    // Participate in LiveBeansView MBean, if active.
    // MBean...没弄明白
    LiveBeansView.registerApplicationContext(this);
}
```































