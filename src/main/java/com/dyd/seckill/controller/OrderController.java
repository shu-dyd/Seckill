package com.dyd.seckill.controller;


import com.dyd.seckill.pojo.User;
import com.dyd.seckill.service.IOrderService;
import com.dyd.seckill.vo.OrderDetailVo;
import com.dyd.seckill.vo.RespBean;
import com.dyd.seckill.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dyd
 * @since 2022-08-19
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    /**
     * 订单详情
     *
     * @param user
     * @param orderId
     * @return
     */
    @RequestMapping("/detail")
    public RespBean detail(User user, Long orderId){
        if(user==null){
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }

        OrderDetailVo detail = orderService.detail(orderId);
        return RespBean.success(detail);
    }
}
