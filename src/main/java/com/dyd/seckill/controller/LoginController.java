package com.dyd.seckill.controller;

import com.dyd.seckill.service.IUserService;
import com.dyd.seckill.vo.LoginVo;
import com.dyd.seckill.vo.RespBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller()
@RequestMapping("/login")
@Slf4j
public class LoginController {

    @Autowired
    private IUserService userService;

    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }

    @ResponseBody
    @RequestMapping("/doLogin")
    public RespBean doLogin(LoginVo loginVo){
        return userService.doLogin(loginVo);
    }

}
