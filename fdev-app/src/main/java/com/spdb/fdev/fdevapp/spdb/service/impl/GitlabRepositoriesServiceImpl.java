package com.spdb.fdev.fdevapp.spdb.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.spdb.fdev.fdevapp.base.dict.Dict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.base.utils.GitlabTransport;
import com.spdb.fdev.fdevapp.base.utils.TimeUtils;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabAPIService;
import com.spdb.fdev.fdevapp.spdb.service.IGitlabRepositoriesService;

@Service
@RefreshScope
public class GitlabRepositoriesServiceImpl implements IGitlabRepositoriesService {
    @Value("${gitlib.path}")
    private String url;// gitlab地址http://xxx/api/v4/

    @Autowired
    private IGitlabAPIService gitlabAPIService;

    @Value("${gitlab.resource.Path}")
    private String task_files;
    @Value("${gitlab.token}")
    private String gitlab_token;

    @Value("${gitlab.resource.filepath}")
    private String gitlab_filepath;

    @Autowired
    private GitlabTransport gitlabTransport;


    /**
     * 查询项目文件树
     *
     * @param id    项目id
     * @param token 验证码
     * @return 文件数
     * @throws Exception
     */
    @Override
    public Object queryRepositoriesTree(String id, String token) throws Exception {
        String pipeline_url = CommonUtils.projectUrl(url) + "/" + id + "/repository/tree";
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitGet(pipeline_url, header);

    }

    /**
     * 校验项目是否存在或者用户有权限
     *
     * @param id    项目id
     * @param token 验证码
     * @return 当项目存在时返回true
     */
    @Override
    public boolean projectVaildation(String id, String token) throws Exception {
        boolean flag = false;
        try {
            Object result = gitlabAPIService.queryProjectById(id, token);
            if (CommonUtils.isNullOrEmpty(result)) {
                flag = true;
            }
        } catch (Exception e) {
            return false;
        }
        return flag;
    }

    @Override
    public void changeMasterBranch(String id, String token, String branch) throws Exception {
        // 将默认分支由master改为tag
        changeDefaultBranch(id, token, branch);
        // 从master分支上拉取新分支 命名规则为master_时间戳
        String branchname = "master_" + TimeUtils.formatDate(TimeUtils.STANDARDDATEPATTERN);
        gitlabAPIService.createBranch(id, branchname, Dict.MASTER, token);
        // 删除原有的master分支
        gitlabAPIService.deleteBranch(id, Dict.MASTER, token);
        // 从新分支上拉取新的master分支
        gitlabAPIService.createBranch(id, Dict.MASTER, branch, token);
        // 修改默认分支为新的master分支
        changeDefaultBranch(id, token, Dict.MASTER);
    }

    @Override
    public void changeDefaultBranch(String id, String token, String branch) throws Exception {
        String changeDefaultBranch = CommonUtils.projectUrl(url) + "/" + id + "?default_branch=" + branch;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN_L, token);
        gitlabTransport.submitPut(changeDefaultBranch, header);
    }

    @Override
    public Object queryMergeRequestList(String id, String token, String date) throws Exception {
        String mr_url = CommonUtils.projectUrl(url) + "/" + id + "/merge_requests?created_after=" + date;
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        return gitlabTransport.submitGet(mr_url, header);

    }

    @Override
    public List<Map<String, String>> queryTaskFileList(String path) throws Exception {
        String file_url = task_files + "?path=" + path + "&recursive=true";
        HttpHeaders header = new HttpHeaders();
        header.set(Dict.PRIVATE_TOKEN, gitlab_token);
        Object obj = gitlabTransport.submitGet(file_url, header);
        JSONArray files = JSONObject.parseArray((String) obj);
        List<String> treelist = new ArrayList<String>();
        List<String> bloblist = new ArrayList<String>();
        List<Map<String, String>> taskfile = new ArrayList<Map<String, String>>();
        for (Object file : files) {
            Map<String, String> filemap = (Map<String, String>) file;
            if (filemap.get(Dict.TYPE).equals(Dict.TREE)) {
                treelist.add(filemap.get(Dict.NAME));
            } else if (filemap.get(Dict.TYPE).equals(Dict.BLOB)) {
                bloblist.add(filemap.get(Dict.PATH));
            }
        }
        for (String tree : treelist) {
            Map<String, String> dic = new HashMap<String, String>();
            for (String blob : bloblist) {
                if (blob.contains(tree) && !blob.contains(tree + "-")) {
                    String[] split = blob.split("/");
                    String filename = split[split.length - 1];
                    dic.put(filename, gitlab_filepath + "/" + blob);
                }
            }
            dic.put(Dict.NAME, tree);
            taskfile.add(dic);
        }
        return taskfile;
    }

    @Override
    public boolean isProjectExist(Integer namespaceId, String name, String token) throws Exception {
        MultiValueMap<String, String> header = new LinkedMultiValueMap<String, String>();
        header.add(Dict.PRIVATE_TOKEN, token);
        String searce_pro_url = CommonUtils.projectUrl(url) + "?search=" + name;//获取所以该名称的项目信息
        Object projects = gitlabTransport.submitGet(searce_pro_url, header);
        if (CommonUtils.isNullOrEmpty(projects)) {//若未查询到 则说明不存在
            return false;
        }
        JSONArray project_list = JSONObject.parseArray((String) projects);
        boolean flag = false;
        for (Object project : project_list) {
            Map map = (Map) project;
            Map namespace = (Map) map.get("namespace");
            String project_name = (String) map.get(Dict.NAME);
            Integer id = (Integer) namespace.get(Dict.ID);
            //判断groupid是否相同
            //判断name是否相同
            if (namespaceId.equals(id) && (name.equals(project_name))) {
                flag = true;
                break;
            }
        }
        return flag;
    }

}
