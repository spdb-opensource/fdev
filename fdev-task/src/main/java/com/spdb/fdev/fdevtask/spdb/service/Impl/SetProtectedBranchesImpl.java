package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.GitlabTransport;
import com.spdb.fdev.fdevtask.spdb.service.ISetProtectedBranches;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
@Service
@RefreshScope
public class SetProtectedBranchesImpl implements ISetProtectedBranches {
    @Value("${gitlib.path}")
    private String url;// gitlab地址http://xxx/api/v4/
    @Autowired
    private GitlabTransport gitlabTransport;

    @Override
    public Object setProtectedBranches(String id, String name, String token) throws Exception {
            String protected_branchesUrl = CommonUtils.projectUrl(url) + "/" + id + "/protected_branches/";
            Map<String, String> param = new HashMap<>();
            param.put(Dict.ID, id);
            param.put(Dict.NAME, name);
            param.put(Dict.PRIVATE_TOKEN_L, token);
            return this.gitlabTransport.submitPost(protected_branchesUrl, param);
    }
}
