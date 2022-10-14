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
import com.spdb.fdev.spdb.entity.CaasDeployment;
import com.spdb.fdev.spdb.service.ICaasDeploymentService;
import com.spdb.fdev.spdb.service.ICaasPodService;
import com.spdb.fdev.spdb.service.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author:guanz2
 * @Date:2021/10/1-19:27
 * @Description: caas_deployment service层 实现类
 */
@Component
public class CaasDeploymentServiceImpl implements ICaasDeploymentService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private ICaasDeploymentDao caasDeploymentDao;

    @Autowired
    private ICaasPodDao caasPodDao;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private ICaasPodService caasPodService;

    @Value("${bk_app_secret}")
    private String bk_app_secret;
    @Value("${searchInst}")
    private String searchInst;
    @Value("${findInstanceAssociation}")
    private String findInstanceAssociation;
    @Value("${caasNamespaceList}")
    private String caasNamespaceList;
    @Value("${caasDeprecatedDeployment}")
    private String caasDeprecatedDeployment;
    @Value("${mailReceiver}")
    private String mailReceiver;

    private Logger logger = LoggerFactory.getLogger(CaasDeploymentServiceImpl.class);

    private Set<String> deployment_set = new HashSet<>();

    /*
    * @author:guanz2
    * @Description:手动拉取蓝鲸deployment信息
    */
    @Override
    public void pullBluekingDataByManual() throws Exception {
        logger.info("caas---开始手动拉取数据");
        this.pullCaasDeploymentFromBlueking();
        if(!CommonUtils.isNullOrEmpty(this.deployment_set)){
            this.caasPodService.pullCaasPodFromBlueking(this.deployment_set);
        }
    }

    //自动获取生产数据
    @Scheduled(cron = "${update.caasblueking.info.cron}")
    public void updateBluekingByAuto(){
        logger.info("开始自动获取生产数据");
        try {
            this.pullCaasDeploymentFromBlueking();
            if(!CommonUtils.isNullOrEmpty(this.deployment_set)){
                this.caasPodService.pullCaasPodFromBlueking(this.deployment_set);
            }
        } catch (Exception e) {
            logger.error("定时获取生产数据失败");
            return;
        }
        logger.info("定时获取生产数据成功");
    }

    /*
    * @author:guanz2
    * @Description: 从蓝鲸拉取caas_deployment数据
    */
    @Override
    public void pullCaasDeploymentFromBlueking() throws Exception {
        logger.info("caas---deployment开始同步蓝鲸数据");
        //调用蓝鲸接口
        //获取deployment信息
        List deployments = this.deploymentData();
        if (!CommonUtils.isNullOrEmpty(deployments)){
            caasDeploymentDao.dropCollection(Dict.CAASDEPLOYMENT);
            logger.info("caas--- 删除数据库caas_deployments集合成功");
            caasDeploymentDao.addAll(deployments);
            logger.info("caas---deployment数据获取成功");
        }else {
            logger.info("caas---蓝鲸获取deployment数据为空，caas不做更新");
        }
    }


    /*
    * @author:guanz2
    * @Description: 获取deployment数据，存入caas_deployment
    * 返回值：所用deployement的名字，去重后
    */
    private List deploymentData(){
        Set<String> namespace_set = new HashSet<>();
        Set<String> deprecatedDeployment_set = new HashSet<>();
        Map<String, Object> findDeploymentMap = baseParams();
        findDeploymentMap.put(Dict.BK_OBJ_ID, Dict.DEPLOYMENT);
        List<CaasDeployment> caasDeployments = new ArrayList<>();
        try {
            Map responseMap = this.restTemplate.postForObject(searchInst, findDeploymentMap, Map.class, new Object[0]);
            Map data = (Map) responseMap.get(Dict.DATA);
            int count = (int) data.get(Dict.COUNT);
            List<Map> infoMap = (List) data.get(Dict.INFO);

            //获取指定租户下的应用,去除废弃的应用
            namespace_set = CommonUtils.byteToSet(caasNamespaceList);
            deprecatedDeployment_set = CommonUtils.byteToSet(caasDeprecatedDeployment);
            for(int j = 0; j < count; j++){
                if(namespace_set.contains(infoMap.get(j).get(Dict.NAMESPACE)) &&
                        !deprecatedDeployment_set.contains(infoMap.get(j).get(Dict.DEPLOYMENT))){
                    //获取目标deployment
                    this.deployment_set.add((String) infoMap.get(j).get(Dict.DEPLOYMENT));
                    CaasDeployment caasDeployment = new CaasDeployment();
                    Map<String, Object> caasDeploymentMap = JSON.parseObject(JSON.toJSONString(caasDeployment, SerializerFeature.WriteMapNullValue, SerializerFeature.QuoteFieldNames), Map.class);
                    for(String key : caasDeploymentMap.keySet()){
                        caasDeploymentMap.put(key, infoMap.get(j).get(key));
                    }
                    //对日期进行格式转换
                    String lastdate = (String) caasDeploymentMap.get(Dict.LAST_MODIFIED_DATE);
                    if(lastdate != null){
                        caasDeploymentMap.put(Dict.LAST_MODIFIED_DATE, DateUtil.getDate(lastdate, DateUtil.DATE_UTC_FORMAT));
                    }
                    caasDeployments.add(JSON.parseObject(JSON.toJSONString(caasDeploymentMap),CaasDeployment.class));
                }
            }
            return  caasDeployments;
        } catch (Exception e) {
            sendEmail("获取网银系统群caas_deployment详细信息失败",e.getMessage());
            logger.error("获取网银系统群caas_deployment详细信息失败" + e.getMessage());
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
