1） 在 5.5.3 之前，MySQL 的 utf8 编码最大支持 3 字节（即 utf8mb3 编码）

也就是说 MySQL 的 utf8 = utf8mb3 （很多特殊符号不支持 如 Emoji，不常用汉字等）

后来增加了 utf8mb4 编码，用来兼容 4 字节的 Unicode

**utf8mb4 是 utf8mb3 的超集**



2） 执行 `show variables like 'char%';` 命令会显示 MySQL Server 中字符集相关的环境变量

character_set_server、character_set_database 这两个最重要，但默认已经是 utf8mb4







