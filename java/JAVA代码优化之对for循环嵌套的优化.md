###  JAVA代码优化之对for循环嵌套的优化



### 嵌套循环应该内大外小，还是内小外大？

答：外大内小

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

            }
        }
        Long endTime = System.nanoTime();
        System.out.println("内大外小耗时： "+(endTime-startTime));
    }

}

```



外大内小耗时： 1965000

内大外小耗时： 9732100



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



catchOut耗时： 3531800
catchInSide耗时： 2218800













