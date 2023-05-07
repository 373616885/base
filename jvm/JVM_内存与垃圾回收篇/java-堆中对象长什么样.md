### java查看内存工具

```xml
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.16</version>
</dependency>

使用

System.out.println(ClassLayout.parseInstance(obj).toPrintable());

```



### 堆中 java 对象

在堆中java对象只有数据部分，没有对象的数据含义

要结合方法区中的class才能说明

![](images\2023-04-07 000000.jpg)



内存堆中：student 只有数据部分

结构：

mark 对象头  + class 指针对象   + 12  + 100 + 字符串name的引用地址 

![](images\2023-04-07 000007.jpg)



要结合方法区来得到属性的含义

![](images\2023-04-07 000008.jpg)

例如：

student.name ：通过class指针指向方法区的 name 发现 offset（偏移量） 是 20

那么堆中指针往后20个字节表示name的值

student.age ：通过class指针指向方法区的 age 发现 offset（偏移量） 是 12

那么堆中指针再往后12个字节表示age的值

student.score ：通过class指针指向方法区的 score 发现 offset（偏移量） 是 16

那么堆中指针再往后16个字节表示score的值



```java
@Data
public class Student {

    private String name;

    private int age;

    private int score;
    
    public Student() {
    }

    public Student(String name, int age, int score) {
        this.name = name;
        this.age = age;
        this.score = score;
    }
    
}
```



### 使用 jhsdb 查看真容

```java
   public static void main(String[] args) {
        Student student = new Student("1",12,100);

        System.out.println(ClassLayout.parseInstance(student).toPrintable());
    }
```



### 启动hsdb图形 

```shell
### jps 拿到java PID
jps 

### 启动hsdb图形 
jhsdb hsdb
```

file  -> Attach to HotSpot process 输入 pid

![](images\2023-04-07 000001.jpg)

![](images\2023-04-07 000002.jpg)



student 对象在main 线程

选择 main 线程 ，然后点旁边的

![](images\2023-04-07 000003.jpg)



![](images\2023-04-07 000004.jpg)



前面的 0x8a6abff658 是局部变量的地址

后面的 0x712517680  是堆中的地址

点击 Tools  ->  Memory Viewer -> Address 输入 0x712517680 就会看到真容

![](images\2023-04-07 000005.jpg)



![](images\2023-04-07 000006.jpg)



0x0000000000000001  是 mark 对象头

​    00c00a00  是 class 指针对象   

0x0000000c  是 12 （16进制转换就是12）

​    00000064  是 100 （16进制转换就是100）

0xe24a2ed3  是  字符串 name 的引用地址















