package com.spdb.fdev.spdb.dao;

import com.spdb.fdev.spdb.entity.CaasModifyKey;

import java.util.List;

/**
 * @Author:guanz2
 * @Date:2021/9/29-16:27
 * @Description:
 */
public interface ICaasModifyKeyDao {
    /*
    * @author:guanz2
    * @Description: 跟新需要修改的key,对于每个key,如果现在的数据库里有，
    * 就update，如果没有就insert
    */
    void updateModifyKeys(List<CaasModifyKey> list);

    /*
    * @author:guanz2
    * @Description:查询私有的修改的key
    */
    CaasModifyKey queryModifyKeys(String deployment, String cluster, String namespace);


    /*
    * @author:guanz2
    * @Description:查询共有的key
    */
    CaasModifyKey queryCommonModifyKeys();
}
