package com.example.jdbc.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class AnnotationServiceImpl implements AnnotationService {

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private String insert_sql = "INSERT INTO t_player (uid, name) VALUES ('10', 'a')";

    @Override
    public void save() {
        System.out.println("==开始执行sql");

        jdbcTemplate.update(insert_sql);

        System.out.println("==结束执行sql");

        System.out.println("==准备抛出异常");

        throw new RuntimeException("==手动抛出一个异常");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = RuntimeException.class,readOnly = false)
    public void delete() throws RuntimeException {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void beforeCommit(boolean readOnly) {
                System.out.println("==回调,事物提交之前");
                super.beforeCommit(readOnly);
            }

            @Override
            public void afterCommit() {
                System.out.println("==回调,事物提交之后");
                super.afterCommit();
            }

            @Override
            public void beforeCompletion() {
                super.beforeCompletion();
                System.out.println("==回调,事物完成之前");
            }

            @Override
            public void afterCompletion(int status) {
                super.afterCompletion(status);
                System.out.println("==回调,事物完成之后");
            }
        });

        System.out.println("==调用AccountService的dele方法\n");
        jdbcTemplate.update(insert_sql);
    }
}
