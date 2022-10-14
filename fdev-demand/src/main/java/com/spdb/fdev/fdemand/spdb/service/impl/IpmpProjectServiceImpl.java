package com.spdb.fdev.fdemand.spdb.service.impl;

import com.spdb.fdev.fdemand.base.dict.Constants;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.dao.IIpmpProjectDao;
import com.spdb.fdev.fdemand.spdb.entity.IpmpProject;
import com.spdb.fdev.fdemand.spdb.service.IIpmpProjectService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RefreshScope
public class IpmpProjectServiceImpl implements IIpmpProjectService {
    @Autowired
    private IIpmpProjectDao ipmpProjectDao;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${ipmp.gateway}")
    private String ipmpGateWay;
    @Value("${ipmp.fdev.appno}")
    private String appno;
    @Value("${ipmp.getAllProjects.appKey}")
    private String getAllProjectsAppKey;
    @Value("${no.sync.projectNo}")
    private String noSyncProjectNo;

    @Override
    public void syncAllIpmpProject() {
        //调用ipmp接口查询项目/任务集
        Map<String,Object> param = new HashMap<>();
        Map<String,Object> common = new HashMap<>();
        common.put(Dict.APPNO,appno);
        common.put(Dict.METHOD,Constants.IPMP_METHOD_GETALLPROJECTS);
        common.put(Dict.APPKEY,getAllProjectsAppKey);
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
            List<IpmpProject> ipmpProjectList = new ArrayList<>();
            List<String> noSyncProjectNoList = Arrays.asList(noSyncProjectNo.split(","));
            data.stream().forEach(map -> {
                if(!noSyncProjectNoList.contains((String) map.get(Dict.PROJECTNO))) {
                    IpmpProject ipmpProject = new IpmpProject();
                    ObjectId objectId = new ObjectId();
                    ipmpProject.set_id(objectId);
                    ipmpProject.setProject_no((String) map.get(Dict.PROJECTNO));
                    ipmpProject.setProject_name((String) map.get(Dict.PROJECTNAME));
                    ipmpProject.setProject_status_name((String) map.get(Dict.PROJECTSTATUSNAME));
                    ipmpProject.setProject_type((String) map.get(Dict.PROJECTTYPE));
                    ipmpProjectList.add(ipmpProject);
                }
            });
            ipmpProjectDao.addIpmpProjectBatch(ipmpProjectList);
        }
    }

    @Override
    public List<IpmpProject> queryIpmpProject(String projectNo) {
        return ipmpProjectDao.queryIpmpProject(projectNo);
    }
}
