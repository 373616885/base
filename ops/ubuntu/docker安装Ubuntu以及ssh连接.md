### Windows的安装docker

下载：https://www.docker.com/products/docker-desktop



### 拉取ubuntu

```shell
docker pull ubuntu:20.04
```



### 查看拉取是否成功

```shell
docker images
```



### 运行容器

```shell
docker run --name iubuntu -it -d -p 3316:22 ubuntu
```



### 查看是否运行成功

```shell
docker ps
```



### 安装ssh服务



### 进入容器终端安装ssh服务

```shell
docker exec -it iubuntu /bin/bash


如果出现：
the input device is not a TTY.  If you are using mintty, try prefixing the command with 'winpty'

winpty docker exec -it iubuntu bash
```



### 执行更新

```shell
apt-get update
```



### 安装ssh-client

```shell
apt-get install openssh-client
```



### 安装ssh-server

```shell
apt-get install openssh-server
```



### 启动ssh服务

```shell
/etc/init.d/ssh start
```



### 安装vim编辑器

```shell
apt-get install vim
```



### 编辑sshd_config文件

```shell
vim /etc/ssh/sshd_config
```

修改：

PermitRootLogin yes

Port 22

PasswordAuthentication yes

保存退出 ESC + : + WQ





### 重启ssh服务

```shell
service ssh restart
```



### 设置ssh密码

```shell
passwd root
```



### 查看容器的IP



### 安装net-tools工具包

```shell
apt-get install net-tools
```



### 查看IP

```shell
ifconfig
```



### 退出

```shell
exit
```



### 保存刚刚修改的镜像

```shell
docker commit  iubuntu   ubuntu-qin:20.04
```



### 运行新的镜像

```shell
docker run --name ubuntu-qin -it -d -p 3316:22 ubuntu-qin:20.04
```



### 下载MobaXterm

https://mobaxterm.mobatek.net/download-home-edition.html



### 链接

```
ifconfig 查看ip,不一定是127.0.0.1
```

IP: 127.0.0.1

port : 3316





### 无法登录SSH问题-ssh: connect to host localhost port 22: Connection refused

可能没有启动 ： openssh-server

```shell
docker exec -it ubuntu-qin /bin/bash
ps -ef|grep sshd
service ssh restart
再次
ps -ef|grep sshd

```



