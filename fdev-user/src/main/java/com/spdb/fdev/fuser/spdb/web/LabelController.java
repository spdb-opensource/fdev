package com.spdb.fdev.fuser.spdb.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
import com.spdb.fdev.fuser.base.dict.ErrorConstants;
import com.spdb.fdev.fuser.base.utils.CommonUtils;
import com.spdb.fdev.fuser.spdb.entity.user.Label;
import com.spdb.fdev.fuser.spdb.entity.user.User;
import com.spdb.fdev.fuser.spdb.service.LabelService;
import com.spdb.fdev.fuser.spdb.service.UserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Api(tags = "标签接口")
@RequestMapping("/api/label")
@RestController
public class LabelController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

	@Resource
	private LabelService labelService;
	
	@Resource
    private UserService userService;

	@Autowired
	private UserVerifyUtil userVerifyUtil;
	
	

	/* 新增标签 */
	@ApiOperation(value = "新增标签")
	@ApiImplicitParams(@ApiImplicitParam(name = "name", value = "标签名字"))
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult addlabel(@RequestBody @ApiParam(name = "新增标签参数", value = "示例： {\"name\": \"标签\" }") Label label)
			throws Exception {
		String addlabelName = label.getName();
		logger.info("###### LabelName : " + addlabelName);
		if (StringUtils.isEmpty(addlabelName)) {
			throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"标签名为空"});
		}
		Label lab = labelService.addLabel(new Label(null, addlabelName, 0, "1"));
		return JsonResultUtil.buildSuccess(lab);
	}

	/* 查询标签 */
	@ApiOperation(value = "查询标签")
	@ApiImplicitParams(@ApiImplicitParam(name = "name", value = "标签名字"))
	@RequestMapping(value = "/query", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult getLabel(
			@RequestBody @ApiParam(name = "查询标签参数", value = "示例：可任意传一个参数 {\"id\" : \"5c47cb4436ba06b8416e5c42\", \"name\" : \"标签2\"}"
					+ "  \r\n 传{}查询全量列表") Label label,
			HttpServletRequest request) throws Exception {		
		List<Label> labs = labelService.queryLabel(label);
		return JsonResultUtil.buildSuccess(labs);
	}

	/* 更新标签 */
	@ApiOperation(value = "更新标签")
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult putLabel(
			@RequestBody @ApiParam(name = "更新标签参数", value = "示例： {\"id\" : \"5c47cb4436ba06b8416e5c42\", \"name\" : \"新标签\"}") Label label)
			throws Exception {
		String labelId = label.getId();
		List<Label> oldLabList = labelService.queryLabel(new Label(labelId, null, null, null));
		if (CommonUtils.isNullOrEmpty(oldLabList)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"标签不存在"});
		}
		Label oldLab = oldLabList.get(0);
		String oldLabelName = oldLab.getName();
		String newLabelName = label.getName();
		if (StringUtils.isEmpty(newLabelName)) {
			throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"标签名为空"});
		}
		if (newLabelName.equals(oldLabelName)) {
			throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"标签名已存在"});
		}
		labelService.updateLabel(label);
		List<Label> labs = labelService.queryLabel(label);
		return JsonResultUtil.buildSuccess(labs);
	}

	/* 删除标签 */
	@ApiOperation(value = "删除标签")
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult delLabelByID(
			@RequestBody @ApiParam(name = "删除标签参数", value = "示例：[{\"id\" : \"5c47cb4436ba06b8416e5c42\"}]") List<Label> labelList
			) throws Exception {
		List<String> list = new ArrayList<String>();
		for (Label label : labelList) {
			String id = label.getId();
			if (StringUtils.isEmpty(id)) {
				throw new FdevException(ErrorConstants.PARAM_ERROR,new String[]{"标签id为空"});
			}
			List<Label> labList = labelService.queryLabel(new Label(id, null, null, null));
			if (CommonUtils.isNullOrEmpty(labList)) {
				throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"标签不存在"});
			}
			list.add(id);
			User user = new User();
			user.setLabels(list);
			List<Map> maps = userService.queryUserCoreData(user);
			//如果该标签还有用户使用,提示不能删除
			if (!maps.isEmpty()) {
				throw new FdevException(ErrorConstants.USR_LABELUSE_ERROR);
			}
			labelService.delLabelByID(id);
		}
		Label label = new Label();
		return JsonResultUtil.buildSuccess(labelService.queryLabel(label));
	}
}
