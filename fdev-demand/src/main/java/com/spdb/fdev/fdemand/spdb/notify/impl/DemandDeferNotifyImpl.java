package com.spdb.fdev.fdemand.spdb.notify.impl;

import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.RelatePartDetail;
import com.spdb.fdev.fdemand.spdb.notify.INotifyStrategy;
import com.spdb.fdev.fdemand.spdb.service.IDemandBaseInfoService;
import com.spdb.fdev.fdemand.spdb.service.IMailService;
import com.spdb.fdev.fdemand.spdb.service.IRoleService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.*;
@RefreshScope
@Component(Dict.DemandDeferNotifyImpl)
public class DemandDeferNotifyImpl implements INotifyStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DemandDeferNotifyImpl.class);

    @Autowired
    private IDemandBaseInfoService demandBaseInfoService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IMailService mailService;

    @Autowired
    private RestTransport restTransport;


    @Value("${history.demand.url}")
    private String history_demand_url;

    @Override
    public void doNotify(Map<String, Object> param) {
        try {
            String id = (String) param.get(Dict.ID);
            DemandBaseInfo demandBaseInfo = demandBaseInfoService.queryById(id);
            //1.拼接邮件内容
            HashMap hashMap = new HashMap();
            String user_name_cn = CommonUtils.getSessionUser().getUser_name_cn();//获取邮件联系人
            String oa_contact_no = demandBaseInfo.getOa_contact_no();
            hashMap.put(Dict.OA_CONTACT_NO, oa_contact_no);
            hashMap.put(Dict.NAME_CN, demandBaseInfo.getOa_contact_name());
            hashMap.put(Dict.USER_NAME_CN, user_name_cn);
            hashMap.put(Dict.DEFER_REASON, param.get(Dict.DEFER_REASON));//获取暂缓原因
            hashMap.put(Dict.HISTORY_DEMAND_URL, history_demand_url + demandBaseInfo.getId());//需求链接地址
            String mailContent = mailService.getDeferMailContent(hashMap);
            //2. 获取所有使用关联人员
            Set<String> users = new HashSet<>();
            if (null != demandBaseInfo.getDemand_leader())
                users.addAll(demandBaseInfo.getDemand_leader());//获取需求牵头人
            users.add(CommonUtils.getSessionUser().getId());//获取当前暂缓需求处理人
            if (!CommonUtils.isNullOrEmpty(demandBaseInfo.getRelate_part_detail())) {
                demandBaseInfo.getRelate_part_detail().stream().filter(x -> null != x.getAssess_user()).map(RelatePartDetail::getAssess_user).forEach(users::addAll);
            }
            
            //3.获取用户的邮箱
            Map<String, Object> mapId = new HashMap<>();
            mapId.put(Dict.IDS, users);
            mapId.put(Dict.REST_CODE, Dict.QUERYBYUSERCOREDATA);
            Map<String, Map> userMap = (Map<String, Map>) restTransport.submit(mapId);
            List<String> email_receivers = new ArrayList<>();
            Set<Map.Entry<String, Map>> entries = userMap.entrySet();
            if (!CommonUtils.isNullOrEmpty(entries)) {
                for (Map.Entry<String, Map> entry : entries) {
                    try {
                        String email = (String) entry.getValue().get(Dict.EMAIL);
                        email_receivers.add(email);
                    } catch (Exception e) {
                        logger.error("获取人员邮箱信息错误" + entry.getKey());
                    }
                }
            }
            //4.发送邮件
            HashMap<String, String> sendMap = new HashMap();
            sendMap.put(Dict.EMAIL_CONTENT, mailContent);
            String topic = "需求暂缓提醒";
            mailService.sendEmail(topic, Dict.FDEMAND_DEFER_FTL, sendMap, email_receivers.toArray(new String[email_receivers.size()]));
        } catch (Exception e) {
            logger.error("需求暂缓邮件发送失败{}", e.getStackTrace());
        }
    }
}
