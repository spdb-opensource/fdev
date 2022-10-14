package com.spdb.fdev.codeReview.spdb.service;

import com.spdb.fdev.codeReview.spdb.entity.CodeMeeting;

import java.util.List;
import java.util.Map;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
public interface IMeetingService {

    List<CodeMeeting> queryMeetingByOrderId(String orderId);

    Map<String, Object> queryMeetings(String orderId) throws Exception;

    void deleteMeetingById(String id) throws Exception;

    void add(CodeMeeting codeMeeting) throws Exception;

    void update(CodeMeeting codeMeeting) throws Exception;
}
