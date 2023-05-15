
# 2. JVM 监控及诊断工具-命令行篇

## 2.1. 概述

性能诊断是软件工程师在日常工作中需要经常面对和解决的问题，在用户体验至上的今天，解决好应用的性能问题能带来非常大的收益。

Java 作为最流行的编程语言之一，其应用性能诊断一直受到业界广泛关注。可能造成 Java 应用出现性能问题的因素非常多，例如线程控制、磁盘读写、数据库访问、网络 I/O、垃圾收集等。想要定位这些问题，一款优秀的性能诊断工具必不可少。

体会 1：使用数据说明问题，使用知识分析问题，使用工具处理问题。

体会 2：无监控、不调优！

**简单命令行工具**

在我们刚接触 java 学习的时候，大家肯定最先了解的两个命令就是 javac，java，那么除此之外，还有没有其他的命令可以供我们使用呢？

我们进入到安装 jdk 的 bin 目录，发现还有一系列辅助工具。这些辅助工具用来获取目标 JVM 不同方面、不同层次的信息，帮助开发人员很好地解决 Java 应用程序的一些疑难杂症。

![image-20210504195803526](img\5b7c5d239e4da192ba65edb0800055c5.png)

![image-20210504195836342](img\fa3c5e41cbf999d261bcf32851731565.png)

官方源码地址：

https://github.com/openjdk/jdk/tree/jdk-17%2B35/src

本地源码

jdk-17.0.1\lib\src.zip

本地编译的class

jdk-17.0.1\jmods

## 2.2. jps：查看正在运行的 Java 进程

jps(Java Process Status)：显示指定系统内所有的 HotSpot 虚拟机进程（查看虚拟机进程信息），可用于查询正在运行的虚拟机进程。

说明：对于本地虚拟机进程来说，进程的本地虚拟机 ID 与操作系统的进程 ID 是一致的，是唯一的。

基本使用语法为：jps [options] [hostid]

我们还可以通过追加参数，来打印额外的信息。

**options 参数**

- -q：仅仅显示 LVMID（local virtual machine id），即本地虚拟机唯一 id。不显示主类的名称等
- -l：输出应用程序主类的全类名 或 如果进程执行的是 jar 包，则输出 jar 完整路径
- -m：输出虚拟机进程启动时传递给主类 main()的参数
- -v：列出虚拟机进程启动时的 JVM 参数。比如：-Xms20m -Xmx50m 是启动程序指定的 jvm 参数。

说明：以上参数可以综合使用。

补充：如果某 Java 进程关闭了默认开启的 UsePerfData 参数（即使用参数-XX：-UsePerfData），那么 jps 命令（以及下面介绍的 jstat）将无法探知该 Java 进程。

**PerfData文件相关参数：**

PerfData：

java程序启动后，默认会在 %TEMP%/hsperfdata_%USERNAME% 目录下以该进程的id为文件名新建文件PerfData文件是mmap到内存中的，，默认是32KB

linux : /tmp/hsperfdata_/

windows ：C:\Users\Administrator\AppData\Local\Temp\hsperfdata_Administrator

jps，其实就是读取/tmp/hsperfdata_$username/ 目录下所有的文件

这个文件是否存在取决于两个参数，一个UsePerfData，另一个是PerfDisableSharedMem

UsePerfData：默认是打开的，如果关闭了UsePerfData这个参数，那么JVM启动过程中PerfData的内存不会被创建，更别说PerfData的内存是不是可以被共享了

PerfDisableSharedMem：该参数决定了存储PerfData的内存是不是可以被共享。JVM在启动的时候会分配一块内存来存PerfData，如果设置了这个参数，说明该内存数据不能被其它进程共享，这样一来譬如Jps、Jstat等都无法工作。默认支持共享；

PerfData文件删除：正常情况下当JVM进程退出的时候会自动删除，但是当执行kill -9命令时，由于JVM不能捕获这种信号，虽然JVM进程不存在了，但是这个文件还是存在的。这个文件不是一直存在的，当再次有JVM进程启动时会自动删除这些无用的文件。

PerfData文件更新：由于PerfData文件是通过mmap的方式映射到了内存里，而jstat是直接通过DirectByteBuffer的方式从PerfData里读取的，所以只要内存里的值变了，那我们从jstat看到的值就会发生变化，内存里的值什么时候变，取决于-XX:PerfDataSamplingInterval这个参数，默认是50ms，也就是说50ms更新一次值，基本上可以认为是实时的了。



**hostid 参数**

RMI 注册表中注册的主机名。如果想要远程监控主机上的 java 程序，需要安装 jstatd。

对于具有更严格的安全实践的网络场所而言，可能使用一个自定义的策略文件来显示对特定的可信主机或网络的访问，尽管这种技术容易受到 IP 地址欺诈攻击。

如果安全问题无法使用一个定制的策略文件来处理，那么最安全的操作是不运行 jstatd 服务器，而是在本地使用 jstat 和 jps 工具。



```java
package com.tools;

import java.util.Scanner;

public class Command2 {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        final String next = scanner.next();
    }

}
```

![](img\2225de448c4af005aa0f72e84bba5e58.png)

```shell
$ jps
12000 Launcher  //idea
12272 Command   //java代码，Command
10452 Jps       //jps自身也是java进程
10900           //idea


## 只输出本地虚拟机唯一 id
$ jps -q
12000
12272
10900
6900

## 输出程序主类的全类名,或者jar全路径类名
$ jps -l
12000 org.jetbrains.jps.cmdline.Launcher
12272 com.tools.Command
10900
11464 jdk.jcmd/sun.tools.jps.Jps

## 输出虚拟机进程启动时传递给主类 main()的参数
$ jps -m
12000 Launcher E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/jps-builders.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/jps-builders-6.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/jps-javac-extension-1.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/util.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/annotations.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/3rd-party-rt.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/jna.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/kotlin-stdlib-jdk8.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/protobuf.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/platform-api.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/jps-model.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/javac2.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/forms_rt.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/qdox.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/aether-dependency-resolver.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/3rd-party.jar;E
12272 Command
10900
1436 Jps -m

## JVM 启动时参数
$ jps -v
12000 Launcher -Xmx700m -Djava.awt.headless=true --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED -Dpreload.project.path=E:/workspace/jvm01 -Dpreload.config.path=C:/Users/Administrator/AppData/Roaming/JetBrains/IntelliJIdea2021.3/options -Dexternal.project.config=C:\Users\Administrator\AppData\Local\JetBrains\IntelliJIdea2021.3\external_build_system\jvm01.1f4e26ee -Dcompile.parallel=false -Drebui
12272 Command -Dvisualvm.id=8279275434699 -javaagent:E:\JetBrains\IntelliJ IDEA 2021.3.3\lib\idea_rt.jar=60020:E:\JetBrains\IntelliJ IDEA 2021.3.3\bin -Dfile.encoding=UTF-8
10900  exit -Xms128m -Xmx1024m -XX:ReservedCodeCacheSize=512m -XX:+IgnoreUnrecognizedVMOptions -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=50 -XX:CICompilerCount=2 -XX:+HeapDumpOnOutOfMemoryError -XX:-OmitStackTraceInFastThrow -ea -Dsun.io.useCanonCaches=false -Djdk.http.auth.tunneling.disabledSchemes="" -Djdk.attach.allowAttachSelf=true -Djdk.module.illegalAccess.silent=true -Dkotlinx.coroutines.debug=off -XX:ErrorFile=$USER_HOME/java_error_in_idea_%p.log -XX:HeapDumpPath=$USER_HOME/java_error_in_idea.hprof -Dfile.encoding=UTF-8 -javaagent:E:\JetBrains\ja-netfilter-all\ja-netfilter.jar=jetbrains -Djb.vmOptionsFile=E:\JetBrains\ja-netfilter-all\vmoptions\idea.vmoptions -Djava.system.class.loader=com.intellij.util.lang.PathClassLoader -Didea.vendor.name=JetBrains -Didea.paths.selector=IntelliJIdea2021.3 -Didea.jre.check=true -Dsplash=true -Dide.native.launcher=true -XX:ErrorFile=C:\Users\Administrator\java_error_in_idea64_%p.log -XX:HeapDumpPath=C:\Users\Administrator\java_error_in_idea64.hprof
14132 Jps -Denv.class.path=E:\Java\jdk-17.0.1\lib -Dapplication.home=E:\Java\jdk-17.0.1 -Xms8m -Djdk.module.main=jdk.jcmd

## 和起来lvm ,注意不能和 -q 联合
$ jps -lvm
12000 org.jetbrains.jps.cmdline.Launcher E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/jps-builders.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/jps-builders-6.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/jps-javac-extension-1.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/util.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/annotations.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/3rd-party-rt.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/jna.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/kotlin-stdlib-jdk8.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/protobuf.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/platform-api.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/jps-model.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/javac2.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/lib/forms_rt.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/qdox.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/aether-dependency-resolver.jar;E:/JetBrains/IntelliJ IDEA 2021.3.3/plugins/java/lib/3rd-party.jar;E -Xmx700m -Djava.awt.headless=true --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED -Dpreload.project.path=E:/workspace/jvm01 -Dpreload.config.path=C:/Users/Administrator/AppData/Roaming/JetBrains/IntelliJIdea2021.3/options -Dexternal.project.config=C:\Users\Administrator\AppData\Local\JetBrains\IntelliJIdea2021.3\external_build_system\jvm01.1f4e26ee -Dcompile.parallel=false -Drebui
12272 com.tools.Command -Dvisualvm.id=8279275434699 -javaagent:E:\JetBrains\IntelliJ IDEA 2021.3.3\lib\idea_rt.jar=60020:E:\JetBrains\IntelliJ IDEA 2021.3.3\bin -Dfile.encoding=UTF-8
10900  exit -Xms128m -Xmx1024m -XX:ReservedCodeCacheSize=512m -XX:+IgnoreUnrecognizedVMOptions -XX:+UseG1GC -XX:SoftRefLRUPolicyMSPerMB=50 -XX:CICompilerCount=2 -XX:+HeapDumpOnOutOfMemoryError -XX:-OmitStackTraceInFastThrow -ea -Dsun.io.useCanonCaches=false -Djdk.http.auth.tunneling.disabledSchemes="" -Djdk.attach.allowAttachSelf=true -Djdk.module.illegalAccess.silent=true -Dkotlinx.coroutines.debug=off -XX:ErrorFile=$USER_HOME/java_error_in_idea_%p.log -XX:HeapDumpPath=$USER_HOME/java_error_in_idea.hprof -Dfile.encoding=UTF-8 -javaagent:E:\JetBrains\ja-netfilter-all\ja-netfilter.jar=jetbrains -Djb.vmOptionsFile=E:\JetBrains\ja-netfilter-all\vmoptions\idea.vmoptions -Djava.system.class.loader=com.intellij.util.lang.PathClassLoader -Didea.vendor.name=JetBrains -Didea.paths.selector=IntelliJIdea2021.3 -Didea.jre.check=true -Dsplash=true -Dide.native.launcher=true -XX:ErrorFile=C:\Users\Administrator\java_error_in_idea64_%p.log -XX:HeapDumpPath=C:\Users\Administrator\java_error_in_idea64.hprof
12556 jdk.jcmd/sun.tools.jps.Jps -lvm -Denv.class.path=E:\Java\jdk-17.0.1\lib -Dapplication.home=E:\Java\jdk-17.0.1 -Xms8m -Djdk.module.main=jdk.jcmd

```





## 2.3. jstat：查看 JVM 统计信息

jstat（JVM Statistics Monitoring Tool）：用于监视虚拟机各种运行状态信息的命令行工具。它可以显示本地或者远程虚拟机进程中的类装载、内存、垃圾收集、JIT 编译等运行数据。在没有 GUI 图形界面，只提供了纯文本控制台环境的服务器上，它将是运行期定位虚拟机性能问题的首选工具。常用于检测垃圾回收问题以及内存泄漏问题。

官方文档：[https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jstat.html](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jstat.html)

基本使用语法为：jstat -&lt;option&gt; [-t] [-h&lt;lines&gt;] &lt;vmid&gt; [&lt;interval&gt; [&lt;count&gt;]]

查看命令相关参数：jstat-h 或 jstat-help

其中 vmid 是进程 id 号，也就是 jps 之后看到的前面的号码，如下：

主要和 类装载相关，垃圾回收相关，JIT 相关

![image-20210504201703222](img\83dddc874824b88d7fd03dab2b3889f1.png)

**option 参数**

选项 option 可以由以下值构成。

<mark>类装载相关的：</mark>

- -class：显示 ClassLoader 的相关信息：类的装载、卸载数量、总空间、类装载所消耗的时间等

<mark>垃圾回收相关的：</mark>

- -gc：显示与 GC 相关的堆信息。包括 Eden 区、两个 Survivor 区、老年代、永久代等的容量、已用空间、GC 时间合计等信息。
- -gccapacity：显示内容与-gc 基本相同，但输出主要关注 Java 堆各个区域使用到的最大、最小空间。
- -gcutil：显示内容与-gc 基本相同，但输出主要关注已使用空间占总空间的百分比。
- -gccause：与-gcutil 功能一样，但是会额外输出导致最后一次或当前正在发生的 GC 产生的原因。
- -gcnew：显示新生代 GC 状况
- -gcnewcapacity：显示内容与-gcnew 基本相同，输出主要关注使用到的最大、最小空间
- -geold：显示老年代 GC 状况
- -gcoldcapacity：显示内容与-gcold 基本相同，输出主要关注使用到的最大、最小空间
- -gcpermcapacity：显示永久代使用到的最大、最小空间。

<mark>JIT 相关的：</mark>

- -compiler：显示 JIT 编译器编译过的方法、耗时等信息

- -printcompilation：输出已经被 JIT 编译的方法

**jstat -class**

![img](img\9f2cea8b0a9b1bc47c10281b5c140cc4.png)

**jstat -compiler**

![img](img\4e11a07ce9b8ff2f73ba5585e11e1da3.png)

**jstat -printcompilation**

![img](img\2a2553eef35293d28ef095feee3bb3b7.png)

**jstat -gc**

```shell
$ jstat -gc  -t -h4 11116  1000 10
```

![img](img\6ea2aa6665c49b4bd35d46152dd2f1aa.png)

字段	说明
S0C	年轻代第一个Survivor区的大小（字节）
S1C	年轻代第二个Survivor区的大小（字节）
S0U	年轻代第一个Survivor区的使用大小（字节）
S1U	年轻代第二个Survivor区的使用大小（字节）
EC	年轻代中Eden区的大小（字节）
EU	年轻代中Eden区的使用大小（字节）
OC	老年代大小（字节）
OU	老年代使用大小（字节）
MC	方法区大小（字节）
MU	方法区使用大小（字节）
CCSC	压缩类空间大小（字节）
CCSU	压缩类空间使用大小（字节）
YGC	年轻代垃圾回收次数
YGCT	年轻代垃圾回收消耗时间
FGC	老年代垃圾回收次数
FGCT	老年代垃圾回收消耗时间
GCT	垃圾回收消耗总时间

**jstat -gccapacity**

```shell
$ jstat -gccapacity  -t -h4 11116  1000 10
```

![img](img\be1dbc9fb1100c4ab76fdf802171c000.png)

NGCMN	新生代最小容量
NGCMX	新生代最大容量
NGC	当前新生代容量
S0C	第一个Survivor区大小
S1C	第二个Survivor区的大小
EC	Eden区的大小
OGCMN	老年代最小容量
OGCMX	老年代最大容量
OGC	当前老年代大小
OC	当前老年代大小
MCMN	最小元数据容量
MCMX	最大元数据容量
MC	当前元数据空间大小
CCSMN	最小压缩类空间大小
CCSMX	最大压缩类空间大小
CCSC	当前压缩类空间大小
YGC	年轻代GC次数
FGC	老年代GC次数

**jstat -gcutil**

```shell
$ jstat -gcutil -h4 5032 1000
```

![img](img\527f347102e0f48036f4e643103a735f.png)

| 字段   | 说明                         |
| ------ | ---------------------------- |
| `S0`   | 第一个Servivor区当前使用比例 |
| `S1`   | 第二个Servivor区当前使用比例 |
| `E`    | Eden区使用比例               |
| `O`    | 老年代使用比例               |
| `M`    | 元数据区使用比例             |
| `CCS`  | 压缩使用比例                 |
| `YGC`  | 年轻代垃圾回收次数           |
| `FGC`  | 老年代垃圾回收次数           |
| `FGCT` | 老年代垃圾回收消耗时间       |
| `GCT`  | 垃圾回收消耗总时间           |



**jstat -gccause**

```shell
$ jstat -gccause -h4 16048  1000 10
```

![img](img\2e5d220a3ceb094b3d6aee8b46867942.png)

字段	说明
S0	第一个Servivor区当前使用比例
S1	第二个Servivor区当前使用比例
E	Eden区使用比例
O	老年代使用比例
M	元数据区使用比例
CCS	压缩使用比例
YGC	年轻代垃圾回收次数
FGC	老年代垃圾回收次数
FGCT	老年代垃圾回收消耗时间
GCT	垃圾回收消耗总时间
LGCC	最近垃圾回收的原因
GCC	当前垃圾回收的原因

**jstat -gcnew**

```shell
$ jstat -gcnew  -t -h4 11116  1000 10
```

![img](img\766a9d8c98c1add9ff60f001fcbe552b.png)

S0C	第一个Survivor区大小
S1C	第二个Survivor区的大小
S0U	第一个Survivor区的使用大小
S1U	第二个Survivor区的使用大小
TT	对象在新生代存活的次数
MTT	对象在新生代存活的最大次数
DSS	期望的Survivor区大小
EC	Eden区的大小
EU	Eden区的使用大小
YGC	年轻代垃圾回收次数
YGCT	年轻代垃圾回收消耗时间



**jstat -gcnewcapacity**

```shell
$ jstat -gccapacity  -t -h4 11116  1000 10
```

![img](img\d26356900de541c149df9c00852245a1.png)

NGCMN	新生代最小容量
NGCMX	新生代最大容量
NGC	当前新生代容量
S0CMX	第一个Survivor区最大大小
S0C	第一个Survivor区当前大小
S1CMX	第二个Survivor区最大大小
S1C	第二个Survivor区当前大小
ECMX	Eden区最大大小
EC	Eden区当前大小
YGC	年轻代垃圾回收次数
FGC	老年代回收次数

**jstat -gcold**

```shell
$ jstat -gcold  -t -h4 11116  1000 10
```

![img](img\64f18adec84996fec58edf7052440610.png)

| 字段   | 说明                   |
| ------ | ---------------------- |
| `MC`   | 方法区大小             |
| `MU`   | 方法区使用大小         |
| `CCSC` | 压缩类空间大小         |
| `CCSU` | 压缩类空间使用大小     |
| `OC`   | 老年代大小             |
| `OU`   | 老年代使用大小         |
| `YGC`  | 年轻代垃圾回收次数     |
| `FGC`  | 老年代垃圾回收次数     |
| `FGCT` | 老年代垃圾回收消耗时间 |
| `GCT`  | 垃圾回收消耗总时间     |

**jstat -gcoldcapacity**

```shell
$ jstat -gcoldcapacity  -t -h4 11116  1000 10
```

![img](img\52bf3b50ba4a48247742caa0aa30be7e.png)

| 字段    | 说明                   |
| ------- | ---------------------- |
| `OGCMN` | 老年代最小容量         |
| `OGCMX` | 老年代最大容量         |
| `OGC`   | 当前老年代大小         |
| `OC`    | 老年代大小             |
| `YGC`   | 年轻代垃圾回收次数     |
| `FGC`   | 老年代垃圾回收次数     |
| `FGCT`  | 老年代垃圾回收消耗时间 |
| `GCT`   | 垃圾回收消耗总时间     |



**jstat -t**

![img](img\61a5c6b9c421ba9ec38db1f132ef4161.png)

**jstat -t -h**

![img](img\73a294c043f770940daa6a501c1e8d2c.png)

| 表头 | 含义（字节）                                                 |
| :--- | :----------------------------------------------------------- |
| EC   | Eden 区的大小                                                |
| EU   | Eden 区已使用的大小                                          |
| S0C  | 幸存者 0 区的大小                                            |
| S1C  | 幸存者 1 区的大小                                            |
| S0U  | 幸存者 0 区已使用的大小                                      |
| S1U  | 幸存者 1 区已使用的大小                                      |
| MC   | 元空间的大小                                                 |
| MU   | 元空间已使用的大小                                           |
| OC   | 老年代的大小                                                 |
| OU   | 老年代已使用的大小                                           |
| CCSC | 压缩类空间的大小                                             |
| CCSU | 压缩类空间已使用的大小                                       |
| YGC  | 从应用程序启动到采样时 young gc 的次数                       |
| YGCT | 从应用程序启动到采样时 young gc 消耗时间（秒）               |
| FGC  | 从应用程序启动到采样时 full gc 的次数                        |
| FGCT | 从应用程序启动到采样时的 full gc 的消耗时间（秒）            |
| GCT  | 从应用程序启动到采样时 gc 的总时间                           |
| LGCC | 最近垃圾回收的原因 （触发 Pause Young (G1 Evacuation Pause)） |
| GCC  | 当前垃圾回收的原因                                           |



**interval 参数：** 用于指定输出统计数据的周期，单位为毫秒。即：查询间隔

**count 参数：** 用于指定查询的总次数

**-t 参数：** 可以在输出信息前加上一个 Timestamp 列，显示程序的运行时间。单位：秒

**-h 参数：** 可以在周期性数据输出时，输出多少行数据后输出一个表头信息

**补充：** jstat 还可以用来判断是否出现内存泄漏。

第 1 步：在长时间运行的 Java 程序中，我们可以运行 jstat 命令连续获取多行性能数据，并取这几行数据中 OU 列（即已占用的老年代内存）的最小值。

第 2 步：然后，我们每隔一段较长的时间重复一次上述操作，来获得多组 OU 最小值。如果这些值呈上涨趋势，则说明该 Java 程序的老年代内存已使用量在不断上涨，这意味着无法回收的对象在不断增加，因此很有可能存在内存泄漏。

**经验：**

我们在查看GC的时候要加上 -t  

用程序运行时间及总GC （GCT 列）的时间间隔，测出GC时间占运行时间的比例（GC列的差值计算比例）

如果超过20% ，则说明堆的压力较大（GC 时间过多了）

如果超过90%，则说明随时可能报OOM

$ jstat -gc  -t -h4 11116  1000 10，这代表1秒打印出1行，一共10行，-t代表打印出Timestamp总运行时间，结果如下所示：

![](img\fa3c5e41cbf999d261bcf32951831765.png)

上方红色框框中代表Timestamp，而蓝色框框中代表垃圾回收时间，单位都是秒，如果让红色框框中的某两个值相减，假设这个值是num1，然后让对应行的蓝色框框中的另外两个值相减，假设这个值是num2，之后让num2/num1，得出的差值就是上述所说的GC时间占运行时间的比例

虽然这种方式比较繁琐，但是在项目部署之后就需要使用命令行去看了，就没有可视化界面了，所以这种方式也要会



```shell
$ jstat -class 11388
Loaded  Bytes  Unloaded  Bytes     Time
  1068  2513.7        0     0.0       4.15
Loaded :加载class个数
Bytes：加载class大小
Unloaded：卸载class个数
Bytes：卸载class大小
Time：类装载所消耗的时间
```

```shell
## 每个1000毫秒查询一次
$ jstat -class 11388 1000
Loaded  Bytes  Unloaded  Bytes     Time
  1068  2513.7        0     0.0       4.15
  1068  2513.7        0     0.0       4.15
  1068  2513.7        0     0.0       4.15

```

```shell
## 每个1000毫秒查询一次，总共查 5次
$ jstat -class 11388 1000 5
Loaded  Bytes  Unloaded  Bytes     Time
  1068  2513.7        0     0.0       4.15
  1068  2513.7        0     0.0       4.15
  1068  2513.7        0     0.0       4.15
  1068  2513.7        0     0.0       4.15
  1068  2513.7        0     0.0       4.15

```

```shell
##程序一共运行的多少秒 -t  ,注意在-class 后面
$ jstat -class -t 11388 1000 5
Timestamp       Loaded  Bytes  Unloaded  Bytes     Time
          508.2   1068  2513.7        0     0.0       4.15
          509.2   1068  2513.7        0     0.0       4.15
          510.2   1068  2513.7        0     0.0       4.15
          511.2   1068  2513.7        0     0.0       4.15
          512.2   1068  2513.7        0     0.0       4.15

```

```shell
## -h3 每隔3行输出一个表头  ,注意在-t 后面
$ jstat -class -t -h3 11388 1000 10
Timestamp       Loaded  Bytes  Unloaded  Bytes     Time
          686.8   1068  2513.7        0     0.0       4.15
          687.8   1068  2513.7        0     0.0       4.15
          688.9   1068  2513.7        0     0.0       4.15
Timestamp       Loaded  Bytes  Unloaded  Bytes     Time
          689.9   1068  2513.7        0     0.0       4.15
          690.9   1068  2513.7        0     0.0       4.15
          691.8   1068  2513.7        0     0.0       4.15
Timestamp       Loaded  Bytes  Unloaded  Bytes     Time
          692.8   1068  2513.7        0     0.0       4.15
          693.9   1068  2513.7        0     0.0       4.15
          694.9   1068  2513.7        0     0.0       4.15
Timestamp       Loaded  Bytes  Unloaded  Bytes     Time
          695.8   1068  2513.7        0     0.0       4.15

```

```shell
## 显示 JIT 编译器编译过的方法、耗时等信息
$ jstat -compiler 11388
Compiled Failed Invalid   Time   FailedType FailedMethod
    7671      0       0     7.74          0

```

```shell
## JIT最近执行编译过的情况
$ jstat -printcompilation 11388
Compiled  Size  Type Method
    7671    126    1 java/io/Reader read
    
Compiled - 最近执行的编译任务次数
Size - 最近编译方法的大小
Type - 最新编译方法的类型
Method - 最新编译方法的名字
```



jstat -gc 

```java
package com.tools;

import lombok.SneakyThrows;

import java.util.ArrayList;

public class GCTest {
    /**
     * -Xms600m -Xmx600m -XX:SurvivorRatio=8
     */
    @SneakyThrows
    public static void main(String[] args) {
        var list = new ArrayList<byte[]>();
        for (var i = 0; i < 1000; i++) {
            //100Kb
            var arr = new byte[1024 * 100];
            list.add(arr);
            Thread.sleep(120);
        }
        list.forEach(System.out::println);

    }
}

```

![](img\fa3c5e41cbf999d261bcf32851831765.png)

```shell
### 堆空间占比
$ jstat -gcutil -h4 5032 1000 10
  S0     S1     E      O      M     CCS    YGC     YGCT     FGC    FGCT     CGC    CGCT       GCT
  0.00   0.00  65.22   0.00      -      -      0     0.000     0     0.000     0     0.000     0.000
  0.00   0.00  69.57   0.00      -      -      0     0.000     0     0.000     0     0.000     0.000
  0.00   0.00  73.91   0.00      -      -      0     0.000     0     0.000     0     0.000     0.000
  0.00   0.00  78.26   0.00      -      -      0     0.000     0     0.000     0     0.000     0.000
  S0     S1     E      O      M     CCS    YGC     YGCT     FGC    FGCT     CGC    CGCT       GCT
  0.00   0.00  82.61   0.00      -      -      0     0.000     0     0.000     0     0.000     0.000
  0.00   0.00  82.61   0.00      -      -      0     0.000     0     0.000     0     0.000     0.000

```

```shell

### 堆空间占比 比上面的多了，LGCC ：最新GC的原因   GCC：当前GC原因
$ jstat -gccause -h4 11116  1000 10
  S0     S1     E      O      M     CCS    YGC     YGCT     FGC    FGCT     CGC    CGCT       GCT    LGCC                 GCC
  0.00  74.25   2.22   4.50  98.64  94.56      2     0.006     0     0.000     0     0.000     0.006 G1 Evacuation Pause  No GC
  0.00  74.25   2.22   4.50  98.64  94.56      2     0.006     0     0.000     0     0.000     0.006 G1 Evacuation Pause  No GC
  0.00  74.25   2.22   4.50  98.64  94.56      2     0.006     0     0.000     0     0.000     0.006 G1 Evacuation Pause  No GC
  0.00  74.25   2.22   4.50  98.64  94.56      2     0.006     0     0.000     0     0.000     0.006 G1 Evacuation Pause  No GC
  S0     S1     E      O      M     CCS    YGC     YGCT     FGC    FGCT     CGC    CGCT       GCT    LGCC                 GCC
  0.00  74.25   2.22   4.50  98.64  94.56      2     0.006     0     0.000     0     0.000     0.006 G1 Evacuation Pause  No GC
  0.00  74.25   2.22   4.50  98.64  94.56      2     0.006     0     0.000     0     0.000     0.006 G1 Evacuation Pause  No GC
  0.00  74.25   2.22   4.50  98.64  94.56      2     0.006     0     0.000     0     0.000     0.006 G1 Evacuation Pause  No GC
  0.00  74.25   2.22   4.50  98.64  94.56      2     0.006     0     0.000     0     0.000     0.006 G1 Evacuation Pause  No GC
  S0     S1     E      O      M     CCS    YGC     YGCT     FGC    FGCT     CGC    CGCT       GCT    LGCC                 GCC
  0.00  74.25   2.22   4.50  98.64  94.56      2     0.006     0     0.000     0     0.000     0.006 G1 Evacuation Pause  No GC
  0.00  74.25   2.22   4.50  98.64  94.56      2     0.006     0     0.000     0     0.000     0.006 G1 Evacuation Pause  No GC

```







##  2.4. jinfo：实时查看和修改 JVM 配置参数

jps -v 只能看到我们设置的参数，不设置的看不到，这时候可以用jinfo可以看到



jinfo(Configuration Info for Java)：查看虚拟机配置参数信息，也可用于调整虚拟机的配置参数。在很多情况卡，Java 应用程序不会指定所有的 Java 虚拟机参数。而此时，开发人员可能不知道某一个具体的 Java 虚拟机参数的默认值。在这种情况下，可能需要通过查找文档获取某个参数的默认值。这个查找过程可能是非常艰难的。但有了 jinfo 工具，开发人员可以很方便地找到 Java 虚拟机参数的当前值。

基本使用语法为：jinfo [options] pid

说明：java 进程 ID 必须要加上

| 选项             | 选项说明                                                                  |
| ---------------- | ------------------------------------------------------------------------- |
| no option        | 输出全部的参数和系统属性                                                  |
| -flag name       | 输出对应名称的参数                                                        |
| -flag [+-]name   | 开启或者关闭对应名称的参数 只有被标记为 manageable 的参数才可以被动态修改 |
| -flag name=value | 设定对应名称的参数                                                        |
| -flags           | 输出全部的参数                                                            |
| -sysprops        | 输出系统属性                                                              |

**jinfo -sysprops** ：查看系统属性

```properties
> $ jinfo -sysprops 14636
jboss.modules.system.pkgs = com.intellij.rt
java.vendor = Oracle Corporation
sun.java.launcher = SUN_STANDARD
sun.management.compiler = HotSpot 64-Bit Tiered Compilers
catalina.useNaming = true
os.name = Windows 10
...
```

**jinfo -flags** ： 查看全部的参数

```shell
> jinfo -flags 25592
Non-default VM flags: -XX:CICompilerCount=4 -XX:InitialHeapSize=333447168 -XX:MaxHeapSize=5324668928 -XX:MaxNewSize=1774714880 -XX:MinHeapDeltaBytes=524288 -XX:NewSize=111149056 -XX:OldSize=222298112 -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseFastUnorderedTimeStamps -XX:-UseLargePagesIndividualAllocation -XX:+UseParallelGC
Command line:  -agentlib:jdwp=transport=dt_socket,address=127.0.0.1:8040,suspend=y,server=n -Drebel.base=C:\Users\Vector\.jrebel -Drebel.env.ide.plugin.version=2021.1.2 -Drebel.env.ide.version=2020.3.3 -Drebel.env.ide.product=IU -Drebel.env.ide=intellij -Drebel.notification.url=http://localhost:7976 -agentpath:C:\Users\Vector\AppData\Roaming\JetBrains\IntelliJIdea2020.3\plugins\jr-ide-idea\lib\jrebel6\lib\jrebel64.dll -Dmaven.home=D:\eclipse\env\maven -Didea.modules.paths.file=C:\Users\Vector\AppData\Local\JetBrains\IntelliJIdea2020.3\Maven\idea-projects-state-596682c7.properties -Dclassworlds.conf=C:\Users\Vector\AppData\Local\Temp\idea-6755-mvn.conf -Dmaven.ext.class.path=D:\IDEA\plugins\maven\lib\maven-event-listener.jar -javaagent:D:\IDEA\plugins\java\lib\rt\debugger-agent.jar -Dfile.encoding=UTF-8
```

**jinfo -flag**  查看对应名称的参数

```shell
> jinfo -flag UseParallelGC 25592
-XX:+UseParallelGC

> jinfo -flag UseG1GC 25592
-XX:-UseG1GC
```

**jinfo -flag name **查看对应名称的参数

```shell
> jinfo -flag UseParallelGC 25592
-XX:+UseParallelGC

> jinfo -flag UseG1GC 25592
-XX:-UseG1GC
```

**jinfo -flag [+-]name** ： 修改对应名称的参数

```shell
> jinfo -flag +PrintGCDetails 25592
> jinfo -flag PrintGCDetails 25592
-XX:+PrintGCDetails

> jinfo -flag -PrintGCDetails 25592
> jinfo -flag PrintGCDetails 25592
-XX:-PrintGCDetails
```



jinfo -flag [+-]name 修改，立即生效，但不是支持所有的参数修改

只有标记为 manageable 才可以被修改

java -XX:+PrintFlagsFinal -version |grep manageable 

可以看到可以修改的属性不多

```shell
$ java -XX:+PrintFlagsFinal -version |grep manageable
openjdk version "17.0.1" 2021-10-19
OpenJDK Runtime Environment (build 17.0.1+12-39)
OpenJDK 64-Bit Server VM (build 17.0.1+12-39, mixed mode, sharing)
    uintx G1PeriodicGCInterval                     = 0                                      {manageable} {default}
   double G1PeriodicGCSystemLoadThreshold          = 0.000000                               {manageable} {default}
     bool HeapDumpAfterFullGC                      = false                                  {manageable} {default}
     bool HeapDumpBeforeFullGC                     = false                                  {manageable} {default}
     intx HeapDumpGzipLevel                        = 0                                      {manageable} {default}
     bool HeapDumpOnOutOfMemoryError               = false                                  {manageable} {default}
    ccstr HeapDumpPath                             =                                        {manageable} {default}
    uintx MaxHeapFreeRatio                         = 70                                     {manageable} {default}
    uintx MinHeapFreeRatio                         = 40                                     {manageable} {default}
     bool PrintClassHistogram                      = false                                  {manageable} {default}
     bool PrintConcurrentLocks                     = false                                  {manageable} {default}
     bool ShowCodeDetailsInExceptionMessages       = true                                   {manageable} {default}
   size_t SoftMaxHeapSize                          = 4278190080                             {manageable} {ergonomic}

```

```shell
##修改到不可以改的参数
$ jinfo -flag +PrintGCDetails 11472
Exception in thread "main" com.sun.tools.attach.AttachOperationFailedException: flag 'PrintGCDetails' cannot be changed
        at jdk.attach/sun.tools.attach.VirtualMachineImpl.execute(VirtualMachineImpl.java:130)
        at jdk.attach/sun.tools.attach.HotSpotVirtualMachine.executeCommand(HotSpotVirtualMachine.java:310)
        at jdk.attach/sun.tools.attach.HotSpotVirtualMachine.setFlag(HotSpotVirtualMachine.java:283)
        at jdk.jcmd/sun.tools.jinfo.JInfo.flag(JInfo.java:152)
        at jdk.jcmd/sun.tools.jinfo.JInfo.main(JInfo.java:127)

```





拓展：

- java -XX:+PrintFlagsInitial  > 1.txt 查看所有 JVM 参数启动的初始值

  ```shell
  [Global flags]
       intx ActiveProcessorCount                      = -1                                  {product}
      uintx AdaptiveSizeDecrementScaleFactor          = 4                                   {product}
      uintx AdaptiveSizeMajorGCDecayTimeScale         = 10                                  {product}
      uintx AdaptiveSizePausePolicy                   = 0                                   {product}
  ...
  ```

- java -XX:+PrintFlagsFinal  > 2.txt 查看所有 JVM 参数的最终值

  ```shell
  [Global flags]
       intx ActiveProcessorCount                      = -1                                  {product}
  ...
       intx CICompilerCount                          := 4                                   {product}
      uintx InitialHeapSize                          := 333447168                           {product}
      uintx MaxHeapSize                              := 1029701632                          {product}
      uintx MaxNewSize                               := 1774714880                          {product}
  ```

- java -XX:+PrintCommandLineFlags > 3.txt 查看哪些已经被用户或者 JVM 设置过的详细的 XX 参数的名称和值

  ```shell
  -XX:InitialHeapSize=332790016 -XX:MaxHeapSize=5324640256 -XX:+PrintCommandLineFlags -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:-UseLargePagesIndividualAllocation -XX:+UseParallelGC
  ```







## 2.5. jmap：导出内存映像文件&内存使用情况

jmap 只是能查看执行命令时间节点的 内存情况，不像 jstat  可以 周期查看 interval 和 count



jmap（JVM Memory Map）：作用一方面是获取 dump 文件（堆转储快照文件，二进制文件），它还可以获取目标 Java 进程的内存相关信息，包括 Java 堆各区域的使用情况、堆中对象的统计信息、类加载信息等。开发人员可以在控制台中输入命令“jmap -help”查阅 jmap 工具的具体使用方式和一些标准选项配置。

官方帮助文档：[https://docs.oracle.com/en/java/javase/11/tools/jmap.html](https://docs.oracle.com/en/java/javase/11/tools/jmap.html)

基本使用语法为：

- jmap [option] &lt;pid&gt;
- jmap [option] &lt;executable &lt;core&gt;
- jmap [option] [server_id@] &lt;remote server IP or hostname&gt;

| 选项            | 作用                                                                                      |
| :-------------- | :---------------------------------------------------------------------------------------- |
| -dump           | 生成 dump 文件（Java 堆转储快照），-dump:live 只保存堆中的存活对象                        |
| -heap           | 输出整个堆空间的详细信息，包括 GC 的使用、堆配置信息，以及内存的使用信息等                |
| -histo          | 输出堆空间中对象的统计信息，包括类、实例数量和合计容量，-histo:live 只统计堆中的存活对象  |
| -J &lt;flag&gt; | 传递参数给 jmap 启动的 jvm                                                                |
| -finalizerinfo  | 显示在 F-Queue 中等待 Finalizer 线程执行 finalize 方法的对象，仅 linux/solaris 平台有效   |
| -permstat       | 以 ClassLoader 为统计口径输出永久代的内存状态信息，仅 linux/solaris 平台有效              |
| -F              | 当虚拟机进程对-dump 选项没有任何响应时，强制执行生成 dump 文件，仅 linux/solaris 平台有效 |

说明：这些参数和 linux 下输入显示的命令多少会有不同，包括也受 jdk 版本的影响。

```shell
> jmap -dump:format=b,file=<filename.hprof> <pid>> jmap -dump:live,format=b,file=<filename.hprof> <pid>
```

由于 jmap 将访问堆中的所有对象，为了保证在此过程中不被应用线程干扰，jmap 需要借助安全点机制，让所有线程停留在不改变堆中数据的状态。也就是说，由 jmap 导出的堆快照必定是安全点位置的。这可能导致基于该堆快照的分析结果存在偏差。

举个例子，假设在编译生成的机器码中，某些对象的生命周期在两个安全点之间，那么:live 选项将无法探知到这些对象。

另外，如果某个线程长时间无法跑到安全点，jmap 将一直等下去。与前面讲的 jstat 则不同，垃圾回收器会主动将 jstat 所需要的摘要数据保存至固定位置之中，而 jstat 只需直接读取即可。





## 手动导出

 **jmap 导出的堆快照必定是安全点位置的，可能导致基于该堆快照的分析结果存在偏差**



### jmap  -dump pid : 内存快照

format=b 标准的二进制

live 只dump存活的对象，生产环境使用这个

```shell
jmap -dump:live,file=E:/GitHub/live.hprof 11472
jmap -dump:all,file=E:/GitHub/all.hprof 11472
jmap -dump:format=b,file=E:/GitHub/b.hprof 11472   
jmap -dump:all,format=b,file=E:/GitHub/b.hprof 11472   
jmap -dump:live,format=b,file=E:/GitHub/b.hprof 11472   
  
 
# 在当前目录下生成 heap.bin
jhsdb jmap --pid 11472 --binaryheap
# 指定文件目录
jhsdb jmap --pid 11472 --binaryheap --dumpfile E:/GitHub/jhsdb.hprof

```



### jmap -heap ：整个堆空间的详细信息

```shell
Attaching to process ID 12644, please wait...
Debugger attached successfully.
Server compiler detected.
JVM version is 17.0.1+12-39

using thread-local object allocation.
Garbage-First (G1) GC with 6 thread(s)

Heap Configuration:
   MinHeapFreeRatio         = 40
   MaxHeapFreeRatio         = 70
   MaxHeapSize              = 62914560 (60.0MB)
   NewSize                  = 1363144 (1.2999954223632812MB)
   MaxNewSize               = 37748736 (36.0MB)
   OldSize                  = 5452592 (5.1999969482421875MB)
   NewRatio                 = 2
   SurvivorRatio            = 8
   MetaspaceSize            = 22020096 (21.0MB)
   CompressedClassSpaceSize = 1073741824 (1024.0MB)
   MaxMetaspaceSize         = 17592186044415 MB
   G1HeapRegionSize         = 1048576 (1.0MB)

Heap Usage:
G1 Heap:
   regions  = 60
   capacity = 62914560 (60.0MB)
   used     = 54628864 (52.09814453125MB)
   free     = 8285696 (7.90185546875MB)
   86.83024088541667% used
G1 Young Generation:
Eden Space:
   regions  = 0
   capacity = 3145728 (3.0MB)
   used     = 0 (0.0MB)
   free     = 3145728 (3.0MB)
   0.0% used
Survivor Space:
   regions  = 1
   capacity = 1048576 (1.0MB)
   used     = 1048576 (1.0MB)
   free     = 0 (0.0MB)
   100.0% used
G1 Old Generation:
   regions  = 52
   capacity = 58720256 (56.0MB)
   used     = 53580288 (51.09814453125MB)
   free     = 5139968 (4.90185546875MB)
   91.24668666294643% used
```

jdk 9 开始使用 jhsdb jmap --heap --pid 11472  代替 ,不能再使用jmap -heap pid的命令了

```shell
$ jhsdb jmap --heap --pid 11472 

$ jhsdb jmap --heap --pid 11472 > 1.txt
```

使用：visualvm 查看



### jmap -histo ：堆空间中对象的统计信息

jmap -histo:live : 记录安全点的堆中存活的对象

```shell
jmap -histo 11472
jmap -histo:live 11472
jmap -histo:all 11472
jmap -histo:all,file=E:/GitHub/histo.txt 11472
```

```shell
num     #instances         #bytes  class name (module)
-------------------------------------------------------
   1:          7420       11773952  [B (java.base@17.0.1)
   2:           208         254904  [I (java.base@17.0.1)
   3:          6822         163728  java.lang.String (java.base@17.0.1)
   4:           138         158776  [C (java.base@17.0.1)
   5:          1123         137904  java.lang.Class (java.base@17.0.1)
   6:          1148          89824  [Ljava.lang.Object; (java.base@17.0.1)
   7:          2726          87232  java.util.HashMap$Node (java.base@17.0.1)
   8:           337          43024  [Ljava.util.HashMap$Node; (java.base@17.0.1)
   9:          1135          36320  java.util.concurrent.ConcurrentHashMap$Node (java.base@17.0.1)
  10:            50          18208  [Ljava.util.concurrent.ConcurrentHashMap$Node; (java.base@17.0.1)
  11:           342          16416  java.util.HashMap (java.base@17.0.1)
  12:           370           8880  java.lang.module.ModuleDescriptor$Exports (java.base@17.0.1)
  13:            87           6688  [Ljava.lang.String; (java.base@17.0.1)
  14:            71           5680  java.net.URI (java.base@17.0.1)
  15:           227           5448  java.util.ImmutableCollections$Set12 (java.base@17.0.1)
  16:           168           5376  java.lang.module.ModuleDescriptor$Requires (java.base@17.0.1)
  17:           102           4896  java.lang.invoke.MemberName (java.base@17.0.1)
```



## 自动导出

-XX:+HeapDumpOnOutOfMemoryError

-XX:HeapDumpPath=<filename.hprof>

-XX:+HeapDumpAfterFullGC 或者 -XX:+HeapDumpBeforeFullGC  



## 2.6. jhat：JDK 自带堆分析工具

jhat(JVM Heap Analysis Tool)：Sun JDK 提供的 jhat 命令与 jmap 命令搭配使用，用于分析 jmap 生成的 heap dump 文件（堆转储快照）。jhat 内置了一个微型的 HTTP/HTML 服务器，生成 dump 文件的分析结果后，用户可以在浏览器中查看分析结果（分析虚拟机转储快照信息）。

使用了 jhat 命令，就启动了一个 http 服务，端口是 7000，即 http://localhost:7000/，就可以在浏览器里分析。

说明：jhat 命令在 JDK9、JDK10 中已经被删除，官方建议用 VisualVM 代替。

基本适用语法：jhat &lt;option&gt; &lt;dumpfile&gt;

| option 参数            | 作用                                      |
| :--------------------- | :---------------------------------------- |
| -stack false ｜ true   | 关闭｜打开对象分配调用栈跟踪              |
| -refs false ｜ true    | 关闭｜打开对象引用跟踪                    |
| -port port-number      | 设置 jhat HTTP Server 的端口号，默认 7000 |
| -exclude exclude-file  | 执行对象查询时需要排除的数据成员          |
| -baseline exclude-file | 指定一个基准堆转储                        |
| -debug int             | 设置 debug 级别                           |
| -version               | 启动后显示版本信息就退出                  |
| -J &lt;flag&gt;        | 传入启动参数，比如-J-Xmx512m              |

## 2.7. jstack：打印 JVM 中线程快照

jstack（JVM Stack Trace）：用于生成虚拟机指定进程当前时刻的线程快照（虚拟机堆栈跟踪）。线程快照就是当前虚拟机内指定进程的每一条线程正在执行的方法堆栈的集合。

生成线程快照的作用：可用于定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的长时间等待等问题。这些都是导致线程长时间停顿的常见原因。当线程出现停顿时，就可以用 jstack 显示各个线程调用的堆栈情况。

官方帮助文档：[https://docs.oracle.com/en/java/javase/11/tools/jstack.html](https://docs.oracle.com/en/java/javase/11/tools/jstack.html)

在 thread dump 中，要留意下面几种状态

- <mark>死锁，Deadlock（重点关注）</mark>
- <mark>等待资源，Waiting on condition（重点关注）</mark>
- <mark>等待获取监视器，Waiting on monitor entry（重点关注）</mark>
- <mark>阻塞，Blocked（重点关注）</mark>
- 执行中，Runnable
- 暂停，Suspended
- 对象等待中，Object.wait() 或 TIMED＿WAITING
- 停止，Parked

| option 参数 | 作用                                         |
| :---------- | :------------------------------------------- |
| -F          | 当正常输出的请求不被响应时，强制输出线程堆栈 |
| -l          | 除堆栈外，显示关于锁的附加信息               |
| -m          | 如果调用本地方法的话，可以显示 C/C++的堆栈   |



每个线程都有一个虚拟机栈：这个命令就是打印线程的栈都打印出来

-l 除堆栈外，显示关于锁的附加信息

```shell
 jstack 19036
 jstack -e 19036 
 jstack -l 19036 > 1.txt 
```

```java
package com.tools;

public class ThreadSynchronized {
    public static void main(String[] args) throws InterruptedException {
        Object obj1 = new Object();
        Object obj2 = new Object();
        new Thread(() -> {
            synchronized (obj1) {
                try {
                    Thread.sleep(100);
                    synchronized (obj2){
                        System.out.println("obj2");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start();

        new Thread(() -> {
            synchronized (obj2) {
                try {
                    Thread.sleep(100);
                    synchronized (obj1) {
                        System.out.println("obj1");
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).start(); ;

        Thread.sleep(999999999999L);
    }
}

```

```shell
2023-05-14 05:10:52
Full thread dump OpenJDK 64-Bit Server VM (17.0.1+12-39 mixed mode, sharing):

Threads class SMR info:
_java_thread_list=0x00000176cf6363c0, length=15, elements={
0x00000176ab5f4820, 0x00000176ce9f1fe0, 0x00000176ce9f4d60, 0x00000176cf40a9f0,
0x00000176cf40c2c0, 0x00000176cf40cc70, 0x00000176cf40d620, 0x00000176cf40e3b0,
0x00000176cf45ed70, 0x00000176cf469370, 0x00000176ce9dd830, 0x00000176cf5893e0,
0x00000176cf5898b0, 0x00000176cf636670, 0x00000176cf638b50
}

"main" #1 prio=5 os_prio=0 cpu=93.75ms elapsed=26.12s tid=0x00000176ab5f4820 nid=0x4804 waiting on condition  [0x00000067ccaff000]
   java.lang.Thread.State: TIMED_WAITING (sleeping)
	at java.lang.Thread.sleep(java.base@17.0.1/Native Method)
	at com.tools.ThreadSynchronized.main(ThreadSynchronized.java:36)

   Locked ownable synchronizers:
	- None

"Reference Handler" #2 daemon prio=10 os_prio=2 cpu=0.00ms elapsed=26.11s tid=0x00000176ce9f1fe0 nid=0x3448 waiting on condition  [0x00000067cd1ff000]
   java.lang.Thread.State: RUNNABLE
	at java.lang.ref.Reference.waitForReferencePendingList(java.base@17.0.1/Native Method)
	at java.lang.ref.Reference.processPendingReferences(java.base@17.0.1/Reference.java:253)
	at java.lang.ref.Reference$ReferenceHandler.run(java.base@17.0.1/Reference.java:215)

   Locked ownable synchronizers:
	- None

"Finalizer" #3 daemon prio=8 os_prio=1 cpu=0.00ms elapsed=26.11s tid=0x00000176ce9f4d60 nid=0x27a0 in Object.wait()  [0x00000067cd2ff000]
   java.lang.Thread.State: WAITING (on object monitor)
	at java.lang.Object.wait(java.base@17.0.1/Native Method)
	- waiting on <0x0000000710e0d5a8> (a java.lang.ref.ReferenceQueue$Lock)
	at java.lang.ref.ReferenceQueue.remove(java.base@17.0.1/ReferenceQueue.java:155)
	- locked <0x0000000710e0d5a8> (a java.lang.ref.ReferenceQueue$Lock)
	at java.lang.ref.ReferenceQueue.remove(java.base@17.0.1/ReferenceQueue.java:176)
	at java.lang.ref.Finalizer$FinalizerThread.run(java.base@17.0.1/Finalizer.java:172)

   Locked ownable synchronizers:
	- None

"Signal Dispatcher" #4 daemon prio=9 os_prio=2 cpu=0.00ms elapsed=26.10s tid=0x00000176cf40a9f0 nid=0x4078 waiting on condition  [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"Attach Listener" #5 daemon prio=5 os_prio=2 cpu=15.63ms elapsed=26.10s tid=0x00000176cf40c2c0 nid=0x312c waiting on condition  [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"Service Thread" #6 daemon prio=9 os_prio=0 cpu=0.00ms elapsed=26.10s tid=0x00000176cf40cc70 nid=0x42d4 runnable  [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"Monitor Deflation Thread" #7 daemon prio=9 os_prio=0 cpu=0.00ms elapsed=26.10s tid=0x00000176cf40d620 nid=0x4f7c runnable  [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"C2 CompilerThread0" #8 daemon prio=9 os_prio=2 cpu=46.88ms elapsed=26.10s tid=0x00000176cf40e3b0 nid=0x28fc waiting on condition  [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE
   No compile task

   Locked ownable synchronizers:
	- None

"C1 CompilerThread0" #10 daemon prio=9 os_prio=2 cpu=46.88ms elapsed=26.10s tid=0x00000176cf45ed70 nid=0x4e1c waiting on condition  [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE
   No compile task

   Locked ownable synchronizers:
	- None

"Sweeper thread" #11 daemon prio=9 os_prio=2 cpu=0.00ms elapsed=26.10s tid=0x00000176cf469370 nid=0x321c runnable  [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"Common-Cleaner" #12 daemon prio=8 os_prio=1 cpu=0.00ms elapsed=26.08s tid=0x00000176ce9dd830 nid=0x3e1c in Object.wait()  [0x00000067cdafe000]
   java.lang.Thread.State: TIMED_WAITING (on object monitor)
	at java.lang.Object.wait(java.base@17.0.1/Native Method)
	- waiting on <0x0000000710f05b18> (a java.lang.ref.ReferenceQueue$Lock)
	at java.lang.ref.ReferenceQueue.remove(java.base@17.0.1/ReferenceQueue.java:155)
	- locked <0x0000000710f05b18> (a java.lang.ref.ReferenceQueue$Lock)
	at jdk.internal.ref.CleanerImpl.run(java.base@17.0.1/CleanerImpl.java:140)
	at java.lang.Thread.run(java.base@17.0.1/Thread.java:833)
	at jdk.internal.misc.InnocuousThread.run(java.base@17.0.1/InnocuousThread.java:162)

   Locked ownable synchronizers:
	- None

"Monitor Ctrl-Break" #13 daemon prio=5 os_prio=0 cpu=0.00ms elapsed=26.03s tid=0x00000176cf5893e0 nid=0xec runnable  [0x00000067cdbfe000]
   java.lang.Thread.State: RUNNABLE
	at sun.nio.ch.SocketDispatcher.read0(java.base@17.0.1/Native Method)
	at sun.nio.ch.SocketDispatcher.read(java.base@17.0.1/SocketDispatcher.java:46)
	at sun.nio.ch.NioSocketImpl.tryRead(java.base@17.0.1/NioSocketImpl.java:261)
	at sun.nio.ch.NioSocketImpl.implRead(java.base@17.0.1/NioSocketImpl.java:312)
	at sun.nio.ch.NioSocketImpl.read(java.base@17.0.1/NioSocketImpl.java:350)
	at sun.nio.ch.NioSocketImpl$1.read(java.base@17.0.1/NioSocketImpl.java:803)
	at java.net.Socket$SocketInputStream.read(java.base@17.0.1/Socket.java:966)
	at sun.nio.cs.StreamDecoder.readBytes(java.base@17.0.1/StreamDecoder.java:270)
	at sun.nio.cs.StreamDecoder.implRead(java.base@17.0.1/StreamDecoder.java:313)
	at sun.nio.cs.StreamDecoder.read(java.base@17.0.1/StreamDecoder.java:188)
	- locked <0x0000000710ce7678> (a java.io.InputStreamReader)
	at java.io.InputStreamReader.read(java.base@17.0.1/InputStreamReader.java:177)
	at java.io.BufferedReader.fill(java.base@17.0.1/BufferedReader.java:162)
	at java.io.BufferedReader.readLine(java.base@17.0.1/BufferedReader.java:329)
	- locked <0x0000000710ce7678> (a java.io.InputStreamReader)
	at java.io.BufferedReader.readLine(java.base@17.0.1/BufferedReader.java:396)
	at com.intellij.rt.execution.application.AppMainV2$1.run(AppMainV2.java:49)

   Locked ownable synchronizers:
	- <0x0000000710cddfa8> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)

"Notification Thread" #14 daemon prio=9 os_prio=0 cpu=0.00ms elapsed=26.03s tid=0x00000176cf5898b0 nid=0x3f48 runnable  [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"Thread-0" #15 prio=5 os_prio=0 cpu=0.00ms elapsed=26.03s tid=0x00000176cf636670 nid=0x1bd0 waiting for monitor entry  [0x00000067cdefe000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at com.tools.ThreadSynchronized.lambda$main$0(ThreadSynchronized.java:12)
	- waiting to lock <0x0000000710c77910> (a java.lang.Object)
	- locked <0x0000000710c77900> (a java.lang.Object)
	at com.tools.ThreadSynchronized$$Lambda$14/0x0000000800c01200.run(Unknown Source)
	at java.lang.Thread.run(java.base@17.0.1/Thread.java:833)

   Locked ownable synchronizers:
	- None

"Thread-1" #16 prio=5 os_prio=0 cpu=0.00ms elapsed=26.03s tid=0x00000176cf638b50 nid=0x3850 waiting for monitor entry  [0x00000067cdfff000]
   java.lang.Thread.State: BLOCKED (on object monitor)
	at com.tools.ThreadSynchronized.lambda$main$1(ThreadSynchronized.java:26)
	- waiting to lock <0x0000000710c77900> (a java.lang.Object)
	- locked <0x0000000710c77910> (a java.lang.Object)
	at com.tools.ThreadSynchronized$$Lambda$15/0x0000000800c01420.run(Unknown Source)
	at java.lang.Thread.run(java.base@17.0.1/Thread.java:833)

   Locked ownable synchronizers:
	- None

"VM Thread" os_prio=2 cpu=0.00ms elapsed=26.11s tid=0x00000176ce9eaf60 nid=0x38dc runnable  

"GC Thread#0" os_prio=2 cpu=0.00ms elapsed=26.12s tid=0x00000176ab640df0 nid=0x42c4 runnable  

"G1 Main Marker" os_prio=2 cpu=0.00ms elapsed=26.12s tid=0x00000176ab651c30 nid=0x29a8 runnable  

"G1 Conc#0" os_prio=2 cpu=0.00ms elapsed=26.12s tid=0x00000176ab653200 nid=0x4134 runnable  

"G1 Refine#0" os_prio=2 cpu=0.00ms elapsed=26.12s tid=0x00000176ce8a4580 nid=0x3e08 runnable  

"G1 Service" os_prio=2 cpu=0.00ms elapsed=26.12s tid=0x00000176ce8a4fa0 nid=0x2a34 runnable  

"VM Periodic Task Thread" os_prio=2 cpu=0.00ms elapsed=26.03s tid=0x00000176ab659780 nid=0x2e9c waiting on condition  

JNI global refs: 14, weak refs: 0


Found one Java-level deadlock:
=============================
"Thread-0":
  waiting to lock monitor 0x00000176cf49fd70 (object 0x0000000710c77910, a java.lang.Object),
  which is held by "Thread-1"

"Thread-1":
  waiting to lock monitor 0x00000176cf4a0d30 (object 0x0000000710c77900, a java.lang.Object),
  which is held by "Thread-0"

Java stack information for the threads listed above:
===================================================
"Thread-0":
	at com.tools.ThreadSynchronized.lambda$main$0(ThreadSynchronized.java:12)
	- waiting to lock <0x0000000710c77910> (a java.lang.Object)
	- locked <0x0000000710c77900> (a java.lang.Object)
	at com.tools.ThreadSynchronized$$Lambda$14/0x0000000800c01200.run(Unknown Source)
	at java.lang.Thread.run(java.base@17.0.1/Thread.java:833)
"Thread-1":
	at com.tools.ThreadSynchronized.lambda$main$1(ThreadSynchronized.java:26)
	- waiting to lock <0x0000000710c77900> (a java.lang.Object)
	- locked <0x0000000710c77910> (a java.lang.Object)
	at com.tools.ThreadSynchronized$$Lambda$15/0x0000000800c01420.run(Unknown Source)
	at java.lang.Thread.run(java.base@17.0.1/Thread.java:833)

Found 1 deadlock.


```



```java
package com.tools;

import java.util.Map;

public class JstackTest {

    public static void main(String[] args) {
        //追踪所有线程的栈信息
        final Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        for (Map.Entry<Thread, StackTraceElement[]> threadEntry : allStackTraces.entrySet()) {
            final Thread key = threadEntry.getKey();
            System.out.println(" Thead :" + key.toString()  + "   name：" + key.getName());
            for (StackTraceElement stackTraceElement : threadEntry.getValue()) {
                System.out.println("         stack :" + stackTraceElement.toString());
            }
        }
    }
}

```





## 2.8. jcmd：多功能命令行

除了 jstat 之外所有命令的功能都有

在 JDK 1.7 以后，新增了一个命令行工具 jcmd。它是一个多功能的工具，可以用来实现前面除了 jstat 之外所有命令的功能。比如：用它来导出堆、内存使用、查看 Java 进程、导出线程信息、执行 GC、JVM 运行时间等。

官方帮助文档：[https://docs.oracle.com/en/java/javase/11/tools/jcmd.html](https://docs.oracle.com/en/java/javase/11/tools/jcmd.html)

jcmd 拥有 jmap 的大部分功能，并且在 Oracle 的官方网站上也推荐使用 jcmd 命令代 jmap 命令

**jcmd -l：**列出所有的 JVM 进程

**jcmd 进程号 help：**针对指定的进程，列出支持的所有具体命令

![image-20210504213044819](img\f3507ac3e24d40625f6c3d54c25c743b.png)

**jcmd 进程号 具体命令：**显示指定进程的指令命令的数据

- Thread.print 可以替换 jstack 指令
- GC.class_histogram 可以替换 jmap 中的-histo 操作
- GC.heap_dump 可以替换 jmap 中的-dump 操作
- GC.run 可以查看 GC 的执行情况
- VM.uptime 可以查看程序的总执行时间，可以替换 jstat 指令中的-t 操作
- VM.system_properties 可以替换 jinfo -sysprops 进程 id
- VM.flags 可以获取 JVM 的配置参数信息



jcmd 使用三部曲：

1. jcmd -l    （替换 jps）
2. jcmd pid help  (查看可以打印的项目)
3. jcmd pid Thread.print  （替换 jstack 指令）
4. jcmd pid GC.class_histogram （替换jmap 中的-histo 操作）



```shell
$ jcmd 10208 help
10208:
The following commands are available:
Compiler.CodeHeap_Analytics
Compiler.codecache
Compiler.codelist
Compiler.directives_add
Compiler.directives_clear
Compiler.directives_print
Compiler.directives_remove
Compiler.queue
GC.class_histogram //jmap 中的-histo 操作
GC.finalizer_info
GC.heap_dump  
GC.heap_info
GC.run
GC.run_finalization
JFR.check
JFR.configure
JFR.dump
JFR.start
JFR.stop
JVMTI.agent_load
JVMTI.data_dump
ManagementAgent.start
ManagementAgent.start_local
ManagementAgent.status
ManagementAgent.stop
Thread.print //替换jstack 
VM.cds
VM.class_hierarchy  // 加载类的层次结构
VM.classloader_stats //类加载器的状态
VM.classloaders
VM.command_line
VM.dynlibs //查看动态库信息--加载的dll
VM.events
VM.flags
VM.info
VM.log
VM.metaspace
VM.native_memory //本地内存
VM.print_touched_methods
VM.set_flag
VM.stringtable
VM.symboltable
VM.system_properties
VM.systemdictionary
VM.uptime //系统运行时间
VM.version
help

```



## 2.9. jstatd：远程主机信息收集

之前的指令只涉及到监控本机的 Java 应用程序，而在这些工具中，一些监控工具也支持对远程计算机的监控（如 jps、jstat）。为了启用远程监控，则需要配合使用 jstatd 工具。命令 jstatd 是一个 RMI 服务端程序，它的作用相当于代理服务器，建立本地计算机与远程监控工具的通信。jstatd 服务器将本机的 Java 应用程序信息传递到远程计算机。

![image-20210504213301077](img\2225de448c4af005aa0f72e84bba5e57.png)

<hr/>
