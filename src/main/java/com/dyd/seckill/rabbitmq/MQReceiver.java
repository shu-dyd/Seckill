package com.dyd.seckill.rabbitmq;

import com.dyd.seckill.pojo.SeckillMessage;
import com.dyd.seckill.pojo.SeckillOrder;
import com.dyd.seckill.pojo.User;
import com.dyd.seckill.service.IGoodsService;
import com.dyd.seckill.service.IOrderService;
import com.dyd.seckill.utils.JsonUtil;
import com.dyd.seckill.vo.GoodsVo;
import com.dyd.seckill.vo.RespBean;
import com.dyd.seckill.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQReceiver {

    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IOrderService orderService;

    @RabbitListener(queues = "seckillQueue")
    public void receive(String message){
        log.info("接收到的消息："+message);
        SeckillMessage seckillMessage = JsonUtil.jsonStr2Object(message, SeckillMessage.class);
        Long goodsId = seckillMessage.getGoodsId();
        User user = seckillMessage.getUser();
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        if (goodsVo.getStockCount()<1){
            return;
        }
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.opsForValue().get("order:" + user.getId() + ":" + goodsId);
        if(seckillOrder != null){
            return;
        }
        // 下单
        orderService.seckill(user, goodsVo);
    }
}
