package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.executor.service.UpdateGitworkService;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

/**
 * @Author: zhangxt
 * @Date: 2021/4/12 13:41
 **/
public class UpdateGitworkAction extends BaseExecutableAction {

    private static Logger logger = LoggerFactory.getLogger(UpdateGitworkAction.class);

    @Autowired
    private UpdateGitworkService updateGitworkService;

    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        updateGitworkService.batchCode();
        updateGitworkService.batchCodeNew();
    }

}
