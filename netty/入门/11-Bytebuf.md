### Bytebuf

从底层读取数据到Bytebuf 

应用程序处理完成之后，再次封装成Bytebuf 写入底层



### 常见问题

内存的类别有哪些？

Pooled 和 Unpooled 

Head 和 DIrect

unsafe 和 非unsafe



如何减少多线程内存的分配之间的竞争？

TheadLocal原理，一个Thead分配一个Arena 



不同大小的内存如何分配？



内存与内存管理的抽象



不同规格的大小和不同内存的分配策略



内存的回收过程





### ByteBuf 结构



```html
 <pre>
      +-------------------+------------------+------------------+
      | discardable bytes |  readable bytes  |  writable bytes  |
      |                   |     (CONTENT)    |                  |
      +-------------------+------------------+------------------+
      |                   |                  |                  |
      0      <=      readerIndex   <=   writerIndex    <=    capacity
</pre>
```





0      <=      readerIndex 这段数据是无效的

readerIndex   <=   writerIndex  这段数据是可读的

writerIndex    <=    capacity 这段数据是可写的



当capacity不够的时候，netty会提前进行扩容

还有一个maxCapacity这个是最大的容量，扩容不能超过这个，一般设置成很大



```java
//增加引用计数
//ByteBuf使用了引用计数，缺省下读取一次之后refCnt就会减到0，再读就出现异常了。
//0 时对象会被回收
//如果使用retain()可以增加引用计数，可以多读一次
//相反，release()则减一次，可能还没有读就不能再读了
byte[] req = new byte[buf.readableBytes()];
buf.readBytes(req);
String body = new String(req,"UTF-8");
buf.retain();

byte[] bytes = ByteBufUtil.getBytes(buf);

buf.retain();
```





### 重要API

read  ,write  ,set 

mask  ,reset



- ByteBuf 维护了 readerIndex 和 writerIndex 索引。
- 当 readerIndex > writerIndex 时，则抛出 IndexOutOfBoundsException。
- ByteBuf 容量 = writerIndex。
- ByteBuf 可读容量 = writerIndex - readerIndex。
- readXXX () 和 writeXXX () 方法将会推进其对应的索引，自动推进。
- getXXX () 和 setXXX () 方法对 writerIndex 和 readerIndex 无影响，不会改变 index 值
- mask 标记当前指针 markReaderIndex 和 markWriterIndex
- reset 恢复到之前的mask指针 resetReaderIndex 和 resetWriterIndex
- readableBytes 当前读指针
- writableBytes 当前写指针





### ByteBuf 

![](img\2022-09-07 061450.png)



#### AbstractByteBuf 对 ByteBuf 的抽象实现

```java
public abstract class AbstractByteBuf extends ByteBuf {
    //几个重要变量
	int readerIndex;
    int writerIndex;
    private int markedReaderIndex;
    private int markedWriterIndex;
    private int maxCapacity;
    
    //读从readerIndex开始读一个字节，然后readerIndex加一
    public byte readByte() {
        checkReadableBytes0(1);
        //拿到当前readerIndex
        int i = readerIndex;
        //读一个字节
        //_getByte交给子类实现
        byte b = _getByte(i);
        //readerIndex加一
        readerIndex = i + 1;
        return b;
    } 
    
    //写指针
    public ByteBuf writeByte(int value) {
        ensureWritable0(1);
        //交给子类实现
        _setByte(writerIndex++, value);
        return this;
    }
    
    //获取字节，但读指针不变
    public byte getByte(int index) {
        checkIndex(index);
        return _getByte(index);
    }
    
}
```



### ByteBuf 分配

创建和管理 ByteBuf 实例的多种方式：

按需分配 (ByteBufAllocator)  -- netty 根据应用自动为你选择最优的分配方案

Pooled 和 Unpooled  缓冲区

ByteBufUtil 类







### ByteBuf 的分类  

Pooled 和 Unpooled 

对于 Java 程序，默认使用 PooledByteBufAllocator (池化)

对于安卓，默认使用 UnpooledByteBufAllocator (非池化)



unsafe 和 非unsafe

unsafe： jvm底层能拿到对象具体的内存地址的对象,然后基于内存地址可以进行读写操作

非unsafe ：不依赖jvm底层的unsafe对象，Unsafe对象不能直接调用，只能通过反射获得



Head 和 DIrect

Head ：直接在jvm堆上进行分配的，GC可以直接回收的

DIrect：直接调用堆外内存，不受GC影响的 



PooledHeapByteBuf ： 

PooledUnsafeHeapByteBuf：

PooledDirectByteBuf： 

PooledUnsafeDirectByteBuf：



UnpooledHeapByteBuf：

UnpooledUnsafeHeapByteBuf：

UnpooledDuplicatedByteBuf：

UnpooledUnsafeDirectByteBuf：



### ByteBufAllocator 内存分配管理器 

ByteBufAllocator 顶层接口：实现负责分配缓冲区。该接口的实现应该是线程安全的

```java
//顶层接口：实现负责分配缓冲区。该接口的实现应该是线程安全的
public interface ByteBufAllocator {

    ByteBufAllocator DEFAULT = ByteBufUtil.DEFAULT_ALLOCATOR;

    .........
}

```



#### AbstractByteBufAllocator 

骨架ByteBufAllocator的基本实现

通过暴露：newHeapBuffer 和 newDirectBuffer 让子类自己去实现

两大子类具体实现类：PooledByteBufAllocator和UnpooledByteBufAllocator	

PooledByteBufAllocator.newHeapBuffer
UnpooledByteBufAllocator.newHeapBuffer
    
PooledByteBufAllocator.newDirectBuffer
UnpooledByteBufAllocator.newDirectBuffer



```java
//骨架ByteBufAllocator的基本实现
public abstract class AbstractByteBufAllocator implements ByteBufAllocator {

    public ByteBuf heapBuffer(int initialCapacity, int maxCapacity) {
        if (initialCapacity == 0 && maxCapacity == 0) {
            return emptyBuf;
        }
        validate(initialCapacity, maxCapacity);
        return newHeapBuffer(initialCapacity, maxCapacity);
    }
    
    public ByteBuf directBuffer(int initialCapacity, int maxCapacity) {
        if (initialCapacity == 0 && maxCapacity == 0) {
            return emptyBuf;
        }
        validate(initialCapacity, maxCapacity);
        return newDirectBuffer(initialCapacity, maxCapacity);
    }
    
	
    protected abstract ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity);

   
    protected abstract ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity);

}
```





### UnpooledByteBufAllocator内存分配器

```java
//UnpooledByteBufAllocator内存分配器
public final class UnpooledByteBufAllocator extends AbstractByteBufAllocator implements ByteBufAllocatorMetricProvider {

	@Override
    protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
        // 底层帮你判断是否有unsafe对象（这个对象只能反射获取）
        // 有unsafe对象：UnpooledUnsafeHeapByteBuf
        // 无unsafe对象：UnpooledHeapByteBuf
        // UnpooledHeapByteBuf: 
        //    new byte[initialCapacity] new一个byte数组
        // 	  memory[index] 通过数组下标获取数据	
        // UnpooledUnsafeHeapByteBuf:
        //    UNSAFE.getByte(data, BYTE_ARRAY_BASE_OFFSET + index); 通过UNSAFE获取数据
        //    内存地址加上偏移量计算
        return PlatformDependent.hasUnsafe() ?
                new InstrumentedUnpooledUnsafeHeapByteBuf(this, initialCapacity, maxCapacity) :
                new InstrumentedUnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
    }

    @Override
    protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
        final ByteBuf buf;
        //底层帮你判断是否有unsafe对象（这个对象只能反射获取）
        //jdk底层调用：ByteBuffer.allocateDirect(initialCapacity)
        //有unsafe，通过UNSAFE.getByte(data, BYTE_ARRAY_BASE_OFFSET + index); 通过UNSAFE获取数据
        //有unsafe内存地址加上偏移量去计算
        if (PlatformDependent.hasUnsafe()) {
            //有则UnpooledUnsafeDirectByteBuf
            //noCleaner有没有Cleaner回收对象
            buf = noCleaner ? new InstrumentedUnpooledUnsafeNoCleanerDirectByteBuf(this, initialCapacity, maxCapacity) :
                    new InstrumentedUnpooledUnsafeDirectByteBuf(this, initialCapacity, maxCapacity);
        } else {
            // 没有UnpooledDirectByteBuf
            buf = new InstrumentedUnpooledDirectByteBuf(this, initialCapacity, maxCapacity);
        }
        return disableLeakDetector ? buf : toLeakAwareBuffer(buf);
    }

}
```







### PooledByteBufAllocator内存分配器

```java
//PooledByteBufAllocator内存分配器
public class PooledByteBufAllocator extends AbstractByteBufAllocator implements ByteBufAllocatorMetricProvider {

    protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
        //每个线程都有自己的内存分配器--通过FastThreadLocal获取自己的内存分配器
        //里面有一个heapArena分配heap的
        //还有一个directArena分配direct的
        //默认创建2倍CPU核数的heapArena数组和directArena数组
        PoolThreadCache cache = threadCache.get();
        //在局部线程缓存的Arena上进行内存分配
        PoolArena<ByteBuffer> directArena = cache.directArena;

        final ByteBuf buf;
        if (directArena != null) {
            // 池中获取
            buf = directArena.allocate(cache, initialCapacity, maxCapacity);
        } else {
            buf = PlatformDependent.hasUnsafe() ?
                UnsafeByteBufUtil.newUnsafeDirectByteBuf(this, initialCapacity, maxCapacity) :
            new UnpooledDirectByteBuf(this, initialCapacity, maxCapacity);
        }

        return toLeakAwareBuffer(buf);
    }

    protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
        //每个线程都有自己的内存分配器--通过FastThreadLocal获取自己的内存分配器
        //里面有一个heapArena分配heap的
        //还有一个directArena分配direct的
        //默认创建2倍CPU核数的heapArena数组和directArena数组
        PoolThreadCache cache = threadCache.get();
        //在局部线程缓存的Arena上进行内存分配
        PoolArena<byte[]> heapArena = cache.heapArena;

        final ByteBuf buf;
        if (heapArena != null) {
            // 池中获取
            buf = heapArena.allocate(cache, initialCapacity, maxCapacity);
        } else {
            buf = PlatformDependent.hasUnsafe() ?
                    new UnpooledUnsafeHeapByteBuf(this, initialCapacity, maxCapacity) :
                    new UnpooledHeapByteBuf(this, initialCapacity, maxCapacity);
        }

        return toLeakAwareBuffer(buf);
    }
    
}    

```





### PooledByteBufAllocator结构

每一个线程绑定一个PoolThreadCache

​		FastThreadLocal获取自己线程的PoolThreadCache

​		可以看出在一个ByteBuf一般都只由一个线程操作 

通过PoolThreadCache获取Arena 

PoolThreadCache里有两个PoolArena

​		heapArena 和 directArena

​		heapArena数组和directArena数组 默认2倍CPU核数大小，因为线程也是默认怎么大

PoolThreadCache维护着

​		tinyCacheSize  :  tinyCache大小的个数 0

​		smallCacheSize  : smallCache大小的个数  256

​		normalCacheSize   : normalCache大小的个数 64

![](img\2022-09-07 075758.png)





### directArena分配内存direct流程

从对象池里拿到PooledByteBuf进行复用

从缓存上进行内存分配

从内存堆里面进行内存分配



### 内存规格

tiny 0-512b 

small 512b - 8K 

normal 8K - 16M 

huge --大于16M 不缓存



Chunk：16M大小的

Page:  8K大小切分Chunk，就2048个8K

SubPage 16B，512B，1K，2K，4K

Page里面切分SubPage ，例如1K内存，Page里面切分8个SubPage（8*1K）



内存申请以chunk为单位的向操作系统分配

例如申请1M内存 ：先申请16M的 chunk，然后16M里面取一段连续的1M内存扔给buff

例如申请16K内存 ：先拿到16M的 chunk，然后在里面找到2个连续的Page (2*8K=16K)

例如申请1K内存 ：先拿到16M的 chunk，然后在里面找到一个 8K Page , 最后Page里面找到一个空闲的SubPage（按1K切分Page）





![](img\2022-09-07 083405.png)







### MemoryRegionCache 缓存的数据结构

最新版已经没有tiny 规格的了

```java
enum SizeClass {
    Small,
    Normal
}
```



queue :  一个chunk 和 handler

一个chunk 加上 handler指向一段连续的内存，就能确定内存的大小和位置

sizeClass： 一个chunk的内存规格

1. tiny 0-512b 
2. small 512b - 8K  
3. normal 8K - 16M 
4. huge --大于16M 不缓存

size：sizeClass规格个数



![](img\2022-09-07 085436.png)







MemoryRegionCache : cache缓存的queue 规格大小是固定的

例如：

tiny 有32种内存规格的queue

16B ，32B，48B, ........ 480B  

例如要申请30B，那就找打到规格为32B规格的tiny queue

samll 有4种规格的queue

例如要申请1K，那就找打1K规格的samll queue

normal 有3种规格的queue

例如申请10K，那就找打16K规格的normal  queue







判断：

第一步：判断是否小于512B，小于512B

第二步：大小除以16，就可以得到数组下标，去拿到对应规格的queue

例如：32B大小，32b/16,就得到2，tinySubpagePools中的MemoryRegionCache[2]得到32B规格的queue





![](img\2022-09-07 090917.png)





```java
final class PoolThreadCache {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(PoolThreadCache.class);
    private static final int INTEGER_SIZE_MINUS_ONE = Integer.SIZE - 1;

    final PoolArena<byte[]> heapArena;
    final PoolArena<ByteBuffer> directArena;

    // Hold the caches for the different size classes, which are small and normal.
    //用于分配大于等于512字节的内存，默认长度为4, 每次容量翻倍
    private final MemoryRegionCache<byte[]>[] smallSubPageHeapCaches;
    private final MemoryRegionCache<ByteBuffer>[] smallSubPageDirectCaches;
    
    //用于分配大于8K字节的内存，小于32K,默认长度为3, 每次容量翻倍
    private final MemoryRegionCache<byte[]>[] normalHeapCaches;
    private final MemoryRegionCache<ByteBuffer>[] normalDirectCaches;

    private final int freeSweepAllocationThreshold;
    private final AtomicBoolean freed = new AtomicBoolean();

    private int allocations;
    
}

//新版smallCacheSize是256个，normalCacheSize是64个
PoolThreadCache(PoolArena<byte[]> heapArena, PoolArena<ByteBuffer> directArena,
                    int smallCacheSize, int normalCacheSize, int maxCachedBufferCapacity,
                    int freeSweepAllocationThreshold) {
        checkPositiveOrZero(maxCachedBufferCapacity, "maxCachedBufferCapacity");
        this.freeSweepAllocationThreshold = freeSweepAllocationThreshold;
        this.heapArena = heapArena;
        this.directArena = directArena;
        if (directArena != null) {
            //缓存256个small类型内存配	
            //small 4种规格
            //例如：1K ，small[1] 就能找到1K对应的queue
            smallSubPageDirectCaches = createSubPageCaches(
                    smallCacheSize, directArena.numSmallSubpagePools);
			//缓存64个normal类型内存配
            //normal 3种规格
            //例如：10K，normal[1]  就能找到16K规格的queue
            normalDirectCaches = createNormalCaches(
                    normalCacheSize, maxCachedBufferCapacity, directArena);

            directArena.numThreadCaches.getAndIncrement();
        } else {
            // No directArea is configured so just null out all caches
            smallSubPageDirectCaches = null;
            normalDirectCaches = null;
        }
        if (heapArena != null) {
            // Create the caches for the heap allocations
            smallSubPageHeapCaches = createSubPageCaches(
                    smallCacheSize, heapArena.numSmallSubpagePools);

            normalHeapCaches = createNormalCaches(
                    normalCacheSize, maxCachedBufferCapacity, heapArena);

            heapArena.numThreadCaches.getAndIncrement();
        } else {
            // No heapArea is configured so just null out all caches
            smallSubPageHeapCaches = null;
            normalHeapCaches = null;
        }

        // Only check if there are caches in use.
        if ((smallSubPageDirectCaches != null || normalDirectCaches != null
                || smallSubPageHeapCaches != null || normalHeapCaches != null)
                && freeSweepAllocationThreshold < 1) {
            throw new IllegalArgumentException("freeSweepAllocationThreshold: "
                    + freeSweepAllocationThreshold + " (expected: > 0)");
        }
    }




```





### 命中缓存的分配流程

找到对应size的 MemoryRegionCache

从queue中弹出一个entry给ByteBuff初始化

将entry扔到对象池里进行复用







### Arena

Arena 有 ChunkList 组成

```java
//使用率100%,多个chunk-16M内存 使用率都是100%
q100 = new PoolChunkList<T>(this, null, 100, Integer.MAX_VALUE, chunkSize);
//使用率75-100% ,多个chunk-16M内存 使用率都是在75-100% 
q075 = new PoolChunkList<T>(this, q100, 75, 100, chunkSize);
//使用率50-100%
q050 = new PoolChunkList<T>(this, q075, 50, 100, chunkSize);
//使用率25-75%
q025 = new PoolChunkList<T>(this, q050, 25, 75, chunkSize);
//使用率1-50%
q000 = new PoolChunkList<T>(this, q025, 1, 50, chunkSize);
//使用率0-25%
qInit = new PoolChunkList<T>(this, q000, Integer.MIN_VALUE, 25, chunkSize);
```



### Chunk

向操作系统申请的内存大小16M

要么不申请，申请就申请一个chunk



### Page 

8k大小

Chunk按8k大小切分成N个page



### SubPage

继续划分page ，page还是太大了 

smallSubpagePools分配大于等于512字节的内存,规格512B,1K，2K, 4K



### Page 内存级别的分配

尝试在现有的chunk上分配

​	chunkList里面找

创建一个chunk进行内存分配

​	通过深度和下标，确定那一块连续的内存大小

​	例如：深度10 ，下标2   表示16k-32K连续内存可以使用-连续二叉树



![](img\2022-09-07 114300.png)







### SubPage 内存级别的分配

定位一个SubPage 

初始化SubPage 

初始化PooledByteBuf



### 调试代码

```java

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.concurrent.FastThreadLocalThread;

public class BatyBufAllocator {

    public static void main(String[] args) {
        PooledByteBufAllocator aDefault = PooledByteBufAllocator.DEFAULT;

        ByteBuf byteBuf = aDefault.directBuffer(16);

        byteBuf.release();

    }
}

```





### 内存释放

连续的内存区段加到缓存

标记连续的内存区段为未使用

ByteBuff加到对象池里







### 总结

ByteBuff的api和分类

重要api : read  write set 

分类：

Pooled 和 Unpooled 

Head 和 DIrect

unsafe 和 非unsafe





分配pooled内存的步骤





不同规格的pooled内存分配与释放





### netty 分配内存过程



两个重要的类：

ByteBufAllocator.DEFAULT   和 RecvByteBufAllocator

```java
private volatile ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
private volatile RecvByteBufAllocator rcvBufAllocator;
```



ByteBufAllocator.DEFAULT 就是我们上面的内存分配器

如果只是ByteBufAllocator.DEFAULT 分配内存，默认大小是256 --MessageToByteEncoder.write方法里默认分配内存大小就是256

往sockect底层写MessageToByteEncoder.write默认大小是256

allocator.ioBuffer()

```java
static final int DEFAULT_INITIAL_CAPACITY = 256;

public ByteBuf ioBuffer() {
    if (PlatformDependent.hasUnsafe() || isDirectBufferPooled()) {
        return directBuffer(DEFAULT_INITIAL_CAPACITY);
    }
    return heapBuffer(DEFAULT_INITIAL_CAPACITY);
}
```





RecvByteBufAllocator是具体要分配多少的接收内存分配器

首次默认：NioSocketChannelConfig.RecvByteBufAllocator  默认是2048

new NioSocketChannel () 的时候 config = new NioSocketChannelConfig



首次分配内存：就是通过RecvByteBufAllocator.guess()  return nextReceiveBufferSize=2048  从 ByteBufAllocator.DEFAULT 里拿一个2048的ByteBuff

读完 2048 ，就会记录读到了多少 lastBytesRead 方法，计算下次 RecvByteBufAllocator的大小 nextReceiveBufferSize



SIZE_TABLE  ： 512B之前一个16B 开始递增的内存大小数组，大于512则两倍扩容



然后从通过nextReceiveBufferSize 从 SIZE_TABLE 里 拿到下一次 RecvByteBufAllocator.nextReceiveBufferSize的大小

通过nextReceiveBufferSize的大小从 ByteBufAllocator.DEFAULT 里拿一个和nextReceiveBufferSize相近大小的ByteBuff



首次小于 2048 ，记录，下一次减少ByteBuff大小，传播ChannelRead结束，没有下一次



大于ByteBuff大小，记录，下一次扩容ByteBuff大小，传播ChannelRead完成之后，继续循环处理



### 产生 tcp 拆包粘包问题

tcp粘包，即tcp在发送数据时，可能会把两个tcp包合并成一个发送

tcp拆包，即tcp在发送数据时，可能会把一个tcp包拆成多个来发送

例如：客户端分两次给服务端发送了两个消息"ABCD" 和 "EFG"

- 服务端可能收到三个数据包，分别是"AB", "CD", "EFG"，即第一个数据包被拆包成了两个
- 服务端可能只会收到一个数据包："ABCDEFG"，即两个数据包被合并成了一个包
- 服务端甚至可能会收到"ABC", "DEFG"，会拆包再粘包



####  产生的原因

1. socket缓冲区造成的粘包：

   每个socket都有一个发送缓存区与接收缓冲区，客户端向服务端写数据时，实际上是写到了服务端socket的接收缓冲区中。

   服务端调用read方法时，其实只是把接收缓冲区的内容读取到内存中了。

   因此，服务端调用read方法时，可能客户端已经写了两个包到接收缓冲区中了，因此read到的数据其实是两个包粘包后的数据。

2. MSS/MTU限制导致的拆包

   MSS是指TCP每次发送数据允许的最大长度，一般是1500字节，如果某个数据包超过了这个长度，就要分多次发送，这就是拆包。

3. Nagle算法导致的粘包

    网络数据包都是要带有数据头部的，通常是40字节，假如我们发送一个字节的数据，也要加上这40个字节的头部再发送，显然这样是非常不划算的。

    所以tcp希望尽可能的一次发送大块的数据包，Nagle算法就是做这个事的，它会收集多个小数据包，合并为一个大数据包后再发送，这就是粘包。



#### 解决办法

通常，解决tcp粘包拆包问题，是通过定义通信协议来实现的：

1. 定长协议
    即规定每个数据包的长度，假如我们规定每个数据包的长度为3，假如服务端收到客户端的数据为："ABCD", "EF"，那么也可以解析出实际的数据包为"ABC", "DEF"。
2. 特殊分隔符协议
    即规定每个数据包以什么样的字符结尾，如规定以$符号结尾，假如服务端收到的数据包为："ABCD$EF", "G$"，那么可以解析出实际数据包为："ABCD", "EFG"。这种方式要确保消息体中可能会出现分隔符的情况。
3. 长度编码协议
    即把消息分为消息头和消息体，在消息头中包含消息的长度

关于tcp粘包拆包的内容，这有篇文章讲得非常好，强推：[TCP粘包、拆包与通信协议](https://links.jianshu.com/go?to=https%3A%2F%2Fblog.csdn.net%2Fu013857458%2Farticle%2Fdetails%2F82686275)



###  Netty中解决tcp粘包拆包问题的方法 

在对应的解码器里面判断是否发生了拆包，后续还有数据，有数据不处理，记住这个ByteBuf，下次循环读到的ByteBuf，会和当前ByteBuf合并，之后再处理

#### ByteToMessageDecoder 关键类

```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof ByteBuf) {
        selfFiredChannelRead = true;
        CodecOutputList out = CodecOutputList.newInstance();
        try {
            // 首次
            first = cumulation == null;
            // 写入累积区
            // 首次cumulation = cumulation EMPTY_BUFFER + msg
            // 将之前的msg 相加写到 cumulation
            cumulation = cumulator.cumulate(ctx.alloc(),
                    first ? Unpooled.EMPTY_BUFFER : cumulation, (ByteBuf) msg);
            // 调用 decode 方法进行解码
            callDecode(ctx, cumulation, out);
        } catch (DecoderException e) {
            throw e;
        } catch (Exception e) {
            throw new DecoderException(e);
        } finally {
            try {
                if (cumulation != null && !cumulation.isReadable()) {
                    numReads = 0;
                    cumulation.release();
                    cumulation = null;
                } else if (++numReads >= discardAfterReads) {
                    // We did enough reads already try to discard some bytes, so we not risk to see a OOME.
                    // See https://github.com/netty/netty/issues/4275
                    numReads = 0;
                    discardSomeReadBytes();
                }

                int size = out.size();
                firedChannelRead |= out.insertSinceRecycled();
                // out 里有数据就向下传播
                fireChannelRead(ctx, out, size);
            } finally {
                out.recycle();
            }
        }
    } else {
        ctx.fireChannelRead(msg);
    }
}



 protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    // 如果累计区还有可读字节
    while (in.isReadable()) {
        int outSize = out.size();
        // 上次循环成功解码，但ByteBuf里面还有数据可以读
        if (outSize > 0) {
            // 调用后面的业务 handler 的  ChannelRead 方法
            fireChannelRead(ctx, out, outSize);
            // 将 size 置为0 ,防止finally 的 fireChannelRead
            out.clear();//
            if (ctx.isRemoved()) {
                break;
            }
            outSize = 0;
        }
        // 得到可读字节数
        int oldInputLength = in.readableBytes();
        // 调用 decode 方法，将成功解码后的数据放入道 out 数组中，可能会删除当前节点，删除之前会将数据发送到最后的 handler
        decodeRemovalReentryProtection(ctx, in, out);// decode()
        if (ctx.isRemoved()) {
            break;
        }
        // 调用子类decode方法,没有数据放到 out 中
        if (outSize == out.size()) {
            //两种情况:
            //1.没读数据
            //2.读了数据，但没解析成对象
            
            //没读数据，就证明子类decode没有完整的数据包，需要继续接收buff
            //当前channelRead结束，继续channelRead--NioEventLoop.unsafe.read继续读数据到buff
            if (oldInputLength == in.readableBytes()) {
                break;
            } else {
                //  out 没数据，buff读了，就接着读
                continue;
            }
        }
        
        if (oldInputLength == in.readableBytes()) {
            		// out 有数据，但没读数据，这是错误的
                    throw new DecoderException(
                            StringUtil.simpleClassName(getClass()) +
                                    ".decode() did not read anything but decoded a message.");
                }

        
        if (isSingleDecode()) {
            break;
        }
    }
}
```







#### 自定义一个tcp粘包拆包处理器

基于ByteToMessageDecoder，我们可以很容易的实现处理tcp粘包拆包问题的Handler，以定长协议为例，我们来实现一个定长协议的tcp粘包拆包处理器：

```java
public class LengthDecoder extends ByteToMessageDecoder {
    private int length;

    public LengthDecoder(int length) {
        this.length = length;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.readableBytes() >= length) {
            byte[] buff = new byte[length];
            in.readBytes(buff);
            out.add(new String(buff));
        }
    }
}
```

在decode方法中，我们循环判断，如果ByteBuf中未读的数据量大于指定的长度length，我们就读到lenght个数据，然后转成字符串加入到List中。

后续ByteToMessageDecoder依次把List中的数据fireChannelRead传递事件。

如果ByteBuf中的未读数据不够length，说明发生了拆包，后续还有数据，这里直接不处理即可，ByteToMessageDecoder会帮我们记住这次的ByteBuf，下一次数据来了之后，会跟这次的数据合并后再处理。



#### Netty中自带的tcp粘包拆包处理器

Netty中实现了很多种粘包拆包处理器：

- FixedLengthFrameDecoder：与我们上面自定义的一样，定长协议处理器
- DelimiterBasedFrameDecoder：特殊分隔符协议的处理器
- LineBasedFrameDecoder：特殊分隔符协议处理器的一种特殊情况，行分隔符协议处理器。
- JsonObjectDecoder：json协议格式处理器
- **HttpRequestDecoder：http请求体协议处理器**
- **HttpResponseDecoder：http响应体处理器，很明显这个是用于客户端的**

























































































































