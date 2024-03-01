package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.RedisCache;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisClient;
import com.hmdp.utils.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hmdp.utils.cl.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedisClient redisClient;
    @Override
    public Shop querybyid(Long id) {
        String key=CACHE_PERFIX+id;
        return redisClient.querypassthrough(key,Shop.class,id,this::getById, 60L,TimeUnit.SECONDS);
    }



    @Override
    public Result querybytype(Integer typeId, Integer current, Double x, Double y) {
        if(x==null||y==null){
            String key=CACHE_TYPE+typeId+"sd"+current;
            String typeJson = redisTemplate.opsForValue().get(key);
            List<Shop> records;
            if(StrUtil.isNotBlank(typeJson)) {
                records= JSONUtil.toList(typeJson,Shop.class);
                return Result.ok(records);
            }
            // 根据类型分页查询
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            // 返回数据
            records = page.getRecords();
            redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(records));
            redisTemplate.expire(key,2, TimeUnit.MINUTES);
            return Result.ok(records);
        }
        int from=(current-1)*5;
        int end=current*5;
        String key="shopgeo:"+typeId;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.
                opsForGeo().search(key, GeoReference.fromCoordinate(x, y),
                new Distance(5000),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
        );
        if(results==null) return Result.ok();
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = results.getContent();
        if(content.size()<=from) return Result.ok();
        Map<Long,Distance> distanceMap=new HashMap<>();
        content = content.stream().skip(from).collect(Collectors.toList());
        content.forEach(r->
            distanceMap.put(Long.parseLong(r.getContent().getName()),r.getDistance())
        );
        List<Long> ids=content.stream().map(r->Long.parseLong(r.getContent().getName())).collect(Collectors.toList());

        String idstr=StrUtil.join(",",ids);
        System.out.println("ORDER BY FIELD (id,"+idstr+")");
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD (id,"+idstr+")").list();
        for (Shop shop : shops) {
            shop.setDistance(distanceMap.get(shop.getId()).getValue());
        }
        return Result.ok(shops);
    }

    @Override
    public Result update(Shop shop) {
        Long id = shop.getId();
        updateById(shop);
        redisTemplate.delete(CACHE_PERFIX+id);
        return Result.ok();
    }
    static final ExecutorService CACHE_EXEC= Executors.newFixedThreadPool(8);
    @Override
    public Shop querybyid2(Long id) {
        String key=CACHE_SHOP+id;
        return redisClient.querywithexpire(key,Shop.class,id,this::getById, 20L,TimeUnit.SECONDS);
    }

    private void saveshop(String key,RedisCache value,int time) {
        value.setLocalDateTime(LocalDateTime.now().plusSeconds(time));
        redisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(value));
    }
    public void save6(Long id){
        Shop shop = getById(id);
        RedisCache cache=new RedisCache();
        cache.setRedisdata(shop);
        cache.setLocalDateTime(LocalDateTime.now());
        String key=CACHE_SHOP+id;
        saveshop(key,cache,10);
    }


}











