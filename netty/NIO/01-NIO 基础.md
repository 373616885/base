### NIO 三大核心

#### Selector 多路复用器

#### Buffer 缓冲区

#### Channel 通道



### NIO 的整体流程

用一个线程管理多个客户端连接

![](img\2022-07-20 162541.png)



### 什么是多路复用

NIO相较于BIO,使用多路复用器，可以一个线程管理所有客户端连接

而BIO需要对每个连接创建一个线程

Java 中一个线程管理多个Sockect对象，不叫多路复用

```java
List<Sockect> allSocket = new ArrayList<>;
while(true) {
    Sockect sockect = serverSockect.accpet();
    new Thead(() -> doWork(sockect)).start()
}
void doWork(Sockect sockect) {
    // 新线程处理连接任务
}
```

多路复用是操作系统内核的概念并不是一种实现效果

简单认为：只有使用了select ，pull , epull 三个系统调用才能称为多路复用

 

