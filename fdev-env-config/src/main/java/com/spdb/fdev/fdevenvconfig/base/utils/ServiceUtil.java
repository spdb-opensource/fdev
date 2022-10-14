package com.spdb.fdev.fdevenvconfig.base.utils;

import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RefreshScope
public class ServiceUtil {

    @Value("${record.switch}")
    private Boolean recordSwitch;
    @Autowired
	public UserVerifyUtil userVerifyUtil;

    /**
     * 获取操作人id
     *
     * @return
     */
    public String getOpno() throws Exception{
        if (recordSwitch) {
            try {
                User user = userVerifyUtil.getRedisUser();
                return user.getId();
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.SERVER_ERROR, new String[]{"获取当前用户id失败！"});
            }
        }
        return "";
    }

}