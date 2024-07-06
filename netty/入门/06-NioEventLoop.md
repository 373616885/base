### NioEventLoop 常见问题

1. 默认情况下，netty服务端起多少线程？何时启动？

   默认情况下NioEventLoop启动2*CPU的线程，NioEventLoop在首次调用execute方法启动，通过成员变量thread判断是否是本线程，本线程说明已经启动，如果是外部线程则直接new 一个线程启动

2. netty是如何解决jdk空轮询bug的？

   空轮询默认到达512次，就重新创建selector，将旧的Channel重新注册到新的selector中

3. netty如何保证异步串行无锁化？

   如果是当前线程（当前的NioEventLoop ），一般就直接执行了
   
   如果是外部线程的任务,将任务封装成一个netty自己的task，然后丢到MpscQueue任务队列中，然后等待select执行结束，用NioEventLoop 绑定了的唯一线程去执行这个任务队列，用NioEventLoop 绑定了的唯一线程（单线程）来保证异步串行无锁化



### NioEventLoop 总体三部分

1. NioEventLoop 的创建
2. NioEventLoop 的启动
3. NioEventLoop 的执行逻辑



### NioEventLoop 的创建 

1. new NioEventLoopGroup()  线程组 默认 2*CPU核心
   1. new ThreadPerTaskExecutor() 线程创建器 
   2. for (int i = 0; i < nThreads; i ++)  newChild(); 构建 NioEventLoop
   3. chooserFactory.newChooser(children); 在EventLoopGroup线程池里选择一个线程NioEventLoop



### newChild(); 构建 NioEventLoop

保存线程执行器 ThreadPerTaskExecutor：负责创建一个新线程去执行任务

创建一个newMpscQueue（ 任务队列-外部线程也可以将任务扔进来）

创建一个Selector 多路复用器 : NioEventLoop需要保存NIO Selector 的属性





### chooserFactory.newChooser() 给新连接绑定NioEventLoop

在NioEventLoopGroup() 线程池（数组）里面选择一个NioEventLoop给新连接绑定

```java
//给每一个新连接绑定对应的NioEventLoop，那么对应的方法就在NioEventLoopGroup中的next()
public EventExecutor next() {
    // chooser 就是chooserFactory.newChooser()得到的
    return chooser.next();
}

// ChooserFactory.newChooser() 
public EventExecutorChooser newChooser(EventExecutor[] executors) {
    //判断executors.length是否为2的n次方
    if (isPowerOfTwo(executors.length)) {
        return new PowerOfTwoEventExecutorChooser(executors);
    } else {
        return new GenericEventExecutorChooser(executors);
    }
}
//PowerOfTwoEventExecutorChooser 或者 GenericEventExecutorChooser
//主要暴露 next() 方法
private static final class PowerOfTwoEventExecutorChooser implements EventExecutorChooser {
    private final AtomicInteger idx = new AtomicInteger();
    private final EventExecutor[] executors;

    PowerOfTwoEventExecutorChooser(EventExecutor[] executors) {
        this.executors = executors;
    }

    @Override
    public EventExecutor next() {
        // 
        return executors[idx.getAndIncrement() & executors.length - 1];
    }
}

private static final class GenericEventExecutorChooser implements EventExecutorChooser {
    // Use a 'long' counter to avoid non-round-robin behaviour at the 32-bit overflow boundary.
    // The 64-bit long solves this by placing the overflow so far into the future, that no system
    // will encounter this in practice.
    private final AtomicLong idx = new AtomicLong();
    private final EventExecutor[] executors;

    GenericEventExecutorChooser(EventExecutor[] executors) {
        this.executors = executors;
    }

    @Override
    public EventExecutor next() {
        return executors[(int) Math.abs(idx.getAndIncrement() % executors.length)];
    }
    }

```

这里netty做了优化：

如果是2的指数幂就（PowerOfTwoEventExecutorChooser）idx.getAndIncrement() & length - 1

不是就是idx.getAndIncrement() % length 取余

这里的优化和HashMap一样

关键：如果是2的指数幂 取余结果取决于被除数的低n位

通过 EventExecutorChooser的next() 获取NioEventLoop



### NioEventLoop 的启动

1. 服务端启动NioEventLoop 在绑定端口绑定时执行启动（NioEventLoop.run循环处理事件）
2. 新连接接入通过chooser绑定一个NioEventLoop



### 服务端启动NioEventLoop 

在绑定端口绑定时执行启动（NioEventLoop.run循环处理事件）

在注册服务端Channel的时候绑定线程 this.eventLoop = eventLoop; （bossGroup） 

bind()  

​	execute(task) 入口

​		startThread()   -> doStartThread(); 创建线程

​			ThreadPerTaskExecutor.execute() 创建一个新线程，然后执行

​				thread = Thread.currentThread();  保存刚才创建的线程，同时用于判断是否当前线程执行不是就扔任务队列里

​				SingleThreadEventExecutor.this.run(); 用刚才创建的新线程驱动服务端启动的核心 ：NioEventLoop.run() 循环处理



execute(task) 入口:

channel.eventLoop() 在服务端注册Channel的时候绑定线程 this.eventLoop = eventLoop; （bossGroup）

execute: 调用NioEventLoop.SingleThreadEventLoop.SingleThreadEventExecutor.execute

channel.eventLoop().execute(new Runnable() {

})



SingleThreadEventExecutor.execute：
首先判断是否：是否当前线程

添加任务队列

是否当前线程:

​	是：已经创建过线程

​	否：创建一个先线程 



创建一个先线程 

startThread();

​	SingleThreadEventExecutor的属性thread   首次调用必须为空

​	调用ThreadPerTaskExecutor.execute 创建一个先线程threadFactory.newThread(command).start()

​	去执行启动SingleThreadEventExecutor.this.run()

​	实际最终是去调用：NioEventLoop..SingleThreadEventExecutor.run() 

​	

```java

private static void doBind0(
            final ChannelFuture regFuture, final Channel channel,
            final SocketAddress localAddress, final ChannelPromise promise) {

    //channel.eventLoop()
    //在注册服务端Channel的时候绑定线程 this.eventLoop = eventLoop; （bossGroup） 
    //execute(task) 入口： NioEventLoop.SingleThreadEventLoop.SingleThreadEventExecutor.execute
    channel.eventLoop().execute(new Runnable() {
        @Override
        public void run() {
            if (regFuture.isSuccess()) {
                channel.bind(localAddress, promise).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                promise.setFailure(regFuture.cause());
            }
        }
    });
}

//NioEventLoop.SingleThreadEventLoop.SingleThreadEventExecutor.execute
private void execute(Runnable task, boolean immediate) {
    // 判断是否是当前线程
    boolean inEventLoop = inEventLoop();
    // 添加队列
    addTask(task);
    // 开始Thread = null ,当前线程是main方法 inEventLoop= false
    if (!inEventLoop) {
        // 开始创建线程 startThread() -> doStartThread()
        // 1.创建新线程
        // 2.NioEventLoop属性thread = 创建的新线程
        // 3.用新线程去启动服务端run方法：for循环处理 
        startThread();
        .......
    }
    
}
// startThread()
private void startThread() {
    //判断当前线程是否是未启动。
    if (STATE_UPDATER.get(this) == ST_NOT_STARTED) {
        //通过一个cas操作，来进行开启线程，这里就是防止多个线程同时调用同一个NioEventLoop实例执行这段代码，造成线程不安全。
        if (STATE_UPDATER.compareAndSet(this, ST_NOT_STARTED, ST_STARTED)) {
            //然后我们来看看这个方法
            doStartThread();
        }
    }
}

//SingleThreadEventExecutor.doStartThread
private void doStartThread() {
    // 首次属性thread必须为空，用于判断是否是当前线程：inEventLoop();
    // SingleThreadEventExecutor的属性thread
    assert thread == null;
    // ThreadPerTaskExecutor:创建一个新线程去执行任务
    // 调用ThreadPerTaskExecutor.execute
    executor.execute(new Runnable() {
        @Override
        public void run() {
            // NioEventLoop.SingleThreadEventLoop.SingleThreadEventExecutor的属性thread赋值
            //拿到当前线程，并保存。这里就是进行NioEventLoop 和线程进行唯一的绑定。
            thread = Thread.currentThread();
            ......
            try {
                // 服务端NioEventLoop启动:最终调用：NioEventLoop.run() 
                SingleThreadEventExecutor.this.run();
                success = true;
            } catch (Throwable t) {
                logger.warn("Unexpected exception from an event executor: ", t);
            } 
            ......
        }
    });
}

// ThreadPerTaskExecutor.execute
public void execute(Runnable command) {
    // 创建一个新线程然后启动：SingleThreadEventExecutor.this.run()
    threadFactory.newThread(command).start();
}
```





#### NioEventLoop 服务端的执行

NioEventLoop.SingleThreadEventLoop.SingleThreadEventExecutor.this.run()

最终调用：NioEventLoop.run 这个方法主要处理三件事情

run()  -> for(::) 

1. select() 检查注册到Selector上的Channel是否有IO事件
2. processSelectedKeys() 处理IO事件
3. runAllTasks() 处理异步任务队列 --处理队列和处理IO事件的时间个50%



#### 检查注册到Selector上的Channel是否有IO事件主要过程

1. 计算deadline以及任务的穿插逻辑处理（bossGroup服务端的处理accept事件会穿插处理任务）
2. 阻塞式seelct
3. 避免jdk空轮询的bug：解决的办法空轮询一定的次数默认512



```java
@Override
protected void run() {
    int selectCnt = 0;
    for (;;) {
        try {
            int strategy;
            try {
                // 获取通道策略值
                // 有任务就立马唤醒：selectNow() ,没任务就 等于SelectStrategy.SELECT
                strategy = selectStrategy.calculateStrategy(selectNowSupplier, hasTasks());
                switch (strategy) {
                case SelectStrategy.CONTINUE:
                    continue;

                case SelectStrategy.BUSY_WAIT:
                    // 没有需要执行的任务
                    // fall-through to SELECT since the busy-wait is not supported with NIO

                case SelectStrategy.SELECT:
                    // 获取下一个定时任务的截止时间
                    long curDeadlineNanos = nextScheduledTaskDeadlineNanos();
                    if (curDeadlineNanos == -1L) {
                        curDeadlineNanos = NONE; // nothing on the calendar
                    }
                    nextWakeupNanos.set(curDeadlineNanos);
                    try {
                        if (!hasTasks()) {
                            // 计算deadline然后 阻塞式seelct ：selector.select(timeoutMillis)
                            // 获取已就绪的通道
                            // 如果curDeadlineNanos为NONE,会一直阻塞 selector.select()
                            strategy = select(curDeadlineNanos);
                        }
                    } finally {
                        // This update is just to help block unnecessary selector wakeups
                        // so use of lazySet is ok (no race condition)
                        nextWakeupNanos.lazySet(AWAKE);
                    }
                    // fall through
                default:
                }
            } catch (IOException e) {
                // If we receive an IOException here its because the Selector is messed up. Let's rebuild
                // the selector and retry. https://github.com/netty/netty/issues/8566
                // 异常重新创建Selector，channel重新注册到新的Selector上
                rebuildSelector0();
                selectCnt = 0;
                handleLoopException(e);
                continue;
            }
			
			// 每次执行，数量+1
            selectCnt++;
            cancelledKeys = 0;
            needsToSelectAgain = false;
            // IO 处理时间占比和 处理任务占比 :默认处理任务占比50-- (100 - ioRatio) / ioRatio
            final int ioRatio = this.ioRatio;
            boolean ranTasks;
            // 处理IO事件的占比为100
            if (ioRatio == 100) {
                try {
                    // 先处理IO事件
                    if (strategy > 0) {
                        // 处理IO事件
                        processSelectedKeys();
                    }
                } finally {
                    // 最后再处理任务
                    // Ensure we always run tasks.
                    ranTasks = runAllTasks();
                }
            } else if (strategy > 0) {
                //有已就绪的通道,处理IO事件占比不是100。默认50
                final long ioStartTime = System.nanoTime();
                try {
                    // 处理IO事件占比默认为50
                    processSelectedKeys();
                } finally {
                    // Ensure we always run tasks.
                    final long ioTime = System.nanoTime() - ioStartTime;
                    //处理任务事件占比默认为50:IO处理时间的一半
                    ranTasks = runAllTasks(ioTime * (100 - ioRatio) / ioRatio);
                }
            } else {
                // strategy = 0 
                // 没有已就绪的通道，直接处理任务
                ranTasks = runAllTasks(0); // This will run the minimum number of tasks
            }
			// 运行过任务后，ranTasks会设置为true
            // 当处理的异步任务或者IO事件的数量大于0,证明没有发生空轮询
            // 既没有IO就绪事件，也没有异步任务，Reactor线程从Selector上被异常唤醒 触发JDK Epoll空轮训BUG
            if (ranTasks || strategy > 0) {
                if (selectCnt > MIN_PREMATURE_SELECTOR_RETURNS && logger.isDebugEnabled()) {
                    logger.debug("Selector.select() returned prematurely {} times in a row for Selector {}.",
                            selectCnt - 1, selector);
                }
                // 正常返回，selectCnt重置为0
                selectCnt = 0;
                
            // 【重点分析】判断selectCnt是否超过阈值，否则重建selector，解决NIO空轮转的问题
            } else if (unexpectedSelectorWakeup(selectCnt)) { // Unexpected wakeup (unusual case)
                // unexpectedSelectorWakeup解决jdk空轮询的bug
                
                selectCnt = 0;
            }
        } catch (CancelledKeyException e) {
            // Harmless exception - log anyway
            if (logger.isDebugEnabled()) {
                logger.debug(CancelledKeyException.class.getSimpleName() + " raised by a Selector {} - JDK bug?",
                        selector, e);
            }
        } catch (Error e) {
            throw e;
        } catch (Throwable t) {
            handleLoopException(t);
        } finally {
            // Always handle shutdown even if the loop processing threw an exception.
            try {
                if (isShuttingDown()) {
                    closeAll();
                    if (confirmShutdown()) {
                        return;
                    }
                }
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                handleLoopException(t);
            }
        }
    }
}

// 计算deadline
private int select(long deadlineNanos) throws IOException {
    // 没有定时任务，就一直阻塞
    if (deadlineNanos == NONE) {
        return selector.select();
    }
    // Timeout will only be 0 if deadline is within 5 microsecs
    // 计算deadline 5 microsecs以内超时时间都是0
    // 任务队列的最新任务的截止时间和现在比较是否有超过（计算当前时间有没有超时-和任务队列最新任务的截止时间比较）
    long timeoutMillis = deadlineToDelayNanos(deadlineNanos + 995000L) / 1000000L;
    // 已经超过定时任务的截止时间就立马selectNow（） ，没到定时任务的截止时间就阻塞等到定时任务开始时间
    return timeoutMillis <= 0 ? selector.selectNow() : selector.select(timeoutMillis);
}


// returns true if selectCnt should be reset
private boolean unexpectedSelectorWakeup(int selectCnt) {
    // 异常情况处理，当前线程可能被其他线程中断了
    if (Thread.interrupted()) {
        // Thread was interrupted so reset selected keys and break so we not run into a busy loop.
        // As this is most likely a bug in the handler of the user or it's client library we will
        // also log it.
        //
        // See https://github.com/netty/netty/issues/2426
        if (logger.isDebugEnabled()) {
            logger.debug("Selector.select() returned prematurely because " +
                         "Thread.currentThread().interrupt() was called. Use " +
                         "NioEventLoop.shutdownGracefully() to shutdown the NioEventLoop.");
        }
        return true;
    }
    // seelct循环次数超过SELECTOR_AUTO_REBUILD_THRESHOLD 默认512 次就创建Selector避免jdk空轮询bug
    if (SELECTOR_AUTO_REBUILD_THRESHOLD > 0 &&
        selectCnt >= SELECTOR_AUTO_REBUILD_THRESHOLD) {
        // The selector returned prematurely many times in a row.
        // Rebuild the selector to work around the problem.
        logger.warn("Selector.select() returned prematurely {} times in a row; rebuilding Selector {}.",
                    selectCnt, selector);
        rebuildSelector();
        return true;
    }
    return false;
}

// 重新创建Selector，将旧Selector中注册的事件全部取消，channel重新注册到新的Selector上
// 整个过程就是新建一个Selector，遍历旧的selector，将旧Selector中注册的事件全部取消，把channel注册到新的selector后，  将旧的selector关闭
private void rebuildSelector0() {
    final Selector oldSelector = selector;
    final SelectorTuple newSelectorTuple;

    if (oldSelector == null) {
        return;
    }

    try {
        newSelectorTuple = openSelector();
    } catch (Exception e) {
        logger.warn("Failed to create a new Selector.", e);
        return;
    }

    // Register all channels to the new Selector.
    int nChannels = 0;
    for (SelectionKey key: oldSelector.keys()) {
        Object a = key.attachment();
        try {
            if (!key.isValid() || key.channel().keyFor(newSelectorTuple.unwrappedSelector) != null) {
                continue;
            }

            int interestOps = key.interestOps();
            key.cancel();
            SelectionKey newKey = key.channel().register(newSelectorTuple.unwrappedSelector, interestOps, a);
            if (a instanceof AbstractNioChannel) {
                // Update SelectionKey
                ((AbstractNioChannel) a).selectionKey = newKey;
            }
            nChannels ++;
        } catch (Exception e) {
            logger.warn("Failed to re-register a Channel to the new Selector.", e);
            if (a instanceof AbstractNioChannel) {
                AbstractNioChannel ch = (AbstractNioChannel) a;
                ch.unsafe().close(ch.unsafe().voidPromise());
            } else {
                @SuppressWarnings("unchecked")
                NioTask<SelectableChannel> task = (NioTask<SelectableChannel>) a;
                invokeChannelUnregistered(task, key, e);
            }
        }
    }

    selector = newSelectorTuple.selector;
    unwrappedSelector = newSelectorTuple.unwrappedSelector;

    try {
        // time to close the old selector as everything else is registered to the new one
        oldSelector.close();
    } catch (Throwable t) {
        if (logger.isWarnEnabled()) {
            logger.warn("Failed to close the old Selector.", t);
        }
    }

    if (logger.isInfoEnabled()) {
        logger.info("Migrated " + nChannels + " channel(s) to the new Selector.");
    }
}

```



##### JDK空轮询Bug

问题产生于linux的epoll。如果一个socket文件描述符，注册的事件集合码为0，然后连接突然被对端中断，那么epoll会被POLLHUP或者有可能是POLLERR事件给唤醒，并返回到事件集中去。这意味着，Selector会被唤醒，即使对应的channel兴趣事件集是0，并且返回的events事件集合也是0。

简而言之就是，jdk认为linux的epoll告诉我事件来了，但是jdk没有拿到任何事件（READ、WRITE、CONNECT、ACCPET）。但此时select()方法不再选择阻塞了，而是选择返回了0。

```java
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class NioServer {

    public static void main(String[] args) throws IOException {
        // 绑定端口，开启服务
        final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));

        final Selector selector = Selector.open();
        // 服务端的serverSocketChannel注册到 selector上
        SelectionKey register = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, "qinjp");

        System.out.println("服务器启动成功" + register.toString());

        while (true) {
            // 阻塞等待客户端事件发送，这里有超时时间设置
            // 这里通常是阻塞的，但是linux的epoll的问题，被唤醒而对应的channel兴趣事件集却是0
            int select = selector.select();
            if (select < 1) {
                System.out.println("当前没有连接进来");
            }
            // 注册上了的 channel 都对应一个 SelectionKey
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            // 被唤醒而对应的channel兴趣事件集是0，导致这段没有执行，又回到上面的while (true)，
            // 循环往复，不断的轮询，直到linux系统出现100%的CPU情况,这就是JDK空轮询Bug
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                // selector多路复用器接收到一个accept事件
                if (key.isAcceptable()) {
                    Object attachment = key.attachment();
                    System.out.println(attachment);
                    // 接受请求
                    ServerSocketChannel sktChannel = (ServerSocketChannel) key.channel();
                    SocketChannel newSocketChannel = sktChannel.accept();
                    // 这里会接收一个客户端SocketChannel的连接请求，并返回对应的SocketChannel
                    // 注意这里如果没有对应的客户端Channel就会返回null
//                    SocketChannel newSocketChannel = serverSocketChannel.accept();
                    System.out.println("收到客户端请求：" + newSocketChannel.getRemoteAddress());
                    // 每一个新的客户端请求都设置成非阻塞
                    newSocketChannel.configureBlocking(false);
                    // 将与客户端对接好的newSocketChannel注册到select上，并关注读事件
                    // 注册读事件，需要绑定一个buffer相当于附件，所有的事件交互都通过这个buffer
                    newSocketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }
                // 数据读取事件
                if (key.isReadable()) {
                    // 其他的代码基本都是这个模板，只是这个处理客户端请求需要定制
                    // accept事件是ServerSocketChannel
                    // read事件是SocketChannel
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    // 取上次accept注册的buffer
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    buffer.clear();
                    socketChannel.read(buffer);
                    String request = new String(buffer.array(), StandardCharsets.UTF_8);
                    System.out.println("收到客户端消息：" + request);

                    // 回写
                    String str = "服务端收到消息：" + request;
                    socketChannel.write(ByteBuffer.wrap(str.getBytes(StandardCharsets.UTF_8)));

                }
                // 已经处理完的事件要清除，防止重复处理
                // 不然的话，对应连接请求，服务端还是会去accept产生一个SocketChannel
                // 但此时客户端没有开对接，就会返回一个null
                iterator.remove();

            }

        }
    }


}
```

![](img\2022-08-22 024347.png)

因为selector的select方法，返回numKeys是0，所以下面本应该对key值进行遍历的事件处理根本执行不了，又回到最上面的while(true)循环，循环往复，不断的轮询，直到linux系统出现100%的CPU情况，其它执行任务干不了活，





#### processSelectedKeys() 执行逻辑 

1. selected KeySet 优化 
2. processSelectedKeysOptimized 真正的处理IO事件



selected KeySet 优化 ： 在openSelector里体现

将sun.nio.ch.SelectorImpl 的selectedKeys和publicSelectedKeys属性由HashSet变成数组

时间复杂度变成 O(1)

在 new NioEventLoopGroup -> new NioEventLoop()  --> openSelector()

```java
NioEventLoop(NioEventLoopGroup parent, Executor executor, SelectorProvider selectorProvider,
                 SelectStrategy strategy, RejectedExecutionHandler rejectedExecutionHandler,
                 EventLoopTaskQueueFactory taskQueueFactory, EventLoopTaskQueueFactory tailTaskQueueFactory) {
    super(parent, executor, false, newTaskQueue(taskQueueFactory), newTaskQueue(tailTaskQueueFactory),
          rejectedExecutionHandler);
    this.provider = ObjectUtil.checkNotNull(selectorProvider, "selectorProvider");
    this.selectStrategy = ObjectUtil.checkNotNull(strategy, "selectStrategy");
    // 这里将selectedkey的属性由 HashSet变成数组
    final SelectorTuple selectorTuple = openSelector();
    this.selector = selectorTuple.selector;
    this.unwrappedSelector = selectorTuple.unwrappedSelector;
}

private SelectorTuple openSelector() {
    // jdk原生的
    final Selector  unwrappedSelector = provider.openSelector();
    
	// 不需要优化
    if (DISABLE_KEY_SET_OPTIMIZATION) {
        return new SelectorTuple(unwrappedSelector);
    }
	
    // 替换成这个 SelectedSelectionKeySet
    // 因为只要关注add方法，remove方法不需要关注，所以可以替换成数组
    // 扩容：2倍扩容的形式
    final SelectedSelectionKeySet selectedKeySet = new SelectedSelectionKeySet();
    
    Object maybeSelectorImplClass = AccessController.doPrivileged(new PrivilegedAction<Object>() {
        @Override
        public Object run() {
            try {
                return Class.forName(
                    // SelectorImpl的属性由这个类的属性由HashSet变成数组
                    "sun.nio.ch.SelectorImpl",
                    false,
                    PlatformDependent.getSystemClassLoader());
            } catch (Throwable cause) {
                return cause;
            }
        }
    });
	
    //不是SelectorImpl
    if (!(maybeSelectorImplClass instanceof Class) ||
            // ensure the current selector implementation is what we can instrument.
            !((Class<?>) maybeSelectorImplClass).isAssignableFrom(unwrappedSelector.getClass())) {
        // 不是SelectorImp就返回原生的
        return new SelectorTuple(unwrappedSelector);
    }
    .....
        
    Object maybeException = AccessController.doPrivileged(new PrivilegedAction<Object>() {
        @Override
        public Object run() {
            try {
                Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
                Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");
                
                ......
                // selectedKeys属性重新赋值     
                selectedKeysField.set(unwrappedSelector, selectedKeySet);
                // publicSelectedKeys属性重新赋值    
                publicSelectedKeysField.set(unwrappedSelector, selectedKeySet);
                
                .....
            } 
        }
    });
}
```





#### **NioEventLoop.processSelectedKeys() 真正的处理IO事件**

实际是：NioEventLoop的unsafe.read() 去处理

```java
private void processSelectedKeys() {
    // selectedKeys经过优化可能不为空
    if (selectedKeys != null) {
        // 处理IO事件
        processSelectedKeysOptimized();
    } else {
        // 没有经过优化的处理
        processSelectedKeysPlain(selector.selectedKeys());
    }
}

// 真正的处理IO事件
private void processSelectedKeysOptimized() {
    for (int i = 0; i < selectedKeys.size; ++i) {
        final SelectionKey k = selectedKeys.keys[i];
        // null out entry in the array to allow to have it GC'ed once the Channel close
        // See https://github.com/netty/netty/issues/2363
        // 拿到就置为空
        selectedKeys.keys[i] = null;
		// 获取Channel的附加对象，之前已经知道是netty对channel的封装 
        final Object a = k.attachment();
		// 如果是netty对channel的封装 
        if (a instanceof AbstractNioChannel) {
            processSelectedKey(k, (AbstractNioChannel) a);
        } 
    }
}

// 处理IO事件
private void processSelectedKey(SelectionKey k, AbstractNioChannel ch) {
    // 拿到unsafe
    final AbstractNioChannel.NioUnsafe unsafe = ch.unsafe();
    // 连接不可用
    if (!k.isValid()) {
		// 连接不可用 close
        unsafe.close(unsafe.voidPromise());
        return;
    }

   
    int readyOps = k.readyOps();
    // We first need to call finishConnect() before try to trigger a read(...) or write(...) as otherwise
    // the NIO JDK channel implementation may throw a NotYetConnectedException.
    if ((readyOps & SelectionKey.OP_CONNECT) != 0) {
        // remove OP_CONNECT as otherwise Selector.select(..) will always return without blocking
        // See https://github.com/netty/netty/issues/924
        int ops = k.interestOps();
        ops &= ~SelectionKey.OP_CONNECT;
        k.interestOps(ops);

        unsafe.finishConnect();
    }

    // Process OP_WRITE first as we may be able to write some queued buffers and so free memory.
    if ((readyOps & SelectionKey.OP_WRITE) != 0) {
        // Call forceFlush which will also take care of clear the OP_WRITE once there is nothing left to write
        ch.unsafe().forceFlush();
    }

    // Also check for readOps of 0 to workaround possible JDK bug which may otherwise lead
    // to a spin loop
    // [重点关注]如果是workGroup就是OP_READ ，如果是bossGroup就关注OP_ACCEPT，就是有新连接进来了
    if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
        unsafe.read();
    }
    
}

```









#### runAllTasks() 执行逻辑

- task的分类和添加
- 任务聚合
- 任务执行



task的分类：

两种：

普通队列new NioEventLoop 的时候初始化 newMpscQueue

定时任务队列 NioEventLoop队列暴露的schedule方法

task的添加：

```java
// 添加
//NioEventLoop.SingleThreadEventLoop.SingleThreadEventExecutor.execute
//NioEventLoop执行execute的时候会添加任务
//开始NioEventLoop启动的时候NioServerSocketChannel绑定端口的时候-执行了
// NioEventLoop.execute一般外部线程调用都是第一次
// 第一次就创建一个新线程让后调用NioEventLoop.SingleThreadEventLoop.run
// 在NioEventLoop.run里面runAllTasks里处理添加的任务
// 都是在等待select结束后才会去执行的这个任务
private void execute(Runnable task, boolean immediate) {
    // 这里用于判断是否是当前线程需要执行
    // 有可能是外部线程调用 execute
    boolean inEventLoop = inEventLoop();
    // 添加任务
    addTask(task);
    if (!inEventLoop) {
        // 开始创建线程 startThread() -> doStartThread()
        // 1.创建新线程
        // 2.NioEventLoop属性thread = 创建的新线程
        // 3.用新线程去启动服务端run方法：for循环处理 
        startThread();
        if (isShutdown()) {
            boolean reject = false;
            try {
                if (removeTask(task)) {
                    reject = true;
                }
            } catch (UnsupportedOperationException e) {
                // The task queue does not support removal so the best thing we can do is to just move on and
                // hope we will be able to pick-up the task before its completely terminated.
                // In worst case we will log on termination.
            }
            if (reject) {
                reject();
            }
        }
    }

}    
//NioEventLoop队列暴露的schedule方法
public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    ObjectUtil.checkNotNull(command, "command");
    ObjectUtil.checkNotNull(unit, "unit");
    if (delay < 0) {
        delay = 0;
    }
    validateScheduled0(delay, unit);

    return schedule(new ScheduledFutureTask<Void>(
        this,
        command,
        deadlineNanos(getCurrentTimeNanos(), unit.toNanos(delay))));
}

@Override
public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    ObjectUtil.checkNotNull(callable, "callable");
    ObjectUtil.checkNotNull(unit, "unit");
    if (delay < 0) {
        delay = 0;
    }
    validateScheduled0(delay, unit);

    return schedule(new ScheduledFutureTask<V>(
        this, callable, deadlineNanos(getCurrentTimeNanos(), unit.toNanos(delay))));
}
// 当前线程就添加定时任务，不是当前线程
// 就execute添加任务，让NioEventLoop去串形化执行
// 为什么这样呢？因为DefaultPriorityQueue不是线程安全的
private <V> ScheduledFuture<V> schedule(final ScheduledFutureTask<V> task) {
    if (inEventLoop()) {
        scheduleFromEventLoop(task);
    } else {
        final long deadlineNanos = task.deadlineNanos();
        // task will add itself to scheduled task queue when run if not expired
        if (beforeScheduledTaskSubmitted(deadlineNanos)) {
            execute(task);
        } else {
            lazyExecute(task);
            // Second hook after scheduling to facilitate race-avoidance
            if (afterScheduledTaskSubmitted(deadlineNanos)) {
                execute(WAKEUP_TASK);
            }
        }
    }

    return task;
}


```



任务聚合：聚合定时任务和普通任务，将定时任务添加到普通任务队列里

```java
   
//NioEventLoop.runAllTasks
protected boolean runAllTasks(long timeoutNanos) {
    // 先聚合任务，聚合定时任务和普通任务，将定时任务添加到普通任务队列里
    fetchFromScheduledTaskQueue();
    // 拿任务
    Runnable task = pollTask();
    if (task == null) {
        // 跑完所有任务后执行一个收尾的工作
        afterRunningAllTasks();
        return false;
    }
	.........
}
```



任务执行:

```java
protected boolean runAllTasks(long timeoutNanos) {
    // 先聚合任务
    fetchFromScheduledTaskQueue();
    // 拿任务
    Runnable task = pollTask();
    if (task == null) {
        // 跑完所有任务后执行一个收尾的工作
        afterRunningAllTasks();
        return false;
    }

    final long deadline = timeoutNanos > 0 ? getCurrentTimeNanos() + timeoutNanos : 0;
    long runTasks = 0;
    long lastExecutionTime;
    for (;;) {
        // 执行任务
        safeExecute(task);

        runTasks ++;
		
        // Check timeout every 64 tasks because nanoTime() is relatively expensive.
        // XXX: Hard-coded value - will make it configurable if it is really a problem.
        // 64 次任务之后 才去检查超时时间，因为getCurrentTimeNanos是一个耗时的操作
        if ((runTasks & 0x3F) == 0) {
            lastExecutionTime = getCurrentTimeNanos();
            if (lastExecutionTime >= deadline) {
                break;
            }
        }
		// 拿任务
        task = pollTask();
        if (task == null) {
            lastExecutionTime = getCurrentTimeNanos();
            break;
        }
    }
	// 跑完所有任务后执行一个收尾的工作
    afterRunningAllTasks();
    this.lastExecutionTime = lastExecutionTime;
    return true;
}

// 执行任务
protected static void safeExecute(Runnable task) {
    try {
        // NioEventLoop,执行任务，不会拋异常，只会打一个warn的日志
        runTask(task);
    } catch (Throwable t) {
        logger.warn("A task raised an exception. Task: {}", task, t);
    }
}

```









### 简单总结

**NioEventLoop 的创建** 

用户在创建boosEventLoopGroup的时候被创建

如果不指定参数，默认会创建2倍CPU的倍数的NioEventLoop

每一个NioEventLoop都会有一个chooserFactory.newChooser() 创建的chooser去分配线程

这个chooser也会针对NioEventLoop的个数进行优化，2的指数幂，就idx & length - 1，非2指数幂则idx % length，想hashMap一样优化

在创建NioEventLoop的时候，会创建一个Selector和一个任务队列newMpscQueue 和一个线程执行器 ThreadPerTaskExecutor

在创建一个Selector的时候，会通过反射的方式，用数据实现替换掉底层两个的HashSet属性（selectedKeys和publicSelectedKeys）

**NioEventLoop 的启动**

NioEventLoop 在首次调用execute() 方法的时候启动线程，这个线程是FastThreadLocalThread

启动线程之后保存到成员变量thread，通过这个变量判断，执行的逻辑是否本线程（可保证异步串形无锁化）

**NioEventLoop 的执行逻辑** 

在NioEventLoop.run() 方法里面，主要包括三个过程

1. select() 检查注册是否有IO事件
2. processSelectedKeys() 处理IO事件
3. runAllTasks() 执行任务队列





















































