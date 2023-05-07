### JVM 类加载

![](images\image-20200705080719531.png)

完整图如下

![](images\image-20200705080911284.png)



### 类加载过程

完整图：

![](images\image-20200705082601441.png)



### 加载阶段 Loading

类加载器加载class文件：通过一个类的全限定名获取定义此类的二进制字节流

通过这个二进制字节流，转换成Class的静态数据结构

最后内存中生成一个代表这个类的java.lang.Class对象，作为方法区访问入口



**简单的说就是：**

- 通过**包名 + 类名**，获取这个类，准备用**流**进行传输
- 在这个类加载到**内存**中
- 加载完毕创建一个**class对象**



### 链接

分为3个阶段

#### 验证

验证文件的正确性，保证不会危害虚拟机自身安全

例如：开头 CA FE BA BE

#### 准备

为（static修饰的）类变量分配内存并且设置该类变量的默认初始值，即零值。

final修饰的static除外，final修饰的static在这里会直接显式初始化

这里不会为实例变量分配初始化，类变量会分配在方法区中，而实例变量是会随着对象一起分配到Java堆中

#### 解析

将符号引用变成直接引用

简单理解：将加载当前class需要的Object，String 等其他需要的类的关系建立关系（变成直接引用）

解析操作往往会伴随着JVM在执行完初始化之后再执行



### 初始化

简单理解：执行静态代码

1. <clinit>（）的过程：代码中有static的时候，如果没有static就不会生成<clinit>（）

2. 注意 执行  static 是按顺序执行的 

3. 有父类的情况下，会执行父类的<clinit>（）

4. 一个类的<clinit>（）方法在多线程下被同步加锁了

5. 构造器是虚拟机视角下是<init>（）

6. 任何一个类在声明后，都有生成一个构造器<init>（），默认是空参构造器

   

 static 按顺序执行的

```java
package com.qin.demo.classloader;

public class ClassLoaderTest {
	
    static {
        num = 10;
    }
    public static int num = 20;
    
    // num 在链接准备阶段就赋初始值 num = 0
    // 在初始化阶段：<clinit>（）的过程（执行static代码）是按顺行的
    // 先 num = 10   ，然后  num = 20
    // 最后输出 20
    public static void main(String[] args)  {
        System.out.println(ClassLoaderTest.num);
    }

}
```







































