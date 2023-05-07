### FastThreadLocal 

如果当前线程不是FastThreadLocalThread ，就不要用 FastThreadLocal 了

因为如果是普通线程，性能没有很好的提升

FastThreadLocal  先对与 ThreadLocal  主要的优化是

将ThreadLocal 的ThreadLocalMap由一个map变成InternalThreadLocalMap一个数组



FastThreadLocal和数组下标一一对应： 主要因为 FastThreadLocal在 JVM 里有一个唯一的index







```java
//demo
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.FastThreadLocalThread;

public class FastThreadLocalUtil {
    // netty 创建FastThreadLocalThread线程的工具类
    final static DefaultThreadFactory DEFAULT_THREAD_FACTORY = new DefaultThreadFactory("thread");

    final static FastThreadLocal<Admin> threadLocal = new FastThreadLocal<>() {
        @Override
        protected Admin initialValue() throws Exception {
            return new Admin(33,"qinjp");
        }
    };
    
    public static void main(String[] args) {
        // 使用FastThreadLocal 当前线程就必须是 FastThreadLocalThread
        FastThreadLocalThread thread = (FastThreadLocalThread) DEFAULT_THREAD_FACTORY.newThread(() -> {
            final User admin = threadLocal.get();
            System.out.println(admin);
        });

        thread.start();
    }

}


//关键代码
public class FastThreadLocal<V> {
    // 当前FastThreadLocal在jvm里的唯一标识（一个自增下标）
    // 通过当前线程的一个数组 InternalThreadLocalMap.indexedVariables  
    // FastThreadLocal和数组下标一一对应，通过这个index就可以拿到对应的值
    private final int index;
	
    //
    public FastThreadLocal() {
        // 创建FastThreadLocal就确定index唯一值
        index = InternalThreadLocalMap.nextVariableIndex();
    }
    
    public final V get() {
        // 拿到当前线程对应的InternalThreadLocalMap
        // FastThread获取方式和普通线程获取方式，
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
        // 通过index在threadLocalMap（FastThreadLocal和数组下标一一对应的数据）获取值
        Object v = threadLocalMap.indexedVariable(index);
        if (v != InternalThreadLocalMap.UNSET) {
            //非空直接获取
            return (V) v;
        }
		//空就初始化
        return initialize(threadLocalMap);
    }

    //和get区别就是不初始化
    public final V getIfExists() {
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
        if (threadLocalMap != null) {
            Object v = threadLocalMap.indexedVariable(index);
            if (v != InternalThreadLocalMap.UNSET) {
                return (V) v;
            }
        }
        return null;
    }
    
    //获取jvm里有多少个FastThreadLocal
    public static int size() {
        //直接拿数组的
        InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.getIfSet();
        if (threadLocalMap == null) {
            return 0;
        } else {
            return threadLocalMap.size();
        }
    }
    
    //设置值
    public final void set(V value) {
        //UNSET对象不能删
        if (value != InternalThreadLocalMap.UNSET) {
            //获取当前线程的InternalThreadLocalMap
            InternalThreadLocalMap threadLocalMap = InternalThreadLocalMap.get();
            //设置值，threadLocalMap数组设置值
            setKnownNotUnset(threadLocalMap, value);
        } else {
            //调用用户自定义的onRemove方法
            remove();
        }
    }
    	
    private void setKnownNotUnset(InternalThreadLocalMap threadLocalMap, V value) {
        //threadLocalMap数组设置值
        if (threadLocalMap.setIndexedVariable(index, value)) {
            //这个是为后续删除设置的，不是重点
            addToVariablesToRemove(threadLocalMap, this);
        }
    }
    
}

//InternalThreadLocalMap: 里面装这一个数组，数组下标代表是哪个FastThreadLocal，对应的值就是用户设置的值
//InternalThreadLocalMap和FastThreadLocalThread线程绑定的，每一个FastThreadLocalThread线程一个InternalThreadLocalMap
public final class InternalThreadLocalMap extends UnpaddedInternalThreadLocalMap {
    //数据存放的地方
    private Object[] indexedVariables
    
    //InternalThreadLocalMap初始化
    private InternalThreadLocalMap() {
        indexedVariables = newIndexedVariableTable();
    }
	//初始化的时候创建一个32容量的Object[]数组，这个数据默认值是 UNSET
    private static Object[] newIndexedVariableTable() {
        Object[] array = new Object[INDEXED_VARIABLE_TABLE_INITIAL_SIZE];
        Arrays.fill(array, UNSET);
        return array;
    }
    
	//拿到一个index当FastThreadLocal在jvm里唯一索引
	public static int nextVariableIndex() {
        int index = nextIndex.getAndIncrement();
        if (index >= ARRAY_LIST_CAPACITY_MAX_SIZE || index < 0) {
            nextIndex.set(ARRAY_LIST_CAPACITY_MAX_SIZE);
            throw new IllegalStateException("too many thread-local indexed variables");
        }
        return index;
    }


    //拿到当前线程对应的InternalThreadLocalMap
    public static InternalThreadLocalMap get() {
        //拿到当前线程
        Thread thread = Thread.currentThread();
        if (thread instanceof FastThreadLocalThread) {
            //FastThread获取方式
            return fastGet((FastThreadLocalThread) thread);
        } else {
            //普通线程获取方式
            return slowGet();
        }
    }

    //FastThread获取方式
    //直接在当前线程获取InternalThreadLocalMap
    private static InternalThreadLocalMap fastGet(FastThreadLocalThread thread) {
        InternalThreadLocalMap threadLocalMap = thread.threadLocalMap();
        if (threadLocalMap == null) {
            thread.setThreadLocalMap(threadLocalMap = new InternalThreadLocalMap());
        }
        return threadLocalMap;
    }
    //普通线程获取方式
    //通过JDK的ThreadLocal获取InternalThreadLocalMap
    private static InternalThreadLocalMap slowGet() {
        InternalThreadLocalMap ret = slowThreadLocalMap.get();
        if (ret == null) {
            ret = new InternalThreadLocalMap();
            slowThreadLocalMap.set(ret);
        }
        return ret;
    }
    //通过下标获取数据数组的值
    public Object indexedVariable(int index) {
        Object[] lookup = indexedVariables;
        return index < lookup.length? lookup[index] : UNSET;
    }
    
    //删除对应数组下标的值：就是将原来的值设置成UNSET
    public Object removeIndexedVariable(int index) {
        Object[] lookup = indexedVariables;
        if (index < lookup.length) {
            Object v = lookup[index];
            lookup[index] = UNSET;
            return v;
        } else {
            return UNSET;
        }
    }

}


/**
 * FastThreadLocalThread 
 */
public class FastThreadLocalThread extends Thread {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(FastThreadLocalThread.class);

    // This will be set to true if we have a chance to wrap the Runnable.
    private final boolean cleanupFastThreadLocals;
	// FastThreadLocalThread线程对应内部的InternalThreadLocalMap
    private InternalThreadLocalMap threadLocalMap;

    public FastThreadLocalThread() {
        cleanupFastThreadLocals = false;
    }

    public FastThreadLocalThread(Runnable target) {
        super(FastThreadLocalRunnable.wrap(target));
        cleanupFastThreadLocals = true;
    }

    public FastThreadLocalThread(ThreadGroup group, Runnable target) {
        super(group, FastThreadLocalRunnable.wrap(target));
        cleanupFastThreadLocals = true;
    }

    public FastThreadLocalThread(String name) {
        super(name);
        cleanupFastThreadLocals = false;
    }

    public FastThreadLocalThread(ThreadGroup group, String name) {
        super(group, name);
        cleanupFastThreadLocals = false;
    }

    public FastThreadLocalThread(Runnable target, String name) {
        super(FastThreadLocalRunnable.wrap(target), name);
        cleanupFastThreadLocals = true;
    }

    public FastThreadLocalThread(ThreadGroup group, Runnable target, String name) {
        super(group, FastThreadLocalRunnable.wrap(target), name);
        cleanupFastThreadLocals = true;
    }

    public FastThreadLocalThread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, FastThreadLocalRunnable.wrap(target), name, stackSize);
        cleanupFastThreadLocals = true;
    }

    /**
     * Returns the internal data structure that keeps the thread-local variables bound to this thread.
     * Note that this method is for internal use only, and thus is subject to change at any time.
     */
    public final InternalThreadLocalMap threadLocalMap() {
        if (this != Thread.currentThread() && logger.isWarnEnabled()) {
            logger.warn(new RuntimeException("It's not thread-safe to get 'threadLocalMap' " +
                    "which doesn't belong to the caller thread"));
        }
        return threadLocalMap;
    }

    /**
     * Sets the internal data structure that keeps the thread-local variables bound to this thread.
     * Note that this method is for internal use only, and thus is subject to change at any time.
     */
    public final void setThreadLocalMap(InternalThreadLocalMap threadLocalMap) {
        if (this != Thread.currentThread() && logger.isWarnEnabled()) {
            logger.warn(new RuntimeException("It's not thread-safe to set 'threadLocalMap' " +
                    "which doesn't belong to the caller thread"));
        }
        this.threadLocalMap = threadLocalMap;
    }

    /**
     * Returns {@code true} if {@link FastThreadLocal#removeAll()} will be called once {@link #run()} completes.
     */
    @UnstableApi
    public boolean willCleanupFastThreadLocals() {
        return cleanupFastThreadLocals;
    }

    /**
     * Returns {@code true} if {@link FastThreadLocal#removeAll()} will be called once {@link Thread#run()} completes.
     */
    @UnstableApi
    public static boolean willCleanupFastThreadLocals(Thread thread) {
        return thread instanceof FastThreadLocalThread &&
                ((FastThreadLocalThread) thread).willCleanupFastThreadLocals();
    }

    /**
     * Query whether this thread is allowed to perform blocking calls or not.
     * {@link FastThreadLocalThread}s are often used in event-loops, where blocking calls are forbidden in order to
     * prevent event-loop stalls, so this method returns {@code false} by default.
     * <p>
     * Subclasses of {@link FastThreadLocalThread} can override this method if they are not meant to be used for
     * running event-loops.
     *
     * @return {@code false}, unless overriden by a subclass.
     */
    public boolean permitBlockingCalls() {
        return false;
    }
}


```



























