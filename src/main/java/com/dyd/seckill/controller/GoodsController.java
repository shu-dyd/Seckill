package com.dyd.seckill.controller;

import com.dyd.seckill.pojo.User;
import com.dyd.seckill.service.IGoodsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;

//    // 原本的方式
//    @RequestMapping("/toList")
//    public String toList(HttpSession session, Model model, @CookieValue("userTicket") String ticket){
//        if(StringUtils.isEmpty(ticket)){
//            // 说明没有登陆过
//            return "login";
//        }
//        User user = (User) session.getAttribute(ticket);
//        if(user == null){
//            return "login";
//        }
//        model.addAttribute("user", user);
//        return "goodsList";
//    }

    // 改进之后，在WebMvcConfigurer中加入ArgumentResolvers
    @RequestMapping("/toList")
    public String toList(Model model, User user){
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        return "goodsList";
    }

    @RequestMapping("/toDetail/{goodsId}")
    public String toDetail(Model model, User user, @PathVariable Long goodsId){
        model.addAttribute("user", user);
        model.addAttribute("goods", goodsService.findGoodsVoByGoodsId(goodsId));
        return "goodsDetail";
    }

}
