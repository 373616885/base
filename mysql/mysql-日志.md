

MySQL为了提高的性能，对于增、删、改这种操作都是在内存中完成的，所谓的内存就是BufferPool

有专门的后台线程等其他机制负责将脏数据页刷新同步回磁盘

![](img\20220914142941246.png)



### mysql 数据落盘过程

![](img\20220914142941245.png)

### undo

Undo日志存储事务更改前、未发生变化的数据

写入Redo日志这个操作之前，它将获取一个未经修改的数据库页的副本，并将其保存到Undo日志。



**作用：用于事务的回滚**

MVCC 使用 undo log 进行事务的回滚

**undo log的写入时机**

DML操作修改聚簇索引前，记录undo日志



### redo

是`InnoDB`存储引擎独有的`redo log`（重做日志）

作用：事务提交了，数据没有落盘，数据库崩了，通过redo.log 可以恢复，因为redo.log落盘了

一个事务提交的时候写入缓存中的   一次性日志    文件（事务提交之后没用了）



采用的是环形数组形式，从头开始写，写到末尾又回到头循环写

![](img\20220914142941242.png)

在个**日志文件组**中还有两个重要的属性，分别是`write pos、checkpoint`

- **write pos是当前记录的位置，一边写一边后移**
- **checkpoint是当前要擦除的位置，也是往后推移**

极端情况：如果`write pos`追上`checkpoint`，表示**日志文件组**满了，这时候不能再写入新的`redo log`记录，`MySQL`得停下来，清空一些记录，把`checkpoint`推进一下



#### 工作过程

因为mysql提交时，是采取了两段提交协议的

1. redo log 处于prepare阶段，日志写入到磁盘。

2. 写入binlog。

3. redo commit 提交事务。

如果  redo log 处于prepare 阶段宕机了，事务没提交，数据不见了就算了

如果  写入binlog 阶段宕机了，事务也没提交，数据不见了就算了

如果  redo commit 提交   阶段宕机了，恢复就可以从这里拿到 2的数据进行恢复

![](img\20220914142941244.png)







#### 刷盘时机

`InnoDB`存储引擎为`redo log`的刷盘策略提供了`innodb_flush_log_at_trx_commit`参数，它支持三种策略

- **设置为0的时候，表示每次事务提交时不进行刷盘操作**
- **设置为1的时候，表示每次事务提交时都将进行刷盘操作（默认值）**
- **设置为2的时候，表示每次事务提交时都只把redo log buffer内容写入page cache**

`InnoDB`存储引擎有一个后台线程，每隔`1`秒，就会把`redo log buffer`中的内容写到文件系统缓存（`page cache`），然后调用`fsync`刷盘。

事务执行过程`redo log`记录是会写入`redo log buffer`中，这些`redo log`记录会被后台线程刷盘



![](img\20220914142941243.png)



**一旦持久化到磁盘，redo log中对应的那部分数据就可以释放**







### bin log

记录了数据库所有执行的 `DDL` 和 `DML` 等数据库更新的语句

但是不包含`select`或者`show`等没有修改任何数据的语句

作用：

- **数据恢复**，如果MySQL数据库意外挂了，可以利用`bin log`进行数据恢复，因为该日志记录所有数据库所有的变更，保证数据的安全性。
- **数据备份**，利用一定的机制将主节点MySQL的日志数据传递给从节点，实现数据的一致性，实现架构的高可用和高性能。

所以`bin log`对于**数据备份**、**主从**、**主主**等都都起到了关键作用。



**bin log位置**

```
show variables like '%log_bin%';查看bin log最终输出的位置
```

![](img\20220914142941247.png)

- `log_bin_basename`: 是`bin log`日志的基本文件名，后面会追加标识来表示每一个文件
- `log_bin_index`: 是binlog文件的索引文件，这个文件管理了所有的binlog文件的目录



通过` SHOW BINARY LOGS;`查看当前的二进制日志文件列表及大小，如下图：

```
SHOW BINARY LOGS
```

![](img\20220914142941248.png)

**修改 bin log位置**

修改MySQL的my.cfg或my.ini配置

```
#启用二进制日志
log-bin=cxw-bin
binlog_expire_logs_seconds=600
max_binlog_size=100M
```

- `log-bin`: `bin log`日志保存的位置
- `binlog_expire_logs_seconds`: `bin log`日志保存的时间，单位是秒
- `max_binlog_size`： 单个`bin log`日志的容量



**bin log内容**

```sql
show binlog events [IN 'log_name'] [FROM pos] [LIMIT [offset,] row_count];

show binlog events  in 'binlog.000001';

```

![](img\20220914142941249.png)





**bin log 格式**

实际上bin log输出的格式类型有3种，默认是ROW类型，就是上面例子中的格式。

> **目前Mysql日志默认格式是ROW，5.7.7之前STATEMENT**



**Statement格式：**每一条会修改数据的sql都会记录在bin log中

优点：不需要记录每一行的变化，减少了bin log日志量，节约了IO，提高性能。

缺点：比如sql中存在函数如now()等，依赖环境的函数，会导致主从同步、恢复数据不一致



**ROW格式：**为了解决Statement缺点，记录具体哪一个分区中的、哪一个页中的、哪一行数据被修改了

优点：清楚的记录下每一行数据修改的细节，不会出现某些特定情况下 的存储过程，或function无法被正确复制的问题。

缺点：比如对ID<600的所有数据进行了修改操作,那么意味着很多数据发生变化，最终导致同步的log很多，那么磁盘IO、网络带宽开销会很高。



**Mixed格式:** 混合模式，即Statment、Row的结合版

对于可以复制的SQL采用Statment模式记录，对于无法复制的SQL采用Row记录。



### bin log和redo log区别？

redo log ： 

作用事务提交后，恢复数据用的，另外的线程异步落盘后checkpoint 之后就不见了，`InnoDB` 独有的

bin log ：

全量日志，一直增加，Server层生成的日志，所有的存储引擎都有



从使用场景角度来说：

- `redo log`主要实现故障情况下的数据恢复，保证事务的持久性
- `bin log`主要用于数据灾备、同步

从**数据内容**角度来说：

- `redo log`是"物理日志", 记录的是具体数据页上做了什么修改
- `bin log`是"逻辑日志", 记录内容是语句的原始逻辑，类似于“给 ID=2 这一行的 name 改为alvin”

从**生成范围**角度来说：

- `redo log`是`InnoDB`存储引擎生成的事务日志，其他存储引擎没有
- `bin log`是MySQL Server生成的日志，所有的存储引擎都有

从**生成时机**角度来说：

- `redo log`是在事务执行过程中就会write
- `bin log`是在事务提交的时候write





### relay_log 

从节点的中继日志