package com.spdb.fdev.spdb.dao;

import com.spdb.fdev.spdb.entity.SccDeploy;
import com.spdb.fdev.spdb.entity.SccModifyKey;

import java.util.List;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/10/3-17:43
 * @Description: scc_modifyKey
 */
public interface ISccModifyKeyDao {


   void updateModifyKeyDeploy(List<SccModifyKey> list);

   List<SccModifyKey> getModifyDeploy(String resource_code);


}
