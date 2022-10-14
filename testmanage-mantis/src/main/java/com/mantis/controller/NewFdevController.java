package com.mantis.controller;

import com.mantis.dict.Dict;
import com.mantis.service.NewFdevService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.vaildate.RequestValidate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fdev")
public class NewFdevController {
    @Autowired
    private NewFdevService newFdevService;

    /**
     * 重构fdev缺陷交互
     *
     * @param map
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.ID})
    @PostMapping(value = "/updateFdevMantis")
    public JsonResult updateFdevMantis(@RequestBody Map map) throws Exception {
        String result = newFdevService.updateFdevMantis(map);
        return JsonResultUtil.buildSuccess(result);
    }

    /**
     * 实施单元删除关联工单缺陷删除
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value ="/updateMantisStatus")
    public JsonResult updateMantisStatus(@RequestBody Map<String ,Object>  map) throws Exception {
        List<String> workNos =(List<String>) map.get("workNos");
         newFdevService.updateMantisStatus(workNos);
        return JsonResultUtil.buildSuccess();
    }


    /**
     * 任务删除关联缺陷删除
     *
     * @param map
     * @return
     * @throws Exception
     */
    @PostMapping(value ="/updateMantisByTaskIds")
    public JsonResult updateMantisByTaskIds(@RequestBody Map<String ,Object>  map) throws Exception {
        List<String> taskIds =(List<String>) map.get("taskIds");
        newFdevService.updateMantisByTaskIds(taskIds);
        return JsonResultUtil.buildSuccess();
    }
}
