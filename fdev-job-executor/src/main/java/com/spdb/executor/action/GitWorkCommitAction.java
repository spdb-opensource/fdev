package com.spdb.executor.action;

import com.csii.pe.action.ext.BaseExecutableAction;
import com.csii.pe.core.Context;
import com.csii.pe.spdb.common.dict.Dict;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class GitWorkCommitAction extends BaseExecutableAction {

    private static final Logger logger = LoggerFactory.getLogger(GitWorkCommitAction.class);

    @Autowired
    private RestTransport restTransport;

    @Override
    public void execute(Context context) {
        try {
            restTransport.submit(new HashMap<String, String>() {{
                put(Dict.REST_CODE, "commitStatistics");
            }});
        } catch (Exception e) {
            logger.error("定时获取commit数据异常", e);
        }
    }
}
