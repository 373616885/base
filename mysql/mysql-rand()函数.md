### rand() 随机数

```mysql
SELECT id,rand() FROM t_menu;
```



### 随机获取10条数据

```mysql
SELECT id,rand() FROM t_menu ORDER BY RAND() LIMIT 10;
```



### bug

```mysql
select * from (
SELECT id, rand() *10  as randval FROM t_menu ) t
where t.randval > 2

得到的结果还有 randval 小于 2 的

2	5.107979555714857
3	2.4013372067374523
5	4.153405767077027
7	7.436372765746315
8	5.03485273107407
9	8.734849764718534
10	0.6484737160135746
11	3.707355487819161
12	4.603493590469932
```

当子查询中有引用到rand()都会重算一次

这个一个bug已经有人提bug单了https://bugs.mysql.com/bug.php?id=86624

但一直没修复

解决办法：加上 limit

```mysql
select * from (
SELECT id, rand() *10  as randval FROM t_menu limit 5 ) t
where t.randval > 2


结果：
1	7.5158952339700384
2	5.052671493080139
3	2.71567207082703
4	8.420346182231182
5	3.954715006011273
```





