### 背景

1. 使用feign远程rpc调用的时候总会遇到上下线调用到已经重启的服务（我遇到的）
2. 在一些宕机需要关闭资源的地方



### 优雅停机

Spring Boot 2.3.0.RELEASE引入了Graceful Shutdown的功能

SmartLifecycle bean停止的最早阶段执行



**不同 web 容器优雅停机后的行为区别**

**容器停机后的行为取决于具体的 web 容器行为**

| web 容器名称   | 行为说明                                 |
| -------------- | ---------------------------------------- |
| tomcat 9.0.33+ | 停止接收请求，客户端新请求等待超时。     |
| Reactor Netty  | 停止接收请求，客户端新请求等待超时。     |
| Undertow       | 停止接收请求，客户端新请求直接返回 503。 |



### 实现

```yaml
server:
  shutdown: GRACEFUL #开启优雅停机

#应用执行时间过长导致应用销毁不掉，就还是需要强行杀死应
#spring内部去注册一些关闭钩子，关闭的话，server.shutdown 无效 还有@PreDestroy bean 销毁执行的方法
#@PreDestroy bean 销毁一般在宕机的时候执行
spring:
  main:
    register-shutdown-hook: true # 默认为true 
  lifecycle:
    timeout-per-shutdown-phase: 20s #设置缓冲时间 默认30s 应用执行时间过长导致应用销毁不掉，就还是需要强行杀死应用
```



```java
package com.qin.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PreDestroy;

@RestController
@Slf4j
public class ShutDownController {
	
    /**
     * @PreDestroy bean 销毁一般在宕机的时候执行
     */
    @PreDestroy
    public void destroy(){
        System.out.println("==destroy");
    }

    @GetMapping("shutDown")
    public String shutDown(){
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("==============");
        return "success";
    }
}

```



### 自己注册 shutdown-hook

```java
package com.qin.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        //自己向JVM注册 shutdown-hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("JVM 销毁");
            }
        }));
    }
}

```



### kill -9 和 kill -15 区别

想要触发 Spring boot 的优雅关机，就得使用 kill -15 

kill -15 是正常关闭 ：可以触发 Spring boot 的优雅关机

kill -9  是强制结束进程，不能触发 Spring boot 的优雅关机







