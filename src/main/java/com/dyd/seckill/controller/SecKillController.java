package com.dyd.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dyd.seckill.pojo.Order;
import com.dyd.seckill.pojo.SeckillOrder;
import com.dyd.seckill.pojo.User;
import com.dyd.seckill.service.IGoodsService;
import com.dyd.seckill.service.IOrderService;
import com.dyd.seckill.service.ISeckillOrderService;
import com.dyd.seckill.vo.GoodsVo;
import com.dyd.seckill.vo.RespBean;
import com.dyd.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/seckill")
public class SecKillController {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;

    /**
     * windows优化前QPS：94.2
     *      redis集群QPS：203.9
     *      加入缓存QPS：165.6
     * linux优化前QPS：71.3
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    // 2.0版本，秒杀页面静态化
    @RequestMapping(value = "/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(Model model, User user, Long goodsId){
        System.out.println("^^^^^^^^^^^^^^^^^^");
        if(user==null){
            System.out.println("null");
        }else{
            System.out.println(user.getId());
        }

        System.out.println("~~~~~~~~~~~~~~~~~~");
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        // 判断库存
        if(goodsVo.getStockCount()<1){
            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 判断是否重复抢购
        // 若实际查询结果多余一条，则返回too many错误
        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
        if(seckillOrder != null){
            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        Order order = orderService.seckill(user, goodsVo);
        return RespBean.success(order);
    }


//    /**
//     * windows优化前QPS：94.2
//     *      redis集群QPS：203.9
//     *      加入缓存QPS：165.6
//     * linux优化前QPS：71.3
//     * @param model
//     * @param user
//     * @param goodsId
//     * @return
//     */
//    // 1.0版本
//    @RequestMapping("/doSeckill")
//    public String doSeckill(Model model, User user, Long goodsId) {
//        System.out.println("^^^^^^^^^^^^^^^^^^");
//        if (user == null) {
//            System.out.println("null");
//        } else {
//            System.out.println(user.getId());
//        }
//
//        System.out.println("~~~~~~~~~~~~~~~~~~");
//        if (user == null) {
//            return "login";
//        }
//        model.addAttribute("user", user);
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//        // 判断库存
//        if (goodsVo.getStockCount() < 1) {
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return "secKillFail";
//        }
//        // 判断是否重复抢购
//        // 若实际查询结果多余一条，则返回too many错误
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
//        if (seckillOrder != null) {
//            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
//            return "secKillFail";
//        }
//        Order order = orderService.seckill(user, goodsVo);
//        model.addAttribute("order", order);
//        model.addAttribute("goods", goodsVo);
//        return "orderDetail";
//    }

}
