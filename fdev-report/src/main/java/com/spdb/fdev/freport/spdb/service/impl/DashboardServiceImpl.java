package com.spdb.fdev.freport.spdb.service.impl;

import com.spdb.fdev.common.User;
import com.spdb.fdev.freport.base.dict.DashboardDict;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.spdb.dao.ReportDao;
import com.spdb.fdev.freport.spdb.entity.report.DashBoardUserConfig;
import com.spdb.fdev.freport.spdb.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ReportDao reportDao;

    @Value("${fdev.user.group.default.ids}")
    private String defaultGroupIds;

    @Override
    public DashBoardUserConfig queryUserConfig() throws Exception {
        DashBoardUserConfig userConfig = reportDao.findUserConfig(new DashBoardUserConfig() {{
            User sessionUser = CommonUtils.getSessionUser();
            setUserId(sessionUser.getId());
            setGroupId(sessionUser.getGroup_id());
        }});
        if (CommonUtils.isNullOrEmpty(userConfig)) {
            userConfig = new DashBoardUserConfig() {{
                setConfigs(DashboardDict.defaultConfig);
            }};
        }
        return userConfig;
    }

    @Override
    public void addUserConfig(List<Map<String, Object>> configs) throws Exception {
        reportDao.upsertDashBoardUserConfig(new DashBoardUserConfig() {{
            User sessionUser = CommonUtils.getSessionUser();
            setUserId(sessionUser.getId());
            setGroupId(sessionUser.getGroup_id());
            setConfigs(configs);
        }});
    }

    @Override
    public Set<String> queryDefaultGroupIds() throws Exception {
        return new HashSet<>(Arrays.asList(defaultGroupIds.split(",")));
    }

}
