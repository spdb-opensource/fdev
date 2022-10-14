package com.spdb.fdev.fdemand.spdb.notify.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.notify.INotifyStrategy;
import com.spdb.fdev.fdemand.spdb.service.IDemandBaseInfoService;
import com.spdb.fdev.fdemand.spdb.service.IMailService;
import com.spdb.fdev.fdemand.spdb.service.IRoleService;
import com.spdb.fdev.fdemand.spdb.unit.DesignUnit;
import com.spdb.fdev.transport.RestTransport;
@RefreshScope
@Lazy
@Component(Dict.UiUploadedImpl)
public class UiUploadedImpl implements INotifyStrategy{
	private static final Logger logger = LoggerFactory.getLogger(DemandDeferNotifyImpl.class);

    @Autowired
    private IDemandBaseInfoService demandBaseInfoService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IMailService mailService;

    @Autowired
    private RestTransport restTransport;
    
    @Autowired
    private IRoleService fdevUserService;
    
    @Autowired
    private DesignUnit designUnit;


    @Value("${fdev.demand.ip}")
    private String demandIp;

    
    @Override
    public void doNotify(Map<String, Object> param) {
        try {
            String id = (String) param.get(Dict.ID);
            DemandBaseInfo demandBaseInfo = demandBaseInfoService.queryById(id);
            //1.拼接邮件内容
            HashMap hashMap = new HashMap();
            String demand_type = demandBaseInfo.getDemand_type();
            String oa_contact_no = demandBaseInfo.getOa_contact_no();
            String demand_type_cn = new String();
            if ((Dict.BUSINESS).equalsIgnoreCase(demand_type)) {
            	demand_type_cn = "业务需求";
			}else {
				throw new FdevException(ErrorConstants.DEMAND_TYPE_ERROR);
			}
            Map groupInfo = designUnit.getGroupInfo(new HashMap() {{
                put(Dict.ID, demandBaseInfo.getDemand_leader_group());
            }});
            String group = (String) groupInfo.get("parent_id");
            String sortNum = (String) groupInfo.get("sortNum");
            String emailGroup;
            if (sortNum.length()>3) {
            	String newSortNum = sortNum.substring(0, 3);
            	Map currentGroup = designUnit.getGroupInfo(new HashMap() {{
            		put("sortNum", newSortNum);
            	}});
            	emailGroup = (String) currentGroup.get("name");
			}else {
				emailGroup = (String) groupInfo.get("name");
			}
            hashMap.put(Dict.DEMAND_UI_URL, demandIp + "/fdev/#/rqrmn/designReviewRqr/"+demandBaseInfo.getId());//UI设计稿审核链接地址
            String mailContent = mailService.getUiUploadedMailContent(hashMap);
            //2. 获取所有使用关联人员
            Map uiMainRole = fdevUserService.queryFdevRoleByName(Constants.UI_MANAGER);
            String mainId = (String) uiMainRole.get(Dict.ID);
            List<LinkedHashMap> userInfos = fdevUserService.queryUserByRole(mainId);
            List<String> userIds = new ArrayList<>();
            for (LinkedHashMap userInfo : userInfos) {
                if (!CommonUtils.isNullOrEmpty(userInfo.get(Dict.ID)))
                    userIds.add((String) userInfo.get(Dict.ID));
            }
            
            //3.获取用户的邮箱
            Map<String, Object> mapId = new HashMap<>();
            mapId.put(Dict.IDS, userIds);
            mapId.put(Dict.REST_CODE, Dict.QUERYBYUSERCOREDATA);
            Map<String, Map> userMap = (Map<String, Map>) restTransport.submit(mapId);
            List<String> email_receivers = new ArrayList<>();
            Set<Map.Entry<String, Map>> entries = userMap.entrySet();
            if (!CommonUtils.isNullOrEmpty(entries)) {
                for (Map.Entry<String, Map> entry : entries) {
                    try {
                        String email = (String) entry.getValue().get(Dict.EMAIL);
                        if (!CommonUtils.isNullOrEmpty(email)) {
                        	email_receivers.add(email);
						}
                        continue;
                    } catch (Exception e) {
                        logger.error("获取人员邮箱信息错误" + entry.getKey());
                    }
                }
            }
            //4.发送邮件
            HashMap<String, String> sendMap = new HashMap();
            sendMap.put(Dict.EMAIL_CONTENT, mailContent);
            String topic = "【"+emailGroup+"-"+demand_type_cn+"-"+oa_contact_no+"】"+"设计稿审核申请通知";
            mailService.sendEmail(topic, Dict.FDEMAND_UIUPLOADED_FTL, sendMap, email_receivers.toArray(new String[email_receivers.size()]));
        } catch (Exception e) {
            logger.error("Ui设计稿已上传邮件发送失败{}", e.getStackTrace());
        }
    }
}