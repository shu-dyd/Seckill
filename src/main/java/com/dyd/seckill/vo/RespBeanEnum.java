package com.dyd.seckill.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public enum RespBeanEnum {
    // 通用
    SUCCESS(200, "success"),
    ERROR(500, "服务端异常"),
    // 登录模块错误，主要用于参数校验失败的情况，没有用validation来处理
    LOGIN_ERROR(500210, "用户名或密码有误"),
    MOBILE_ERROR(500211, "手机号码格式不正确"),
    // 登录模块错误，主要用于参数校验失败的情况，没有用validation来处理
    BIND_ERROR(500212, "参数校验失败"),

    // 秒杀模块
    EMPTY_STOCK(500500, "库存不足"),
    REPEATE_ERROR(500501, "该商品每人限购一件");
    private final Integer code;
    private final String message;
}
