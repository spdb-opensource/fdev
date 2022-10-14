package com.spdb.fdev.fdemand.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdemand.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ErrorConstants;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.service.ExportExcelService;
import com.spdb.fdev.fdemand.spdb.service.IDemandBaseInfoService;
import com.spdb.fdev.fdemand.spdb.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.spdb.fdev.common.annoation.LazyInitProperty;
import com.spdb.fdev.common.annoation.OperateRecord;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.spdb.fdev.transport.RestTransport;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/demand")
public class DemandBaseInfoController {

    @Autowired
    private IDemandBaseInfoService demandBaseInfoService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    @Autowired
    private ExportExcelService exportExcelService;

    /**
     * @param demandBaseInfo
     * @return
     */
    @PostMapping("/query")
    public JsonResult query(@RequestBody DemandBaseInfo demandBaseInfo) throws Exception {
        return JsonResultUtil.buildSuccess(demandBaseInfoService.query(demandBaseInfo));
    }

    @PostMapping("/queryForSelect")
    public JsonResult queryForSelect() throws Exception {
        return JsonResultUtil.buildSuccess(demandBaseInfoService.queryForSelect());
    }

    @PostMapping("/queryTechBusinessForSelect")
    public JsonResult queryTechBusinessForSelect() throws Exception {
        return JsonResultUtil.buildSuccess(demandBaseInfoService.queryTechBusinessForSelect());
    }

    /**
     * 分页查询
     *
     * @param param
     * @return
     */
    @PostMapping("/queryPagination")
    @RequestValidate(NotEmptyFields = {Dict.SIZE, Dict.INDEX})
    public JsonResult queryPagination(@RequestBody Map<String, Object> param) {
        return JsonResultUtil.buildSuccess(demandBaseInfoService.queryPagination(param));
    }

    /**
     * 需求录入
     *
     * @param demandBaseInfo
     * @return
     * @throws Exception
     */
    @PostMapping("/save")
    @NoNull(require = {Dict.DEMAND_TYPE,Dict.DEMAND_LEADER_ALL, Dict.DEMAND_LEADER_GROUP,Dict.RELATE_PART,Dict.RELATE_PART_DETAIL}, parameter = DemandBaseInfo.class)
    public JsonResult save(@RequestBody DemandBaseInfo demandBaseInfo) throws Exception {
    	demandBaseInfoService.save(demandBaseInfo);
    	return JsonResultUtil.buildSuccess(demandBaseInfo.getOa_contact_no());
    }

    /**
     * 信息修改
     *
     * @param demandBaseInfo
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/update")
    @NoNull(require = {Dict.ID, Dict.OA_CONTACT_NO, Dict.OA_CONTACT_NAME, Dict.DEMAND_TYPE, Dict.DEMAND_LEADER_GROUP,
            Dict.DEMAND_LEADER_ALL, Dict.RELATE_PART,Dict.RELATE_PART_DETAIL}, parameter = DemandBaseInfo.class)
    public JsonResult update(@RequestBody DemandBaseInfo demandBaseInfo) throws Exception {
        return JsonResultUtil.buildSuccess(demandBaseInfoService.update(demandBaseInfo));
    }
    
    /**
     * 预评估
     *
     * @param demandBaseInfo
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/updateImpl")
    @NoNull(require = {Dict.ID, Dict.OA_CONTACT_NAME, Dict.PRIORITY, Dict.RELATE_PART,Dict.RELATE_PART_DETAIL,
             Dict.EXTRA_IDEA, Dict.DEMAND_PLAN_NO, Dict.UI_VERIFY}, parameter = DemandBaseInfo.class)
    public JsonResult updateImpl(@RequestBody DemandBaseInfo demandBaseInfo) throws Exception {
        return JsonResultUtil.buildSuccess(demandBaseInfoService.updateImpl(demandBaseInfo));
    }

    /**
     * 查询详情
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryDemandInfoDetail")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryDemandInfoDetail(@RequestBody Map<String, String> param) throws Exception {
        String id = param.get(Dict.ID);
        DemandBaseInfo info = demandBaseInfoService.queryById(id);

        return JsonResultUtil.buildSuccess(info);
    }

    /**
     * 导出评估表
     *
     * @param param
     * @throws Exception
     */
    @PostMapping(value = "/exportAssessExcel")
    public void ExportAssessExcel(@RequestBody Map<String, String> param, HttpServletResponse resp) throws Exception {
        String id = param.get("demand_id");
        exportExcelService.ToExportAssessExcelByDemandId(id, resp);

    }


    /**
     * 需求暂缓，需将实施单元和任务同时暂缓
     * wangfq
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/defer")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult defer(@RequestBody Map<String, Object> param) throws Exception {
        demandBaseInfoService.defer(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 查询需求列表
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryDemandList")
    public JsonResult queryDemandList(@RequestBody Map map) throws Exception {


        Map info = demandBaseInfoService.queryDemandList(map);
        return JsonResultUtil.buildSuccess(info);
    }

    /**
     * 需求恢复，将实施单元修改为恢复中，此时实施单元可以修改计划时间
     * 当实施单元计划时间修改完成后，将实施单元和任务状态修改为恢复完成
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/recover")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult recover(@RequestBody Map<String, Object> param) throws Exception {
        demandBaseInfoService.recover(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 需求撤销，将需求进行归档，任务归档无影响
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/repeal")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult repeal(@RequestBody Map<String, Object> param) throws Exception {
        demandBaseInfoService.repeal(param);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 需求归档
     * 需求管理员和需求牵头人可以点击归档
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/placeonfile")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult placeonfile(@RequestBody Map<String, Object> param) throws Exception {
        demandBaseInfoService.placeOnFile(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据需求id查任务列表
     * 根据需求id查实施单元，再根据实施单元id查任务
     * zhanghp4
     *
     * @param param
     * @return
     */
    @PostMapping("/queryTaskByDemandId")
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID})
    public JsonResult queryTaskByDemandId(@RequestBody Map<String, Object> param) {
        return JsonResultUtil.buildSuccess(demandBaseInfoService.queryTaskByDemandId(param));
    }

    /**
     * 导出需求列表
     */
    @PostMapping(value = "/exportDemandsExcel")
    public void exportDemandExcel(@RequestBody Map map, HttpServletResponse resp) throws Exception {
        exportExcelService.exportDemandsExcel(map, resp);
    }
    /**
     * 导入需求表
     * @throws Exception 
     */
    @RequestMapping(value = "/importDemandExcel",method = RequestMethod.POST)
    public JsonResult importDemanExcel(@RequestParam(value = "file") MultipartFile file,@RequestParam(value = "demandId")String demandId) throws Exception {
    	if (file.getSize()>=(5*1024*1024)) {  
			throw new FdevException(ErrorConstants.FILE_SIZE_FILE);
		}
    	return JsonResultUtil.buildSuccess(exportExcelService.importDemandExcel(file,demandId));
    }

    /**
     * 提供接口供任务模块使用，可以修改需求的状态和实际时间
     * wangfq
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/updateDemandForTask")
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID, Dict.STAGE})
    public JsonResult updateDemandForTask(@RequestBody Map<String, Object> param) throws Exception {
        demandBaseInfoService.updateDemandForTask(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据需求编号查询需求是否存在
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/queryDemandByOaContactNo")
    public JsonResult queryDemandByOaContactNo(@RequestBody Map map) throws Exception {
        String oaContactNo = (String) map.get(Dict.OA_CONTACT_NO);
        if (CommonUtils.isNullOrEmpty(oaContactNo)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{"需求编号"});
        }
        Map resMap = new HashMap();
        Long demandCount = demandBaseInfoService.demandCount(oaContactNo);
        if (demandCount >= 1) {
            resMap.put("demandCount", demandCount);
        } else if (demandCount == null || demandCount != 0) {
            throw new FdevException(ErrorConstants.DATA_QUERY_ERROR);
        }
        return JsonResultUtil.buildSuccess(resMap);
    }
    /**
     * 导出模板表
     */
    @PostMapping(value = "/exportModelExcel")
    public void exportModelExcel(HttpServletResponse resp) throws Exception{
    	exportExcelService.exportModelExcel(resp);
    }

    /**
     * 新增和补充实施单元时，可选小组信息
     * zhanghp4
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/queryPartInfo")
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID, Dict.PART_ID})
    public JsonResult queryPartInfo(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(demandBaseInfoService.queryPartInfo(param));
    }
    /**
     * 查询科技需求类型
     * t-huyz
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/queryTechType")
    public JsonResult queryTechType(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(demandBaseInfoService.queryTechType(param));
    }

    /**
     * 通过id集合批量查询需求
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/queryDemandByIds")
    public JsonResult queryDemandByIds(@RequestBody Map<String, Object> param) throws Exception {
        List<String> ids = (List<String>) param.get(Dict.IDS);
        return JsonResultUtil.buildSuccess(demandBaseInfoService.queryDemandByIds(ids.stream().collect(Collectors.toSet())));
    }

    /**
     * 修改代码审核工单编号
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/updateDemandCodeOrderNo")
    @RequestValidate(NotEmptyFields = {Dict.CODE_ORDER_NO})
    public JsonResult updateDemandCodeOrderNo(@RequestBody Map<String, Object> param) throws Exception {
        demandBaseInfoService.updateDemandCodeOrderNo(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据需求IDS获取需求
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/getDemandsInfoByIds")
    @RequestValidate(NotEmptyFields = {Dict.DEMANDIDS})
    public JsonResult getDemandsInfoByIds(@RequestBody Map<String, Object> param) throws Exception {

        return JsonResultUtil.buildSuccess(demandBaseInfoService.getDemandsInfoByIds(param));
    }


    /**
     * 需求归档前查询各必须文档是否已有
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/queryDemandFile")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult queryDemandFile(@RequestBody Map<String, String> param) throws Exception {
        return JsonResultUtil.buildSuccess(demandBaseInfoService.queryDemandFile(param.get(Dict.ID)));
    }
}
