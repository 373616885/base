### **同服务的跨库查询**

```sql
SELECT * FROM 数据库1.table1 x JOIN 数据库2.table2 y ON x.field1=y.field2


SELECT * from qin.tlc_order a LEFT JOIN jie.tlc_order_package b on  a.logistic_no =b.logistic_no
```





### **不同服务的跨库查询**

需要MySQL支持federated引擎

```sql
show engines;

Engine             Support 
FEDERATED          NO      # No,表示没有启用
```

开启 

my.cnf 文件末加上1行FEDERATED，重启 MySQL 即可



语句格式：

```sql
CREATE TABLE table_name（……）ENGINE=FEDERATED CONNECTION='mysql://[username]:[password]@[localtion]:[port]/[db-name]/[table-name]'


CREATE TABLE `app` (
 ) ENGINE=FEDERATED DEFAULT CHARSET=utf8 CONNECTION='mysql://root:123456@127.0.0.1:3306/test/app1';
```



需要注意的几点：

1. 本地的表结构必须与远程的完全一样。

2. 远程数据库目前仅限MySQL

3. 不支持事务

4. 不支持表结构修改

