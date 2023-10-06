### 下载



下载地址：https://nodejs.org/en/download



下载 Binary zip 免安装版



### 配置环境变量

我的电脑-->属性-->高级系统设置 -->环境变量

添加 ： path 

D:\Program Files\node-v18.18.0



### 验证

```shell
npm -v
```



### 改变安装NodeJS依赖的下载位置

分别在NodeJS安装目录下创建node_cache和node_global两个文件夹

打开dos命令窗口，分别执行下面两行命令

```shell
npm config set prefix "D:\Program Files\node-v18.18.0\node_global"
npm config set cache "D:\Program Files\node-v18.18.0\node_cache"
```



### 新建NODE_PATH系统变量

在 node_global 下新建的 node_modules

NODE_PATH : 

```shell
D:\Program Files\node-v18.18.0\node_global\node_modules
```

注意：是在 node_global 下新建的 node_modules ，不是Node安装目录下的 node_modules



###  node 全局模块的变量也放到Path下

由于 node 全局模块大多数都是可以通过命令行访问的

还要把【node_global】的路径  D:\Program Files\node-v18.18.0\node_global  

加入到【系统变量 】下的【PATH】 变量中，方便直接使用命令行运行



### 设置淘宝镜像

查看当前使用的镜像路径

```shell
npm config get registry
```

更换npm为淘宝镜像

```shell
npm config set registry https://registry.npm.taobao.org
```





### 全局安装cnpm

```
npm install -g cnpm --registry=https://registry.npm.taobao.org
```

