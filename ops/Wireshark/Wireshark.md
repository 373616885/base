### 官网

https://www.wireshark.org/

下载地址

https://www.wireshark.org/#downloadLink



### Wireshark   https 设置

只要配置环境变量 SSLKEYLOGFILE 即可

Chrome 浏览器会把 https 请求中的秘钥保存到 SSLKEYLOGFILE  指定的文件

W11--> 开始菜单 -> 搜索  设置 -> 系统--> 拉到最低  系统信息 --> 高级系统设置

--> 环境变量 --> 顶部用户变量

```
SLKEYLOGFILE=D:\sslkey.log
```

![](img\202501121319.png)



Wireshark

编辑>首选项>protocol>TLS （有的版本只有SSL）

![](img\202501121320.png)



#### 接着

彻底关闭 Chrome 进程

重启Wireshark   即可看到 http 数据流

右键追踪流 http 流 就能看到网站 https 数据流



### wireshark界面认识

![](img\202501121327.png)



### 数据报对应网络模型

![](img\202501121330.png)



### 常用过滤

#### 过滤IP

```shell
ip.addr == IP
ip.addr != 210.52.217.84

ip.src eq ${IP} or ip.dst eq ${IP}
```

`eq`也可以用`==`代替



#### 过滤端口

```shell
# 过滤tcp端口==443
tcp.port == 443

tcp.port == 443 && ip.addr == 210.52.217.84
tcp.port == 443 || tcp.port == 80

# 过滤tcp目标端口==80 或 tcp源端口==80
tcp.dstport == 80 or tcp.srcport == 80

tcp.port == 443 and tcp.port == 80

# 过滤udp端口==53
udp.port eq 53
```



or  和 || 一样

and 和 && 一样



#### 过滤方向

| src                 | 源                    |
| ------------------- | --------------------- |
| dst                 | 目的地                |
| src and dst         | 源 and 目的地         |
| src or dst          | 源 or 目的地          |
| srcport             | 源端口                |
| dstport             | 目的地端口            |
| srcport and dstport | 源端口 and 目的地端口 |
| srcport or dstport  | 源端口 or 目的地端口  |





### 抓包本地 127.0.0.1 

捕获Adapter for loopback选项的网卡



过滤条件：tcp.port  要抓发送请求的端口，浏览器并发请求回显的很乱

```shell
tcp.port == 80 && ip.addr == 127.0.0.1
## tcp.port  要抓发送请求的端口，浏览器并发请求回显的很乱
tcp.port == 7076 && ip.addr == 127.0.0.1
```



