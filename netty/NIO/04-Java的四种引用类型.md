### Java的四种引用类型

1. 强引用：Strong Reference 
2. 软引用：SoftReference
3. 弱引用：WeakReference 
4. 虚引用PhantomReference



| 类型   | 垃圾回收时间     | 用途                      | 生存时间       |
| ------ | ---------------- | ------------------------- | -------------- |
| 强引用 | 不会被回收       | 正常编码使用              | 随JVM          |
| 软引用 | 内存不够了，被GC | 可作为缓存                | 内存不足时终止 |
| 弱引用 | GC发生时         | 可作为缓存（WeakHashMap） | 垃圾回收后终止 |
| 虚引用 | 任何时候         | 监控对象回收，记录日志    | 垃圾回收后终止 |





### 强引用 Strong Reference 

常见的引用类型，普通的变量引用

```java
public static User user = new User();
```

只要强引用关系还存在，垃圾收集器就永远不会回收掉被引用的对象

宁愿抛出 OOM 错误，使程序异常终止，也不会靠随意回收具有强引用的对象来解决内存不足问题





###  软引用 SoftReference

如果内存空间足够，垃圾回收器不会回收它，如果不足就会回收这些对象的内存

内存空间接近临界值、JVM 即将抛出oom的时候，垃圾回收器才会将该引用对象进行回收，避免了系统内存溢出的情况

软引用可用来实现内存敏感的高速缓存,比如网页缓存、图片缓存等 高速缓存, 防止内存泄露

```java
public class SoftRefence {
    public static void main(String[] args) {
        SoftReference<String> pen = new SoftReference<String>(new String("penguin"));
        System.out.println(pen.get());
        //通知JVM进行内存回收
        System.gc();
        // 内存足-还在
        System.out.println(pen.get());
    }
}

penguin
penguin
```





### 弱引用 WeakReference

可有可无

弱引用也是用来描述那些非必须对象

被弱引用关联的对象只能生存到下一次垃圾收集发生为止（线程优先级低，可以重新声明为强引用）

当垃圾收集器开始工作，无论当前内存是否足够，都会回收掉只 被弱引用关联的对象

```java
public class Weak {
    public static void main(String[] args) {
 
        WeakReference<String> str = new WeakReference<String>(new String("penguin"));
        System.out.println(str.get());
        //通知JVM进行内存回收
        System.gc();
 		// 只有发生GC就没了
        System.out.println(str.get()); // null
    }
}
```





### 虚引用 PhantomReference

形同虚设

和没有一样：如何时候都可以被垃圾回收器回收

若某个对象与虚引用关联，那么在任何时候都可能被JVM回收掉。虚引用不能单独使用，必须配合引用队列一起使用

虚引用存在于每一个对象里面，不会对对象的存活造成任何影响

**唯一用处就是：能在对象被GC时收到系统通知**

```java
public class Phantom {
    public static void main(String args[]) {
 
        ReferenceQueue<String> queue = new ReferenceQueue<String>();
        PhantomReference<String> str = new PhantomReference<String>("abc", queue);
        // 由于虚引用的引用始终不可访问，因此此方法始终返回null 
        System.out.println(str.get());// 永远null
    }
}
```





### Java引用机制的整体流程



**GC会在不同的资源条件下对不同的Java弱引用做不同的操作**



1. 当JVM中发生垃圾收集的时候，如果GC发现referent是弱可达的（四种弱可达的对象），那么GC会将封装了该referent的java.lang.ref.Reference及其子类的引用对象挂到pending-reference list上，这条链表是由GC维护的
2. 当GC将Reference对象挂到pending-reference list上时，发生一次线程间通信，会通知ReferenceHandler线程来取走这些节点并进行后续处理；
3. ReferenceHandler线程拿到节点的后续处理如下:

​			1. 如果节点Reference是jdk.internal.ref.Cleaner的实例，那么调用其方法jdk.internal.ref.Cleaner.clean()

​            2. 如果是其他的引用类型的实例（SoftReference、WeakReference、PhantomReference、FinalReference），那么拿到其实例化时构造函数中传入的ReferenceQueue并调用该队列的enqueue函数，将引用对象入队列: 

​							1. 对于SoftReference和WeakReference，入队列其实没有太大意义因为这俩对应着应用对Java堆内存的两种不同程度的需求，而Java堆内存的释放会由GC自动管理，所以通常使用软引用和弱引用的时候不会关联ReferenceQueue；

​							2. 对于PhantomReference，对应着应用对其他资源的释放需求（例如堆外native memory、文件句柄、socket端口等），几乎没有直接使用PhantomReference的，而都是用其子类jdk.internal.ref.Cleaner，在ReferenceHandler中调用clean方法执行事先写好的释放资源的代码逻辑；

​							3. 对于FinalReference，也几乎没有直接使用该类的，而都是直接使用其子类java.lang.ref.Finalizer，在队列中会发生第二次线程间通信，ReferenceHandler线程在将Reference对象入队列时，会通知FinalizerThread做进一步的处理——即调用FinalReference封装的referent重写的java.lang.Object的finalize()方法；


总结：

- 生产者-消费者模型：GC --> pending-reference list --> ReferenceHandler
- 生产者：GC
- 产品：引用对象（封装的referent失去强引用的java.lang.ref.Reference实例，或者说其封装的referent的可达性从强可达变成其中一种弱可达的引用对象）
- 仓库：pending-reference list
- 消费者：ReferenceHandler线程
- 该模型适用于：Soft Reference、Weak Reference、Phantom Reference、jdk.internal.ref.Cleaner
  



### Reference状态机制 

active ：当Reference对象刚刚被创建且没有发生GCROOT检查时 

pending：active 发送GC,有制定ReferenceQueue参数的转移将对象放到pending-Reference list里，等待ReferenceHandler处理  

enqueued : 相应的对象已经为待回收，而且相应的引用对象已经放到queue当中了,等待ReferenceHandler线程中会拿到引用对象的队列并将引用进栈，并将Reference的queue属性修改为ReferenceQueue.EQNUEUE，调用ReferenceQueue.enqueue()方法，处于这个状态

inactive：即此对象已经由外部从queue中获取到，而且已经处理掉了。即意味着此引用对象能够被回收，而且对内部封装的对象也能够被回收掉了( 实际的回收运行取决于clear动做是否被调用 )。简单理解进入到此状态的可以被回收掉的。一旦一个Reference实例变为了Inactive，它的状态将不会再改变



![](img\2022-08-12 060654.png)







