package com.dyd.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dyd.seckill.mapper.SeckillOrderMapper;
import com.dyd.seckill.pojo.SeckillOrder;
import com.dyd.seckill.pojo.User;
import com.dyd.seckill.service.ISeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dyd
 * @since 2022-08-19
 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements ISeckillOrderService {

    @Resource
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * orderId:成功；-1：秒杀失败；0：排队中
     *
     * @param user
     * @param goodsId
     * @return
     */
    @Override
    public Long getResult(User user, Long goodsId) {
        SeckillOrder seckillOrder = seckillOrderMapper.selectOne(new QueryWrapper<SeckillOrder>().eq("user_id", user.getId()).
                eq("goods_id", goodsId));

        if(seckillOrder != null){
            // 找到了秒杀订单，说明秒杀成功直接返回订单Id
            return seckillOrder.getOrderId();
        }else if(redisTemplate.hasKey("isStockEmpty:" + goodsId)){
            // 没库存，秒杀失败
            return -1L;
        }else{
            // 排队中
            return 0L;
        }
    }
}
