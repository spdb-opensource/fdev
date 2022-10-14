
package com.spdb.fdev.release.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spdb.fdev.release.dao.IProdTemplateDao;
import com.spdb.fdev.release.entity.ProdTemplate;
import com.spdb.fdev.release.service.IProdTemplateService;

@Service
public class ProdTemplateServiceImpl implements IProdTemplateService {
	
	@Autowired
	private IProdTemplateDao releaseTemplateDao;
	
	@Override
	public ProdTemplate create(ProdTemplate releaseTemplate) throws Exception {
		return releaseTemplateDao.create(releaseTemplate);
	}

	@Override
	public List<ProdTemplate> query(ProdTemplate releaseTemplate) throws Exception{
		return releaseTemplateDao.query(releaseTemplate);
	}

	@Override
	public ProdTemplate queryDetail(ProdTemplate releaseTemplate) throws Exception{
		
		return releaseTemplateDao.queryDetail(releaseTemplate);
	}

	@Override
	public ProdTemplate update(ProdTemplate releaseTemplate) throws Exception {
		return releaseTemplateDao.update(releaseTemplate);
	}

	@Override
	public boolean delete(String tempId) throws Exception {
	
		return releaseTemplateDao.delete(tempId);
	}

	@Override
	public void cancel(String template_id) throws Exception {
		 releaseTemplateDao.cancel(template_id);
	}

	@Override
	public ProdTemplate queryDetailById(String template_id) {
		return releaseTemplateDao.queryDetailById(template_id);
	}

	@Override
	public List<ProdTemplate> findExists(ProdTemplate prodTemplate) {
		return releaseTemplateDao.findExists(prodTemplate);
	}

	@Override
	public List<ProdTemplate> queryByGroupType(ProdTemplate prodTemplate) {
		return releaseTemplateDao.queryByGroupType(prodTemplate);
	}

	@Override
	public List<ProdTemplate> queryList(String owner_group, String owner_system, String type) {
		return releaseTemplateDao.queryList(owner_group, owner_system, type);
	}

}
