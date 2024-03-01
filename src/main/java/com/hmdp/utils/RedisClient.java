package com.hmdp.utils;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.entity.RedisCache;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class RedisClient {
    final ExecutorService CACHE_EXEC= Executors.newFixedThreadPool(8);
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    public void set(String key, Object value,Long time, TimeUnit unit){
        String json = JSONUtil.toJsonStr(value);
        stringRedisTemplate.opsForValue().set(key,json,time,unit);
    }
    public void set(String key, Object value){
        String json = JSONUtil.toJsonStr(value);
        stringRedisTemplate.opsForValue().set(key,json);
    }
    public void setwithexpire(String key, Object value, Long time, TimeUnit unit){
        RedisCache cache=new RedisCache();
        cache.setRedisdata(value);
        cache.setLocalDateTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        String json=JSONUtil.toJsonStr(cache);
        stringRedisTemplate.opsForValue().set(key,json);
    }
    public <R,ID> R querywithexpire(String key, Class<R> type,ID id,
                             Function<ID,R> queryfromdb,Long t, TimeUnit unit){
        String json = stringRedisTemplate.opsForValue().get(key);
        if(!StrUtil.isNotBlank(json)) return null;
        RedisCache cache = JSONUtil.toBean(json, RedisCache.class);
        R data = JSONUtil.toBean((JSONObject) cache.getRedisdata(),type);
        LocalDateTime time=cache.getLocalDateTime();
        if(time.isAfter(LocalDateTime.now())){
            return data;
        }
        boolean islock=trylock(key);
        if(islock){
            CACHE_EXEC.submit(()->{
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                R res= queryfromdb.apply(id);
                cache.setRedisdata(res);
                setwithexpire(key,res, Long.valueOf(20),TimeUnit.SECONDS);
                unlock(key);
            });
        }
        return data;
    }
    public <R,ID> R querypassthrough(String key,Class<R> type,ID id,
                              Function<ID,R> queryfromdb,Long t,TimeUnit unit){
        do {
            String json = stringRedisTemplate.opsForValue().get(key);
            if(StrUtil.isNotBlank(json)){
                return JSONUtil.toBean(json,type);
            }
            if(json!=null) return null;
            boolean islock=trylock(key);
            if(islock){
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while (true);
        R res=queryfromdb.apply(id);
        if(res==null){
            set(key,"",t,unit);
        }else {
            set(key,res,t,unit);
        }
        unlock(key);
        return res;
    }
    private void unlock(String key) {
        String lockkey="lock:"+key;
        stringRedisTemplate.delete(lockkey);
    }

    private boolean trylock(String key) {
        String lockkey="lock:"+key;
        return stringRedisTemplate.opsForValue().setIfAbsent(lockkey,"1");
    }

}










