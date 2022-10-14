package com.test.entity;


import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.stereotype.Component;

import java.io.Serializable;

@Table(name="FTMS_USER")
@Component
public class User implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 2583093419733801695L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,generator = "JDBC")
    private Integer user_id;//ID

    private String role_en_name;//USER_ROLE_CODE

    private String user_en_name;//USER_CODE 

    private String user_name;//USER_NAME

    private String user_phone;//USER_PHONE

    private String email;//USER_EMAIL

    private String password;//USER_PSWD

    private String rank;//IS_BANK

    private String test_direction;

    private String group_id;// GROUP_ID
    
    private String group_name;
    
    private String role_describe;
    
    private String mantis_token;//MANTIS_TOKEN
    
    private String is_leave;//IS_LEAVE
    
    private String company;//USER_COMPANY
    
    private Integer role_weight;
    
    private String create_tm;
    
    private String create_opr;
    
	private String fnl_opr;
    
    private String fnl_tm; 
    
    private String is_work_manager;
    
    private String is_work_leader; 
    
    private String level;//资深-3、高级-2、中级-1、初级-0
    

	public String getIs_work_manager() {
		return is_work_manager;
	}

	public void setIs_work_manager(String is_work_manager) {
		this.is_work_manager = is_work_manager;
	}

	public String getIs_work_leader() {
		return is_work_leader;
	}

	public void setIs_work_leader(String is_work_leader) {
		this.is_work_leader = is_work_leader;
	}

	public String getCREATE_TM() {
		return create_tm;
	}

	public void setCREATE_TM(String cREATE_TM) {
		create_tm = cREATE_TM;
	}

	public String getCREATE_OPR() {
		return create_opr;
	}

	public void setCREATE_OPR(String cREATE_OPR) {
		create_opr = cREATE_OPR;
	}    

    public String getFNL_OPR() {
		return fnl_opr;
	}

	public void setFNL_OPR(String fNL_OPR) {
		fnl_opr = fNL_OPR;
	}

	public String getFNL_TM() {
		return fnl_tm;
	}

	public void setFNL_TM(String fNL_TM) {
		fnl_tm = fNL_TM;
	}

	public Integer getRole_weight() {
		return role_weight;
	}

	public void setRole_weight(Integer role_weight) {
		this.role_weight = role_weight;
	}

	public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer id) {
        this.user_id = id;
    }

    public String getRole_en_name() {
        return role_en_name;
    }

    public void setRole_en_name(String role_en_name) {
        this.role_en_name = role_en_name;
    }

    public String getUser_en_name() {
        return user_en_name;
    }

    public void setUser_en_name(String user_en_name) {
        this.user_en_name = user_en_name;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getTest_direction() {
        return test_direction;
    }

    public void setTest_direction(String test_direction) {
        this.test_direction = test_direction;
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

	public String getRole_describe() {
		return role_describe;
	}

	public void setRole_describe(String role_describe) {
		this.role_describe = role_describe;
	}

	public String getMantis_token() {
		return mantis_token;
	}

	public void setMantis_token(String mantis_token) {
		this.mantis_token = mantis_token;
	}
	
	public String getIs_leave() {
		return is_leave;
	}

	public void setIs_leave(String is_leave) {
		this.is_leave = is_leave;
	}
	
	
	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}
	
	
	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public User() {
    }

    public User(String role_en_name, String user_en_name, String user_name, String user_phone, String email, String password, String rank, String test_direction, String group_id) {
        this.role_en_name = role_en_name;
        this.user_en_name = user_en_name;
        this.user_name = user_name;
        this.user_phone = user_phone;
        this.email = email;
        this.password = password;
        this.rank = rank;
        this.test_direction = test_direction;
        this.group_id = group_id;
    }
}
