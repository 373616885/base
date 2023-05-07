### 文档下载查看

oracle 官网

Products

Java

Download Java now

[Online Documentation](https://docs.oracle.com/en/java/javase/17/)

Specifications

- [Language and VM](https://docs.oracle.com/javase/specs/index.html)

https://docs.oracle.com/javase/specs/index.html

The Java Virtual Machine Specification, Java SE 17 Edition

[HTML](https://docs.oracle.com/javase/specs/jvms/se17/html/index.html) | [PDF](https://docs.oracle.com/javase/specs/jvms/se17/jvms17.pdf)



### JVM虚拟机

只关心二进制字节码，负责装载字节码到其内部，解释/编译为对应平台上的机器指令执行

不管是Java 编译的还是别的语言编译的 



特点：

- 一次编译，到处运行
- 自动内存管理
- 自动垃圾回收功能





### JVM程序中的位置

JVM是运行在操作系统之上的，它与硬件没有直接的交互

![](images\image-20200704183048061.png)

### Java体系中的位置

![](images\image-20200704183236169.png)



Java 文件 需要编译器  编译才能 变成 class 文件

JDK 中的 javac 就是对应的前端编译器（后端编译器 -JIT ）



### 执行引擎

1. 解释器---将字节码文件逐行解释成机器指令
2. JIT编译器---运行的时候去寻找字节码文件中的热点代码编译成机器指令并缓存到方法区
3. 垃圾回收器

JIT编译器：编译期间需要进行优化，启动耗时比较长，只有优化，缓存之后执行才会快

解释器：字节码文件逐行解释--前期比较快

中和两者的特点，需要两者结合

俗语：开始走路，然后发现有公交车可以坐一段（JIT）就坐车走一段，

接着下车走路，发现又可以坐交车，又坐一段，重复过程

JIT就是发现公交站点，并上车



### Java代码执行流程

![images\image-20200704210429535.png](images\image-20200704210429535.png)













