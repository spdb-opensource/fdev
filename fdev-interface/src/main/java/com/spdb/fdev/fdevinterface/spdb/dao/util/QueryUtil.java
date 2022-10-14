package com.spdb.fdev.fdevinterface.spdb.dao.util;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.spdb.vo.InterfaceParamShow;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class QueryUtil {

    private QueryUtil() {
    }

    /**
     * 接口关系页查询条件
     *
     * @param interfaceParamShow
     * @return
     */
    public static Criteria getRelationCriteria(InterfaceParamShow interfaceParamShow) {
        Criteria criteria = getCriteria(interfaceParamShow);
        String interfaceType = interfaceParamShow.getInterfaceType();
        String serviceCalling = interfaceParamShow.getServiceCalling();
        if (serviceCalling != null) {
            Pattern pattern;
            if (Dict.SOAP.equals(interfaceType)) {
                // 过滤调用方为msper-web-common-service的SOAP数据
                pattern = Pattern.compile("^(?!.*" + Dict.MSPER_WEB_COMMON_SERVICE + ")" + "^.*"
                        + interfaceParamShow.getServiceCalling() + ".*$", Pattern.CASE_INSENSITIVE);
            } else {
                pattern = Pattern.compile("^.*" + serviceCalling + ".*$", Pattern.CASE_INSENSITIVE);
            }
            criteria.and(Dict.SERVICE_CALLING).regex(pattern);
        }
        return criteria;
    }

    /**
     * 接口列表页查询条件
     *
     * @param interfaceParamShow
     * @return
     */
    public static Criteria getCriteria(InterfaceParamShow interfaceParamShow) {
        Criteria criteria = new Criteria();
        // 设置分支
        String branchDefault = interfaceParamShow.getBranchDefault();
        if (Dict.OTHER.equals(branchDefault)) {
            Pattern pattern = Pattern.compile(
                    "^(?!.*" + Dict.MASTER + "|" + Dict.SIT + ")" + "^.*" + interfaceParamShow.getBranch() + ".*$",
                    Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.BRANCH).regex(pattern);
        } else {
            criteria.and(Dict.BRANCH).is(branchDefault);
        }
        List<String> transIdList = interfaceParamShow.getTransId();
        if (CollectionUtils.isNotEmpty(transIdList) && !Dict.SOAP.equals(interfaceParamShow.getInterfaceType())) {
            if (transIdList.size() == 1) {
                Pattern pattern = Pattern.compile("^.*" + transIdList.get(0) + ".*$", Pattern.CASE_INSENSITIVE);
                criteria.and(Dict.TRANS_ID).regex(pattern);
            } else {
                criteria.and(Dict.TRANS_ID).in(transIdList);
            }
        }
        String serviceId = interfaceParamShow.getServiceId();
        if (!StringUtils.isEmpty(serviceId)) {
            Pattern pattern = Pattern.compile("^.*" + serviceId + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.SERVICE_ID).regex(pattern);
        }
        String interfaceName = interfaceParamShow.getInterfaceName();
        if (!StringUtils.isEmpty(interfaceName)) {
            Pattern pattern = Pattern.compile("^.*" + interfaceName + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.INTERFACE_NAME).regex(pattern);
        }
        criteria.and(Dict.IS_NEW).is(1);
        return criteria;
    }

    /**
     * 路由列表查询条件
     *
     * @param params
     * @return
     */
    public static Criteria getRoutesCriteria(Map params) {
        Criteria criteria = new Criteria();
        String name = (String) params.get(Dict.NAME);
        String projectName = (String) params.get(Dict.PROJECTNAME);
        String branch = (String) params.get(Dict.BRANCH);
        String path = (String) params.get(Dict.PATH);
        String module = (String) params.get(Dict.MODULE);
        Integer version = (Integer) params.get(Dict.VER);
        if (!StringUtils.isEmpty(name)) {
            Pattern pattern = Pattern.compile("^.*" + name + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.NAME).regex(pattern);
        }
        if (!StringUtils.isEmpty(projectName)) {
            Pattern pattern = Pattern.compile("^.*" + projectName + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.PROJECT_NAME).regex(pattern);
        }
        if (!StringUtils.isEmpty(branch)) {
            Pattern pattern = Pattern.compile("^.*" + branch + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.BRANCH).regex(pattern);
        } else {
            criteria.and(Dict.BRANCH).is(Dict.MASTER);
        }
        if (!StringUtils.isEmpty(path)) {
            Pattern pattern = Pattern.compile("^.*" + path + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.PATH).regex(pattern);
        }
        if (!StringUtils.isEmpty(module)) {
            Pattern pattern = Pattern.compile("^.*" + module + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.MODULE).regex(pattern);
        }
        // 不传版本，默认查询最新版本
        if (version == null) {
            criteria.and(Dict.IS_NEW).is(1);
        } else {
            criteria.and(Dict.VER).is(version);
        }
        return criteria;
    }

    /**
     * 路由调用关系查询条件
     *
     * @param params
     * @return
     */
    public static Criteria getRoutesRelationCriteria(Map params) {
        Criteria criteria = new Criteria();
        String name = (String) params.get(Dict.NAME);
        String sourceProject = (String) params.get("sourceProject");
        String branch = (String) params.get(Dict.BRANCH);
        String targetProject = (String) params.get("targetProject");
        if (!StringUtils.isEmpty(name)) {
            Pattern pattern = Pattern.compile("^.*" + name + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.NAME).regex(pattern);
        }
        if (!StringUtils.isEmpty(sourceProject)) {
            Pattern pattern = Pattern.compile("^.*" + sourceProject + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.SOURCE_PROJECT).regex(pattern);
        }
        if (!StringUtils.isEmpty(branch)) {
            Pattern pattern = Pattern.compile("^.*" + branch + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.BRANCH).regex(pattern);
        } else {
            criteria.and(Dict.BRANCH).is(Dict.MASTER);
        }
        if (!StringUtils.isEmpty(targetProject)) {
            Pattern pattern = Pattern.compile("^.*" + targetProject + ".*$", Pattern.CASE_INSENSITIVE);
            criteria.and(Dict.TARGET_PROJECT).regex(pattern);
        }
        return criteria;
    }
}
