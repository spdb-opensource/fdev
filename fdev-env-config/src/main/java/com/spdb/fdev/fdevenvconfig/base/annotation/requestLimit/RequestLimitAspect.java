package com.spdb.fdev.fdevenvconfig.base.annotation.requestLimit;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.service.IRequestService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @auther duancj
 * @date 2021-01-19 16:50
 * @descripiton : 用户权限和请求频率判断
 */
@Aspect
@Component
public class RequestLimitAspect {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private long lastTime = 0;
    @Autowired
    private IRequestService requestService;
    @Autowired
	public UserVerifyUtil userVerifyUtil;

    @Pointcut("@annotation(com.spdb.fdev.fdevenvconfig.base.annotation.requestLimit.RequestLimit)")
    public void controllerInterceptor(){
        //限制接口调用切面类
    }
    @Around("controllerInterceptor()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        //获取当前用户
        User user = userVerifyUtil.getRedisUser();
        //获取所有环境配置管理员
        List<Map<String, Object>> allusers = requestService.queryUser(Dict.ROLE_ENV_ADMIN);
        //获取环境配置管理员ID
        List<String> userids = new ArrayList<>();
        for(int i=0; i < allusers.size(); i++){
            userids.add(allusers.get(i).get("id").toString());
        }
        //判断权限
        if(!userids.contains(user.getId())){
            throw new FdevException("COM0004");
        }


        //获取当前时间
        long currentTime = System.currentTimeMillis();
        //超过10分钟允许请求
        if (currentTime - lastTime > 1000 * 60 * 10) {
            lastTime = currentTime;
            return pjp.proceed();
        }
        //10分钟之内返回错误提示
        else {
            logger.error("同步生产信息请求频繁");
            throw new FdevException("ENV0025");
        }
    }
}
