package com.spdb.fdev.fuser.spdb.service.Impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.LabelDao;
import com.spdb.fdev.fuser.spdb.entity.user.Label;
import com.spdb.fdev.fuser.spdb.service.LabelService;

@Service
public class LabelServiceImpl implements LabelService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());//日志打印
	
	@Resource
	private LabelDao labelDao;
	

	@Override
	public Label addLabel(Label label) throws Exception {
		List<Label> lab = labelDao.queryLabel(new Label(null,label.getName(),null,null));
		if(!CommonUtils.isNullOrEmpty(lab)) {//该数据已经存在
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR,new String[]{"标签名重复"});
		}
		return labelDao.addLabel(label);
	}

	@Override
	public Label delLabelByID(String id) {
		return labelDao.delLabelByID(id);
	}


	public Label updateLabel(Label label) throws Exception{
		List<Label> lab = labelDao.queryLabel(new Label(label.getId(),null,null,null));
		if(CommonUtils.isNullOrEmpty(lab)) {//该数据不存在
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"标签为空"});
		}
		return labelDao.updateLabel(label);
	}


	@Override
	public List<Label> queryLabel(Label label) throws Exception{
		return labelDao.queryLabel(label);
	}
	
}
