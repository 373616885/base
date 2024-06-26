### 为什么使用 MySQL 主从 ?

- **读写分离**：从库提供查询，减少主库压力，提升性能；
- **高可用**：故障时可切换从库，保证服务高可用；
- **数据备份**：数据备份到从库，防止服务器宕机导致数据丢失。

在 4 核 8G 的机器上运行 MySQL 5.7 时，大概可以支撑 500 的 TPS （每秒处理量 ）和 10000 的 QPS







### 主从原理

mysql主从同步过程

主从复制主要有三个线程

master : binlog ，dump 线程

salve：relay_log ，I/O 线程 ，sql 线程

master 一个 salve两个

1. 主节点 开启binlog ，主节点将所有变更记录到 binlog 中
2. 主节点的 binlog dump 线程发送去内容到从节点
3. 从节点 I/O 线程收到后，将其写入relay_log 文件中（中继日志）
4. 从节点的sql 线程读取relay_log 的内容对数据更新重放



注意：主节点使用的 binlog + position 偏移量来定位主从的位置，从节点会保存其接收到的偏移量

如果从节点发送宕机，会自动从这个 position 的位置发起同步



### mysql 复制方式

异步的：这是默认的，主库不关心从库是否已经处理，这会产生一个问题，主库桂了，从库处理失败，从库升主库后，日志就丢失了

全同步：主库写入binlog后，强制同步到从库relay_log日志，等所有从库执行完成后才返回给客户端，但这明显性能有问题

半同步：主库写入binlog后，收到至少一个从库的确认完成后，就认为完成返回客户端



### 问：如何保证主从一致

答案：binlog 格式修改为：row 

> **目前Mysql日志默认格式是ROW，5.7.7之前STATEMENT**



对于下面的情况，假如主库执行如下 SQL，其中 a 和 create_time 都是索引：

```sql
delete from t where a > '666' and create_time<'2022-03-01' limit 1;
```

我们知道，数据选择了 a 索引和选择 create_time 索引，最后 limit 1 出来的数据一般是不一样的。

所以就会存在这种情况：在 binlog = statement 格式时，主库在执行这条 SQL 时，使用的是索引 a，而从库在执行这条 SQL 时，使用了索引 create_time，最后主从数据不一致了。



**那么我们该如何解决呢？**

可以把 binlog 格式修改为 row，row 格式的 binlog 日志记录的不是 SQL 原文，而是两个 event:Table_map 和 Delete_rows。

Table_map event 说明要操作的表，Delete_rows event用于定义要删除的行为，记录删除的具体行数。**row 格式的 binlog 记录的就是要删除的主键 ID 信息，因此不会出现主从不一致的问题。**

但是如果 SQL 删除 10 万行数据，使用 row 格式就会很占空间，10 万条数据都在 binlog 里面，写 binlog 的时候也很耗 IO。但是 statement 格式的 binlog 可能会导致数据不一致。

设计 MySQL 的大叔想了一个折中的方案，mixed 格式的 binlog，其实就是 row 和 statement 格式混合使用，**当 MySQL 判断可能数据不一致时，就用 row 格式，否则使用就用 statement 格式**







### 主从延迟原理

谈到 MySQL 数据库主从同步延迟原理，得从 MySQL 的主从复制原理说起：

- MySQL 的主从复制都是单线程的操作，主库对所有 DDL 和 DML 产生 binlog，binlog 是顺序写，所以效率很高；
- Slave 的 Slave_IO_Running 线程会到主库取日志，放入 relay log，效率会比较高；
- Slave 的 Slave_SQL_Running 线程将主库的 DDL 和 DML 操作都在 Slave 实施，DML 和 DDL 的 IO 操作是随机的，不是顺序的，因此成本会很高，还可能是 Slave 上的其他查询产生 lock 争用，由于 Slave_SQL_Running 也是单线程的，所以一个 DDL 卡住了，需要执行 10 分钟，那么所有之后的 DDL 会等待这个 DDL 执行完才会继续执行，这就导致了延时。

**总结一下主从延迟的主要原因**：主从延迟主要是出现在 “relay log 回放” 这一步，当主库的 TPS 并发较高，产生的 DDL 数量超过从库一个 SQL 线程所能承受的范围，那么延时就产生了，当然还有就是可能与从库的大型 query 语句产生了锁等待。



### 问：有没有遇到主从延迟情况（场景）

答：机器较差，大事务，网络较差，从库过多



- **从库机器性能**：从库机器比主库的机器性能差，只需选择主从库一样规格的机器就好。
- **从库压力大**：可以搞了一主多从的架构，还可以把 binlog 接入到 Hadoop 这类系统，让它们提供查询的能力。
- **从库过多**：要避免复制的从节点数量过多，**从库数据一般以3-5个为宜。**
- **大事务**：如果一个事务执行就要 10 分钟，那么主库执行完后，给到从库执行，最后这个事务可能就会导致从库延迟 10 分钟啦。日常开发中，不要一次性 delete 太多 SQL，需要分批进行，另外大表的 DDL 语句，也会导致大事务。
- **网络延迟**：优化网络，比如带宽 20M 升级到 100M。
- **MySQL 版本低**：低版本的 MySQL 只支持单线程复制，如果主库并发高，来不及传送到从库，就会导致延迟，可以换用更高版本的 MySQL，支持多线程复制。



### 问：你是如何解决主从延迟

答：使用缓存，查询主库，延迟查询



我们一般会把从库落后的时间作为一个重点的数据库指标做监控和报警，正常的时间是在毫秒级别，一旦落后的时间达到了秒级别就需要告警了。

解决该问题的方法，除了缩短主从延迟的时间，还有一些其它的方法，基本原理都是尽量不查询从库，具体解决方案如下：

- **使用缓存**：我们在同步写数据库的同时，也把数据写到缓存，查询数据时，会先查询缓存，不过这种情况会带来 MySQL 和 Redis 数据一致性问题。
- **查询主库**：直接查询主库，这种情况会给主库太大压力，不建议这种方式。
- **延迟查询**：延迟 1 秒或者 5秒 然后再去查询

在实际应用场景中，对于一些非常核心的场景，比如库存，支付订单等，需要直接查询主库，其它非核心场景，就不要去查主库了。





一般小公司都是一主一从的模式，这时主从分离的意义其实并不大，因为小公司的流量不高，更多是为了数据库的可用性，以及数据备份。







