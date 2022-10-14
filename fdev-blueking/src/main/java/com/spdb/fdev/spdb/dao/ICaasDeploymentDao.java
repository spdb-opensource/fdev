package com.spdb.fdev.spdb.dao;

import com.spdb.fdev.spdb.entity.CaasDeployment;

import java.util.List;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/9/29-16:23
 * @Description:caas_deployment的dao 层操作
 */
public interface ICaasDeploymentDao {

    /*
    * @author:guanz2
    * @Description：想caas_deployment插入deployment信息
    */
    void addAll(List<CaasDeployment> listDeployment);

    /*
    * @author:guanz2
    * @Description:删除caas_deployment集合
    */
    void dropCollection(String collectionName);

    /*
    * @author:guanz2
    * @Description:根据deployment的名字查询
    */
    List<CaasDeployment> queryCaasDeploymentCondition(String deploymentName);

    /*
    * @author:guanz2
    * @Description: 根据网段、应用名、网段进行查询
    */
    CaasDeployment queryCaasDeploymentCondition(String deploymentName, String cluster, String vlan);
    /*
    * @author:guanz2
    * @Description:查询数量
    */
    long queryCountByConditon(String cluster);

    /*
    * @author:guanz2
    * @Description:根据条件，进行模糊查询
    */
    List<CaasDeployment> fuzzyQueryCaasDeployment(String key, String vlaue);

    /*
    * @author:guanz2
    * @Description:获取所有的应用列表
    */
    List<String> getDistinctField(String field);
}
