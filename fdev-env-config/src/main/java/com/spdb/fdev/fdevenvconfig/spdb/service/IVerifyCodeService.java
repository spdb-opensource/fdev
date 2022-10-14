package com.spdb.fdev.fdevenvconfig.spdb.service;

import java.util.Map;

public interface IVerifyCodeService {

    /**
     * 生成验证码
     *
     * @param map
     * @return
     * @throws Exception 
     */
    String getVerifyCode(Map map) throws Exception;

    /**
     * 校验验证码
     * @throws Exception 
     */
    void checkVerifyCode(String verifyCode) throws Exception;

}
