package com.fdev.notify.service.impl;


import com.fdev.notify.entity.MailAuthenticator;
import com.fdev.notify.service.MailService;
import com.fdev.notify.util.CommonUtils;
import com.spdb.fdev.common.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.*;

@Service
@EnableAsync
@RefreshScope
public class MailServiceImpl implements MailService {

    // ���巢���ˡ��ռ��ˡ�SMTP���������û��������롢���⡢���ݵ�
    private String displayName;
    @Value("${send.email.from:xxx}")
    private String from = "";
    @Value("${send.email.smtpServer.host:xxx}")
    private String smtpServer = "";
    @Value("${send.email.smtpServer.port:9025}")
    private String smtpPort = "";
    @Value("${send.email.username:xxx}")
    private String username = "";
    @Value("${send.email.password:wujin@1234}")
    private String password = "";
    private boolean isExchange = false;
    private String domain;
    private boolean ifAuth = true; // �������Ƿ�Ҫ������֤
    private String filename = "";
    private Vector<String> file = new Vector<String>(); // ���ڱ��淢�͸������ļ����ļ���
    @Value("${spring.profiles.active}")
    private String active;

    /**
     * ���ʼ��������ӻ�����ʶ
     *
     * @param to
     * @param subject
     * @param content
     */
    @Async
    public void send(List<String> to, List<String> cc, String subject, String content) throws Exception {
        if (!Util.isNullOrEmpty(subject) && !"pro".equals(active) && !"pro-new".equals(active))
            subject = "[" + active + "]  " + subject;
        sendEmail(to, cc, subject, content, null);
    }

    /**
     * ���ʼ��������ӻ�����ʶ(������)
     *
     * @param to
     * @param subject
     * @param content
     * @param files
     * @throws Exception
     */
    @Async
    public void send(List<String> to, List<String> cc, String subject, String content, List<File> files) throws Exception {
        if (!Util.isNullOrEmpty(subject) && !"pro".equals(active) && !"pro-new".equals(active))
            subject = "[" + active + "]  " + subject;
        sendEmail(to, cc, subject, content, files);
    }

    @Async
    public void sendEmail(List<String> to, List<String> cc, String subject, String content, List<File> files) throws Exception {
        Session session = null;
        Properties props = System.getProperties();
        props.put("mail.smtp.host", smtpServer);
        props.put("mail.smtp.port", smtpPort);

        if (isExchange) {
            if (domain == null || domain.equals("")) {
                throw new RuntimeException("domain is null");
            }
            props.setProperty("mail.smtp.auth.ntlm.domain", domain);
        }
        if (ifAuth) { // ��������Ҫ������֤
            props.put("mail.smtp.auth", "true");
            MailAuthenticator smtpAuth = new MailAuthenticator(username, password);
            session = Session.getDefaultInstance(props, smtpAuth);
        } else {
            props.put("mail.smtp.auth", "false");
            session = Session.getDefaultInstance(props, null);
        }
        Message msg = new MimeMessage(session);
        Address from_address = new InternetAddress(from, displayName);
        msg.setFrom(from_address);
        msg.setSubject(subject);
        Multipart mp = new MimeMultipart();
        MimeBodyPart mbp = new MimeBodyPart();
        mbp.setContent(content, "text/html;charset=utf-8");
        mp.addBodyPart(mbp);

        //�ʼ�����
        if (!Util.isNullOrEmpty(files)) {
            for (File file : files) {
                MimeBodyPart attachmentMpb = new MimeBodyPart();
                // ��ȡ�ļ�
                DataHandler dataHandler2 = new DataHandler(new FileDataSource(file));
                // �������������ӵ�"�ڵ�"
                attachmentMpb.setDataHandler(dataHandler2);
                // ���ø������ļ�������Ҫ���룩
                attachmentMpb.setFileName(MimeUtility.encodeText(dataHandler2.getName()));
                mp.addBodyPart(attachmentMpb);
            }
            // ��Ϲ�ϵ
            ((MimeMultipart) mp).setSubType("mixed");
        }

        msg.setContent(mp); // Multipart���뵽�ż�
        msg.setSentDate(new Date()); // �����ż�ͷ�ķ�������
        // �����ż�
        msg.saveChanges();
        //�ʼ�������
        HashSet<InternetAddress> ccInternetAddresses = new HashSet<>();
        if(!CommonUtils.isNullOrEmpty(cc)){
            for (String s : cc) {
                ccInternetAddresses.add(new InternetAddress(s));
            }
        }
        //�ʼ��ռ��˷�������
        List<List<String>> emailsLists = groupList(to);
        int i = 1;//��¼�����ʼ������Σ����һ�����ϳ�����
        for (List<String> emailList : emailsLists) {
            HashSet<InternetAddress> internetAddresses = new HashSet<>();
            for (String email : emailList) {
                internetAddresses.add(new InternetAddress(email));
            }
            msg.setRecipients(Message.RecipientType.TO, internetAddresses.toArray(new InternetAddress[internetAddresses.size()]));
            if(i == emailsLists.size()){
                msg.setRecipients(Message.RecipientType.CC, ccInternetAddresses.toArray(new InternetAddress[ccInternetAddresses.size()]));
            }
            Transport trans = null;
            trans = session.getTransport("smtp");
            trans.connect(smtpServer, username, password);
            trans.sendMessage(msg, msg.getAllRecipients());
            trans.close();
            i++;
        }
        //�ʼ��������ɾ�����ش洢����
        if (!Util.isNullOrEmpty(files)) {
            for (File file : files) {
                try {
                    file.delete();
                } catch (Exception e) {
                    continue;
                }
            }
        }
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isExchange() {
        return isExchange;
    }

    public void setExchange(boolean exchange) {
        isExchange = exchange;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isIfAuth() {
        return ifAuth;
    }

    public void setIfAuth(boolean ifAuth) {
        this.ifAuth = ifAuth;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Vector<String> getFile() {
        return file;
    }

    public void setFile(Vector<String> file) {
        this.file = file;
    }

    /**
     * ���Ӹ����ľ���·����ַ
     */
    public void addAttachfile(String fname) {
        file.addElement(fname);
    }


    public static List<List<String>> groupList(List<String> list) {
        List<List<String>> listGroup = new ArrayList<List<String>>();
        int listSize = list.size();
        int toIndex = 20;
        for (int i = 0; i < list.size(); i += 20) {
            if (i + 20 > listSize) {
                toIndex = listSize - i;
            }
            List<String> newList = list.subList(i, i + toIndex);
            listGroup.add(newList);
        }
        return listGroup;
    }

}