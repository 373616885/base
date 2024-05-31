### 下载



下载地址：https://nodejs.org/en/download



下载 Binary zip 免安装版



### 配置环境变量

我的电脑-->属性-->高级系统设置 -->环境变量

添加 ： path 

D:\Program Files\node-v20.14.0



### 验证

```shell
npm -v
```



### 改变安装NodeJS依赖的下载位置

分别在NodeJS安装目录下创建node_cache和node_global两个文件夹

打开dos命令窗口，分别执行下面两行命令

```shell
npm config set prefix "D:\Program Files\node-v20.14.0\node_global"
npm config set cache "D:\Program Files\node-v20.14.0\node_cache"
```



### 新建NODE_PATH系统变量

在 node_global 下新建的 node_modules

NODE_PATH : 

```shell
D:\Program Files\node-v20.14.0\node_global\node_modules
```

注意：是在 node_global 下新建的 node_modules ，不是Node安装目录下的 node_modules



###  node 全局模块的变量也放到Path下

由于 node 全局模块大多数都是可以通过命令行访问的

还要把【node_global】的路径  D:\Program Files\node-v20.14.0\node_global  

加入到【系统变量 】下的【PATH】 变量中，方便直接使用命令行运行



### 设置淘宝镜像

查看当前使用的镜像路径

```shell
npm config get registry
```

更换npm为淘宝镜像

```shell
npm config set registry https://registry.npmmirror.com
```





### 全局安装cnpm

```
npm install -g cnpm --registry=https://registry.npmmirror.com
```





### 查看当前镜像源： 

```bash
npm config get registry
```

### 查看npm当前安装的依赖包：

```bash
npm list
```





### npm报错：request to https://registry.npm.taobao.org failed, reason certificate has expired



**错误提示已经告诉原因是淘宝镜像过期了！**

其实，早在 2021 年，淘宝就发文称，[npm](https://so.csdn.net/so/search?q=npm&spm=1001.2101.3001.7020) 淘宝镜像已经从 registry.npm.taobao.org 切换到了 registry.npmmirror.com。旧域名也将于 2022 年 5 月 31 日停止服务（不过，直到今天 HTTPS 证书到期才真正不能用了）



老项目记得先删掉package-lock.json



三、解决方案

1、查看当前的npm镜像设置：npm config list

2、清空缓存：npm cache clean --force

3、然后修改镜像即可：npm config set registry https://registry.npmmirror.com

4、再次运行： npm config list，查看 registry 已经被更改为默认的 npm 公共镜像地址。



