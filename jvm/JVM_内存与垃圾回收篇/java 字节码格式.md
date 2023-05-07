

### 官方文档

https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.4



### 什么是字节码

因为JVM针对各种操作系统和平台都进行了定制，无论在什么平台，都可以通过javac命令将一个.java文件编译成固定格式的字节码（.class文件）供JVM使用。

之所以被称为字节码，是因为 .class文件是由十六进制值组成的，JVM以两个十六进制值为一组，就是以字节为单位进行读取

![](images\2022-10-12 212135.png)

```java
package com.qin;

public class Hello {

    public static void main(String[] args){
        System.out.println("Hello World");
    }
}
```

`Hello World`程序，用EditPlus编辑器打开生成的`Hello.class`文件，使用 Hex viewer打开

![](images\2022-10-12 212134.png)





`Hello `为例，复制前面一段来讲：

CA FE BA BE 00 00 00 37  00 22 0A 00 02 00 03 07
00 04 0C 00 05 00 06 01  00 10 6A 61 76 61 2F 6C
61 6E 67 2F 4F 62 6A 65  63 74 01 00 06 3C 69 6E
69 74 3E 01 00 03 28 29  56 09 00 08 00 09 07 00

- `CA FE BA BE`是魔数，`00 00 00 37`为主次版本号

- `00 22`表示常量池数量+1  （x22 = 34）

- 再往后一个字节就是常量池的`tag`,`0A`从常量类型表中可以看到类型是CONSTANT_Methodref 

  那么第一个常量就是CONSTANT_Methodref  , class_index = 00 02 , name_and_type_index = 00 03

  ```java
  CONSTANT_Methodref_info {
      u1 tag;
      u2 class_index;
      u2 name_and_type_index;
  }
  
  ```


- 再往后一个字节是常量池的`tag = 07`从常量类型表中可以看到类型是`CONSTANT_Class_info`

  那么这个常量就是`CONSTANT_Class_info`,`name_index`为：`00 04`

  ```java
  CONSTANT_Class_info {
      u1 tag;
      u2 name_index;//name_index需要是常量池中有效下标
  }
  ```

 - 再往后一个字节是常量池的`tag = 0C `的  CONSTANT_NameAndType_info

   那么这个常量就是`CONSTANT_NameAndType_info`, `name_index`为：`00 05`， `descriptor_index`为 `00 06 `

   ```java
   CONSTANT_NameAndType_info {
       u1 tag;
       u2 name_index;
       u2 descriptor_index;
   }
   ```

  - 再往后一个字节是常量池的`tag = 01 `的 CONSTANT_Utf8_info

    那么接下来的两个自己就是bytes数组的长度即后续的字节数，00 10 = 16 也就是第二个常量还需要在读取16 个字节

    6A 61 76 61 2F 6C 61 6E 67 2F 4F 62 6A 65  63 74, 这个23个字节转成字符串就是java/lang/Object

    ```
    CONSTANT_Utf8_info {
        u1 tag;
        u2 length; //bytes的长度，即字节数
        u1 bytes[length];
    }
    ```

    





### Javap 查看字节码内容

javap -v Hello.class 查看.class文件的字节码

javap -c -verbose Hello.class  查看字节码的详细信息

jclasslib Bytecode Viewer 插件，可以方便查看每个java类编译后的字节码文件（使用：View --> Show Bytecode With jclasslib ）

```shell
E:\workspace\jvm01\target\classes\com\qin>javap -v Hello.class
Classfile /E:/workspace/jvm01/target/classes/com/qin/Hello.class
  Last modified 2022年10月12日; size 528 bytes
  SHA-256 checksum 5aba31ee98833433bacb4f4786cb6fe9a97f1b2a6cbb7d4b6d341e1b984a5a5b
  Compiled from "Hello.java"
public class com.qin.Hello
  minor version: 0
  major version: 55
  flags: (0x0021) ACC_PUBLIC, ACC_SUPER
  this_class: #21                         // com/qin/Hello
  super_class: #2                         // java/lang/Object
  interfaces: 0, fields: 0, methods: 2, attributes: 1
Constant pool:
   #1 = Methodref          #2.#3          // java/lang/Object."<init>":()V
   #2 = Class              #4             // java/lang/Object
   #3 = NameAndType        #5:#6          // "<init>":()V
   #4 = Utf8               java/lang/Object
   #5 = Utf8               <init>
   #6 = Utf8               ()V
   #7 = Fieldref           #8.#9          // java/lang/System.out:Ljava/io/PrintStream;
   #8 = Class              #10            // java/lang/System
   #9 = NameAndType        #11:#12        // out:Ljava/io/PrintStream;
  #10 = Utf8               java/lang/System
  #11 = Utf8               out
  #12 = Utf8               Ljava/io/PrintStream;
  #13 = String             #14            // Hello
  #14 = Utf8               Hello
  #15 = Methodref          #16.#17        // java/io/PrintStream.println:(Ljava/lang/String;)V
  #16 = Class              #18            // java/io/PrintStream
  #17 = NameAndType        #19:#20        // println:(Ljava/lang/String;)V
  #18 = Utf8               java/io/PrintStream
  #19 = Utf8               println
  #20 = Utf8               (Ljava/lang/String;)V
  #21 = Class              #22            // com/qin/Hello
  #22 = Utf8               com/qin/Hello
  #23 = Utf8               Code
  #24 = Utf8               LineNumberTable
  #25 = Utf8               LocalVariableTable
  #26 = Utf8               this
  #27 = Utf8               Lcom/qin/Hello;
  #28 = Utf8               main
  #29 = Utf8               ([Ljava/lang/String;)V
  #30 = Utf8               args
  #31 = Utf8               [Ljava/lang/String;
  #32 = Utf8               SourceFile
  #33 = Utf8               Hello.java
{
  public com.qin.Hello();
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 3: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   Lcom/qin/Hello;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=1, args_size=1
         0: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;
         3: ldc           #13                 // String Hello
         5: invokevirtual #15                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
         8: return
      LineNumberTable:
        line 5: 0
        line 6: 8
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       9     0  args   [Ljava/lang/String;
}
SourceFile: "Hello.java"

```









### 字节码的组成结构

规范是有要求的，要求每一个字节码文件都要有十部分固定的顺序组成

![](images\2022-10-12 212136.png)



### Java 字节码信息说明

| 名称                                               | 说明                                                         |
| -------------------------------------------------- | ------------------------------------------------------------ |
| magic                                              | 魔数，用于说明这是一个 Java 字节码文件，固定值 0xCAFEBABE    |
| minor_version, major_version                       | 字节码文件版本，决定了文件的实际格式信息，对于 Java 1.2 之后版本 Java X，major_version = 44 + X |
| constant_pool_count,constant_pool                  | 常量池信息，常量池索引从 1 到 constant_pool_count - 1        |
| access_flags                                       | 类的访问标识                                                 |
| this_class,super_class,interfaces_count,interfaces | 类、父类、接口信息，为常量池中索引。super_class 索引为0，则标示为 Object 类。非零，则此类及父类访问标识不能为 ACC_FINAL |
| fields_count,fields                                | 字段信息                                                     |
| methods_count,methods                              | 方法信息                                                     |
| attributes_count,attributes                        | 属性信息，比如源文件名称、行号信息、注解、异常等（[详细](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7)） |



### Java class 文件结构信息

官方文档给出的定义

```java
ClassFile {
    u4             magic; //魔数
    u2             minor_version; //次版本号
    u2             major_version; //主版本号
    u2             constant_pool_count; //常量池数量+1
    cp_info        constant_pool[constant_pool_count-1]; //常量池
    u2             access_flags; // 访问标识
    u2             this_class; // 当前类索引
    u2             super_class; // 父类索引
    u2             interfaces_count; // 接口数
    u2             interfaces[interfaces_count];// 下标从0开始，元素为常量池的有效下标
    u2             fields_count;
    field_info     fields[fields_count];
    u2             methods_count;
    method_info    methods[methods_count];
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}
```



### 数据结构

```java
cp_info {
    u1 tag;
    u1 info[]; //根据tag不同，长度不同
}
field_info {
    u2             access_flags;
    u2             name_index;
    u2             descriptor_index;
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}
method_info {
    u2             access_flags;
    u2             name_index;
    u2             descriptor_index;
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}
attribute_info {
    u2 attribute_name_index;
    u4 attribute_length;
    u1 info[attribute_length];
}

```



### 魔数

所有的.class文件的前4个字节都是一个固定值魔数，魔数是一个固定值：0xCA FE BA BE



### 版本号

版本号是魔术之后的4个字节，前两个字节表示次版本号（Minor Version），后两个字节表示主版本号（Major Version）

`00 00 00 37`为主次版本号

| JDK Version                                | Bytecode Version |
| ------------------------------------------ | ---------------- |
| [Java 1.0](https://javaalmanac.io/jdk/1.0) | 45.0             |
| [Java 1.1](https://javaalmanac.io/jdk/1.1) | 45.3             |
| [Java 1.2](https://javaalmanac.io/jdk/1.2) | 46.0             |
| [Java 1.3](https://javaalmanac.io/jdk/1.3) | 47.0             |
| [Java 1.4](https://javaalmanac.io/jdk/1.4) | 48.0             |
| [Java 5](https://javaalmanac.io/jdk/5)     | 49.0             |
| [Java 6](https://javaalmanac.io/jdk/6)     | 50.0             |
| [Java 7](https://javaalmanac.io/jdk/7)     | 51.0             |
| [Java 8](https://javaalmanac.io/jdk/8)     | 52.0             |
| [Java 9](https://javaalmanac.io/jdk/9)     | 53.0             |
| [Java 10](https://javaalmanac.io/jdk/10)   | 54.0             |
| [Java 11](https://javaalmanac.io/jdk/11)   | 55.0             |
| [Java 12](https://javaalmanac.io/jdk/12)   | 56.0             |
| [Java 13](https://javaalmanac.io/jdk/13)   | 57.0             |
| [Java 14](https://javaalmanac.io/jdk/14)   | 58.0             |
| [Java 15](https://javaalmanac.io/jdk/15)   | 59.0             |
| [Java 16](https://javaalmanac.io/jdk/16)   | 60.0             |
| [Java 17](https://javaalmanac.io/jdk/17)   | 61.0             |
| [Java 18](https://javaalmanac.io/jdk/18)   | 62.0             |
| [Java 19](https://javaalmanac.io/jdk/19)   | 63.0             |
| [Java 20](https://javaalmanac.io/jdk/20)   | 64.0             |





### 常量池

紧接着主版本号之后的字节为常量池入口



常量池中存放了文字字符串， 常量值， 

当前类的类名， 字段名， 方法名， 各个字段和方法的描述符， 

对当前类的字段和方法的引用信息， 

当前类中对其他类的引用信息等等。



常量池中几乎包含类中的所有信息的描述， class文件中的很多其他部分都是对常量池中的数据项的引用，

比如后面要讲到的this_class, super_class, field_info, attribute_info等都对常量池中的数据项引用



另外字节码指令中也存在对常量池的引用， 

这个对常量池的引用当做字节码指令的一个操作数。  

此外， 常量池中各个项也会相互引用



class文件中的项constant_pool_count的值为1, 说明**每个类都只有一个常量池**

常量池中的数据也是一项一项的， 没有间隙的依次排放。

常量池中各个数据项通过索引来访问， 有点类似与数组， 只不过常量池中的第一项的索引为1, 而不为0, 

如果class文件中的其他地方引用了索引为0的常量池项， 就说明它不引用任何常量池项。

class文件中的每一种数据项都有自己的类型， 相同的道理，常量池中的每一种数据项也有自己的类型。 





常量池整体分为两个部分：常量池计数器以及常量池数据区

![](images\2022-10-12 212137.png)













#### 常量池中的数据项的类型

| Constant Type               | **Value** | 描述                             |
| --------------------------- | --------- | -------------------------------- |
| CONSTANT_Class              | 7         | 对一个类或接口的符号引用         |
| CONSTANT_Fieldref           | 9         | 对一个字段的符号引用             |
| CONSTANT_Methodref          | 10        | 对一个类中声明的方法的符号引用   |
| CONSTANT_InterfaceMethodref | 11        | 对一个接口中声明的方法的符号引用 |
| CONSTANT_NameAndType        | 12        | 对一个字段或方法的部分符号引用   |
| CONSTANT_Utf8               | 1         | UTF-8编码的Unicode字符串         |
| CONSTANT_String             | 8         | String类型字面值                 |
| CONSTANT_Integer            | 3         | int类型字面值                    |
| CONSTANT_Float              | 4         | float类型字面值                  |
| CONSTANT_Long               | 5         | long类型字面值                   |
| CONSTANT_Double             | 6         | double类型字面值                 |
| CONSTANT_MethodHandle       | 15        |                                  |
| CONSTANT_MethodType         | 16        |                                  |
| CONSTANT_InvokeDynamic      | 18        |                                  |



每个数据项叫做一个XXX_info项， 比如， 一个常量池中一个CONSTANT_Utf8类型的项， 就是一个CONSTANT_Utf8_info 。

除此之外， 每个info项中都有一个标志值（tag）

CONSTANT_Utf8_info中的tag值为1， 而CONSTANT_Fieldref_info中的tag值为9



常量池里不同常量对应数据结构

```java
CONSTANT_Class_info {
    u1 tag;
    u2 name_index;//name_index需要是常量池中有效下标
}

CONSTANT_Utf8_info {
    u1 tag;
    u2 length; //bytes的长度，即字节数
    u1 bytes[length];
}

CONSTANT_Fieldref_info {
    u1 tag;
    u2 class_index;
    u2 name_and_type_index;
}

CONSTANT_Methodref_info {
    u1 tag;
    u2 class_index;
    u2 name_and_type_index;
}

CONSTANT_InterfaceMethodref_info {
    u1 tag;
    u2 class_index;
    u2 name_and_type_index;
}


CONSTANT_String_info {
    u1 tag;
    u2 string_index;
}

CONSTANT_Integer_info {
    u1 tag;
    u4 bytes;
}

CONSTANT_Float_info {
    u1 tag;
    u4 bytes;
}

CONSTANT_Long_info {
    u1 tag;
    u4 high_bytes;
    u4 low_bytes;
}

CONSTANT_Double_info {
    u1 tag;
    u4 high_bytes;
    u4 low_bytes;
}

CONSTANT_NameAndType_info {
    u1 tag;
    u2 name_index;
    u2 descriptor_index;
}

CONSTANT_MethodHandle_info {
    u1 tag;
    u1 reference_kind;
    u2 reference_index;
}

CONSTANT_MethodType_info {
    u1 tag;
    u2 descriptor_index;
}

CONSTANT_InvokeDynamic_info {
    u1 tag;
    u2 bootstrap_method_attr_index;
    u2 name_and_type_index;
}
```



> PS： 为什么constant_pool_count的值是常量池的数量+1，从1开始到n-1结束？不从0开始的原因是什么？
>
> 这个问题在这里提一下，因为常量池中很多常量需要引用其他常量，而有可能存在常量并不需要任何有效引用，所以常量池空置了下标0的位置作为备用



#### javap 对应

基本类型常量

```java
#2 = Double             2.0d       //double类型的存了实际值
#4 = String             #35            // 引用#35
#35 = Utf8               3             // new String("3")
#23 = Utf8               a            //int类型只存了名称，没有存实际值
#24 = Utf8               I
```

符号引用

```java
  #5 = Methodref          #10.#36        // com/rrtx/adm/Main.add:(Ljava/lang/String;ID)V
   #6 = Methodref          #37.#38        // java/lang/Integer.valueOf:(Ljava/lang/String;)Ljava/lang/Integer;
   #7 = Methodref          #37.#39        // java/lang/Integer.intValue:()I
   #8 = Fieldref           #40.#41        // java/lang/System.out:Ljava/io/PrintStream;
   #9 = Methodref          #42.#43        // java/io/PrintStream.println:(D)V
  #10 = Class              #44            // com/rrtx/adm/Main
  #11 = Class              #45            // java/lang/Object
```



### 访问标志

描述的是当前类（或者接口）的访问修饰符， 如public， private等，

此外， 这里面还存在一个标志位， 标志当前的额这个class描述的是类， 还是接口

比如类的修饰符是Public Final，则对应的访问修饰符的值为ACC_PUBLIC | ACC_FINAL，即0x0001 | 0x0010=0x0011



access_flags 对应类型

| Flag           | Name   | Value Interpretation                     |
| -------------- | ------ | ---------------------------------------- |
| ACC_PUBLIC     | 0x0001 | 是否为public                             |
| ACC_PRIVATE    | 0x0002 | 是否为private                            |
| ACC_PROTECTED  | 0x0004 | 是否为protected                          |
| ACC_STATIC     | 0x0008 | 是否为static                             |
| ACC_FINAL      | 0x0010 | 是否为final                              |
| ACC_VOLATILE   | 0x0040 | 是否为volatile                           |
| ACC_INTERFACE  | 0x0200 | 是否为接口                               |
| ACC_ABSTRACT   | 0x0400 | 是否为abstract                           |
| ACC_SYNTHETIC  | 0x1000 | 这个关键字不是源码生成，而是编译器生成的 |
| ACC_ANNOTATION | 0x2000 | 是否为注解                               |
| ACC_ENUM       | 0x4000 | 是否为枚举                               |



### 当前类索引

访问标志后的两个字节，描述的是当前类的全限定名，这两个字节保存的值是常量池中的索引值，根据索引值就能在常量池中找到这个类的全限定名

```java
this_class: #21                         // com/qin/Hello
    
Constant pool:    
  #21 = Class              #22            // com/qin/Hello
  #22 = Utf8               com/qin/Hello 
    
```

两个字节的数据对应常量池中的一个CONSTANT_Class_info数据项的一个索引。 

 CONSTANT_Class_info中有一个字段叫做name_index ， 指向一个CONSTANT_Utf8_info ， 在这个CONSTANT_Utf8_info 中存放着当前类的全限定名。

```
CONSTANT_Class_info {
    u1 tag;
    u2 name_index;//name_index需要是常量池中有效下标
}
```



### 父类索引

当前类名后的两个字节，描述的父类的全限定名，也是保存的常量池中的索引值

```java
super_class: #2                         // java/lang/Object
    
Constant pool:    
   #2 = Class              #4             // java/lang/Object
   #4 = Utf8               java/lang/Object
      
```

和this_class一样是一个指向常量池数据项的索引。 

它指向一个CONSTANT_Class_info， 这个CONSTANT_Class_info数据项描述的是当前类的超类的信息。

CONSTANT_Class_info中的name_index指向常量池中的一个CONSTANT_Utf8_info ，CONSTANT_Utf8_info 中存放的是当前类的超类的全限定名。



如果没有显式的继承一个，也就是说如果当前类是直接继承Object的， 那么super_class值为0 。 

我们在前面的文章中提到过， 如果一个索引值为0， 那么就说明这个索引不引用任何常量池中的数据项， 因为常量池中的数据项是从1开始的。 

也就是说， 如果一个类的class文件中的super_class为0 ， 那么就代表该类直接继承Object类。






### 接口索引

父类名称后的两个字节，是接口计数器，描述了该类或者父类实现的接口数量，紧接着的n个字节是所有接口名称的字符串常量的索引值

代码示例：

```
package com.qin;

import java.io.Serializable;

public class Hello implements Cloneable, Serializable {
    public static void main(String[] args){
        System.out.println("Hello");
    }
}

```

常量池中会有如下信息：

```

 interfaces: 2
 
Constant pool:
 
.........
.........
 
  #23 = Class              #24            // java/lang/Cloneable
  #24 = Utf8               java/lang/Cloneable
  #25 = Class              #26            // java/io/Serializable
  #26 = Utf8               java/io/Serializable
   
.........
```



### 字段表

用于描述类或者接口中声明的变量，包含类级别的变量和实例变量，

但是不包含方法内部声明的局部变量，字段表也分为两个部分，第一部分是两个字节，描述字段个数，第二部分是每个字段的详细信息fields_info

```java
ClassFile {
    u4             magic; //魔数
    u2             minor_version; //次版本号
    u2             major_version; //主版本号
    u2             constant_pool_count; //常量池数量+1
    cp_info        constant_pool[constant_pool_count-1]; //常量池
    u2             access_flags; // 访问标识
    u2             this_class; // 当前类索引
    u2             super_class; // 父类索引
    u2             interfaces_count; // 接口数
    u2             interfaces[interfaces_count];// 下标从0开始，元素为常量池的有效下标
    u2             fields_count; 
    field_info     fields[fields_count];
    u2             methods_count;
    method_info    methods[methods_count];
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}

field_info {
    u2             access_flags; //访问标识
    u2             name_index;
    u2             descriptor_index;
    u2             attributes_count; //属性个数
    attribute_info attributes[attributes_count];
}

```

注意：

类、字段与方法的访问标识类型都不太相同，field的访问标识

field访问标识类型如下：

<table><thead><tr><th>Flag Name</th><th>Value</th><th>Interpretation</th></tr></thead><tbody><tr><td><code>ACC_PUBLIC</code></td><td>0x0001</td><td>Declared <code>public</code>; may be accessed from outside its package.</td></tr><tr><td><code>ACC_PRIVATE</code></td><td>0x0002</td><td>Declared <code>private</code>; usable only within the defining class.</td></tr><tr><td><code>ACC_PROTECTED</code></td><td>0x0004</td><td>Declared <code>protected</code>; may be accessed within subclasses.</td></tr><tr><td><code>ACC_STATIC</code></td><td>0x0008</td><td>Declared <code>static</code>.</td></tr><tr><td><code>ACC_FINAL</code></td><td>0x0010</td><td>Declared <code>final</code>; never directly assigned to after object construction (JLS §17.5).</td></tr><tr><td><code>ACC_VOLATILE</code></td><td>0x0040</td><td>Declared <code>volatile</code>; cannot be cached.</td></tr><tr><td><code>ACC_TRANSIENT</code></td><td>0x0080</td><td>Declared <code>transient</code>; not written or read by a persistent object manager.</td></tr><tr><td><code>ACC_SYNTHETIC</code></td><td>0x1000</td><td>Declared synthetic; not present in the source code.</td></tr><tr><td><code>ACC_ENUM</code></td><td>0x4000</td><td>Declared as an element of an <code>enum</code>.</td></tr></tbody></table>

![](images\2022-10-12 212141.png)



### 方法表

字段表结束后为方法表，方法表也分为两个部分，

第一个部分是两个字节表述方法的个数，

第二部分是每个方法的详细信息 方法的访问信息比较复杂，包括方法的访问标志、方法名、方法的描述符和方法的属性

```java
ClassFile {
    u4             magic; //魔数
    u2             minor_version; //次版本号
    u2             major_version; //主版本号
    u2             constant_pool_count; //常量池数量+1
    cp_info        constant_pool[constant_pool_count-1]; //常量池
    u2             access_flags; // 访问标识
    u2             this_class; // 当前类索引
    u2             super_class; // 父类索引
    u2             interfaces_count; // 接口数
    u2             interfaces[interfaces_count];// 下标从0开始，元素为常量池的有效下标
    u2             fields_count; 
    field_info     fields[fields_count];
    u2             methods_count;
    method_info    methods[methods_count];
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}

method_info {
    u2             access_flags; //访问标识
    u2             name_index;
    u2             descriptor_index;
    u2             attributes_count;
    attribute_info attributes[attributes_count];
}
```

注意：

类、字段与方法的访问标识类型都不太相同，方法的访问标识

<table><thead><tr><th>Flag Name</th><th>Value</th><th>Interpretation</th></tr></thead><tbody><tr><td><code>ACC_PUBLIC</code></td><td>0x0001</td><td>Declared <code>public</code>; may be accessed from outside its package.</td></tr><tr><td><code>ACC_PRIVATE</code></td><td>0x0002</td><td>Declared <code>private</code>; accessible only within the defining class.</td></tr><tr><td><code>ACC_PROTECTED</code></td><td>0x0004</td><td>Declared <code>protected</code>; may be accessed within subclasses.</td></tr><tr><td><code>ACC_STATIC</code></td><td>0x0008</td><td>Declared <code>static</code>.</td></tr><tr><td><code>ACC_FINAL</code></td><td>0x0010</td><td>Declared <code>final</code>; must not be overridden (<a href="https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-5.html#jvms-5.4.5">§5.4.5</a>).</td></tr><tr><td><code>ACC_SYNCHRONIZED</code></td><td>0x0020</td><td>Declared <code>synchronized</code>; invocation is wrapped by a monitor use.</td></tr><tr><td><code>ACC_BRIDGE</code></td><td>0x0040</td><td>A bridge method, generated by the compiler.</td></tr><tr><td><code>ACC_VARARGS</code></td><td>0x0080</td><td>Declared with variable number of arguments.</td></tr><tr><td><code>ACC_NATIVE</code></td><td>0x0100</td><td>Declared <code>native</code>; implemented in a language other than Java.</td></tr><tr><td><code>ACC_ABSTRACT</code></td><td>0x0400</td><td>Declared <code>abstract</code>; no implementation is provided.</td></tr><tr><td><code>ACC_STRICT</code></td><td>0x0800</td><td>Declared <code>strictfp</code>; floating-point mode is FP-strict.</td></tr><tr><td><code>ACC_SYNTHETIC</code></td><td>0x1000</td><td>Declared synthetic; not present in the source code.</td></tr></tbody></table>



> ACC_BRIDGE也是由编译器生成的，比如泛型的子类重写父类方法， 就会有一个在子类生成一个新的方法用ACC_BRIDGE标识
>
> ACC_VARARGS可变参数的方法会出现这个标记
>
> ACC_STRICT strictfp标识的方法中，所有float和double表达式都严格遵守FP-strict的限制,符合IEEE-754规范.





![](images\2022-10-12 212142.png)



#### javap 对应

```java
methods: 2
    
{
  // 这个无参构造器  
  public com.qin.Hello();
    descriptor: ()V
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 3: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   Lcom/qin/Hello;

  // 这个是main方法
  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V //静态的，公共方法
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=1, args_size=1 ///栈帧的深度是2，局部变量是1个，入参数量是1个
         // main方法的操作数栈 start       
         0: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;
         3: ldc           #13                 // String Hello
         5: invokevirtual #15                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
         8: return //出栈 return
         // main方法的操作数栈 end         
      LineNumberTable:
        line 5: 0
        line 6: 8
	   // main方法 局部变量表
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       9     0  args   [Ljava/lang/String;
}
```



常用指令类型

1. 加载以及存储指令load、store
2. 常量定义const
3. ldc 复杂类型定义
4. 入栈出栈基本类型:iconst、istore、iload中的i，代表意思是int，以次类推
5. invokestatic 调用静态方法
6. 出栈 return

```java
i代表int类型
l代表long
s代表short
b代表byte
c代表char
f代表float
d代表double
a和其他的不一样，a代表的是引用类型
```



#### descriptors-描述

方法和字段都有自己的描述信息，方法的描述包括参数、返回值的类型，字段描述为字段的类型，下面是类型表：

<table><thead><tr><th><em>FieldType</em> term</th><th>Type</th><th>Interpretation</th></tr></thead><tbody><tr><td><code>B</code></td><td><code>byte</code></td><td>signed byte</td></tr><tr><td><code>C</code></td><td><code>char</code></td><td>Unicode character code point in the Basic Multilingual Plane, encoded with UTF-16</td></tr><tr><td><code>D</code></td><td><code>double</code></td><td>double-precision floating-point value</td></tr><tr><td><code>F</code></td><td><code>float</code></td><td>single-precision floating-point value</td></tr><tr><td><code>I</code></td><td><code>int</code></td><td>integer</td></tr><tr><td><code>J</code></td><td><code>long</code></td><td>long integer</td></tr><tr><td><code>L</code> <em>ClassName</em> <code>;</code></td><td><code>reference</code></td><td>an instance of class <em>ClassName</em></td></tr><tr><td><code>S</code></td><td><code>short</code></td><td>signed short</td></tr><tr><td><code>Z</code></td><td><code>boolean</code></td><td><code>true</code> or <code>false</code></td></tr><tr><td><code>[</code></td><td><code>reference</code></td><td>one array dimension</td></tr></tbody></table>

描述格式为：`(` *{\*ParameterDescriptor\*}* `)` ReturnDescriptor

例如：

mian方法描述符

```
 public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
```

描述信息就是：`([Ljava/lang/String;)V`



```java
Object m(int i, double d, Thread t);
```

描述信息就是：`(IDLjava/lang/Thread;)Ljava/lang/Object;`

对象类型的后面需要用`;`分割，基础类型不需要





### 附加属性

字节码的最后一部分，该项存放了在该文件中类或接口所定义属性的基本信息。



### attribute-属性

attribute_info类型比较多，这里只把我们最关心的代码说下，即`Code_attribute`:

```java
Code_attribute {
    u2 attribute_name_index;
    u4 attribute_length;
    u2 max_stack;
    u2 max_locals;
    u4 code_length;
    u1 code[code_length];
    u2 exception_table_length;
    {   u2 start_pc;
        u2 end_pc;
        u2 handler_pc;
        u2 catch_type;
    } exception_table[exception_table_length];
    u2 attributes_count;
    attribute_info attributes[attributes_count];
}

```



> 只要不是native、abstact修饰的方法，必须含有`Code_attribute`属性



Code_attribute中包含code、exception、attribute_info等信息，这里主要说下code中的内容。

code数组中的内容就是方法中编译后的代码：

```java
     0: aload_0
     1: invokespecial #10                 // Method java/lang/Object."<init>":()V
     4: return
```

这个就是我们上面那个类的无参构造函数编译后的效果，那这里面的aload_0、invokespecial、return学过JVM相关知识的话，大家已经很熟悉了.

- `aload_0`就是变量0进栈
- `invokespecial`调用实例的初始化方法，即构造方法
- `return` 即方法结束，返回值为`void`



JVM有这样一个指令数组，code数组中的记录的就是指令数组的有效下标，下面是部分指令：

<table><thead><tr><th>JVM指令</th><th>指令下标</th><th>描述</th></tr></thead><tbody><tr><td><strong>return</strong></td><td>0xB1</td><td>当前方法返回<strong>void</strong></td></tr><tr><td><strong>areturn</strong></td><td>0xB0</td><td>从方法中返回一个对象的引用</td></tr><tr><td><strong>ireturn</strong></td><td>0xAC</td><td>当前方法返回int</td></tr><tr><td><strong>iload_0</strong></td><td>0x1A</td><td>第一个int型局部变量进栈</td></tr><tr><td><strong>lload_0</strong></td><td>0x1E</td><td>第一个long型局部变量进栈</td></tr><tr><td><strong>istore_0</strong></td><td>0x3B</td><td>将栈顶int型数值存入第一个局部变量</td></tr><tr><td><strong>lstore_0</strong></td><td>0x3F</td><td>将栈顶long型数值存入第一个局部变量</td></tr><tr><td><strong>getstatic</strong></td><td>0xB2</td><td>获取指定类的静态域，并将其值压入栈顶</td></tr><tr><td><strong>putstatic</strong></td><td>0xB3</td><td>为指定的类的静态域赋值</td></tr><tr><td><strong>invokespecial</strong></td><td>0xB7</td><td>调用超类构造方法、实例初始化方法、私有方法</td></tr><tr><td><strong>invokevirtual</strong></td><td>0xB6</td><td>调用实例方法</td></tr><tr><td><strong>iadd</strong></td><td>0x60</td><td>栈顶两int型数值相加，并且结果进栈</td></tr><tr><td><strong>iconst_0</strong></td><td>0x03</td><td>int型常量值0进栈</td></tr><tr><td><strong>ldc</strong></td><td>0x12</td><td>将int、float或String型常量值从常量池中推送至栈顶</td></tr></tbody></table>



详细指令列表可以查看[官方文档](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-7.html)。


关于`attribute_info`还有其他类型，有兴趣的可以查看[Attribute](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7)，类型及其出现位置如下：

<table><thead><tr><th>Attribute</th><th>Location</th></tr></thead><tbody><tr><td><code>SourceFile</code></td><td><code>ClassFile</code></td></tr><tr><td><code>InnerClasses</code></td><td><code>ClassFile</code></td></tr><tr><td><code>EnclosingMethod</code></td><td><code>ClassFile</code></td></tr><tr><td><code>SourceDebugExtension</code></td><td><code>ClassFile</code></td></tr><tr><td><code>BootstrapMethods</code></td><td><code>ClassFile</code></td></tr><tr><td><code>ConstantValue</code></td><td><code>field_info</code></td></tr><tr><td><code>Code</code></td><td><code>method_info</code></td></tr><tr><td><code>Exceptions</code></td><td><code>method_info</code></td></tr><tr><td><code>RuntimeVisibleParameterAnnotations</code>, <code>RuntimeInvisibleParameterAnnotations</code></td><td><code>method_info</code></td></tr><tr><td><code>AnnotationDefault</code></td><td><code>method_info</code></td></tr><tr><td><code>MethodParameters</code></td><td><code>method_info</code></td></tr><tr><td><code>Synthetic</code></td><td><code>ClassFile</code>, <code>field_info</code>, <code>method_info</code></td></tr><tr><td><code>Deprecated</code></td><td><code>ClassFile</code>, <code>field_info</code>, <code>method_info</code></td></tr><tr><td><code>Signature</code></td><td><code>ClassFile</code>, <code>field_info</code>, <code>method_info</code></td></tr><tr><td><code>RuntimeVisibleAnnotations</code>, <code>RuntimeInvisibleAnnotations</code></td><td><code>ClassFile</code>, <code>field_info</code>, <code>method_info</code></td></tr><tr><td><code>LineNumberTable</code></td><td><code>Code</code></td></tr><tr><td><code>LocalVariableTable</code></td><td><code>Code</code></td></tr><tr><td><code>LocalVariableTypeTable</code></td><td><code>Code</code></td></tr><tr><td><code>StackMapTable</code></td><td><code>Code</code></td></tr><tr><td><code>RuntimeVisibleTypeAnnotations</code>, <code>RuntimeInvisibleTypeAnnotations</code></td><td><code>ClassFile</code>, <code>field_info</code>, <code>method_info</code>, <code>Code</code></td></tr></tbody></table>











