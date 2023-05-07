### aspectj-autoproxy 标签属性

<aop:aspectj-autoproxy  /> 而该标签有两个属性，proxy-target-class 和 expose-proxy。

 proxy-target-class : 默认false   如果被代理的目标对象至少实现了一个接口，则会使用JDK动态代理，所有实现该目标类实现的接口都将被代理；如果该目标对象没有实现任何接口，则创建CGLIB动态代理 。 但是可以通过proxy-target-class属性强制指定使用CGLIB代理。如果指定了`proxy-target-class="true"`则将强制开启CGLIB动态代理。 

**如果用了 JDK动态代理  想使用 非接口的方法 在转换类型的时候会报 com.sun.proxy.$Proxy15 cannot be cast to com.qin.demo.proxy.Dog 错误**

 **JDK动态代理 只代理 接口的方法**

**CGLIB动态代理 可以代理所有的方法 但无法通知（ advise ) final 方法，因为它们不能被覆写**

**另外CGLIB动态代理 需要将 CGLIB 二进制友行包放在 classpath 下面**

expose-proxy：解决目标对象内部的自我调用无法实施切面增强的问题  

**（(AService) AopContext.currentProxy() ). b（）；**



- JDK 动态代理：其代理对象必须是某个接口的实现，它是通过在运行期间创建一个接口的实现类未完成对目标对象的代理。
- CGLIB 代理：实现原理类似于JDK 动态代理，只是它在运行期间生成的代理对象是针对目标类扩展的子类。 CGLIB 是高效的代码生成包，底层是依靠 ASM （开源的 Java 字节码编辑类库）操作字节码实现的，性能比 JDK 强。
- expose-proxy：有时候目标对象内部的自我调用将无法实施切面中的增强 



### aspectj-autoproxy 标签解析

解析入口 ：

DefaultBeanDefinitionDocumentReader类的parseBeanDefinitions方法 

```java
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
    if (delegate.isDefaultNamespace(root)) {
        NodeList nl = root.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                if (delegate.isDefaultNamespace(ele)) {
                    // 默认命名空间
                    parseDefaultElement(ele, delegate);
                }
                else {
                    // 自定义命名空间
                    delegate.parseCustomElement(ele);
                }
            }
        }
    }
    else {
        // 自定义命名空间
        delegate.parseCustomElement(root);
    }
}
```

通过parseCustomElement解析自定义标签 :

对自定义标签的解析交给了NamespaceHandler接口，如果我们想在Spring中实现自己的自定义标签，那么就需要实现NamespaceHandler接口，并通过重写其中的方法，来完成对自定义标签的解析 

```java
// 解析自定义标签
@Nullable
public BeanDefinition parseCustomElement(Element ele, @Nullable BeanDefinition containingBd) {
    // 1、获取namespaceUri
    String namespaceUri = getNamespaceURI(ele);
    if (namespaceUri == null) {
        return null;
    }
    // 2、根据namespaceUri得到命名空间解析器
    // 如果我们想在Spring中实现自己的自定义标签，那么就需要实现NamespaceHandler接口，
    // 并通过重写其中的方法，来完成对自定义标签的解析
    NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
    
    if (handler == null) {
        error("Unable to locate Spring NamespaceHandler for XML schema namespace [" + namespaceUri + "]", ele);
        return null;
    }
    // 2、使用命名空间解析器解析自定义标签--AOP 使用的 AopNamespaceHandler
    return handler.parse(ele, new ParserContext(this.readerContext, this, containingBd));
}
```



```java
public NamespaceHandler resolve(String namespaceUri) {
    // 1.获取所有的namespaceUri，NamespaceHandler键值对map集合并得到
    // 当前namespaceUri对应的NamespaceHandler类
    Map<String, Object> handlerMappings = getHandlerMappings();
    Object handlerOrClassName = handlerMappings.get(namespaceUri);
    if (handlerOrClassName == null) {
        return null;
    }
    else if (handlerOrClassName instanceof NamespaceHandler) {
        return (NamespaceHandler) handlerOrClassName;
    }
    else {
        // 2、通过BeanUtils实例化NamespaceHandler并调用其init方法进行初始化操作
        String className = (String) handlerOrClassName;
        try {
            Class<?> handlerClass = ClassUtils.forName(className, this.classLoader);
            if (!NamespaceHandler.class.isAssignableFrom(handlerClass)) {
                throw new FatalBeanException("Class [" + className + "] for namespace [" + namespaceUri +
                        "] does not implement the [" + NamespaceHandler.class.getName() + "] interface");
            }
            NamespaceHandler namespaceHandler = (NamespaceHandler) BeanUtils.instantiateClass(handlerClass);
            // 3、执行init 方法
            namespaceHandler.init();
            handlerMappings.put(namespaceUri, namespaceHandler);
            return namespaceHandler;
        }
        catch (ClassNotFoundException ex) {
            throw new FatalBeanException("Could not find NamespaceHandler class [" + className +
                    "] for namespace [" + namespaceUri + "]", ex);
        }
        catch (LinkageError err) {
            throw new FatalBeanException("Unresolvable class definition for NamespaceHandler class [" +
                    className + "] for namespace [" + namespaceUri + "]", err);
        }
    }
}

// AopNamespaceHandler的init 方法 AspectJAutoProxyBeanDefinitionParser解析器

// AopNamespaceHandler 继承 NamespaceHandlerSupport
public class AopNamespaceHandler extends NamespaceHandlerSupport {

	/**
	 * Register the {@link BeanDefinitionParser BeanDefinitionParsers} for the
	 * '{@code config}', '{@code spring-configured}', '{@code aspectj-autoproxy}'
	 * and '{@code scoped-proxy}' tags.
	 */
	@Override
	public void init() {
		// In 2.0 XSD as well as in 2.1 XSD.
		registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
		registerBeanDefinitionParser("aspectj-autoproxy", new AspectJAutoProxyBeanDefinitionParser());
		registerBeanDefinitionDecorator("scoped-proxy", new ScopedProxyBeanDefinitionDecorator());

		// Only in 2.0 XSD: moved to context namespace as of 2.1
		registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
	}

}

// BeanDefinitionParser 使用 AspectJAutoProxyBeanDefinitionParser
@Override
@Nullable
public BeanDefinition parse(Element element, ParserContext parserContext) {
    BeanDefinitionParser parser = findParserForElement(element, parserContext);
    return (parser != null ? parser.parse(element, parserContext) : null);
}

@Override
@Nullable
public BeanDefinition parse(Element element, ParserContext parserContext) {
    // 1、注册AnnotationAwareAspectJAutoProxyCreator
    AopNamespaceUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(parserContext, element);
    // 2、处理子标签<aop:include/>， 指定@Aspect类，支持正则表达式，符合该表达式的切面类才会被应用
    extendBeanDefinition(element, parserContext);
    return null;
}

public static void registerAspectJAnnotationAutoProxyCreatorIfNecessary(
    ParserContext parserContext, Element sourceElement) {
	// 1、注册AnnotationAwareAspectJAutoProxyCreator
    // public static final String AUTO_PROXY_CREATOR_BEAN_NAME =
			"org.springframework.aop.config.internalAutoProxyCreator";
    // 创建一个叫 AUTO_PROXY_CREATOR_BEAN_NAME 管理自动代理的制造者
    BeanDefinition beanDefinition = AopConfigUtils.registerAspectJAnnotationAutoProxyCreatorIfNecessary(
        parserContext.getRegistry(), parserContext.extractSource(sourceElement));
    // 2、解析子标签 proxy-target-class 和 expose-proxy
    useClassProxyingIfNecessary(parserContext.getRegistry(), sourceElement);
    // 3、注册组件并发送组件注册事件，便于监听器做进一步处理
    registerComponentIfNecessary(beanDefinition, parserContext);
}

// 注册AnnotationAwareAspectJAutoProxyCreator
private static BeanDefinition registerOrEscalateApcAsRequired(Class<?> cls, BeanDefinitionRegistry registry,
			@Nullable Object source) {
    // cls --> org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator
    // AUTO_PROXY_CREATOR_BEAN_NAME --> org.springframework.aop.config.internalAutoProxyCreator
    if (registry.containsBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME)) {
        // 如果registry已经包含了internalAutoProxyCreator，
        BeanDefinition apcDefinition = registry.getBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME);
        // 如果已经注册的internalAutoProxyCreator不是AnnotationAwareAspectJAutoProxyCreator，则需要判断优先级并决定使用哪个
        if (!cls.getName().equals(apcDefinition.getBeanClassName())) {
            int currentPriority = findPriorityForClass(apcDefinition.getBeanClassName());
            int requiredPriority = findPriorityForClass(cls);
            if (currentPriority < requiredPriority) {
                apcDefinition.setBeanClassName(cls.getName());
            }
        }
        // 如果已经注册的internalAutoProxyCreator是AnnotationAwareAspectJAutoProxyCreator，则无需特殊处理
        return null;
    }

    RootBeanDefinition beanDefinition = new RootBeanDefinition(cls);
    beanDefinition.setSource(source);
    // HIGHEST_PRECEDENCE --> 指定最高优先级
    beanDefinition.getPropertyValues().add("order", Ordered.HIGHEST_PRECEDENCE);
    // ROLE_INFRASTRUCTURE --> 表示Spring的内部bean
    beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
    // 注册BeanDefinition
    registry.registerBeanDefinition(AUTO_PROXY_CREATOR_BEAN_NAME, beanDefinition);
    return beanDefinition;
}

// 解析子标签 proxy-target-class 和 expose-proxy
private static void useClassProxyingIfNecessary(BeanDefinitionRegistry registry, @Nullable Element sourceElement) {
    if (sourceElement != null) {
        // PROXY_TARGET_CLASS_ATTRIBUTE --> proxy-target-class
        boolean proxyTargetClass = Boolean.parseBoolean(sourceElement.getAttribute(PROXY_TARGET_CLASS_ATTRIBUTE));
        if (proxyTargetClass) {
            AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
        }
        // EXPOSE_PROXY_ATTRIBUTE --> expose-proxy
        boolean exposeProxy = Boolean.parseBoolean(sourceElement.getAttribute(EXPOSE_PROXY_ATTRIBUTE));
        if (exposeProxy) {
            AopConfigUtils.forceAutoProxyCreatorToExposeProxy(registry);
        }
    }
}


private void extendBeanDefinition(Element element, ParserContext parserContext) {
    BeanDefinition beanDef = parserContext.getRegistry().getBeanDefinition(AopConfigUtils.AUTO_PROXY_CREATOR_BEAN_NAME);
    if (element.hasChildNodes()) {
        addIncludePatterns(element, parserContext, beanDef);
    }
}

// 处理子标签<aop:include/>
private void addIncludePatterns(Element element, ParserContext parserContext, BeanDefinition beanDef) {
    ManagedList<TypedStringValue> includePatterns = new ManagedList<>();
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
        Node node = childNodes.item(i);
        if (node instanceof Element) {
            Element includeElement = (Element) node;
            TypedStringValue valueHolder = new TypedStringValue(includeElement.getAttribute("name"));
            valueHolder.setSource(parserContext.extractSource(includeElement));
            includePatterns.add(valueHolder);
        }
    }
    if (!includePatterns.isEmpty()) {
        includePatterns.setSource(parserContext.extractSource(element));
        beanDef.getPropertyValues().add("includePatterns", includePatterns);
    }
}

```