package com.hmdp.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // TODO 发送短信验证码并保存验证码
        return userService.sendcode(phone,session);
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        // TODO 实现登录功能
        return userService.login(loginForm,session);
    }
    @RequestMapping("/loginall")
    public Result loginall(){
        // TODO 实现登录功能
        List<User> list = userService.list();

        File file = new File("D:\\ziliao\\黑马redis\\Redis-笔记资料\\02-实战篇\\资料\\tokens.txt");
        StringBuffer sb=new StringBuffer();
        for (User user : list) {
            sendCode(user.getPhone(),null);
            LoginFormDTO dto=new LoginFormDTO();
            dto.setCode("000000");
            dto.setPhone(user.getPhone());
            Result res = userService.login(dto, null);
            String data = (String) res.getData();
            sb.append(data+"\n");
        }
        FileUtil.writeString(sb.toString(),
                file,"UTF-8");
        return Result.ok("ok");
    }
    /**
     * 登出功能
     * @return 无
     */
    @PostMapping("/logout")
    public Result logout(){
        // TODO 实现登出功能
        return Result.fail("功能未完成");
    }

    @GetMapping("/me")
    public Result me(){
        // TODO 获取当前登录的用户并返回
        UserDTO u= UserHolder.getUser();
        return Result.ok(u);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

// UserController 根据id查询用户

    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.ok(userDTO);
    }
    @Autowired
    StringRedisTemplate redisTemplate;
    @RequestMapping("sign")
    public Result sign(){
        LocalDateTime now = LocalDateTime.now();
        int dayOfMonth = now.getDayOfMonth();
        String key=now.getYear()+":"+now.getMonth()+":"+UserHolder.getUser().getId()+":";
        redisTemplate.opsForValue().setBit(key,dayOfMonth,true);
        return Result.ok(dayOfMonth);
    }
    @RequestMapping("signnum")
    public Result signnum(){
        LocalDateTime now = LocalDateTime.now();
        int dayOfMonth = now.getDayOfMonth();
        String key=now.getYear()+":"+now.getMonth()+":"+UserHolder.getUser().getId()+":";
        List<Long> longs = redisTemplate.opsForValue().bitField(key,
                BitFieldSubCommands.create().get(
                        BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth+1)).valueAt(0));
        if(longs.isEmpty()) return Result.ok("空");
        Long aLong = longs.get(0);
        int num=0;
        for(int i=0;i<64;i++) {
            if((aLong&1)>0) num++;
            else {
                break;
            }
            aLong>>=1;
        }
        return Result.ok(num);
    }

}
