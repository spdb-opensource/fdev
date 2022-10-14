package com.spdb.fdev.codeReview.spdb.dao;

import com.spdb.fdev.codeReview.spdb.entity.CodeMeeting;

import java.util.List;
import java.util.Set;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
public interface IMeetingDao {

    List<CodeMeeting> queryMeetingByOrderId(String orderId);

    CodeMeeting queryMeetingById(String id);

    CodeMeeting deleteMeetingById(String id);

    CodeMeeting add(CodeMeeting codeMeeting) throws Exception;

    void update(CodeMeeting codeMeeting) throws Exception;

    List<CodeMeeting> queryMeetingByIds(Set<String> meetingIds);
}
