package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class IamsInterfaceFileParseAction extends BaseExecutableAction {
    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        logger.info("execute iamsInterfaceFileParse begin");
        HashMap requstParams = new HashMap();
        requstParams.put(Dict.REST_CODE, "iamsInterfaceFileParse");
        try {
            restTransport.submit(requstParams);
        } catch (Exception e) {
            logger.error("定时解析挡板接口文件失败", e);
            throw new FdevException("定时解析挡板接口文件失败");
        }
        logger.info("execute iamsInterfaceFileParse end");
    }
}
