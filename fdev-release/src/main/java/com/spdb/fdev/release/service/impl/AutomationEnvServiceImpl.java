package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IAutomationEnvDao;
import com.spdb.fdev.release.entity.AutomationEnv;
import com.spdb.fdev.release.service.IAppService;
import com.spdb.fdev.release.service.IAutomationEnvService;
import com.spdb.fdev.release.service.IRoleService;
import com.spdb.fdev.transport.RestTransport;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;
@RefreshScope
@Service
public class AutomationEnvServiceImpl implements IAutomationEnvService, InitializingBean {

    @Autowired
    private IAutomationEnvDao automationParamDao;

    @Autowired
    private IAppService appService;

    @Value("${fdev.sccDir.key.mapping}")
    private String sccDir;

    @Value("${fdev.dockerDir.key.mapping}")
    private String dockerDir;

    private Map<String, Object> sccDirMap;

    private Map<String, Object> dockerDirMap;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IAutomationEnvDao automationEnvDao;

    private static final Logger logger = LoggerFactory.getLogger(AutomationEnvServiceImpl.class);

    @Override
    public List<AutomationEnv> query() {
        return automationParamDao.query();
    }

    @Override
    public void addAutomationEnv(Map<String, Object> requestParam) throws Exception {
        if (!roleService.isReleaseManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        ObjectId id = new ObjectId();
        String env_name = (String) requestParam.get(Dict.ENV_NAME);
        String description = (String) requestParam.get(Dict.DESCRIPTION);
        List<String> platformList = (List<String>) requestParam.get("platform"); //部署平台:CAAS SCC
        Map<String,Object> fdevEnvMap = setFdevEnv(requestParam);
        try {
            Map<String,Object> fdev_env_name = new HashMap<>();
            Map<String, Object> scc_fdev_env_name = new HashMap<>();
            if(!CommonUtils.isNullOrEmpty(fdevEnvMap)){
                fdev_env_name = (Map<String, Object>) fdevEnvMap.get("fdev_env_name");
                scc_fdev_env_name = (Map<String, Object>) fdevEnvMap.get("scc_fdev_env_name");
            }
            automationEnvDao.save(new AutomationEnv(id, id.toString(), env_name, fdev_env_name, scc_fdev_env_name, description, platformList));
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{env_name + "已存在！"});
        }
    }

    @Override
    public void update(Map<String, Object> requestParam) throws Exception {
        if (!roleService.isReleaseManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR);
        }
        String id = (String) requestParam.get(Dict.ID);
        String env_name = (String) requestParam.get(Dict.ENV_NAME);
        String description = (String) requestParam.get(Dict.DESCRIPTION);
        List<String> platformList = (List<String>) requestParam.get("platform"); //部署平台:CAAS SCC
        Map<String,Object> fdevEnvMap = setFdevEnv(requestParam);
        Map<String,Object> fdev_env = new HashMap<>();
        Map<String, Object> scc_fdev_env = new HashMap<>();
        if(!CommonUtils.isNullOrEmpty(fdevEnvMap)){
            fdev_env = (Map<String, Object>) fdevEnvMap.get("fdev_env_name");
            scc_fdev_env = (Map<String, Object>) fdevEnvMap.get("scc_fdev_env_name");
        }
        automationEnvDao.update(new AutomationEnv(id, env_name, fdev_env, scc_fdev_env, description, platformList));
    }

    /**
     * 设置不同部署平台，不同变更类型及不同网段的fdev环境
     * */
    private Map<String,Object> setFdevEnv(Map<String,Object> requestParam){

        Map<String, Object> result = new HashMap<>();
        // caas平台
        String gray_dmz_caas = (String) requestParam.get("gray_dmz_caas");
        String gray_biz_caas = (String) requestParam.get("gray_biz_caas");
        String pro_dmz_caas = (String) requestParam.get("pro_dmz_caas");
        String pro_biz_caas = (String) requestParam.get("pro_biz_caas");

        Map<String, Object> caas_gray = new HashMap<>();
        caas_gray.put("dmz", gray_dmz_caas);
        caas_gray.put("biz", gray_biz_caas);

        Map<String, Object> caas_pro = new HashMap<>();
        caas_pro.put("dmz", pro_dmz_caas);
        caas_pro.put("biz", pro_biz_caas);

        Map<String, Object> caas = new HashMap<>();
        caas.put("gray", caas_gray);
        caas.put("proc", caas_pro);

        result.put("fdev_env_name" , caas);

        // scc平台
        String gray_dmz_scc = (String) requestParam.get("gray_dmz_scc");
        String gray_biz_scc = (String) requestParam.get("gray_biz_scc");
        String pro_dmz_scc = (String) requestParam.get("pro_dmz_scc");
        String pro_biz_scc = (String) requestParam.get("pro_biz_scc");

        Map<String, Object> scc_gray = new HashMap<>();
        scc_gray.put("dmz", gray_dmz_scc);
        scc_gray.put("biz", gray_biz_scc);

        Map<String, Object> scc_pro = new HashMap<>();
        scc_pro.put("dmz", pro_dmz_scc);
        scc_pro.put("biz", pro_biz_scc);

        Map<String, Object> scc = new HashMap<>();
        scc.put("gray", scc_gray);
        scc.put("proc", scc_pro);

        result.put("scc_fdev_env_name" , scc);
        return result;
    }

    @Override
    public void deleteById(String id) {
        automationParamDao.deleteById(id);
    }

    @Override
    public Map<String, Object> queryEnvParams(Map<String,Object> req) throws Exception {
        String applicationId = (String) req.get(Dict.APPLICATION_ID);
        String gitlabId = (String) req.get(Dict.GITLABID); // 应用id，gitlabid任填其一
        String content = (String) req.get(Dict.CONTENT); // 目录 docker、docker_all、scc、scc_all
//        List<String> envList = (List<String>) req.get(Dict.ENV_NAME); //环境 /PROC/SHK1
//        String fdevEnv = String.join(",", envList);
        String fdevEnv = (String) req.get(Dict.ENV_NAME);
        // 1.获取指定目录下变更类型网段的环境英文名List<String> scc-pro-uz11
        String content_copy = content;
        if(content_copy.indexOf("_") != -1){
            content_copy = content_copy.split("_")[0];
        }
        // 2.获取属性名 sccdeploy_namespace
        Map<String,Object> contentKeysMapping = "scc".equals(content_copy)? this.sccDirMap : this.dockerDirMap;
        List<String> keys = (List<String>) contentKeysMapping.get(content);
        // 3.循环调环境配置模块接口，获取指定环境对应的属性的属性值
        return appService.getEnvValues(gitlabId,applicationId,fdevEnv,keys);


    }

    @Override
    public void afterPropertiesSet() {
        this.sccDirMap = JSONObject.fromObject(this.sccDir);
        this.dockerDirMap = JSONObject.fromObject(this.dockerDir);
    }
}
