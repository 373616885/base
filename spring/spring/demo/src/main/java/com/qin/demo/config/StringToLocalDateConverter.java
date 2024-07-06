package com.qin.demo.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    final static DateTimeFormatter formatter =  DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate convert(String source) {
        return LocalDate.parse(source, formatter);
    }

    public static void main(String[] args) {
        DefaultConversionService convers1onService =new DefaultConversionService ();
        convers1onService.addConverter(new StringToLocalDateConverter ());
        String phoneNumberStr = "2019-09-10";
        LocalDate date = convers1onService.convert(phoneNumberStr,LocalDate.class);
        System.out.println(date.format(formatter));
    }
}
