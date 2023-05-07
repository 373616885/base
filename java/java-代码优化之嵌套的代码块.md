### 嵌套的代码块

嵌套的代码块：下面的情况

```java
public void evaluate(int operator) {
    // Do some computation...
    {   int a = stack.pop();      
     	int b = stack.pop();      
        int result = a + b;      
        stack.push(result);    
    }
}
```



**有个别人使用嵌套的代码块优化代码**

**觉得嵌套的代码块可以缩短变量的生命周期，可以节约内存**

**确实可以可以缩短变量的生命周期，可以节约内存**



**但是代码规范里有一条：**

**嵌套的代码块可用于创建新的作用域，并限制其中定义的变量的可见性。**

**在方法中使用此功能通常表示该方法有太多的责任，应该重构为更小的方法。**



总结：如果无嵌套的代码块外产生了新的变量

确实比 无嵌套的代码块 消耗少点内存

**有嵌套的代码块的使用了**

**stack=2, locals=2, args_size=0**

**无嵌套代码块**

**stack=2, locals=3, args_size=0**

**但如果重用变量**

**stack=2, locals=2, args_size=0**

**LocalVariableTable ：这个只有 2** 

性能最好

所以应该根据代码规范里来，不要有太多的责任

如果需要应该重构为更小的方法（将嵌套的代码块提取成一个方法）

实在不行，重用变量占用的 Slot

嵌套的代码块的出现其实是代码方法有太多的责导致的



```java
// 有嵌套的代码块--产生了新变量a 
public static String dai(){

   String mail = "1525336367@qq.com";
    {
        String b = "3";
        mail = mail + b;
    }
    String a = "2";
    return String.valueOf(a);
}
// 无嵌套的代码块--产生了新变量a  
public static String nodai(){
    String mail = "1525336367@qq.com";
    String b = "3";
    mail = mail + b;
    String a = "2";
    return String.valueOf(b);
}
// 无嵌套的代码块--没产生了新变量a 
 public static String nodaiNo(){
     String mail = "1525336367@qq.com";
     String b = "3";
     mail = mail + b;
     b = "2";
     return String.valueOf(b);
 }
```

上面的代码反编译后

```java
	public static String dai() {
        String mail = "1525336367@qq.com";
        String a = "3";
        mail.makeConcatWithConstants<invokedynamic>(mail, a);
        a = "2";
        return String.valueOf(a);
    }

    public static String nodai() {
        String mail = "1525336367@qq.com";
        String b = "3";
        mail.makeConcatWithConstants<invokedynamic>(mail, b);
        String a = "2";
        return String.valueOf(a);
    }

    public static String nodaiNo() {
        String mail = "1525336367@qq.com";
        String b = "3";
        mail.makeConcatWithConstants<invokedynamic>(mail, b);
        b = "2";
        return String.valueOf(b);
    }
```

javap 查看字节码

```java
 
  public static java.lang.String dai();
    descriptor: ()Ljava/lang/String;
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=0
         0: ldc           #2                  // String 1525336367@qq.com
         2: astore_0
         3: ldc           #3                  // String 3
         5: astore_1
         6: aload_0
         7: aload_1
         8: invokedynamic #4,  0              // InvokeDynamic #0:makeConcatWithConstants:(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
        13: astore_0
        14: ldc           #5                  // String 2
        16: astore_1
        17: aload_1
        18: invokestatic  #6                  // Method java/lang/String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
        21: areturn
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            6       8     1     b   Ljava/lang/String;
            3      19     0  mail   Ljava/lang/String;
           17       5     1     a   Ljava/lang/String;

  public static java.lang.String nodai();
    descriptor: ()Ljava/lang/String;
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=3, args_size=0
         0: ldc           #2                  // String 1525336367@qq.com
         2: astore_0
         3: ldc           #3                  // String 3
         5: astore_1
         6: aload_0
         7: aload_1
         8: invokedynamic #4,  0              // InvokeDynamic #0:makeConcatWithConstants:(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
        13: astore_0
        14: ldc           #5                  // String 2
        16: astore_2
        17: aload_2
        18: invokestatic  #6                  // Method java/lang/String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
        21: areturn
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            3      19     0  mail   Ljava/lang/String;
            6      16     1     b   Ljava/lang/String;
           17       5     2     a   Ljava/lang/String;

  public static java.lang.String nodaiNo();
    descriptor: ()Ljava/lang/String;
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=0
         0: ldc           #2                  // String 1525336367@qq.com
         2: astore_0
         3: ldc           #3                  // String 3
         5: astore_1
         6: aload_0
         7: aload_1
         8: invokedynamic #4,  0              // InvokeDynamic #0:makeConcatWithConstants:(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
        13: astore_0
        14: ldc           #5                  // String 2
        16: astore_1
        17: aload_1
        18: invokestatic  #6                  // Method java/lang/String.valueOf:(Ljava/lang/Object;)Ljava/lang/String;
        21: areturn
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            3      19     0  mail   Ljava/lang/String;
            6      16     1     b   Ljava/lang/String;
}
```



### 还有一种说法

```java
public String s() {
    String a = "9";
    String b = "9";
    b = null;
    String c = "9";
    return c;
}
```

这里b的Slot并没有被重复利用，其实只是与堆中的关系被切断，在堆中等着被回收
这样做只是提前将关系切开，但在堆中并没有被回收，没有意义，堆中回收要等GC

























































