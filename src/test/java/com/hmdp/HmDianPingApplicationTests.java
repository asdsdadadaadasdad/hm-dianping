package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@SpringBootTest
public class HmDianPingApplicationTests {
    static final long BEGIN_TIMESTRAP=56131212L;

    @Autowired
    StringRedisTemplate redisTemplate;
    @Test
    public void main() {
        for(int i=0;i<10;i++){
            System.out.println(nextid("order"));
        }
    }

    public long nextid(String keyPrefix){
        LocalDateTime now=LocalDateTime.now();
        long nowstrap = now.toEpochSecond(ZoneOffset.UTC);
        long timestrap=nowstrap-BEGIN_TIMESTRAP;
        String date=now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        String key=keyPrefix+date;
        long count = redisTemplate.opsForValue().increment(key);
        System.out.println("niya:"+(timestrap<<32));
        return timestrap<<32|count;
    }
    @Autowired
    IShopService shopService;
    @Test
    public void addshopgeo(){
        List<Shop> shops = shopService.list();
        Map<Long, List<Shop>> collect = shops.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        for (Map.Entry<Long, List<Shop>> entry : collect.entrySet()) {
            String key="shopgeo:"+entry.getKey();
            Map<String, Point> locations=new HashMap<>();
            for (Shop shop : entry.getValue()) {
                locations.put(shop.getId().toString(),new Point(shop.getX(),shop.getY()));
            }
            redisTemplate.opsForGeo().add(key,locations);
        }
    }
    @Test
    public void hyperlog(){
        String[] list=new String[1000];
        for(int i=0;i<1000;i++){
            for(int j=0;j<1000;j++){
                list[j]="user"+j+i*1000;
            }
            redisTemplate.opsForHyperLogLog().add("sdnp6667",list);
        }
        Long sdnp666 = redisTemplate.opsForHyperLogLog().size("sdnp6667");
        System.out.println(sdnp666);
        Long sdnp6667 = redisTemplate.opsForHyperLogLog().union("sdnp6667","sdnp666");
        System.out.println(sdnp6667);
    }

}
