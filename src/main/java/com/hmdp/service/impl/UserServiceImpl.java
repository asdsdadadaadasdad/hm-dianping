package com.hmdp.service.impl;
import cn.hutool.core.bean.copier.CopyOptions;
import com.hmdp.utils.cl ;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;

import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.cl.USER_PERFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    StringRedisTemplate template;
    public Result sendcode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("格式错误666");
        }
        String numbers = RandomUtil.randomNumbers(6);
        template.opsForValue().set("yzm:"+phone,"000000",2, TimeUnit.MINUTES);
        System.out.println("验证码是"+numbers);
        return Result.ok();
    }


    public Result login(LoginFormDTO loginForm, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(loginForm.getPhone())) {
            return Result.fail("格式错误666");
        }
        if(RegexUtils.isCodeInvalid(loginForm.getCode())){
            return Result.fail("格式错误666");
        }
        Object yzm = template.opsForValue().get("yzm:"+loginForm.getPhone());
        if(yzm==null||!yzm.equals(loginForm.getCode())) return Result.fail("666");
        User user = query().eq("phone", loginForm.getPhone()).one();
        if(user==null){
            user=createuserbyphone(loginForm.getPhone());
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> map = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create().setFieldValueEditor((k,v)->v.toString()));
        String token = UUID.randomUUID().toString();

        template.opsForHash().putAll(USER_PERFIX+token,map);
        template.expire(USER_PERFIX+token,1, TimeUnit.HOURS);
        return Result.ok(token);
    }

    private User createuserbyphone(String phone) {
        System.out.println("创建");
        User u=new User();
        u.setPhone(phone);
        u.setNickName("name"+RandomUtil.randomNumbers(6));
        save(u);
        return u;
    }
}
