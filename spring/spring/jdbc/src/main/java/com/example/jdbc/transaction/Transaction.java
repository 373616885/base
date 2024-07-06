package com.example.jdbc.transaction;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

public class Transaction {

    private JdbcTemplate jdbcTemplate;

    // jdbc 管理事务接口
    private DataSourceTransactionManager txManager;

    private DefaultTransactionDefinition txDefinition;

    private String insert_sql = "INSERT INTO t_player (uid, name) VALUES ('10', 'a')";

    public static void main(String[] args) {
        Transaction transaction = new Transaction();

        transaction.save();
    }

    public void save() {
        // 1、初始化jdbcTemplate
        DataSource dataSource = getDataSource();
        jdbcTemplate = new JdbcTemplate(dataSource);

        // 2、创建物管理器
        txManager = new DataSourceTransactionManager();
        txManager.setDataSource(dataSource);

        // 3、定义事物属性
        txDefinition = new DefaultTransactionDefinition();
        // 事务传播特性
        txDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        // 事务隔离级别
        txDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT);


        // 3、开启事物
        TransactionStatus txStatus = txManager.getTransaction(txDefinition);

        // 4、执行业务逻辑
        try {
            jdbcTemplate.execute(insert_sql);
            //int i = 1/0;
            //jdbcTemplate.execute(insert_sql);
            txManager.commit(txStatus);
        } catch (DataAccessException e) {
            txManager.rollback(txStatus);
            e.printStackTrace();
        }
    }




    public DataSource getDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setJdbcUrl("jdbc:mysql://47.100.185.77:3306/qin?useSSL=false&useUnicode=true&characterEncoding=UTF-8");
        dataSource.setUsername("root");
        dataSource.setPassword("373616885");
        return dataSource;
    }

}
