package com.dyd.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dyd.seckill.pojo.Order;
import com.dyd.seckill.pojo.User;
import com.dyd.seckill.vo.GoodsVo;
import com.dyd.seckill.vo.OrderDetailVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dyd
 * @since 2022-08-19
 */
public interface IOrderService extends IService<Order> {

    Order seckill(User user, GoodsVo goodsVo);

    OrderDetailVo detail(Long orderId);
}
