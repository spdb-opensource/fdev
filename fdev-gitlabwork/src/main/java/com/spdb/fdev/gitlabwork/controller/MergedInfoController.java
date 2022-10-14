package com.spdb.fdev.gitlabwork.controller;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.gitlabwork.service.CastService;
import com.spdb.fdev.gitlabwork.service.MergedService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class MergedInfoController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MergedService mergedService;
    @Autowired
    private CastService castService;

    /**
     * 分页按条件查询merged信息
     *
     * @param request
     * @return
     */
    @PostMapping("/getMergedInfo")
    public JsonResult getMergedInfo(@RequestBody Map<String, Object> request) {
        return JsonResultUtil.buildSuccess(this.mergedService.getMergedInfo(request));
    }

    @PostMapping("/exportMergedInfo")
    public void getMergedInfo(@RequestBody Map<String, Object> request, HttpServletResponse resp) {
        XSSFWorkbook workbook = this.mergedService.exportMergedInfo(request);
        try {
            resp.reset();
            resp.setContentType("application/octet-stream");
            resp.setCharacterEncoding("UTF-8");
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            resp.setHeader("Content-Disposition", "attachment;filename=" +
                    "mergeRequest" + ".xlsx");
            workbook.write(resp.getOutputStream());
        } catch (IOException e) {
            logger.error("导出merge request时，数据读写错误" + e.getMessage());
        }
    }

    @PostMapping("/querySitMerged")
    public JsonResult querySitMerged(@RequestBody Map request){
        return  JsonResultUtil.buildSuccess(castService.query(request));
    }
}
