package com.spdb.fdev.release.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.release.dao.IProdAppScaleDao;
import com.spdb.fdev.release.entity.ProdAppScale;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.service.IAppService;
import com.spdb.fdev.release.service.IProdAppScaleService;
import com.spdb.fdev.release.service.IProdApplicationService;
import com.spdb.fdev.release.service.IProdRecordService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProdAppScaleServiceImpl implements IProdAppScaleService {
    @Autowired
    private IProdAppScaleDao prodAppScaleDao;
    @Autowired
    private IAppService appService;
    @Autowired
    private IProdRecordService prodRecordService;
    @Autowired
    private IProdApplicationService prodApplicationService;


    private Logger logger = LoggerFactory.getLogger(ProdAppScaleServiceImpl.class);

    @Override
    public ProdAppScale addAppScale(ProdAppScale prodAppScale) throws Exception {
        ProdAppScale saveProd = this.prodAppScaleDao.addAppScale(prodAppScale);
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("@@@@@@ 新增变更应用扩展 成功{}", objectMapper.writeValueAsString(saveProd));
        return saveProd;
    }

    @Override
    public void updateAppScale(Map<String, Object> map) throws Exception {
    	String prod_id = (String) map.get(Dict.PROD_ID);
        String application_id = (String) map.get(Dict.APPLICATION_ID);
        List<String> deploy_type = (List<String>) map.get("deploy_type");
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
    	HashSet<String> allDeployedType = prodApplicationService.findAllDeployedType(application_id, prodRecord); // 查询所有部署过的平台，未部署无法进行弹性扩展
        deploy_type.forEach(deploy -> {
        	if(!allDeployedType.contains(deploy)){
        		throw new FdevException(ErrorConstants.RELEASE_DEPLOY_TYPE_ERROR, new String[]{deploy, "不能对此应用进行弹性扩展！"});
        	}
        });
        List<Map<String,Object>> env_scales = (List<Map<String, Object>>) map.get(Dict.ENV_SCALES);
        this.prodAppScaleDao.updateAppScale(prod_id,application_id,deploy_type, env_scales);
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("@@@@@@ 修改变更应用扩展 成功{}", objectMapper.writeValueAsString(map));
    }

    @Override
    public void deleteAppScale(Map<String, Object> map) {
        this.prodAppScaleDao.deleteAppScale(map);
        logger.info("@@@@@@ 删除变更应用扩展成功");
    }

    @Override
    public List<Map<String, Object>> queryAppScale(Map<String, Object> map) throws Exception {
        List<ProdAppScale> prodAppScales = this.prodAppScaleDao.queryAppScale(map);
        List<Map<String, Object>> list = new ArrayList<>();
        for (ProdAppScale prodAppScale : prodAppScales) {
            Map<String, Object> param = new HashMap<>();
            param.put(Dict.APPLICATION_ID, prodAppScale.getApplication_id());
            //通过应用id查询应用名称
            Map<String, Object> appMap = appService.queryAPPbyid(prodAppScale.getApplication_id());
            param.put(Dict.APPLICATION_NAME_CN, appMap.get(Dict.NAME_ZH));
            param.put(Dict.APPLICATION_NAME_EN, appMap.get(Dict.NAME_EN));
            param.put(Dict.TYPE_NAME, appMap.get(Dict.TYPE_NAME));
            param.put(Dict.NETWORK, appMap.get(Dict.NETWORK)); // 网段 dmz、biz
            param.put(Dict.PROD_SEQUENCE, prodAppScale.getProd_sequence());
            param.put(Dict.TAG_NAME, prodAppScale.getTag_name());
            param.put(Dict.PRO_IMAGE_URI, prodAppScale.getPro_image_uri());
            param.put(Dict.PRO_SCC_IMAGE_URI, prodAppScale.getPro_scc_image_uri());
            param.put(Dict.ENV_SCALES, prodAppScale.getEnv_scales());
            param.put(Dict.DEPLOY_TYPE, prodAppScale.getDeploy_type());// 部署平台 CAAS、SCC
            list.add(param);
        }
        return list;
    }

}
