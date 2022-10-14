package com.spdb.fdev.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.util.Util;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.entity.user.StuckPoint;
import com.spdb.fdev.fuser.spdb.service.StuckPointService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RefreshScope
public class OperateRecordAspect {
    private static Logger logger = LoggerFactory.getLogger(OperateRecordAspect.class);

    @Resource(name = "getRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    public UserVerifyUtil userVerifyUtil;

    @Value("${spring.application.name:}")
    public String appName;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private StuckPointService stuckPointService;

    public OperateRecordAspect() {
    }

    @Pointcut("execution(* *(..)) && (@annotation(operateRecord))")
    public void annotationPointCut(OperateRecord operateRecord) {
    }

    @Before("annotationPointCut(operateRecord)")
    public void operateRecordProcess(JoinPoint joinPoint, OperateRecord operateRecord) throws Throwable {
        User user = Util.getSessionUser();
        if (!Util.isNullOrEmpty(user) && this.userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            Object[] args = joinPoint.getArgs();
            String actionArgs = this.objectMapper.writeValueAsString(args[0]);
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String requestURI = request.getRequestURI();
            Map param = new HashMap();
            param.put("interface_name", requestURI);
            param.put("interface_desc", this.appName + ": " + operateRecord.operateDiscribe());
            param.put("p_user_name", user.getUser_name_en());
            param.put("p_user_id", user.getId());
            param.put("p_user_group", user.getGroup_id());
            param.put("interface_data", actionArgs);
            param.put("id", CommonUtils.createId());
            StuckPoint stuckPoint = new StuckPoint((String) param.get(Dict.ID), (String) param.get(Dict.INTERFACE_NAME),
                    (String) param.get(Dict.INTERFACE_DESC), (String) param.get(Dict.P_USER_NAME),
                    (String) param.get(Dict.P_USER_ID), (String) param.get(Dict.P_USER_GROUP), null,
                    (String) param.get(Dict.INTERFACE_DATA), null);
            //新增卡点
            this.stuckPointService.addStuckPoint(stuckPoint);
        }
    }
}
