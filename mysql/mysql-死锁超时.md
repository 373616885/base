### Deadlock found when trying to get lock; try restarting transaction

出现死锁需要2个条件：
1）至少2个client（A，B）同时在执行事务
2）clientA锁定了某一行，未提交事务，此时clientB也需要update/delete这一行，此时clientB就会进入等待状态，直到出现Deadlock 



client（A，B）同时在删除 一条记录，

clientA锁定了某一行，删除，未提交事务，

clientB锁定了某一行，删除，未提交事务，

最后Deadlock found when trying to get lock或者 clientB超时





