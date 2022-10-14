package com.spdb.fdev.spdb.dao;

import com.spdb.fdev.spdb.entity.SccDeploy;

import java.util.List;

/**
 * @Author:guanz2
 * @Date:2021/10/3-17:41
 * @Description: scc_deploy
 */
public interface ISccDeploymentDao {

    /*
    * @author:guanz2
    * @Description:添加数据
    */
    void addAll(List<SccDeploy> list);

    //删除集合
    void dropCollection(String collectionName);


    List<SccDeploy> getSccDeployCondition(String resource_code);

    /*
    * @author:guanz2
    * @Description: 获取SccDeploy
    * 入参：namespace_code   名字空间代码
    *      resource_code   资源代码
    * 返回值：SccDeploy
    */
    SccDeploy getSccDeployCondition(String namespace_code, String resource_code);

    /*
    * @author:guanz2
    * @Description: 查询所有应用
    */
    List getAllSccDeploy();

    long querySccCount();

    List<String> getDistinctField(String field);

    /*
    * @author:guanz2
    * @Description:模糊查询
    */
    List<SccDeploy> fuzzyQueryCaasDeployment(String key, String vlaue);
}
