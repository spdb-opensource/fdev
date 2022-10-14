package com.spdb.fdev.fdevenvconfig.spdb.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevenvconfig.base.CommonUtils;
import com.spdb.fdev.fdevenvconfig.base.CommonValidate;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.base.dict.ErrorConstants;
import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import com.spdb.fdev.fdevenvconfig.base.utils.ServiceUtil;
import com.spdb.fdev.fdevenvconfig.spdb.dao.AppEnvMappingDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.ICommonDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IEnvironmentDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelEnvDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.AppEnvMapping;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Environment;
import com.spdb.fdev.fdevenvconfig.spdb.service.IEnvironmentService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IVerifyCodeService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IlabelService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author xxx
 * @date 2019/7/5 13:16
 */
@Service
@RefreshScope
public class EnvironmentServiceImpl implements IEnvironmentService {

    @Value("${record.switch}")
    private Boolean recordSwitch;
    @Value("${env.sort}")
    private List<String> envSort;
    @Value("${env.label}")
    private List<String> envData;
    @Autowired
    private IEnvironmentDao environmentDao;
    @Autowired
    private ICommonDao commonDao;
    @Autowired
    private IModelEnvDao modelEnvDao;
    @Autowired
    private ServiceUtil serviceUtil;
    @Autowired
    private IVerifyCodeService verifyCodeService;
    @Autowired
    private IlabelService labelService;
    @Autowired
    private AppEnvMappingDao appEnvMappingDao;
    @Autowired
    public UserVerifyUtil userVerifyUtil;

    private Logger logger = LoggerFactory.getLogger(EnvironmentServiceImpl.class);

    @Override
    public List<Environment> query(Environment environment) {
        environment.setStatus(Constants.STATUS_OPEN);
        List<Environment> queryEnv = this.environmentDao.query(environment);
        List<Environment> result = new ArrayList<>();
        if (envSort.isEmpty()) {
            return queryEnv;
        }
        for (String envLabel : envSort) {
            Iterator<Environment> iterator = queryEnv.iterator();
            while (iterator.hasNext()) {
                Environment env = iterator.next();
                List<String> labels = env.getLabels();
                if (labels.contains(envLabel)) {
                    result.add(env);
                    iterator.remove();
                }
            }
        }
        result.addAll(queryEnv);
        return result;
    }

    @Override
    public Environment save(Environment environment) throws Exception {
        // 对环境英文名遵循正则
        boolean flag = Pattern.matches(Constants.PATTERN_ENV_NAME_EN, environment.getName_en());
        if (!flag) {
            String errorMsg = "环境英文名只能字母开头且只能是字母、数字、特殊字符.-_的结合";
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{errorMsg});
        }
        CommonValidate.validateRepeatParam(environment, Constants.OR, new String[]{Constants.NAME_EN, Constants.NAME_CN}, Environment.class,
                commonDao);
        if (this.recordSwitch) {
            User user = userVerifyUtil.getRedisUser();
            environment.setOpno(user.getId());
        }
        if (CommonUtils.isNullOrEmpty(environment.getLabels())) {
            environment.setLabels(new ArrayList<String>());
        }
        environment.setCtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        environment.setStatus(Constants.STATUS_OPEN);
        Environment saveEnv = this.environmentDao.save(environment);
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("@@@@@@ 创建环境 成功{}", objectMapper.writeValueAsString(saveEnv));
        return saveEnv;
    }

    @Override
    public void delete(Map map) throws Exception {
        String verfityCode = (String) map.get(Dict.VERFITYCODE);
        // 校验验证码
        verifyCodeService.checkVerifyCode(verfityCode);
        String id = (String) map.get(Dict.ID);
        String opno = serviceUtil.getOpno();
        this.environmentDao.delete(id, opno);
        logger.info("@@@@@@ 删除环境 成功");
        this.modelEnvDao.deleteModelEnv(Constants.ENV_ID, id, serviceUtil.getOpno());
        logger.info("@@@@@@ 环境对应的实体映射删除 成功");
    }

    @Override
    public Environment update(Environment environment) throws Exception {
        // 对环境英文名遵循正则
        boolean flag = Pattern.matches(Constants.PATTERN_ENV_NAME_EN, environment.getName_en());
        if (!flag) {
            String errorMsg = "环境英文名只能字母开头且只能是字母、数字、特殊字符.-_的结合";
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{errorMsg});
        }
        CommonValidate.validateRepeatParamForUpdate(environment, Constants.OR, new String[]{Constants.NAME_EN, Constants.NAME_CN},
                Environment.class, commonDao);
        if (this.recordSwitch) {
            User user = userVerifyUtil.getRedisUser();
            environment.setOpno(user.getId());
        }
        if (CommonUtils.isNullOrEmpty(environment.getLabels())) {
            environment.setLabels(new ArrayList<String>());
        }
        environment.setUtime(DateUtil.getDate(new Date(), DateUtil.DATETIME_COMPACT_FORMAT));
        Environment updateEnv = this.environmentDao.update(environment);
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("@@@@@@ 修改环境 成功{}", objectMapper.writeValueAsString(updateEnv));
        return updateEnv;
    }

    @Override
    public Environment queryById(Environment environment) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Environment queryById = this.environmentDao.queryById(environment);
        logger.info("@@@@@@ 根据环境ID查询环境 成功{}", objectMapper.writeValueAsString(queryById));
        return queryById;
    }

    @Override
    public Environment queryById(String id) {
        return environmentDao.queryById(id);
    }

    @Override
    public List<Environment> queryByLabels(Environment environment) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Environment> environments = this.environmentDao.getByLabels(environment.getLabels(), Constants.STATUS_OPEN);
        logger.info("@@@@@@ 根据环境标签查询环境 成功{}", objectMapper.writeValueAsString(environments));
        return environments;
    }

    @Override
    public List<Environment> queryAutoEnv(Environment environment) throws Exception {
        List<Environment> list = this.environmentDao.queryAutoEnv(environment);
        return list;


    }

    @Override
    public List<String> getEnvNameByLabels(String label) {
        List<String> labels = new ArrayList<>();
        labels.add(label);
        List<Environment> environments = environmentDao.queryByLabels(labels, Constants.ONE);
        return environments.stream().map(Environment::getName_en).collect(Collectors.toList());
    }

    @Override
    public List<Environment> getEnvByLabels(List<String> labels) {
        return environmentDao.queryByLabels(labels, Constants.ONE);
    }

    @Override
    public Environment queryByNameEn(String envNameEn) {
        return this.environmentDao.queryByNameEn(envNameEn);
    }

    @Override
    public List<Environment> queryByLabelsFuzzy(Map<String, Object> requestParamMap) {
        List<String> labels = (List<String>) requestParamMap.get(Constants.LABELS);
        Map<String, Object> labelsMap = labelService.queryAllLabels();
        List<String> netWorkLabels = (List<String>) labelsMap.get(Dict.NETWORK);
        List<String> envLabelsParam = new ArrayList<>();
        List<String> networkLabelsParam = new ArrayList<>();
        for (String label : labels) {
            if (netWorkLabels.contains(label)) {
                networkLabelsParam.add(label);
            } else {
                envLabelsParam.add(label);
            }
        }
        List<Environment> environmentList = new ArrayList<>();
        if (CollectionUtils.isEmpty(networkLabelsParam)) {
            return environmentDao.queryByLabelsFuzzy(labels);
        }
        for (String netWorkLabel : networkLabelsParam) {
            List<String> labelsParam = new ArrayList<>();
            labelsParam.addAll(envLabelsParam);
            labelsParam.add(netWorkLabel);
            labelsParam.add("caas");
            List<Environment> environments = environmentDao.queryByLabelsFuzzy(labelsParam);
            environmentList.addAll(environments);
        }
        return environmentList;
    }

    @Override
    public List<Environment> queryByLabelsFuzzy(List<String> labels) {
        Map<String, Object> labelsMap = labelService.queryAllLabels();
        List<String> netWorkLabels = (List<String>) labelsMap.get(Dict.NETWORK);
        List<String> envLabels = (List<String>) labelsMap.get(Dict.ENV);
        List<String> envLabelsParam = new ArrayList<>();
        List<String> networkLabelsParam = new ArrayList<>();
        for (String label : labels) {
            if (netWorkLabels.contains(label)) {
                networkLabelsParam.add(label);
            } else if (envLabels.contains(label)) {
                envLabelsParam.add(label);
            }
        }
        List<Environment> environmentList = new ArrayList<>();
        for (String netWorkLabel : networkLabelsParam) {
            for (String envLabel : envLabelsParam) {
                List<String> labelsParam = new ArrayList<>();
                labelsParam.add(envLabel);
                labelsParam.add(netWorkLabel);
                labelsParam.add("caas");
                List<Environment> environments = environmentDao.queryByLabelsFuzzy(labelsParam);
                environmentList.addAll(environments);
            }
        }
        return environmentList;
    }

    @Override
    public Environment queryByNameCN(String env_name) {
        return this.environmentDao.queryByNameCN(env_name);
    }


    @Override
    public List<Environment> getEnvs(List<String> labels) {
        List<Environment> environmentList;
        if (CollectionUtils.isEmpty(labels)) {
            environmentList = this.query(new Environment());
        } else {
            environmentList = environmentDao.queryByLabelsFuzzy(labels);
        }
        return environmentList;
    }

    @Override
    public List<Environment> queryEnvByAppId(Map<String, String> map) {
        List<AppEnvMapping> appEnvMappingList = appEnvMappingDao.queryEnvByAppId(map.get(Dict.APPID));
        List<Environment> envList = new ArrayList<>();
        for (AppEnvMapping appEnvMapping : appEnvMappingList) {
            String network = appEnvMapping.getNetwork();
            if (!StringUtils.isEmpty(appEnvMapping.getEnv_id())) {

                // 组装生产环境
                Environment proEnv = this.queryById(appEnvMapping.getEnv_id());
                envList.add(proEnv);
            } else if (!StringUtils.isEmpty(network)) {

                // 组装测试环境
                String[] networks = network.split(",");
                List<String> tags = appEnvMapping.getTags();
                tags.add(Dict.DEV);
                Collections.addAll(tags, networks);
                envList.addAll(this.queryByLabelsFuzzy(tags));
            } else {

                //组装scc环境
                String envId = null;
                if(!StringUtils.isEmpty(appEnvMapping.getScc_sit_id())){
                    envId = appEnvMapping.getScc_sit_id();
                }else if(!StringUtils.isEmpty(appEnvMapping.getScc_uat_id())){
                    envId = appEnvMapping.getScc_uat_id();
                } else if(!StringUtils.isEmpty(appEnvMapping.getScc_rel_id())){
                    envId = appEnvMapping.getScc_rel_id();
                }else if(!StringUtils.isEmpty(appEnvMapping.getScc_pro_id())){
                    envId = appEnvMapping.getScc_pro_id();
                }
                if(envId!=null){
                    Environment sccEnv = this.queryById(envId);
                    envList.add(sccEnv);
                }
            }
        }
        List<Environment> result = new ArrayList<>();
        List<Environment> lableData = this.environmentDao.queryByLabels(envData, Constants.ONE);
        envList.addAll(lableData);
        if (envSort.isEmpty()) {
            return envList;
        }
        for (String envLabel : envSort) {
            Iterator<Environment> iterator = envList.iterator();
            while (iterator.hasNext()) {
                Environment env = iterator.next();
                List<String> labels = env.getLabels();
                if (labels.contains(envLabel)) {
                    result.add(env);
                    iterator.remove();
                }
            }
        }
        
        result.addAll(envList);
        return result;
    }

    @Override
    public List<Environment> querySccEnvByAppId(Map<String, String> map) throws Exception {
        List<AppEnvMapping> appEnvMappingList = appEnvMappingDao.queryEnvByAppId(map.get(Dict.APPID));
        List<Environment> envList = new ArrayList<>();
        for (AppEnvMapping appEnvMapping : appEnvMappingList) {
            String sccEnvId;
            if (map.get("deploy_env").equals("sit")) {
                sccEnvId = appEnvMapping.getScc_sit_id();
            } else if (map.get("deploy_env").equals("uat")) {
                sccEnvId = appEnvMapping.getScc_uat_id();
            } else if (map.get("deploy_env").equals("rel")) {
                sccEnvId = appEnvMapping.getScc_rel_id();
            } else if (map.get("deploy_env").equals("pro")) {
                sccEnvId = appEnvMapping.getScc_pro_id();
            } else {
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{map.get("deploy_env"), "不允许的部署环境"});
            }
            envList.add(environmentDao.queryById(sccEnvId));
        }
        return envList;
    }
}
