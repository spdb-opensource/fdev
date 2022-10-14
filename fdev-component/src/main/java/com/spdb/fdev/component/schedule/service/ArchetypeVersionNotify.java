package com.spdb.fdev.component.schedule.service;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.entity.*;
import com.spdb.fdev.component.service.*;
import com.spdb.fdev.transport.RestTransport;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RefreshScope
public class ArchetypeVersionNotify {
    private static final Logger logger = LoggerFactory.getLogger(ArchetypeVersionNotify.class);

    @Autowired
    private IArchetypeInfoService archetypeInfoService;

    @Autowired
    private IArchetypeRecordService archetypeRecordService;

    @Autowired
    private IComponentInfoService componentInfoService;

    @Autowired
    private IComponentRecordService componentRecordService;

    @Autowired
    private IComponentArchetypeService componentArchetypeService;

    @Autowired
    private IMailService mailService;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private IArchetypeScanService archetypeScanService;

    @Value("${history.archetype.url}")
    private String history_archetype_url;

    /**
     * 检查骨架使用组件是否最新版本，如果不是发送邮件通知更新
     *
     * @throws Exception
     */
    @Async
    public void checkComponent() throws Exception {
        List<ComponentInfo> componentInfoList = componentInfoService.query(new ComponentInfo());
        //封装Map，key为组件id，value为应用英文名
        Map<String, String> componentIdMap = componentInfoList.stream().collect(Collectors.toMap(e -> e.getId(), e -> e.getName_en()));
        //封装Map，key为组件id，value为推荐版本
        Map<String, String> componentMap = new HashMap();
        for (ComponentInfo componentInfo : componentInfoList) {
            //查询组件推荐版本
            ComponentRecord componentRecord = componentRecordService.queryByComponentIdAndType(componentInfo.getId(), Constants.RECORD_RECOMMEND_TYPE);
            if (null != componentRecord) {
                componentMap.put(componentInfo.getId(), componentRecord.getVersion());
            }
        }
        List<ArchetypeInfo> archetypeInfoList = archetypeInfoService.query(new ArchetypeInfo());
        //获取骨架使用组件详情
        if (!CommonUtils.isNullOrEmpty(archetypeInfoList)) {
            JSONObject mailJson = new JSONObject();
            for (ArchetypeInfo archetypeInfo : archetypeInfoList) {
                if (Constants.VUE_LOWCASE.equals(archetypeInfo.getType()))
                    continue;
                List<JSONObject> result = null;
                //查询骨架推荐版本
                ArchetypeRecord archetypeRecord = archetypeRecordService.queryByArchetypeIdAndType(archetypeInfo.getId(), Constants.RECORD_RECOMMEND_TYPE);
                if (archetypeRecord != null) {
                    //查询属于这个推荐版本的所有组件关联信息
                    List<ComponentArchetype> componentArchetypeList =
                            componentArchetypeService.queryByArchetypeIdAndVersion(archetypeRecord.getArchetype_id(), archetypeRecord.getVersion());
                    if (!CommonUtils.isNullOrEmpty(componentArchetypeList)) {
                        result = new ArrayList<>();
                        for (ComponentArchetype componentArchetype : componentArchetypeList) {
                            //如果当前组件推荐版本不为空且不等于骨架使用组件版本，进行记录
                            if (StringUtils.isNotBlank(componentMap.get(componentArchetype.getComponent_id()))
                                    && !componentArchetype.getComponent_version().equals(componentMap.get(componentArchetype.getComponent_id()))) {
                                JSONObject versionJson = new JSONObject();
                                versionJson.put(Dict.CurrnetVersion, componentArchetype.getComponent_version());
                                versionJson.put(Dict.RecommendVersion, componentMap.get(componentArchetype.getComponent_id()));
                                JSONObject returnJson = new JSONObject();
                                returnJson.put(componentIdMap.get(componentArchetype.getComponent_id()), versionJson);
                                result.add(returnJson);
                            }
                        }
                    }
                }
                if (!CommonUtils.isNullOrEmpty(result))
                    mailJson.put(archetypeInfo.getName_en(), result);
            }
            sendNotify(mailJson);
        }

    }

    /**
     * 邮件发送
     *
     * @param param
     * @throws Exception
     */
    private void sendNotify(JSONObject param) throws Exception {
        Iterator<String> keys = param.keys();
        while (keys.hasNext()) {
            String name_en = keys.next();
            ArchetypeInfo archetypeInfo = archetypeInfoService.queryByNameEn(name_en);
            //1.拼接邮件内容
            HashMap hashMap = new HashMap();
            hashMap.put(Dict.NAME_EN, name_en);
            hashMap.put(Dict.HISTORY_ARCHETYPE_URL, history_archetype_url + archetypeInfo.getId());
            StringBuffer componentVesionNofity = new StringBuffer();
            JSONArray components = param.getJSONArray(name_en);
            if (!CommonUtils.isNullOrEmpty(components)) {
                for (int i = 0; i < components.size(); i++) {
                    JSONObject component = components.getJSONObject(i);
                    Iterator<String> componentKeys = component.keys();
                    while (componentKeys.hasNext()) {
                        String componentNameEn = componentKeys.next();
                        JSONObject version = component.getJSONObject(componentNameEn);
                        String currnetVersion = version.getString(Dict.CurrnetVersion);
                        String recommendVersion = version.getString(Dict.RecommendVersion);
                        componentVesionNofity.append("组件：" + componentNameEn + "， 当前版本：" + currnetVersion + "，最新推荐版本：" + recommendVersion + "\n    ");
                    }
                }
            }
            hashMap.put(Dict.COMPONENT_VESION_NOFITY, componentVesionNofity);
            String mailContent = mailService.archetypeNotifyContent(hashMap);
            //2. 获取所有使用此组件的负责人
            Set<String> users = new HashSet<>();
            users.addAll(CommonUtils.getManageIds(archetypeInfo.getManager_id()));
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
//            email_receivers.clear();
//            email_receivers.add("xxx");
            //4.发送邮件
            HashMap<String, String> sendMap = new HashMap();
            sendMap.put(Dict.EMAIL_CONTENT, mailContent);
            String topic = "骨架组件升级提醒";
            mailService.sendEmail(topic, Dict.FCOMPONENT_ARCHETYPE_NOTIFY, sendMap, email_receivers.toArray(new String[email_receivers.size()]));
        }
    }


    /**
     * 更新最新骨架使用的组件信息
     */
    @Async
    public void archetypeAutoScan() {
        try {
            List<ArchetypeInfo> archetypeInfoList = archetypeInfoService.query(new ArchetypeInfo());
            //获取骨架使用组件详情
            if (!CommonUtils.isNullOrEmpty(archetypeInfoList)) {
                for (ArchetypeInfo archetypeInfo : archetypeInfoList) {
                    if (Constants.VUE_LOWCASE.equals(archetypeInfo.getType()))
                        continue;
                    //查询骨架推荐版本
                    ArchetypeRecord archetypeRecord = archetypeRecordService.queryByArchetypeIdAndType(archetypeInfo.getId(), Constants.RECORD_RECOMMEND_TYPE);
                    if (archetypeRecord != null) {
                        archetypeScanService.scanComponentArchetype(archetypeRecord.getArchetype_id(), archetypeRecord.getVersion());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("更新最新骨架使用的组件信息失败,{}", e.getMessage());
        }
    }
}