### Runnable 

可被线程执行的任务





### Callable

必须要被 FutureTask 包装的 任务

executorService.submit 可传 FutureTask 

```java
package com.qin.demo;

import lombok.Setter;

import java.util.concurrent.*;

public class CallableTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Task1 task1 = new Task1();
        Thread t1 = new Thread(task1);
        t1.start();

        Task2 task2 = new Task2();
        task2.setNum(100);
        RunnableFuture<Integer> futureTask = new FutureTask<>(task2);
        Thread t2 = new Thread(futureTask);
        t2.start();
        Integer i = futureTask.get();
        System.out.println(i);


        ExecutorService executorService =  Executors.newSingleThreadExecutor();

        Future<Integer> task2Futre = executorService.submit(task2);
        Integer i1 = task2Futre.get();
        System.out.println(i1);

    }
}

class Task1 implements Runnable {

    @Override
    public void run() {
        System.out.println("--- Runnable ---");
    }
}

@Setter
class Task2 implements Callable<Integer> {

    private int num;

    @Override
    public Integer call() throws Exception {
        return ++num;
    }

}

```

