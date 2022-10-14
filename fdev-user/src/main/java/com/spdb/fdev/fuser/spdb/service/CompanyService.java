package com.spdb.fdev.fuser.spdb.service;

import java.util.List;

import com.spdb.fdev.fuser.spdb.entity.user.Company;

public interface CompanyService {
	/**-----------------Company----------------------------------*/
	/**查询公司*/
	List<Company> getCompany(Company company) throws Exception;
	
	/**更新一个公司*/
	Company updateCompany(Company company)throws Exception;
	
	/**新增一个公司*/
	Company addCompany(Company company)throws Exception;
	
	/**删除公司*/
	Company delCompany(Company company)throws Exception;

	/**
	 * 根据公司名称查询公司信息
	 * @param groupName
	 * @return
	 */
    Company queryByName(String groupName);
}
