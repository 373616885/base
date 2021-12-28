## Codota 代码提示

## Material Theme UI  主题插件

## Alibaba Java Coding Guidelines 阿里巴巴的编码规约检查插件

## Alibaba Cloud Toolkit 快速部署服务器

## Json Parser   json串格式化工具

## MyBatisX myBatis插件支持

## RESTfultoolkit 根据RESTful 客户端

## Translation 翻译插件

## CodeGlance 代码迷你缩放图插件

## JavaDoc  一键生成注释

## Maven Help

## SonarLint 

## SpotBugs

## FindBugs

## idea2020.2.2.x搜索：IDE Eval Reset插件进行安装

## PMD 是一个开源静态源代码分析器
## idea2020.2.2.x搜索：IDE Eval Reset插件进行安装

## Kite AI  代码提示

## aixcoder   代码提示

## jclasslib Bytecode viewer  查看汇编的插件 view 中可以看到show bytecode (javap)

## VisualVM Launcher

## Git Commit Template  Git Commit提交规范

##  AllFormat  格式化插件

## MybatisLog   將mybatis 控制台的sql 打印

## GenerateAllSetter 自动生成set方法

## any-rule 基于IDEA平台的常用正则表达式插件

## GitToolBox 在Idea的状态栏显示git状态，还提供了定时fecth等功能

## CheckStyle-IDEA插件

## **BinEd - Binary/Hexadecimal Edito**  二进制/十六进制编辑器





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



## 远程bug

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Dfile.encoding=UTF-8 -jar app.jar

## 开启日志

-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps

## JDK 11 开启日志

-Xlog:gc* -Xlog:gc+region=trace -Xlog:gc+heap=trace -Xlog:task*=debug



-Duser.timezone=GMT+08, -Dfile.encoding=UTF-8



## 代码学习 

## https://www.codota.com/code 

## https://www.tabnine.com/code

## 命名
## https://unbug.github.io/codelf/



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
