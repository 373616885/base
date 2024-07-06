### java 8 之前finalizer机制

```java
package com.qin.demo.direct;

public class Member {
    public Member() {
        System.out.println("诞生！");
    }

    @Override
    protected void finalize() throws Throwable {//从JDk1.9开始不建议使用
        System.out.println("回收：必死");
        throw new Exception("不想死");
    }
}

```



Finalizer机制是除了不可预知外还有一个缺点：

Finalizer机制线程的运行优先级低于其他应用程序线程，导致对象被回收的速度远低于进入队列的速度

为了防止这类问题 Cleaner机制 出现了

从java 9开始，Finalizer机制已被弃用

java 9中 Cleaner机制代替了Finalizer机制

Cleaner机制不如Finalizer机制那样危险，但仍然是不可预测，运行缓慢并且通常是不必要的

因为Java类的创建者可以控制自己cleaner机制的线程，但cleaner机制仍然在后台运行，在垃圾回收器的控制下运行，但不能保证及时清理。





<u>**finalizer和cleaner机制都会导致严重的性能损失，使用要避免使用**</u>



### Java9提供的资源回收机制：java.lang.ref.Cleaner

Cleaner 在 java.lang.ref 包下的不是 jdk.internal.ref



java 9 开始不建议使用finialize()方法，从而建议使用AutoCloseable或者Cleaner进行处理

```java
public class Member implements Runnable{
    public Member(){
        System.out.println("【构造】Born");
    }

    @Override
    public void run() {     // 执行清除的时候，执行的是此操作
        System.out.println("【回收】Death--GC回收安全机制--最后的倔强");
    }
}
```



```java
import java.lang.ref.Cleaner;

public class MemberCleaning implements AutoCloseable{    // 实现清除
    private static final Cleaner cleaner = Cleaner.create();    // 创建清除处理
    private  Member member;//需要回收的资源，可以搞成内部类
    private Cleaner.Cleanable cleanable;

    public MemberCleaning() {
        this.member = new Member();
        this.cleanable = this.cleaner.register(this,this.member);    // 注册使用的对象
    }

    @Override
    public void close() throws Exception {
        //必须是 cleanable 去调用，使用cleanable才可以保证调用一次
        this.cleanable.clean(); // 启动多线程
    }
}
```



```java
public class Main {
    public static void main(String[] args) throws Exception {
        new MemberCleaning();//GC 回收的时候，
        System.gc();
        Thread.sleep(1000);
        System.out.println("Peace out");
        // ----------------------------------------
        MemberCleaning memberCleaning = new MemberCleaning();//GC 回收的时候，
		//调用了 close() GC 回收的时候，就不调用了
        memberCleaning.close();
        memberCleaning = null;
        System.gc();
        Thread.sleep(1000);
        System.out.println("Peace out");
        // ----------------------------------------
		// 情况 3
        try (MemberCleaning myRoom = new MemberCleaning()) {
            System.out.println("Peace out");
        }
    }
}
```





### Effective Java 经典例子

一个简单的`Room`类。假设`Room`对象必须在被回收前清理干净

静态内部`State`类拥有Cleaner机制清理房间所需的资源。 

在这里，它仅仅包含`numJunkPiles`属性，它代表混乱房间的数量。

```java
// An autocloseable class using a cleaner as a safety net
public class Room implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();

    // Resource that requires cleaning. Must not refer to Room!
    // 内部类需要 Cleaning room，其实就是 run 任务，numJunkPiles 就是外部传递的参数，在run里运行
    private static class State implements Runnable {
        int numJunkPiles; // Number of junk piles in this room

        State(int numJunkPiles) {
            this.numJunkPiles = numJunkPiles;
        }

        // Invoked by close method or cleaner
        @Override
        public void run() {
            System.out.println("Cleaning room");
            numJunkPiles = 0;
        }
    }

    // The state of this room, shared with our cleanable
    // cleanable : run 任务
    // `State`必须是静态的嵌内部类
    // 一个`State`实例不引用它的`Room`实例是非常重要的。
    // 如果它引用了，阻止了`Room`实例成为垃圾收集
    // 使用lambda表达式也是不明智的，因为它们很容易获取对宿主类对象的引用
    private final State state;

    // Our cleanable. Cleans the room when it’s eligible for gc
    private final Cleaner.Cleanable cleanable;

    public Room(int numJunkPiles) {
        state = new State(numJunkPiles);
        // 注册任务
        cleanable = cleaner.register(this, state);
    }
	
    // 如果在Room实例有资格进行垃圾回收的时候客户端没有调用close方法，
    // 那么Cleaner机制将（保证）调用State的run方法。
    // 其run方法最多只能调用一次，只能被我们在Room构造方法中用Cleaner机制注册State实例时得到的Cleanable调用
    // 如果这里外面调用了 close() , 下次GC回收就不会调用
    // Cleaner机制是（保证）调用State的run方法
    @Override
    public void close() {
        cleanable.clean();
    }
}

```



一个`State`实例不引用它的`Room`实例是非常重要的。如果它引用了，则创建了一个循环，阻止了`Room`实例成为垃圾收集的资格(以及自动清除)。

因此，`State`必须是静态的嵌内部类，因为非静态内部类包含对其宿主类的实例的引用(条目 24)。同样，使用lambda表达式也是不明智的，因为它们很容易获取对宿主类对象的引用。

行为良好的客户端如下所示：

```java
public class Adult {
    public static void main(String[] args) {
        try (Room myRoom = new Room(7)) {
            System.out.println("Goodbye");
        }
    }
}

```



不合规矩的程序：

```java
public class Teenager {
    public static void main(String[] args) {
        new Room(99);
        // Cleaner机制是（保证）调用State的run方法
        System.out.println("Peace out");
    }
}

```





<u>**总之，除了作为一个安全网或者终止非关键的本地资源，不要使用Cleaner机制，或者是在Java 9发布之前的finalizers机制。即使是这样，也要当心不确定性和性能影响。**</u>







### jdk.internal.ref.Cleaner

NIO 中创建 Cleaner

```java
cleaner = Cleaner.create(this, new Deallocator(base, size, cap));
```

`Deallocator`实现了`Runnable`接口，也就是说其实这个类就是自定义的一个线程类。可以看到在`run()`方法里面通过`unsafe.freeMemory(address); `真正实现内存的回收。其实这里就是构建好了回收堆外内存的工具。

```java
private static class Deallocator implements Runnable {

    private static Unsafe unsafe = Unsafe.getUnsafe();

    private long address;   //内存地址
    private long size;    //大小
    private int capacity;   //申请的容量

    private Deallocator(long address, long size, int capacity) {
        assert (address != 0);
        this.address = address;
        this.size = size;
        this.capacity = capacity;
    }

    public void run() {   //具体的清理操作
        if (address == 0) {
            // Paranoia
            return;
        }
        unsafe.freeMemory(address);   //这里是直接调用了Unsafe进行内存释放操作
        address = 0;   //内存地址改为0，NULL
        Bits.unreserveMemory(size, capacity);   //取消一开始的保留内存
    }
}

```

`Cleaner`继承了`PhantomReference`，`Cleaner`维护了一个全局唯一的双向链表，用静态成员`first`指向该链表：

```java
private static Cleaner first = null;
```

整个JVM运行过程中产生的所有`Cleaner`都会添加到这个链表当中，`Cleaner`通过`next`寻找到下一个`Cleaner`，通过`prev`寻找到上一个`Cleaner`。

```java
private Cleaner next = null, prev = null;
```

通过如下方法创建`Cleaner`：

```java
// dummyQueue在Cleaner没有起到任何作用，ReferenceHandler不会将Cleaner对象加入到dummyQueue当中，
// 查看ReferenceHandler线程的run方法可以发现当遇到的Reference对象的实际类型是Cleaner时，
// 只是简单的调用其clean方法，定义dummyQueue的原因仅仅因为其父类PhantomReference的
// 构造方法中需要传入一个queue
public class Cleaner extends PhantomReference<Object>{ // 继承自鬼引用，也就是说此对象会存放一个没有任何引用的对象

    // 引用队列，PhantomReference构造方法需要
    private static final ReferenceQueue<Object> dummyQueue = new ReferenceQueue<>();
  	
  	// 执行清理的具体流程
    private final Runnable thunk;
  
    // Cleaner双向链表，每创建一个Cleaner对象都会添加一个结点
  	static private Cleaner first = null; 

    private Cleaner
        next = null,
        prev = null;
    
  	// 添加操作会让新来的变成新的头结点
  	private static synchronized Cleaner add(Cleaner cl) {   
        if (first != null) {
            cl.next = first;
            first.prev = cl;
        }
        first = cl;
        return cl;
    }
    
    private static synchronized boolean remove(Cleaner cl) {

        // If already removed, do nothing
        if (cl.next == cl)
             return false;

        // Update list
        // cl的链表的第一个元素的情况下需要更改first的指向
        if (first == cl) {
             if (cl.next != null)
                 first = cl.next;
             else
                 first = cl.prev;
        }
        // cl后一个元素的prev指向cl中prev指向的元素，cl前一个元素的next指向cl的后一个元素
        if (cl.next != null)
            cl.next.prev = cl.prev;
        if (cl.prev != null)
            cl.prev.next = cl.next;

        // Indicate removal by pointing the cleaner to itself
        // cl的next和prev都指向自己时表示cl已经从链表中移除了
        cl.next = cl;
        cl.prev = cl;
        return true;
    }
    

  	// 可以看到创建鬼引用的对象就是传进的缓冲区对象
    private Cleaner(Object referent, Runnable thunk) {
        super(referent, dummyQueue);
      	// 清理流程实际上是外面的Deallocator
        this.thunk = thunk;
    }

   	// 通过此方法创建一个新的Cleaner
    public static Cleaner create(Object ob, Runnable thunk) {
        if (thunk == null)
            return null;
        return add(new Cleaner(ob, thunk));   // 调用add方法将Cleaner添加到队列
    }
  
  	// 清理操作
  	public void clean() {
        if (!remove(this))
            return;    // 进行清理操作时会从双向队列中移除当前Cleaner，false说明已经移除过了，直接return
        try {
            thunk.run();   // 这里就是直接执行具体清理流程
        } catch (final Throwable x) {
            AccessController.doPrivileged(new PrivilegedAction<>() {
                    public Void run() {
                        if (System.err != null)
                            new Error("Cleaner terminated abnormally", x)
                                .printStackTrace();
                        System.exit(1);
                        return null;
                    }});
        }
    }


```

`Cleaner`的清理资源方法：

```java
private static synchronized boolean remove(Cleaner cl) {

    // If already removed, do nothing
    if (cl.next == cl)
         return false;

    // Update list
    // cl的链表的第一个元素的情况下需要更改first的指向
    if (first == cl) {
         if (cl.next != null)
             first = cl.next;
         else
             first = cl.prev;
    }
    // cl后一个元素的prev指向cl中prev指向的元素，cl前一个元素的next指向cl的后一个元素
    if (cl.next != null)
        cl.next.prev = cl.prev;
    if (cl.prev != null)
        cl.prev.next = cl.next;

    // Indicate removal by pointing the cleaner to itself
    // cl的next和prev都指向自己时表示cl已经从链表中移除了
    cl.next = cl;
    cl.prev = cl;
    return true;
}

// 该方法
public void clean() {
	 // 将cleaner从链表中取下，防止重复执行清除操作
     if (!remove(this))
         return;
     try {
     	 // thunk的run方法里执行自定义的资源清理操作
         thunk.run();
     } catch (final Throwable x) {
         AccessController.doPrivileged(new PrivilegedAction<>() {
                 public Void run() {
                     if (System.err != null)
                         new Error("Cleaner terminated abnormally", x)
                                .printStackTrace();
                     System.exit(1);
                     return null;
                 }});
     }
}

```





### NIO什么时候使用Cleaner？

主要是通过虚引用自动回收机制来实现方法的调用的，上面的源码中Cleaner继承了PhantomReference，

然后虚引用里面有一个虚引用队列，将Cleaner对象加入到其中，然后下面是Reference类的一个静态代码块，

里面创建了ReferenceHandler线程对象，这个线程是一个守护线程，并且从一开始就以最高的优先级运行，

它的一部分工作就是监控和管理虚引用队列中的对象。

简单理解：

**那么我们可以这样去理解，守护线程相当于管理员，而虚引用队列相当于一个容器，里面存放着Cleaner对象和其他对象，这个管理员不断扫描、监控，如果对象被GC回收机制回收，那么管理员就会在GC回收这个对象之前判断它的类型，如果是Cleaner及其子类对象，那么就需要调用Cleaner对象的Clean()方法**。

```java
private static class ReferenceHandler extends Thread {
  ...
	static {
            // 预加载并初始化 InterruptedException 和 Cleaner 类
        	// 以避免出现在循环运行过程中时由于内存不足而无法加载
            ensureClassInitialized(InterruptedException.class);
            ensureClassInitialized(Cleaner.class);
    }
		
    public void run() {
        while (true) {
            // 这里是一个无限循环调用tryHandlePending方法
            tryHandlePending(true);  
        }
    }
}

```

下面是tryHandlePending()方法的源码，这个方法其实是Reference类的一个静态方法，下面逻辑中只需要注意三行代码，就是加了#的代码。

第一行声明了一个Cleaner类型的变量c；

第二行判断r（将要被回收的对象）是不是Cleaner及其子类，如果是就赋值给c，如果不是就赋值为null；

第三行判断c是否为空，如果为null，说明该对象不算Cleaner及其子类，如果不为null，说明该对象是Cleaner及其子类，那么就调用clean()方法。

```java
static boolean tryHandlePending(boolean waitForNotify) {
    Reference<Object> r;
    Cleaner c; // ###########################
    try {
        synchronized (lock) {   // 加锁
          	// 当Cleaner引用的DirectByteBuffer对象即将被回收时，pending会变成此Cleaner对象
          	// 这里判断到pending不为null时就需要处理一下对象销毁了
            if (pending != null) {
                r = pending;
                // 'instanceof' 有时会导致内存溢出，所以将r从链表中移除之前就进行类型判断
                // 如果是Cleaner类型就给到c  ##########################
                c = r instanceof Cleaner ? (Cleaner) r : null;
                // 将pending更新为链表下一个待回收元素
                pending = r.discovered;
                // r不再引用下一个节点
                r.discovered = null;  
            } else {
              	//否则就进入等待
                if (waitForNotify) {
                    lock.wait();
                }
                return waitForNotify;
            }
        }
    } catch (OutOfMemoryError x) {
        Thread.yield();
        return true;
    } catch (InterruptedException x) {
        return true;
    }

    // 如果元素是Cleaner类型，c在上面就会被赋值，这里就会执行其clean方法 ##########################
    if (c != null) {
        c.clean();
        return true;
    }

    ReferenceQueue<? super Object> q = r.queue;
    if (q != ReferenceQueue.NULL) q.enqueue(r);  //这个是引用队列，实际上就是我们之前在JVM篇中讲解的入队机制
    return true;
}

```

启动 ReferenceHandler 在  Reference 的静态代码块中

```java
static {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        for (ThreadGroup tgn = tg;
             tgn != null;
             tg = tgn, tgn = tg.getParent());
        Thread handler = new ReferenceHandler(tg, "Reference Handler");
        /* If there were a special system-only priority greater than
         * MAX_PRIORITY, it would be used here
         */
        handler.setPriority(Thread.MAX_PRIORITY);
        handler.setDaemon(true);
        handler.start();

        // provide access in SharedSecrets
        SharedSecrets.setJavaLangRefAccess(new JavaLangRefAccess() {
            @Override
            public boolean waitForReferenceProcessing()
                throws InterruptedException
            {
                return Reference.waitForReferenceProcessing();
            }

            @Override
            public void runFinalization() {
                Finalizer.runFinalization();
            }
        });
    }
```





### NIO Cleaner 流程

NIO 创建 ByteBuffer 的时候

```
cleaner = Cleaner.create(this, new Deallocator(base, size, cap));
```

ByteBuffer 里面有个虚引用对象  `Cleaner` （继承了 PhantomReference）

虚引用对象  `Cleaner` 引用了 ByteBuffer  --- 代码中 this

jvm 虚引用机制：当垃圾回收器决定对 ByteBuffer  对象进行回收时，会将其虚引用对象 `Cleaner` 插入ReferenceQueue中

专门线程 ReferenceHandler 处理监听的 ReferenceQueue









