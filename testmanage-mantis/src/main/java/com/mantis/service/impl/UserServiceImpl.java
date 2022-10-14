package com.mantis.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mantis.dict.Dict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mantis.service.UserService;
import com.test.testmanagecommon.transport.RestTransport;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private RestTransport restTransport;


    @Override
    public List queryUserByNameCn() throws Exception {
        String userUrl = "xxx/tuser/user/query";
        Map param = new HashMap();
        int i = 1;
        param.put("pageSize", 100);
        param.put("currentPage", i);
        List<Map> userlist = new ArrayList<Map>();
        List<Map> users = (List) restTransport.submit(userUrl, param);
        userlist.addAll(users);
        while (users.size() == 100) {
            param.put("currentPage", i++);
            users = (List) restTransport.submit(userUrl, param);
            userlist.addAll(users);
        }
        return userlist;
    }

    /**
     * 请求fdev所有组名
     * @return
     */
    @Override
    public List queryAllGroup() {
        Map param = new HashMap();
        param.put(Dict.REST_CODE, "queryGroup");
        Object result = null;
        try {
            result = restTransport.submitSourceBack(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (List) result;
    }

    @Override
    public Map<String, List<Map>> queryChildGroupByIds(List<String> groupIds) throws Exception {
        Map<String,Object> param = new HashMap<>();
        param.put(Dict.IDS, groupIds);
        param.put(Dict.REST_CODE, "queryChildGroupByIds");
        return (Map<String, List<Map>>) restTransport.submitSourceBack(param);
    }
}
