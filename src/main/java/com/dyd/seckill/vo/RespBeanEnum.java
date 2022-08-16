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
    // 登录模块
    LOGIN_ERROR(500210, "用户名或密码有误"),
    MOBILE_ERROR(500211, "手机号码格式不正确");
    private final Integer code;
    private final String message;
}
