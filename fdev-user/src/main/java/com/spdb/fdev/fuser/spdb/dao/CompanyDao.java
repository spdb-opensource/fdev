package com.spdb.fdev.fuser.spdb.dao;

import java.util.List;

import com.spdb.fdev.fuser.spdb.entity.user.Company;

public interface CompanyDao {
	/**
	 * @Desc  根据条件查询公司
	 * @param company
	 * @return
	 * @throws Exception
	 * 2019年3月27日
	 */
	List<Company> getCompany(Company company) throws Exception;
	
	/**更新一个公司*/
	Company updateCompany(Company company)throws Exception;

	/**新增一个公司*/
	Company addCompany(Company company)throws Exception;
	
	/**通过id删除*/
	Company delCompanyById(Company company)throws Exception;

	/**
	 * 根据公司名称查询公司信息
	 * @param groupName
	 * @return
	 */
	Company queryByName(String groupName);
}
