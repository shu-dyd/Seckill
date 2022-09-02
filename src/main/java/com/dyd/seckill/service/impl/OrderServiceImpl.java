package com.dyd.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dyd.seckill.exception.GlobalException;
import com.dyd.seckill.mapper.OrderMapper;
import com.dyd.seckill.pojo.Order;
import com.dyd.seckill.pojo.SeckillGoods;
import com.dyd.seckill.pojo.SeckillOrder;
import com.dyd.seckill.pojo.User;
import com.dyd.seckill.service.IGoodsService;
import com.dyd.seckill.service.IOrderService;
import com.dyd.seckill.service.ISeckillGoodsService;
import com.dyd.seckill.service.ISeckillOrderService;
import com.dyd.seckill.vo.GoodsVo;
import com.dyd.seckill.vo.OrderDetailVo;
import com.dyd.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现
 * </p>
 *
 * @author dyd
 * @since 2022-08-19
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    @Autowired
    private ISeckillGoodsService seckillGoodsService;
    @Resource
    private OrderMapper orderMapper;
    @Autowired
    private ISeckillOrderService seckillOrderService;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Transactional
    @Override
    public Order seckill(User user, GoodsVo goodsVo) {

        SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().eq("goods_id", goodsVo.getId()));
        seckillGoods.setStockCount(seckillGoods.getStockCount()-1);

        // 解决超卖问题,减库存的时候做一个判断
        // seckillGoodsService.updateById(seckillGoods);
        // 行级锁
        boolean result = seckillGoodsService.update(new UpdateWrapper<SeckillGoods>().setSql(
                "stock_count = " + "stock_count-1").eq("goods_id", goodsVo.getId()).gt("stock_count", 0));
        if(!result){
            // 如果没有去库存减一，就不执行去创建订单了
            return null;
        }

        //解决同一用户多次下单问题，mysql中用用户id和商品id设置秒杀订单表唯一索引
        //当重复下单的时候，秒杀订单表进行数据添加操作的时候会出现异常
        //然后Transactional注解，会进行事务的回滚，估计是包含库存修改全部回滚了，保证了不会有重复下单
        //生成订单
        Order order = new Order();
        order.setUserId(user.getId());
        order.setGoodsId(goodsVo.getId());
        order.setDeliveryAddrId(0L);
        order.setGoodsName(goodsVo.getGoodsName());
        order.setGoodsCount(1);
        order.setGoodsPrice(seckillGoods.getSeckillPrice());
        order.setOrderChannel(1);
        order.setStatus(0);// 创建未支付
        order.setCreateDate(LocalDateTime.now());
        orderMapper.insert(order);
        //生成秒杀订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(user.getId());
        seckillOrder.setOrderId(order.getId());
        seckillOrder.setGoodsId(goodsVo.getId());
        seckillOrderService.save(seckillOrder);

        //将成功秒杀的用户和商品信息，秒杀订单存放在redis中。
        // 后续查询是否重复抢购的时候就不用再去mysql中查询了。
        redisTemplate.opsForValue().set("order:"+user.getId()+":"+goodsVo.getId(), seckillOrder);

        return order;
    }

    // 创建订单详情页对象
    @Override
    public OrderDetailVo detail(Long orderId) {
        if(orderId==null){
            throw new GlobalException(RespBeanEnum.ORDER_NOT_EXIST);
        }
        Order order = orderMapper.selectById(orderId);
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(order.getGoodsId());
        OrderDetailVo detail = new OrderDetailVo();
        detail.setOrder(order);
        detail.setGoodsVo(goodsVo);
        return detail;
    }
}
