package com.qin.demo.bean;

import java.time.LocalDate;
import java.util.Date;

public class MyBean {

    private Date dateValue;

    private LocalDate localDate;

    private MyTestBean myTestBean;
    public MyBean() {
    }
    public MyBean(MyTestBean myTestBean) {
        this.myTestBean = myTestBean;
    }

    private String testStr = "MyBean";

    private String password = "78802581";

    private String nameStr ;

    public String getTestStr() {
        return testStr;
    }

    public void setTestStr(String testStr) {
        this.testStr = testStr;
    }

    public MyTestBean getMyTestBean() {
        return myTestBean;
    }

    public void setMyTestBean(MyTestBean myTestBean) {
        this.myTestBean = myTestBean;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNameStr() {
        return nameStr;
    }

    public void setNameStr(String nameStr) {
        this.nameStr = nameStr;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }


}
