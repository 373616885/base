### 1 下载并安装 VC_redist.x64.exe

[下载地址](https://learn.microsoft.com/en-US/cpp/windows/latest-supported-vc-redist?view=msvc-170)（2015、2017、2019、2022 是同一个）

该软件必须安装，否则执行 mysqld、mysql 等命令时将没有任何反应。



### 2 下载

[官方下载地址](https://link.zhihu.com/?target=http%3A//dev.mysql.com/downloads/)：http://dev.mysql.com/downloads/



### 3 解压，并移动到安装文件夹

安装文件夹可以任意指定，本文中以 `D:/mysql/` 为例。

你需要将下文中的 `D:/mysql/` 替换为你的 MySQL 安装目录。

### 4 初始化 data 目录

初始化 data 目录就是生成一个名为 data 的目录。数据表、权限表、时区表等都定义在该目录下。

打开命令行，将 `D:/mysql/bin` 设为当前工作目录。执行以下命令，就会在 MySQL 安装目录下生成 data 目录。

```powershell
# 方法一。使用该方法，第一次登陆 MySQL 时不需要密码。
./mysqld --initialize-insecure

# 方法二：使用该方法，会生成一个临时密码。临时密码位于 data 文件夹后缀为 .err 的文件中。
./mysqld --initialize

# 方法三：使用该方法，会生成一个临时密码。临时密码会显示在命令行窗口中。
./mysqld --initialize --console
```

如果你想将 data 目录生成在其它位置，例如 `D:/data`，使用 `--datadir` 选项：

```powershell
.\mysqld --initialize-insecure --datadir=D:/data
```

如果 data 目录不在默认位置，则需要将新的 data 目录的路径写入到 my.ini 中，否则 MySQL Server 就找不到 data 目录了。



#### MariaDB的初始化

```powershell
mysql_install_db.exe
```



### 5 启动 MySQL

在命令行中执行以下命令，就可以启动 MySQL Server 了：

```powershell
./mysqld  # 或 ./mysqld --console
```

不要关闭该命令行窗口！！！关闭该窗口就会关闭 MySQL 的进程。

> 排查问题时可以使用 --verbose 选项。



### 6 登陆 MySQL

再打开一个新的命令行窗口，在其中输入以下命令:

```powershell
./mysql -u root -p
```

输入密码，就可以登陆到 MySQL 了。（如果使用的是第四步中的方法一，则无需输入密码，直接按回车键即可）





### 7 修改密码

登录到 MySQL，然后输入：

```mysql
ALTER USER 'root'@'localhost' IDENTIFIED BY 'new_password';
```

将上述命令中的 `new_password` 替换为你的新密码。







### 配置文件 my.ini

在 D:/mysql/ 内新建文本文件 my.ini，在其中写入以下内容：

```text
[mysqld]
basedir=D:/mysql  # 设置 MySQL 安装文件夹，默认是 mysqld.exe 的上级目录，一般无需设置
datadir=D:/data   # 设置 data 文件夹。如果你没改过 data 目录的位置，那无需设置该项
character_set_server=utf8mb4 # 设置 MySQL Server 的字符集。默认就是 uft8mb4，无需设置
```

除了 my.ini，my.cnf 同样也是可以的；

除了 MySQL 安装目录 D:/mysql/，C:/windows/ 和 C:/ 同样也是可以的。

MySQL Sever 的所有设置项：[Server System Variables](https://link.zhihu.com/?target=https%3A//dev.mysql.com/doc/refman/8.0/en/server-system-variables.html)。**特别注意：**命令行中使用变量，单词间一般用 - 连接，如 --character-set-server=name，其它任何地方使用变量，单词间都使用 _ 连接，如 character_set_server。

配置文件中除了 [mysqld] 组，还有 [client]、[mysql]、[mysqldump] 等组。其中，[client] 下的配置项对所有客户端都有效，[mysql] 只对 mysql.exe 客户端有效，[mysqldump] 只对 mysqldump.exe 有效。



### 设置环境变量 PATH

W11 我的电脑--属性--系统--系统信息--高级设置



### 停机

```
 ./mysqladmin -u root -p373616885 shutdown
```



### 启动

```
./mysqld --console
```



### 后台运行

保存成bat文件

```
@echo off

if "%1"=="h" goto begin

start mshta vbscript:createobject("wscript.shell").run("""%~nx0"" h",0)(window.close)&&exit

:begin

start /b ./mysqld
```

