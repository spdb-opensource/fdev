package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.spdb.dao.IEnvDao;
import com.spdb.fdev.spdb.entity.Env;
import com.spdb.fdev.spdb.entity.EnvType;
import com.spdb.fdev.spdb.service.IEnvService;
import com.spdb.fdev.spdb.service.IRestService;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName EnvServiceImpl
 * @DESCRIPTION 环境实现类
 * @Author xxx
 * @Date 2021/5/8 14:07
 * @Version 1.0
 */
@Service
@RefreshScope
public class EnvServiceImpl implements IEnvService {

    @Autowired
    IEnvDao envDao;
    @Autowired
    private IRestService restService;
    @Autowired
    private UserVerifyUtil userVerifyUtil;
    @Value("${env.sort.rule}")
    private List<String> envSortRule;

    @Override
    public void add(Map<String, Object> req) throws Exception {
        List<Map<String, Object>> list = (List<Map<String, Object>>) req.get(Dict.DATA);
        List<Env> addEnvList = new ArrayList<>();
        if (!CommonUtil.isNullOrEmpty(list)) {
            List<Env> envList = this.queryList(new HashMap<String, Object>());
            //英文名集合
            List<String> nameEnList = new ArrayList<String>();
            //中文名集合
            List<String> nameCnList = new ArrayList<String>();
            if (!CommonUtil.isNullOrEmpty(envList)) {
                for (Env env : envList) {
                    nameEnList.add(env.getNameEn());
                    nameCnList.add(env.getNameCn());
                }
            }
            for (Map<String, Object> envMap : list) {
                Env addEnv = CommonUtil.map2Object(envMap, Env.class);
                if (CommonUtil.isNullOrEmpty(addEnv.getId())) {
                    ObjectId id = new ObjectId();
                    addEnv.set_id(id);
                    addEnv.setId(id.toString());
                }
                //判断是否重复
                if (nameEnList.contains(addEnv.getNameEn())) {
                    throw new FdevException(ErrorConstants.ENV_NAMEEN_ERROR, new String[]{addEnv.getNameEn()});
                }
                if (nameCnList.contains(addEnv.getNameCn())) {
                    throw new FdevException(ErrorConstants.ENV_NAMECN_ERROR, new String[]{addEnv.getNameCn()});
                }
                addEnvList.add(addEnv);
            }
            envDao.save(addEnvList);
        }
    }

    @Override
    public List<Env> queryList(Map<String, Object> req) throws Exception {
        List<Env> envList = envDao.queryList(req);
        List<Env> sortEnvList = new ArrayList<Env>();
        List<Env> sortEnvEndList = new ArrayList<Env>();
        //环境排序
        if (!CommonUtil.isNullOrEmpty(envList)) {
            for (String envSort : envSortRule) {
                for (Env env : envList) {
                    if (envSort.equals(env.getType())) {
                        sortEnvList.add(env);
                    } else if (!envSortRule.contains(env.getType())) {//配置文件不包含该环境排在最后
                        if (!sortEnvEndList.contains(env)) {
                            sortEnvEndList.add(env);
                        }
                    }
                }
            }
        }
        sortEnvList.addAll(sortEnvEndList);
        return sortEnvList;
    }

    @Override
    public void addEnvType(Map<String, Object> req) throws Exception {
        List<Map<String, Object>> list = (List<Map<String, Object>>) req.get(Dict.DATA);
        List<EnvType> addEnvTypeList = new ArrayList<>();
        for (Map<String, Object> envMap : list) {
            EnvType addEnvType = CommonUtil.map2Object(envMap, EnvType.class);
            if (CommonUtil.isNullOrEmpty(addEnvType.getId())) {
                ObjectId id = new ObjectId();
                addEnvType.set_id(id);
                addEnvType.setId(id.toString());
            }
            addEnvTypeList.add(addEnvType);
        }
        envDao.saveEnvType(addEnvTypeList);

    }

    @Override
    public List<EnvType> queryEnvTypeList(Map<String, Object> req) throws Exception {
        List<EnvType> envTypeList = envDao.queryEnvTypeList(req);
        return envTypeList;
    }
}
