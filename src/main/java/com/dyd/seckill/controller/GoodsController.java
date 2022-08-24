package com.dyd.seckill.controller;

import com.dyd.seckill.pojo.User;
import com.dyd.seckill.service.IGoodsService;
import com.dyd.seckill.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ThymeleafViewResolver thymeleafViewResolver;

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
    // 加入页面缓存
    @RequestMapping(value = "/toList",produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toList(Model model, User user, HttpServletRequest request, HttpServletResponse response){
        // Redis中获取页面，如果不为空，直接返回页面
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsList");
        if (!StringUtils.isEmpty(html)){
            return html;
        }
        model.addAttribute("user", user);
        model.addAttribute("goodsList", goodsService.findGoodsVo());
        // 如果为空，手动渲染，存入Redis并返回
        WebContext webContext = new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        // 注意这里的s要和thymeleaf模板页面的名称相同
        html = thymeleafViewResolver.getTemplateEngine().process("goodsList", webContext);
        if (!StringUtils.isEmpty(html)){
            valueOperations.set("goodsList", html, 1, TimeUnit.MINUTES);
        }
        return html;
    }

    // 改进加入URL缓存
    @RequestMapping(value = "/toDetail/{goodsId}", produces = "text/html;charset=utf-8")
    @ResponseBody
    public String toDetail(Model model, User user, @PathVariable Long goodsId, HttpServletRequest request, HttpServletResponse response){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        String html = (String) valueOperations.get("goodsDetails:" + goodsId);
        if(!StringUtils.isEmpty(html)){
            return html;
        }
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
        WebContext webContext = new WebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goodsDetail", webContext);
        if(!StringUtils.isEmpty(html)){
            valueOperations.set("goodsDetails:"+goodsId, html, 1, TimeUnit.MINUTES);
        }
        return html;
    }

}
