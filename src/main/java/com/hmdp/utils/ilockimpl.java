package com.hmdp.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class ilockimpl implements ilock{

    String name;
    StringRedisTemplate redisTemplate;
    final static String KEY_PREFIX="lock:";
    String ID=UUID.randomUUID().toString()+"_"+Thread.currentThread().getId();
    public ilockimpl(String name, StringRedisTemplate redisTemplate){
        this.name=name;
        this.redisTemplate=redisTemplate;
    }
    @Override
    public boolean trylock() {
        return redisTemplate.opsForValue().setIfAbsent(KEY_PREFIX+name,ID,60, TimeUnit.SECONDS);
    }
    static final RedisScript<Long> unlockScript;
    static{
        unlockScript=new DefaultRedisScript<>();
        ((DefaultRedisScript<Long>) unlockScript).setResultType(Long.class);
        ((DefaultRedisScript<Long>) unlockScript).setLocation(new ClassPathResource("unlock.lua"));
    }
    @Override
    public void unlock() {
        redisTemplate.execute(unlockScript, Collections.singletonList(KEY_PREFIX+name),ID);
    }
}
