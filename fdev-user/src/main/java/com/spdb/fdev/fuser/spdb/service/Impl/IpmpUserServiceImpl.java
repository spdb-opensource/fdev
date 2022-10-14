package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.IIpmpUserDao;
import com.spdb.fdev.fuser.spdb.entity.user.IpmpUser;
import com.spdb.fdev.fuser.spdb.service.IIpmpUserService;
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
public class IpmpUserServiceImpl implements IIpmpUserService {
    @Autowired
    private IIpmpUserDao ipmpUserDao;
    @Autowired
    private RestTemplate restTemplate;

    @Value("${ipmp.gateway}")
    private String ipmpGateWay;
    @Value("${ipmp.fdev.appno}")
    private String appno;
    @Value("${ipmp.getUsers.appKey}")
    private String getUsersAppKey;

    @Override
    public void syncAllIpmpUser() {
        //调用ipmp接口查询用户信息
        Map<String,Object> param = new HashMap<>();
        Map<String,Object> common = new HashMap<>();
        common.put(Dict.APPNO,appno);
        common.put(Dict.METHOD,Constants.IPMP_METHOD_GETUSERS);
        common.put(Dict.APPKEY,getUsersAppKey);
        common.put(Dict.ISAUTH,true);
        common.put(Dict.VERSION,Constants.IPMP_VERSION);
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
            List<IpmpUser> ipmpUserList = new ArrayList<>();
            data.stream().forEach(map -> {
                //不要离职的
                if(!CommonUtils.isNullOrEmpty(map.get(Dict.USERSTATUS))
                        && "在职".equals((String)map.get(Dict.USERSTATUS))) {
                    IpmpUser ipmpUser = new IpmpUser();
                    ObjectId objectId = new ObjectId();
                    ipmpUser.set_id(objectId);
                    ipmpUser.setUser_name_en((String) map.get(Dict.USERID2));
                    ipmpUser.setStaff_no((String) map.get(Dict.STAFFNO));
                    ipmpUser.setUser_name_cn((String) map.get(Dict.STAFFNAME));
                    ipmpUser.setEmail((String) map.get(Dict.EMAIL));
                    ipmpUser.setIs_spdb(((String) map.get(Dict.STAFFTYPE)).equals("行内"));
                    ipmpUser.setDept_name((String) map.get(Dict.DEPTNAME));
                    ipmpUser.setCompany_full_name((String) map.get(Dict.COMPANYFULLNAME));
                    ipmpUser.setStatus(Constants.USER_STATUS_IN);
                    ipmpUser.setCreate_date((String) map.get(Dict.CREATETIME));
                    ipmpUser.setUpdate_date((String) map.get(Dict.UPDATETIME));
                    ipmpUserList.add(ipmpUser);
                }
            });
            ipmpUserDao.addIpmpUserBatch(ipmpUserList);
        }
    }

    @Override
    public List<Map<String,String>> queryIpmpUser(String userNameEN) {
        List<IpmpUser> ipmpUserList = ipmpUserDao.queryIpmpUser(userNameEN);
        //只返回前端需要的字段，防止数据包太大前端解析耗时长 
        List<Map<String,String>> userList = new ArrayList<>();
        ipmpUserList.stream().forEach(ipmpUser -> {
            Map<String,String> map = new HashMap<>();
            map.put(Dict.USER_NAME_CN, ipmpUser.getUser_name_cn());
            map.put(Dict.USER_NAME_EN, ipmpUser.getUser_name_en());
            map.put(Dict.EMAIL, ipmpUser.getEmail());
            userList.add(map);
        });
        return userList;
    }

}
