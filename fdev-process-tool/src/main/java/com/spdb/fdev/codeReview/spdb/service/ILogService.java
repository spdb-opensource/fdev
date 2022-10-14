package com.spdb.fdev.codeReview.spdb.service;

import com.mongodb.client.result.DeleteResult;
import com.spdb.fdev.codeReview.spdb.entity.CodeLog;

import java.util.List;

/**
 * @Author liux81
 * @DATE 2021/11/10
 */
public interface ILogService {
    CodeLog add(CodeLog codeLog) throws Exception;

    DeleteResult deleteLog(String orderId);

    List<CodeLog> queryLogs(String orderId) throws Exception;
}
