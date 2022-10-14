package com.spdb.fdev.codeReview.spdb.controller;

import com.spdb.fdev.codeReview.base.annotation.nonull.NoNull;
import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.spdb.entity.CodeMeeting;
import com.spdb.fdev.codeReview.spdb.service.IMeetingService;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author liux81
 * @DATE 2021/11/11
 */
@CrossOrigin
@RestController
@RequestMapping("/api/meeting")
public class MeetingController {

    private static Logger logger = LoggerFactory.getLogger(MeetingController.class);

    @Autowired
    IMeetingService meetingService;

    @PostMapping("/queryMeetings")
    @NoNull(require = {Dict.ORDERID},parameter = CodeMeeting.class)
    public JsonResult queryMeetings(@RequestBody CodeMeeting codeMeeting) throws Exception {
        String orderId = codeMeeting.getOrderId();
        return JsonResultUtil.buildSuccess(meetingService.queryMeetings(orderId));
    }

    @PostMapping("/deleteMeetingById")
    @NoNull(require = {Dict.ID},parameter = CodeMeeting.class)
    public JsonResult deleteMeetingById(@RequestBody CodeMeeting codeMeeting) throws Exception {
        meetingService.deleteMeetingById(codeMeeting.getId());
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/add")
    @NoNull(require = {Dict.MEETINGTIME,Dict.ADDRESS,Dict.MEETINGTYPE,Dict.AUDITEUSERS,Dict.ORDERID},parameter = CodeMeeting.class)
    public JsonResult add(@RequestBody CodeMeeting codeMeeting) throws Exception {
        meetingService.add(codeMeeting);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/update")
    @NoNull(require = {Dict.ID,Dict.MEETINGTIME,Dict.ADDRESS,Dict.MEETINGTYPE,Dict.AUDITEUSERS}, parameter = CodeMeeting.class)
    public JsonResult update(@RequestBody CodeMeeting codeMeeting) throws Exception {
        meetingService.update(codeMeeting);
        return JsonResultUtil.buildSuccess();
    }
}
