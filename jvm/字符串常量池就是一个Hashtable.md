在JVM虚拟机中有一个 Stringtable的全局变量表

这个表有一个StringPool 这就是字符串常量池  它本质就是Hashtable

key 存储的是 字符串和长度的 hashcode值

value 就是字符串对象的**引用**

字符串对像实际存储到 intern string 中了

这个 intern string    JDK 7之前在永久代中  8 之后就防到了堆中了



JDK 9 之前，字符串是用 char 数组来存储的，主要为了支持非英文字符。

JDK 9 之后，value的类型由char[]数组变成了byte[]数组。

那么为什么要做这样的优化呢？为了节省空间，提高String的性能

两种编码方式，Latin1（ISO-8859-1）和UTF-16

当字符都在Latin1范围内的时候就采用Latin1编码方式（紧凑布局），否则使用UTF-16

```java
@Stable
private final byte[] value;
private final byte coder;
private int hash; // Default to 0
private static final long serialVersionUID = -6849794470754667710L;
 
static final boolean COMPACT_STRINGS;
static {
    COMPACT_STRINGS = true;
}
@Native static final byte LATIN1 = 0;
@Native static final byte UTF16  = 1;
```





















