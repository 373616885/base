package com.qin.demo.bean;

public class FinalOverridMethed extends OverridMethed{
    public String methed(){
        return "FinalOverridMethed";
    }

    @Override
    String ov() {
        return "FinalOverridMethed.ov()";
    }



    public static void main(String[] args) {
        OverridMethed o = new FinalOverridMethed();
        System.out.println(o.ov());
        StaticClass staticClass = new StaticClass();

    }
}
