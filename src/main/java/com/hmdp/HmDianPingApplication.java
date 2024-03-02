package com.hmdp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@MapperScan("com.hmdp.mapper")
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = { @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public class HmDianPingApplication {

    void hh(){
        System.out.println("sdnp666");
        System.out.println("jj");
    }
    public static void main(String[] args) {
        System.out.println("caonima");
        System.out.println("sa");
        System.out.println("8");
        System.out.println("sdsidie");
        SpringApplication.run(HmDianPingApplication.class, args);
    }

    void kk() {

    }
    void jj(){


    }
}
