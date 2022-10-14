package com.spdb.fdev.fdemand.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.fdemand.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.service.IImplementUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/implementUnit")
public class ImplementUnitController {

    @Autowired
    private IImplementUnitService implementUnitService;

    @Autowired
    private UserVerifyUtil userVerifyUtil;

    /**
     * 根据需求id可分页查询研发单元,index是页码，size是每页条数。size为0时表示不分页
     * zhanghp4
     *
     * @param param
     * @return
     */
    @PostMapping("/queryPaginationByDemandId")
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID})
    public JsonResult queryPagination(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(implementUnitService.queryPaginationByDemandId(param));
    }

    /**
     * 新增研发单元，权限控制为需求管理员、需求牵头人、涉及小组评估人
     * 判断该需求是否是特殊状态，是，则不允许新建。若是待实施之前，则可以新建
     * 判断需求是否为待实施的之前状态，若否，则不允许新建，是，则还需要根据需求id和涉及小组查询研发单元是否已经评估完成
     * 若评估完成，则不允许新建
     * 若新增实施单元的人员为需求管理员，则可以直接新增实施单元。
     * 若不是需求管理员，则需要根据需求id查询牵头人，若为需求牵头人，则可以直接新增研发单元。
     * 若既不是需求管理员，也不是需求牵头人，则判断该人员是不是板块涉及人员，若是，则可以新建研发单元。
     * 若以上都不是，则提示用户无法新建研发单元。
     * zhanghp4
     *
     * @param fdevImplementUnit
     * @return
     */
    @PostMapping("/add")
    @NoNull(require = {Dict.DEMAND_ID,Dict.DEMAND_TYPE, Dict.IMPLEMENT_UNIT_CONTENT
            , Dict.GROUP,Dict.IMPLEMENT_LEADER_ALL, Dict.PLAN_START_DATE,
            Dict.DEPT_WORKLOAD, Dict.COMPANY_WORKLOAD, Dict.UI_VERIFY}, parameter = FdevImplementUnit.class)
    public JsonResult add(@RequestBody FdevImplementUnit fdevImplementUnit) throws Exception {
        return JsonResultUtil.buildSuccess(implementUnitService.add(fdevImplementUnit));
    }

    /**
     * 更新研发单元，权限控制为需求管理员、需求牵头人、设计小组评估人、实施单元牵头人、研发单元牵头人
     * 判断研发单元是否为待实施的之前状态，
     * 若更新研发单元的人员为需求管理员，则可以修改研发单元。
     * 若不是需求管理员，则需要根据需求id查询牵头人，若为需求牵头人，则可以更新研发单元。
     * 若既不是需求管理员，也不是需求牵头人，则判断该人员是不是板块涉及人员，若是，则可以更新研发单元。
     * 若以上都不是，则提示用户无法更新研发单元。
     * zhanghp4
     *
     * @param fdevImplementUnit
     * @return
     */
    @PostMapping("/update")
    @NoNull(require = {Dict.ID, Dict.DEMAND_ID, Dict.DEMAND_TYPE, Dict.IMPLEMENT_UNIT_CONTENT
            , Dict.GROUP,Dict.IMPLEMENT_LEADER_ALL, Dict.PLAN_START_DATE,
            Dict.DEPT_WORKLOAD, Dict.COMPANY_WORKLOAD, Dict.UI_VERIFY}, parameter = FdevImplementUnit.class)
    public JsonResult update(@RequestBody FdevImplementUnit fdevImplementUnit) throws Exception {
        implementUnitService.update(fdevImplementUnit);
        return JsonResultUtil.buildSuccess();
    }

    /**
    * 补充研发单元，权限控制为需求管理员、需求牵头人、板块牵头人
    * 判断需求至少有一个涉及板块评估完成才可以补充研发单元
    * */
    @PostMapping("/supply")
    @NoNull(require = {Dict.DEMAND_ID, Dict.DEMAND_TYPE, Dict.IMPLEMENT_UNIT_CONTENT
            , Dict.GROUP,Dict.IMPLEMENT_LEADER_ALL, Dict.PLAN_START_DATE,
            Dict.DEPT_WORKLOAD, Dict.COMPANY_WORKLOAD, Dict.UI_VERIFY}, parameter = FdevImplementUnit.class)
    public JsonResult supply(@RequestBody FdevImplementUnit fdevImplementUnit) throws Exception {
        return JsonResultUtil.buildSuccess(implementUnitService.supply(fdevImplementUnit));
    }

    /**
     * 删除研发单元，权限控制为需求管理员、需求牵头人、本板块牵头人、实施单元牵头人
     * 判断需求是否为开发中的之前状态，是可以删除
     * 若为需求管理员，则允许删除。
     * 若不是需求管理员，则需要根据需求id查询牵头人，若为需求牵头人，则可删除。
     * 若既不是需求管理员，也不是需求牵头人，则判断该人员是不是板块涉及人员，若是，则可以删除。
     * 若以上都不是，则提示用户无权限删除研发单元。
     * 研发单元下有任务不可删除
     * 研发单元处于评估中，则可以直接删除
     * 研发单元下没有任务，但是挂载的实施单元状态为已投产、暂缓、暂存、撤销，则不可删除
     * 删除研发单元要删除对应的玉衡工单
     * 小组已经评估完成，删除研发单元时，需要重新推需求计划时间
     * zhanghp4
     *
     * @param param
     * @return
     */
    @PostMapping("/deleteById")
    @RequestValidate(NotEmptyFields = {Dict.IDS})
    public JsonResult deleteById(@RequestBody Map<String, Object> param) throws Exception {
        implementUnitService.deleteById(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 研发单元评估
     * 若为正常的研发单元，则权限为需求管理员、需求牵头人及本涉及小组评估人
     * 若为补充的研发单元，则权限为需求牵头人
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/assess")
    @RequestValidate(NotEmptyFields = {Dict.ID})
    public JsonResult assess(@RequestBody Map<String, Object> param) throws Exception {
        implementUnitService.assess(param);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 提供接口供任务模块使用，可以修改实施单元的状态和实际时间
     * 实施单元的状态和实际时间修改后，需同步修改需求相关信息
     * wangfq
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/updateIpmpUnitForTask")
    @RequestValidate(NotEmptyFields = {Dict.FDEV_IMPLEMENT_UNIT_NO, Dict.STAGE})
    public JsonResult updateIpmpUnitForTask(@RequestBody Map<String, Object> param) throws Exception {
        implementUnitService.updateIpmpUnitForTask(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据fdev实施单元编号、需求id查实施单元信息及需求信息
     * 输出实施单元信息和所属需求的信息
     * 根据fdev实施单元编号查实施单元和需求信息，若没有实施单元，则根据需求id查需求信息。
     *
     * @param param
     * @return
     * @author dengf5
     */
    @PostMapping("/queryByFdevNoAndDemandId")
    public JsonResult queryByFdevNoAndDemandId(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(implementUnitService.queryByFdevNoAndDemandId(param));
    }

    /**
     * 查询所有研发单元，新增任务时使用
     * 过滤撤销、归档、暂缓状态
     *
     * @param param
     * @return
     * @throws Exception wangfq
     */
    @PostMapping("/queryAvailableIpmpUnit")
    public JsonResult queryAvailableIpmpUnit(@RequestBody Map<String, Object> param) throws Exception {
        List<FdevImplementUnit> unitList = implementUnitService.queryAvailableIpmpUnit(param);
        return JsonResultUtil.buildSuccess(unitList);
    }

    /**
     * 查询所有研发单元，新增任务时使用
     * 过滤撤销、归档、暂缓状态
     *
     * @param param
     * @return
     * @throws Exception wangfq
     */
    @PostMapping("/queryAvailableIpmpUnitNew")
    public JsonResult queryAvailableIpmpUnitNew(@RequestBody Map<String, Object> param) throws Exception {
        List<FdevImplementUnit> unitList = implementUnitService.queryAvailableIpmpUnitNew(param);
        return JsonResultUtil.buildSuccess(unitList);
    }

    /**
     * 恢复中
     * 更新实施单元，权限控制为需求管理员、需求牵头人、本板块牵头人
     * 判断实施单元是否为待实施的之前状态，
     * 若更新实施单元的人员为需求管理员，则可以修改实施单元。
     * 若不是需求管理员，则需要根据需求id查询牵头人，若为需求牵头人，则可以更新实施单元。
     * 若既不是需求管理员，也不是需求牵头人，则判断该人员是不是板块涉及人员，若是，则可以更新实施单元。且需要更新需求计划日期及发送任务接口
     * 如果是最后一个实施单元，则需要更新需求的状态为修复完成3
     * 若以上都不是，则提示用户无法更新实施单元。此处用户只能修改计划时间
     * zhanghp4
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/updateByRecover")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.PLAN_START_DATE,
            Dict.PLAN_INNER_TEST_DATE, Dict.PLAN_TEST_DATE})
    public JsonResult updateByRecover(@RequestBody Map<String, Object> param) throws Exception {
        implementUnitService.updateByRecover(param);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/queryImplByGroupAndDemandId")
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID, Dict.GROUP})
    public JsonResult queryImplByGroupAndDemandId(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(implementUnitService.queryImplByGroupAndDemandId(param));
    }

    /**
     * 查询研发单元详情，包括研发单元信息和其下的任务信息
     * @param param
     * @return
     */
    @PostMapping("/queryFdevImplUnitDetail")
    @RequestValidate(NotEmptyFields = {Dict.FDEV_IMPLEMENT_UNIT_NO})
    public JsonResult queryFdevImplUnitDetail(@RequestBody Map<String, Object> param) {
        return JsonResultUtil.buildSuccess(implementUnitService.queryFdevImplUnitDetail(param));
    }

    /**
     * 挂载，需求牵头人、需求管理员、本研发单元牵头人、本研发单元所属小组评估人、实施单元牵头人可挂载
     * @param param
     * @return
     */
    @PostMapping("/mount")
    @RequestValidate(NotEmptyFields = {Dict.ID, Dict.IPMP_IMPLEMENT_UNIT_NO})
    public JsonResult mount(@RequestBody Map<String, Object> param) throws Exception {
        implementUnitService.mount(param);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 根据实施单元编号和所属小组分页查询研发单元
     * 所属小组为空时，仅根据实施单元查询
     * index是页码，size是每页条数。size为0时表示不分页
     * @param param
     * @return
     */
    @PostMapping("/queryPaginationByIpmpUnitNo")
    @RequestValidate(NotEmptyFields = { Dict.DEMAND_ID})
    public JsonResult queryPaginationByIpmpUnitNo(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(implementUnitService.queryPaginationByIpmpUnitNo(param));
    }

    /**
     * 日常需求查询 研发单元
     * 所属小组为空时，仅根据实施单元查询
     * index是页码，size是每页条数。size为0时表示不分页
     * @param param
     * @return
     */
    @PostMapping("/queryDailyFdevUnitList")
    @RequestValidate(NotEmptyFields = { Dict.DEMAND_ID})
    public JsonResult queryDailyFdevUnitList(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(implementUnitService.queryDailyFdevUnitList(param));
    }

    /**
     * 判断评估完成按钮亮不亮
     * @return
     */
    @PostMapping("/assessButton")
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID, Dict.GROUP})
    public JsonResult assessButton(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(implementUnitService.assessButton(param));
    }

    /**
     *
     * @param param
     * @return
     */
    @PostMapping("/checkWork")
    @RequestValidate(NotEmptyFields = {Dict.DEMAND_ID, Dict.IPMP_IMPLEMENT_UNIT_NO})
    public JsonResult checkWork(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(implementUnitService.checkWork(param));
    }

    /**
     *判断当前用户是否展示新增按钮 需求管理员或者需求牵头人或者设计小组评估人或者实施单元牵头人 其他需求任务负责人
     * @param param
     * @return
     */
    @PostMapping("/isShowAdd")
    @RequestValidate(NotEmptyFields = {Dict.DEMANDID})
    public JsonResult isShowAdd(@RequestBody Map<String, Object> param) throws Exception {
        return JsonResultUtil.buildSuccess(implementUnitService.isShowAdd(param));
    }

    /**
     * 批量查询研发单元信息
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("/queryFdevUnitByUnitNos")
    @RequestValidate(NotEmptyFields = {Dict.UNITNOS})
    public JsonResult queryFdevUnitByUnitNos(@RequestBody Map<String, List<String>> param) throws Exception {
        return JsonResultUtil.buildSuccess(implementUnitService.queryFdevUnitByUnitNos(param.get(Dict.UNITNOS)));
    }

    /**
     * 查看研发单元列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/queryFdevUnitList")
    public JsonResult queryFdevUnitList(@RequestBody Map<String,Object> params) throws Exception {
        return JsonResultUtil.buildSuccess(implementUnitService.queryFdevUnitList(params));
    }

    /**
     * 导出研发单元列表
     *
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/exportFdevUnitList")
    public JsonResult exportFdevUnitList(@RequestBody Map<String,Object> params, HttpServletResponse resp) throws Exception {
        implementUnitService.exportFdevUnitList(params,resp);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 研发单元即将超期 超期提醒邮件 每天早上9点
     * 邮件提醒研发单元是否提交业测延期、邮件提醒研发单元是否提交业测即将延期、邮件提醒研发单元是否开发延期、邮件提醒研发单元是否开发即将延期
     * @param params
     * @return
     * @throws Exception
     */
    @PostMapping("/fdevUnitOverdueEmail")
    public JsonResult fdevUnitOverdueEmail(@RequestBody Map<String,Object> params) throws Exception {
        implementUnitService.fdevUnitOverdueEmail(params);
        return JsonResultUtil.buildSuccess();
    }
}
