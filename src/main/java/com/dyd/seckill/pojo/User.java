package com.dyd.seckill.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
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
public class User implements Serializable {
    // spring session作为分布式session的解决方案。
    // session中setAttribute,键是uuid作为的cookie,值是user对象
    private static final long serialVersionUID = 1L;

    private Long id;

    private String nickname;

    private String password;

    private String salt;

    private String head;

    private LocalDateTime registerDate;

    private LocalDateTime lastLoginDate;

    private Integer loginCount;


}
