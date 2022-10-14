package com.spdb.fdev.freport.base.utils;

import com.spdb.fdev.freport.spdb.entity.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class UserUtils {

    @Value("${fdev.user.label.un-resource}")
    private String unResourceLabelId;

    @Value("${fdev.user.role.developer}")
    private String developerRoleId;

    @Value("${fdev.user.role.tester}")
    private String testerRoleId;

    public void removeByUnResourceRoleAndLabel(List<User> totalUser) {
        totalUser.removeIf(this::isUnResource);//标签中包含“非项目组资源”
    }

    public boolean isUnResource(User user) {
        return CommonUtils.isNullOrEmpty(user.getRoleId()) || //角色为空
                (!user.getRoleId().contains(developerRoleId) && !user.getRoleId().contains(testerRoleId)) || //既不是”开发“也不是”测试“
                (!CommonUtils.isNullOrEmpty(user.getLabels()) && user.getLabels().contains(unResourceLabelId));
    }
}
