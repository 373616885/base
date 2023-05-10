![](img\2023-04-03 000000.png)



## synchronized的原子性

**synchronized 底层实际上通过JVM来实现**的，同一时间只能有一个线程去执行synchronized 中的代码块。

**原子性**：既然**同一时间只有一个线程去运行里面的代码**，那么**这个操作就是不能被其它线程打断的，所以这里天然就具有原子性了**



## synchronized通过内存屏障保证可见性

volatile是**通过内存屏障来保证可见性的**，

**Load屏障**保证volatile变量每次读取数据的时候**都强制从主内存读取**；

**Store屏障**每次volatile**修改之后强制将数据刷新会主内存**。



synchronized底层是**通过monitorenter的指令来进行加锁的、通过monitorexit指令来释放锁的**

monitorenter 指令其实也具有Load屏障的作用

读从主内存读

修改之后强制将数据刷新会主内存，并通知别的线程重新读取主内存的数据

![](img\2023-04-03 000001.png)



## synchronized使用内存屏障来保证有序性

synchronized 内部的代码禁止被优化到外部，串行执行，相对于多线程而言是有序的

**注意: synchronized无法禁止内部指令重排**

![](img\2023-04-03 000002.png)







## synchronized 修改mark world的monitor信息来加锁

synchronized  通过CAS自旋的方式修改mark world的monitor信息

简称：对象的监视器计数器





静态同步方法锁的是T.class对象

非静态同步方法锁的是this对象

代码块锁的相应的对象





## 单例： double check + volatile

```java
public class Singleton{
    private static Singleton instance;
    private  Singleton(){}
    public static Singleton getInstance(){
        if(instance==null){
            synchronized (Singleton.class){
                if(instance==null){ 
                    instance=new Singleton();
                }
            }
        }
        return instance;
    }
}



 0 getstatic #7 <com/qin/Singleton.instance : Lcom/qin/Singleton;>
 3 ifnonnull 37 (+34)
 6 ldc #8 <com/qin/Singleton>
 8 dup
 9 astore_0
10 monitorenter
11 getstatic #7 <com/qin/Singleton.instance : Lcom/qin/Singleton;>
14 ifnonnull 27 (+13)
17 new #8 <com/qin/Singleton>
20 dup
21 invokespecial #13 <com/qin/Singleton.<init> : ()V>
24 putstatic #7 <com/qin/Singleton.instance : Lcom/qin/Singleton;>
27 aload_0
28 monitorexit
29 goto 37 (+8)
32 astore_1
33 aload_0
34 monitorexit
35 aload_1
36 athrow
37 getstatic #7 <com/qin/Singleton.instance : Lcom/qin/Singleton;>
40 areturn

```

- 17:new 一个对象
- 20：复制一份对象引用//地址
- 21:利用一个对象引用，调用构造方法。//根据引用地址调用
- 24:表示利用一个对象引用，赋值给static instance

JVM 会优化，可能会先执行24,在执行21。即先赋值，再引用。

因为加了synchronized相当于是单线程。没有问题的。

但比如线程1获得到了锁，先执行24，在执行21。

此时线程2也执行判断实例不为空，直接执行到了37步，得到了静态变量引用，然后return，之后开始使用这个对象了。

与此同时线程1的 21（初始化）还没有完成。

所以问题就是线程2使用的对象可能是未完全初始化的对象。

不过之后就好了，就耽搁了一小会儿（根本无伤大雅，大题小做，加了）

CON50-J 明确规定不要加 volatile



解决这个有序性问题的办法，就是禁止处理器优化和指令重排，

使用 volatile 防止指令重排（还可以保证内存的可见性）

```java
public class Singleton{
    private volatile static Singleton instance;
    private Singleton(){}
    public static Singleton getInstance(){
        if(instance==null){
            synchronized (Singleton.class){
                if(instance==null){ 
                    instance=new Singleton();
                }
            }
        }
        return instance;
    }
}
```

