### JVM 类加载

![](images\image-20200705080719531.png)

完整图如下

![](images\image-20200705080911284.png)



### 类加载过程

完整图：

![](images\image-20200705082601441.png)



### 加载阶段 Loading

类加载器加载class文件：通过一个类的全限定名获取定义此类的二进制字节流

加载到**内存**中的二进制字节流，转换成Class的静态数据结构

方法区中生成一个代表这个类的instanceKClass对象

最后堆中生成一个 java.lang.class对象作为instanceKClass对象的镜像

关系：Person实例<——>Person的instanceKlass<——>Person的Class





**简单的说就是：**

- 通过**包名 + 类名**，获取这个类，准备用**流**进行传输
- 在这个类加载到**内存**中
- 方法区中生成一个代表这个类的instanceKClass对象
- 堆中生成一个对应的镜像 java.lang.class对象

![](images\1606317801322.png)



### 链接

分为3个阶段

#### 验证

验证文件的正确性，保证不会危害虚拟机自身安全

例如：开头 CA FE BA BE

#### 准备

为（static修饰的）类变量分配内存并且设置该类变量的默认初始值，即零值。

final修饰的static除外，final和static修饰的基本数据类型和字面量（这种 str = "qinjp"）直接显式初始化（非 new 的）

final和static修饰的 new 类型（str = "qinjp"这种都不算）在初始化阶段进行new （显式赋值）

这里不会为实例变量分配初始化，类变量会分配在方法区中，而实例变量是会随着对象一起分配到Java堆中

在这个阶段并不会像初始化阶段那样有代码的执行

#### 解析

将符号引用变成直接引用

简单理解：将加载当前class需要的Object，String 等其他需要的类的关系建立关系（变成直接引用）

解析操作往往会伴随着JVM在执行完初始化之后再执行



### 初始化

简单理解：执行静态代码和静态成员的赋值语句

1. <clinit>（）的过程：代码中有static的时候，如果没有static就不会生成<clinit>（）

2. 注意 执行  static 是按顺序执行的 

3. 有父类的情况下，会执行父类的<clinit>（）

4. 一个类的<clinit>（）方法在多线程下被同步加锁了

5. 如果staic 属性是引用类型的（非字面量），也是在这里 new 一个对象给它

6. 构造器是虚拟机视角下是<init>（）

7. 任何一个类在声明后，都有生成一个构造器<init>（），默认是空参构造器

   

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







































