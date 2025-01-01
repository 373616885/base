### 使用root身份自动登录

#### 设置 root 密码

执行sudo passwd root，然后输入设置的密码，输入两次，完成了设置root用户密码

```shell
sudo passwd root

```

#### 修改 lightdm 配置文件： 50-ubuntu.conf

```shell
vi /usr/share/lightdm/lightdm.conf.d/50-ubuntu.conf

文件末尾加入如下两行

greeter-show-manual-login=true
all-guest=false

```

#### 修改 gdm 配置文件：   /etc/gdm3/custom.conf

```shell
vi  /etc/gdm3/custom.conf

在 [security] 部分添加
AllowRoot=true
```



#### 修改 gdm-autologin 

```shell
sudo vi /etc/pam.d/gdm-autologin 
注释掉
#auth required pam_succeed_if.so user != root quiet_success
这一行(第三行左右)
```

#### 修改  gdm-password

```shell
sudo vi /etc/pam.d/gdm-password
注释掉 
#auth required pam_succeed_if.so user != root quiet_success
这一行(第三行左右)
```



#### 修改/root/.profile文件

三、修改profile文件

修改/root/.profile文件，编辑代码如下

```text
sudo nano /root/.profile
```

注释掉或者删除行

```text
mesg n 2＞ /dev/null || true
```

插入新行

```text
tty -s && mesg n || true
```

注意：当没有执行第一步“设置root用户密码”时，/root/.profile文件是不存在的所以对于新安装的系统来说，第一步是非常重要的



### 检查是否开启SSH服务 

```shell
ps -e|grep ssh
```



### 安装SSH服务

```shell
#客户端
sudo apt install openssh-client
#服务端
sudo apt install openssh-server
#或者
sudo apt install ssh
```



### 启动SSH服务 

```shell
service ssh restart
```



### 修改SSH配置文件 

```shell
vim /etc/ssh/sshd_config

在打开sshd_config后，找到PermitRootLogin without-password 修改为PermitRootLogin yes

PermitRootLogin yes
打开注释
Port 22
打开注释
PasswordAuthentication yes

```



### 重启SSH服务

```shell
 service ssh restart
```





### 查看ip

```
ifconfig
由于虚拟机上的ip不一定是127.0.0.1
所以要查看ip
```



### 使用 MobaXterm 登录