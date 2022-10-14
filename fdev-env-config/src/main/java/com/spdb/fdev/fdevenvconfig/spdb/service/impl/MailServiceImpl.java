package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.base.utils.WordUtils;
import com.spdb.fdev.fdevenvconfig.spdb.service.IMailService;
import com.spdb.fdev.transport.RestTransport;
import freemarker.template.Template;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import java.util.*;

@Service
@RefreshScope
public class MailServiceImpl implements IMailService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Resource
    private RestTransport restTransport;
    @Value("${fnotify.host}")
    private String url;
    @Value("${fileUrl:/fdev/attachment/}")
    private String fileUrl;
    /**
     * @param templatePath 模版文件名
     * @param replace      替换的字段和value的map集合
     * @return 邮件正文
     * @throws Exception
     */
    private String getContent(String templatePath, HashMap replace) throws Exception {
        Template template = freeMarkerConfigurer.getConfiguration().getTemplate(templatePath);
        return FreeMarkerTemplateUtils.processTemplateIntoString(template, replace);
    }

    @Override
    public void sendEmail(String subject, String templateName, HashMap<String, Object> model, List<String> to) throws Exception {
        model.put("subject", subject);
        model.put("template_name", templateName);
        model.put("path", Constants.FENVCONFIG);
        String content = getContent("index.ftl", model);
        HashMap<String, Object> mailParams = new HashMap<>();
        mailParams.put("subject", subject);
        mailParams.put("content", content);
        mailParams.put("to", to);
        if(model.get(Dict.APP)!=null){
            if(((List) model.get(Dict.APP)).size()>(int)model.get(Dict.SENDEMAILAPP_SIZE)){
                String filePath = fileUrl+(String) model.get(Dict.MODEL_NAME_EN) + DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT)+Dict.FILE_DOC;
                WordUtils.exportMillCertificateWord(model,(String) model.get(Dict.ENCLOSURE_TEMPLETE),filePath,freeMarkerConfigurer);
                List<String> filePaths = new ArrayList<>();
                String dirPath = Dict.MAIL_PATH + (String) model.get(Dict.MODEL_NAME_EN)+DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT)+Dict.FILE_DOC;
                filePaths.add(dirPath);
                mailParams.put(Dict.FILEPATHS,filePaths);
            }
        }
        mailParams.put(Dict.REST_CODE, "sendEmail");
        restTransport.submit(mailParams);
    }

    /**
     * 根据应用负责人和行内负责人信息准备懒加载的key
     *
     * @param dev_managers  应用负责人
     * @param spdb_managers 行内负责人
     * @return map
     */
    @Override
    public Map<String, Object> valdateKeys(List<Map> dev_managers, List<Map> spdb_managers) {
        String idkey = "";
        String userNameENKey = "";
        String key = "";
        String keys = "";
        List<String> ids = new ArrayList<>();
        List<String> userNameENs = new ArrayList<>();
        Map<String, List<String>> param = new HashMap<>();
        List<Object> list = this.valdate(dev_managers, ids, idkey, userNameENs, userNameENKey);
        List<Object> lists = this.valdate(spdb_managers, (List) list.get(0),
                (String) list.get(1), (List) list.get(2), (String) list.get(3));
        ids = (List) lists.get(0);
        userNameENs = (List) lists.get(2);
        if (ids.isEmpty()) {
            keys = (String) lists.get(3);
            key = Constants.USERNAMERNS;
            param.put(key, userNameENs);
        } else {
            keys = (String) lists.get(1);
            key = com.spdb.fdev.fdevenvconfig.base.dict.Dict.IDS;
            param.put(key, ids);
        }
        Map<String, Object> map = new HashedMap<>();
        map.put(Constants.keys, keys);
        map.put(Constants.DATA, param);
        map.put(Constants.KEY, key);
        return map;
    }

    /***
     * 将应用的行内负责人和厂商负责人合并,并做好懒加载的key
     *
     * @param param         应用的行内负责人和厂商负责人数据
     * @param listIds       Ids集合
     * @param idkey         Idkey
     * @param listNames     userNameEN集合
     * @param userNameENKey userNameENKey
     * @return
     */
    private List<Object> valdate(List<Map> param, List<String> listIds, String idkey, List<String> listNames,
                                 String userNameENKey) {
        StringBuilder idkeyBuilder = new StringBuilder(idkey);
        StringBuilder userNameENKeyBuilder = new StringBuilder(userNameENKey);
        for (Map<String, String> map : param) {
            if (map.containsKey(com.spdb.fdev.fdevenvconfig.base.dict.Dict.ID)) {
                String Id = map.get(com.spdb.fdev.fdevenvconfig.base.dict.Dict.ID);
                if (listIds.contains(Id)) {
                } else {
                    listIds.add(Id);
                    if (idkeyBuilder.length() == 0) {
                        idkeyBuilder.append(Id);
                    } else {
                        idkeyBuilder.append("-").append(Id);
                    }
                }
            } else if (map.containsKey(com.spdb.fdev.fdevenvconfig.base.dict.Dict.USER_NAME_EN)) {
                String name_en = map.get(com.spdb.fdev.fdevenvconfig.base.dict.Dict.USER_NAME_EN);
                if (listNames.contains(name_en)) {
                } else {
                    listNames.add(name_en);
                    if (userNameENKeyBuilder.length() == 0) {
                        userNameENKeyBuilder.append(name_en);
                    } else {
                        userNameENKeyBuilder.append("-").append(name_en);
                    }
                }
            }
        }
        userNameENKey = userNameENKeyBuilder.toString();
        idkey = idkeyBuilder.toString();
        List<Object> list = new ArrayList<>();
        list.add(listIds);
        list.add(idkey);
        list.add(listNames);
        list.add(userNameENKey);
        return list;
    }

}
