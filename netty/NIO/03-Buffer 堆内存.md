### Buffer 堆内存

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

两种方式：也是NIO零拷贝的实现方式

1. FileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 5);
2. FileChannel.transferTo(long position, long count,WritableByteChannel target)

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









