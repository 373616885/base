![](images\20210419005720.png)



![](images\20210420215230.png)



![](images\20210420220224.png)





### Java对象数据结构

概括起来分为对象头、对象体（属性数据）和对齐字节

**对象头：**

Mark Word：锁信息，GC信息，hash

Klass Word ：Class信息的指针--指向方法区的Class的实例

数组长度：只有是数组对象才有

**对象体：**

对象的属性

**对齐字节：**

32位的JVM上，长度为32位（8字节）；64位JVM则为64位，

64位JVM如果开启+UseCompressedOops选项，该区域长度也将由64位压缩至32位，默认开启

由于虚拟机要求 对象起始地址必须是8字节的整数倍



### Object obj  = new Object() 占用字节

JVM默认开启压缩 +UseCompressedOops

```
-XX:+UseCompressedOops  开启指针压缩
-XX:-UseCompressedOops  关闭指针压缩
```

- 未开启指针压缩 占用大小为：8(Mark Word)+8(Class Pointer)=16字节

- 开启了指针压缩(默认是开启的) 开启指针压缩后，Class Pointer会被压缩为4字节，最终大小为： 8(Mark Word)+4(Class Pointer)+4(对齐填充)=16字节

```xml
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.10</version>
</dependency>
```

```java
package com.zwx.jvm;

import org.openjdk.jol.info.ClassLayout;

public class HeapMemory {
    public static void main(String[] args) {
        Object obj = new Object();
        System.out.println(ClassLayout.parseInstance(obj).toPrintable());
    }
}
```

![](images\20220918170956.png)



最后的结果是16字节



```
-XX:-UseCompressedOops  关闭指针压缩
```



![](images\20220918170957.png)





再次运行，得到如下结果：

![](images\20220918170958.png)



这时候已经没有了对齐填充部分了，但是占用大小还是16位



下面我们再来演示一下如果一个对象中带有属性之后的大小

```java

package com.qin.jvm;

public class MyItem {
    byte i = 0;
}


```



开启指针压缩,占用16字节：

![](images\20220918170959.png)

关闭指针压缩，占用24字节：

![](images\20220918170960.png)

这个时候就能看出来开启了指针压缩的优势了，如果不断创建大量对象，指针压缩对性能还是有一定优化的







