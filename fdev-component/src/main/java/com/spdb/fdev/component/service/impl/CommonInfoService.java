package com.spdb.fdev.component.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.service.ICommonInfoService;
import com.spdb.fdev.component.service.IGitlabSerice;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
/**
 * 骨架和组件录入、新增等
 * 基本公共方法
 */
@RefreshScope
public class CommonInfoService implements ICommonInfoService {

    private static final Logger logger = LoggerFactory.getLogger(CommonInfoService.class);

    @Value("${gitlab.manager.token}")
    private String token;

    @Value("${python.maven.path}")
    private String maven_path;

    @Value("${gitlab.component.group}")
    private String gitlab_group;

    private RestTransport restTransport;

    private IGitlabSerice gitlabSerice;

    /**
     * 封装 给项目 添加 成员所需要 的信息
     *
     * @param managers
     * @return
     */
    @Override
    public Map addMembersForApp(HashSet<Map<String, String>> managers, String id, String role) throws Exception {
        Map<String, Object> spdbMap = new HashMap<>();
        spdbMap.put(Dict.ROLE, role);
        spdbMap.put(Dict.ID, id);
        List<String> objects = new ArrayList<>();
        for (Map<String, String> map : managers) {
            Map<String, String> param = new HashMap<>();
            String name_en = map.get(Dict.USER_NAME_EN);
            param.put(Dict.USER_NAME_EN, name_en);
            param.put(Dict.REST_CODE, Dict.QUERYUSER);
            Object submit = this.restTransport.submit(param);
            if (!CommonUtils.isNullOrEmpty(submit)) {
                List userList = net.sf.json.JSONArray.fromObject(submit);
                if (userList.size() > 0) {
                    Map parse = (Map) JSONObject.parse(userList.get(0).toString());
                    objects.add((String) parse.get(Dict.GIT_USER_ID));
                }
            }
        }
        spdbMap.put(Dict.GIT_USER_ID, objects);
        return spdbMap;
    }

    /**
     * 封装 给项目 添加 成员所需要 的信息
     *
     * @param
     * @return
     */
    @Override
    public Map addMembersForApp(String userId, String id, String role) throws Exception {
        Map<String, Object> spdbMap = new HashMap<>();
        spdbMap.put(Dict.ROLE, role);
        spdbMap.put(Dict.ID, id);
        List<String> objects = new ArrayList<>();
        Map<String, Object> send_map = new HashMap<>();
        send_map.put(Dict.REST_CODE, Dict.QUERYUSERCOREDATA);
        send_map.put(Dict.ID, userId);// 发user模块获取用户详细信息
        List<Map<String, Object>> list = (ArrayList) restTransport.submit(send_map);
        if (list != null && list.size() > 0) {
            Map userMap = list.get(0);
            String gituser = (String) userMap.get(Dict.GIT_USER_ID);
            objects.add(gituser);
        }
        spdbMap.put(Dict.GIT_USER_ID, objects);
        return spdbMap;
    }

    /**
     * 添加持续集成
     *
     * @param
     */
    @Override
    public void continueIntegration(String projectId, String projectname, String yaml) {
        try {
            Map projectMap = gitlabSerice.getProjectInfo(projectId, token);
            String http_url_to_repo = (String) projectMap.get(Dict.HTTP_URL_TO_REPO);
            gitlabSerice.addDevops(projectId, token, yaml, projectname, http_url_to_repo);
        } catch (Exception e) {
            logger.error("组件{}添加持续集成失败{}", projectname, e.getMessage());
            throw new FdevException(ErrorConstants.CONTINUE_INTEGRATION_ERROR, new String[]{projectname});
        }
        logger.info("组件添加持续集成完成");
    }

    /**
     * 更新组件持续集成文件
     *
     * @param gitlaburl   gitlab地址
     * @param projectname 项目名
     * @throws Exception
     */
    @Override
    public void updateIntegration(String gitlaburl, String projectname, String yaml) throws Exception {
        String projectId = gitlabSerice.queryProjectIdByUrl(gitlaburl);
        if (StringUtils.isBlank(projectId)) {
            throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{projectname});
        }
        try {
            Map projectMap = gitlabSerice.getProjectInfo(projectId, token);
            String http_url_to_repo = (String) projectMap.get(Dict.HTTP_URL_TO_REPO);
            gitlabSerice.addDevops(projectId, token, yaml, projectname, http_url_to_repo);
        } catch (Exception e) {
            logger.error("组件{}添加持续集成失败{}", projectname, e.getMessage());
            throw new FdevException(ErrorConstants.CONTINUE_INTEGRATION_ERROR, new String[]{projectname});
        }
        logger.info("组件{}添加持续集成完成", projectname);
    }

    /**
     * 是否满足1.0.1-SNAPSHOT(RC,RELEASE)这种版本格式，不满足直接放行，新增版本时无限制
     * 满足再进行版本比较
     *
     * @param latestVersion
     * @return
     */
    @Override
    public boolean isJoinCompare(String latestVersion) {
        if (StringUtils.isBlank(latestVersion)) {
            return false;
        }
        String[] split = latestVersion.split("-");
        String[] versionList = split[0].split("\\.");
        if (versionList.length != 3) {
            return false;
        }
        return true;
    }

    /**
     * 对版本进行比较，比当前版本要大，如仓库最新版本为10.0.0，那么新建版本必须为10.0.1
     *
     * @param versionList
     * @param version
     * @return
     */
    @Override
    public Boolean compareVersion(String[] versionList, String[] version) {
        if (versionList.length != 3 && version.length != 3) {
            return false;
        }
        if (Double.valueOf(version[0]).intValue() < Double.valueOf(versionList[0]).intValue()) {
            return false;
        }
        if (Double.valueOf(version[0]).intValue() == Double.valueOf(versionList[0]).intValue()) {
            if (Double.valueOf(version[1]).intValue() < Double.valueOf(versionList[1]).intValue()) {
                return false;
            }
            if (Double.valueOf(version[1]).intValue() == Double.valueOf(versionList[1]).intValue()) {
                if (Double.valueOf(version[2]).intValue() <= Double.valueOf(versionList[2]).intValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Autowired
    public void setRestTransport(RestTransport restTransport) {
        this.restTransport = restTransport;
    }

    @Autowired
    public void setGitlabSerice(IGitlabSerice gitlabSerice) {
        this.gitlabSerice = gitlabSerice;
    }
}
