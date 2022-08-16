package com.dyd.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum RespBeanEnum {
    SUCCESS(200, "success"),
    ERROR(500, "服务端异常");
    private final Integer code;
    private final String message;
}
