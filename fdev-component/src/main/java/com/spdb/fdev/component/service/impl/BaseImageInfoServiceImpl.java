package com.spdb.fdev.component.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.base.Constant;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.base.utils.validate.ValidateComponent;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.IBaseImageInfoDao;
import com.spdb.fdev.component.dao.ITagRecordDao;
import com.spdb.fdev.component.entity.BaseImageInfo;
import com.spdb.fdev.component.entity.TagRecord;
import com.spdb.fdev.component.service.*;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
@RefreshScope
public class BaseImageInfoServiceImpl implements IBaseImageInfoService {

    private IRoleService roleService;

    private IBaseImageInfoDao baseImageInfoDao;

    private IGitlabSerice gitlabSerice;

    private IGitlabUserService gitlabUserService;

    private ICommonInfoService commonInfoService;


    @Autowired
    private ITagRecordDao tagRecordDao;


    @Value("${gitlab.manager.token}")
    private String token;

    @Override
    public List query(BaseImageInfo baseImageInfo) throws Exception {
        return baseImageInfoDao.query(baseImageInfo);
    }

    @Override
    public BaseImageInfo save(BaseImageInfo baseImageInfo) throws Exception {
        //校验用户权限 必须为基础架构负责人
        if (!roleService.isBasicArchitectManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构负责人"});
        }
        //检查镜像中文名和英文名是否存在
        List<BaseImageInfo> infoList = this.query(new BaseImageInfo());
        ValidateComponent.checkAppNameEnAndNameZh(infoList, baseImageInfo);
        //判断应用英文名只能为数字字母下划线
        if (!Pattern.matches(Constant.COMPONENT_PATTERN, baseImageInfo.getName())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{baseImageInfo.getName(), "当前组件英文名不符合规范！"});
        }
        //对gitlaburl进行校验，不需要.git
        String gitlabUrl = baseImageInfo.getGitlab_url();
        if (StringUtils.isNotBlank(gitlabUrl) && gitlabUrl.endsWith(".git") && gitlabUrl.length() > 4) {
            baseImageInfo.setGitlab_url(gitlabUrl.substring(0, gitlabUrl.length() - 4));
        }
        //判断当前镜像是否在gitlab上存在，存入gitlabid
        String gitlabId = gitlabSerice.queryProjectIdByUrl(baseImageInfo.getGitlab_url());
        if (StringUtils.isBlank(gitlabId)) {
            throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{baseImageInfo.getGitlab_url()});
        }
        baseImageInfo.setGitlab_id(gitlabId);
        baseImageInfo.setCreate_date(Util.simdateFormat(new Date()));//设置创建时间
        //基础镜像增加webhook
        gitlabSerice.addProjectHook(gitlabId, this.token, Dict.TRUE, Dict.TRUE, Constants.COMPONENT_IMAGE);
        //给镜像负责人添加mainter权限
        gitlabUserService.addMembers(commonInfoService.addMembersForApp(baseImageInfo.getManager(), gitlabId, Dict.MAINTAINER));
        //添加持续集成相关文件
        commonInfoService.continueIntegration(gitlabId, baseImageInfo.getName(), Constants.COMPONENT_IMAGE);//持续集成
        //数据入库
        return baseImageInfoDao.save(baseImageInfo);
    }

    @Override
    public BaseImageInfo update(BaseImageInfo baseImageInfo) throws Exception {
        BaseImageInfo baseImage = this.queryByName(baseImageInfo.getName());
        //校验用户权限 必须为基础架构负责人，或镜像管理员
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(baseImage.getManager())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足,必须为基础架构负责人或镜像管理员"});
            }
        }
        //镜像负责人添加mainter权限
        if (StringUtils.isNotBlank(baseImage.getGitlab_url())) {
            String gitlabId = gitlabSerice.queryProjectIdByUrl(baseImage.getGitlab_url());
            if (StringUtils.isBlank(gitlabId)) {
                throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{baseImage.getName()});
            }
            baseImageInfo.setGitlab_id(gitlabId);
            gitlabUserService.addMembers(commonInfoService.addMembersForApp(baseImage.getManager(), gitlabId, Dict.MAINTAINER));
        }
        return baseImageInfoDao.update(baseImageInfo);
    }

    @Override
    public BaseImageInfo queryById(String id) {
        return baseImageInfoDao.queryById(id);
    }

    @Override
    public BaseImageInfo queryByName(String name) {
        return baseImageInfoDao.queryByName(name);
    }

    @Override
    public Map queryMetaData(String name) {
        BaseImageInfo baseImageInfo = this.queryByName(name);
        if (baseImageInfo != null) {
            return baseImageInfo.getMeta_data_declare();
        }
        return null;
    }

    @Override
    public void relDevops(Map<String, String> map) throws Exception {
        String imageId = map.get(Dict.IMAGE_ID);
        String branch = map.get(Dict.BRANCH);
        String target_version = map.get(Dict.TARGET_VERSION);
        String releaseNodeName = map.get("release_node_name");
        String desc = map.get(Dict.DESCRIPTION);
        //检查基础镜像表
        BaseImageInfo baseImage = baseImageInfoDao.queryById(imageId);
        String gitlabId = baseImage.getGitlab_id();
        if (baseImage == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"当前镜像不存在"});
        }
        //持续集成参数传递，imageTag，{target_env}-{yyMMddHHmm}
        String targetTag = baseImage.getTarget_env() + "-" + Util.getTimeStamp(new Date());
        boolean existTag = gitlabSerice.checkBranchOrTag(token, gitlabId, Constants.TAG, targetTag);
        if (existTag) {
            throw new FdevException(ErrorConstants.TAG_EXISTS, new String[]{"Tag: " + targetTag + "已存在  "});
        }
        ArrayList<Map<String, String>> variables = new ArrayList<>();
        Map<String, String> groupId = new HashMap<>();
        groupId.put(Dict.KEY, Dict.IMAGE_TAG);
        groupId.put(Dict.VALUE, targetTag);
        variables.add(groupId);

        //发起release分支到master的合并请求
        Object result = gitlabSerice.createMergeRequest(gitlabId, branch, Dict.MASTER, desc + "-提交准生产测试", desc, token);
        JSONObject jsonObject = JSONObject.fromObject(result);
        ObjectMapper objectMapper = new ObjectMapper();
        Map merge = objectMapper.readValue(jsonObject.toString(), Map.class);
        String mergeId = String.valueOf(merge.get(Dict.IID));
        TagRecord tagRecord = new TagRecord();
        tagRecord.setGitlab_id(gitlabId);
        tagRecord.setMerge_request_id(mergeId);
        tagRecord.setBranch(targetTag);
        tagRecord.setVariables(variables);
        tagRecord.setImage_id(imageId);
        tagRecord.setRelease_node_name(releaseNodeName);
        tagRecordDao.save(tagRecord);
    }

    @Autowired
    public void setRoleService(IRoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setBaseImageInfoDao(IBaseImageInfoDao baseImageInfoDao) {
        this.baseImageInfoDao = baseImageInfoDao;
    }

    @Autowired
    public void setGitlabSerice(IGitlabSerice gitlabSerice) {
        this.gitlabSerice = gitlabSerice;
    }

    @Autowired
    public void setGitlabUserService(IGitlabUserService gitlabUserService) {
        this.gitlabUserService = gitlabUserService;
    }

    @Autowired
    public void setCommonInfoService(ICommonInfoService commonInfoService) {
        this.commonInfoService = commonInfoService;
    }
}
