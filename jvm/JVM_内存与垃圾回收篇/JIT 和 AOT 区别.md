### JIT 

JIT，即Just-in-time, 即时编译，边运行边编译；

### AOT

AOT，Ahead Of Time，指运行前编译，是两种程序的编译方式





jit : 会把程序实时编译为机器字节码，然后虚拟机读取，打开程序

aot 是在安装的时候预先打包好了字节码放在手机内部储存里面，打开程序直接读取预先打包好的字节码



jit 启动的时候需要进行实时编译，需要做很多事情，所以相对启动速度较慢，

有运行时性能加成，可以跑得更快，不过需要一定时间和调用频率才能触发 JIT 的分层机制

并且可以做到动态生成代码等

当程序需要支持动态链接时，只能使用JIT



aot 直接读取字节码，启动快，但是无运行时性能加成，不能根据程序运行情况做进一步的优化



### JIT优点：

- 可以根据当前硬件情况实时编译生成最优机器指令（ps. AOT也可以做到，在用户使用是使用字节码根据机器情况在做一次编译）
- 可以根据当前程序的运行情况生成最优的[机器指令](https://www.zhihu.com/search?q=机器指令&search_source=Entity&hybrid_search_source=Entity&hybrid_search_extra={"sourceType"%3A"answer"%2C"sourceId"%3A2009699464})序列
- 当程序需要支持[动态链接](https://www.zhihu.com/search?q=动态链接&search_source=Entity&hybrid_search_source=Entity&hybrid_search_extra={"sourceType"%3A"answer"%2C"sourceId"%3A2009699464})时，只能使用JIT
- 可以根据进程中内存的实际情况调整代码，使内存能够更充分的利用

### JIT缺点：

- 编译需要占用运行时资源，会导致进程卡顿
- 由于编译时间需要占用运行时间，对于某些代码的[编译优化](https://www.zhihu.com/search?q=编译优化&search_source=Entity&hybrid_search_source=Entity&hybrid_search_extra={"sourceType"%3A"answer"%2C"sourceId"%3A2009699464})不能完全支持，需要在[程序流畅](https://www.zhihu.com/search?q=程序流畅&search_source=Entity&hybrid_search_source=Entity&hybrid_search_extra={"sourceType"%3A"answer"%2C"sourceId"%3A2009699464})和编译时间之间做权衡
- 在编译准备和识别频繁使用的方法需要占用时间，使得初始编译不能达到最高性能

### AOT优点：

- 在程序运行前编译，可以避免在运行时的编译性能消耗和内存消耗
- 可以在程序运行初期就达到最高性能
- 可以显著的加快程序的启动

### AOT缺点：

- 在程序运行前编译会使程序安装的时间增加
- 牺牲Java的一致性
- 将提前编译的内容保存会占用更多的外



Java 9 引入了实验性的 AOT 编译功能（通过 **jaotc** 工具实现），但在 Java 16 中已被移除。

目前，Java 中 AOT 编译的主要实现是通过 **GraalVM** 提供的 Native Image 功能