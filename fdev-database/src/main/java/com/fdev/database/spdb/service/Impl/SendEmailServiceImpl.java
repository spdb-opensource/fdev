package com.fdev.database.spdb.service.Impl;

import com.fdev.database.dict.Dict;
import com.fdev.database.dict.EmailConstants;
import com.fdev.database.spdb.service.MailService;
import com.fdev.database.spdb.service.SendEmailService;
import com.fdev.database.spdb.service.UserService;
import com.spdb.fdev.common.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RefreshScope
public class SendEmailServiceImpl implements SendEmailService {

    @Autowired
    private MailService mailService;

    @Autowired
    UserService userService;

    @Value("${fdev.email.control.enabled}")
    private boolean emailControlEnabled;

    @Value("${fdev.notify.control.enabled}")
    private boolean notifyControlEnabled;

    @Value("${link.port}")
    private String port;

    @Override
    public void sendEmail(HashMap model) throws Exception {
        if(emailControlEnabled) {
            User user = (User) model.get(Dict.SPDB_MANAGERS);
            List<String> email = new ArrayList<>();
            email.add(user.getEmail());
            mailService.sendEmail(EmailConstants.TITLE_DATABASENOTICE, EmailConstants.EMAIL_DATABASE_UPLOADNOTICE, model, email);
        }
    }

    @Override
    public void sendUserNotify(HashMap model) throws Exception {
        if(notifyControlEnabled) {
            List<String> users = new ArrayList<>();
            User user = (User) model.get(Dict.SPDB_MANAGERS);
            users.add(user.getUser_name_en());
            mailService.sendUserNotify(EmailConstants.UPLOAD_NOTIFY, users, EmailConstants.UPLOAD_DESC, "0", (String) model.get(Dict.HYPERLINK));
        }
    }

    
}
