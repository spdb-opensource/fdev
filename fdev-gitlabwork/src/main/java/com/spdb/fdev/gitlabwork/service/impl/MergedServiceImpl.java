package com.spdb.fdev.gitlabwork.service.impl;

import com.spdb.fdev.base.cache.GuavaCache;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.gitlabwork.dao.MergedDao;
import com.spdb.fdev.gitlabwork.entiy.MergedInfo;
import com.spdb.fdev.gitlabwork.service.GitLabService;
import com.spdb.fdev.gitlabwork.service.MergedService;
import com.spdb.fdev.transport.RestTransport;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RefreshScope
public class MergedServiceImpl implements MergedService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MergedDao mergedDao;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    private GitLabService gitLabService;

    @Autowired
    private GuavaCache cache;

    //白名单不记录merge request组
    @Value("${fdev.list.groupId}")
    private List<String> wListGroupIdList;

    @Override
    public MergedInfo addMergedInfo(MergedInfo mergedInfo) {
        return mergedDao.saveMergedInfo(mergedInfo);
    }

    @Override
    public Map getMergedInfo(Map params) {
        handleParamsTime(params);
        getChildrenGroupIds(params);
        getFullNameGroup();
        return this.mergedDao.queryMergedInfo(params);
    }

    @Override
    public XSSFWorkbook exportMergedInfo(Map<String, Object> request) {
        // 初始化workbook
        InputStream inputStream;
        XSSFWorkbook workbook;
        //引入模板
        try {
            ClassPathResource classPathResource = new ClassPathResource("mergeRequest.xlsx");
            inputStream = classPathResource.getInputStream();
            workbook = new XSSFWorkbook(inputStream);
        } catch (Exception e) {
            throw new FdevException("初始化模板失败");
        }
        XSSFSheet sheet = workbook.getSheetAt(0);
        Map result = getMergedInfo(request);
        List<Map<String, Object>> mergedInfo = (List<Map<String, Object>>) result.get("mergedInfo");
        for (int i = 0; i < mergedInfo.size(); i++) {
            Map<String, Object> merge = mergedInfo.get(i);
            XSSFRow row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue((String)merge.get("targetBranch"));//目标分支
            row.createCell(1).setCellValue((String)merge.get("sourceBranch"));//源分支
            row.createCell(2).setCellValue((String)merge.get("appName"));//应用名
            row.createCell(3).setCellValue((String)merge.get("fullName"));//应用所属小组
            row.createCell(4).setCellValue(((Map<String, String>)merge.get("creator")).get("user_name_cn"));//创建人
            row.createCell(5).setCellValue(((Map<String, String>)merge.get("handler")).get("user_name_cn"));//处理人
            row.createCell(6).setCellValue((String)merge.get("createTime"));//创建时间
            row.createCell(7).setCellValue((String)merge.get("handleTime"));//处理时间
        }
        return workbook;
    }

    @Override
    public Map getCreatorInfo(Integer creator) {
        Map gitUserInfo = this.gitLabService.getUserInfoById(creator);
        return getCreatorInfoByFuser(creator, gitUserInfo);
    }

    /**
     * 调用应用模块，通过gitlab的projectId来查询fdev是否存在有该应用
     *
     * @param projectId
     * @return
     */
    @Override
    public Map checkFapp(Integer projectId) {

        Map result = new HashMap();
        Map<String, Object> param = new HashMap<>();
        param.put("id", String.valueOf(projectId));
        param.put(Dict.REST_CODE, "getAppByGitId");
        Map app = new HashMap();
        try {
            app = (Map) this.restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{"调用应用模块接口(/fapp/api/app/getAppByGitId)出错：" + e.getMessage()});
        }
        if (CommonUtil.isNullOrEmpty(app))
            return null;
        //返回fdev appId 以及 group
        result.put(Dict.ID, (String) app.get(Dict.ID));
        result.put(Dict.GROUP, (String) app.get(Dict.GROUP));
        return result;
    }

    /**
     * 调用用户模块获取group信息，带有fullname，进行缓存
     */
    @Override
    public List<Map> getFullNameGroup() {
        Object fullNameCache = this.cache.getCache(Dict.FULLNAMES);
        if (CommonUtil.isNullOrEmpty(fullNameCache)) {
            Map<String, Object> param = new HashMap<>();
            param.put(Dict.REST_CODE, "queryGroup");
            List<Map> groups = new ArrayList<>();
            try {
                groups = (List<Map>) this.restTransport.submit(param);
            } catch (Exception e) {
                throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{"调用用户模块接口(/fuser/api/group/query)出错：" + e.getMessage()});
            }
            this.cache.setCache(Dict.FULLNAMES, groups);
            return groups;
        }
        return (List<Map>) fullNameCache;
    }


    /**
     * 通过调用用户模块来获得用户的信息
     *
     * @param creator gitlab_id
     * @return
     */
    private Map getCreatorInfoByFuser(Integer creator, Map gitUserInfo) {
        Map map = new HashMap<>();
        map.put("gitlab_id", gitUserInfo.get("id"));
        map.put("name", gitUserInfo.get("name"));
        map.put("username", gitUserInfo.get("username"));

        Map<String, Object> param = new HashMap<>();
        param.put("status", "0");
        param.put("git_user_id", creator);
        param.put(Dict.REST_CODE, "queryUser");
        List<Map> users = null;
        try {
            users = (List<Map>) this.restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{"调用用户模块接口(/fuser/api/user/query)出错：" + e.getMessage()});
        }
        if (!CommonUtil.isNullOrEmpty(users)) {
            //可能存在有一个gitlab用户对应多个fdev用户，但是需求只取一个，默认第一个
            Map userMap = new HashMap<>();
            userMap.putAll(map);
            userMap.put(Dict.ID, users.get(0).get(Dict.ID));
            userMap.put("user_name_en", users.get(0).get("user_name_en"));
            userMap.put("user_name_cn", users.get(0).get("user_name_cn"));
            //加入groupId
            userMap.put("groupId", users.get(0).get("group_id"));
            return userMap;
        } else
            return map;
    }


    /**
     * 处理前端传过来的开始时间and结束时间,将yyyy/MM/dd HH:mm 转换为 yyyy-MM-dd HH:mm:ss
     * 处理前端传过来的开始时间and结束时间,将yyyy/MM/dd HH 转换为 yyyy-MM-dd HH:mm:ss
     * 处理前端传过来的开始时间and结束时间,将yyyy/MM/dd 转换为 yyyy-MM-dd HH:mm:ss
     *
     * @param params
     */
    private void handleParamsTime(Map params) {
        if (CommonUtil.isNullOrEmpty(params.get(Dict.START_TIME)) && CommonUtil.isNullOrEmpty(params.get(Dict.END_TIME)))
            return;
        if (!CommonUtil.isNullOrEmpty(params.get(Dict.START_TIME)))
            params.put(Dict.START_TIME, CommonUtil.formatDate((String) params.get(Dict.START_TIME), CommonUtil.INPUT_DATE));
        if (!CommonUtil.isNullOrEmpty(params.get(Dict.END_TIME)))
            params.put(Dict.END_TIME, CommonUtil.formatDate((String) params.get(Dict.END_TIME), CommonUtil.INPUT_DATE));

    }


    /**
     * 获取得到所有子组的groupId
     * 调用用户模块接口查询所有子组以及本组信息
     *
     * @param params
     */
    private void getChildrenGroupIds(Map params) {
        List childrenGroupIds = new ArrayList();
        if (CommonUtil.isNullOrEmpty(params.get(Dict.GROUP_ID))) {
            params.put(Dict.CHILDRENGROUPIDS, childrenGroupIds);
            return;
        }
        //发用户模块接口查询所有子组以及本组信息
        Map<String, Object> param = new HashMap<>();
        param.put(Dict.ID, params.get(Dict.GROUP_ID));
        param.put(Dict.REST_CODE, "queryChildGroupById");
        List<Map<String, Object>> groupList;
        try {
            groupList = (List<Map<String, Object>>) this.restTransport.submit(param);
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.THIRD_SERVER_ERROR, new String[]{"调用用户模块接口(/fuser/api/group/queryChildGroupById)出错：" + e.getMessage()});
        }
        if (CommonUtil.isNullOrEmpty(groupList)) {
            params.put(Dict.CHILDRENGROUPIDS, childrenGroupIds);
            return;
        }
        //遍历获取得到所有的子组groupId
        for (Map<String, Object> group : groupList) {
            String id = (String) group.get(Dict.ID);
            childrenGroupIds.add(id);
        }
        params.put(Dict.CHILDRENGROUPIDS, childrenGroupIds);
    }


    /**
     * 校验该creator用户是否是白名单组的用户
     *
     * @param memberInfo
     * @return
     */
    @Override
    public Boolean checkGroupIsWList(Map memberInfo) {
        String groupId = (String) memberInfo.remove("groupId");
        if (null == groupId)
            return false;
        //标识是否存在有白名单组，若查询回来的list有白名单组，则为true，反之为false
        Boolean isExistWList = false;
        //遍历返回的组列表，查看有没有白名单组，若有直接返回true
        for (String wListGroupId : wListGroupIdList) {
            if (wListGroupId.equals(groupId)) {
                isExistWList = true;
                break;
            }
        }
        return isExistWList;
    }
}
