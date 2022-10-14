package com.testmanage.admin.entity;

import java.io.Serializable;

/**
 * 系统实体
 */
public class SystemModel implements Serializable{

    private static final long serialVersionUID = -482351541975513868L;
    //系统id
    private Integer sys_id;
    //系统中文名
    private String sys_module_name;
    //系统英文名
    private String sys_module_en_name;
    
    //备选字段
    private String field1;
    private String field2;
    private String field3;
    private String field4;
    private String field5;

    public Integer getSys_id() {
        return sys_id;
    }

    public void setSys_id(Integer sys_id) {
        this.sys_id = sys_id;
    }

    public String getSys_module_name() {
        return sys_module_name;
    }

    public void setSys_module_name(String sys_module_name) {
        this.sys_module_name = sys_module_name;
    }

    public String getSys_module_en_name() {
        return sys_module_en_name;
    }

    public void setSys_module_en_name(String sys_module_en_name) {
        this.sys_module_en_name = sys_module_en_name;
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

	public String getField3() {
		return field3;
	}

	public void setField3(String field3) {
		this.field3 = field3;
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
		return "SystemModel [sys_id=" + sys_id + ", sys_module_name=" + sys_module_name + ", sys_module_en_name="
				+ sys_module_en_name + ", field1=" + field1 + ", field2=" + field2 + ", field3=" + field3 + ", field4="
				+ field4 + ", field5=" + field5 + "]";
	}
	
	
	
}
