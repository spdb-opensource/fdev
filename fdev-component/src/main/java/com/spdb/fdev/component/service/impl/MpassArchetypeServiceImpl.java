package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.Constant;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.base.utils.validate.ValidateComponent;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.IMpassArchetypeDao;
import com.spdb.fdev.component.entity.ArchetypeInfo;
import com.spdb.fdev.component.entity.MpassArchetype;
import com.spdb.fdev.component.service.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class MpassArchetypeServiceImpl implements IMpassArchetypeService {

    private IMpassArchetypeDao mpassArchetypeDao;

    private IRoleService roleService;

    private IGitlabSerice gitlabSerice;

    private IGitlabUserService gitlabUserService;

    private ICommonInfoService commonInfoService;

    @Override
    public List query(MpassArchetype mpassArchetype) throws Exception {
        return mpassArchetypeDao.query(mpassArchetype);
    }

    @Override
    public MpassArchetype save(MpassArchetype mpassArchetype) throws Exception {
        //校验用户权限 必须为基础架构管理员
        if (!roleService.isBasicArchitectManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构管理员"});
        }
        //验证中英文名是否重复
        List<MpassArchetype> query = this.query(new MpassArchetype());
        ValidateComponent.checkAppNameEnAndNameZh(query, mpassArchetype);

        //判断应用英文名只能为数字字母下划线
        if (!Pattern.matches(Constant.COMPONENT_PATTERN, mpassArchetype.getName_en())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{mpassArchetype.getName_en(), "当前骨架英文名不符合规范！"});
        }
        //对gitlaburl进行校验，不需要.git
        String gitlabUrl = mpassArchetype.getGitlab_url();
        if (StringUtils.isNotBlank(gitlabUrl) && gitlabUrl.endsWith(".git") && gitlabUrl.length() > 4) {
            mpassArchetype.setGitlab_url(gitlabUrl.substring(0, gitlabUrl.length() - 4));
        }
        String gitlabId = gitlabSerice.queryProjectIdByUrl(mpassArchetype.getGitlab_url());
        if (StringUtils.isBlank(gitlabId)) {
            throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{mpassArchetype.getGitlab_url()});
        }
        mpassArchetype.setGitlab_id(gitlabId);
        MpassArchetype tmp = new MpassArchetype();
        tmp.setGitlab_id(gitlabId);
        List<MpassArchetype> list = mpassArchetypeDao.query(tmp);
        if (!list.isEmpty()){
            throw new FdevException(ErrorConstants.GITLAB_ID_ERROR, new String[]{"前端骨架", "前端骨架", list.get(0).getName_en()});
        }
        //设置创建时间
        mpassArchetype.setCreate_date(Util.simdateFormat(new Date()));
        mpassArchetype.setArchetype_type("mpass");
        //给组件负责人添加mainter权限
        gitlabUserService.addMembers(commonInfoService.addMembersForApp(mpassArchetype.getManager(), gitlabId, Dict.MAINTAINER));
        //异步扫描骨架和应用关联关系
        MpassArchetype result = mpassArchetypeDao.save(mpassArchetype);
        return result;
    }

    @Override
    public void delete(MpassArchetype mpassArchetype) throws Exception {
        mpassArchetypeDao.delete(mpassArchetype);
    }

    @Override
    public MpassArchetype queryById(MpassArchetype mpassArchetype) throws Exception {
        return mpassArchetypeDao.queryById(mpassArchetype);
    }

    @Override
    public MpassArchetype queryById(String id) throws Exception {
        return mpassArchetypeDao.queryById(id);
    }

    @Override
    public MpassArchetype queryByWebUrl(String web_url) {
        return mpassArchetypeDao.queryByWebUrl(web_url);
    }

    @Override
    public MpassArchetype update(MpassArchetype mpassArchetype) throws Exception {
        //校验用户权限 必须为基础架构管理员或骨架管理员
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(mpassArchetype.getManager())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构管理员或骨架管理员"});
            }
        }
        //给骨架负责人添加mainter权限
        if (StringUtils.isNotBlank(mpassArchetype.getGitlab_url())) {
            String gitlabId = gitlabSerice.queryProjectIdByUrl(mpassArchetype.getGitlab_url());
            if (StringUtils.isBlank(gitlabId)) {
                throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{mpassArchetype.getName_en()});
            }
            gitlabUserService.addMembers(commonInfoService.addMembersForApp(mpassArchetype.getManager(), gitlabId, Dict.MAINTAINER));
        }
        mpassArchetype.setArchetype_type("mpass");
        return mpassArchetypeDao.update(mpassArchetype);
    }

    @Override
    public List<MpassArchetype> queryByUserId(String user_id) {
        return mpassArchetypeDao.queryByUserId(user_id);
    }

    @Autowired
    public void setRoleService(IRoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setMpassArchetypeDao(IMpassArchetypeDao mpassArchetypeDao) {
        this.mpassArchetypeDao = mpassArchetypeDao;
    }

    @Autowired
    public void setGitlabSerice(IGitlabSerice gitlabSerice) {
        this.gitlabSerice = gitlabSerice;
    }

    @Autowired
    public void setCommonInfoService(ICommonInfoService commonInfoService) {
        this.commonInfoService = commonInfoService;
    }

    @Autowired
    public void setGitlabUserService(IGitlabUserService gitlabUserService) {
        this.gitlabUserService = gitlabUserService;
    }
}
