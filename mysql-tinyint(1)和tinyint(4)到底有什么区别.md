### [Mysql中tinyint(1)和tinyint(4)到底有什么区别?](https://my.oschina.net/DavidRicardo/blog/869169)

答：

存储的方面没有任何关系

只有在无符号的zerofill的时候，M显示成对应的宽度，并填充0



英文解释：

The syntax of TINYINT data type is TINYINT(M), where M indicates the maximum display width (used only if your MySQL client supports it)



---

tinyint存储只是用一个字节,就是8位,只能存储2^8个数字,也就是256个数字,在mysql实现中,有符号是-128-127,无符号是0-255



tinyint后面的括号带的数字,以后称之为M,和存贮的值没有任何关系,只是在某些情况下和显示的宽度有关系

> 无符号和zerofill的时候会填充0,显示成M对应的宽度



tinyint(1) 不是代表可以接受范围 0-9

---





### 常规测试

基本的表,插入基本的数据

![](img\28144643_GnDd.png)

查询后发现没有任何区别

![](img\28144729_mrcG.png)



可以发现没有任何区别,实际上就是没有任何区别,如果你用navicat之类的工具试验,也会发现没有任何差别,详情可以参见引用> [http://stackoverflow.com/questions/12839927/mysql-tinyint-2-vs-tinyint1-what-is-the-difference](https://www.oschina.net/action/GoToLink?url=http%3A%2F%2Fstackoverflow.com%2Fquestions%2F12839927%2Fmysql-tinyint-2-vs-tinyint1-what-is-the-difference) 里面Aamir的回答可以做很好的验证





### 无符号建表,同时zerofill

zerofill的整数字段必须无符号

M表示特定的情况下的显示宽度,不够的时候会填充0,多余了不作处理



建表的基本语句是 

![](img\28145645_CSWE.png)

后查询结果如下,比较明显

![](img\28145720_oKxn.png)



```shell
mysql> CREATE TABLE tin3(id int PRIMARY KEY,val TINYINT(10) ZEROFILL);
Query OK, 0 rows affected (0.04 sec)

mysql> INSERT INTO tin3 VALUES(1,12),(2,7),(4,101);
Query OK, 3 rows affected (0.02 sec)
Records: 3  Duplicates: 0  Warnings: 0

mysql> SELECT * FROM tin3;
+----+------------+
| id | val        |
+----+------------+
|  1 | 0000000012 |
|  2 | 0000000007 |
|  4 | 0000000101 |
+----+------------+
3 rows in set (0.00 sec)

mysql>

mysql> SELECT LENGTH(val) FROM tin3 WHERE id=2;
+-------------+
| LENGTH(val) |
+-------------+
|          10 |
+-------------+
1 row in set (0.01 sec)


mysql> SELECT val+1 FROM tin3 WHERE id=2;
+-------+
| val+1 |
+-------+
|     8 |
+-------+
1 row in set (0.00 sec)
```

