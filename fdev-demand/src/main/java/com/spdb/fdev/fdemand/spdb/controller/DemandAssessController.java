package com.spdb.fdev.fdemand.spdb.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdemand.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.spdb.dto.DemandAssessDto;
import com.spdb.fdev.fdemand.spdb.entity.DemandAssess;
import com.spdb.fdev.fdemand.spdb.service.IDemandAssessService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author zhanghp4
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/api/demandAssess")
public class DemandAssessController {

    private static Logger logger = LoggerFactory.getLogger(DemandAssessController.class);

    @Autowired
    private IDemandAssessService demandAssessService;

    @PostMapping("/queryById")
    @NoNull(require = {Dict.ID}, parameter = DemandAssess.class)
    public JsonResult queryById(@RequestBody DemandAssess demandAssess) throws Exception {
        return JsonResultUtil.buildSuccess(demandAssessService.queryById(demandAssess.getId()));
    }

    @PostMapping("/save")
    @NoNull(require = {Dict.DEMAND_LEADER_GROUP, Dict.DEMAND_LEADER, Dict.START_ASSESS_DATE, Dict.OA_CONTACT_NO,
            Dict.OA_CONTACT_NAME, Dict.PRIORITY}, parameter = DemandAssess.class)
    public JsonResult save(@RequestBody DemandAssess demandAssess) throws Exception {
        demandAssessService.save(demandAssess);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/update")
    @NoNull(require = {Dict.ID, Dict.DEMAND_LEADER_GROUP, Dict.DEMAND_LEADER, Dict.PRIORITY}, parameter = DemandAssess.class)
    public JsonResult update(@RequestBody DemandAssess demandAssess) throws Exception {
        return JsonResultUtil.buildSuccess(demandAssessService.update(demandAssess));
    }

    @PostMapping("/query")
    public JsonResult query(@RequestBody DemandAssessDto dto) throws Exception {
        return JsonResultUtil.buildSuccess(demandAssessService.query(dto));
    }

    @PostMapping("/delete")
    @NoNull(require = {Dict.ID}, parameter = DemandAssess.class)
    public JsonResult delete(@RequestBody DemandAssess demandAssess) throws Exception {
        demandAssessService.delete(demandAssess.getId());
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 批量更新需求的评估天数，对已撤销和已分析完成的需求不做任何计算
     * 对分析中的需求的计算公式：昨天-起始评估日期
     *
     * @param
     * @return
     * @throws Exception
     */
    @PostMapping("/calcDemandAssessDays")
    public JsonResult calcDemandAssessDays() throws Exception {
        demandAssessService.calcDemandAssessDays();
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/export")
    public void export(@RequestBody DemandAssessDto dto, HttpServletResponse resp) throws Exception {
        XSSFWorkbook workbook = demandAssessService.export(dto);
        try {
            resp.reset();
            resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("需求评估表", "UTF-8") + ".xlsx");
            workbook.write(resp.getOutputStream());
        } catch (IOException e) {
            logger.error("导出需求评估表时，数据读写错误" + e.getMessage());
        }
    }

    @PostMapping("/syncConfState")
    public JsonResult syncConfState() throws Exception {
        demandAssessService.syncConfState();
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 通过传入需求评估id，手动确认需求评估完成
     */
    @PostMapping("/confirmFinish")
    @NoNull(require = {Dict.ID, "end_assess_date"}, parameter = DemandAssess.class)
    public JsonResult confirmFinish(@RequestBody DemandAssess demandAssess) throws Exception {
        return JsonResultUtil.buildSuccess(demandAssessService.confirmFinish(demandAssess.getId(), demandAssess.getEnd_assess_date()));
    }


    /**
     * 依据传递的需求评估id 及 暂缓状态 确定是否暂缓
     *
     * @param
     * @return
     * @throws Exception
     */
    @PostMapping("/deferOperate")
    public JsonResult deferOperate(
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "id") String id,
            @RequestParam(value = "demandStatus") Integer demandStatus,
            @RequestParam(value = "user_group_cn") String userGroupCn,
            @RequestParam(value = "fileType", required = false) String fileType) throws Exception {
        demandAssessService.deferOperate(files, fileType, id, demandStatus, userGroupCn);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * @param
     * @return
     * @throws Exception
     */
    @PostMapping("/cancelDefer")
    @NoNull(require = {Dict.ID}, parameter = DemandAssess.class)
    public JsonResult cancelDefer(@RequestBody DemandAssess demandAssess) throws Exception {
        demandAssessService.deferOperate(null, null, demandAssess.getId(), 1, null);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 修改定稿日期
     *
     * @param
     * @return
     * @throws Exception
     */
    @PostMapping("/updateFinalDate")
    @NoNull(require = {Dict.ID, "final_date"}, parameter = DemandAssess.class)
    public JsonResult updateFinalDate(@RequestBody DemandAssess demandAssess) throws Exception {
        demandAssessService.updateFinalDate(demandAssess);
        return JsonResultUtil.buildSuccess();
    }
}
