package com.spdb.fdev.fuser.spdb.service.Impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.dao.CompanyDao;
import com.spdb.fdev.fuser.spdb.dao.UserDao;
import com.spdb.fdev.fuser.spdb.entity.user.Company;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import com.spdb.fdev.fuser.spdb.service.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class CompanyServiceImpl implements CompanyService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

    @Resource
    private CompanyDao companyDao;

    @Resource
    private UserDao userDao;

    /**
     * @param company
     * @return
     * @throws Exception 2019年3月27日
     * @Desc 新增公司
     */
    @Override
    public Company addCompany(Company company) throws Exception {
        List<Company> coms = companyDao.getCompany(company);
        if (coms.size() != 0) {
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{"公司名重复"});
        }
        company.setCreateTime(CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
        return companyDao.addCompany(company);
    }

    /**
     * @param company
     * @return
     * @throws Exception 2019年3月27日
     * @Desc 删除符合条件的公司
     */
    @Override
    public Company delCompany(Company company) throws Exception {
        return companyDao.delCompanyById(company);
    }

    @Override
    public Company queryByName(String groupName) {
        return companyDao.queryByName(groupName);
    }

    /**
     * @param company
     * @return
     * @throws Exception 2019年3月27日
     * @Desc 查询符合条件的公司
     */
    @Override
    public List<Company> getCompany(Company company) throws Exception {
        List<Company> companyNames = companyDao.getCompany(company);
        for (Company com : companyNames) {
            User u = new User();
            u.setCompany_id(com.getId());
            u.setStatus("0");
            long userCount = userDao.getUserCount(u);
            com.setCount((int) userCount);
        }
        return companyNames;
    }

    /**
     * @param company
     * @return
     * @throws Exception 2019年3月27日
     * @Desc 更新公司
     */
    @Override
    public Company updateCompany(Company company) throws Exception {
        Company com = new Company();
        com.setName(company.getName());
        List<Company> coms = companyDao.getCompany(com);
        if (coms.size() != 0) {
            throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{"公司名重复"});
        }
        Company newCompany = companyDao.updateCompany(company);
        return newCompany;
    }

}
