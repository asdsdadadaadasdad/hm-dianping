package com.hmdp.service;

import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IFollowService extends IService<Follow> {

    void follow(Long followId, Boolean isFollow);

    Boolean isfollow(Long followId);

    List<UserDTO> commonfollow(Long followId);

    ScrollResult getgz(Long lastid, Integer offset);
}
