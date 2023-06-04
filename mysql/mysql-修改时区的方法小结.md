## 检查当前时区设置

```plsql
SELECT @@global.time_zone, @@session.time_zone;

```



## 修改全局时区

```sql
SET GLOBAL time_zone = 'Asia/Shanghai';

```



## 修改当前会话的时区

```sql
SET time_zone = 'Asia/Shanghai';
```



## 使之立即生效

```sql
flush privileges;
```





## 修改配置文件 /etc/my.cnf

```sql
[mysqld]

default-time_zone = 'Asia/Shanghai'
```



## 查询时间，检验时间对不对

```sql
select now();
```

