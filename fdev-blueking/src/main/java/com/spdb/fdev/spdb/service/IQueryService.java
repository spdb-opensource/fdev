package com.spdb.fdev.spdb.service;

import java.util.List;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/12/10-16:54
 * @Description:查询
 */
public interface IQueryService {
    /*
    * @author:guanz2
    * @Description: 查询caas, scc所有的deployemnt,pod的数量信息
    */
    Map queryAllCount();

    /*
    * @author:guanz2
    * @Description: 获取caas,scc的集群信息
    */
    Map queryClusterInfo(String deploy_name) throws Exception;

    /*
    * @author:guanz2
    * @Description:获取caas应用的详细信息
    */
    Map getCaasDeploymentDetail(String deploy_name, String cluster, String vlan) throws  Exception;

    /*
    * @author:guanz2
    * @Description:获取scc应用的详细信息
    */
    Map getSccDeploymentDetail(String deploy_name, String namespace, String vlan) throws Exception;

    /*
    * @author:guanz2
    * @Description:根据条件，查询caas,scc平台包含的应用
    */
    Map getDeployment(String page, String pageSize, Map params) throws Exception;
}
