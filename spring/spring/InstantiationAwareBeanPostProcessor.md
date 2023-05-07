### InstantiationAwareBeanPostProcessor 

InstantiationAwareBeanPostProcessor继承了BeanPostProcessor接口 

所以他有BeanPostProcessor的特性

InstantiationAwareBeanPostProcessor 接口：

```java

public interface BeanPostProcessor {
	// 这里也可以用于 AOP
    // bean已经创建完毕在执行初始化（InitializingBean.afterPropertiesSet()和 Init-Method）之前 
	@Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}
	// 这里也可以用于 AOP
	// bean已经创建完毕在执行初始化（InitializingBean.afterPropertiesSet()和 Init-Method）之后
	@Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}


public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	// 实例化之前的处理，在 doCreateBean 之前 
    // 这里一般用于通过cglib改变bean
	@Nullable
	default Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	// 着里已经实例化完毕，还没设置属性，在这里可以自己完成属性的设置，
    // 返回值标识是否继续执行下去
    // 在 populateBean(beanName, mbd, instanceWrapper); 里执行
	default boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}

    // 已经实例化完毕里执行-- 属性还未被设置 
    // 修改属性值
    // 在 populateBean(beanName, mbd, instanceWrapper); 里执行
	@Nullable
	default PropertyValues postProcessPropertyValues(
			PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {

		return pvs;
	}
    
    // 接着设置属性值 applyPropertyValues(beanName, mbd, bw, pvs);
	
    // 最后在initializeBean(beanName, exposedObject, mbd);里面执行BeanPostProcessor的方法 
}

```



1. InstantiationAwareBeanPostProcessor接口继承BeanPostProcessor接口，它内部提供了3个方法，再加上BeanPostProcessor接口内部的2个方法，所以实现这个接口需要实现5个方法。InstantiationAwareBeanPostProcessor接口的主要作用在于目标对象的实例化过程中需要处理的事情，包括实例化对象的前后过程以及实例的属性设置
2. postProcessBeforeInstantiation方法是最先执行的方法，它在目标对象实例化之前调用，该方法的返回值类型是Object，我们可以返回任何类型的值。由于这个时候目标对象还未实例化，所以这个返回值可以用来代替原本该生成的目标对象的实例(比如代理对象)。如果该方法的返回值代替原本该生成的目标对象，后续只有postProcessAfterInitialization方法会调用，其它方法不再调用；否则按照正常的流程走
3. postProcessAfterInstantiation方法在目标对象实例化之后调用，这个时候对象已经被实例化，但是该实例的属性还未被设置，都是null。因为它的返回值是决定要不要调用postProcessPropertyValues方法的其中一个因素（因为还有一个因素是mbd.getDependencyCheck()）；如果该方法返回false,并且不需要check，那么postProcessPropertyValues就会被忽略不执行；如果返回true，postProcessPropertyValues就会被执行
4. postProcessPropertyValues方法对属性值进行修改(这个时候属性值还未被设置，但是我们可以修改原本该设置进去的属性值)。如果postProcessAfterInstantiation方法返回false，该方法可能不会被调用。可以在该方法内对属性值进行修改
5. 父接口BeanPostProcessor的2个方法postProcessBeforeInitialization和postProcessAfterInitialization都是在目标对象被实例化之后，并且属性也被设置之后调用的
6. Instantiation表示实例化，Initialization表示初始化。实例化的意思在对象还未生成，初始化的意思在对象已经生成