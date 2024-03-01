package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.IFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    IFollowService iFollowService;
    @RequestMapping("/{id}/{isfollow}")
    Result followgn(@PathVariable("id") Long followId,@PathVariable("isfollow") Boolean isFollow){
        iFollowService.follow(followId,isFollow);
        return Result.ok("没毛");
    }
    @RequestMapping("/or/not/{id}")
    Result followgn(@PathVariable("id") Long followId){
        return Result.ok(iFollowService.isfollow(followId));
    }
    @RequestMapping("/common/{id}")
    Result followcommon(@PathVariable("id") Long followId){
        return Result.ok(iFollowService.commonfollow(followId));
    }

}
