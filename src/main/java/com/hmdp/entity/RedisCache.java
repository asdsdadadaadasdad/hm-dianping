package com.hmdp.entity;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class RedisCache {
    LocalDateTime localDateTime;
    Object redisdata;
}
