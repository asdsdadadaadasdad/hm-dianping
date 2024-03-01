package com.hmdp.service;

import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
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
public interface IBlogService extends IService<Blog> {

    List<Blog> queryall(Integer current);

    Blog queryone(Integer current);

    void like(Long id);

    List<UserDTO> likes(Long id);

    void saveblog(Blog blog);
}
