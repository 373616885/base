### try catch for循环内外对比

```java
package com.qin;

public class ForTest {
    public static void main(String[] args) {
        catchOut();
        catchInSide();
    }

    public static void catchOut() {
        // 方法说明 ： 测试for循环优化
        Long startTime = System.nanoTime();
        try {
            for (int j = 0; j < 10000000; j++) {

            }
        } catch (Exception e) {

        }
        Long endTime = System.nanoTime();
        System.out.println("catchOut耗时： " + (endTime - startTime));
    }

    public static void catchInSide() {
        // 方法说明 ： 测试for循环优化
        Long startTime = System.nanoTime();

        for (int j = 0; j < 10000000; j++) {
            try {
            } catch (Exception e) {

            }
        }
        Long endTime = System.nanoTime();
        System.out.println("catchInSide耗时： " + (endTime - startTime));
    }

}

```



catchOut耗时： 1539500
catchInSide耗时： 1735100

catchOut耗时： 1769300
catchInSide耗时： 1644900



### javap -v ForTest.class

发现就几天指令的顺序不一样而已，没有所谓的优劣

```
   19: goto          23
   22: astore_2
   23: iinc          1, 1
   26: goto          9
   
   
   19: iinc          1, 1
   22: goto          9
   25: goto          29
   28: astore_1
   
   
   
D:\workspace\demo\target\classes\com\qin\demo\foreach>javap -v TryCatchFor.class
Classfile /D:/workspace/demo/target/classes/com/qin/demo/foreach/TryCatchFor.class
  Last modified 2023年4月4日; size 1806 bytes
  SHA-256 checksum bca60db5a6dc7ac6aa972154dbf269f1120e626364a85abeb5d3ff9ca1081c21
  Compiled from "TryCatchFor.java"
public class com.qin.demo.foreach.TryCatchFor
  minor version: 0
  major version: 61
  flags: (0x0021) ACC_PUBLIC, ACC_SUPER
  this_class: #8                          // com/qin/demo/foreach/TryCatchFor
  super_class: #2                         // java/lang/Object
  interfaces: 0, fields: 0, methods: 4, attributes: 3
Constant pool:
   #1 = Methodref          #2.#3          // java/lang/Object."<init>":()V
   #2 = Class              #4             // java/lang/Object
   #3 = NameAndType        #5:#6          // "<init>":()V
   #4 = Utf8               java/lang/Object
   #5 = Utf8               <init>
   #6 = Utf8               ()V
   #7 = Methodref          #8.#9          // com/qin/demo/foreach/TryCatchFor.catchOut:()V
   #8 = Class              #10            // com/qin/demo/foreach/TryCatchFor
   #9 = NameAndType        #11:#6         // catchOut:()V
  #10 = Utf8               com/qin/demo/foreach/TryCatchFor
  #11 = Utf8               catchOut
  #12 = Methodref          #8.#13         // com/qin/demo/foreach/TryCatchFor.catchInSide:()V
  #13 = NameAndType        #14:#6         // catchInSide:()V
  #14 = Utf8               catchInSide
  #15 = Fieldref           #16.#17        // java/lang/System.out:Ljava/io/PrintStream;
  #16 = Class              #18            // java/lang/System
  #17 = NameAndType        #19:#20        // out:Ljava/io/PrintStream;
  #18 = Utf8               java/lang/System
  #19 = Utf8               out
  #20 = Utf8               Ljava/io/PrintStream;
  #21 = String             #22            // --------------
  #22 = Utf8               --------------
  #23 = Methodref          #24.#25        // java/io/PrintStream.println:(Ljava/lang/String;)V
  #24 = Class              #26            // java/io/PrintStream
  #25 = NameAndType        #27:#28        // println:(Ljava/lang/String;)V
  #26 = Utf8               java/io/PrintStream
  #27 = Utf8               println
  #28 = Utf8               (Ljava/lang/String;)V
  #29 = Methodref          #16.#30        // java/lang/System.nanoTime:()J
  #30 = NameAndType        #31:#32        // nanoTime:()J
  #31 = Utf8               nanoTime
  #32 = Utf8               ()J
  #33 = Methodref          #34.#35        // java/lang/Long.valueOf:(J)Ljava/lang/Long;
  #34 = Class              #36            // java/lang/Long
  #35 = NameAndType        #37:#38        // valueOf:(J)Ljava/lang/Long;
  #36 = Utf8               java/lang/Long
  #37 = Utf8               valueOf
  #38 = Utf8               (J)Ljava/lang/Long;
  #39 = Integer            10000000
  #40 = Class              #41            // java/lang/Exception
  #41 = Utf8               java/lang/Exception
  #42 = Methodref          #34.#43        // java/lang/Long.longValue:()J
  #43 = NameAndType        #44:#32        // longValue:()J
  #44 = Utf8               longValue
  #45 = InvokeDynamic      #0:#46         // #0:makeConcatWithConstants:(J)Ljava/lang/String;
  #46 = NameAndType        #47:#48        // makeConcatWithConstants:(J)Ljava/lang/String;
  #47 = Utf8               makeConcatWithConstants
  #48 = Utf8               (J)Ljava/lang/String;
  #49 = InvokeDynamic      #1:#46         // #1:makeConcatWithConstants:(J)Ljava/lang/String;
  #50 = Utf8               Code
  #51 = Utf8               LineNumberTable
  #52 = Utf8               LocalVariableTable
  #53 = Utf8               this
  #54 = Utf8               Lcom/qin/demo/foreach/TryCatchFor;
  #55 = Utf8               main
  #56 = Utf8               ([Ljava/lang/String;)V
  #57 = Utf8               i
  #58 = Utf8               I
  #59 = Utf8               args
  #60 = Utf8               [Ljava/lang/String;
  #61 = Utf8               StackMapTable
  #62 = Utf8               MethodParameters
  #63 = Utf8               j
  #64 = Utf8               startTime
  #65 = Utf8               Ljava/lang/Long;
  #66 = Utf8               endTime
  #67 = Utf8               SourceFile
  #68 = Utf8               TryCatchFor.java
  #69 = Utf8               BootstrapMethods
  #70 = MethodHandle       6:#71          // REF_invokeStatic java/lang/invoke/StringConcatFactory.makeConcatWithConstants:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
  #71 = Methodref          #72.#73        // java/lang/invoke/StringConcatFactory.makeConcatWithConstants:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
  #72 = Class              #74            // java/lang/invoke/StringConcatFactory
  #73 = NameAndType        #47:#75        // makeConcatWithConstants:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
  #74 = Utf8               java/lang/invoke/StringConcatFactory
  #75 = Utf8               (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
  #76 = String             #77            // catchOut耗时： \u0001
  #77 = Utf8               catchOut耗时： \u0001
  #78 = String             #79            // catchInSide耗时： \u0001
  #79 = Utf8               catchInSide耗时： \u0001
  #80 = Utf8               InnerClasses
  #81 = Class              #82            // java/lang/invoke/MethodHandles$Lookup
  #82 = Utf8               java/lang/invoke/MethodHandles$Lookup
  #83 = Class              #84            // java/lang/invoke/MethodHandles
  #84 = Utf8               java/lang/invoke/MethodHandles
  #85 = Utf8               Lookup
{
  public com.qin.demo.foreach.TryCatchFor();
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
            0       5     0  this   Lcom/qin/demo/foreach/TryCatchFor;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=1
         0: iconst_0
         1: istore_1
         2: iload_1
         3: bipush        10
         5: if_icmpge     28
         8: invokestatic  #7                  // Method catchOut:()V
        11: invokestatic  #12                 // Method catchInSide:()V
        14: getstatic     #15                 // Field java/lang/System.out:Ljava/io/PrintStream;
        17: ldc           #21                 // String --------------
        19: invokevirtual #23                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        22: iinc          1, 1
        25: goto          2
        28: return
      LineNumberTable:
        line 6: 0
        line 7: 8
        line 8: 11
        line 9: 14
        line 6: 22
        line 11: 28
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            2      26     1     i   I
            0      29     0  args   [Ljava/lang/String;
      StackMapTable: number_of_entries = 2
        frame_type = 252 /* append */
          offset_delta = 2
          locals = [ int ]
        frame_type = 250 /* chop */
          offset_delta = 25
    MethodParameters:
      Name                           Flags
      args

  public static void catchOut();
    descriptor: ()V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=5, locals=2, args_size=0
         0: invokestatic  #29                 // Method java/lang/System.nanoTime:()J
         3: invokestatic  #33                 // Method java/lang/Long.valueOf:(J)Ljava/lang/Long;
         6: astore_0
         7: iconst_0
         8: istore_1
         9: iload_1
        10: ldc           #39                 // int 10000000
        12: if_icmpge     25
        15: invokestatic  #29                 // Method java/lang/System.nanoTime:()J
        18: pop2
        19: iinc          1, 1
        22: goto          9
        25: goto          29
        28: astore_1
        29: invokestatic  #29                 // Method java/lang/System.nanoTime:()J
        32: invokestatic  #33                 // Method java/lang/Long.valueOf:(J)Ljava/lang/Long;
        35: astore_1
        36: getstatic     #15                 // Field java/lang/System.out:Ljava/io/PrintStream;
        39: aload_1
        40: invokevirtual #42                 // Method java/lang/Long.longValue:()J
        43: aload_0
        44: invokevirtual #42                 // Method java/lang/Long.longValue:()J
        47: lsub
        48: invokedynamic #45,  0             // InvokeDynamic #0:makeConcatWithConstants:(J)Ljava/lang/String;
        53: invokevirtual #23                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        56: return
      Exception table:
         from    to  target type
             7    25    28   Class java/lang/Exception
      LineNumberTable:
        line 15: 0
        line 17: 7
        line 18: 15
        line 17: 19
        line 22: 25
        line 20: 28
        line 23: 29
        line 24: 36
        line 25: 56
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            9      16     1     j   I
            7      50     0 startTime   Ljava/lang/Long;
           36      21     1 endTime   Ljava/lang/Long;
      StackMapTable: number_of_entries = 4
        frame_type = 253 /* append */
          offset_delta = 9
          locals = [ class java/lang/Long, int ]
        frame_type = 250 /* chop */
          offset_delta = 15
        frame_type = 66 /* same_locals_1_stack_item */
          stack = [ class java/lang/Exception ]
        frame_type = 0 /* same */

  public static void catchInSide();
    descriptor: ()V
    flags: (0x0009) ACC_PUBLIC, ACC_STATIC
    Code:
      stack=5, locals=3, args_size=0
         0: invokestatic  #29                 // Method java/lang/System.nanoTime:()J
         3: invokestatic  #33                 // Method java/lang/Long.valueOf:(J)Ljava/lang/Long;
         6: astore_0
         7: iconst_0
         8: istore_1
         9: iload_1
        10: ldc           #39                 // int 10000000
        12: if_icmpge     29
        15: invokestatic  #29                 // Method java/lang/System.nanoTime:()J
        18: pop2
        19: goto          23
        22: astore_2
        23: iinc          1, 1
        26: goto          9
        29: invokestatic  #29                 // Method java/lang/System.nanoTime:()J
        32: invokestatic  #33                 // Method java/lang/Long.valueOf:(J)Ljava/lang/Long;
        35: astore_1
        36: getstatic     #15                 // Field java/lang/System.out:Ljava/io/PrintStream;
        39: aload_1
        40: invokevirtual #42                 // Method java/lang/Long.longValue:()J
        43: aload_0
        44: invokevirtual #42                 // Method java/lang/Long.longValue:()J
        47: lsub
        48: invokedynamic #49,  0             // InvokeDynamic #1:makeConcatWithConstants:(J)Ljava/lang/String;
        53: invokevirtual #23                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        56: return
      Exception table:
         from    to  target type
            15    19    22   Class java/lang/Exception
      LineNumberTable:
        line 29: 0
        line 31: 7
        line 33: 15
        line 36: 19
        line 34: 22
        line 31: 23
        line 38: 29
        line 39: 36
        line 40: 56
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            9      20     1     j   I
            7      50     0 startTime   Ljava/lang/Long;
           36      21     1 endTime   Ljava/lang/Long;
      StackMapTable: number_of_entries = 4
        frame_type = 253 /* append */
          offset_delta = 9
          locals = [ class java/lang/Long, int ]
        frame_type = 76 /* same_locals_1_stack_item */
          stack = [ class java/lang/Exception ]
        frame_type = 0 /* same */
        frame_type = 250 /* chop */
          offset_delta = 5
}
SourceFile: "TryCatchFor.java"
BootstrapMethods:
  0: #70 REF_invokeStatic java/lang/invoke/StringConcatFactory.makeConcatWithConstants:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
    Method arguments:
      #76 catchOut耗时： \u0001
  1: #70 REF_invokeStatic java/lang/invoke/StringConcatFactory.makeConcatWithConstants:(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;
    Method arguments:
      #78 catchInSide耗时： \u0001
InnerClasses:
  public static final #85= #81 of #83;    // Lookup=class java/lang/invoke/MethodHandles$Lookup of class java/lang/invoke/MethodHandles

D:\workspace\demo\target\classes\com\qin\demo\foreach>
   
   
   
```









