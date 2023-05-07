package com.example.jdbc.service;

import org.springframework.jdbc.core.JdbcTemplate;

public class AccountServiceImpl implements  AccountService {


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
}
