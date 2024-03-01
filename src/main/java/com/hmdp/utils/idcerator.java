package com.hmdp.utils;


import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


@Component
public class idcerator {

    static final long BEGIN_TIMESTRAP=56131212L;
    
    @Autowired
    StringRedisTemplate redisTemplate;


    public long nextid(String keyPrefix){
        LocalDateTime now=LocalDateTime.now();
        long nowstrap = now.toEpochSecond(ZoneOffset.UTC);
        long timestrap=nowstrap-BEGIN_TIMESTRAP;
        String date=now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        String key=keyPrefix+date;
        long count = redisTemplate.opsForValue().increment(key);
        return timestrap<<32|count;
    }
}
