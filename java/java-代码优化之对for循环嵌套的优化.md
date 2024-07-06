###  JAVA代码优化之对for循环嵌套的优化



### 嵌套循环应该内大外小，还是内小外大？

答：无差别

```java
package com.qin;

public class ForTest {
    public static void main(String[] args) {
        // 外大内小
        outBigInsideSmall();
        // 内大外小
        insideBigOutSmall();
    }

    /**
     * 外大内小
     */
    public static void outBigInsideSmall() {
        // 1. 测试外循环比内循环大
        Long startTime = System.nanoTime();
        for (int i = 0; i < 10000000; i++) {
            for (int j = 0; j < 100; j++) {
				System.nanoTime();
            }
        }
        Long endTime = System.nanoTime();
        System.out.println("外大内小耗时： " + (endTime - startTime));
    }


    /**
     * 内大外小
     */
    public static void insideBigOutSmall() {
        // 1. 测试外循环比内循环小
        Long startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 10000000; j++) {
				System.nanoTime();
            }
        }
        Long endTime = System.nanoTime();
        System.out.println("内大外小耗时： "+(endTime-startTime));
    }

}

```

外大内小耗时： 1898333200
内大外小耗时： 1867767400
外大内小耗时： 1889372500
内大外小耗时： 1865126000
外大内小耗时： 1859156400
内大外小耗时： 1850381400
外大内小耗时： 1861650300
内大外小耗时： 1868321600
外大内小耗时： 1869084100
内大外小耗时： 1862756600
外大内小耗时： 1862279500
内大外小耗时： 1866877700
外大内小耗时： 1863835000
内大外小耗时： 1855616700
外大内小耗时： 1861188100
内大外小耗时： 1864224500
外大内小耗时： 1865760200
内大外小耗时： 1869528900
外大内小耗时： 1868875300
内大外小耗时： 1864118300



### javap -v ForTest.class

差异：

goto          9

goto          17

就是执行指令的不同，但个数是一样的都是执行怎么多条指令，所以几乎没差别

```java
    10: ldc           #39                 // int 1000000000
    12: if_icmpge     35
    15: iconst_0
    16: istore_2
    17: iload_2
    18: bipush        100
    20: if_icmpge     29
    23: iinc          2, 1
    26: goto          17
    29: iinc          1, 1
    32: goto          9
    
    10: bipush        100
    12: if_icmpge     35
    15: iconst_0
    16: istore_2
    17: iload_2
    18: ldc           #39                 // int 1000000000
    20: if_icmpge     29
    23: iinc          2, 1
    26: goto          17
    29: iinc          1, 1
    32: goto          9
```