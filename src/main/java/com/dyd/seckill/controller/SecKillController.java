package com.dyd.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dyd.seckill.config.AccessLimit;
import com.dyd.seckill.exception.GlobalException;
import com.dyd.seckill.pojo.Order;
import com.dyd.seckill.pojo.SeckillMessage;
import com.dyd.seckill.pojo.SeckillOrder;
import com.dyd.seckill.pojo.User;
import com.dyd.seckill.rabbitmq.MQSender;
import com.dyd.seckill.service.IGoodsService;
import com.dyd.seckill.service.IOrderService;
import com.dyd.seckill.service.ISeckillOrderService;
import com.dyd.seckill.utils.JsonUtil;
import com.dyd.seckill.vo.GoodsVo;
import com.dyd.seckill.vo.RespBean;
import com.dyd.seckill.vo.RespBeanEnum;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping("/seckill")
public class SecKillController implements InitializingBean {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private MQSender mqSender;
    // 通过内存进行标记，当redis预减库存已经为0了，就不要在去访问了
    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    /**
     * windows优化前QPS：94.2
     *      redis集群QPS：203.9
     *      加入缓存QPS：165.6
     *      加入静态页面缓存，加入redis缓存秒杀订单QPS：469.2
     *      加入RabbitMQ,QPS:257.8
     * linux优化前QPS：71.3
     * @param user
     * @param goodsId
     * @return
     */
    // 3.0版本，redis预减库存。不用频繁访问mysql查库存
    @RequestMapping(value = "/{path}/doSeckill", method = RequestMethod.POST)
    @ResponseBody
    public RespBean doSeckill(@PathVariable String path, User user, Long goodsId){
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
        ValueOperations valueOperations = redisTemplate.opsForValue();

        // 之前根据用户和商品id获取随机的路径的时候,已经将其保存在了redis中
        // 在这里做一个校验
        boolean check = orderService.checkPath(user, goodsId, path);
        if(!check){
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }


        // 当然依旧保留2.0版本中的通过reids来保存秒杀订单的策略。
        // 这样查询用户是否秒杀的话不用访问mysql。
        // 判断是否重复抢购，并不完全安全，如果一个用户同时多条请求过来，还是有可能会进入到秒杀的服务中。
        // 所以在service层还是要进行优化
        // 在秒杀的seckill()中，除了到mysql中添加秒杀成功记录，还可以在redis中添加秒杀成功记录，便于查询是否已经秒杀
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrder != null){
            System.out.println("已经存在秒杀订单");
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }

        // 如果内存标记已经没库存了，就不要再去访问redis了
        if(EmptyStockMap.get(goodsId)){
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        // 利用提前加载到redis中的商品库存信息来进行判断
        Long stock = valueOperations.decrement("seckillGoods:" + goodsId);
        if(stock < 0){
            EmptyStockMap.put(goodsId, true);
            // 将-1的库存数量恢复回0
            valueOperations.increment("seckillGoods:" + goodsId);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }

        SeckillMessage seckillMessage = new SeckillMessage(user, goodsId);
        mqSender.sendSeckillMessage(JsonUtil.object2JsonStr(seckillMessage));
        return RespBean.success(0);

    }

    /**
     * 获取秒杀结果
     * orderId:成功；-1：秒杀失败；0：排队中
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getResult(User user, Long goodsId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    /**
     * 获取实际秒杀地址
     * @param user
     * @param goodsId
     * @return
     */
    @AccessLimit(second=5, maxCount=5, needLogin=true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public RespBean getPath(User user, Long goodsId, String captcha){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if(!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    public void verifyCode(User user, Long goodsId, HttpServletResponse response){
        if(user==null||goodsId<0){
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        //设置请求头为输出图片的类型
        response.setContentType("image/jpg");
        response.setHeader("Pargam", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        //生成验证码，将结果放入Redis
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 32, 3);
        redisTemplate.opsForValue().set("captcha:"+user.getId()+":"+goodsId, captcha.text(), 300, TimeUnit.SECONDS);
        try {
            captcha.out(response.getOutputStream());
        } catch (IOException e) {
            log.error("验证码生成失败", e.getMessage());
        }
    }


    /**
     * 3.0 用于redis预减库存时，提前加载库存信息到redis。
     * 这个方法应该很熟悉，之前看dubbo源码时也有所接触。在bean的生命周期中，
     * 实例化，生成对象，属性填充后会进行该方法的执行
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:"+goodsVo.getId(),goodsVo.getStockCount());
            EmptyStockMap.put(goodsVo.getId(), false);
        });

    }

    //    /**
//     * windows优化前QPS：94.2
//     *      redis集群QPS：203.9
//     *      加入缓存QPS：165.6
//     *      加入静态页面缓存，加入redis缓存秒杀订单QPS：469.2
//     * linux优化前QPS：71.3
//     * @param model
//     * @param user
//     * @param goodsId
//     * @return
//     */
//    // 2.0版本，秒杀页面静态化
//    @RequestMapping(value = "/doSeckill", method = RequestMethod.POST)
//    @ResponseBody
//    public RespBean doSeckill(Model model, User user, Long goodsId){
//        System.out.println("^^^^^^^^^^^^^^^^^^");
//        if(user==null){
//            System.out.println("null");
//        }else{
//            System.out.println(user.getId());
//        }
//
//        System.out.println("~~~~~~~~~~~~~~~~~~");
//        if(user==null){
//            return RespBean.error(RespBeanEnum.SESSION_ERROR);
//        }
//        model.addAttribute("user", user);
//        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
//        // 判断库存
//        if(goodsVo.getStockCount()<1){
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
//        }
//        // 判断是否重复抢购，并不完全安全，如果一个用户同时多条请求过来，还是有可能会进入到秒杀的服务中。
//        // 所以在service层还是要进行优化
//        // 若实际查询结果多余一条，则返回too many错误
//        // SeckillOrder seckillOrder = seckillOrderService.getOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).eq("goods_id", goodsId));
//        // 在秒杀的seckill()中，除了到mysql中添加秒杀成功记录，还可以在redis中添加秒杀成功记录，便于查询是否已经秒杀
//        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
//        if(seckillOrder != null){
//            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
//            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
//        }
//        Order order = orderService.seckill(user, goodsVo);
//        return RespBean.success(order);
//    }


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
