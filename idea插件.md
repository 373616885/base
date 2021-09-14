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

# idea2020.2.2.x搜索：IDE Eval Reset插件进行安装

### PMD 是一个开源静态源代码分析器
## idea2020.2.2.x搜索：IDE Eval Reset插件进行安装

## Kite AI  代码提示

## aixcoder   代码提示

### jclasslib Bytecode viewer  查看汇编的插件 view 中可以看到show bytecode (javap)

### VisualVM Launcher

# Git Commit Template  Git Commit提交规范

###  AllFormat  格式化插件

# MybatisLog   將mybatis 控制台的sql 打印

### GenerateAllSetter 自动生成set方法





### IDEA 无法输入中文

点击菜单 "Help | Edit Custom VM options..."

添加如下内容到最后一行

```
-Drecreate.x11.input.method=true
```

### IDEA控制台中文乱码解决

点击菜单 "Help | Edit Custom VM options..."

```
-Dfile.encoding=UTF-8
```

IntelliJ IDEA>File>Setting>Editor>File Encodings，将Global Encoding、Project Encoding、Default encodeing for properties files这三项都设置成UTF-8

vm option参数为： -Dfile.encoding=utf-8

Build and run     -->  Modify opotions --> add  vm options

run->Edit Configrations，设置 vm option为 -Dfile.encoding=utf-8





java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -Dfile.encoding=UTF-8 -jar app.jar

-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps

-Xlog:gc* -Xlog:gc+region=trace -Xlog:gc+heap=trace -Xlog:task*=debug


-Duser.timezone=GMT+08, -Dfile.encoding=UTF-8



## 代码学习 https://www.codota.com/code 

## 命名 https://unbug.github.io/codelf/

>>>>>>> 3e418bdc15c66758ff859341c9168a09ff3356c4









