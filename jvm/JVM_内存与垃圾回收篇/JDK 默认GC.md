## JDK 默认GC

JDK 7 及之前：Parallel Scavenge （PS Scavenge）+ Serial Old (PS MarkSweep)

JDK 8 ：Parallel Scavenge（PS Scavenge） + Parallel Old (PS MarkSweep)  

JDK 9 及之后：G1

注意：PS MarkSweep 是 Serial Old 和 Parallel Old的别名



JDK9中完全取消了这些组合的支持：Serial +CMS、ParNew+Serial old  （ParNew：Serial 的多线程版）

JDK9中-XX:+UseParNewGC参数在JDK9中已废弃，

JDK10中被移除了ParNewGC垃圾回收器

JDK14中：弃用Parallel Scavenge和Serialold GC组合

JDK14中：删除CMS垃圾回收器

JDK15中：移除 Z GC 和 Shennandoah GC 的实验性，但默认还是G1



现在就剩下的三种组合：

Serial    Serial old   :  单核，内存小，现在除了嵌入式设备基本不使用了，连桌面应用都多核了

Parallel  Parallel old ： 内存略小的，最大化吞吐量

G1 ：多核内存大的

```shell
-XX:+UseSerialGC:表明新生代使用Serial GC ，同时老年代使用Serial Old GC
-XX:+UseParallelGC:表明新生代使用Parallel GC ，同时老年代使用Parallel Old GC
-XX:+UseG1GC 
```



现在 JVM GC 

https://github.com/openjdk/jdk/tree/master/src/hotspot/share/gc

- epsilon
- g1
- parallel
- serial
- shennandoah
- z

![](images\image-23200705205444110.jpg)





jdk 8 

```shell
java -XX:+PrintCommandLineFlags -version

-XX:InitialHeapSize=132397312 // JVM默认初始化堆大小
-XX:MaxHeapSize=2118356992 //JVM堆的默认最大值
-XX:+PrintCommandLineFlags 
-XX:+UseCompressedClassPointers 
-XX:+UseCompressedOops 
-XX:-UseLargePagesIndividualAllocation 
-XX:+UseParallelGC //Java8使用的GC类型
java version "1.8.0_20" //使用的java版本
Java(TM) SE Runtime Environment (build 1.8.0_20-b26)
Java HotSpot(TM) 64-Bit Server VM (build 25.20-b23, mixed mode)
```



openjdk8 ：加上 -server

```shell
java-XX:+PrintCommandLineFlags -XX:+PrintGCDetails -server -version
-XX:InitialHeapSize=67108864 
-XX:MaxHeapSize=734003200 
-XX:+PrintCommandLineFlags 
-XX:+PrintGCDetails 
-XX:-UseLargePagesIndividualAllocation 
-XX:+UseParallelGC
openjdk version "1.8.0_42"
OpenJDK Runtime Environment (build 1.8.0_42-b03)
OpenJDK Server VM (build 25.40-b25, mixed mode)
```



```shell
java -XX:+PrintGCDetails -version

openjdk version "1.8.0_42"
OpenJDK Runtime Environment (build 1.8.0_42-b03)
OpenJDK Server VM (build 25.40-b25, mixed mode)
Heap
 PSYoungGen      total 19200K, used 1664K [0x2f0c0000, 0x30600000, 0x3da00000)
  eden space 16640K, 10% used [0x2f0c0000,0x2f2602b0,0x30100000)
  from space 2560K, 0% used [0x30380000,0x30380000,0x30600000)
  to   space 2560K, 0% used [0x30100000,0x30100000,0x30380000)
 ParOldGen       total 43776K, used 0K [0x11e00000, 0x148c0000, 0x2f0c0000)
  object space 43776K, 0% used [0x11e00000,0x11e00000,0x148c0000)
 Metaspace       used 1489K, capacity 2240K, committed 2240K, reserved 4480K
```



使用 ManagementFactory.getGarbageCollectorMXBeans() 打印

```java
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
public class GcCollectorPrinter {
    public static void main(String[] args) {
        List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean bean : beans) {
            System.out.println(bean.getName());
        }
    }
} 

输出：
PS Scavenge
PS MarkSweep

```









