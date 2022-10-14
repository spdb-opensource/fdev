package com.testmanage.admin.entity;

import java.io.Serializable;

/**
 * 功能菜单实体,节点类
 */
public class FunctionMenu implements Serializable{
    private static final long serialVersionUID = 7465810615365795763L;
    //系统id
    private Integer sys_func_id;
    //功能菜单id	
    private Integer func_id;
    //菜单级别
    private Integer level;	
    //功能名称
    private String func_model_name;
    //父级菜单id
    private Integer parent_id;
	//标识假删除字段
	private Integer is_del;
    //备选字段
    private String field1;
    private String field2;
    private String field4;
    private String field5;

   
	public Integer getSys_func_id() {
		return sys_func_id;
	}
	public void setSys_func_id(Integer sys_func_id) {
		this.sys_func_id = sys_func_id;
	}
	public Integer getFunc_id() {
        return func_id;
    }
    public void setFunc_id(Integer func_id) {
        this.func_id = func_id;
    }
    public Integer getLevel() {
        return level;
    }
    public void setLevel(Integer level) {
        this.level = level;
    }
    public String getFunc_model_name() {
        return func_model_name;
    }
    public void setFunc_model_name(String func_model_name) {
        this.func_model_name = func_model_name;
    }
    public Integer getParent_id() {
        return parent_id;
    }
    public void setParent_id(Integer parent_id) {
        this.parent_id = parent_id;
    }
    public String getField1() {
		return field1;
	}
	public void setField1(String field1) {
		this.field1 = field1;
	}
	public String getField2() {
		return field2;
	}
	public void setField2(String field2) {
		this.field2 = field2;
	}
	public Integer getIs_del() {
		return is_del;
	}
	public void setIs_del(Integer is_del) {
		this.is_del = is_del;
	}
	public String getField4() {
		return field4;
	}
	public void setField4(String field4) {
		this.field4 = field4;
	}
	public String getField5() {
		return field5;
	}
	public void setField5(String field5) {
		this.field5 = field5;
	}
	@Override
	public String toString() {
		return "FunctionMenu [sys_func_id=" + sys_func_id + ", func_id=" + func_id + ", level=" + level
				+ ", func_model_name=" + func_model_name + ", parent_id=" + parent_id + ", field1=" + field1
				+ ", field2=" + field2 + ", is_del=" + is_del + ", field4=" + field4 + ", field5=" + field5 + "]";
	}
	
	
	
	
}
