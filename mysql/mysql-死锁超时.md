### Deadlock found when trying to get lock; try restarting transaction

出现死锁需要2个条件：
1）至少2个client（A，B）同时在执行事务
2）clientA锁定了某一行，未提交事务，此时clientB也需要update/delete这一行，此时clientB就会进入等待状态，直到出现Deadlock 



client（A，B）同时在删除 一条记录，

clientA锁定了某一行，删除，未提交事务，

clientB锁定了某一行，删除，未提交事务，

最后Deadlock found when trying to get lock或者 clientB超时





insert 违反唯一性约束的也会死锁

第一个插人排他X锁，第二个插人变成共享S锁，第三个插人变成共享S锁

就是后面的两个共享S锁，导致死锁

第一个释放排他X锁，第二个共享S锁就会变成插人意向IX锁，第三个共享S锁就会和第二个插人意向IX锁产生死锁

