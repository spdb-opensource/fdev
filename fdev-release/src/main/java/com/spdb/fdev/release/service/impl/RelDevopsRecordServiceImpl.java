package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.release.dao.IProdApplicationDao;
import com.spdb.fdev.release.dao.IProdRecordDao;
import com.spdb.fdev.release.dao.IRelDevopsRecordDao;
import com.spdb.fdev.release.entity.ProdApplication;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.entity.RelDevopsRecord;
import com.spdb.fdev.release.service.IRelDevopsRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RelDevopsRecordServiceImpl implements IRelDevopsRecordService{
	
	@Autowired
	private IRelDevopsRecordDao relDevopsRecordDao;

	@Autowired
	private IProdRecordDao prodRecordDao;

	@Autowired
	private IProdApplicationDao prodApplicationDao;

	@Autowired
	private ReleaseApplicationServiceWarImpl releaseApplicationWarService;
	
	@Override
	public RelDevopsRecord save(RelDevopsRecord relDevopsRecord)
			throws Exception {
		return relDevopsRecordDao.save(relDevopsRecord);
		 
	}

	@Override
	public List<RelDevopsRecord> query(RelDevopsRecord relDevopsRecord)
			throws Exception {
		return relDevopsRecordDao.query(relDevopsRecord);
	}
	
	@Override
	public Map<String,Object> queryImageTags(RelDevopsRecord relDevopsRecord)
			throws Exception {
		return relDevopsRecordDao.queryImageTags(relDevopsRecord);
	}

	@Override
	public RelDevopsRecord setTag(RelDevopsRecord relDevopsRecord) throws Exception {
		return relDevopsRecordDao.setTag(relDevopsRecord);
	}

	@Override
	public RelDevopsRecord findAppByMidAndAppid(String application_id, String merge_request_id) throws Exception {
	   
		return relDevopsRecordDao.findAppByMidAndAppid(application_id,merge_request_id);
	}

	@Override
	public RelDevopsRecord setUri(RelDevopsRecord relDevopsRecord) throws Exception {
		return relDevopsRecordDao.setUri(relDevopsRecord);
	}

	@Override
	public void setDevStatus(RelDevopsRecord rel) throws Exception {
		 relDevopsRecordDao.setDevStatus(rel);
	}

	@Override
	public RelDevopsRecord findAppByTagAndAppid(String application_id, String product_tag)throws Exception {
		return relDevopsRecordDao.findAppByTagAndAppid(application_id,product_tag);
	}

	@Override
	public List<String> queryTagList(RelDevopsRecord relDevopsRecord) throws Exception {		
		return relDevopsRecordDao.queryTagList(relDevopsRecord);
	}

	@Override
	public Map<String,Object> queryImageList(RelDevopsRecord relDevopsRecord) throws Exception {
		return relDevopsRecordDao.queryImageList(relDevopsRecord);
	}

	@Override
	public void updateNode(String old_release_node_name, String new_release_node_name) throws Exception {		
		relDevopsRecordDao.updateNode(old_release_node_name,new_release_node_name);
	}

	@Override
	public void changeReleaseNodeName(String release_node_name, String release_node_name_new, String application_id) throws Exception{
		relDevopsRecordDao.changeReleaseNodeName(release_node_name,release_node_name_new,application_id);
	}

    @Override
    public List<String> queryNormalTags(String release_node_name, String application_id) {
	    List<String> list = new ArrayList<>();
	    List<RelDevopsRecord> relDevopsRecords = relDevopsRecordDao.queryNormalTags(release_node_name, application_id);
	    for(RelDevopsRecord relDevopsRecord : relDevopsRecords) {
	        if(!CommonUtils.isNullOrEmpty(relDevopsRecord)
                    && !CommonUtils.isNullOrEmpty(relDevopsRecord.getProduct_tag())) {
	            list.add(relDevopsRecord.getProduct_tag());
            }
        }
        return list;
    }

	@Override
	public boolean queryDockerDir(RelDevopsRecord relDevopsRecord, ProdRecord prodRecord, String pro_image_uri) {
		boolean flag = false;
		List<String> imagelist = (List<String>) relDevopsRecordDao.queryImageTags(relDevopsRecord).get("caasImageUriList");
		if(!CommonUtils.isNullOrEmpty(imagelist) && imagelist.contains(pro_image_uri)) {
			List<ProdRecord> prodRecordList = prodRecordDao.queryBeforeProdByReleaseNode(prodRecord);
			if(!CommonUtils.isNullOrEmpty(prodRecordList)) {
				List<String> prodIds = prodRecordList.stream().map(pr -> pr.getProd_id()).collect(Collectors.toList());
				List<ProdApplication> applicationList = prodApplicationDao.queryImages(relDevopsRecord.getApplication_id(), prodIds);
				if(CommonUtils.isNullOrEmpty(applicationList)) {
					flag = true;
				}
			} else {
				flag = true;
			}
		}
		return flag;
	}

	@Override
	public RelDevopsRecord setPackageUri(RelDevopsRecord relDevopsRecord) {
		return relDevopsRecordDao.setPackageUri(relDevopsRecord);
	}

	@Override
	public List<String> queryPackageTags(RelDevopsRecord relDevopsRecord) throws Exception {
		return relDevopsRecordDao.queryPackageTags(relDevopsRecord);
	}

	@Override
	public RelDevopsRecord saveProductWar(Map<String, Object> requestParam) throws Exception {
		String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
		Integer gitlab_project_id = Integer.valueOf((String)requestParam.get(Dict.GITLAB_PROJECT_ID));
		String pro_package_uri = (String) requestParam.get(Dict.PRO_PACKAGE_URI);
		String product_tag = (String) requestParam.get(Dict.PRODUCT_TAG);
		RelDevopsRecord relDevopsRecord = new RelDevopsRecord();
		application_id = releaseApplicationWarService.queryApplicationId(application_id, gitlab_project_id);
		relDevopsRecord.setApplication_id(application_id);
		relDevopsRecord.setProduct_tag(product_tag);
		relDevopsRecord.setPro_package_uri(pro_package_uri);
		relDevopsRecord = this.setPackageUri(relDevopsRecord);
		return relDevopsRecord;
	}

	@Override
	public String queryPackageByTagAndApp(String pro_tag, String application_id) {
		return relDevopsRecordDao.queryPackageByTagAndApp(pro_tag, application_id);
	}

	@Override
	public RelDevopsRecord queryProTagByPackage(String pro_package_uri) {
		return relDevopsRecordDao.queryProTagByPackage(pro_package_uri);
	}

}