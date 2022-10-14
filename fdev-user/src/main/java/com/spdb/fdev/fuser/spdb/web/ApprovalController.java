package com.spdb.fdev.fuser.spdb.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.util.UserVerifyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fuser.base.dict.Constants;
import com.spdb.fdev.fuser.base.dict.Dict;
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.Token.TokenManger;
import com.spdb.fdev.fuser.spdb.entity.user.NetApproval;
import com.spdb.fdev.fuser.spdb.entity.user.Role;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import com.spdb.fdev.fuser.spdb.service.ApprovalService;
import com.spdb.fdev.fuser.spdb.service.MailService;
import com.spdb.fdev.fuser.spdb.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "网络审核")
@RestController
@RequestMapping("/api/approval")
public class ApprovalController {
	private Logger logger = LoggerFactory.getLogger(ApprovalController.class);
	
    @Resource
    private UserService userService;
    
    @Resource
    private ApprovalService approvalService;
    
    @Resource
    private MailService mailService;
    
    @Autowired
    private TokenManger tokenManger;

	@Autowired
	private UserVerifyUtil userVerifyUtil;
	
	@ApiOperation(value = "查询审核列表")
    @RequestMapping(value = "queryApprovalList", method = RequestMethod.POST)
    @ResponseBody
	public JsonResult queryApprovalList(@RequestBody Map param) throws Exception{
		if (StringUtils.isEmpty(param.get("type"))) {
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"审核类型参数不能为空"});
		}
		Map map = approvalService.queryApprovalList(param);
		return JsonResultUtil.buildSuccess(map);
	}

	@OperateRecord(operateDiscribe="网络审核模块-卡点管理员也可以进行网络审核")
	@ApiOperation(value = "网络审核更新接口")
    @RequestMapping(value = "updateApprovalStatus", method = RequestMethod.POST)
    @ResponseBody
	public JsonResult updateApprovalStatus(@RequestBody Map param, HttpServletRequest request) throws Exception{
		if (StringUtils.isEmpty(param.get(Dict.STATUS)) || StringUtils.isEmpty(param.get("type")) || null == param.get(Dict.IDS) || ((List)param.get(Dict.IDS)).size() == 0) {
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"更新的ids和状态不能为空不能为空"});
		}
		String status = (String)param.get(Dict.STATUS);
		if (!Dict.PASSED.equals(status) && !Dict.REFUSED.equals(status)) {
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"审核状态字段异常！"});
		}
		//根据当前用户角色判断是否具有网络审核员资格
    	String userNameEn = tokenManger.getUserByToken(request.getHeader(Dict.AUTHORIZATION));
        User loginUser = new User();
        loginUser.setUser_name_en(userNameEn);
        loginUser.setStatus("0");
        List<Map> result = userService.queryUser(loginUser);
        List<Role> roles = (List<Role>)result.get(0).get(Dict.ROLE);
		List<String> roles_id = (List<String>)result.get(0).get(Dict.ROLE_ID);
		boolean flag = false;
        for(Role role : roles){
        	if ((Constants.NET_APPROVER.equals(role.getName()) && "1".equals(role.getStatus()))) {
				flag = true;
			}
        }
        Boolean stuckFlag = userVerifyUtil.userRoleIsSPM(roles_id);
        if(!flag && !stuckFlag) {
        	throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"当前用户非网络审核员也非卡点管理员"});
        }
        param.put(Dict.USER_NAME_CN, result.get(0).get(Dict.USER_NAME_CN));
		approvalService.updateApproval(param);
		return JsonResultUtil.buildSuccess();
	}
	
	@ApiOperation(value = "批量发送关闭kf网络通知邮件")
    @RequestMapping(value = "sendKfOffEmail", method = RequestMethod.POST)
    @ResponseBody
	public JsonResult sendKfOffEmail(@RequestBody Map param) throws Exception{
		approvalService.sendKfOffEmail();
		return JsonResultUtil.buildSuccess();
	}
	
	@ApiOperation(value = "更新kf网络开通审核标志")
    @RequestMapping(value = "updateKfApprovalFlag", method = RequestMethod.GET)
    @ResponseBody
	public String updateKfApprovalFlag(HttpServletRequest request) throws Exception{
		if (!CommonUtils.isNullOrEmpty(request.getParameter(Dict.ID))) {
			String id = request.getParameter(Dict.ID);
			logger.info("更新kf网络开通审核标志，id:{}",id);
			NetApproval netApproval = new NetApproval();
			netApproval.setId(id);
			netApproval.setType(Dict.KF_APPROVAL);
			List<Map> list = approvalService.queryApproval(netApproval);
			if (!CommonUtils.isNullOrEmpty(list)) {
				netApproval.setOff_flag(1);
				approvalService.updateApprovalStatus(netApproval);
				return "您可以继续使用spdb-kf白名单。";
			}
			return "由于您两天内未点击继续使用，该spdb-kf白名单已被关闭！";
		}
		return null;
	}
	
	@ApiOperation(value = "自增kf网络审核标志字段和提醒网络审核员审核")
    @RequestMapping(value = "addOffFlagAndNotify", method = RequestMethod.POST)
    @ResponseBody
	public JsonResult addOffFlagAndNotify(@RequestBody Map param) throws Exception{
		approvalService.addOffFlagAndNotify();
		return JsonResultUtil.buildSuccess();
	}
}
