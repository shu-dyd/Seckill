package com.dyd.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dyd.seckill.pojo.Goods;
import com.dyd.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author dyd
 * @since 2022-08-19
 */
public interface GoodsMapper extends BaseMapper<Goods> {

    List<GoodsVo> findGoodsVo();

    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
