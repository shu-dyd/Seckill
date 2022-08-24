package com.dyd.seckill.controller;


import com.dyd.seckill.pojo.User;
import com.dyd.seckill.vo.RespBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dyd
 * @since 2022-08-16
 */
@RestController
@RequestMapping("/user")
public class UserController{
    @RequestMapping("/info")
    @ResponseBody
    public RespBean info(User user){
        System.out.println("yyyyy");
        return RespBean.success(user);
    }
}
