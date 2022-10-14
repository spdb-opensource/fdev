package com.spdb.fdev.fuser.spdb.service;

import java.util.List;

import com.spdb.fdev.fuser.spdb.entity.user.Label;

public interface LabelService {
	
	/**-----------------label----------------------------------*/
	/**新增一个标签*/
	Label addLabel(Label label)  throws Exception;
	
	/**删除一个标签*/
	Label delLabelByID(String id);
	
	/**更换标签名称*/
	Label updateLabel(Label label) throws Exception;
	
	/**查询标签*/
	List<Label> queryLabel(Label label) throws Exception;
	
}
