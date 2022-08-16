package com.dyd.seckill.vo;

import com.dyd.seckill.validator.IsMobile;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class LoginVo {
    @NotNull
    @IsMobile(required = true)
    private String mobile;

    @NotNull
    @Length(min = 32)
    private String password;
}
