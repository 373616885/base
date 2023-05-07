###  prepareTransactionInfo 事物管理

之前 分析了基于@Transactional注解的声明式事物事物标签提取 

实现的过程在 TransactionInterceptor 的 invoke 方法里

##### 拦截器链调用回顾

```java
@Override
@Nullable
public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
    Object oldProxy = null;
    boolean setProxyContext = false;
    Object target = null;
    // 原始对象 TargetSource 封装了 class isStatic 属性
    TargetSource targetSource = this.advised.getTargetSource();
    try {
        // 是否暴露代理对象：使用 AopContext.currentProxy() 获取暴露的对象
        if (this.advised.exposeProxy) {
            // Make invocation available if necessary.
            oldProxy = AopContext.setCurrentProxy(proxy);
            setProxyContext = true;
        }
        // Get as late as possible to minimize the time we "own" the target, in case it comes from a pool...
        // 原始对象
        target = targetSource.getTarget();
        // 得到class
        Class<?> targetClass = (target != null ? target.getClass() : null);
        // 获取当前方法的拦截器链，并执行调用
        List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
        Object retVal;
        // Check whether we only have one InvokerInterceptor: that is,
        // no real advice, but just reflective invocation of the target.
        // 检测是否拦截器链是否为空，如果拦截器链为空，
        // 那么直接通过反射调用目标对象的方法，避免创建MethodInvocation
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
@Nullable
public Object proceed() throws Throwable {
    // We start with an index of -1 and increment early.
    // 一个一个循环调用拦截器链 - 执行到最后就执行原始的方法
    if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
        // 调用拦截器链都执行了，最后反射执行 原始的方法
        return invokeJoinpoint();
    }
	// currentInterceptorIndex 从 0 开始一个一个调用 拦截器链
    Object interceptorOrInterceptionAdvice =
        this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);
    
    // 动态匹配增强
    if (interceptorOrInterceptionAdvice instanceof InterceptorAndDynamicMethodMatcher) {
        // Evaluate dynamic method matcher here: static part will already have
        // been evaluated and found to match.
        InterceptorAndDynamicMethodMatcher dm =
            (InterceptorAndDynamicMethodMatcher) interceptorOrInterceptionAdvice;
        Class<?> targetClass = (this.targetClass != null ? this.targetClass : this.method.getDeclaringClass());
        // 匹配成功则执行
        if (dm.methodMatcher.matches(this.method, targetClass, this.arguments)) {
            return dm.interceptor.invoke(this);
        }
        else {
            // Dynamic matching failed.
            // Skip this interceptor and invoke the next in the chain.
            // 匹配失败则跳过并执行下一个拦截器
            return proceed();
        }
    }
    else {
        // It's an interceptor, so we just invoke it: The pointcut will have
        // been evaluated statically before this object was constructed.
        // 静态增强 -- 调用事务 TransactionInterceptor 的 invoke 方法
        return ((MethodInterceptor) interceptorOrInterceptionAdvice).invoke(this);
    }
}
```



##### TransactionInterceptor实现事物管理过程简析

```java
@Override
@Nullable
public Object invoke(MethodInvocation invocation) throws Throwable {
    // Work out the target class: may be {@code null}.
    // The TransactionAttributeSource should be passed the target class
    // as well as the method, which may be from an interface.
    Class<?> targetClass = (invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null);

    // Adapt to TransactionAspectSupport's invokeWithinTransaction...
    // 调用 TransactionAspectSupport 的 invokeWithinTransaction
    return invokeWithinTransaction(invocation.getMethod(), targetClass, invocation::proceed);
}

```

**TransactionAspectSupport . invokeWithinTransaction**

```java
@Nullable
protected Object invokeWithinTransaction(Method method, @Nullable Class<?> targetClass,
                                         final InvocationCallback invocation) throws Throwable {
	
    if (this.reactiveAdapterRegistry != null) {
        if (KotlinDetector.isKotlinType(method.getDeclaringClass()) && KotlinDelegate.isSuspend(method)) {
            throw new TransactionUsageException("Unsupported annotated transaction on suspending function detected: "
                                                + method + ". Use TransactionalOperator.transactional extensions instead.");
        }
        ReactiveAdapter adapter = this.reactiveAdapterRegistry.getAdapter(method.getReturnType());
        if (adapter != null) {
            return new ReactiveTransactionSupport(adapter).invokeWithinTransaction(method, targetClass, invocation);
        }
        
    }

    // If the transaction attribute is null, the method is non-transactional.
    // 获取之前注册的 AnnotationTransactionAttributeSource 
    TransactionAttributeSource tas = getTransactionAttributeSource();
    // 获取事务的注解属性
    final TransactionAttribute txAttr = (tas != null ? tas.getTransactionAttribute(method, targetClass) : null);
    // 获取事物管理器 -- 创建 DataSourceTransactionManager 和 缓存这个事务管理器
    final PlatformTransactionManager tm = determineTransactionManager(txAttr);
    // 包名 + 类名 + 方法名
    final String joinpointIdentification = methodIdentification(method, targetClass, txAttr);
	// 处理声明式事物	
    if (txAttr == null || !(tm instanceof CallbackPreferringPlatformTransactionManager)) {
        // Standard transaction demarcation with getTransaction and commit/rollback calls.
        // 创建事物（如果需要的话的，根据事物传播特性而定）
        TransactionInfo txInfo = createTransactionIfNecessary(tm, txAttr, joinpointIdentification);

        Object retVal;
        try {
            // This is an around advice: Invoke the next interceptor in the chain.
            // This will normally result in a target object being invoked.
            // 继续调用方法拦截器链,这里一般将会调用目标类的方法
            // 拦截器链调用完了，就执行目标类的方法
            retVal = invocation.proceedWithInvocation();
        }
        catch (Throwable ex) {
            // target invocation exception
            // 如果目标类方法抛出异常,则在此处理,例如:事物回滚
            completeTransactionAfterThrowing(txInfo, ex);
            throw ex;
        }
        finally {
            // 清除上一步创建的事物信息
            // 将当前的TransactionInfo 清空或者恢复到之前的 TransactionInfo
            cleanupTransactionInfo(txInfo);
        }

        if (vavrPresent && VavrDelegate.isVavrTry(retVal)) {
            // Set rollback-only in case of Vavr failure matching our rollback rules...
            TransactionStatus status = txInfo.getTransactionStatus();
            if (status != null && txAttr != null) {
                retVal = VavrDelegate.evaluateTryFailure(retVal, txAttr, status);
            }
        }
		// 调用成功完成后执行，但不是在异常被处理后执行。如果我们不创建事务，就什么也不做。
        commitTransactionAfterReturning(txInfo);
        return retVal;
    }

    else {
        // 处理编程式事物
        final ThrowableHolder throwableHolder = new ThrowableHolder();

        // It's a CallbackPreferringPlatformTransactionManager: pass a TransactionCallback in.
        try {
            Object result = ((CallbackPreferringPlatformTransactionManager) tm).execute(txAttr, status -> {
                TransactionInfo txInfo = prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
                try {
                    Object retVal = invocation.proceedWithInvocation();
                    if (vavrPresent && VavrDelegate.isVavrTry(retVal)) {
                        // Set rollback-only in case of Vavr failure matching our rollback rules...
                        retVal = VavrDelegate.evaluateTryFailure(retVal, txAttr, status);
                    }
                    return retVal;
                }
                catch (Throwable ex) {
                    if (txAttr.rollbackOn(ex)) {
                        // A RuntimeException: will lead to a rollback.
                        if (ex instanceof RuntimeException) {
                            throw (RuntimeException) ex;
                        }
                        else {
                            throw new ThrowableHolderException(ex);
                        }
                    }
                    else {
                        // A normal return value: will lead to a commit.
                        throwableHolder.throwable = ex;
                        return null;
                    }
                }
                finally {
                    cleanupTransactionInfo(txInfo);
                }
            });

            // Check result state: It might indicate a Throwable to rethrow.
            if (throwableHolder.throwable != null) {
                throw throwableHolder.throwable;
            }
            return result;
        }
        catch (ThrowableHolderException ex) {
            throw ex.getCause();
        }
        catch (TransactionSystemException ex2) {
            if (throwableHolder.throwable != null) {
                logger.error("Application exception overridden by commit exception", throwableHolder.throwable);
                ex2.initApplicationException(throwableHolder.throwable);
            }
            throw ex2;
        }
        catch (Throwable ex2) {
            if (throwableHolder.throwable != null) {
                logger.error("Application exception overridden by commit exception", throwableHolder.throwable);
            }
            throw ex2;
        }
    }
}

```

总结：

- 创建事物（如果需要的话的，根据事物传播特性而定）
-  继续调用方法拦截器链,这里一般将会调用目标类的方法 
- 如果目标类方法抛出异常,则在此处理,例如:事物回滚
- 清除上一步创建的事物信息
-  调用成功完成后执行commitTransactionAfterReturning方法 



### 创建事务

```java
protected TransactionInfo createTransactionIfNecessary(@Nullable PlatformTransactionManager tm,
														   @Nullable TransactionAttribute txAttr,
														   final String joinpointIdentification) {

    // If no name specified, apply method identification as transaction name.
    // 1. 如果没有指定名称，则应用方法标识作为事务名称。并使用DelegatingTransactionAttribute封装 txAttr
    if (txAttr != null && txAttr.getName() == null) {
        txAttr = new DelegatingTransactionAttribute(txAttr) {
            @Override
            public String getName() {
                return joinpointIdentification;
            }
        };
    }

    // 2.获取TransactionStatus对象
    TransactionStatus status = null;
    if (txAttr != null) {
        if (tm != null) {
            // 重点: 根据指定的传播行为，返回当前活动的事务或创建新事务。
            status = tm.getTransaction(txAttr);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Skipping transactional joinpoint [" + joinpointIdentification +
                        "] because no transaction manager has been configured");
            }
        }
    }
    // 3.创建TransactionInfo对象
    return prepareTransactionInfo(tm, txAttr, joinpointIdentification, status);
}
```

### 获取事务 -- Spring事物最最关键、最最核心的方法 

```java
@Override
public final TransactionStatus getTransaction(@Nullable TransactionDefinition definition)
    throws TransactionException {

    // Use defaults if no transaction definition given.
    // 如果传递的事务属性为空就默认一个
    TransactionDefinition def = (definition != null ? definition : TransactionDefinition.withDefaults());
	
    // 获取事务的封装对象
    Object transaction = doGetTransaction();
    boolean debugEnabled = logger.isDebugEnabled();
	
    // 如果当前已经存在事物
    if (isExistingTransaction(transaction)) {
        // Existing transaction found -> check propagation behavior to find out how to behave.
        // 当前线程已经存在事务
        return handleExistingTransaction(def, transaction, debugEnabled);
    }
	
    // 下面都是当前不存在事物
    
    // Check definition settings for new transaction.
    // 如果事物定义的超时时间,小于默认的超时时间(-1),抛出异常
    if (def.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {
        throw new InvalidTimeoutException("Invalid transaction timeout", def.getTimeout());
    }

    // No existing transaction found -> check propagation behavior to find out how to proceed.
    // PROPAGATION_MANDATORY 这个Spring事物传播特性 必须在事务中运行，所以这个特性没有事务就拋异常
    if (def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
        throw new IllegalTransactionStateException(
            "No existing transaction found for transaction marked with propagation 'mandatory'");
    } else if (def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
             def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
             def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
        
       	// REQUIRED , REQUIRES_NEW , NESTED都需要新建事务
        // 如果事物传播特性为以下三种,则创建新的事物:
        // PROPAGATION_REQUIRED --> 如果当前没有事物，则新建一个事物；如果已经存在一个事物，则加入到这个事物中。
        // PROPAGATION_REQUIRES_NEW --> 新建事物，如果当前已经存在事物，则挂起当前事物。
        // PROPAGATION_NESTED --> 如果当前存在事物，则在嵌套事物内执行；如果当前没有事物，则与PROPAGATION_REQUIRED传播特性相同 
        
        // 空挂起
        SuspendedResourcesHolder suspendedResources = suspend(null);
        
        if (debugEnabled) {
            logger.debug("Creating new transaction with name [" + def.getName() + "]: " + def);
        }
        try {
            // 都是事务同步
            // 对于事务同步表示设置为 true 的需要重新获取连接
            boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
            
            // 创建DefaultTransactionStatus对象实例
            DefaultTransactionStatus status = newTransactionStatus(
                def, transaction, true, newSynchronization, debugEnabled, suspendedResources);
            
            // 完善 transaction ，包括设置 ConnectionHolder 隔离级别 timeout
            // 如果是新事务则绑定当前线程 -- 绑定对象 ConnectionHolder
            doBegin(transaction, def);
            // 初始化事务同步。针对于当前线程的设置
            // 将事务信息记录在当前线程
            prepareSynchronization(status, def);
            
            return status;
        }
        catch (RuntimeException | Error ex) {
            resume(null, suspendedResources);
            throw ex;
        }
    }
    else {
        // Create "empty" transaction: no actual transaction, but potentially synchronization.
        // 3.4 对于其他的三种传播特性,无需开启新的事物
        // PROPAGATION_SUPPORTS --> 支持当前事物，如果当前没有事物，则以非事物方式执行
        // PROPAGATION_NOT_SUPPORTED --> 以非事物方式执行，如果当前存在事物，则挂起当前事物
        // PROPAGATION_NEVER --> 以非事物方式执行，如果当前存在事物，则抛出异常
        if (def.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT && logger.isWarnEnabled()) {
            logger.warn("Custom isolation level specified but no actual transaction initiated; " +
                        "isolation level will effectively be ignored: " + def);
        }
        boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
        // 封装事务的状态： DefaultTransactionStatus 和 新事务就绑定当前线程
        return prepareTransactionStatus(def, null, true, newSynchronization, debugEnabled, null);
    }
}
```

###  获取当前事物对象  DataSourceTransactionManager :

 获取当前事物对象(如果当前已经存在了事物)。对于不同的事物管理器，获取的方法也是不同的，本例使用的是DataSourceTransactionManager： 

```java
protected Object doGetTransaction() {
    DataSourceTransactionObject txObject = new DataSourceTransactionObject();
    // 是否允许使用保存点，是否允许使用保存点会在具体的事物管理器的构造方法中进行初始化
    /**
     * 例如：
     * 针对本例分析的DataSourceTransactionManager，会在其构造方法中调动setNestedTransactionAllowed(true)方法，
     * 设置允许使用保存点
     */
    txObject.setSavepointAllowed(isNestedTransactionAllowed());
    // 从当前线程中获取ConnectionHolder对象--数据库连接对象
    // 第一次获取到的是空 ,false 表示被新连接 
    ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(obtainDataSource());
    txObject.setConnectionHolder(conHolder, false);
    return txObject;
}
```

 **Spring事物传播特性表** 

<table>
<thead>
<tr>
<th align="left">传播特性名称</th>
<th align="left">说明</th>
</tr>
</thead>
<tbody>
<tr>
<td align="left">PROPAGATION_REQUIRED</td>
<td align="left">如果当前没有事物，则新建一个事物；如果已经存在一个事物，则加入到这个事物中</td>
</tr>
<tr>
<td align="left">PROPAGATION_SUPPORTS</td>
<td align="left">支持当前事物，如果当前没有事物，则以非事物方式执行</td>
</tr>
<tr>
<td align="left">PROPAGATION_MANDATORY</td>
<td align="left">使用当前事物，如果当前没有事物，则抛出异常</td>
</tr>
<tr>
<td align="left">PROPAGATION_REQUIRES_NEW</td>
<td align="left">新建事物，如果当前已经存在事物，则挂起当前事物</td>
</tr>
<tr>
<td align="left">PROPAGATION_NOT_SUPPORTED</td>
<td align="left">以非事物方式执行，如果当前存在事物，则挂起当前事物</td>
</tr>
<tr>
<td align="left">PROPAGATION_NEVER</td>
<td align="left">以非事物方式执行，如果当前存在事物，则抛出异常</td>
</tr>
<tr>
<td align="left">PROPAGATION_NESTED</td>
<td align="left">如果当前存在事物，则在嵌套事物内执行；如果当前没有事物，则与PROPAGATION_REQUIRED传播特性相同</td>
</tr>
</tbody>
</table>

```java
/**
 * This implementation sets the isolation level but ignores the timeout.
 */
@Override
protected void doBegin(Object transaction, TransactionDefinition definition) {
    // DataSourceTransactionManager 用到的事务对象属性
    DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
    // 连接
    Connection con = null;
	// ConnectionHolder简介:包装JDBC连接的资源容器。DataSourceTransactionManager将该类的实例绑定到特定数据源的线程
    try {
        if (!txObject.hasConnectionHolder() ||
            txObject.getConnectionHolder().isSynchronizedWithTransaction()) {
            // 从连接池获取新连接
            Connection newCon = obtainDataSource().getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug("Acquired Connection [" + newCon + "] for JDBC transaction");
            }
            // 事务对象封装连接对象
            txObject.setConnectionHolder(new ConnectionHolder(newCon), true);
        }
		// 同步事务设置为 true
        txObject.getConnectionHolder().setSynchronizedWithTransaction(true);
        // 获取连接
        con = txObject.getConnectionHolder().getConnection();
		// 设置是否只读和隔离级别 --- 在 Connection 里设置 (隔离级别不等于默认才设置)
        Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
        // 事务对象设置隔离级别
        txObject.setPreviousIsolationLevel(previousIsolationLevel);

        // Switch to manual commit if necessary. This is very expensive in some JDBC drivers,
        // so we don't want to do it unnecessarily (for example if we've explicitly
        // configured the connection pool to set it already).
        // 如果已经配置了自动提交，这切换到手动提交 由spring控制
        if (con.getAutoCommit()) {
            txObject.setMustRestoreAutoCommit(true);
            if (logger.isDebugEnabled()) {
                logger.debug("Switching JDBC Connection [" + con + "] to manual commit");
            }
            con.setAutoCommit(false);
        }
		// 只读设置-- 通过 Connection 执行 SET TRANSACTION READ ONLY
        prepareTransactionalConnection(con, definition);
        // 这个标志位 当前连接已经被事务激活
        // 用于判断当前事务的情况：例如-嵌套事务-当前连接是否存在事务的判断
        txObject.getConnectionHolder().setTransactionActive(true);
		// 设置超时时间--这个超时时间 只有 JdbcTemplate 和 TransactionAwareInvocationHandler调用才会
        // 有效，用mybatis 操作数据库 spring的超时是无效的
        int timeout = determineTimeout(definition);
        if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
            txObject.getConnectionHolder().setTimeoutInSeconds(timeout);
        }

        // Bind the connection holder to the thread.
        // 新事务绑定当前线程
        if (txObject.isNewConnectionHolder()) {
            TransactionSynchronizationManager.bindResource(obtainDataSource(), txObject.getConnectionHolder());
        }
    } catch (Throwable ex) {
        if (txObject.isNewConnectionHolder()) {
            DataSourceUtils.releaseConnection(con, obtainDataSource());
            txObject.setConnectionHolder(null, false);
        }
        throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", ex);
    }
}
```

总结：

1. 获取新连接--当前线程不存在ConnectionHolder或者事务同步设置为true的需要重新获取新连接

2. 设置隔离级别和只读--在 Connection 里设置 (隔离级别不等于默认才设置)

3. 更改默认提交的配置--如果设置了自动提交，那么需要改变由Spring来控制

4. 设置标志位，标识当前连接已经被事务激活

5. 设置超时时间--只有 JdbcTemplate 和 TransactionAwareInvocationHandler调用才会有效，用mybatis 操作数据库 spring的超时是无效的

6. 将ConnectionHolder绑定到当前线程

7. ConnectionHolder简介:包装JDBC连接的资源容器。DataSourceTransactionManager将该类的实例绑定到特定数据源的线程

   

```java
// DataSourceUtils.prepareConnectionForTransaction
public static Integer prepareConnectionForTransaction(Connection con, @Nullable TransactionDefinition definition) throws SQLException {
    Assert.notNull(con, "No Connection specified");
    // Set read-only flag.
    // 设置只读标记
    /**
     * 假如：@Transactional(propagation = Propagation.REQUIRED,readOnly = true)
     * 定义了开启事物，又将readOnly属性设置为true，则这里要将数据库连接的readOnly设置为true
     * 这样做可以减少数据库开销，只读数据库连接将占用更少的数据库开销
     */
    if (definition != null && definition.isReadOnly()) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Setting JDBC Connection [" + con + "] read-only");
            }
            con.setReadOnly(true);
        } catch (SQLException | RuntimeException ex) {
            // 异常处理,这里可能会引发连接超时,直接抛出异常
            // 也有可能数据库不支持read-only属性,但是此种情况无需特殊处理,这里也只打印了一行日志而已
            Throwable exToCheck = ex;
            while (exToCheck != null) {
                if (exToCheck.getClass().getSimpleName().contains("Timeout")) {
                    // Assume it's a connection timeout that would otherwise get lost: e.g. from JDBC 4.0
                    throw ex;
                }
                exToCheck = exToCheck.getCause();
            }
            // "read-only not supported" SQLException -> ignore, it's just a hint anyway
            logger.debug("Could not set JDBC Connection read-only", ex);
        }
    }

    // Apply specific isolation level, if any.
    // 如果指定了事物隔离级别,且该隔离级别不等于Spring事物数据库默认隔离级别,则在此设置
    // Spring事物数据库默认隔离级别为-1,即使用底层数据库的事物隔离级别
    Integer previousIsolationLevel = null;
    if (definition != null && definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
        if (logger.isDebugEnabled()) {
            logger.debug("Changing isolation level of JDBC Connection [" + con + "] to " + definition.getIsolationLevel());
        }
        // 从当前数据库连接获取数据库事物隔离级别
        int currentIsolation = con.getTransactionIsolation();
        // 如果我们自己配置的数据库事物隔离级别与数据库连接获取的数据库事物隔离级别不相同,
        // 则更改连接的数据库事物隔离级别
        // 从这里也可以看出数据库连接资源的宝贵性啊...
        if (currentIsolation != definition.getIsolationLevel()) {
            previousIsolationLevel = currentIsolation;
            con.setTransactionIsolation(definition.getIsolationLevel());
        }
    }

    return previousIsolationLevel;
}
```

### 将事务信息记录在当前线程

```java
// 将事务信息记录在当前线程
prepareSynchronization(status, def);

/**
 * Initialize transaction synchronization as appropriate.
 */
protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
    if (status.isNewSynchronization()) {
        // 这几个变量都是 ThreadLocal 
        // 设置事物激活状态
        TransactionSynchronizationManager.setActualTransactionActive(status.hasTransaction());
        // 设置事物隔离级别
        TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(
            definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT ?
            definition.getIsolationLevel() : null);
        // 设置事物只读属性
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(definition.isReadOnly());
        // 设置事物名称
        TransactionSynchronizationManager.setCurrentTransactionName(definition.getName());
        // 激活当前线程的事务同步。事务管理器在事务开始时调用。
        TransactionSynchronizationManager.initSynchronization();
    }
}
```



最后涉及到一个事物同步回调接口的概念：如果为当前事物设置了回调接口，那么事物管理器会在事物执行期间调用该接口。例如：为下面的业务方法注册了TransactionSynchronizationAdapter接口，那么事物管理器会在事物执行期间调用我们已经实现的TransactionSynchronizationAdapter接口的方法。

```java
@Override
@Transactional(propagation = Propagation.REQUIRED,rollbackFor = RuntimeException.class,readOnly = false)
public void delete() throws RuntimeException {
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
        @Override
        public void beforeCommit(boolean readOnly) {
            System.out.println("==回调,事物提交之前");
            super.beforeCommit(readOnly);
        }

        @Override
        public void afterCommit() {
            System.out.println("==回调,事物提交之后");
            super.afterCommit();
        }

        @Override
        public void beforeCompletion() {
            super.beforeCompletion();
            System.out.println("==回调,事物完成之前");
        }

        @Override
        public void afterCompletion(int status) {
            super.afterCompletion(status);
            System.out.println("==回调,事物完成之后");
        }
    });

    System.out.println("==调用AccountService的dele方法\n");
    jdbcTemplate.update(insert_sql);
}
```



### 处理已经存在的事务

```java
/**
 * Create a TransactionStatus for an existing transaction.
 */
private TransactionStatus handleExistingTransaction(
    TransactionDefinition definition, Object transaction, boolean debugEnabled)
    throws TransactionException {
    
	// 如果是非事务运行的形式--报错
    if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
        throw new IllegalTransactionStateException(
            "Existing transaction found for transaction marked with propagation 'never'");
    }
    
	// 以非事物方式执行，如果当前存在事物，则挂起当前事物
    if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
        if (debugEnabled) {
            logger.debug("Suspending current transaction");
        }
        // 重点:挂起已有事物
        Object suspendedResources = suspend(transaction);
        // 事务同步
        boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
        // 创建新事物,注意:transaction参数为null,所以这里创建的不是一个真正的事物
        return prepareTransactionStatus(
            definition, null, false, newSynchronization, debugEnabled, suspendedResources);
    }
    
	//3.新建事物，如果当前已经存在事物，则挂起当前事物。
    if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
        if (debugEnabled) {
            logger.debug("Suspending current transaction, creating new transaction with name [" +
                         definition.getName() + "]");
        }
        // 挂起已有事物
        SuspendedResourcesHolder suspendedResources = suspend(transaction);
        try {
            
            boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
            // 创建事物
            DefaultTransactionStatus status = newTransactionStatus(
                definition, transaction, true, newSynchronization, debugEnabled, suspendedResources);
            // 开启事物
            doBegin(transaction, definition);
            // 初始化事物同步属性
            prepareSynchronization(status, definition);
            
            return status;
        }
        catch (RuntimeException | Error beginEx) {
            resumeAfterBeginException(transaction, suspendedResources, beginEx);
            throw beginEx;
        }
    }
	// 4.如果当前存在事物，则在嵌套事物内执行；如果当前没有事物，则与PROPAGATION_REQUIRED传播特性相同
    // 嵌套事务--内层事务回滚与否与外层事务无关,外层事务回滚，也回滚内层事务
    if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
        if (!isNestedTransactionAllowed()) {
            throw new NestedTransactionNotSupportedException(
                "Transaction manager does not allow nested transactions by default - " +
                "specify 'nestedTransactionAllowed' property with value 'true'");
        }
        if (debugEnabled) {
            logger.debug("Creating nested transaction with name [" + definition.getName() + "]");
        }
        // useSavepointForNestedTransaction(),是否为嵌套事务使用保存点
        // 1.对于JtaTransactionManager-->返回false
        // 2.对于AbstractPlatformTransactionManager-->返回true
        if (useSavepointForNestedTransaction()) {
            // Create savepoint within existing Spring-managed transaction,
            // through the SavepointManager API implemented by TransactionStatus.
            // Usually uses JDBC 3.0 savepoints. Never activates Spring synchronization.
            // 创建保存点在现有spring管理事务,通过TransactionStatus SavepointManager API实现。
            // 通常使用JDBC 3.0保存点。永远不要激活Spring同步。
            
            DefaultTransactionStatus status =
                prepareTransactionStatus(definition, transaction, false, false, debugEnabled, null);
            // SAVEPOINT_ + 自增数字 作为保存事务节点
            status.createAndHoldSavepoint();
            return status;
        }
        else {
            // Nested transaction through nested begin and commit/rollback calls.
            // Usually only for JTA: Spring synchronization might get activated here
            // in case of a pre-existing JTA transaction.
            // 通过嵌套的开始,提交调用,及回滚调用进行嵌套事务。
            // 只对JTA有效,如果已经存在JTA事务，这里可能会激活Spring同步。
            boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
            DefaultTransactionStatus status = newTransactionStatus(
                definition, transaction, true, newSynchronization, debugEnabled, null);
            doBegin(transaction, definition);
            prepareSynchronization(status, definition);
            return status;
        }
    }

    // Assumably PROPAGATION_SUPPORTS or PROPAGATION_REQUIRED.
    // 处理PROPAGATION_SUPPORTS和PROPAGATION_REQUIRED两种传播特性
    // PROPAGATION_REQUIRED --> 如果当前没有事物，则新建一个事物；如果已经存在一个事物，则加入到这个事物中。
    // PROPAGATION_SUPPORTS --> 支持当前事物，如果当前没有事物，则以非事物方式执行。
    if (debugEnabled) {
        logger.debug("Participating in existing transaction");
    }
    
    // 该函数的作用是指定新事物参与已有事物时，新旧两个事物的验证级别。该属性值默认为false，宽松范围的验证，也就是不验证
    if (isValidateExistingTransaction()) {
        // 验证事物隔离级别
        // 如果当前事物的隔离级别不为默认隔离级别,则比较当前事物隔离级别与已有事物隔离级别,
        // 如不同,则抛出事物隔离级别不兼容异常
        if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
            Integer currentIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
            if (currentIsolationLevel == null || currentIsolationLevel != definition.getIsolationLevel()) {
                Constants isoConstants = DefaultTransactionDefinition.constants;
                throw new IllegalTransactionStateException("Participating transaction with definition [" +
                                                           definition + "] specifies isolation level which is incompatible with existing transaction: " +
                                                           (currentIsolationLevel != null ?
                                                            isoConstants.toCode(currentIsolationLevel, DefaultTransactionDefinition.PREFIX_ISOLATION) :
                                                            "(unknown)"));
            }
        }
        // 当前事务不是只读--存在的事务也必须不是只读
        if (!definition.isReadOnly()) {
            if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                throw new IllegalTransactionStateException("Participating transaction with definition [" +
                                                           definition + "] is not marked as read-only but existing transaction is");
            }
        }
    }
    boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
    return prepareTransactionStatus(definition, transaction, false, newSynchronization, debugEnabled, null);
}
```

###  事物挂起 

事物属性封装到SuspendedResourcesHolder对象中，该类就持有了被挂起事物的属性。接下来调用prepareTransactionStatus方法并将suspendedResources入参，这样一来，新创建的事物就持有了被挂起事物的的属性，就会形成一个事物链

```java
protected final SuspendedResourcesHolder suspend(@Nullable Object transaction) throws TransactionException {
    // 1.如果存在事物同步回调接口
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
        // 1.1 挂起事务同步回调接口
        List<TransactionSynchronization> suspendedSynchronizations = doSuspendSynchronization();
        try {
            // 挂起事物
            Object suspendedResources = null;
            if (transaction != null) {
                suspendedResources = doSuspend(transaction);
            }
            // 获取已有事物名称
            String name = TransactionSynchronizationManager.getCurrentTransactionName();
            // 清空已有事物名称
            TransactionSynchronizationManager.setCurrentTransactionName(null);
            // 获取已有事物的readOnly属性值
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            // 将已有事物的readOnly属性值设置为false
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
            // 获取已有事物数据库事物隔离级别
            Integer isolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
            // 清空已有事物数据库事物隔离级别
            TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(null);
            // 获取已有事物激活标识
            boolean wasActive = TransactionSynchronizationManager.isActualTransactionActive();
            // 将当前事物激活标识设置为false
            TransactionSynchronizationManager.setActualTransactionActive(false);
            // 返回SuspendedResourcesHolder
            /**
             * 将上面获取到的一系列事物属性,重新封装至SuspendedResourcesHolder对象,并返回
             */
            return new SuspendedResourcesHolder(suspendedResources, suspendedSynchronizations, name, readOnly, isolationLevel, wasActive);
        }
        catch (RuntimeException | Error ex) {
            // doSuspend failed - original transaction is still active...
            doResumeSynchronization(suspendedSynchronizations);
            throw ex;
        }
    }
    // 不存在事物同步回调接口,且当前事物不为空
    else if (transaction != null) {
        // Transaction active but no synchronization active.
        // 事物已经被激活,但是没有事物同步回调,则直接挂起当前事物即可
        Object suspendedResources = doSuspend(transaction);
        // 返回挂起的事物资源
        return new SuspendedResourcesHolder(suspendedResources);
    }
    // 处理没有事物的情况...
    else {
        // Neither transaction nor synchronization active.
        return null;
    }
}


private List<TransactionSynchronization> doSuspendSynchronization() {
    // 1.获取当前线程的所有事物同步回调
    List<TransactionSynchronization> suspendedSynchronizations = TransactionSynchronizationManager.getSynchronizations();
    // 2.循环并挂起所有同步回调接口
    for (TransactionSynchronization synchronization : suspendedSynchronizations) {
        synchronization.suspend();
    }
    // 3.清除资源
    TransactionSynchronizationManager.clearSynchronization();
    return suspendedSynchronizations;
}
```

### 回滚



```java

/**
	 * Handle a throwable, completing the transaction.
	 * We may commit or roll back, depending on the configuration.
	 * @param txInfo information about the current transaction
	 * @param ex throwable encountered
	 */
protected void completeTransactionAfterThrowing(@Nullable TransactionInfo txInfo, Throwable ex) {
    // 有事务属性也有事务的状态
    if (txInfo != null && txInfo.getTransactionStatus() != null) {
        if (logger.isTraceEnabled()) {
            logger.trace("Completing transaction for [" + txInfo.getJoinpointIdentification() +
                         "] after exception: " + ex);
        }
        // 1.回滚
        /**
         * txInfo.transactionAttribute.rollbackOn(ex)判断回滚的条件:
         *
         * 1. 如果自定了RollbackRuleAttribute列表,如果当前异常匹配到了RollbackRuleAttribute其中的条目,则回滚
         *    例如:可以通过rollbackFor指定触发回滚的异常@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
         *
         * 2. 否则如果异常是RuntimeException或者Error的类型,则回滚
         *
         * 3. 其他的异常是不会回滚的,这里要注意一下...
         */
        // rollbackFor属性 spring 就使用 RuleBasedTransactionAttribute
        // RuleBasedTransactionAttribute 的 rollbackOn 不符合 就运行下面的commit
        // 默认 DefaultTransactionAttribute 回滚 RuntimeException 和 Error
        // (ex instanceof RuntimeException || ex instanceof Error)
        if (txInfo.transactionAttribute != null && txInfo.transactionAttribute.rollbackOn(ex)) {
            try {
                // 执行回滚
                txInfo.getTransactionManager().rollback(txInfo.getTransactionStatus());
            }
            catch (TransactionSystemException ex2) {
                logger.error("Application exception overridden by rollback exception", ex);
                ex2.initApplicationException(ex);
                throw ex2;
            }
            catch (RuntimeException | Error ex2) {
                logger.error("Application exception overridden by rollback exception", ex);
                throw ex2;
            }
        }
        else {
            // We don't roll back on this exception.
            // Will still roll back if TransactionStatus.isRollbackOnly() is true.
            // 如果未能满足回滚条件,则有可能会提交事物,也有可能会回滚事物
        	// 注意:如果TransactionStatus.isRollbackOnly()为true,则仍然会执行回滚
            try {
                // 这里的commit方法并不一定会真正的提交事物，也有可能会回滚事物
                txInfo.getTransactionManager().commit(txInfo.getTransactionStatus());
            }
            catch (TransactionSystemException ex2) {
                logger.error("Application exception overridden by commit exception", ex);
                ex2.initApplicationException(ex);
                throw ex2;
            }
            catch (RuntimeException | Error ex2) {
                logger.error("Application exception overridden by commit exception", ex);
                throw ex2;
            }
        }
    }
}

// 执行回滚前检查事物状态
public final void rollback(TransactionStatus status) throws TransactionException {
    if (status.isCompleted()) {
        throw new IllegalTransactionStateException(
                "Transaction is already completed - do not call commit or rollback more than once per transaction");
    }
    DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;
    processRollback(defStatus, false);
}

// 执行回滚
private void processRollback(DefaultTransactionStatus status, boolean unexpected) {
    try {
        boolean unexpectedRollback = unexpected;
        try {
            // 1.事物回滚前调用事物同步接口
            // 注册了TransactionSynchronizationAdapter接口，那么在这里会分别执行我们实现了接口的方法，我们可以通过该接口来做一些额外的功能扩展
            triggerBeforeCompletion(status);
            // 2.如果有保存点,则回滚到保存点
            if (status.hasSavepoint()) {
                if (status.isDebug()) {
                    logger.debug("Rolling back transaction to savepoint");
                }
                // 回滚到事务保存点--使用的是jdbc连接用的是 JdbcTransactionObjectSupport
                status.rollbackToHeldSavepoint();
            }
            // 3.如果当前事物是一个新的事物,则调用doRollback执行给定事物的回滚
            else if (status.isNewTransaction()) {
                if (status.isDebug()) {
                    logger.debug("Initiating transaction rollback");
                }
                // 直接调用 Connection的 rollback();
                doRollback(status);
            }
            else {
                // Participating in larger transaction
// 4.如果当前事物并非独立事物,则将当前事物的rollbackOnly属性标记为true,等到事物链完成之后,一起执行回滚
// 如果当前存在事物,但是事物的rollbackOnly属性已经被标记为true
// 或者globalRollbackOnParticipationFailure(返回是否仅在参与事务失败后才将现有事务全局标记为回滚)为true
                if (status.hasTransaction()) {
                    if (status.isLocalRollbackOnly() || isGlobalRollbackOnParticipationFailure()) {
                        if (status.isDebug()) {
                            logger.debug("Participating transaction failed - marking existing transaction as rollback-only");
                        }
                        // 则将ConnectionHolder中的rollbackOnly标记为true
                        // 如果当前事务不是独立的事务，那么只能标记状态， 等到事务连执行完毕后统一回滚 
                        doSetRollbackOnly(status);
                    }
                    else {
                        if (status.isDebug()) {
                            logger.debug("Participating transaction failed - letting transaction originator decide on rollback");
                        }
                    }
                }
                // 5.如果当前不存在事物,则不执行任何操作
                else {
                    logger.debug("Should roll back transaction but cannot - no transaction available");
                }
                // Unexpected rollback only matters here if we're asked to fail early
                if (!isFailEarlyOnGlobalRollbackOnly()) {
                    unexpectedRollback = false;
                }
            }
        }
        catch (RuntimeException | Error ex) {
            triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
            throw ex;
        }
        // 6.事物回滚后调用事物同步接口
        // 注册了TransactionSynchronizationAdapter接口，那么在这里会分别执行我们实现了接口的方法，我们可以通过该接口来做一些额外的功能扩展
        triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
        // Raise UnexpectedRollbackException if we had a global rollback-only marker
        if (unexpectedRollback) {
            throw new UnexpectedRollbackException("Transaction rolled back because it has been marked as rollback-only");
        }
    }
    finally {
        // 7.事物完成后清理资源
        cleanupAfterCompletion(status);
    }
}

// 直接调用 Connection的 rollback();
// doRollback(status);
@Override
protected void doRollback(DefaultTransactionStatus status) {
    DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
    Connection con = txObject.getConnectionHolder().getConnection();
    if (status.isDebug()) {
        logger.debug("Rolling back JDBC transaction on Connection [" + con + "]");
    }
    try {
        con.rollback();
    }
    catch (SQLException ex) {
        throw new TransactionSystemException("Could not roll back JDBC transaction", ex);
    }
}
  // 7.事物完成后清理资源
private void cleanupAfterCompletion(DefaultTransactionStatus status) {
    // 1.将当前事物状态标记为已完成
    status.setCompleted();
    // 2.清除synchronization
    if (status.isNewSynchronization()) {
        TransactionSynchronizationManager.clear();
    }
    // 3.事务完成后清理资源。
    if (status.isNewTransaction()) {
        doCleanupAfterCompletion(status.getTransaction());
    }
    // 4.从嵌套事物中恢复被挂起的资源
    if (status.getSuspendedResources() != null) {
        if (status.isDebug()) {
            logger.debug("Resuming suspended transaction after completion of inner transaction");
        }
        Object transaction = (status.hasTransaction() ? status.getTransaction() : null);
        resume(transaction, (SuspendedResourcesHolder) status.getSuspendedResources());
    }
}
// 事务完成后清理资源。
protected void doCleanupAfterCompletion(Object transaction) {
    DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;

    // Remove the connection holder from the thread, if exposed.
    // 解绑ConnectionHolder
    if (txObject.isNewConnectionHolder()) {
        TransactionSynchronizationManager.unbindResource(obtainDataSource());
    }

    // Reset connection.
    // 重置连接
    Connection con = txObject.getConnectionHolder().getConnection();
    try {
        if (txObject.isMustRestoreAutoCommit()) {
            con.setAutoCommit(true);
        }
        DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
    }
    catch (Throwable ex) {
        logger.debug("Could not reset JDBC Connection after transaction", ex);
    }

    // 释放连接
    if (txObject.isNewConnectionHolder()) {
        if (logger.isDebugEnabled()) {
            logger.debug("Releasing JDBC Connection [" + con + "] after transaction");
        }
        DataSourceUtils.releaseConnection(con, this.dataSource);
    }

    txObject.getConnectionHolder().clear();
}
```

1. 事物完成前、完成后触发器的调用，例如：我们在上一节的业务方法里注册了TransactionSynchronizationAdapter接口，那么在这里会分别执行我们实现了接口的方法，我们可以通过该接口来做一些额外的功能扩展。
2. 如果有保存点,则调用rollbackToHeldSavepoint回滚到保存点（后面嵌套事物章节分析）
3. 如果当前事物是一个新的事物,则调用doRollback执行给定事物的回滚
4. 如果当前事物并非独立事物,则将当前事物的rollbackOnly属性标记为true,等到事物链完成之后,一起执行回滚
   大概的流程就是这样了，因为我们分析的是单service下的单个业务方法调用，所以这里我们还是只分析最简单的doRollback正常回滚调用（后面嵌套事物章节分析）

### 提交

```java
public final void commit(TransactionStatus status) throws TransactionException {
    // 如果当前事物已经被标记为完成,抛出异常
    if (status.isCompleted()) {
        throw new IllegalTransactionStateException(
                "Transaction is already completed - do not call commit or rollback more than once per transaction");
    }

    DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;

    // 如果rollbackOnly为true,则回滚
    if (defStatus.isLocalRollbackOnly()) {
        if (defStatus.isDebug()) {
            logger.debug("Transactional code has requested rollback");
        }
        processRollback(defStatus, false);
        return;
    }

    // shouldCommitOnGlobalRollbackOnly --> 返回是否对标记为仅以全局方式回滚的事务调用
    // defStatus.isGlobalRollbackOnly() --> 实现了SmartTransactionObject并且事物的rollbackOnly被标记为true
    if (!shouldCommitOnGlobalRollbackOnly() && defStatus.isGlobalRollbackOnly()) {
        if (defStatus.isDebug()) {
            logger.debug("Global transaction is marked as rollback-only but transactional code requested commit");
        }
        processRollback(defStatus, true);
        return;
    }

    // 提交事物
    processCommit(defStatus);
}



private void processCommit(DefaultTransactionStatus status) throws TransactionException {
    try {
        boolean beforeCompletionInvoked = false;

        try {
            boolean unexpectedRollback = false;
            // 空方法
            prepareForCommit(status);
            // 之前注册的 TransactionSynchronizationAdapter.beforeCommit
            triggerBeforeCommit(status);
            // 之前注册的 TransactionSynchronizationAdapter.beforeCompletion
            triggerBeforeCompletion(status);
            
            beforeCompletionInvoked = true;
			// 有保存点
            if (status.hasSavepoint()) {
                if (status.isDebug()) {
                    logger.debug("Releasing transaction savepoint");
                }
                unexpectedRollback = status.isGlobalRollbackOnly();
                //如果当前status有保存点，表示当前提交的是嵌套在某个事务
            	//内的子事务，通过释放保存点提交--嵌套事务不影响外事务
                //在外事务提交--外事务回滚内事务也回滚，外事务提交内事务也提交
                status.releaseHeldSavepoint();
            }
            else if (status.isNewTransaction()) {
                if (status.isDebug()) {
                    logger.debug("Initiating transaction commit");
                }
                unexpectedRollback = status.isGlobalRollbackOnly();
                // 直接Connection.commit()
                doCommit(status);
            }
            else if (isFailEarlyOnGlobalRollbackOnly()) {
                unexpectedRollback = status.isGlobalRollbackOnly();
            }

            // Throw UnexpectedRollbackException if we have a global rollback-only
            // marker but still didn't get a corresponding exception from commit.
            if (unexpectedRollback) {
                throw new UnexpectedRollbackException(
                    "Transaction silently rolled back because it has been marked as rollback-only");
            }
        }
        catch (UnexpectedRollbackException ex) {
            // can only be caused by doCommit
            triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
            throw ex;
        }
        catch (TransactionException ex) {
            // can only be caused by doCommit
            if (isRollbackOnCommitFailure()) {
                doRollbackOnCommitException(status, ex);
            }
            else {
                triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
            }
            throw ex;
        }
        catch (RuntimeException | Error ex) {
            if (!beforeCompletionInvoked) {
                triggerBeforeCompletion(status);
            }
            doRollbackOnCommitException(status, ex);
            throw ex;
        }

        // Trigger afterCommit callbacks, with an exception thrown there
        // propagated to callers but the transaction still considered as committed.
        try {
            // 之前注册的 TransactionSynchronizationAdapter.afterCommit
            triggerAfterCommit(status);
        }
        finally {
            // 之前注册的 TransactionSynchronizationAdapter.afterCompletion
            triggerAfterCompletion(status, TransactionSynchronization.STATUS_COMMITTED);
        }

    }
    finally {
        // 清除当前连接
        cleanupAfterCompletion(status);
    }
}

private void cleanupAfterCompletion(DefaultTransactionStatus status) {
    // 1.将当前事物状态标记为已完成
    status.setCompleted();
    // 2.清除synchronization
    if (status.isNewSynchronization()) {
        TransactionSynchronizationManager.clear();
    }
    // 3.事务完成后清理资源。
    if (status.isNewTransaction()) {
        doCleanupAfterCompletion(status.getTransaction());
    }
    // 4.从嵌套事物中恢复被挂起的资源
    if (status.getSuspendedResources() != null) {
        if (status.isDebug()) {
            logger.debug("Resuming suspended transaction after completion of inner transaction");
        }
        Object transaction = (status.hasTransaction() ? status.getTransaction() : null);
        resume(transaction, (SuspendedResourcesHolder) status.getSuspendedResources());
    }
}

```

### 回滚和提交后的清除操作：cleanupAfterCompletion

```java
/**
	 * Clean up after completion, clearing synchronization if necessary,
	 * and invoking doCleanupAfterCompletion.
	 * @param status object representing the transaction
	 * @see #doCleanupAfterCompletion
	 */
private void cleanupAfterCompletion(DefaultTransactionStatus status) {
    // 1.将当前事物状态标记为已完成
    status.setCompleted();
    // 2.清除synchronization
    if (status.isNewSynchronization()) {
        TransactionSynchronizationManager.clear();
    }
    // 3.事务完成后清理资源。
    if (status.isNewTransaction()) {
        doCleanupAfterCompletion(status.getTransaction());
    }
    // 4.从嵌套事物中恢复被挂起的资源
    if (status.getSuspendedResources() != null) {
        if (status.isDebug()) {
            logger.debug("Resuming suspended transaction after completion of inner transaction");
        }
        Object transaction = (status.hasTransaction() ? status.getTransaction() : null);
        resume(transaction, (SuspendedResourcesHolder) status.getSuspendedResources());
    }
}


// doCleanupAfterCompletion(status.getTransaction());
@Override
protected void doCleanupAfterCompletion(Object transaction) {
    // 数据源事务对象
    DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;

    // Remove the connection holder from the thread, if exposed.
    // 新事务
    if (txObject.isNewConnectionHolder()) {
        // 将当前连接从当前连接中解除绑定
        TransactionSynchronizationManager.unbindResource(obtainDataSource());
    }

    // Reset connection. 释放连接
    Connection con = txObject.getConnectionHolder().getConnection();
    try {
        // 将原来的自动提交设置回去
        if (txObject.isMustRestoreAutoCommit()) {
            con.setAutoCommit(true);
        }
        //重置数据库连接--关于连接的只读属性和事务隔离级别的重新设置
        DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel());
    }
    catch (Throwable ex) {
        logger.debug("Could not reset JDBC Connection after transaction", ex);
    }

    if (txObject.isNewConnectionHolder()) {
        if (logger.isDebugEnabled()) {
            logger.debug("Releasing JDBC Connection [" + con + "] after transaction");
        }
        //当前线程的数据库连接释放--一般close
        DataSourceUtils.releaseConnection(con, this.dataSource);
    }

    txObject.getConnectionHolder().clear();
}

protected final void resume(@Nullable Object transaction, @Nullable SuspendedResourcesHolder resourcesHolder)
			throws TransactionException {
	// 如果之前的事务被挂起就恢复之前的事务
    if (resourcesHolder != null) {
        Object suspendedResources = resourcesHolder.suspendedResources;
        if (suspendedResources != null) {
            doResume(transaction, suspendedResources);
        }
        List<TransactionSynchronization> suspendedSynchronizations = resourcesHolder.suspendedSynchronizations;
        if (suspendedSynchronizations != null) {
            TransactionSynchronizationManager.setActualTransactionActive(resourcesHolder.wasActive);
            TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(resourcesHolder.isolationLevel);
            TransactionSynchronizationManager.setCurrentTransactionReadOnly(resourcesHolder.readOnly);
            TransactionSynchronizationManager.setCurrentTransactionName(resourcesHolder.name);
            doResumeSynchronization(suspendedSynchronizations);
        }
    }
}


```

