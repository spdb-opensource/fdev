package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.Constant;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.base.utils.validate.ValidateComponent;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.dao.IComponentRecordDao;
import com.spdb.fdev.component.dao.IMpassComponentDao;
import com.spdb.fdev.component.entity.MpassComponent;
import com.spdb.fdev.component.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RefreshScope
public class MpassComponentServiceImpl implements IMpassComponentService {

    private IMpassComponentDao mpassComponentDao;

    private IGitlabSerice gitlabSerice;

    private IRoleService roleService;

    private IGitlabUserService gitlabUserService;

    private RestTransport restTransport;

    private IComponentRecordDao componentRecordDao;

    private ICommonInfoService commonInfoService;

    private IComponentScanService componentScanService;

    @Value("${gitlab.manager.token}")
    private String token;


    @Override
    public List query(MpassComponent mpassComponent) throws Exception {
        return mpassComponentDao.query(mpassComponent);
    }

    @Override
    public MpassComponent save(MpassComponent mpassComponent) throws Exception {
        //业务组件参数校验
        if (mpassComponent.getType().equals("0")) {
            if (CommonUtils.isNullOrEmpty(mpassComponent.getSkillstack())) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"技术栈"});
            }
            if (CommonUtils.isNullOrEmpty(mpassComponent.getBusinessarea())) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"业务领域"});
            }
        }
        //校验用户权限 必须为基础机构管理员
        if (!roleService.isBasicArchitectManager()) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构管理员"});
        }
        //判断当前组件中文名和英文名是否存在
        List<MpassComponent> query = this.query(new MpassComponent());
        ValidateComponent.checkAppNameEnAndNameZh(query, mpassComponent);
        //判断应用英文名只能为数字字母下划线
        if (!Pattern.matches(Constant.COMPONENT_PATTERN, mpassComponent.getName_en())) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{mpassComponent.getName_en(), "当前组件英文名不符合规范！"});
        }
        //判断当前组件的npm坐标name是否已存在
        MpassComponent info = mpassComponentDao.queryByNpmname(mpassComponent.getNpm_name());
        if (info != null) {
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"npm_name", "当前组件npm坐标name已存在"});
        }
        //对gitlaburl进行校验，不需要.git
        String gitlabUrl = mpassComponent.getGitlab_url();
        if (StringUtils.isNotBlank(gitlabUrl) && gitlabUrl.endsWith(".git") && gitlabUrl.length() > 4) {
            mpassComponent.setGitlab_url(gitlabUrl.substring(0, gitlabUrl.length() - 4));
        }
        //组内维护组件
        if (Constants.INNER_SOURCE.equals(mpassComponent.getSource())) {
            //判断当前组件是否在gitlab上存在
            String gitlabId = gitlabSerice.queryProjectIdByUrl(mpassComponent.getGitlab_url());
            if (StringUtils.isBlank(gitlabId)) {
                throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{mpassComponent.getGitlab_url()});
            }
            mpassComponent.setGitlab_id(gitlabId);
            MpassComponent tmp = new MpassComponent();
            tmp.setGitlab_id(gitlabId);
            if (!mpassComponentDao.query(tmp).isEmpty()){
                List<MpassComponent> list = mpassComponentDao.query(tmp);
                throw new FdevException(ErrorConstants.GITLAB_ID_ERROR, new String[]{"前端组件", "前端组件", list.get(0).getName_en()});
            }
            gitlabSerice.addProjectHook(gitlabId, this.token, Dict.TRUE, Dict.TRUE, Constants.COMPONENT_MPASS_COMPONENT);
            commonInfoService.continueIntegration(gitlabId, mpassComponent.getName_en(), Constants.COMPONENT_MPASS_COMPONENT);//持续集成
            //给组件负责人添加mainter权限
            gitlabUserService.addMembers(commonInfoService.addMembersForApp(mpassComponent.getManager(), gitlabId, Dict.MAINTAINER));
        }

        //设置创建时间
        mpassComponent.setCreate_date(Util.simdateFormat(new Date()));
        mpassComponent.setComponent_type("mpass");
        MpassComponent result = mpassComponentDao.save(mpassComponent);
        componentScanService.initMpassComponentHistory(result);//通过npm命令获取组件历史版本并入库
        componentScanService.initMpassComponentApplication(result);//添加组件和应用关系
        return result;
    }

    @Override
    public void delete(MpassComponent mpassComponent) throws Exception {
        //校验用户权限 必须为基础架构管理员或组件管理员
        MpassComponent info = this.queryById(mpassComponent.getId());
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(info.getManager())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足,必须为基础架构管理员或组件管理员"});
            }
        }
        this.delete(mpassComponent);
        //删除组件时删除历史信息
        componentRecordDao.deleteByComponentId(mpassComponent.getId());
    }

    @Override
    public MpassComponent queryById(String id) throws Exception {
        return mpassComponentDao.queryById(id);
    }

    @Override
    public MpassComponent queryByGitlabUrl(String web_url) {
        return mpassComponentDao.queryByGitlabUrl(web_url);
    }

    @Override
    public MpassComponent update(MpassComponent mpassComponent) throws Exception {
        MpassComponent info = mpassComponentDao.queryById(mpassComponent.getId());
        //业务组件参数校验
        if (mpassComponent.getType().equals("0")) {
            if (CommonUtils.isNullOrEmpty(mpassComponent.getSkillstack())) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"技术栈"});
            }
            if (CommonUtils.isNullOrEmpty(mpassComponent.getBusinessarea())) {
                throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"业务领域"});
            }
        }

        //校验用户权限 必须为基础架构管理员或组件管理员
        if (!roleService.isBasicArchitectManager()) {
            if (!roleService.isComponentManager(info.getManager())) {
                throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足，必须为基础架构管理员或组件管理员"});
            }
        }
        //给组件负责人添加mainter权限
        if (StringUtils.isNotBlank(mpassComponent.getGitlab_url())) {
            String gitlabId = gitlabSerice.queryProjectIdByUrl(mpassComponent.getGitlab_url());
            if (StringUtils.isBlank(gitlabId)) {
                throw new FdevException(ErrorConstants.PROJECT_NOT_EXIST_IN_GITLAB, new String[]{mpassComponent.getName_en()});
            }
            mpassComponent.setGitlab_id(gitlabId);
            gitlabUserService.addMembers(commonInfoService.addMembersForApp(mpassComponent.getManager(), gitlabId, Dict.MAINTAINER));
        }
        mpassComponent.setComponent_type("mpass");
        return mpassComponentDao.update(mpassComponent);
    }

    @Override
    public List<MpassComponent> queryByUserId(String user_id) {
        return mpassComponentDao.queryByUserId(user_id);
    }

    @Override
    public MpassComponent queryByGitlabId(String gitlabId) {
        return mpassComponentDao.queryByGitlabId(gitlabId);
    }

    @Autowired
    public void setMpassComponentDao(IMpassComponentDao mpassComponentDao) {
        this.mpassComponentDao = mpassComponentDao;
    }

    @Autowired
    public void setGitlabSerice(IGitlabSerice gitlabSerice) {
        this.gitlabSerice = gitlabSerice;
    }

    @Autowired
    public void setRoleService(IRoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setGitlabUserService(IGitlabUserService gitlabUserService) {
        this.gitlabUserService = gitlabUserService;
    }

    @Autowired
    public void setRestTransport(RestTransport restTransport) {
        this.restTransport = restTransport;
    }

    @Autowired
    public void setComponentRecordDao(IComponentRecordDao componentRecordDao) {
        this.componentRecordDao = componentRecordDao;
    }

    @Autowired
    public void setCommonInfoService(ICommonInfoService commonInfoService) {
        this.commonInfoService = commonInfoService;
    }

    @Autowired
    public void setComponentScanService(IComponentScanService componentScanService) {
        this.componentScanService = componentScanService;
    }
}
