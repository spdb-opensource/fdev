package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;

import java.util.*;

@ApiModel(value="任务关联评估项")
public class TaskReview {
	
	@ApiModelProperty(value="涉及其他系统")
	private TaskReviewChild[] other_system;
	
	@ApiModelProperty(value="数据库变更")
	private TaskReviewChild[] data_base_alter;
	
	/*@ApiModelProperty(value="集中配置文件")
	private TaskReviewChild[] ebank_common_alter;
	
	@ApiModelProperty(value="防火墙变更")
	private TaskReviewChild[] fire_wall_open;
	
	@ApiModelProperty(value="接口变更")
	private TaskReviewChild[] interface_alter;
	
	@ApiModelProperty(value="脚本变更")
	private TaskReviewChild[] script_alter;
	
	@ApiModelProperty(value="静态资源变更")
	private TaskReviewChild[] static_resource;*/

	@ApiModelProperty(value = "是否涉及安全测试")
	private String securityTest = "";

	@ApiModelProperty(value = "是否涉及特殊情况")
	private List<String> specialCase = new ArrayList<>(4);

	@ApiModelProperty(value = "公共配置文件更新")
	private Boolean commonProfile = false;


	public TaskReview() {
		
	}
	//该任关联项是否全部审核完成
	public boolean allChecked(){
		Map<String ,Boolean> checkResultMap =  checkTaskReview();
		Iterator<String> keysIt = checkResultMap.keySet().iterator();
		while(keysIt.hasNext()) {
			String key = keysIt.next();
			//只检测数据库审核
			if (key.equals("data_base_alter")) {
				boolean value = checkResultMap.get(key);
				if (!value) {
					return false;
				}
			}else continue;
		}
		return true;
	}
	//该任关联项是否审核完成
	public Map<String ,Boolean> checkTaskReview() {
		Map<String ,Boolean> result = new HashMap<String, Boolean>();
		result.put("data_base_alter", opFalse(this.data_base_alter));
		result.put("other_system", opFalse(this.other_system));
		/*result.put("ebank_common_alter", opFalse(this.ebank_common_alter));
		result.put("fire_wall_open", opFalse(this.fire_wall_open));
		result.put("interface_alter", opFalse(this.interface_alter));
		result.put("script_alter", opFalse(this.script_alter));
		result.put("static_resource", opFalse(this.static_resource));*/
		return result;
	}
	//该任关联项某一项是否审核完成
	public boolean opFalse(TaskReviewChild[] views) {
		if(null==views) {
			return true;
		}
		for(TaskReviewChild view:views) {
			//为否的关联项不需要审核
			if("否".equals(view.getName())){
				continue;
			}
			if(view.getAudit() == null ||!view.getAudit()) {
				return false;
			}
		}
		return true;
	}


	public TaskReview viewId(){
		id(this.data_base_alter);
		id(this.other_system);
		/*id(this.ebank_common_alter);
		id(this.fire_wall_open);
		id(this.interface_alter);
		id(this.script_alter);
		id(this.static_resource);*/
		return this;
	}
	
	public void id(TaskReviewChild[] views) {
		if(null==views){
			return ;
		}
		for(TaskReviewChild view :views) {
			String id = view.getId();
			if(null==id) {
				view.setId(new ObjectId().toString());
			}
		}
	}



	public TaskReviewChild[] getOther_system() {
		return other_system;
	}



	public void setOther_system(TaskReviewChild[] other_system) {
		this.other_system = other_system;
	}



	public TaskReviewChild[] getData_base_alter() {
		return data_base_alter;
	}



	public void setData_base_alter(TaskReviewChild[] data_base_alter) {
		this.data_base_alter = data_base_alter;
	}


/*
	public TaskReviewChild[] getEbank_common_alter() {
		return ebank_common_alter;
	}



	public void setEbank_common_alter(TaskReviewChild[] ebank_common_alter) {
		this.ebank_common_alter = ebank_common_alter;
	}



	public TaskReviewChild[] getFire_wall_open() {
		return fire_wall_open;
	}



	public void setFire_wall_open(TaskReviewChild[] fire_wall_open) {
		this.fire_wall_open = fire_wall_open;
	}



	public TaskReviewChild[] getInterface_alter() {
		return interface_alter;
	}



	public void setInterface_alter(TaskReviewChild[] interface_alter) {
		this.interface_alter = interface_alter;
	}



	public TaskReviewChild[] getScript_alter() {
		return script_alter;
	}



	public void setScript_alter(TaskReviewChild[] script_alter) {
		this.script_alter = script_alter;
	}



	public TaskReviewChild[] getStatic_resource() {
		return static_resource;
	}



	public void setStatic_resource(TaskReviewChild[] static_resource) {
		this.static_resource = static_resource;
	}*/

	public List<String> getSpecialCase() {
		return specialCase;
	}

	public void setSpecialCase(List<String> specialCase) {
		this.specialCase = specialCase;
	}

	public String getSecurityTest() {
		return securityTest;
	}

	public void setSecurityTest(String securityTest) {
		this.securityTest = securityTest;
	}

	public Boolean getCommonProfile() {
		return commonProfile;
	}

	public void setCommonProfile(Boolean commonProfile) {
		this.commonProfile = commonProfile;
	}
}
