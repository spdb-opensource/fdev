package com.spdb.fdev.release.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.service.IProdNoteService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Api(tags = "发布说明管理接口")
@RequestMapping("/api/note")
@RestController
public class ProdNoteController {

	@Autowired
	private IProdNoteService prodNoteService;
	
	@OperateRecord(operateDiscribe="投产模块-新建发布说明")
	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME, Dict.DATE, Dict.VERSION, Dict.TYPE,
			Dict.OWNER_GROUPID, Dict.PLAN_TIME, Dict.IMAGE_DELIVER_TYPE })
	@PostMapping(value = "/createNote")
	public JsonResult createNote(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.createNote(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-查询发布说明列表")
	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME })
	@PostMapping(value = "/queryReleaseNote")
	public JsonResult queryReleaseNote(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String release_node_name = (String) requestParam.get(Dict.RELEASE_NODE_NAME);
		return JsonResultUtil.buildSuccess(prodNoteService.queryReleaseNote(release_node_name));
	}

	@OperateRecord(operateDiscribe="投产模块-查询发布说明详情")
	@RequestValidate(NotEmptyFields = { Dict.NOTE_ID })
	@PostMapping(value = "/queryNoteDetail")
	public JsonResult queryNoteDetail(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		return JsonResultUtil.buildSuccess(prodNoteService.queryNoteDetail(requestParam));
	}
	
	@OperateRecord(operateDiscribe="投产模块-删除发布说明")
	@RequestValidate(NotEmptyFields = { Dict.NOTE_ID })
	@PostMapping(value = "/deleteNote")
	public JsonResult deleteNote(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.deleteNote(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-添加发布说明应用")
	@PostMapping(value = "/addNoteService")
	public JsonResult addNoteService(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.addNoteSrevice(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-查询发布说明应用")
	@PostMapping(value = "/queryNoteService")
	public JsonResult queryNoteService(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		return JsonResultUtil.buildSuccess(prodNoteService.queryNoteSrevice(requestParam));
	}
	
	@OperateRecord(operateDiscribe="投产模块-删除发布说明应用")
	@PostMapping(value = "/deleteNoteService")
	public JsonResult deleteNoteService(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.deleteNoteSrevice(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-修改发布说明应用")
	@PostMapping(value = "/updateNoteService")
	public JsonResult updateNoteService(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.updateNoteSrevice(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-添加发布说明配置文件")
	@PostMapping(value = "/addNoteConfiguration")
	public JsonResult addNoteConfiguration(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.addNoteConfiguration(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-查询发布说明配置文件")
	@PostMapping(value = "/queryNoteConfiguration")
	public JsonResult queryNoteConfiguration(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		return JsonResultUtil.buildSuccess(prodNoteService.queryNoteConfiguration(requestParam));
	}
	
	@OperateRecord(operateDiscribe="投产模块-删除发布说明配置文件")
	@PostMapping(value = "/deleteNoteConfiguration")
	public JsonResult deleteNoteConfiguration(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.deleteNoteConfiguration(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-修改发布说明配置文件")
	@PostMapping(value = "/updateNoteConfiguration")
	public JsonResult updateNoteConfiguration(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.updateNoteConfiguration(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-添加发布说明数据库文件")
	@PostMapping(value = "/addNoteSql")
	public JsonResult addNoteSql(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.addNoteSql(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-查询发布说明数据库文件")
	@PostMapping(value = "/queryNoteSql")
	public JsonResult queryNoteSql(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		return JsonResultUtil.buildSuccess(prodNoteService.queryNoteSql(requestParam));
	}
	
	@OperateRecord(operateDiscribe="投产模块-删除发布说明数据库文件")
	@PostMapping(value = "/deleteNoteSql")
	public JsonResult deleteNoteSql(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.deleteNoteSql(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-修改发布说明数据库文件")
	@PostMapping(value = "/updateNoteSql")
	public JsonResult updateNoteSql(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.updateNoteSql(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-锁定发布说明")
	@PostMapping(value = "/lockNote")
	public JsonResult lockNote(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.lockNote(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe = "投产模块-更新发布说明数据库文件序号")
    @PostMapping(value = "/updateNoteSeqNo")
    public JsonResult updateNoteSeqNo(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
        return JsonResultUtil.buildSuccess(prodNoteService.updateNoteSeqNo(requestParam));
    }
	
	@OperateRecord(operateDiscribe="投产模块-生成发布说明文件")
	@PostMapping(value = "/generateReleaseNotes")
	public JsonResult autoReleaseNotes(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.generateReleaseNotes(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-新建手动发布说明")
	@RequestValidate(NotEmptyFields = { Dict.RELEASE_NODE_NAME, Dict.DATE, Dict.TYPE,
			Dict.OWNER_GROUPID, Dict.IMAGE_DELIVER_TYPE })
	@PostMapping(value = "/createManualNote")
	public JsonResult createManualNote(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.createManualNote(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-新建手动发布说明信息")
	@PostMapping(value = "/addManualNoteInfo")
	public JsonResult addManualNoteInfo(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.addManualNoteInfo(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-修改手动发布说明信息")
	@PostMapping(value = "/updateManualNoteInfo")
	public JsonResult updateManualNoteInfo(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		prodNoteService.updateManualNoteInfo(requestParam);
		return JsonResultUtil.buildSuccess(null);
	}
	
	@OperateRecord(operateDiscribe="投产模块-查询手动发布说明信息")
	@PostMapping(value = "/queryManualNoteInfo")
	public JsonResult queryManualNoteInfo(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		return JsonResultUtil.buildSuccess(prodNoteService.queryManualNoteInfo(requestParam));
	}
}
