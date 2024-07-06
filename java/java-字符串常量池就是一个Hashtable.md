在JVM虚拟机中有一个 Stringtable的全局变量表

这个表有一个StringPool 这就是字符串常量池  它本质就是Hashtable

Hashtable - 一个synchronized 的HashMap

key 存储的是 字符串和长度的 hashcode值

value 就是字符串对象的**引用**

字符串对像实际存储到 intern string 中了

这个 intern string    JDK 7之前在永久代中  8 之后就防到了堆中了





















