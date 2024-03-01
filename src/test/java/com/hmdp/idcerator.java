package com.hmdp;


import cn.hutool.core.io.FileUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@SpringBootTest
@Component
public class idcerator {

    static final long BEGIN_TIMESTRAP=56131212L;
    
    @Autowired
    StringRedisTemplate redisTemplate;
    @Test
    public void main() {
        File file = new File("D:\\ziliao\\黑马redis\\Redis-笔记资料\\02-实战篇\\资料\\tokens.txt");
        StringBuffer sb=new StringBuffer();
        for(int i=1;i<5;i++){
            sb.append("data\n");
        }
        FileUtil.writeString(sb.toString(),file,"UTF-8");
    }

    long nextid(String keyPrefix){
        LocalDateTime now=LocalDateTime.now();
        long nowstrap = now.toEpochSecond(ZoneOffset.UTC);
        long timestrap=nowstrap-BEGIN_TIMESTRAP;
        String date=now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        String key=keyPrefix+date;
        long count = redisTemplate.opsForValue().increment(key);
        return timestrap<<32|count;
    }
    @Test
    public void ssssssssss() {
        Integer a = 1;
        Integer b = 2;
        Integer c = null;
        Boolean flag = false;
        Integer result = flag ? a * b : c;
        System.out.println(result);

    }
}
