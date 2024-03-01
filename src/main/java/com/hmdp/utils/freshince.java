package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.cl.USER_PERFIX;
@Mapper
public class freshince implements HandlerInterceptor {
    @Autowired
    StringRedisTemplate template;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("authorization");
        Map<Object, Object> map = template.opsForHash().entries(USER_PERFIX+token);
        if(!map.isEmpty()){
            template.expire(USER_PERFIX+token,1, TimeUnit.HOURS);
            UserDTO user = BeanUtil.fillBeanWithMap(map, new UserDTO(), false);
            UserHolder.saveUser(user);
        }
        return true;
    }
    public freshince(StringRedisTemplate template){
        this.template=template;
    }

}
