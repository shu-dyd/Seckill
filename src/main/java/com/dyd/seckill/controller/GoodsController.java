package com.dyd.seckill.controller;

import com.dyd.seckill.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @RequestMapping("/toList")
    public String toList(HttpSession session, Model model, @CookieValue("userTicket") String ticket){
        if(StringUtils.isEmpty(ticket)){
            // 说明没有登陆过
            return "login";
        }
        User user = (User) session.getAttribute(ticket);
        if(user == null){
            return "login";
        }
        model.addAttribute("user", user);
        return "goodsList";
    }
}
