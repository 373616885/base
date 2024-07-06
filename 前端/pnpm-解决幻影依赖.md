### npm 存在幻影依赖

解决：使用 pnpm



### 幻影依赖

npm 安装完成之后会将所有的 **直接依赖 和 间接依赖** 都放到 node_modules 里面

好处节省空间（相同的间接依赖不对存在多份）



**例如：**

安装了 element-plus 间接依赖了 lodash-es ，这两个都放到了 node_modules 里面 

由于间接依赖的 lodash-es 在  node_modules 里面，项目可以正常使用



**产生两个隐患问题：**

第一个：版本问题

依赖的 A 版本升级了，导致 间接依赖的 B 也升级了

项目中使用的  B 还是旧版本的写法，这就导致修改起来很麻烦

真实的开发中这样的问题很难发现 js 不像 java 一样有个编译报错的过程

第二个：依赖丢失

dev 开发的时候，依赖的 A 和 间接依赖 B

项目中使用了（无意） 间接依赖 B（真实开发package.json里面很大，一般不会一个一个对必）

pro 的时候，没了依赖的 A 导致 间接依赖 B 也丢了

由于项目使用 间接依赖 B 导致项目出问题



### 使用 pnpm 解决

原理：

node_modules 只保存声明依赖的软连接（快捷方式）

真实依赖都放到了 pnpm 文件夹下面



由于 node_modules 只保存声明依赖，在项目中使用了间接依赖会报错找不到

必须声明一下才可以



### 总结

npm 间接依赖可以在项目中直接使用，pnpm 切断了这一步

想要在项目中直接使用，必须显示声明



### 安装

```
npm install -g pnpm
```



### 查看当前pnpm版本

```
pnpm -v
```



### 安装完成后，配置镜像源

```
# 获取当前配置的镜像地址
pnpm get registry
or
pnpm config get registry

# 设置新的镜像地址
pnpm set registry https://registry.npmmirror.com

```



### 修改官方默认的安装包安装路径位置

该操作可选- - 默认安装包路径位置是C盘的Local目录下

```
# 允许设置全局安装包的 bin 文件的目标目录。
pnpm config set global-bin-dir "E:\pnpm-store"
# 包元数据缓存的位置。
pnpm config set cache-dir "E:\pnpm-store\pnpm-cache"
# pnpm 创建的当前仅由更新检查器使用的 pnpm-state.json 文件的目录。
pnpm config set state-dir "E:\pnpm-store\pnpm-state"
# 指定储存全局依赖的目录。
pnpm config set global-dir "E:\pnpm-store\global"
# 所有包被保存在磁盘上的位置。
#（可选，以下这条命令可以选择不执行也是OK的）
pnpm config set store-dir "E:\pnpm-store\pnpm-store"

```

设置完后，在以下目录中有一个配置文件 rc 生成。里面的内容就是上面设置的

> **C:\Users\用户名\AppData\Local\pnpm\config\rc**



##### **pnpm 默认是安装在C盘的Local目录下的**



##### 配置pnpm环境变量

PNPM_HOME = xxxx

path= %PNPM_HOME%

 

##### 检验设置的路径是否是自己设置

```bash
pnpm c list
```

可以看到相关配置，自己对比是否正确







