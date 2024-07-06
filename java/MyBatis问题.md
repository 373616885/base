### MyBatis 3.2.3版本支持parameterType和实际参数类型不匹配

MyBatis 3.2.3及以前的版本，会忽略XML中的parameterType这个属性

支持parameterType和实际参数类型不匹配，就没用

MyBatis 3.2.4及之后Mapper方法中的参数和XML中的parameterType不匹配时，进而会出现类型转换报错



### mybatis3.5.1 LocalDatetime转换异常问题

mybatis3.5.1更新了一个问题，导致不向后兼容

 #1478 

LocalDateTypeHandler、LocalTimeTypeHandler 和 LocalDateTimeTypeHandler 

现在需要一个支持 JDBC 4.2 API 的 JDBC 驱动程序

解决：

1.mybatis3.5.1降级或jdbc升级

2.自己写处理器处理LocalDate

