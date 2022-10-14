package com.spdb.fdev.fuser.spdb.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import com.spdb.fdev.fuser.spdb.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.entity.user.Company;
import com.spdb.fdev.fuser.spdb.service.CompanyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "公司接口")
@RequestMapping("/api/company")
@RestController
public class CompanyController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());// 控制台日志打印

	@Resource
	private CompanyService companyService;

	@Resource
	private UserService userService;

	@Autowired
	private UserVerifyUtil userVerifyUtil;

	/* 通过公司名来查询公司 */
	@ApiOperation(value = "公司查询")
	@RequestMapping(value = "/query", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult getCompanyByCompanyName(
			@RequestBody @ApiParam(name = "company", value = "例如:{\"name\":\"科蓝\"}") Company company) throws Exception {
		List<Company> list = null;
		list = companyService.getCompany(company);
		return JsonResultUtil.buildSuccess(list);
	}

	/* 新增公司信息 */
	@ApiOperation(value = "新增公司")
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult addCompany(
			@RequestBody @ApiParam(name = "company", value = "例如:{\"name\":\"科蓝\"}") Company company) throws Exception {
		Company result = null;
		if (CommonUtils.isNullOrEmpty(company.getName())) {
			throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"公司名为空"});
		}
		result = companyService.addCompany(company);
		return JsonResultUtil.buildSuccess(result);
	}

	/* 修改公司信息 */
	@ApiOperation(value = "修改公司")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult updateCompany(
			@RequestBody @ApiParam(name = "company", value = "例如:{ \"id\":\"5c452d6fd3e2a12d806e0dd1\",\"name\":\"科蓝2\"}") Company company)
			throws Exception {
		if (CommonUtils.isNullOrEmpty(company.getName()) || CommonUtils.isNullOrEmpty(company.getId())) {
			throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"公司名或id为空"});
		}
		List<Company> list = new ArrayList<>();
		companyService.updateCompany(company);
		list = companyService.getCompany(null);
		return JsonResultUtil.buildSuccess(list);
	}

	/* 刪除公司信息 */
	@ApiOperation(value = "刪除公司")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult deleteCompany(
			@RequestBody @ApiParam(name = "companyList", value = "例如:[{ \"id\":\"5c452d6fd3e2a12d806e0dd1\"}]") List<Company> companyList
			) throws Exception {
		List<Company> clist = new ArrayList<>();
		for (Company company : companyList) {
			company.setCount(null);
			if (CommonUtils.isNullOrEmpty(company.getId())) {
				throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"公司id为空"});
			}
			Company company1 = new Company();
			company1.setId(company.getId());
			Company com = companyService.getCompany(company1).get(0);
			User user =new User();
			user.setCompany_id(com.getId());
			user.setStatus("0");
			List<Map> listmap = userService.queryUserCoreData(user);
			if (listmap.size() == 0) {
				// 進行刪除
				companyService.delCompany(company);
			} else {
				throw new FdevException(ErrorConstants.USR_INUSE_ERROR);
			}
		}
		Company query = new Company();
		clist = companyService.getCompany(query);
		return JsonResultUtil.buildSuccess(clist);
	}
}
