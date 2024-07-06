### btop 

是个开源工具，开源地址是这个 https://github.com/aristocratos/btop

top 的升级版

```shell
# 下载压缩包
wget https://github.com/aristocratos/btop/releases/download/v1.2.13/btop-x86_64-linux-musl.tbz
# 下载解压工具
yum install bzip2 -y
# 解压
bunzip2 btop-x86_64-linux-musl.tbz
tar xf btop-x86_64-linux-musl.tar
# 进入解压后的文件夹，进行安装
cd btop
# 指定安装的目录
make install PREFIX=/opt/btop
# 运行
/opt/btop/bin/btop
```

![](img\c5bc99822bbddb9cef1b11944759a8c1.png)





### **theFuck**

日常操作服务器的时候，虽然有些终端工具可以进行命令提示，

但是有时候难免还是会输错命令，或者少了空格，或者敲错了字符，这种情况下，我们都只能重新再输入一遍

`theFuck` 这个工具，可以在我们输错命令过后，纠正我们的命令，从而继续进行执行，效果如下



![](img\37db9346b1828f3ce485761429dd556d.gif)

```shell
sudo apt update
sudo apt install python3-dev python3-pip python3-setuptools
sudo pip3 install thefuck
```

