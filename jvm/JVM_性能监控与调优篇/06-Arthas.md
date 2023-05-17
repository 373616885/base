## Arthas

上述工具都必须在服务端项目进程中配置相关的监控参数，然后工具通过远程连接到项目进程，获取相关的数据。这样就会带来一些不便，比如线上环境的网络是隔离的，本地的监控工具根本连不上线上环境。并且类似于 Jprofiler 这样的商业工具，是需要付费的。

那么有没有一款工具不需要远程连接，也不需要配置监控参数，同时也提供了丰富的性能监控数据呢？

阿里巴巴开源的性能分析神器 Arthas 应运而生。



Arthas 是 Alibaba 开源的 Java 诊断工具，深受开发者喜爱。在线排查问题，无需重启；动态跟踪 Java 代码；实时监控 JVM 状态。

Arthas 支持 JDK 6 ＋，支持 Linux／Mac／Windows，采用命令行交互模式，同时提供丰富的 Tab 自动补全功能，进一步方便进行问题的定位和诊断。



当你遇到以下类似问题而束手无策时，Arthas 可以帮助你解决：

- 这个类从哪个 jar 包加载的？为什么会报各种类相关的 Exception？
- 我改的代码为什么没有执行到？难道是我没 commit？分支搞错了？
- 遇到问题无法在线上 debug，难道只能通过加日志再重新发布吗？
- 线上遇到某个用户的数据处理有问题，但线上同样无法 debug，线下无法重现！
- 是否有一个全局视角来查看系统的运行状况？
- 有什么办法可以监控到 JVM 的实时运行状态？
- 怎么快速定位应用的热点，生成火焰图？



基于哪些工具开发而来

- greys-anatomy：Arthas代码基于Greys.二次开发而来，非常感谢Greys.之前所有的工作，以
  及Greys原作者对Arthas提出的意见和建议！
- termd：Arthas的命令行实现基于termd开发，是一款优秀的命令行程序开发框架，感谢
  termd提供了优秀的框架。
- crash：Arthas的文本渲染功能基于crash中的文本渲染功能开发，可以从这里看到源码，感谢
  crash在这方面所做的优秀工作。
- cli：Arthas的命令行界面基于vert.x提供的cli库进行开发，感谢vert.x在这方面做的优秀
  工作。
- compiler：Arthas里的内存编绎器代码来源I
- Apache Commons Net：Arthas里的Telnet client代码来源
- JavaAgent：运行在main方法之前的拦截器，它内定的方法名叫premain,也就是说先执行
  premain方法然后再执行main方法
- ASM：一个通用的]ava字节码操作和分析框架。它可以用于修改现有的类或直接以二进制形式动
  态生成类。ASM提供了一些常见的字节码转换和分析算法，可以从它们构建定制的复杂转换和代
  码分析工具。ASM提供了与其他Java字节码框架类似的功能，但是主要关注性能。因为它被设计
  和实现得尽可能小和快，所以非常适合在动态系统中使用（当然也可以以静态方式使用，例如在
  编译器中)



## 卸载

- 在 Linux/Unix/Mac 平台

  删除下面文件：

  

  ```bash
  rm -rf ~/.arthas/
  rm -rf ~/logs/arthas
  ```

- Windows 平台直接删除 user home 下面的`.arthas`和`logs/arthas`目录



工程目录

![](img\fa3c5e41cbf999d261bcf32951831868.png)



官方地址：[https://arthas.aliyun.com/doc/quick-start.html](https://arthas.aliyun.com/doc/quick-start.html)

安装方式：如果速度较慢，可以尝试国内的码云 Gitee 下载。

```shell
wget https://io/arthas/arthas-boot.jar
wget https://arthas/gitee/io/arthas-boot.jar
curl -O https://arthas.aliyun.com/arthas-boot.jar
```

Arthas 只是一个 java 程序，所以可以直接用 java -jar 运行。

除了在命令行查看外，Arthas 目前还支持 Web Console。在成功启动连接进程之后就已经自动启动

可以直接访问 http://127.0.0.1:8563/ 访问，页面上的操作模式和控制台完全一样。



## 启动

```bash
java -jar arthas-boot.jar
```



## Web Console

http://127.0.0.1:8563/

Arthas 目前支持 Web Console，用户在 attach 成功之后，可以直接访问：[http://127.0.0.1:8563

端口号：固定了

默认情况下，arthas 只 listen 127.0.0.1，所以如果想从远程连接，则可以使用 `--target-ip`参数指定 listen 的 IP



**scrollback URL 参数**

默认 Web Console 支持向上回滚的行数是 1000。可以在 URL 里用`scrollback`指定。比如

http://127.0.0.1:8563/?scrollback=3000



## 退出

quit 、 exit 、logout、q：退出客户端

只是退出当前的连接。Attach到目标进程上的arthas还会继续运行，端口会保持开放

下次连接时执行`java -jar arthas-boot.jar`可以直接连接上。



stop 或者 shutdown : 关闭 Arthas ，并退出所有客户端

完全退出Arthas 

执行stop指令后，所有 Arthas 客户端全部退出。关闭Arthas服务器之前，会重置掉所有做过的增强类



如果是非正常退出，会报下面的错误，提示端口占用。原因是上次连接了一个进程，未正常退出。

```shell
[ERROR] The telnet port 3658 is used by process 3804 instead of target process 15043, you will connect to an unexpected process.
[ERROR] 1. Try to restart arthas-boot, select process 3804, shutdown it first with running the 'stop' command.
[ERROR] 2. Or try to stop the existing arthas instance: java -jar arthas-client.jar 127.0.0.1 3658 -c "stop"
[ERROR] 3. Or try to use different telnet port, for example: java -jar arthas-boot.jar --telnet-port 9998 --http-port -1

```



## 日志

windows： user home 下面的`logs/arthas`目录

linux：~/logs/arthas 



## **基础指令**

```shell
quit/exit 退出当前 Arthas客户端，其他 Arthas喜户端不受影响
stop/shutdown 关闭 Arthas服务端，所有 Arthas客户端全部退出
help 查看命令帮助信息
cat 打印文件内容，和linux里的cat命令类似
echo 打印参数，和linux里的echo命令类似
grep 匹配查找，和linux里的gep命令类似
tee 复制标隹输入到标准输出和指定的文件，和linux里的tee命令类似
pwd 返回当前的工作目录，和linux命令类似
cls 清空当前屏幕区域
session 查看当前会话的信息
reset 重置增强类，将被 Arthas增强过的类全部还原, Arthas服务端关闭时会重置所有增强过的类
version 输出当前目标Java进程所加载的 Arthas版本号
history 打印命令历史
keymap Arthas快捷键列表及自定义快捷键
```

**jvm 相关**

```shell
dashboard 当前系统的实时数据面板
thread 查看当前JVM的线程堆栈信息
jvm 查看当前JVM的信息
sysprop 查看和修改JVM的系统属性
sysem 查看JVM的环境变量
vmoption 查看和修改JVM里诊断相关的option
perfcounter 查看当前JVM的 Perf Counter信息
logger 查看和修改logger
getstatic 查看类的静态属性
ognl 执行ognl表达式
mbean 查看 Mbean的信息
heapdump dump java heap，类似jmap命令的 heap dump功能
```

**class/classloader 相关**

```shell
sc 查看JVM已加载的类信息
	-d 输出当前类的详细信息，包括这个类所加载的原始文件来源、类的声明、加载的Classloader等详细信息。如果一个类被多个Classloader所加载，则会出现多次
	-E 开启正则表达式匹配，默认为通配符匹配
	-f 输出当前类的成员变量信息（需要配合参数-d一起使用）
	-X 指定输出静态变量时属性的遍历深度，默认为0，即直接使用toString输出
sm 查看已加载类的方法信息
	-d 展示每个方法的详细信息
	-E 开启正则表达式匹配,默认为通配符匹配
jad 反编译指定已加载类的源码
mc 内存编译器，内存编译.java文件为.class文件
retransform 加载外部的.class文件, retransform到JVM里
redefine 加载外部的.class文件，redefine到JVM里
dump dump已加载类的byte code到特定目录
classloader 查看classloader的继承树，urts，类加载信息，使用classloader去getResource
	-t 查看classloader的继承树
	-l 按类加载实例查看统计信息
	-c 用classloader对应的hashcode来查看对应的 Jar urls
```



- redefine 的 class 不能修改、添加、删除类的 field 和 method，包括方法参数、方法名称及返回值
- classloader --url-stat  统计 ClassLoader 实际使用 URL 和未使用的 URL



**monitor/watch/trace 相关**

```shell
monitor 方法执行监控，调用次数、执行时间、失败率
	-c 统计周期，默认值为120秒
watch 方法执行观测，能观察到的范围为：返回值、抛出异常、入参，通过编写groovy表达式进行对应变量的查看
	-b 在方法调用之前观察(默认关闭)
	-e 在方法异常之后观察(默认关闭)
	-s 在方法返回之后观察(默认关闭)
	-f 在方法结束之后(正常返回和异常返回)观察(默认开启)
	-x 指定输岀结果的属性遍历深度,默认为0
trace 方法内部调用路径,并输出方法路径上的每个节点上耗时
	-n 执行次数限制
stack 输出当前方法被调用的调用路径
tt 方法执行数据的时空隧道,记录下指定方法每次调用的入参和返回信息,并能对这些不同的时间下调用进行观测
	通过tt 记录每次调用的入参和返回信息，然后可以重放
	-p  重放
```

```bash
tt -t com.Arthas out -n 5

trace com.Arthas out -n 5
```

**其他**

```shell
jobs 列出所有job
kill 强制终止任务
fg 将暂停的任务拉到前台执行
bg 将暂停的任务放到后台执行
grep 搜索满足条件的结果
plaintext 将命令的结果去除ANSI颜色
wc 按行统计输出结果
options 查看或设置Arthas全局开关
profiler 使用async-profiler对应用采样，生成火焰图
```

