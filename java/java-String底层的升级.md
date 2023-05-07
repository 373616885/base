### String 底层存储结构的升级

提案：JEP 254

https://openjdk.org/jeps/254



### 目的

节省空间，提高性能，减轻GC压力

原String 使用char字节数组存储， 每个char 使用两个字节

但是大多数情况下，String 对象都是Latin-1 字符（拉丁字符），仅需要一个字节的存储，因此使用 char 数据有一半的浪费



String类将原有的UTF-16的char数组改为byte数组加上标志位	

新的String 存储编码为 ISO-8859-1/Latin-1（每个字符一个字节）或者 UTF-16（每个字符两个字节）的 byte 数组，编码标志位将决定使用哪种编码

```java
public final class String {
    // 存储内容
    private final byte[] value;

    /**
     * LATIN1
     * UTF16
     * 这个值只有 0 和 1
     */
    private final byte coder
    
}    
```





### jdk 8 

底层存储 char 数组  内容为 UTF-16编码集 Unicode 

```java
String a = "a";
char[0] = 97 
String a ="中";
char[0] = 20013 //"中"
```



### jdk 9

底层存储 btyte 数组  内容为 Latin-1编码集 ascii 或者 UTF-16编码集 Unicode 

coder标志位来区分

```java
String a ="a“;
byte[0] = 97 
coder = 0  

//两字节    中 的 Unicode =  20013 
String a ="中“;
byte[0] = 45   
byte[1] = 78     
coder = 1  
    
45 = 20013的低8位
78 = 20013的高8位
```



### 装换流程

```java
 public static byte[] charToByte(char c) {
     byte[] b = new byte[2];
     b[0] = (byte) ((c & 0xFF00) >> 8);
     b[1] = (byte) (c & 0xFF);
     return b;
 }

c ="中" 的Unicode = 20013
20013 装换2进制
0100111000101101

高8位 78 
低8位 45
```





### String.intern

首先去判断该字符串是否在常量池中存在，如果存在返回常量池中的字符串，

如果在字符串常量池中不存在，先在字符串常量池中添加该字符串，然后返回引用地址

```java
String s1 = new String("1");
System.out.println(s1.intern() == s1);
System.out.println(s1.intern() == "1");

运行结果：
false
true
```



存储字符串的集合时候，new String 的时候

可以使用 String.intern() 

这样集合存储的就是字符串常量池的数据，可以在堆中节省很多内存

因为字符串常量池中没有重复的字符串，new String在堆中的数据也可以GC抛弃

但有个缺点就是性能有点慢，毕竟每次都要多余存储的就是字符串常量池然后返回







