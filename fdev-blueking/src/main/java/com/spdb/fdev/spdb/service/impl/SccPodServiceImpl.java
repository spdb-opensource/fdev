package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.util.CommonUtils;
import com.spdb.fdev.base.util.DateUtil;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.spdb.dao.ISccPodDao;
import com.spdb.fdev.spdb.entity.SccPod;
import com.spdb.fdev.spdb.service.IEmailService;
import com.spdb.fdev.spdb.service.ISccPodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author:guanz2
 * @Date:2021/10/5-13:47
 * @Description: scc_pod service 实现类
 */
@Component
public class SccPodServiceImpl implements ISccPodService {

    @Autowired
    RestTemplate restTemplate;

    @Value("${bk_app_secret}")
    private String bk_app_secret;

    @Value("${searchInst}")
    private String searchInst;

    @Value("${mailReceiver}")
    private String mailReceiver;

    @Autowired
    private IEmailService emailService;




    @Autowired
    private ISccPodDao sccPodDao;

    private Logger logger = LoggerFactory.getLogger(SccPodServiceImpl.class);


    @Override
    public void pullSccPodFromBlueking(Set<String> deploy_set) throws Exception {
        logger.info("scc---pod开始同步蓝鲸数据");
        List pods = this.SccPod(deploy_set);
        if (!CommonUtils.isNullOrEmpty(pods)){
            sccPodDao.dropCollection(Dict.SCCPOD);
            logger.info("scc---删除scc_pod集合成功");
            sccPodDao.addAll(pods);
            logger.info("scc---scc_pod数据获取成功");
        }else {
            logger.info("scc---蓝鲸获取pod数据为空，pod不更新");
        }

    }

    private List SccPod(Set<String> deploy_set){
        Map params = this.baseParams();
        params.put(Dict.BK_OBJ_ID, Dict.PODSCC);
        List<SccPod> sccPods = new ArrayList<>();
        try {
            Map<String, Object> conditionMap = new HashMap<>();
            List<Map> podList = new ArrayList<>();
            Map<String, Object> podFieldMap = new HashMap<>();


            podFieldMap.put(Dict.FIELD, Dict.OWNERCODE);
            podFieldMap.put(Dict.OPERATOR, "$in");
            podFieldMap.put(Dict.VALUE, deploy_set);
            podList.add(podFieldMap);
            conditionMap.put(Dict.PODSCC, podList);
            params.put(Dict.CONDITION, conditionMap);

            Map response = this.restTemplate.postForObject(searchInst, params, Map.class, new Object[0]);

            Map data = (Map) response.get(Dict.DATA);
            int count = (int) data.get(Dict.COUNT);
            List<Map> infoMap = (List<Map>) data.get(Dict.INFO);
            for (int i = 0; i < count; i++){
                SccPod sccPod = new SccPod();
                sccPod.setCluster_code((String) infoMap.get(i).get("cluster_code"));
                sccPod.setNamespace_code((String) infoMap.get(i).get("namespace_code"));
                sccPod.setOwner_code((String) infoMap.get(i).get("owner_code"));
                sccPod.setPod_code((String) infoMap.get(i).get("pod_code"));
                sccPod.setNode_name((String) infoMap.get(i).get("node_name"));
                sccPod.setPod_ip((String) infoMap.get(i).get("pod_ip"));

                //时间转换
                String last_modified_date = (String) infoMap.get(i).get("last_modified_date");
                if(last_modified_date != null){
                    sccPod.setLast_modified_date(DateUtil.getDate(last_modified_date, DateUtil.DATE_UTC_FORMAT));
                }

                sccPods.add(sccPod);
            }
            //sccPodDao.addAll(sccPods);
            return sccPods;
        }   catch (Exception e){
            sendEmail("获取网银系统群scc_pod详细信息失败",e.getMessage());
            logger.error("获取网银系统群scc_pod详细信息失败" + e.getMessage());
            throw new FdevException(ErrorConstants.BLUEKING_POST_ERROR);
        }

    }

    private Map<String, Object> baseParams() {
        Map<String, Object> baseParams = new HashMap<>();
        baseParams.put("bk_app_code", "cc-automation_saas");
        baseParams.put("bk_app_secret", bk_app_secret);
        baseParams.put("bk_username", "admin");
        baseParams.put("bk_supplier_account", "0");
        return baseParams;
    }

    public void sendEmail(String subject,String content) {
        String[] receiver = mailReceiver.split(";");
        List<String> to = Arrays.stream(receiver).collect(Collectors.toList());
        if(CommonUtils.isNullOrEmpty(content)) {
            content = "java.lang.NullPointerException,空指针异常！";
        }
        content = subject + "		" + content;
        emailService.sendEmail(subject, content, to);
    }
}
