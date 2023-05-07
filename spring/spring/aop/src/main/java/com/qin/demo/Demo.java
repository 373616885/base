package com.qin.demo;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class Demo {

    public static void main(String[] args) {
        ThreadMXBean mxBean =  ManagementFactory.getThreadMXBean();
        long[] ids = mxBean.getAllThreadIds();
        for (long id : ids) {
            ThreadInfo info = mxBean.getThreadInfo(id);
            System.out.println(info.getThreadId() + "   " + info.getThreadName());
        }
    }

}
