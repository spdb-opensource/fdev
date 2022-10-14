package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.util.CommonUtils;
import com.spdb.fdev.spdb.dao.ICaasModifyKeyDao;
import com.spdb.fdev.spdb.entity.CaasModifyKey;
import com.spdb.fdev.spdb.service.ICaasModifyKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/10/1-19:27
 * @Description:
 */
@Component
public class CaasModifyKeyServiceImpl implements ICaasModifyKeyService {

    @Autowired
    private ICaasModifyKeyDao caasModifyKeyDao;


    @Override
    public void updateModifyKeys(List<Map<String, String>> list) {
        List<CaasModifyKey> caasModifyKeys = new ArrayList();
        for(Map map:list) {
            CaasModifyKey caasModifyKey = new CaasModifyKey();
            caasModifyKey.setModifykey(map);
            caasModifyKey.setType(Dict.PERSONAL);
            caasModifyKeys.add(caasModifyKey);
        }
        caasModifyKeyDao.updateModifyKeys(caasModifyKeys);
    }

    @Override
    public Map<String, Object> queryPersonalModifyKeys(String deployment, String cluster, String namespace) {
        CaasModifyKey caasModifyKey = caasModifyKeyDao.queryModifyKeys(deployment,cluster,namespace);
        Map resultMap = new HashMap();
        if(!(CommonUtils.isNullOrEmpty(caasModifyKey))) {
            resultMap = caasModifyKey.getModifykey();
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> queryCommonModifyKeys() {
        CaasModifyKey caasModifyKey = caasModifyKeyDao.queryCommonModifyKeys();
        Map resultMap = new HashMap();
        if(!CommonUtils.isNullOrEmpty(caasModifyKey)){
            resultMap = caasModifyKey.getModifykey();
        }
        return resultMap;
    }
}
