package com.dyd.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dyd.seckill.mapper.UserMapper;
import com.dyd.seckill.pojo.User;
import com.dyd.seckill.service.IUserService;
import com.dyd.seckill.utils.MD5Utils;
import com.dyd.seckill.utils.ValidateUtil;
import com.dyd.seckill.vo.LoginVo;
import com.dyd.seckill.vo.RespBean;
import com.dyd.seckill.vo.RespBeanEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dyd
 * @since 2022-08-16
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    // @Autowired 会爆红，因为不是由spring来注入的
    @Resource
    private UserMapper userMapper;

    /**
     * 登录验证
     *
     * @param loginVo
     * @return
     */
    @Override
    public RespBean doLogin(LoginVo loginVo) {
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        // 先判断手机号码和密码是否为空
        if(StringUtils.isEmpty(mobile)||StringUtils.isEmpty(password)){
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        // 再判断手机号的格式是否正确
        if(!ValidateUtil.isMobile(mobile)){
            return RespBean.error(RespBeanEnum.MOBILE_ERROR);
        }

        User user = userMapper.selectById(mobile);
        // 手机号没有注册过
        if(user == null){
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        // 判断密码是否正确
        if(!MD5Utils.fromPassToDBPass(password, user.getSalt()).equals(user.getPassword())){
            return RespBean.error(RespBeanEnum.LOGIN_ERROR);
        }
        return RespBean.success(RespBeanEnum.SUCCESS);
    }
}
