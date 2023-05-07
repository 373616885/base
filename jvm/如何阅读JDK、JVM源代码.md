# JDK、JRE、JVM

在阅读源码之前，先明白Java平台几个组件的关系。以Oracle JDK 8为例。

## JDK简介

JDK（Java Development Kit），Java平台的全套设施，**包含 Java类库、JRE、Java工具应用**等。

jdk下主要存在4个目录：

- **bin**：`Java工具应用`。如：javac（编译器）、javap（字节码查看工具）、jinfo（Java应用配置信息查看）、jps（Java进程诊断）等等。
- **include**：`Java和JVM交互的头文件`。因为JVM是C系语言编写的，而C语言的类型定义在`.h`文件中。
- **jre**：`Java运行环境`。包含Java运行时所需的所有依赖（Java、C、C++等类库）。
- **lib**：`Java类库`。

## JRE简介

JRE（Java Runtime Environment），Java应用运行的必须环境，**包含Java运行所需的所有依赖集合**。

jre下有两个目录：

- **bin**：JVM的实现。
- **lib**：JVM运行所需要的的依赖。

## JVM简介

JVM（Java Virtual Machine），Java虚拟机，**Java能够跨平台的关键**。

可以将JVM看做一个沙盒，Java应用运行在其内部，而OS在其外部，JVM作为一个中间层用来将字节码映射到OS的系统调用，帮助开发者抹平了不同平台的差异。

![img](img\2023050511376557.png)

## 三者关系

**1. 开发Java应用需要JDK，JDK包含JRE。**

**2. 运行Java应用需要JRE，JRE包含JVM。**

![img](img\2023050511376558.png)

# 阅读Java类库源码

JDK包含的Java类库很多，它们存在于`jdk/lib`、`jdk/jre/lib`目录下，Java的类加载机制默认会加载`jdk/jre/lib`下的jar，其它需要加载的jar可以配置在`class-path`系统变量中。

Java提供的主要类库如下：

- **jdk/jre/lib/rt.jar**：Java基础类库，rt即`rutime.jar`。
- **jdk/lib/tools.jar**：主要是`jdk/bin`下提供的Java工具应用对应的jar文件，例如javac、javap等。
- **jdk/lib/dt.jar**：dt.jar主要是swing包。

我们开发时，一般用到的类库就是`rt.jar`，下载的Oracle JDK中也包含`rt.jar`的源代码，因此可以直接阅读。

# 阅读Java类库native代码

> 参考：[如何查找 jdk 中的 native 实现](https://gorden5566.com/post/1027.html)

一般情况下看`rt.jar`就行了，但是有些方法是`native`方法，这些方法在运行时会映射到对应的C/C++函数，且依赖于具体平台实现。如果想要继续往下看，就得找到对应的C/C++代码，Oracle JDK中没有C/C++源代码，因此需要下载[OpenJDK](http://hg.openjdk.java.net/jdk8)源码。

下载页面如下：

![img](img\2023050511376559.png)

可以看到jdk 8存在多个项目，我们主要关注如下几个项目（其它项目简介可参考 [OpenJDK Build README](http://hg.openjdk.java.net/jdk8u/jdk8u/raw-file/832508a6165c/README-builds.html)、[jdk源码剖析一：OpenJDK-Hotspot源码包目录结构](https://www.cnblogs.com/dennyzhangdd/p/6734933.html)）：

- **hotspot**：hotspot虚拟机源代码。
- **jdk**：jdk源代码。
- **langtools**：javac、javap、javadoc等工具的源代码。

下载这3个项目，进入`jdk/src`，其下目录如下：

![img](img\2023050511376560.png)

其中`share/`目录为公共实现，其它目录为各平台的特有实现。进入`share/`目录如下：

![img](img\2023050511376570.png)

- classes：java基础类库源代码。
- native：java基础类库对应的C/C++代码。
- sample、demo：示例。
- 其它：实现Java基础功能的C/C++代码，。

比如我们要查看`System.currentTimeMillis()`的native实现。需要先找到对应目录的C文件，`jdk/src/share/native/java/lang/System.c:38`。

```cpp
static JNINativeMethod methods[] = {
    {"currentTimeMillis", "()J",              (void *)&JVM_CurrentTimeMillis},
    {"nanoTime",          "()J",              (void *)&JVM_NanoTime},
    {"arraycopy",     "(" OBJ "I" OBJ "II)V", (void *)&JVM_ArrayCopy},
};
```

为了保证性能，该方法被注册，指向`JVM_CurrentTimeMillis`函数，该函数以JVM开头，表明是JVM实现。进入hotspot项目，该函数定义位于`hotspot/src/share/vm/prims/jvm.cpp:287`。

```cpp
JVM_LEAF(jlong, JVM_CurrentTimeMillis(JNIEnv *env, jclass ignored))
  JVMWrapper("JVM_CurrentTimeMillis");
  return os::javaTimeMillis();
JVM_END
```

如下，`JVM_LEAF`和`JVM_END`是两个宏定义，定义了函数体的头和尾。

```cpp
#define JVM_LEAF(result_type, header)                                \
extern "C" {                                                         \
  result_type JNICALL header {                                       \
    VM_Exit::block_if_vm_exited();                                   \
    VM_LEAF_BASE(result_type, header)
#define JVM_END } }
```

函数体直接调用`os::javaTimeMillis()`函数，该函数不同平台有不同实现，以linux为例，文件位于`hotspot/src/os/linux/vm/os_linux.cpp:1362`。

```cpp
jlong os::javaTimeMillis() {
  timeval time;
  // 1. 执行linux系统调用，返回一个结构体
  int status = gettimeofday(&time, NULL);
  assert(status != -1, "linux error");
  // 2. 将时间转换为秒：time.tv_sec为毫秒，time.tv_usec为微妙
  return jlong(time.tv_sec) * 1000  +  jlong(time.tv_usec / 1000);
}
```

如上，`gettimeofday()`是一个系统调用，定义位于`<sys/time.h>`头文件中。

> Java代码对应的native实现，大多依赖于具体平台，因此存在很多不同的实现（这就是JVM为何能够跨平台的原因），阅读时选择对应平台的实现即可。

# 阅读JVM源码

https://github.com/openjdk/jdk/tree/master/src/hotspot

Oracle JDK并未将jvm开源，需要从[OpenJDK](http://hg.openjdk.java.net/jdk8)下载jvm实现。

JVM的详细目录结构如下（参考[OpenJDK-Hotspot源码包目录结构](https://www.cnblogs.com/dennyzhangdd/p/6734933.html)）：

```html
├─agent                            Serviceability Agent的客户端实现
├─make                             用来build出HotSpot的各种配置文件
├─src                              HotSpot VM的源代码
│  ├─cpu                            CPU相关代码（汇编器、模板解释器、ad文件、部分runtime函数在这里实现）
│  ├─os                             操作系相关代码
│  ├─os_cpu                         操作系统+CPU的组合相关的代码
│  └─share                          平台无关的共通代码
│      ├─tools                        工具
│      │  ├─hsdis                      反汇编插件
│      │  ├─IdealGraphVisualizer       将server编译器的中间代码可视化的工具
│      │  ├─launcher                   启动程序“java”
│      │  ├─LogCompilation             将-XX:+LogCompilation输出的日志（hotspot.log）整理成更容易阅读的格式的工具
│      │  └─ProjectCreator             生成Visual Studio的project文件的工具
│      └─vm                           HotSpot VM的核心代码
│          ├─adlc                       平台描述文件（上面的cpu或os_cpu里的*.ad文件）的编译器
│          ├─asm                        汇编器接口
│          ├─c1                         client编译器（又称“C1”）
│          ├─ci                         动态编译器的公共服务/从动态编译器到VM的接口
│          ├─classfile                  类文件的处理（包括类加载和系统符号表等）
│          ├─code                       动态生成的代码的管理
│          ├─compiler                   从VM调用动态编译器的接口
│          ├─gc_implementation          GC的实现
│          │  ├─concurrentMarkSweep      Concurrent Mark Sweep GC的实现
│          │  ├─g1                       Garbage-First GC的实现（不使用老的分代式GC框架）
│          │  ├─parallelScavenge         ParallelScavenge GC的实现（server VM默认，不使用老的分代式GC框架）
│          │  ├─parNew                   ParNew GC的实现
│          │  └─shared                   GC的共通实现
│          ├─gc_interface               GC的接口
│          ├─interpreter                解释器，包括“模板解释器”（官方版在用）和“C++解释器”（官方版不在用）
│          ├─libadt                     一些抽象数据结构
│          ├─memory                     内存管理相关（老的分代式GC框架也在这里）
│          ├─oops                       HotSpot VM的对象系统的实现
│          ├─opto                       server编译器（又称“C2”或“Opto”）
│          ├─prims                      HotSpot VM的对外接口，包括部分标准库的native部分和JVMTI实现
│          ├─runtime                    运行时支持库（包括线程管理、编译器调度、锁、反射等）
│          ├─services                   主要是用来支持JMX之类的管理功能的接口
│          ├─shark                      基于LLVM的JIT编译器（官方版里没有使用）
│          └─utilities                  一些基本的工具类
└─test                             单元测试
```

JVM完全是C/C++实现，阅读时选取自己需要的模块即可。