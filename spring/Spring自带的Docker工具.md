 ### SpringBoot 应用构建成 Docker 镜像三种方法

（1）使用 spring-boot-maven-plugin 内置的 **build-image**.

（2）使用 Google 的 **jib-maven-plugin**。

（3）使用 **dockerfle-maven-plugin**。



### Spring Boot maven 插件 的 build-image

Spring Boot 预装了自己的用于构建 Docker 镜像的插件

我们无需进行任何更改，因为它就在 pom.xml 中的 spring-boot-starter-parent。



添加配置：

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <docker>
            <!-- 默认推到本地的2376 ：localhost:2375 -->
            <!-- 注意：本地的docker只能localhost访问 -->
            <host>tcp://localhost:2375</host>
        </docker>
        <image>
         <!--镜像名称， 使用构建时间作为镜像的版本号	<name>docker.io/library/${project.artifactId}-${project.version}:${maven.build.timestamp}</name> --> 	
        </image>
        <excludes>
            <exclude>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
            </exclude>
        </excludes>
    </configuration>
</plugin>
```



直接使用：

```shell
mvn spring-boot:build-image
mvn spring-boot:build-image -DskipTests
```



运行容器测试：

```shell
docker run -p 9090:8080 -t demo:0.0.1-SNAPSHOT
```





不需要写 Dockerfile，也不用操别的心，plugin 都帮你做了，例如 Spring 建议的安全、内存、性能等问题。



![](img\2023-04-18-154846.jpg)





### 提前准备

因为spring-boot-maven-plugin本身会在docker中pull两个镜像

在里面下载可能会比较慢，提前拉取到本地

```shell
docker pull paketobuildpacks/builder:base
docker pull paketobuildpacks/run:base-cnb
```



### 无法链接github下载镜像问题

使用 FastGithub.UI.exe 等工具



### 文档

https://docs.spring.io/spring-boot/docs/2.4.0/maven-plugin/reference/htmlsingle/#build-image