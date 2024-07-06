package com.qin.demo.bean;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.StringUtils;

public class CarFactoryBean implements FactoryBean<Car> {

    private String info;

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public Car getObject() throws Exception {
        String[] infos = StringUtils.tokenizeToStringArray(info, ",");
        Car car = new Car();
        if (infos.length >= 3) {
            car.setBrand(infos[0]);
            car.setMaxSpeed(Integer.valueOf(infos[1]));
            car.setPrice(Double.valueOf(infos[2]));
        }
        return car;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public Class<?> getObjectType() {
        return Car.class;
    }
}
