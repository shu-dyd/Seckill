package com.dyd.seckill.controller;

import com.dyd.seckill.pojo.User;
import com.dyd.seckill.service.IGoodsService;
import com.dyd.seckill.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.time.LocalDateTime;

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
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        LocalDateTime startDate = goodsVo.getStartDate();
        LocalDateTime endDate = goodsVo.getEndDate();
        LocalDateTime nowDate = LocalDateTime.now();
        int secKillStatus = 0;
        // 秒杀开始倒计时
        int remainSeconds = 0;
        if(nowDate.isBefore(startDate)){
            Duration duration = Duration.between(startDate, endDate);
            long millis = duration.toMillis() / 1000;
            remainSeconds = (int) millis;
        }else if(nowDate.isAfter(endDate)){
            secKillStatus = 2;
            // 秒杀已经结束
            remainSeconds = -1;
        }else{
            // 秒杀中
            secKillStatus = 0;
        }
        model.addAttribute("seckillStatus",secKillStatus);
        model.addAttribute("goods", goodsVo);
        model.addAttribute("remainSeconds", remainSeconds);
        return "goodsDetail";
    }

}
