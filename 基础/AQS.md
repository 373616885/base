### AbstractQueuedSynchronizer 抽象队列同步器

1. AQS对象内部有一个核心的变量叫做**state**，是int类型的，代表了**加锁的状态**。初始状态下，state的值是0 

2. 这个AQS内部还有一个**关键变量**，用来记录**当前加锁的是哪个线程** 
3. 共享资源的线程封装成一个CLH（双向）队列的一个结点（Node）（FIFO线程等待队列）

```java
private volatile int state;

private transient Thread exclusiveOwnerThread;

private transient volatile Node head;

private transient volatile Node tail;

static final class Node {
    
    volatile int waitStatus;

    volatile Node prev;

    volatile Node next;

    volatile Thread thread;

    Node nextWaiter;
}
```



AQS维护了一个volatile int state和一个FIFO线程等待队列，

线程通过CAS去改变状态符，成功则获取锁成功，失败则进入等待队列，等待被唤醒。

state就是共享资源，其访问方式有如下三种原子操作：

```java
getState()
    
setState()
    
compareAndSetState()
    
private volatile int state;

protected final int getState() {
    return state;
}

protected final void setState(int newState) {
    state = newState;
}

protected final boolean compareAndSetState(int expect, int update) {
    // See below for intrinsics setup to support this
    return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
}
```



资源的共享方式分为2种：

- 独占式(Exclusive)

只有单个线程能够成功获取资源并执行，如ReentrantLock。

- 共享式(Shared)

多个线程可成功获取资源并执行，如Semaphore/CountDownLatch等。



###  ReentrantLock（安去lock）和 AQS的关系

![](img\20200601.jpg)



 **Java并发包下的ReentrantLock （互斥锁）来加锁和释放锁** 

```java
//可重入的意思是 -- 可以多次加锁--多次解锁 

// 这个可重入锁默认是非公平锁--唤醒是竞争的
ReentrantLock lock = new ReentrantLock();
// true 标示公平锁--先进先出
ReentrantLock lock = new ReentrantLock(true);
try {
    lock.lock();// 加锁
    
    // 业务逻辑
} catch (Exception e) {
    e.printStackTrace();
} finally {
    lock.unlock();// 释放锁
}

```



**ReentrantLock 基于AQS来实现的加锁和释放锁功能的**

1. ReentrantLock的lock()方法加锁 ： 用CAS操作将state值从0变为1

2. 如果之前没人加过锁，那么state的值肯定是0，此时线程1就可以加锁成功

3. 线程1加锁成功了之后，就可以设置当前加锁线程是自己 

4. 一旦线程1加锁成功了之后，就可以设置当前加锁线程是自己 

![](img\20200602.jpg)

5. ReentrantLock  是可重入的锁表示 ReentrantLock对象可以多次执行lock()加锁和unlock()释放锁

6. 其实每次线程1可重入加锁一次，会判断一下当前加锁线程就是自己，那么他自己就可以可重入多次加锁，每次加锁就是把state的值给累加1 

7. 接着，如果线程1加锁了之后，线程2跑过来加锁会怎么样呢？ 

8.  线程2跑过来发现 state的值不是0，所以CAS操作将state从0变为1的过程会失败，因为state的值当前为1，说明已经有人加锁了！ 

9.  接着线程2会看一下，是不是自己之前加的锁啊？当然不是了，**“加锁线程”**这个变量明确记录了是线程1占用了这个锁，所以线程2此时就是加锁失败。 

   

   ![](img\20200603.jpg)

   

10.   接着线程2会将自己放入AQS中的一个等待队列，因为自己尝试加锁失败了，此时就要将自己放入队列中来等待，等待线程1释放锁之后，自己就可以重新尝试加锁了 

![](img\20200604.jpg)

11.  接着，线程1在执行完自己的业务逻辑代码之后，就会释放锁！**他释放锁的过程非常的简单**，就是将AQS内的state变量的值递减1，如果state值为0，则彻底释放锁，会将“加锁线程”变量也设置为null！ 
12. 接下来，会从**等待队列的队头唤醒线程2重新尝试加锁。**公平锁就是FIFO，非公平锁就是竞争
13. 好！线程2现在就重新尝试加锁，这时还是用CAS操作将state从0变为1，此时就会成功，成功之后代表加锁成功，就会将state设置为1。
14. 此外，还要把**“加锁线程”**设置为线程2自己，同时线程2自己就从等待队列中出队了。

 最后再来一张图，大家来看看这个过程 

![](img\20200605.jpg)















