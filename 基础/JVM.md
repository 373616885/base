### JDK，JRE，JVM的关系

![](img\20200610231132.png)

![](img\20190914142941228.png)****

### JDK

JDK  包含了JRE，同时还包含了编译java源码的编译器javac，还包含了很多java程序调试和分析的工具：jconsole，jvisualvm等工具软件 （这些工具的实现类在  和 tools.jar 里面 ）

**和 JRE 区别是 多了 编译器javac，java程序调试和分析的工具这些**

JDK包含的基本组件包括：

1. javac – 编译器，将源程序转成字节码
2. jar – 打包工具，将相关的类文件打包成一个文件
3. javadoc – 文档生成器，从源码注释中提取文档
4. jdb – debugger，查错工具
5. java – 运行编译后的java程序（.class后缀的）
6. appletviewer：小程序浏览器，一种执行HTML文件上的Java小程序的Java浏览器。
7. Javah：产生可以调用Java过程的C过程，或建立能被Java程序调用的C过程的头文件。
8. Javap：Java反汇编器，显示编译类文件中的可访问功能和数据，同时显示字节代码含义。
9. Jconsole: Java进行系统调试和监控的工具。



### JRE 

JRE是可以在其上运行、测试和传输应用程序的Java平台。它包括Java虚拟机（jvm）、Java核心类库 （即Java API 包括rt.jar ）和支持文件。**它不包含开发工具(JDK)–编译器、调试器和其它工具。** 



### JVM-从软件层面屏蔽不同操作系统在底层硬件与指令上区别

负责解释执行字节码文件，是可运行java字节码文件的虚拟计算机。它是整个java实现跨平台的最核心的部分。 所有的java程序首先被编译为.class文件，这种.class文件不是直接在机器的操作系统运行，而是经过虚拟机间接的与操作系统交互，由虚拟机将程序解释给本地系统执行。 jvm屏蔽了具体操作系统平台的相关信息  ，使得java程序只需要生成在java虚拟机上运行的目标代码 

![](img\20200610234254.png)







 **jvm 的主要组成部分** 

- 类加载器（ClassLoader）--把字节码加载到内存中
- 运行时数据区（Runtime Data Area）
- 执行引擎（Execution Engine）--字节码文件(class)不能直接交个底层操作系统去执行--将字节码翻译成底层系统指令（01）
- 本地库接口（Native Interface）--调用其他语言的本地库接口（Native Interface）来实现整个程序的功能

![](img\20200616210328.jpg)



栈 -- FILO -- 先入后出 -- java 用的栈数据结构（大学学的数据结构）

程序的方法调用和栈的数据结构一样



![](img\20200616210328.jpg)