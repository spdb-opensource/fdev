package com.spdb.fdev.fdevapp.spdb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.base.utils.DateUtil;
import com.spdb.fdev.fdevapp.spdb.dao.IAppEntityDao;
import com.spdb.fdev.fdevapp.spdb.dao.IAutoTestDao;
import com.spdb.fdev.fdevapp.spdb.entity.AppEntity;
import com.spdb.fdev.fdevapp.spdb.entity.AutoTestEnv;
import com.spdb.fdev.fdevapp.spdb.service.IAutoTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

@Service
public class AutoTestServiceImpl implements IAutoTestService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

    @Autowired
    IAutoTestDao autoTestDao;
    @Autowired
    IAppEntityDao appEntityDao;
    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Override
    public AutoTestEnv query(AutoTestEnv entity) throws Exception {
        AutoTestEnv autoTest = autoTestDao.find(entity);
        if (CommonUtils.isNullOrEmpty(autoTest)) {
            return null;
        }
        return autoTest;
    }

    @Override
    public AutoTestEnv update(AutoTestEnv autoTest) throws Exception {

        String nowDate = getNowDate();
        Boolean authorityManager = false;
        Integer appId = Integer.parseInt(autoTest.getGitlab_project_id());
        //从数据库拿到应用实体queryappEntity
        AppEntity appEntity = appEntityDao.getAppByGitlabId(appId);
        if (CommonUtils.isNullOrEmpty(appEntity)) {
            throw new FdevException(ErrorConstants.APP_NOT_EXIST);
        }
        User user = (User) ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest()
                .getSession().getAttribute(Dict._USER);
        if (null == user) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"用户身份验证失败"});
        }
        //判断用户是否是当前应用的行内项目负责人
        for (Object object : appEntity.getSpdb_managers()) {
            Map spdb_managers = (Map) JSONObject.toJSON(object);
            if (user.getUser_name_en().equals(spdb_managers.get(Dict.USER_NAME_EN))) {
                authorityManager = true;
                break;
            }
        }
        //判断用户是否是当前应用的厂商项目负责人
        for (Object object1 : appEntity.getDev_managers()) {
            Map dev_managers = (Map) JSONObject.toJSON(object1);
            if (user.getUser_name_en().equals(dev_managers.get(Dict.USER_NAME_EN))) {
                authorityManager = true;
                break;
            }
        }
        //判断用户是否是卡点管理员
        if (userVerifyUtil.userRoleIsSPM(user.getRole_id())) {
            authorityManager = true;
        }
        if (authorityManager == false) {
            throw new FdevException(ErrorConstants.ROLE_ERROR, new String[]{"用户权限不足"});
        }

        // 选择环境才能配置内测自动化测试环境的开关
        String sitProjectDeploySwitchInfo = autoTest.getSitProjectDeploySwitchInfo();
        List<Map<String, String>> sitAutoTest = autoTest.getSitAutoTest();
        if (!CommonUtils.isNullOrEmptys(sitAutoTest)) {
            if (CommonUtils.isNullOrEmpty(sitProjectDeploySwitchInfo)) {
                autoTest.setSitProjectDeploySwitchInfo(Dict.ZERO);
            }
        } else {
            // 环境为空时，对内测自动化测试环境 置空  不做任何操作
            autoTest.setSitProjectDeploySwitchInfo(new String());
            autoTest.setSitAutoTest(new ArrayList<Map<String, String>>());
        }


        // 选择环境才能配置UAT自动化测试环境的开关
        String uatProjectDeploySwitchInfo = autoTest.getUatProjectDeploySwitchInfo();
        List<Map<String, String>> uatAutoTest = autoTest.getUatAutoTest();
        if (!CommonUtils.isNullOrEmptys(uatAutoTest)) {
            if (CommonUtils.isNullOrEmpty(uatProjectDeploySwitchInfo))
                autoTest.setUatProjectDeploySwitchInfo(Dict.ZERO);
        } else {
            // 环境为空时， 对UAT自动化测试环境置空 不做任何操作
            autoTest.setUatProjectDeploySwitchInfo(new String());
            autoTest.setUatAutoTest(new ArrayList<Map<String, String>>());
        }


        //选择环境才能配置REL自动化测试环境的开关
        String relProjectDeploySwitchInfo = autoTest.getRelProjectDeploySwitchInfo();
        List<Map<String, String>> relAutoTest = autoTest.getRelAutoTest();
        if (!CommonUtils.isNullOrEmptys(relAutoTest)) {
            if (CommonUtils.isNullOrEmpty(relProjectDeploySwitchInfo))
                autoTest.setRelProjectDeploySwitchInfo(Dict.ZERO);
        } else {
            // 对REL自动化测试环境 置空  不做任何操作
            autoTest.setRelProjectDeploySwitchInfo(new String());
            autoTest.setRelAutoTest(new ArrayList<Map<String, String>>());
        }
        autoTest.setLastUpdateDate(nowDate);
        autoTest.setLastUpdateUserId(user.getId());
        AutoTestEnv oldAutoTest = query(autoTest);
        if (CommonUtils.isNullOrEmpty(oldAutoTest)) { //如果当前信息不存在，即新增操作，设置创建时间
            autoTest.setCreateDate(nowDate);
        } else
            autoTest.setCreateDate(oldAutoTest.getCreateDate());    //dao层有全量更新
        return autoTestDao.update(autoTest);
    }

    @Override
    public Map autoTestEnv(Map requestParam) throws Exception {
        String gitlabId = String.valueOf(requestParam.get(Dict.GITLAB_PROJECT_ID));
        String branch = (String) requestParam.get(Dict.BRANCH);
        AutoTestEnv entity = new AutoTestEnv();
        entity.setGitlab_project_id(gitlabId);
        boolean flag = false;
        Map returnmap = new HashMap();
        AutoTestEnv autoTest = autoTestDao.find(entity);
        if (CommonUtils.isNullOrEmpty(autoTest)) {
            logger.error("自动化测试实体不存在，gitlabId：" + gitlabId);
            returnmap.put(Dict.AUTOSWITCH, flag);
            return returnmap;
        }
        String autoSwitch = null;
        List<String> envNames = new ArrayList<String>();
        String name = null;
        if (branch.startsWith(Dict.SIT_UP) || branch.startsWith(Dict.SIT)) {
            autoSwitch = autoTest.getSitProjectDeploySwitchInfo();//获取开关状态
            if (autoSwitch.equals(Dict.ONE)) {
                flag = true;
            }
            name = Dict.SIT_UP;
            for (Map<String, String> item : autoTest.getSitAutoTest()) {
                if (!CommonUtils.isNullOrEmpty(item.get(Dict.SITAUTOTESTENVNAME)))
                    envNames.add(item.get(Dict.SITAUTOTESTENVNAME));
            }
        } else if (branch.startsWith(Dict.RELEASE)) {
            autoSwitch = autoTest.getUatProjectDeploySwitchInfo();//获取开关状态
            if (autoSwitch.equals(Dict.ONE)) {
                flag = true;
            }
            name = Dict.UAT_UP;
            for (Map<String, String> item : autoTest.getUatAutoTest()) {
                if (!CommonUtils.isNullOrEmpty(item.get(Dict.UATAUTOTESTENVNAME)))
                    envNames.add(item.get(Dict.UATAUTOTESTENVNAME));
            }

        } else if (branch.startsWith(Dict.PRO)) {
            autoSwitch = autoTest.getRelProjectDeploySwitchInfo();//获取开关状态
            if (autoSwitch.equals(Dict.ONE)) {
                flag = true;
            }
            name = Dict.REL;
            for (Map<String, String> item : autoTest.getRelAutoTest()) {
                if (!CommonUtils.isNullOrEmpty(item.get(Dict.RELAUTOTESTENVNAME)))
                    envNames.add(item.get(Dict.RELAUTOTESTENVNAME));
            }
        }
        if (CommonUtils.isNullOrEmpty(name)) {
            logger.error("分支不存在：" + branch);
            returnmap.put(Dict.AUTOSWITCH, flag);
            return returnmap;
        }
        returnmap.put(Dict.AUTOSWITCH, flag);
        returnmap.put(Dict.NAME, name);
        returnmap.put(Dict.ENV_NAME, envNames);

        return returnmap;
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public String getNowDate() {
        return DateUtil.getDate(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

}
