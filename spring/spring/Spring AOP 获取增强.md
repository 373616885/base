### Spring aop 获取增强

aspectj-autoproxy标签的解析过程 ：

注册了 名称为 org.springframework.aop.config.internalAutoProxyCreator 的 AnnotationAwareAspectJAutoProxyCreator  管理器

AnnotationAwareAspectJAutoProxyCreator类实现了BeanPostProcessor接口 

在实例化之前：postProcessBeforeInstantiation

```java
@Override
public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
    Object cacheKey = getCacheKey(beanClass, beanName);
	// 1、预处理判断
    if (!StringUtils.hasLength(beanName) || !this.targetSourcedBeans.contains(beanName)) {
        // 判断之前是否已经缓存有信息
        // 当前类是否需要被自动代理
        if (this.advisedBeans.containsKey(cacheKey)) {
            return null;
        }
        // 判断beanClass是否需要被代理
        // isInfrastructureClass-->
        // 		判断beanClass是否为AOP基础类例如Advice(增强)，Advisors(切面),Pointcut(切点)
        // shouldSkip-->判断beanClass是否指定了不需要代理
        if (isInfrastructureClass(beanClass) || shouldSkip(beanClass, beanName)) {
            // 缓存找到的切面类信息
            this.advisedBeans.put(cacheKey, Boolean.FALSE);
            return null;
        }
    }

    // Create proxy here if we have a custom TargetSource.
    // Suppresses unnecessary default instantiation of the target bean:
    // The TargetSource will handle target instances in a custom fashion.
     // 2、如果有自定义TargetSource的话，则在此创建代理
    /**
     *  自定义TargetSource示例:
     * 	<bean class="org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator">
     * 		<property name="customTargetSourceCreators">
     * 			<list>
     * 				<bean class="org.springframework.aop.framework.autoproxy.target.LazyInitTargetSourceCreator"/>        
     * 			</list>
     * 		</property>
     * 	</bean>
     */
    TargetSource targetSource = getCustomTargetSource(beanClass, beanName);
    if (targetSource != null) {
        if (StringUtils.hasLength(beanName)) {
            this.targetSourcedBeans.add(beanName);
        }
        Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(beanClass, beanName, targetSource);
        Object proxy = createProxy(beanClass, beanName, specificInterceptors, targetSource);
        this.proxyTypes.put(cacheKey, proxy.getClass());
        return proxy;
    }

    return null;
}
```

**isInfrastructureClass：判断当前类是否动态代理类**

```java
@Override
protected boolean isInfrastructureClass(Class<?> beanClass) {
	// 调用下面的方面:
    // 判断是否 Advice ，Pointcut，Advisor AopInfrastructureBean 这些动态代理类
    // 这些类不需要动态代理
    return (super.isInfrastructureClass(beanClass) ||
            (this.aspectJAdvisorFactory != null && 
             // 是否包含 @Aspect 这个注解
             this.aspectJAdvisorFactory.isAspect(beanClass)));
}

protected boolean isInfrastructureClass(Class<?> beanClass) {
    boolean retVal = Advice.class.isAssignableFrom(beanClass) ||
        Pointcut.class.isAssignableFrom(beanClass) ||
            Advisor.class.isAssignableFrom(beanClass) ||
                AopInfrastructureBean.class.isAssignableFrom(beanClass);
    if (retVal && logger.isTraceEnabled()) {
        logger.trace("Did not attempt to auto-proxy infrastructure class [" + beanClass.getName() + "]");
    }
    return retVal;
}
```

##### shouldSkip  方法判断beanClass是否需要被自动代理 

```java
@Override
protected boolean shouldSkip(Class<?> beanClass, String beanName) {
    // TODO: Consider optimization by caching the list of the aspect names
    // 1、查找所有的切面 :
    //		Advisor 这个接口的实现类
    //      从当前BeanFactory中查找所有标记了@AspectJ的注解的bean
    List<Advisor> candidateAdvisors = findCandidateAdvisors();
    // 2、循环判断所有的增强,如果增强是AspectJPointcutAdvisor的实例
    //    并且其名称与当前bean的名称相同,则返回true,即该bean无需代理 
    for (Advisor advisor : candidateAdvisors) {
        if (advisor instanceof AspectJPointcutAdvisor &&
            ((AspectJPointcutAdvisor) advisor).getAspectName().equals(beanName)) {
            return true;
        }
    }
    // 3、父类默认false --不跳过增强--被aop切入
    return super.shouldSkip(beanClass, beanName);
}

@Override
protected List<Advisor> findCandidateAdvisors() {
    // Add all the Spring advisors found according to superclass rules.
    // 1、从父类中获取所有的增强--Advisor 这个接口的实现类 
    List<Advisor> advisors = super.findCandidateAdvisors();
    // Build Advisors for all AspectJ aspects in the bean factory.
    if (this.aspectJAdvisorsBuilder != null) {
        // 2、从当前BeanFactory中查找所有标记了@AspectJ的注解的bean，并返回增强注解集合
        advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
    }
    return advisors;
}



```

```java
/**
 * Look for AspectJ-annotated aspect beans in the current bean factory,
 * and return to a list of Spring AOP Advisors representing them.
 * <p>Creates a Spring Advisor for each AspectJ advice method.
 * @return the list of {@link org.springframework.aop.Advisor} beans
 * @see #isEligibleBean
 */
// 从当前BeanFactory中查找所有标记了@AspectJ的注解的bean，并返回增强注解集合
public List<Advisor> buildAspectJAdvisors() {
    // aspectBeanNames缓存 beanName -- 在后面转换成 bean
    List<String> aspectNames = this.aspectBeanNames;

    if (aspectNames == null) {
        synchronized (this) {
            aspectNames = this.aspectBeanNames;
            if (aspectNames == null) {
                List<Advisor> advisors = new ArrayList<>();
                aspectNames = new ArrayList<>();
                // 查找 所有的bean
                String[] beanNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
                    this.beanFactory, Object.class, true, false);
                
                for (String beanName : beanNames) {
                    // 判断是否在<aop:include name=""/> 里面 
                    // 只有@Aspect切面类的增强才会被提取
                    if (!isEligibleBean(beanName)) {
                        continue;
                    }
                    // We must be careful not to instantiate beans eagerly as in this case they
                    // would be cached by the Spring container but would not have been weaved.
                    // 可能容器还没有织入 -- 通过weaved 静态代理
                    Class<?> beanType = this.beanFactory.getType(beanName);
                    if (beanType == null) {
                        continue;
                    }
                    // 当前类 有 @Aspect 注解
                    // 当前beanType是一个切面类
                    if (this.advisorFactory.isAspect(beanType)) {
                        
                        aspectNames.add(beanName);
                        
                        AspectMetadata amd = new AspectMetadata(beanType, beanName);
                        
                         /**
                         * 切面实例化模型简介
                         *
                         * singleton: 即切面只会有一个实例；
                         * perthis  : 每个切入点表达式匹配的连接点对应的AOP对象都会创建一个新切面实例；
                         *            使用@Aspect("perthis(切入点表达式)")指定切入点表达式；
                         *            例如: @Aspect("perthis(this(com.lyc.cn.v2.day04.aspectj.Dog))")
                         * pertarget: 每个切入点表达式匹配的连接点对应的目标对象都会创建一个新的切面实例；
                         *            使用@Aspect("pertarget(切入点表达式)")指定切入点表达式；
                         *            例如:
                         *
                         * 默认是singleton实例化模型，Schema风格只支持singleton实例化模型，而@AspectJ风格支持这三种实例化模型。
                         */
                        // 切面 singleton实例化模型处理
                        if (amd.getAjType().getPerClause().getKind() == PerClauseKind.SINGLETON) {
                            MetadataAwareAspectInstanceFactory factory =
                                new BeanFactoryAspectInstanceFactory(this.beanFactory, beanName);
                            // 获取切面类里面的 增强类（通知类）
                            List<Advisor> classAdvisors = this.advisorFactory.getAdvisors(factory);
                            if (this.beanFactory.isSingleton(beanName)) {
                                this.advisorsCache.put(beanName, classAdvisors);
                            } else {
                                this.aspectFactoryCache.put(beanName, factory);
                            }
                            advisors.addAll(classAdvisors);
                        } else {
                           
                            // 切面 perthis或pertarget实例化模型处理
                            // Per target or per this.
                            if (this.beanFactory.isSingleton(beanName)) {
                                throw new IllegalArgumentException("Bean with name '" + beanName +
                                                                   "' is a singleton, but aspect instantiation model is not singleton");
                            }
                            MetadataAwareAspectInstanceFactory factory =
                                new PrototypeAspectInstanceFactory(this.beanFactory, beanName);
                            this.aspectFactoryCache.put(beanName, factory);
                            advisors.addAll(this.advisorFactory.getAdvisors(factory));
                        }
                    }
                }
                this.aspectBeanNames = aspectNames;
                return advisors;
            }
        }
    }
	// 2、返回从缓存中获取提取到的增强方法
    if (aspectNames.isEmpty()) {
        return Collections.emptyList();
    }
    
    List<Advisor> advisors = new ArrayList<>();
    for (String aspectName : aspectNames) {
        
        List<Advisor> cachedAdvisors = this.advisorsCache.get(aspectName);
        if (cachedAdvisors != null) {
            advisors.addAll(cachedAdvisors);
        }
        else {
            MetadataAwareAspectInstanceFactory factory = this.aspectFactoryCache.get(aspectName);
            advisors.addAll(this.advisorFactory.getAdvisors(factory));
        }
    }
    return advisors;
}



// 检查给定的 aspect bean 是不是在  <aop:include name=""/> 里面
protected boolean isEligibleAspectBean(String beanName) {
    if (this.includePatterns == null) {
        return true;
    } else {
        for (Pattern pattern : this.includePatterns) {
            if (pattern.matcher(beanName).matches()) {
                return true;
            }
        }
        return false;
    }
}
```



### 关于Advisor：

**这里获取的增强，并不是Advice而是Advisor** ：

如果我们定义了一个DogAspect类，并用@AspectJ对其进行注解,那么该类仅仅代表一个切面类，会被Spring扫描并解析，仅此而已，该类不代表SpringAop概念中的切面 

关于SpringAop中的切面概念，可以理解为 切面=连接点+增强 

而标记了@AspectJ注解的类在被Spring解析的时候 

1. 提取该类的方法上的切点表达式注解：例如–>@Pointcut(“execution(* com.lyc.cn.v2.day07.*.*(…))”)，解析之后,就可以的到具体的切点.
2. 提取该类的方法上的增强注解:例如：@Before(“test()”)解析之后,就可以得到具体的增强代码

最后,通过第一步和第二步的操作,就可以得到切点+增强,那么自然就构成了一个切面 

**但是Advisor接口里只包含了一个Advice,并且Advisor一般不直接提供给用户使用**,所以这里也可以理解为获取增强，当然如果理解为切面也是没有问题的 















