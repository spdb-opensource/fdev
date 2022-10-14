package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.util.CommonUtils;
import com.spdb.fdev.spdb.dao.ISccModifyKeyDao;
import com.spdb.fdev.spdb.entity.SccModifyKey;
import com.spdb.fdev.spdb.service.ISccModifyKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Author:guanz2
 * @Date:2021/10/5-13:47
 * @Description: scc_modifyKey service 实现类
 */
@Component
public class SccModifyKeyServiceImpl implements ISccModifyKeyService {

    @Autowired
    private ISccModifyKeyDao sccModifyKeyDao;

    @Override
    public void updateModifyKeys(List<LinkedHashMap<String, Object>> list) {
        if(list.size() > 0){
            List<SccModifyKey> sccModifyKeys = new ArrayList<>();
            for(int i = 0; i < list.size(); i++){
                SccModifyKey sccModifyKey = new SccModifyKey();
                sccModifyKey.setNamespace_code((String) list.get(i).get("namespace_code"));
                sccModifyKey.setResource_code((String) list.get(i).get("resource_code"));
                sccModifyKey.setYaml((String) list.get(i).get("yaml"));
                sccModifyKeys.add(sccModifyKey);
            }
            sccModifyKeyDao.updateModifyKeyDeploy(sccModifyKeys);
        }
    }

    @Override
    public  List<LinkedHashMap<String, Object>> queryModifyKeys(String resource_code) {
        List<SccModifyKey> sccModifyKeys = sccModifyKeyDao.getModifyDeploy(resource_code);
        List<LinkedHashMap<String, Object>> result = new ArrayList<>();
        if(!CommonUtils.isNullOrEmpty(sccModifyKeys)){
            //将yaml字符串转换成json输出
            for(int i = 0; i < sccModifyKeys.size(); i++){
                LinkedHashMap temp = new LinkedHashMap();
                temp.put("namespace_code", sccModifyKeys.get(i).getNamespace_code());
                temp.put("resource_code", sccModifyKeys.get(i).getResource_code());
                DumperOptions dumperOptions = new DumperOptions();
                dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                Yaml wYaml = new Yaml();
                temp.put("yaml", wYaml.load(sccModifyKeys.get(i).getYaml()));
                result.add(temp);
            }
        }
        return result;
    }
}
