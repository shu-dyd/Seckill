package com.dyd.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsVo {
    private Long id;
    private BigDecimal seckillPrice;
    private Integer stockCount;
    private String goodsName;
    private String goodsTitle;
    private String goodsImg;
    private String goodsDetail;
    private BigDecimal goodsPrice;
    private Integer goodsStock;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
