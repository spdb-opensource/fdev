package com.fdev.docmanage.util;

import com.spdb.fdev.common.util.UserVerifyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fdev.docmanage.dict.ErrorConstants;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.dict.Dict;
import com.spdb.fdev.common.exception.FdevException;

import java.util.List;
import java.util.Map;

@Configuration
public class CommonUtils {
    @Autowired
    public UserVerifyUtil userVerifyUtil;
    /**
     * 判断输入数据是否是空
     *
     * @param obj
     * @return
     */
    public static boolean isNullOrEmpty(Object obj) {
        if (obj instanceof Object[]) {
            Object[] o = (Object[]) obj;
            if (o.length == 0) {
                return true;
            }
            return false;
        } else {
            if (obj instanceof String) {
                if ((("").equals(((String) obj).trim())) || (("null").equals(((String) obj).trim()))) {
                    return true;
                }
                return false;
            }
            if (obj instanceof List) {
                List objList = (List) obj;
                if (objList.isEmpty()) {
                    return true;
                }
                return false;
            }
            if (obj instanceof Map) {
                Map objMap = (Map) obj;
                if (objMap.isEmpty()) {
                    return true;
                }
                return false;
            }
            if ((obj == null) || (("").equals(obj))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从session中获取用户信息
     *
     * @return 用户英文名
     * @throws Exception
     */

    public  User getSessionUser() throws Exception {
        User user = userVerifyUtil.getSessionUser(((ServletRequestAttributes)RequestContextHolder.
                getRequestAttributes()).getRequest().getHeader(Dict.TOKEN_AUTHORIZATION_KEY));
        if (CommonUtils.isNullOrEmpty(user)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL);
        }
        return user;
    }

}