###  DynamicAdvisedInterceptor类的intercept方法 

```java
/**
 * General purpose AOP callback. Used when the target is dynamic or when the
 * proxy is not frozen.
 */
// 
private static class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {

    private final AdvisedSupport advised;

    public DynamicAdvisedInterceptor(AdvisedSupport advised) {
        this.advised = advised;
    }

    @Override
    @Nullable
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        
        Object oldProxy = null;
        boolean setProxyContext = false;
        Object target = null;
        
        TargetSource targetSource = this.advised.getTargetSource();
        try {
            // exposeProxy 是否需要 暴露 给当前线程
            if (this.advised.exposeProxy) {
                // Make invocation available if necessary.
                oldProxy = AopContext.setCurrentProxy(proxy);
                setProxyContext = true;
            }
            // Get as late as possible to minimize the time we "own" the target, in case it comes from a pool...
            // 目标对象
            target = targetSource.getTarget();
            Class<?> targetClass = (target != null ? target.getClass() : null);
            // 获取当前方法的拦截器链，并执行调用
            List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
            Object retVal;
            // Check whether we only have one InvokerInterceptor: that is,
            // no real advice, but just reflective invocation of the target.
            // 检测是否拦截器链是否为空，如果拦截器链为空，那么直接通过反射调用目标对象的方法，避免创建MethodInvocation
            if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
                // We can skip creating a MethodInvocation: just invoke the target directly.
                // Note that the final invoker must be an InvokerInterceptor, so we know
                // it does nothing but a reflective operation on the target, and no hot
                // swapping or fancy proxying.
                // 通过反射直接调用目标对象的方法
                Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
                retVal = methodProxy.invoke(target, argsToUse);
            }
            else {
                // We need to create a method invocation...
                // 创建CglibMethodInvocation对象并调用proceed方法，拦截器链被封装到了retVal中
                retVal = new CglibMethodInvocation(proxy, target, method, args, targetClass, chain, methodProxy).proceed();
            }
            retVal = processReturnType(proxy, target, method, retVal);
            return retVal;
        }
        finally {
            if (target != null && !targetSource.isStatic()) {
                targetSource.releaseTarget(target);
            }
            if (setProxyContext) {
                // Restore old proxy.
                AopContext.setCurrentProxy(oldProxy);
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        return (this == other ||
                (other instanceof DynamicAdvisedInterceptor &&
                 this.advised.equals(((DynamicAdvisedInterceptor) other).advised)));
    }

    /**
		 * CGLIB uses this to drive proxy creation.
		 */
    @Override
    public int hashCode() {
        return this.advised.hashCode();
    }
}

```



### JdkDynamicAopProxy 类的invoke方法： 

```java
/**
 * Implementation of {@code InvocationHandler.invoke}.
 * <p>Callers will see exactly the exception thrown by the target,
 * unless a hook method throws an exception.
 */
@Override
@Nullable
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Object oldProxy = null;
    boolean setProxyContext = false;

    TargetSource targetSource = this.advised.targetSource;
    Object target = null;

    try {
        // 1、处理equals方法，如果接口中没有定义equals而在实现类中覆盖了equals方法，
        // 那么该equals方法不会被增强
        if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
            // The target does not implement the equals(Object) method itself.
            return equals(args[0]);
        }
        // 2、处理hashCode方法，如果接口中没有定义hashCode而在实现类中覆盖了hashCode方法，
        // 那么该hashCode方法不会被增强
        else if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
            // The target does not implement the hashCode() method itself.
            return hashCode();
        }
        // 3、如果目标对象是DecoratingProxy类型，则返回目标对象的最终对象类型
        // DecoratingProxy接口只有一个getDecoratedClass方法，用于返回目标对象的最终对象类型
        else if (method.getDeclaringClass() == DecoratingProxy.class) {
            // There is only getDecoratedClass() declared -> dispatch to proxy config.
            // 返回 ultimateTargetClass 调用的对象（这里使用了装饰类）
            return AopProxyUtils.ultimateTargetClass(this.advised);
        }
        // 4、如果目标对象是Advice类型，则直接使用反射进行调用
        // opaque-->标记是否需要阻止通过该配置创建的代理对象转换为Advised类型，默认值为false，表示代理对象可以被转换为Advised类型
        // method.getDeclaringClass().isInterface()-->目标对象是接口
        // method.getDeclaringClass().isAssignableFrom(Advised.class)-->
        // 是用来判断一个类Class1和另一个类Class2是否相同或者Class1类是不是Class2的父类。例如：Class1.isAssignableFrom(Class2)
        else if (!this.advised.opaque && method.getDeclaringClass().isInterface() &&
                 method.getDeclaringClass().isAssignableFrom(Advised.class)) {
            // Service invocations on ProxyConfig with the proxy config...
            return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
        }

        Object retVal;
		// 5、解决目标对象内部自我调用无法实施切面增强，在这里暴露代理
        if (this.advised.exposeProxy) {
            // Make invocation available if necessary.
            oldProxy = AopContext.setCurrentProxy(proxy);
            setProxyContext = true;
        }

        // Get as late as possible to minimize the time we "own" the target,
        // in case it comes from a pool.
        target = targetSource.getTarget();
        Class<?> targetClass = (target != null ? target.getClass() : null);

        // Get the interception chain for this method.
        // 6、获取当前方法的拦截器链，并执行调用
        List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

        // Check whether we have any advice. If we don't, we can fallback on direct
        // reflective invocation of the target, and avoid creating a MethodInvocation.
        if (chain.isEmpty()) {
            // We can skip creating a MethodInvocation: just invoke the target directly
            // Note that the final invoker must be an InvokerInterceptor so we know it does
            // nothing but a reflective operation on the target, and no hot swapping or fancy proxying.
            Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
            retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
        }
        else {
            // We need to create a method invocation...
            // 创建MethodInvocation对象并调用proceed方法，拦截器链被封装到了invocation中
            MethodInvocation invocation =
                new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
            // Proceed to the joinpoint through the interceptor chain.
            // 调用拦截器链
            retVal = invocation.proceed();
        }

        // Massage return value if necessary.
        Class<?> returnType = method.getReturnType();
        if (retVal != null && retVal == target &&
            returnType != Object.class && returnType.isInstance(proxy) &&
            !RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
            // Special case: it returned "this" and the return type of the method
            // is type-compatible. Note that we can't help if the target sets
            // a reference to itself in another returned object.
            retVal = proxy;
        }
        else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
            throw new AopInvocationException(
                "Null return value from advice does not match primitive return type for: " + method);
        }
        return retVal;
    }
    finally {
        if (target != null && !targetSource.isStatic()) {
            // Must have come from TargetSource.
            targetSource.releaseTarget(target);
        }
        if (setProxyContext) {
            // Restore old proxy.
            AopContext.setCurrentProxy(oldProxy);
        }
    }
}
```

```java

/* 调用拦截器链
 * currentInterceptorIndex维护了一个计数器，该计数器从-1开始，当计数器值等于拦截方法长度减一时，
 * 表名所有的增强方法已经被调用（但是不一定被真正执行），那么此时调用连接点的方法，
 * 针对本例：即sayHello方法
 */
@Override
@Nullable
public Object proceed() throws Throwable {
    //	We start with an index of -1 and increment early.
    if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
        return invokeJoinpoint();
    }

    Object interceptorOrInterceptionAdvice =
        this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
    
    // 动态匹配增强
    if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
        // Evaluate dynamic method matcher here: static part will already have
        // been evaluated and found to match.
        InterceptorAndDynamicMethodMatcher dm =
            (InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
        if (dm.methodMatcher.matches(this.method, this.targetClass, this.arguments)) {
            return dm.interceptor.invoke(this);
        }
        else {
            // Dynamic matching failed.
            // Skip this interceptor and invoke the next in the chain.
            return proceed();
        }
    }
    else {
        // It's an interceptor, so we just invoke it: The pointcut will have
        // been evaluated statically before this object was constructed.
        // 静态增强
        return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
    }
}
```

