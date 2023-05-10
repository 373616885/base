![](img\2023-04-03 000000.png)



## 方法级的 synchronized

方法级的同步：

是隐式的，即无需通过字节码指令来控制，它实现在方法调用和返回操作之中。虚拟机可以从方法常量池的方法表结构中的 ACC_SYNCHRONIZED 访问标志得知一个方法是否声明为同步方法



当调用方法时，调用指令将会检查方法的 ACC_SYNCHRONIZED 访问标志是否设置

- 如果设置了，执行线程将先持有同步锁，然后执行方法，最后在方法完成(无论是正常完成还是非正常完成)时释放同步锁
- 在方法执行期间，执行线程持有了同步锁，其它任何线程都无法再获得同一个锁
- 如果一个同步方法执行期间抛出了异常，并且在方法内部无法处理此异常，那么这个同步方法所持有的锁将在异常抛到同步方法之外时自动释放



总结：

方法级的 synchronized 是JVM内部自己控制的，通过 方法的ACC_SYNCHRONIZED 访问标志得知

一个线程只要持有同步锁，其它任何线程都无法进入

锁的当前对象 this 的对象头的 锁信息和锁持有者部分

```java
private int i = 0;
public synchronized void method1() {
    i++;
}
```

![](img\1606317801318.png)





## 方法内 的 synchronized

通过 monitorenter 和 monitorexit 两条指令来支持 synchronized 

当一个线程进入同步代码块时，它使用 monitorenter 指令请求进入。如果当前对象的监视器计数器为0，则它会被准许进入，若为1，则判断持有当前监视器的线程是否为自己，如果是，则进入，否则进行等待，知道对象的监视器计数器为0，才会被允许进入同步块

```java
Object obj = new Object();
public void method3() {
    // obj：当前对象的监视器计数器
    // 在对象头里面
    synchronized (obj) {
        i++;
    }
}
```

```java
  public void method3(); 
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=3, locals=3, args_size=1
         0: aload_0
         1: getfield      #13                 // Field obj:Ljava/lang/Object;
         4: dup
         5: astore_1
         6: monitorenter
         7: aload_0
         8: dup
         9: getfield      #7                  // Field i:I
        12: iconst_1
        13: iadd
        14: putfield      #7                  // Field i:I
        17: aload_1
        18: monitorexit
        19: goto          27
        22: astore_2
        23: aload_1
        24: monitorexit
        25: aload_2
        26: athrow
        27: return
      Exception table:
         from    to  target type
    		//#如果7-19  monitorexit 失败 跳到22
            //保证  7-19  monitorexit 一定被执行
             7    19    22   any 
            // 22-25 就保证 monitorexit 失败 继续到22循环执行
             // 保证 monitorexit 一定被执行到
            22    25    22   any  
             				
monitorenter 之后，为了防止异常没有执行monitorexit
加了Exception table的处理，保证一定会执行monitorexit
```







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

此时线程2也执行判断实例不为空，直接执行到了37步，得到了静态变量引用，然后return，之后开始马上使用这个对象。

与此同时线程1的 21（初始化）还没有完成。

所以问题就是线程2使用的对象可能是未完全初始化的对象。

不过之后就好了，就耽搁了一小会儿（根本无伤大雅，大题小做，加了还消耗性能呢）

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

