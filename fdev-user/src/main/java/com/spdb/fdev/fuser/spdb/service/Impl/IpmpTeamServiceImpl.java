package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.IIpmpTeamDao;
import com.spdb.fdev.fuser.spdb.entity.user.IpmpTeam;
import com.spdb.fdev.fuser.spdb.service.IIpmpTeamService;
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
public class IpmpTeamServiceImpl implements IIpmpTeamService {
    @Autowired
    private IIpmpTeamDao ipmpTeamDao;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${ipmp.gateway}")
    private String ipmpGateWay;
    @Value("${ipmp.fdev.appno}")
    private String appno;
    @Value("${ipmp.getHeadDeptAndTeam.appKey}")
    private String getHeadDeptAndTeamAppKey;

    @Override
    public void syncAllIpmpTeam() {
        //调用ipmp接口查询牵头单位团队
        Map<String,Object> param = new HashMap<>();
        Map<String,Object> common = new HashMap<>();
        common.put(Dict.APPNO,appno);
        common.put(Dict.METHOD,Constants.IPMP_METHOD_GETHEADDEPTANDTEAM);
        common.put(Dict.APPKEY,getHeadDeptAndTeamAppKey);
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
            List<IpmpTeam> ipmpTeamList = new ArrayList<>();
            data.stream().forEach(map -> {
                IpmpTeam ipmpTeam = new IpmpTeam();
                ObjectId objectId = new ObjectId();
                ipmpTeam.set_id(objectId);
                ipmpTeam.setDept_id((String) map.get(Dict.HEADERDEPTID));
                ipmpTeam.setDept_name((String) map.get(Dict.HEADERDEPTNAME));
                ipmpTeam.setTeam_id((String) map.get(Dict.HEADERTEAMDEPTID));
                ipmpTeam.setTeam_name((String) map.get(Dict.HEADERTEAMDEPTNAME));
                ipmpTeamList.add(ipmpTeam);
            });
            ipmpTeamDao.addIpmpTeamBatch(ipmpTeamList);
        }
    }

    @Override
    public List<IpmpTeam> queryIpmpLeadTeam(String deptId, String teamId, String deptName) {
        return ipmpTeamDao.queryIpmpLeadTeam(deptId, teamId, deptName);
    }
}
