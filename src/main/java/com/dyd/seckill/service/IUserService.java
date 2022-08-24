package com.dyd.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dyd.seckill.pojo.User;
import com.dyd.seckill.vo.LoginVo;
import com.dyd.seckill.vo.RespBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dyd
 * @since 2022-08-16
 */
public interface IUserService extends IService<User> {

    RespBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response);

    User getUserByCookie(String userTicket, HttpServletRequest request, HttpServletResponse response);
}
