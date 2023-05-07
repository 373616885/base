### Buffer 堆内存

#### 关系

![](img\2022-07-31 215728.png)



#### HeapByteBuffer : 堆内内存

JVM原居民-受STW影响 （安全高效，GC介入管理）

#### DirectByteBuffer ： 堆外内存 

由于是堆外的性能提升（不需要GC回收，没有STW，效率更高），但风险也同时存在，没有管理好，造成内存泄漏，整个系统都会受影响

#### MappedByteBuffer ：文件映射内存

零拷贝内存映射





### 为什么要使用堆外内存

提升内存使用效率

堆内内存由于GC介入管理靠谱，安全高效，但是GC的STW无法避免，为了提升使用效率，就使用堆外内存

堆外内存没有GC的STW困惑，效率更高，但风险也存在，但只要正确使用就没有问题

#### 内存管理三部曲正确使用

1. 内存初始化
2. 内存数据读写
3. 内存释放





### DirectBuffer优缺点

DirectBuffer的读写操作比普通Buffer快，但它的创建、销毁却比普通Buffer慢，**因此一般对Direct ByteBuffer进行池化处理**

- DirectByteBuffer的创建就是使用了 malloc (动态内存分配) 申请的内存
- linux底层操作write、read、pwrite，pread函数进行系统调用时，需要连续的内存空间和固定的地址
- Java对象在Java堆里申请内存的时候，实际上是比malloc (动态内存分配) 要快的，所以DirectBuffer的创建效率往往是比Heap Buffer差的
- 但是，如果进行网络读写或者文件读写的时候，DirectBuffer就会比较快了，因为少了一次拷贝。因此一般对Direct ByteBuffer进行池化处理

原因：GC的时候要移动对象，地址会变，不能将GC堆的内存地址直接作为参数传给io系统调用，所以才会采用堆外中介缓冲区来实现（多做一次拷贝）

Socket 传输的时候：

DirectByteBuffer 本身数据符合writeFromNativeBuffer的调用，直接writeFromNativeBuffer操作内存

HeapByteBuffer 本身数据不符合writeFromNativeBuffer的调用，GC会使内存地址变化，

需要将数据复制到堆外中介缓冲区（固定的内存地址 Util.getTemporaryDirectBuffer）变成符合writeFromNativeBuffer调用 

所以多了一次拷贝

具体可以参考 sun.nio.ch.IOUtil#write和sun.nio.ch.IOUtil#read  

```java
static int write(FileDescriptor fd, ByteBuffer src, long position,
                 boolean directIO, boolean async, int alignment,
                 NativeDispatcher nd)
    throws IOException
{
    if (src instanceof DirectBuffer) {
        return writeFromNativeBuffer(fd, src, position, directIO, async, alignment, nd);
    }

    // Substitute a native buffer
    int pos = src.position();
    int lim = src.limit();
    assert (pos <= lim);
    int rem = (pos <= lim ? lim - pos : 0);
    ByteBuffer bb;
    if (directIO) {
        Util.checkRemainingBufferSizeAligned(rem, alignment);
        bb = Util.getTemporaryAlignedDirectBuffer(rem, alignment);
    } else {
        bb = Util.getTemporaryDirectBuffer(rem);
    }
    try {
        bb.put(src);
        bb.flip();
        // Do not update src until we see how many bytes were written
        src.position(pos);

        int n = writeFromNativeBuffer(fd, bb, position, directIO, async, alignment, nd);
        if (n > 0) {
            // now update src
            src.position(pos + n);
        }
        return n;
    } finally {
        Util.offerFirstTemporaryDirectBuffer(bb);
    }
}

static int read(FileDescriptor fd, ByteBuffer dst, long position,
                    boolean directIO, boolean async,
                    int alignment, NativeDispatcher nd)
        throws IOException
    {
        if (dst.isReadOnly())
            throw new IllegalArgumentException("Read-only buffer");
        if (dst instanceof DirectBuffer)
            return readIntoNativeBuffer(fd, dst, position, directIO, async, alignment, nd);

        // Substitute a native buffer
        ByteBuffer bb;
        int rem = dst.remaining();
        if (directIO) {
            Util.checkRemainingBufferSizeAligned(rem, alignment);
            bb = Util.getTemporaryAlignedDirectBuffer(rem, alignment);
        } else {
            bb = Util.getTemporaryDirectBuffer(rem);
        }
        try {
            int n = readIntoNativeBuffer(fd, bb, position, directIO, async, alignment, nd);
            bb.flip();
            if (n > 0)
                dst.put(bb);
            return n;
        } finally {
            Util.offerFirstTemporaryDirectBuffer(bb);
        }
    }
```







### 内存申请过程



#### HeapByteBuffer 内存对象初始化

```java
ByteBuffer.allocate(1024);

public static ByteBuffer allocate(int capacity) {
    if (capacity < 0)
        throw createCapacityException(capacity);
    return new HeapByteBuffer(capacity, capacity, null);
}
	
/*   
  最终: new byte[] 数组
 */
HeapByteBuffer(int cap, int lim, MemorySegmentProxy segment) {            // package-private
    super(-1, 0, lim, cap, new byte[cap], 0, segment);
    /*
        hb = new byte[cap];
        offset = 0;
        */
    this.address = ARRAY_BASE_OFFSET;
}
ByteBuffer(int mark, int pos, int lim, int cap,   // package-private
                 byte[] hb, int offset, MemorySegmentProxy segment)
{
    super(mark, pos, lim, cap, segment);
    this.hb = hb;
    this.offset = offset;
}
```

主要参数：

1. mark ：标记位
2. position：当前操作位置
3. limit：缓冲区终点
4. capacity：缓冲区数据容量
5. hb：hb数据缓冲区
6. offset：当前偏移量



#### DirectByteBuffer 内存对象初始化

```java
ByteBuffer allocateDirect(1024)

public static ByteBuffer allocateDirect(int capacity) {
	return new DirectByteBuffer(capacity);
}

// Primary constructor
//
DirectByteBuffer(int cap) {                   // package-private
    //1 调用父类构造器，没有传入保存数据的结构
    super(-1, 0, cap, cap, null);
    boolean pa = VM.isDirectMemoryPageAligned();
    int ps = Bits.pageSize();
    long size = Math.max(1L, (long)cap + (pa ? ps : 0));

    //2 记录内存相关信息
    Bits.reserveMemory(size, cap);

    long base = 0;
    try {
        // 3 分配内存地址
        base = UNSAFE.allocateMemory(size);
    } catch (OutOfMemoryError x) {
        Bits.unreserveMemory(size, cap);
        throw x;
    }
    UNSAFE.setMemory(base, size, (byte) 0);
    if (pa && (base % ps != 0)) {
        // Round up to page boundary
        address = base + ps - (base & (ps - 1));
    } else {
        address = base;
    }
    // 4 构建内存回收对象
    cleaner = Cleaner.create(this, new Deallocator(base, size, cap));
    att = null;

}

```





#### MappedByteBuffer  内存对象初始化

**两种方式：也是NIO零拷贝的实现方式**

Java可以通过java.nio.channels支持零复制 和 FileChannel的transferTo()方法支持零复制	（如果底层操作系统支持）。

1. FileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 5);  --nmap实现零拷贝（CPU还是有一次拷贝）
2. FileChannel.transferTo(long position, long count,WritableByteChannel target); --sendFiles方式实现零拷贝（真正的零拷贝）

```java

    public static void mappedByteBuffer() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("a.txt", "rw");
        FileChannel channel = randomAccessFile.getChannel();
        /**
         * 参数1：FileChannel.MapMode.READ_WRITE 使用读写模式
         * 参数2：内存起始位置
         * 参数3：映射内存的大小,即将a.txt的多少个字节映射到内存中
         * 超过大小，会报错 IndexOutOfBoundsException
         */
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);

        mappedByteBuffer.put(0, "Q".getBytes());
        mappedByteBuffer.put(1, "J".getBytes());
        mappedByteBuffer.put(2, "P".getBytes());
        // 报错 IndexOutOfBoundsException
        // mappedByteBuffer.put(5, "P".getBytes());
        randomAccessFile.close();
        System.out.println("修改结束");

    }

    public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }
```





Java的堆外内存回收设计是这样的：当GC发现DirectByteBuffer对象变成垃圾时，

会调用Cleaner#clean回收对应的堆外内存（虚引用去调用），一定程度上防止了内存泄露。

当然，也可以手动的调用该方法，对堆外内存进行提前回收。





### 内存读写过程

**需要注意：由写转读操作需要flip操作**

```java
import java.nio.IntBuffer;

public class BufferDemo {
    
 	public static void buffer( ) {
        // 创建buffer
        IntBuffer intBuffer = IntBuffer.allocate(5);
        // 写操作
        for (int i = 0; i < intBuffer.capacity(); i++) {
            intBuffer.put(i * 5);
        }
        // 由写转读操作需要flip操作
        // limit = position;
        // position = 0;
        // mark = -1;
        intBuffer.flip();

        while (intBuffer.hasRemaining()) {
            //顺序读
            int tmp = intBuffer.get();
            System.out.println("顺序读结果：" + tmp);
        }
        // 读取指定位置
        IntBuffer two = intBuffer.position(2);
        System.out.println("指定读位置：" + two.get());
        System.out.println("指定读位置：" + intBuffer.get(2));
    }
    
    public static void directBuffer(){
     // 创建buffer
     ByteBuffer buffer = ByteBuffer.allocateDirect(5);
     // 写操作
     for (int i = 0; i < buffer.capacity(); i++) {
         buffer.put((byte) i);
     }
     // 由写转读操作需要flip操作
     // limit = position;
     // position = 0;
     // mark = -1;
     buffer.flip();

     while (buffer.hasRemaining()) {
         //顺序读
         int tmp = buffer.get();
         System.out.println("顺序读结果：" + tmp);
     }
     // 读取指定位置
     ByteBuffer two = buffer.position(2);
     System.out.println("指定读位置：" + two.get());
     System.out.println("指定读位置：" + buffer.get(2));
 }
    
}

// 这个是DirectBuffer的put和get方法
public byte get() {
    try {
        return ((SCOPED_MEMORY_ACCESS.getByte(scope(), null, ix(nextGetIndex()))));
    } finally {
        Reference.reachabilityFence(this);
    }
}
public ByteBuffer put(byte x) {

    try {
        SCOPED_MEMORY_ACCESS.putByte(scope(), null, ix(nextPutIndex()), ((x)));
    } finally {
        Reference.reachabilityFence(this);
    }
    return this;
}

// 这是HeapByteBuffer的put和get方法
public int get() {
    return hb[ix(nextGetIndex())];
}

public IntBuffer put(int x) {
    hb[ix(nextPutIndex())] = x;
    return this;
}
```



**DirectBuffer的put和get方法 是通过 native 去调用的**

**HeapByteBuffer的put和get方法 直接操作数组的**



### 经典面试题

**什么是零拷贝**

官方回答：減少内核态往用户态文件拷贝的过程（**通过CPU来搬运数据**）

实现方式：程序运行在用户态的，用户态的应用程序并没有保存文件内容，不需要将文件拷贝过来，只需要保存指针，所有的文件变化，都是让内核去完成

进而减少上下文切换以及CPU的拷贝时间。它是一种IO操作优化技术。

**sendfile + DMA 实现的零拷贝**

**全程只发生了2次上下文切换以及2次数据拷贝，没有通过CPU来搬运数据，这就是真正的零拷贝技术，所有的数据都是通过DMA进行传输的。**

**DMA** : Direct  Memory   Access  直接存储器访问



### Buffer 堆外内存自动回收

HeapByteBuffer 交由GC回收就可以了

但DirectByteBuffer呢

![](img\2022-08-11 210437.png)



栈中存变量    堆中存外内存地址指针    指向堆外内存



#### 释放内存步骤

1. 创建Cleaner对象
2. 调用Cleaner的Deallocator
3. 虚引用自动回收机制（建议手动回收）



#### 创建Cleaner对象

cleaner = Cleaner.create(this, new Deallocator(base, size, cap));

```java
DirectByteBuffer(int cap) {                   // package-private

    super(-1, 0, cap, cap, null);
    boolean pa = VM.isDirectMemoryPageAligned();
    int ps = Bits.pageSize();
    long size = Math.max(1L, (long)cap + (pa ? ps : 0));
    Bits.reserveMemory(size, cap);

    long base = 0;
    try {
        base = UNSAFE.allocateMemory(size);
    } catch (OutOfMemoryError x) {
        Bits.unreserveMemory(size, cap);
        throw x;
    }
    UNSAFE.setMemory(base, size, (byte) 0);
    if (pa && (base % ps != 0)) {
        // Round up to page boundary
        address = base + ps - (base & (ps - 1));
    } else {
        address = base;
    }
    // 关键代码
    // this注册清理容器中 并且设置需要清理对象的引用Deallocator
    cleaner = Cleaner.create(this, new Deallocator(base, size, cap));
    att = null;
}
   
Deallocator类
 private static class Deallocator
        implements Runnable
    {

        private long address;
        private long size;
        private int capacity;

        private Deallocator(long address, long size, int capacity) {
            assert (address != 0);
            this.address = address;
            this.size = size;
            this.capacity = capacity;
        }
		// 关键代码
        public void run() {
            if (address == 0) {
                // Paranoia
                return;
            }
            UNSAFE.freeMemory(address);
            address = 0;
            Bits.unreserveMemory(size, capacity);
        }

    }    
    

```



#### 调用Cleaner的Deallocator

```java
 /**
     * Runs this cleaner, if it has not been run before.
     */
Cleaner类
    @SuppressWarnings("removal")
    public void clean() {
        if (!remove(this))
            return;
        try {
            // 这里调用Deallocator类关键代码：释放内存
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


```

​                 

#### 虚引用自动回收机制（建议手动回收）

##### 手动回收

```java

import sun.nio.ch.DirectBuffer;
import java.nio.ByteBuffer;
// 自己写的手动回收
public static void chean(ByteBuffer buffer) {
    if(buffer.isDirect()) {
        // DirectBuffer 是sun.nio包下的
        ((DirectBuffer)buffer).cleaner().clean();
    } else {
        buffer.clear();
    }

}
```



##### 虚引用自动回收 

Cleaner 对象继承了虚引用PhantomReference

当Reference引用的对象被GC回收的时候就会将对象加入到ReferenceQueue

一个专门的线程（线程等级最高的守护线程）不断扫描然后去执行 Cleaner对象的clean方法

```java
public class Cleaner
    extends PhantomReference<Object> {
    
	private Cleaner(Object referent, Runnable thunk) {
        // 调用父类
        super(referent, dummyQueue);
        this.thunk = thunk;
    }
}
public class PhantomReference<T> extends Reference<T> {
    
}

public abstract class Reference<T> {
    static {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        for (ThreadGroup tgn = tg;
             tgn != null;
             tg = tgn, tgn = tg.getParent());
        // ReferenceHandler线程处理器
        // 里面一直 while (true) 去调用Cleaner对象的clean方法
        Thread handler = new ReferenceHandler(tg, "Reference Handler");
        /* If there were a special system-only priority greater than
         * MAX_PRIORITY, it would be used here
         */
        // 线程等级最高
        handler.setPriority(Thread.MAX_PRIORITY);
        // 生命周期随JVM--守护线程
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
    
    
     /* High-priority thread to enqueue pending References
     */
    private static class ReferenceHandler extends Thread {

        private static void ensureClassInitialized(Class<?> clazz) {
            try {
                Class.forName(clazz.getName(), true, clazz.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw (Error) new NoClassDefFoundError(e.getMessage()).initCause(e);
            }
        }

        static {
            // pre-load and initialize Cleaner class so that we don't
            // get into trouble later in the run loop if there's
            // memory shortage while loading/initializing it lazily.
            ensureClassInitialized(Cleaner.class);
        }

        ReferenceHandler(ThreadGroup g, String name) {
            super(g, null, name, 0, false);
        }

        public void run() {
            // 一直循环Cleaner对象的clean方法
            while (true) {
                processPendingReferences();
            }
        }
    }
    
     private static void processPendingReferences() {
        // Only the singleton reference processing thread calls
        // waitForReferencePendingList() and getAndClearReferencePendingList().
        // These are separate operations to avoid a race with other threads
        // that are calling waitForReferenceProcessing().
        waitForReferencePendingList();
        Reference<?> pendingList;
        synchronized (processPendingLock) {
            pendingList = getAndClearReferencePendingList();
            processPendingActive = true;
        }
        while (pendingList != null) {
            Reference<?> ref = pendingList;
            pendingList = ref.discovered;
            ref.discovered = null;

            if (ref instanceof Cleaner) {
                ((Cleaner)ref).clean();
                // Notify any waiters that progress has been made.
                // This improves latency for nio.Bits waiters, which
                // are the only important ones.
                synchronized (processPendingLock) {
                    processPendingLock.notifyAll();
                }
            } else {
                ref.enqueueFromPending();
            }
        }
        // Notify any waiters of completion of current round.
        synchronized (processPendingLock) {
            processPendingActive = false;
            processPendingLock.notifyAll();
        }
    }
    
}

```





### ByteBuffer的`大小如何分配`的注意点

**需要设计自行设置一个大小可变的ByteBUffer**

> 每个Channel都需要记录可能被切分的消息，因为ByteBuffer不能够被多个Channel共同使用，因此需要为每个channel维护一个独立的ByteBUffer

- ByteBuffer不能太大，比如一个ByteBuffer1Mb的话，需要支持百万连接就要1Tb内存，因此需要设计大小可变的ByteBUffer

- - 思路一：首先分配一个较小的buffer，例如4k，如果发现数据不够，再分配8k的buffer，将4kbuffer内容拷贝至8k的buffer，优点是消息连续容易处理，缺点是数据拷贝耗费性能。
  - 思路二：用多个数组组成buffer，一个数组不够，把多出来的内容写入新的数组，与前面的区别是消息存储不连续解析复杂，优点是避免了拷贝引起的性能损耗





### Cleaner的使用

CleanDemo回收的时候，属性cleaner也被回收

GC回收到cleaner的时候，会触发Cleaner的clean方法

这时候CleanDemo可能已经被回收了，与Cleaner没有关系

```java
package com.qin;

import jdk.internal.ref.Cleaner;

public class CleanDemo {
    private final Cleaner cleaner;

    public CleanDemo() {
        cleaner = Cleaner.create(this, () -> {
            System.out.println("GC --clean");
        });
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 1000; i++) {
            CleanDemo ob = new CleanDemo();
            Thread.sleep(1000);
            System.out.println("ob==");
            if(i % 2 == 0){
                System.gc();
            }
        }
    }
}

Cleaner的clean方法
public void clean() {
        if (!remove(this))
            return;
        try {
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







### 经典面试题 

比较Cleaner回收机制和Finalize回收机制

1. 从 Java 9 开始，Finalizer 机制已被弃用
2. Java 9 中 Cleaner 机制代替了 Finalizer 机制
3. Finalizer 和 Cleaner 机制的一个缺点是不能保证他们能够及时执行
4. Finalizer机制线程的运行优先级低于其他应用程序线程，Cleaner机制线程的运行优先级最高
5. 使用 finalizer 和 cleaner 机制都会导致严重的性能损失
6. **除了作为一个安全网或者终止非关键的本地资源，不要使用 Cleaner 机制**



### 代码



```java

import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class BufferDemo {

    public static void buffer( ) {
        // 创建buffer
        IntBuffer intBuffer = IntBuffer.allocate(5);
        // 写操作
        for (int i = 0; i < intBuffer.capacity(); i++) {
            intBuffer.put(i * 5);
        }
        // 由写转读操作需要flip操作
        // limit = position;
        // position = 0;
        // mark = -1;
        intBuffer.flip();

        while (intBuffer.hasRemaining()) {
            //顺序读
            int tmp = intBuffer.get();
            System.out.println("顺序读结果：" + tmp);
        }
        // 读取指定位置
        IntBuffer two = intBuffer.position(2);
        System.out.println("指定读位置：" + two.get());
        System.out.println("指定读位置：" + intBuffer.get(2));

    }


    public static void directBuffer(){
        // 创建buffer
        ByteBuffer buffer = ByteBuffer.allocateDirect(5);
        // 写操作
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.put((byte) i);
        }
        // 由写转读操作需要flip操作
        // limit = position;
        // position = 0;
        // mark = -1;
        buffer.flip();

        while (buffer.hasRemaining()) {
            //顺序读
            int tmp = buffer.get();
            System.out.println("顺序读结果：" + tmp);
        }
        // 读取指定位置
        ByteBuffer two = buffer.position(2);
        System.out.println("指定读位置：" + two.get());
        System.out.println("指定读位置：" + buffer.get(2));
    }

    public static void chean(ByteBuffer buffer) {
        if(buffer.isDirect()) {
            ((DirectBuffer)buffer).cleaner().clean();
        } else {
            buffer.clear();
        }

    }



    public static void main(String[] args) {
        directBuffer();
    }


}


```



```java
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MappedByteBufferDemo {

    public static void mappedByteBuffer() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("a.txt", "rw");
        FileChannel channel = randomAccessFile.getChannel();
        /**
         * 参数1：FileChannel.MapMode.READ_WRITE 使用读写模式
         * 参数2：内存起始位置
         * 参数3：映射内存的大小,即将a.txt的多少个字节映射到内存中
         * 超过大小，会报错 IndexOutOfBoundsException
         */
        MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, 5);

        mappedByteBuffer.put(0, "Q".getBytes());
        mappedByteBuffer.put(1, "J".getBytes());
        mappedByteBuffer.put(2, "P".getBytes());
        // 报错 IndexOutOfBoundsException
        // mappedByteBuffer.put(5, "P".getBytes());
        randomAccessFile.close();
        System.out.println("修改结束");

    }

    public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

}

```

