### try catch 不对会对性能有很大的影响

JVM 中 异常处理的catch语句

在很早之前通过 jsr和 ret指令来完成

它们在很早之前的版本里就被舍弃了

现在的JVM通过异常表(Exception table 方法体中能找到其内容)来完成 catch 语句

显式抛出异常由athrow指令支持，除了通过 throw 主动抛出异常外

JVM规范中还规定了许多运行时异常会在检测到异常状况时自动抛出(效果等同athrow)



**1、** 我们编写如下的类，add方法中计算++x;并捕获异常；

```java
public class TestClass {
    private static int len = 779;
    public int add(int x){
        try {
            // 若运行时检测到 x = 0,那么 jvm会自动抛出异常，(可以理解成由jvm自己负责 athrow 指令调用)
            x = 100/x;
        } catch (Exception e) {
            x = 100;
        }
        return x;
    }
}
```

**2、** 使用javap工具查看上述类的编译后的class文件；

```java
 编译
 javac TestClass.java
 使用javap 查看 add 方法被编译后的机器指令
 javap -verbose TestClass.class
```

忽略常量池等其他信息，下边贴出add 方法编译后的 机器指令集：

```java
  public int add(int);
    descriptor: (I)I
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=3, args_size=2
         0: bipush        100   //  加载参数100
         2: iload_1             //  将一个int型变量推至栈顶
         3: idiv                //  相除
         4: istore_1            //  除的结果值压入本地变量
         5: goto          11    //  跳转到指令：11
         8: astore_2            //  将引用类型值压入本地变量
         9: bipush        100   //  将单字节常量推送栈顶<这里与数值100有关，可以尝试修改100后的编译结果：iconst、bipush、ldc> 
        10: istore_1            //  将int类型值压入本地变量
        11: iload_1             //  int 型变量推栈顶
        12: ireturn             //  返回
      // 注意看 from 和 to 以及 targer，然后对照着去看上述指令
      Exception table:
         from    to  target type
             0     5     8   Class java/lang/Exception
      LineNumberTable:
        line 6: 0
        line 9: 5
        line 7: 8
        line 8: 9
        line 10: 11
      StackMapTable: number_of_entries = 2
        frame_type = 72 /* same_locals_1_stack_item */
          stack = [ class java/lang/Exception ]
        frame_type = 2 /* same */
```

再来看Exception table：

![图片](img\20230910185555.jpg) 

from=0， to=5。 指令 0~5 对应的就是 try 语句包含的内容，而targer = 8 正好对应 catch 语句块内部操作。

个人理解，from 和 to 相当于划分区间，只要在这个区间内抛出了type 所对应的，“java/lang/Exception” 异常(主动athrow 或者 由jvm运行时检测到异常自动抛出)，那么就跳转到target 所代表的第八行。

若执行过程中，没有异常，直接从第5条指令跳转到第11条指令后返回，由此可见未发生异常时，所谓的性能损耗几乎不存在；

如果硬是要说的话，用了try catch 编译后指令篇幅变长了；goto 语句跳转会耗费性能，当你写个数百行代码的方法的时候，编译出来成百上千条指令，这时候这句goto的带来的影响显得微乎其微。

插播一条：如果你近期准备面试跳槽，建议在ddkk.com在线刷题，涵盖 1万+ 道 Java 面试题，几乎覆盖了所有主流技术面试题。

如图所示为去掉try catch 后的指令篇幅，几乎等同上述指令的前五条。

![图片](img\20230910185556.jpg) 

综上所述：“Java中使用try catch 会严重影响性能” 是民间说法，它并不成立。









唯一 一个问题，一个try ，一条goto

千万次使用，发现性能有下降，千万次计算差值为：5~7 毫秒；

这里造成这个差异的主要原因是 goto 指令占比过大，放大了问题；

当我们在几百行代码里使用少量try catch 时，goto所占比重就会很低，结果是合理的





**可怕的不是 try catch 影响性能，而是 搬砖业务不熟练，导致代码很难看**









