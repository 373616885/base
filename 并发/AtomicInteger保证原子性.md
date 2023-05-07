#### AtomicInteger 能够实现整型数据的原子操作

使用乐观锁实现：

volatile保证了一个线程修改数据时，其它线程也能看到数据的修改

CAS操作保证了数据修改的安全性（乐观锁）



#### CAS存在ABA问题

存在这样一种情况：如果一个值原来是A，变成了B，然后又变成了A，那么在CAS检查的时候会发现没有改变，但是实质上它已经发生了改变，这就是所谓的ABA问题



#### AtomicInteger的源码

incrementAndGet 取一个方法（相当于 ++ i ）

```java
	private static final Unsafe unsafe = Unsafe.getUnsafe();
	//数据在内存中的地址偏移量，通过偏移地址可以获取数据原值
    private static final long valueOffset;
    static {
        try {
            //计算变量 value 在类对象中的偏移量
            valueOffset = unsafe.objectFieldOffset
                (AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }
	//要修改的值 volatile保证可见性
    private volatile int value;
	// 相当于 ++i 是先赋值后加 
    public final int incrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
    }

	//内部使用自旋的方式进行CAS更新（while循环进行CAS更新，如果更新失败，则循环再次重试）
    public final int getAndAddInt(Object var1, long var2, int var4) {
         //var1为当前这个对象，如count.getAndIncrement()，则var1为count这个对象
        //第二个参数为AtomicInteger对象value成员变量在内存中的偏移量
        //第三个参数为要增加的值
        int var5;
        do {
            // var1  =  this
            // var2  =  AtomicInteger对象value成员变量在内存中的偏移量
            // var5  =  获取对象内存地址偏移量上的数值v 即预期旧值
            var5 = this.getIntVolatile(var1, var2);
            // var1  =  this
            // var2  =  AtomicInteger对象value成员变量在内存中的偏移量
            // var5  =  获取对象内存地址偏移量上的数值v 即预期旧值
            // var5 + var4 = 期望修改值
        } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));//循环判断内存位置的值与预期原值是否相匹配
        return var5;
    }
```

getIntVolatile()：通过内存地址获取volatile修饰的变量

```java
public native int  getIntVolatile(Object o, long offset);
```

unsafe的CPU级别的操作CAS指令的类：

**你只需要记住：**CAS是靠硬件实现的，从而在硬件层面提升效率。实现方式是基于硬件平台的汇编指令

```java
    public final boolean weakCompareAndSetInt(Object o, long offset,
                                              int expected,
                                              int x) {
        return compareAndSetInt(o, offset, expected, x);
    }    

	public final boolean compareAndSet(int expect, int update) {
        // 先比较现在的value 是否与expect相等，如果不相等，
        // 则进入下一个循环。如果相等，则会更新成update值
        return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    }
	// C++ 代码	
	// o : this.AtomicInteger对象
	// offset : AtomicInteger对象value成员变量在内存中的偏移量
	// expected : 即旧值
	// x ： 修改值
	public final native boolean compareAndSetInt(Object o, long offset,
                                                 int expected,
                                                 int x);
```



#### incrementAndGet和getAndIncrement的区别

```java
AtomicInteger i=new AtomicInteger(0);
System.out.println(i.incrementAndGet());
System.out.println(i.getAndIncrement());

//两种方式输入的值都是1，那他们就没有区别了吗？

AtomicInteger i=new AtomicInteger(0);
int q=i.incrementAndGet();
System.out.println(q);输出1
或
int q=i.getAndIncrement();
System.out.println(q);输出0
类似于i++和++i的区别
一般都用 incrementAndGet
 
getAndIncrement i++
getAndDecrement i--
getAndAdd 先取值后计算
incrementAndGet ++i
decrementAndGet --i
addAndGet 先计算后取值  
    
    
```



#### AtomicLong 存在的性能问题

```java
package com.qin.demo.atomic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 描述：在16个线程下使用AtomicLong
 */
public class AtomicTest {

    public static void main(String[] args) throws InterruptedException {

        var counter = new AtomicLong(0);

        ExecutorService service = Executors.newFixedThreadPool(16);

        for (int i = 0; i < 100; i++) {
            service.submit(new Task(counter));
        }

        Thread.sleep(2000);

        System.out.println(counter.get());
    }

    static class Task implements Runnable {

        private final AtomicLong counter;

        public Task(AtomicLong counter) {
            this.counter = counter;
        }

        @Override
        public void run() {
            counter.incrementAndGet(); // ++i
            //counter.getAndIncrement(); // i++
        }
    }
}

```



多线程并发访问，但是 AtomicLong 依然可以保证 incrementAndGet 操作的原子性，

所以不会发生线程安全问题

但深入一步去看内部情景的话，如图所示

![](img\20230101132401.png)



我们可以看到在这个图中，每一个线程是运行在自己的 core 中的，并且它们都有一个本地内存是自己独用的。在本地内存下方，有两个 CPU 核心共用的共享内存



对于 AtomicLong 内部的 value 属性而言，也就是保存当前 AtomicLong 数值的属性，它是被 volatile 修饰的，所以它需要保证自身可见性。



这样一来，每一次它的数值有变化的时候，它都需要进行 flush 和 refresh。比如说，如果开始时，ctr 的数值为 0 的话，那么如图所示，一旦 core 1 把它改成 1 的话，它首先会在左侧把这个 1 的最新结果给 flush 到下方的共享内存。然后，再到右侧去往上 refresh 到核心 2 的本地内存。这样一来，对于核心 2 而言，它才能感知到这次变化。



由于竞争很激烈，这样的 flush 和 refresh 操作耗费了很多资源，而且 CAS 也会经常失败。





#### LongAdder 带来的改进和原理

在 JDK 8 中又新增了 LongAdder 这个类，这是一个针对 Long 类型的操作工具类

```
package com.qin.demo.atomic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 描述：     在16个线程下使用AtomicLong
 */
public class LongAdderTest {

    public static void main(String[] args) throws InterruptedException {

        LongAdder counter = new LongAdder();

        ExecutorService service = Executors.newFixedThreadPool(16);

        for (int i = 0; i < 100; i++) {
            service.submit(new Task(counter));
        }

        Thread.sleep(2000);

        System.out.println(counter.sum());
    }

    static class Task implements Runnable {

        private final LongAdder counter;

        public Task(LongAdder counter) {
            this.counter = counter;
        }

        @Override
        public void run() {
            counter.increment();
        }
    }
}
```



上面的的代码和AtomicTest一样 ，但是运行速度比刚才 AtomicLong 的实现要快



为什么高并发下 LongAdder 比 AtomicLong 效率更高。



因为 LongAdder 引入了分段累加的概念，内部一共有两个参数参与计数：第一个叫作 base，它是一个变量，第二个是 Cell[] ，是一个数组。



其中的 base 是用在竞争不激烈的情况下的，可以直接把累加结果改到 base 变量上。



那么，当竞争激烈的时候，就要用到我们的 Cell[] 数组了。一旦竞争激烈，各个线程会分散累加到自己所对应的那个 Cell[] 数组的某一个对象中，而不会大家共用同一个。



这样一来，LongAdder 会把不同线程对应到不同的 Cell 上进行修改，降低了冲突的概率，这是一种分段的理念，提高了并发性，这就和 Java 7 的 ConcurrentHashMap 的 16 个 Segment 的思想类似。



竞争激烈的时候，LongAdder 会通过计算出每个线程的 hash 值来给线程分配到不同的 Cell 上去，每个 Cell 相当于是一个独立的计数器，这样一来就不会和其他的计数器干扰，Cell 之间并不存在竞争关系，所以在自加的过程中，就大大减少了刚才的 flush 和 refresh，以及降低了冲突的概率，这就是为什么 LongAdder 的吞吐量比 AtomicLong 大的原因，本质是空间换时间，因为它有多个计数器同时在工作，所以占用的内存也要相对更大一些。



那么 LongAdder 最终是如何实现多线程计数的呢？答案就在最后一步的求和 sum 方法，执行 LongAdder.sum() 的时候，会把各个线程里的 Cell 累计求和，并加上 base，形成最终的总和。代码如下：

```java
 public long sum() {
     Cell[] cs = cells;
     long sum = base;
     if (cs != null) {
         for (Cell c : cs)
             if (c != null)
                 sum += c.value;
     }
     return sum;
}
```



#### 如何选择

在低竞争的情况下，AtomicLong 和 LongAdder 这两个类具有相似的特征，吞吐量也是相似的，因为竞争不高。但是在竞争激烈的情况下，LongAdder 的预期吞吐量要高得多，经过试验，LongAdder 的吞吐量大约是 AtomicLong 的十倍，不过凡事总要付出代价，LongAdder 在保证高效的同时，也需要消耗更多的空间。



#### AtomicLong 可否被 LongAdder 替代？

那么我们就要考虑了，有了更高效的 LongAdder，那 AtomicLong 可否不使用了呢？是否凡是用到 AtomicLong 的地方，都可以用 LongAdder 替换掉呢？答案是不是的，这需要区分场景。



LongAdder 只提供了 add、increment 等简单的方法，适合的是统计求和计数的场景，场景比较单一，甚至都没赋值的方法

AtomicLong 还具有 compareAndSet 等高级方法，可以应对除了加减之外的更复杂的需要 CAS 的场景。



结论：如果我们的场景仅仅是需要用到加和减操作的话，那么可以直接使用更高效的 LongAdder，但如果我们需要利用 CAS 比如 compareAndSet 等操作的话，就需要使用 AtomicLong 来完成。



`LongAdder`更适合高并发、写操作远多于读操作的场景

`AtomicLong`更适合读远多于写，或线程数不多的场景



在低并发情况下，`AtomicLong`的读写效率跟`LongAdder`基本相同，甚至略优于`LongAdder`；
 `AtomicLong`的读效率总是优于`LongAdder`的；
 但`AtomicLong`的写效率会随着竞争的激烈程度线性降低，但`LongAdder`的写效率几乎可以保持地很好。

