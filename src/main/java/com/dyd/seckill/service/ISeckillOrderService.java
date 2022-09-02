package com.dyd.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dyd.seckill.pojo.SeckillOrder;
import com.dyd.seckill.pojo.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dyd
 * @since 2022-08-19
 */
public interface ISeckillOrderService extends IService<SeckillOrder> {

    Long getResult(User user, Long goodsId);
}
