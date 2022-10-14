package com.spdb.fdev.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.dict.Dict;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.util.Util;
import net.sf.json.JSONObject;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * description： 验证角色后获取交易数据后记录操作信息
 * 验证的角色role_id通过配置来约定
 * author Hubery
 * date 2020-07-22
 * version v0.0.1
 * since v0.0.1
 **/

@Aspect
@Component
public class OperateRecordAspect {

    private static Logger logger = LoggerFactory.getLogger(OperateRecordAspect.class);

    @Resource(name = "getRestTemplate")
    private RestTemplate restTemplate;

    @Value("${userStuckPoint.url}")
    public String url;

    @Autowired
    public UserVerifyUtil userVerifyUtil;

    @Value("${spring.application.name:}")
    public String appName;

    @Pointcut("execution(* *(..)) && (@annotation(operateRecord))")
    public void annotationPointCut(OperateRecord operateRecord) {
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before("annotationPointCut(operateRecord)")
    public void operateRecordProcess(JoinPoint joinPoint, OperateRecord operateRecord) throws Throwable {

        //请求头中添加source=back跳过权限验证
        String back = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(Dict.BACKEND_REQUEST_FLAG_KEY);
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader(Dict.TOKEN_AUTHORIZATION_KEY));
        if (!Dict.BACKEND_REQUEST_FLAG_VALUE.equals(back) && !Util.isNullOrEmpty(user)) {
            //  验证 user 角色
            boolean isStuckPointManage = this.userVerifyUtil.userRoleIsSPM(user.getRole_id());
            if (isStuckPointManage) {
                //  记录卡点操作
                Object[] args = joinPoint.getArgs();
                String actionArgs = this.objectMapper.writeValueAsString(args.length == 0 ? "" : args[0]);
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                //  接口名称
                String requestURI = request.getRequestURI();
                Map param = new HashMap();
                param.put(Dict.INTERFACE_NAME, requestURI);
                param.put(Dict.INTERFACE_DESC, appName + ": " + operateRecord.operateDiscribe());
                param.put(Dict.P_USER_NAME, user.getUser_name_en());
                param.put(Dict.P_USER_ID, user.getId());
                param.put(Dict.P_USER_GROUP, user.getGroup_id());
                param.put(Dict.INTERFACE_DATA, actionArgs);
                Map responseMap = (Map) this.restTemplate.postForObject(this.url, param, Map.class, new Object[0]);
                JSONObject dataMap = JSONObject.fromObject(responseMap);
                if ("AAAAAAA".equals(dataMap.get(Dict.CODE))) {
                    logger.info("RecordOperat success ! ");
                }
            }
        }
    }


}
