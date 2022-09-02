package com.dyd.seckill.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 主要用于创建对象传递给 RabbitMQ 来进行具体的秒杀操作
 * 之前的seckill(user, goodsVo)；中主要需要user对象和goodsVo对象。所以这个类要包含
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillMessage {
    private User user;
    private Long goodsId;
}
