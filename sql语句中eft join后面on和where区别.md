在使用left jion时，on和where条件的区别如下：

1. on条件是在生成临时表时使用的条件，它不管on中的条件是否为真，都会返回左边表中的记录。
2. where条件是在临时表生成好后，再对临时表进行过滤的条件。这时已经没有left join的含义（必须返回左边表的记录）了，条件不为真的就全部过滤掉。

关键原因就是left join,right join,full join的特殊性

1. inner jion没这个特殊性，则条件放在on中和where中，返回的结果集是相同的。
2. full则具有left和right的特性的并集





