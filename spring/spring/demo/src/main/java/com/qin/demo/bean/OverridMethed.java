package com.qin.demo.bean;

public class OverridMethed {

    private String methed() {
        return "OverridMethed";
    }

    String ov() {
        return "OverridMethed.ov";
    }

    final String finalov() {
        return "OverridMethed.finalov";
    }

    String t() {
        ov();
        return "tttttt";
    }

}
