package com.spdb.fdev.fuser.spdb.dao.Impl;

import java.util.List;

import javax.annotation.Resource;

import com.spdb.fdev.fuser.base.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.spdb.dao.CompanyDao;
import com.spdb.fdev.fuser.spdb.entity.user.Company;

@Repository
public class CompanyDaoImpl implements CompanyDao {


	@Resource
	private MongoTemplate mongoTemplate;
	
	@Override
	public List<Company> getCompany(Company company) throws Exception {
		ObjectMapper mapper=new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		String json = company==null?"{}":mapper.writeValueAsString(company);
		Query query =new BasicQuery(json);
		return mongoTemplate.find(query,Company.class);
	}

	@Override
	public Company updateCompany(Company company) throws Exception {
		Query query =Query.query(Criteria.where(Dict.ID).is(company.getId()));
		Update update = Update.update(Dict.NAME, company.getName());
		Company findAndModify = mongoTemplate.findAndModify(query, update, Company.class);
		return mongoTemplate.findOne(query, Company.class);
	}

	@Override
	public Company addCompany(Company company) throws Exception {
		company.initId();
		company.setStatus("1");
		company.setCount(0);
		return mongoTemplate.save(company);
	}

	@Override
	public Company delCompanyById(Company company) throws Exception {
		Query query =Query.query(Criteria.where(Dict.ID).is(company.getId()));
		Update update = Update.update(Dict.STATUS, "0").set("deleteTime", CommonUtils.formatDate(CommonUtils.STANDARDDATEPATTERN));
		return mongoTemplate.findAndModify(query, update, Company.class);
	}

    @Override
    public Company queryByName(String groupName) {
		Query query =Query.query(Criteria.where(Dict.NAME).is(groupName));
        return mongoTemplate.findOne(query, Company.class);
    }


}
