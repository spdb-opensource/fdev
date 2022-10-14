package com.spdb.fdev.codeReview.spdb.service.impl;

import com.mongodb.client.result.DeleteResult;
import com.spdb.fdev.codeReview.base.utils.CommonUtils;
import com.spdb.fdev.codeReview.base.utils.TimeUtil;
import com.spdb.fdev.codeReview.base.utils.UserUtil;
import com.spdb.fdev.codeReview.spdb.dao.ILogDao;
import com.spdb.fdev.codeReview.spdb.dto.LogContent;
import com.spdb.fdev.codeReview.spdb.entity.CodeLog;
import com.spdb.fdev.codeReview.spdb.service.ILogService;
import com.spdb.fdev.common.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author liux81
 * @DATE 2021/11/10
 */
@Service
public class LogServiceImpl implements ILogService {

    @Autowired
    ILogDao logDao;
    @Autowired
    UserUtil userUtil;

    @Override
    public CodeLog add(CodeLog codeLog) throws Exception {
        User sessionUser = CommonUtils.getSessionUser();
        codeLog.setOperateUser(sessionUser.getId());
        codeLog.setOperateTime(TimeUtil.formatTodayHs());
        return logDao.add(codeLog);
    }

    @Override
    public DeleteResult deleteLog(String orderId) {
        return logDao.deleteLog(orderId);
    }

    @Override
    public List<CodeLog> queryLogs(String orderId) throws Exception {
        List<CodeLog> codeLogs = logDao.queryLogs(orderId);
        //封装操作人姓名
        Set<String> userIds = new HashSet<>();
        for(CodeLog codeLog : codeLogs){
            userIds.add(codeLog.getOperateUser());
        }
        Map userInfos = userUtil.getUserByIds(userIds);
        for(CodeLog codeLog : codeLogs){
            //封装成前端展示所需的格式
            List<String> fieldList = new ArrayList<>();
            List oldValueList = new ArrayList();
            List newValueList = new ArrayList();
            for(LogContent logContent : codeLog.getContent()){
                fieldList.add(logContent.getFieldName());
                oldValueList.add(logContent.getOldValue());
                newValueList.add(logContent.getNewValue());
            }
            codeLog.setFieldList(fieldList);
            codeLog.setOldValueList(oldValueList);
            codeLog.setNewValueList(newValueList);
            //封装操作人姓名
            codeLog.setOperateUserName(userUtil.getUserNameById(codeLog.getOperateUser(),userInfos));
        }
        return codeLogs;
    }
}
