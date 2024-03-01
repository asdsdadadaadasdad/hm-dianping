package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Override
    public void follow(Long followId, Boolean isFollow) {
        if(isFollow){
            Follow follow=new Follow();
            follow.setUserId(UserHolder.getUser().getId());
            follow.setFollowUserId(followId);
            boolean issave = save(follow);
            if(issave){
                redisTemplate.opsForSet().add("follow:"+UserHolder.getUser().getId(),followId.toString());
            }

        }else {
            boolean isrem = remove(new QueryWrapper<Follow>()
                    .eq("user_id", UserHolder.getUser().getId())
                    .eq("follow_user_id", followId));
            if(isrem){
                redisTemplate.opsForSet().remove("follow:"+UserHolder.getUser().getId(),followId.toString());
            }
        }
    }

    @Override
    public Boolean isfollow(Long followId) {
        Integer count = query().eq("user_id", UserHolder.getUser().getId())
                .eq("follow_user_id", followId).count();
        if(count>0) return true;
        return false;
    }
    @Autowired
    IUserService userService;
    @Override
    public List<UserDTO> commonfollow(Long followId) {
        Long id = UserHolder.getUser().getId();
        Set<String> set = redisTemplate.opsForSet().intersect("follow:" + id, "follow:" + followId);
        if (set.isEmpty()) return Collections.emptyList();
        List<Long> ids=set.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> users = userService.listByIds(ids).stream()
                .map(u -> BeanUtil.copyProperties(u, UserDTO.class))
                .collect(Collectors.toList());
        return users;
    }
    @Autowired
    IBlogService blogService;
    @Override
    public ScrollResult getgz(Long lastid, Integer offset) {
        UserDTO user = UserHolder.getUser();
        String key="feed:"+user.getId();
        Set<ZSetOperations.TypedTuple<String>> set =
                redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 0, lastid, offset, 2);
        if(set.isEmpty()) return null;
        int cishu=1;
        long lid = 0;
        List<Long> ids=new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : set) {
            ids.add(Long.parseLong(tuple.getValue()));
            long l = tuple.getScore().longValue();
            if(lid==l){
                cishu++;
            }else {
                cishu=1;
                lid=l;
            }
        }
        String idstr = StrUtil.join(",",ids);
        //System.out.println("(ORDER BY FIELD (id,"+idstr+")");
        List<Blog> blogs = blogService.query().in("id",ids)
                .last("ORDER BY FIELD (id,"+idstr+")").list();
        ScrollResult result=new ScrollResult();
        result.setList(blogs);
        result.setMinTime(lid);
        result.setOffset(cishu);
        return result;
    }

}
