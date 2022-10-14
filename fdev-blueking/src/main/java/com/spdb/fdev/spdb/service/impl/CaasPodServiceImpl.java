package com.spdb.fdev.spdb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.util.CommonUtils;
import com.spdb.fdev.base.util.DateUtil;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.spdb.dao.ICaasDeploymentDao;
import com.spdb.fdev.spdb.dao.ICaasPodDao;
import com.spdb.fdev.spdb.entity.CaasPod;
import com.spdb.fdev.spdb.service.ICaasPodService;
import com.spdb.fdev.spdb.service.IEmailService;
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
 * @Date:2021/10/1-19:27
 * @Description:
 */
@Component
public class CaasPodServiceImpl implements ICaasPodService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private ICaasDeploymentDao caasDeploymentDao;

    @Autowired
    private ICaasPodDao caasPodDao;

    @Autowired
    private IEmailService emailService;

    @Value("${bk_app_secret}")
    private String bk_app_secret;
    @Value("${searchInst}")
    private String searchInst;
    @Value("${findInstanceAssociation}")
    private String findInstanceAssociation;
    @Value("${mailReceiver}")
    private String mailReceiver;

    private Logger logger = LoggerFactory.getLogger(CaasPodServiceImpl.class);


    @Override
    public void pullCaasPodFromBlueking(Set<String> deployment_set) throws Exception {
        logger.info("caas---pod开始同步蓝鲸数据");

        //调用蓝鲸接口
        //获取deployment信息
        List pods = this.podData(deployment_set);
        if (!CommonUtils.isNullOrEmpty(pods)){
            caasPodDao.dropCollection(Dict.CAASPOD);
            logger.info("caas--- 删除数据库caas_pod集合成功");
            caasPodDao.addAll(pods);
            logger.info("caas---pod数据获取成功");
        }else {
            logger.info("caas---蓝鲸获取pod数据为空，pod不更新");
        }
    }

    /*
     * @author:guanz2
     * @Description: 获取pod数据存入caas_pod
     * 参数：deployments数组
     * 返回值，所有与deployments关联的pod
     */
    private List podData(Set<String> deployment_set){
        Map<String, Object> findPodMap = baseParams();
        findPodMap.put(Dict.BK_OBJ_ID, Dict.POD);
        List<CaasPod> caasPods = new ArrayList<>();
        try{
            Map<String, Object> conditionMap = new HashMap<>();
            List<Map> podList = new ArrayList<>();
            Map<String, Object> podFieldMap = new HashMap<>();

            //获取需要的而应用列表,确定需要哪些deployment
            podFieldMap.put(Dict.FIELD, Dict.DEPLOYMENT);
            podFieldMap.put(Dict.OPERATOR, "$in");
            podFieldMap.put(Dict.VALUE, deployment_set);
            podList.add(podFieldMap);
            conditionMap.put(Dict.POD, podList);
            findPodMap.put(Dict.CONDITION, conditionMap);

            Map responseMap = this.restTemplate.postForObject(searchInst, findPodMap, Map.class, new Object[0]);
            Map data = (Map) responseMap.get(Dict.DATA);
            int count = (int) data.get(Dict.COUNT);
            List<Map> infoMap = (List) data.get(Dict.INFO);
            for(int j = 0; j< count ; j++){
                CaasPod caasPod = new CaasPod();
                Map<String, Object> caasPodMap = JSON.parseObject(JSON.toJSONString(caasPod, SerializerFeature.WriteMapNullValue, SerializerFeature.QuoteFieldNames), Map.class);
                for(String key : caasPodMap.keySet()){
                    caasPodMap.put(key, infoMap.get(j).get(key));
                }
                //对日期进行格式转换
                String last_modified_date  = (String) caasPodMap.get(Dict.LAST_MODIFIED_DATE);
                if(last_modified_date != null){
                    caasPodMap.put(Dict.LAST_MODIFIED_DATE, DateUtil.getDate(last_modified_date, DateUtil.DATE_UTC_FORMAT));
                }
                caasPods.add(JSON.parseObject(JSON.toJSONString(caasPodMap),CaasPod.class));
            }
            return caasPods;
        } catch (Exception e){
            sendEmail("获取网银系统群caas_pod详细信息失败",e.getMessage());
            logger.error("获取网银系统群caas_pod详细信息失败" + e.getMessage());
            throw new FdevException(ErrorConstants.BLUEKING_POST_ERROR);
        }
    }

    //连接蓝鲸接口所需参数
    private Map<String, Object> baseParams() {
        Map<String, Object> baseParams = new HashMap<>();
        baseParams.put("bk_app_code", "cc-automation_saas");
        baseParams.put("bk_app_secret", bk_app_secret);
        baseParams.put("bk_username", "admin");
        baseParams.put("bk_supplier_account", "0");
        return baseParams;
    }

    /*
     * @author:guanz2
     * @Description:蓝鲸信息拉取信息失败，发送邮件
     */
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
