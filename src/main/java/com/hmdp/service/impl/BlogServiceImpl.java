package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import static cn.hutool.core.util.NumberUtil.min;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
    @Autowired
    IUserService userService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Override
    public List<Blog> queryall(Integer current) {
        // 根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(this::getuserdto);
        return records;
    }

    private void getuserdto(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
        blog.setIsLike(isLike(blog.getId(),UserHolder.getUser()));
    }

    @Override
    public Blog queryone(Integer current) {
        Blog blog = getById(current);
        getuserdto(blog);
        return blog;
    }

    @Override
    public void like(Long id) {
        UserDTO user = UserHolder.getUser();
        if(user==null) return;
        boolean like = isLike(id, user);
        if(like){
            update()
                    .setSql("liked = liked - 1").eq("id", id).update();
            redisTemplate.opsForZSet().remove("isLike2:" + id,UserHolder.getUser().getId().toString());
            //redisTemplate.opsForSet().remove("isLike:" + id,UserHolder.getUser().getId().toString());
        }else {
            update()
                    .setSql("liked = liked + 1").eq("id", id).update();
            redisTemplate.opsForZSet().add("isLike2:" + id,UserHolder.getUser().getId().toString(),
                    System.currentTimeMillis());
            //redisTemplate.opsForSet().add("isLike:" + id,UserHolder.getUser().getId().toString());
        }

    }

    @Override
    public List<UserDTO> likes(Long id) {
        Set<String> users = redisTemplate.opsForZSet().range("isLike2:" + id, 0, 4);
        List<UserDTO> res=new ArrayList<>();
        Iterator<String> iterator = users.iterator();
        int shu=0;
        while (iterator.hasNext()){
            shu++;
            String next = iterator.next();
            User user = userService.getById(Long.parseLong(next));
            UserDTO userDTO=new UserDTO();
            userDTO.setIcon(user.getIcon());
            userDTO.setId(user.getId());
            userDTO.setNickName(user.getNickName());
            res.add(userDTO);
            if(shu==5) break;
        }
        return res;
    }
    @Autowired
    IFollowService followService;
    @Override
    public void saveblog(Blog blog) {
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        List<Follow> follows = followService.query().eq("follow_user_id", user.getId()).list();
        save(blog);
        for (Follow follow : follows) {
            Long userId = follow.getUserId();
            String key="feed:"+userId;
            redisTemplate.opsForZSet().add(key,blog.getId().toString(),System.currentTimeMillis());
        }
    }

    private boolean isLike(Long id, UserDTO user) {
        if(user==null) return false;
        Double score = redisTemplate.opsForZSet().score("isLike2:" + id,user.getId().toString());
        if(score!=null)
        return true;
        return false;
    }

}









