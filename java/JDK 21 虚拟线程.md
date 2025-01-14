### java内存模型

Java内存模型即Java Memory Model，简称JMM（**一套规则**）

一套规则： 

八种操作

> lock(锁定)， unock(解锁)，read(读取)， load(载入)，use(使用)，assign(赋值)，store(存储)，write(写入)

同步规则

> - 不允许read和load、store和write操作之一单独出现
> - 不允许一个线程丢弃它的最近assign的操作，即变量在工作内存中改变了之后必须同步到主内存中
> - 不允许一个线程无原因的(没有发生过任何assign操作)把数据从工作内存同步回主内存中
> - 一个新的变量只能在主内存中诞生，不允许在工作内存中直接使用一个未被初始化(load或assign)的变量。即就是对一个变量实施use和store操作之前，必须先执行过了assign和load操作
> - 一个变量在同一时刻只允许一条线程对其进行lock操作，但lock操作可以被同一条线程重复执行多次，多次执行lock后，只有执行相同次数的unlock操作，变量才会解锁。lock和unlock必须成对出现
> - 如果对一个变量执行lock操作，将会清空工作内存中此变量的值，在执行引擎使用这个变量前需要重新执行load或assign操作初始化变量的值
> - 如果一个变量事先没有被lock操作锁定，则不允许对它执行unlock操作，也不允许去unlock一个被其他线程锁定的变量
> - 对一个变量执行unlock操作之前，必须先把此变量同步到主内存中(执行store和write操作)



简单理解：

一个线程仅能访问自己的线程栈，一个线程创建的本地变量对其它线程不可见，仅自己可见

每个线程拥有每个本地变量的独有版本

两个线程之间的通信必须 通过 JMM 规范 才能读取到

例子：

本地内存A和B有主内存中共享变量z的副本，线程A要修改 z 值，会先拿到本地内存空间然后修改

线程B需要通信时读取线程A的 z 值，需要线程A先会把自己本地内存中修改后的z值刷新到主内存中

随后，线程B才能从主内存中去读取线程A更新后的z值

java典型的例子 volatile

两个线程同时修改同一个对象的属性，都会复制一份变量到本地内存，修改相互之间是，无法感知的

经过volatile的修饰，修改变量值，会马上刷入主内存，然后主内存其他线程缓存失效，重新从主内存中读取





### 进程和线程的区别

进程是**资源管理**的基本单位，线程是**程序执行**的基本单位

（线程就是执行的，进程就是管理执行的）

简单比喻：

工厂 CPU 

一个车间 进程 

员工 线程

一个车间可以管理多个员工

车间空间是共享的：

车间有很多房间，工人可以进出

大房间里面的东西大家都可以用，这象征着进程空间是共享的，每个线程都可以使用

如果不想共享，可以加锁，防止互斥



### 虚拟线程

线程的成本消耗

两方面：

1. 一个创建于销毁 --> 解决方案：线程池
2. 频繁切换（并发越高导致月频繁）

virtual threads 就是解决频繁切换的问题



java 一个线程对应 一个系统线程 **（一一对应）**

virtual threads 多个对应一个 java 线程

本质上都在一个线程里面，没有切换的成本

在 IO 高的程序，提升非常明显

CPU密集的则没有提升（还降了一些）







### **直接创建虚拟线程并立即运行**

```
// 创建虚拟线程并立即运行
Thread vt = Thread.startVirtualThread(() -> {
    System.out.println("Start virtual thread...");
    Thread.sleep(10);
    System.out.println("End virtual thread.");
});

Thread.ofVirtual().start(() -> {
	System.out.println(Thread.currentThread());
});
```



### **创建虚拟线程但不立即运行**

```
// 创建虚拟线程，但不立即运行
Thread vt = Thread.ofVirtual().unstarted(() -> {
    System.out.println("Start virtual thread...");
    Thread.sleep(1000);
    System.out.println("End virtual thread.");
});
// 手动启动虚拟线程
vt.start();
```



### **通过ThreadFactory创建虚拟线程**

```
// 使用ThreadFactory创建虚拟线程
ThreadFactory tf = Thread.ofVirtual().factory();
Thread vt = tf.newThread(() -> {
    System.out.println("Start virtual thread...");
    Thread.sleep(1000);
    System.out.println("End virtual thread.");
});
// 启动虚拟线程
vt.start();
```



### **使用Executor调度虚拟线程**

```
// 创建Executor服务来调度虚拟线程
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

// 创建大量虚拟线程并提交到Executor中
for (int i = 0; i < 100000; i++) {
    executor.submit(() -> {
        Thread.sleep(1000);
        System.out.println("End virtual thread.");
        return true;
    });
}
```





### 在 SpringBoot 中，我们可以通过自定义 TaskExecutor 来支持虚拟线程。

```

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class VirtualThreadConfig {

    @Bean
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
```

通过 @Async 注解或者直接使用配置的 Executor 来执行任务

```
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class VirtualThreadService {

    @Async
    public void executeTask() {
        System.out.println("Task executed by: " + Thread.currentThread());
    }
}
```



手动提交任务

```
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class VirtualThreadTask {

    @Autowired
    private Executor taskExecutor;

    public void runTask() {
        taskExecutor.execute(() -> {
            System.out.println("Task executed by: " + Thread.currentThread());
        });
    }
}
```

