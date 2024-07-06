### MergedBeanDefinitionPostProcessor

这个接口对@Autowired和@Value的支持起到了至关重要的作用

bean在实例化的时候就会调到所有的实现了MergedBeanDefinitionPostProcessor接口的实例

其中就有一个非常关键的类：**AutowiredAnnotationBeanPostProcessor**

在doCreateBean 实例化之后，属性赋值之前 

```java
protected Object doCreateBean(final String beanName, final RootBeanDefinition mbd, final @Nullable Object[] args)
			throws BeanCreationException {

		
		if (instanceWrapper == null) {
            // 实例化bean
			instanceWrapper = createBeanInstance(beanName, mbd, args);
		}
		.....

		// Allow post-processors to modify the merged bean definition.
		synchronized (mbd.postProcessingLock) {
			if (!mbd.postProcessed) {
				try {
                    // 调用 postProcessMergedBeanDefinition
					applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
				}
				catch (Throwable ex) {
					throw new BeanCreationException(mbd.getResourceDescription(), beanName,
							"Post-processing of merged bean definition failed", ex);
				}
				mbd.postProcessed = true;
			}
		}
		.....
		// 设置属性
        populateBean(beanName, mbd, instanceWrapper);
    	// 初始化
        exposedObject = initializeBean(beanName, exposedObject, mbd);
		
		
	}

```

```java
@Override
	public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        // 对bean中的属性或者方法进行扫描，扫描的是@Autowired和@Value注解
        // 一旦发现方法或者属性上有这些注解，
        // 就把属性或者方法封装成AutowiredFieldElement或者AutowiredMethodElement对象
        // InjectionMetadata是AutowiredFieldElement和AutowiredMethodElement的抽象类
        // 这样AutowiredAnnotationBeanPostProcessor类的装配工作就完成了，
        // 在后续IOC，依赖注入对bean进行依赖注入时
        // 就可以根据InjectionMetadata对象里面封装的内容进行属性赋值了
		InjectionMetadata metadata = findAutowiringMetadata(beanName, beanType, null);
		metadata.checkConfigMembers(beanDefinition);
	}

```

