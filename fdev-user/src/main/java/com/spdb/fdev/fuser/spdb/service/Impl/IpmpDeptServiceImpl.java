package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.IIpmpDeptDao;
import com.spdb.fdev.fuser.spdb.entity.user.IpmpDept;
import com.spdb.fdev.fuser.spdb.service.IIpmpDeptService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class IpmpDeptServiceImpl implements IIpmpDeptService {
    @Autowired
    private IIpmpDeptDao ipmpDeptDao;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${ipmp.gateway}")
    private String ipmpGateWay;
    @Value("${ipmp.fdev.appno}")
    private String appno;
    @Value("${ipmp.getDeptOrgs.appKey}")
    private String getDeptOrgsKey;

    @Override
    public void syncAllIpmpDept() {
        //调用ipmp接口查询用户信息
        Map<String,Object> param = new HashMap<>();
        Map<String,Object> common = new HashMap<>();
        common.put(Dict.APPNO,appno);
        common.put(Dict.METHOD,Constants.IPMP_METHOD_GETDEPTORGS);
        common.put(Dict.APPKEY,getDeptOrgsKey);
        common.put(Dict.ISAUTH,true);
        common.put(Dict.VERSION, Constants.IPMP_VERSION);
        param.put(Dict.COMMON, common);
        param.put(Dict.REQUEST, new HashMap<>());
        Map<String,Object> result = null;
        try {
            result = (Map)this.restTemplate.postForObject(ipmpGateWay, param, Map.class, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!CommonUtils.isNullOrEmpty(result) && "0".equals(result.get(Dict.STATUS))) {
            List<Map> data = (List<Map>) result.get(Dict.DATA);
            List<IpmpDept> ipmpDeptList = new ArrayList<>();
            data.stream().forEach(map -> {
                IpmpDept ipmpDept = new IpmpDept();
                ObjectId objectId = new ObjectId();
                ipmpDept.set_id(objectId);
                ipmpDept.setDept_id((String) map.get(Dict.DEPTID));
                ipmpDept.setDept_name((String) map.get(Dict.DEPTNAME));
                ipmpDept.setParent_id((String) map.get(Dict.PARENTID));
                ipmpDept.setDept_status((String) map.get(Dict.DEPTSTATUS));
                ipmpDept.setCreate_time((String) map.get(Dict.CREATETIME));
                ipmpDept.setUpdate_time((String) map.get(Dict.UPDATETIME));
                ipmpDeptList.add(ipmpDept);
            });
            ipmpDeptDao.addIpmpDeptBatch(ipmpDeptList);
        }
    }

    @Override
    public List<IpmpDept> queryIpmpDept(String deptId) {
        return ipmpDeptDao.queryIpmpDept(deptId);
    }

}
