package com.fdev.notify.service;

import java.io.File;
import java.util.List;

public interface MailService {

    public void send(List<String> to, List<String> cc, String subject, String content) throws Exception;

    public void send(List<String> to, List<String> cc, String subject, String content, List<File> files) throws Exception;

    public void sendEmail(List<String> to, List<String> cc, String subject, String content, List<File> files) throws Exception;

}
