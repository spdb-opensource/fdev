package com.spdb.fdev.fdevapp.base.utils.validate;


import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.entity.GitlabCI;

/**
 * @author xxx
 * @date 2019/5/16 17:04
 */
public class ValidateGitlabCI {

    /**
     * 检查 gitlabCI 是否为空
     * @param gitlabCI
     */
    public static void checkGitlabCI(GitlabCI gitlabCI) {
        if (null == gitlabCI) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"持续集成模板不存在"});
        }
    }

    /**
     * 检查 gitlabCI yaml name  是否为空
     * @param gitlabCI
     */
    public static void checkYamlName(GitlabCI gitlabCI) {
        checkGitlabCI(gitlabCI);
        if (CommonUtils.isNullOrEmpty(gitlabCI.getYaml_name())) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"持续集成模板名字不存在"});
        }
    }
}
