## talkx 代码 ChatGPT -- 优化代码，解释代码

talkx--chatgpt-gpt-4-free-ai-code-assistant

## codegeex :copilot 的平替

## FittenCode AI代码提示

## comate 百度AI组手，自动改bug和接口自动生成

## Bito ChatGPT 团队开发的代码提示

https://mp.weixin.qq.com/s/L6LBtYppe6U1ukh_Agc_oQ

## AI comment 代码自动注释的插件

## Github copilot 注释转代码插件,免费两个月

## tabnine ：Github copilot 代替品

## Codota 代码提示

## Material Theme UI  主题插件

## Alibaba Java Coding Guidelines 阿里巴巴的编码规约检查插件

## Alibaba Cloud Toolkit 快速部署服务器

## Json Parser   json串格式化工具

## MyBatisX myBatis插件支持

## RESTfultoolkit 根据RESTful 客户端

## Translate 翻译插件

## Translation 翻译插件

## CodeGlance 代码迷你缩放图插件

## JavaDoc  一键生成注释 

## Easy Javadoc 自动生成javadoc文档注释

## Maven Help

## SonarLint 

## SpotBugs

## FindBugs

## idea2020.2.2.x搜索：IDE Eval Reset插件进行安装

## PMD 是一个开源静态源代码分析器
## idea2020.2.2.x搜索：IDE Eval Reset插件进行安装

## Kite AI  代码提示

## aixcoder   代码提示

## codota插件：可以优先显示使用频率较高的类、方法

## jclasslib Bytecode viewer  查看汇编的插件 view 中可以看到show bytecode (javap)

## VisualVM Launcher  ：查看内存工具

## Git Commit Template  Git Commit提交规范

##  AllFormat  格式化插件

## Gsonformat  json与实体对象互转

## MybatisLog   將mybatis 控制台的sql 打印

## GenerateAllSetter 自动生成set方法

## any-rule 基于IDEA平台的常用正则表达式插件

## GitToolBox 在Idea的状态栏显示git状态，还提供了定时fecth等功能

## CheckStyle-IDEA插件

## **BinEd - Binary/Hexadecimal Edito**  看class文件 二进制/十六进制

## Manifold 编写扩展方法  

https://github.com/manifold-systems/manifold

Manifold 的原理和 Lombok 是类似的

## Spring Boot Assistant  ：spring boot 配置文件和注解的提示

https://github.com/flikas/idea-spring-boot-assistant

需要添加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```



## **Sequence Diagram  查看代码调用 社区版没带 可以代替** 



## Rainbow Brackets 插件 成对的括号用相同的颜色表示出来了



## GitToolBox插件

idea本身的git提示

ntelliJ IDEA>File>Setting>Editor>inlay Hints 

![](img\20200416203833815.png)



## IDEA 无法输入中文

点击菜单 "Help | Edit Custom VM options..."

添加如下内容到最后一行

```
-Drecreate.x11.input.method=true
```

## IDEA控制台中文乱码解决

点击菜单 "Help | Edit Custom VM options..."

```
-Dfile.encoding=UTF-8
```

IntelliJ IDEA>File>Setting>Editor>File Encodings，将Global Encoding、Project Encoding、Default encodeing for properties files这三项都设置成UTF-8

vm option参数为： -Dfile.encoding=utf-8

Build and run     -->  Modify opotions --> add  vm options

run->Edit Configrations，设置 vm option为 -Dfile.encoding=utf-8



## idea add custom tag

这里的 @name 和 @date 被 idea 标注为黄色，显示警告信息为 Wrong tag，也就是说 idea 不能识别这个标签，对于有强迫症的开发者来说，不能容忍这种提示标记，如何消除这个标记呢？可以根据提示点击 Add date to custom tags，之后这个黄色的标记就没有了，idea 将这个自定义的标签添加到了 Java doc 的自定义标签中，我们有自定义的标签也可以添加到这里，用逗号分隔

![](img\20200416203833816.png)



## 远程bug

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Dfile.encoding=UTF-8 -jar app.jar

## 开启日志

-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps

## JDK 11 开启日志

-Xlog:gc* -Xlog:gc+region=trace -Xlog:gc+heap=trace -Xlog:task*=debug



-Duser.timezone=GMT+08, -Dfile.encoding=UTF-8



查看class加载情况

-Xlog:class+load=info 

查看GC情况

-Xlog:gc

标准参数

verbose

-verbose:class

输出jvm载入类的相关信息，当jvm报告说找不到类或者类冲突时可此进行诊断。

-verbose:gc

输出每次GC的相关情况。

-verbose:jni

输出native方法调用的相关情况，一般用于诊断jni调用错误信息。



-verbose:gc //在控制台输出GC情况

 -XX:+PrintGCDetails  //在控制台输出详细的GC情况

 -Xloggc: filepath  //将GC日志输出到指定文件中



```shell
-Xlog:gc* 代替-XX:+PrintGCDetails
-Xlog:gc #查看GC情况
-Xlog:class+load=info  #查看class加载情况
-Xloggc:gc.log 变成了-Xlog:gc:gc.log
-XX:+PrintHeapAtGC 变成了-Xlog:gc+heap=trace
-XX:+PrintReferenceGC  变成了 -Xlog:ref*=debug
```



## java 启动参数 

```shell
-Xms600m -Xmx600m -XX:+UseG1GC -Xlog:ref*=debug -Xlog:gc+heap=trace -Xlog:gc* -Xlog:gc,gc+cpu::uptime Xlog:gc:gc.log -Xcomp -XX:-UseCounterDecay -Duser.timezone=Asia/Shanghai -Dfile.encoding=UTF-8 -XX:-UsePerfData -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./error.hprof -XX:+UseAdaptiveSizePolicy -Duser.timezone=GMT+08
```

-XX:-UsePerfData ：关闭 jps jstat 这些命令，hsperfdata文件，关闭jvmstat工具

-XX:-UseCounterDecay ：关闭热点代码衰减

-Xlog:gc* 代替-XX:+PrintGCDetails

-Xlog:gc #查看GC情况

-Xlog:class+load=info  #查看class加载情况

-Xloggc:gc.log 变成了-Xlog:gc:gc.log

-XX:+PrintHeapAtGC 变成了-Xlog:gc+heap=trace

-XX:+PrintReferenceGC  变成了 -Xlog:ref*=debug

-XX:+UseAdaptiveSizePolicy(自适应大小策略) ：GC的情况下自动计算动态调整 Eden、From 和 To 区的大小 ，默认开启



## 代码学习 

## https://www.codota.com/code 

## https://www.tabnine.com/code

## 命名
## https://unbug.github.io/codelf/



## aixcoder 代码自动补全

## https://codesearch.aixcoder.com



## 自动导包

Editor--General--Auto import --java模块

一个自动导包

一个自动删除无用的包

![img/164023856320214.jpg](img\164023856320214.jpg)



## 自动编译设置

Build,Execution,Deployment--Compiler

![img/2021-12-23-135716.png](img\2021-12-23-135716.png)



## 不区分大小写提示代码

Editor--General--Code Completion

![img/20200416203833802.png](img\20200416203833802.png)



## **双斜杠注释改成紧跟代码头**

![img/20211223140235.jpg](img\20211223140235.jpg)



## **优化版本控制的目录颜色展示**

![img/2021-12-23-140604.png](img\2021-12-23-140604.png)



## idea同个项目不同端口多开

点击allow multiple instances  

![](img\20200416203833803.jpg)



## MOMO Code Sec Inspector   Java项目静态代码安全审计idea插件工具

使用方式
被动：装完愉快的打代码，一边它会提醒你哪里有安全风险（支持的漏洞检查规则内的）

主动：主动触发项目代码扫描，使用该安全漏洞检查规则

主动扫描步骤
Analyze→Inspect Code

![](img\20200416203833801.png)


Inspection profile→...

![](img\20200416203833812.png)

确认已勾选momo插件安全规则，点击OK按钮开始扫描

![](img\20200416203833813.png)

扫描结果

![](img\20200416203833814.png)

注意事项
1、被扫描代码的版本请确认为最新版本

2、耗时：多个Java项目仅耗时几分钟，耗时较少，不需要担心耗时较长影响

3、扫描目录可支持包含多项目的父目录

4、需结合代码实际作用评估漏洞解决优先级，可能存在误报

5、扫描时可选择过滤测试代码，减少误报

6、插件仅部分规则支持一键修复



## Dependency-Check 组件漏洞测试工具

```xml
<project>
    <build>
        <plugins>
            ...
            <plugin>
              <groupId>org.owasp</groupId>
              <artifactId>dependency-check-maven</artifactId>
              <version>8.2.1</version>
              <executions>
                  <execution>
                      <goals>
                          <goal>check</goal>
                      </goals>
                  </execution>
              </executions>
            </plugin>
            ...
        </plugins>
        ...
    </build>
    ...
</project>

```



### IntelliJ IDEA小技巧之代码折叠

//**region** 

//**endregion**