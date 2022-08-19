package com.dyd.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dyd.seckill.pojo.Goods;
import com.dyd.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dyd
 * @since 2022-08-19
 */
public interface IGoodsService extends IService<Goods> {
    /**
     * 获取商品列表
     *
     * @return
     */
    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
