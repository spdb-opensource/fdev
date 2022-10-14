package com.fdev.notify.web;

import com.fdev.notify.dict.Dict;
import com.fdev.notify.dict.ErrorConstants;
import com.fdev.notify.service.MailService;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.common.validate.RequestValidate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class MailController {

    @Resource
    private MailService mailService;

    @RequestValidate(NotEmptyFields = {Dict.SUBJECT, Dict.CONTENT, Dict.TO})
    @RequestMapping(value = "/sendEmail", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult sendEmail(@RequestBody Map<String, Object> params) {
        String subject = (String) params.get(Dict.SUBJECT);
        String content = (String) params.get(Dict.CONTENT);
        List<String> to = (List<String>) params.get(Dict.TO);
        List<String> cc = (List<String>) params.get(Dict.CC);
        List<String> filePaths = (List<String>) params.get(Dict.FILEPATHS);
        List<File> files = new ArrayList<>();
        try {
            if (Util.isNullOrEmpty(filePaths)) {
                mailService.send(to, cc, subject, content);
            } else {
                for (String filePath : filePaths) {
                    File file = new File(filePath);
                    if (file.exists() && file.isFile()) {
                        files.add(file);
                    } else {
                        continue;
                    }
                }
                mailService.send(to, cc, subject, content, files);
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.EMAIL_SEND_ERROR);
        }
        return JsonResultUtil.buildSuccess();
    }
}
