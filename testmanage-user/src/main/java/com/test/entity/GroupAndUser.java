package com.test.entity;

import java.io.Serializable;
import java.util.Map;

public class GroupAndUser implements Serializable{
	
	private String group_id;


    private String group_name;
    
    //工单管理员
    private String work_manager;
    
    //工单管理员中文名
    private String user_name;
    
    //分配的测试组长英文名
    private String work_leader; 
    

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getWork_manager() {
		return work_manager;
	}

	public void setWork_manager(String work_manager) {
		this.work_manager = work_manager;
	}

	public String getWork_leader() {
		return work_leader;
	}

	public void setWork_leader(String work_leader) {
		this.work_leader = work_leader;
	}

	public String getGroup_id() {
        return group_id;

    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }
		
	//分配到的组长的中英文名
    private Map<String,String> groupLeader;
    
   
    public Map<String,String> getGroupLeader() {
		return groupLeader;
	}

	public void setGroupLeader(Map<String,String> groupLeader) {
		this.groupLeader = groupLeader;
	}

}
