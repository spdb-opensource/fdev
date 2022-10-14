package com.spdb.fdev.release.dao;

import com.spdb.fdev.release.entity.ProdTemplate;

import java.util.List;

public interface IProdTemplateDao { 
     
   ProdTemplate create(ProdTemplate releaseTemplate) throws Exception;

   List<ProdTemplate> query(ProdTemplate releaseTemplate) throws Exception;

   ProdTemplate queryDetail(ProdTemplate releaseTemplate) throws Exception;

   ProdTemplate update(ProdTemplate releaseTemplate) throws Exception;

   boolean delete(String tempId) throws Exception;

   void cancel(String template_id) throws Exception;

   ProdTemplate queryDetailById(String template_id);

   List<ProdTemplate> findExists(ProdTemplate prodTemplate);

   List<ProdTemplate> queryByGroupType(ProdTemplate prodTemplate);

   List<ProdTemplate> queryList(String owner_group, String owner_system, String type);
}
