package com.spdb.fdev.fuser.spdb.dao;

import java.util.List;

import com.spdb.fdev.fuser.spdb.entity.user.Label;

public interface LabelDao {
	
	
	/**新增一个标签*/
	Label addLabel(Label label);
	
	/**删除一个标签*/
	Label delLabelByID(String id);
	
	/**修改标签
	 * */
	Label updateLabel(Label label) throws Exception;
	
	/**查询标签*/
	List<Label> queryLabel(Label label) throws Exception;

	/**通过name模糊查询标签*/
	List<Label> queryLableByName(String label);
}
