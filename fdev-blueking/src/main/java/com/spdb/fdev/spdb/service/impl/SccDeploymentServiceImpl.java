package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.util.CommonUtils;
import com.spdb.fdev.base.util.DateUtil;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.spdb.dao.ISccDeploymentDao;
import com.spdb.fdev.spdb.entity.SccDeploy;
import com.spdb.fdev.spdb.service.IEmailService;
import com.spdb.fdev.spdb.service.ISccDeploymentService;
import com.spdb.fdev.spdb.service.ISccPodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author:guanz2
 * @Date:2021/10/5-13:43
 * @Description: scc_deploy service 实现类
 */
@Component
public class SccDeploymentServiceImpl implements ISccDeploymentService {

    @Value("${bk_app_secret}")
    private String bk_app_secret;

    @Value("${searchInst}")
    private String searchInst;

    @Value("${sccNamespaceList}")
    private String sccNamespaceList;

    @Value("${sccDeprecatedDeployment}")
    private String sccDeprecatedDeployment;

    @Value("${mailReceiver}")
    private String mailReceiver;

    @Autowired
    private IEmailService emailService;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private ISccDeploymentDao sccDeployDao;

    @Autowired
    private ISccPodService sccPodService;

    private Logger logger = LoggerFactory.getLogger(SccDeploymentServiceImpl.class);

    private Set<String> deploy_set = new HashSet<>();

    @Override
    public void pullBluekingDataByManual() throws Exception {
        logger.info("scc---手动拉取数据开始");
        this.pullSccDeployFromBlueking();
        if(!CommonUtils.isNullOrEmpty(this.deploy_set)){
            this.sccPodService.pullSccPodFromBlueking(this.deploy_set);
        }
    }

    //自动获取生产数据
    @Scheduled(cron = "${update.sccblueking.info.cron}")
    public void updateBluekingByAuto(){
        logger.info("开始自动获取生产数据");
        try {
            this.pullSccDeployFromBlueking();
            if(!CommonUtils.isNullOrEmpty(this.deploy_set)){
                this.sccPodService.pullSccPodFromBlueking(this.deploy_set);
            }
        } catch (Exception e) {
            logger.error("定时获取生产数据失败");
            return;
        }
        logger.info("定时获取生产数据成功");
    }

    @Override
    public void pullSccDeployFromBlueking() throws Exception {
        logger.info("scc---deploy开始同步蓝鲸数据");
        List sccDeploys = this.SccDeploy();
        if (!CommonUtils.isNullOrEmpty(sccDeploys)){
            sccDeployDao.dropCollection(Dict.SCCDEPLOY);
            logger.info("scc---删除scc_deploy集合成功");
            sccDeployDao.addAll(sccDeploys);
            logger.info("scc---scc_deploy数据获取成功");
        }else {
            logger.info("scc---蓝鲸获取deploy数据为空，deploy不更新");
        }
    }

    private List SccDeploy(){
        Map<String, Object> params = baseParams();
        params.put(Dict.BK_OBJ_ID, Dict.DEPLOYSCC);
        List<SccDeploy> res = new ArrayList<>();
        try {
            Map response = this.restTemplate.postForObject(searchInst, params, Map.class, new Object[0]);
            Map data = (Map) response.get(Dict.DATA);
            int count = (int) data.get(Dict.COUNT);
            List<Map> infoMap = (List<Map>) data.get(Dict.INFO);
            Set<String> namespace_set = new HashSet<>();
            namespace_set = CommonUtils.byteToSet(sccNamespaceList);
            Set<String> deprecatedDeployment_set = new HashSet<>();
            deprecatedDeployment_set = CommonUtils.byteToSet(sccDeprecatedDeployment);
            Set nameSpaceCod_resourceCode = new TreeSet();
            for( int i =0 ; i < count; i++){
                if(namespace_set.contains(infoMap.get(i).get("namespace_code")) &&
                        !deprecatedDeployment_set.contains(infoMap.get(i).get("resource_code"))){
                    //获取目标deploy
                    this.deploy_set.add((String) infoMap.get(i).get("resource_code"));

                    SccDeploy sccDeploy = new SccDeploy();
                    sccDeploy.setNamespace_code((String) infoMap.get(i).get("namespace_code"));
                    sccDeploy.setResource_code((String) infoMap.get(i).get("resource_code"));
                    //对日期格式进行转换
                    String deploy_create_time = (String) infoMap.get(i).get("deploy_create_time");
                    if(deploy_create_time != null){
                        sccDeploy.setDeploy_create_time(deploy_create_time);
                    }
                    String last_modified_date = (String) infoMap.get(i).get("last_modified_date");
                    if(last_modified_date != null){
                        sccDeploy.setLast_modified_date(DateUtil.getDate(last_modified_date, DateUtil.DATE_UTC_FORMAT));
                    }
                    //拼接yaml
                    String yaml_content = (String) infoMap.get(i).get("yaml_content");
                    for (int j = 1; j <= 10; j++){
                        if(infoMap.get(i).get("yaml_content" + j) != null){
                            yaml_content = yaml_content + infoMap.get(i).get("yaml_content" + j);
                        }
                    }
                    sccDeploy.setYaml(yaml_content);
                    nameSpaceCod_resourceCode.add(sccDeploy.getNamespace_code()+"#"+sccDeploy.getResource_code());
                    res.add(sccDeploy);
                }
            }
            return res;
        }catch (Exception e){
            sendEmail("获取网银系统群scc_deploy详细信息失败",e.getMessage());
            logger.error("获取网银系统群scc_deploy详细信息失败" + e.getMessage());
            throw new FdevException(ErrorConstants.BLUEKING_POST_ERROR);
        }
    }

    @Override
    public  List<LinkedHashMap<String, Object>> getSccDeployCondition(String resource_code) throws Exception {
        List<SccDeploy> sccDeploys = new ArrayList<>();
        sccDeploys = this.sccDeployDao.getSccDeployCondition(resource_code);
        if(CommonUtils.isNullOrEmpty(sccDeploys)){
            return  null;
        }
        List<LinkedHashMap<String, Object>>  result = new ArrayList<>();
        for(int i = 0 ; i < sccDeploys.size() ; i++){
            LinkedHashMap temp = new LinkedHashMap();
            temp.put("namespace_code", sccDeploys.get(i).getNamespace_code());
            temp.put("resource_code", sccDeploys.get(i).getResource_code());
            String yaml = sccDeploys.get(i).getYaml();
            //将yaml字符串转换成json输出
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml wYaml = new Yaml();
            LinkedHashMap<String, Object> yamlMap = wYaml.load(yaml);
            temp.put("yaml", yamlMap);
            result.add(temp);
        }
        return  result;
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
