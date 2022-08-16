package com.dyd.seckill.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author dyd
 * @since 2022-08-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_user")
public class User{

    private static final long serialVersionUID = 1L;

    private String nickname;

    private String password;

    private String salt;

    private String head;

    private LocalDateTime registerDate;

    private LocalDateTime lastLoginDate;

    private Integer loginCount;


}
