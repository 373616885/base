package com.qin.demo.bean;

public class MyTestBean {

    private MyBean myBean;

    private String testStr = "testStr";

    public MyTestBean(MyBean myBean) {
        this.myBean= myBean;
    }
//    public MyTestBean(MyBean myBean,String testStr) {
//        this.testStr = testStr;
//        this.myBean= myBean;
//    }

    public String getTestStr() {
        return testStr;
    }

    public void setTestStr(String testStr) {
        this.testStr = testStr;
    }

    public MyBean getMyBean() {
        return myBean;
    }

    public void setMyBean(MyBean myBean) {
        this.myBean = myBean;
    }
}
