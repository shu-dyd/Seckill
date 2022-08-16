package com.dyd.seckill.exception;

import com.dyd.seckill.vo.RespBean;
import com.dyd.seckill.vo.RespBeanEnum;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


// 参考springmvc的异常处理器
// 注意RestControllerAdvice和ControllerAdvice的区别
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public RespBean ExceptionHandler(Exception e){
        if(e instanceof GlobalException){
            GlobalException ex = (GlobalException) e;
            return RespBean.error(ex.getRespBeanEnum());
        }else if(e instanceof BindException){
            // 因为注解方式进行参数校验，异常是BindException
            BindException ex = (BindException) e;
            // 因为是用注解方式进行参数校验，相关注解本身会有一个异常信息，可以利用起来。而不是用BIND_ERROR中默认的
            RespBean respBean = RespBean.error(RespBeanEnum.BIND_ERROR);
            respBean.setMessage("参数校验异常:" + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
            return respBean;
        }
        // 如果以上两种异常都不属于的话
        return RespBean.error(RespBeanEnum.ERROR);
    }

}
