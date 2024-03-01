package com.hmdp.config;

import com.hmdp.utils.freshince;
import com.hmdp.utils.loginhandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    StringRedisTemplate template;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new loginhandle()).excludePathPatterns(
                "/user/login",
                "/user/code",
                "/user/loginall",
                "/shop/**",
                "/voucher/**",
                "/shop-type/**",
                "/upload/**",
                "/blog/hot"
        ).order(1);
        registry.addInterceptor(new freshince(template)).order(0);
    }
}
