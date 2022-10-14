package com.spdb.fdev.fdevenvconfig.spdb.web;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.fdevenvconfig.base.annotation.nonull.NoNull;
import com.spdb.fdev.fdevenvconfig.base.dict.Constants;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.service.HistoryDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 实体与环境映射更新流水记录
 *
 * @author xxx
 * @date 2020/6/28 16:06
 */
@RestController
@RequestMapping("/api/v2/history")
public class HistoryDetailsController {

    @Autowired
    private HistoryDetailsService historyDetailsService;

    /**
     * 根据实体id和环境id分页查询实体与环境映射历史修改记录
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/getMappingHistoryList")
    @NoNull(require = {Constants.MODEL_ID, Constants.ENV_ID, Dict.PAGE, Dict.PER_PAGE}, parameter = LinkedHashMap.class)
    public JsonResult getMappingHistoryList(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(historyDetailsService.getMappingHistoryList(requestMap));
    }

    /**
     * 根据流水id查询映射值修改详情
     *
     * @param requestMap
     * @return
     */
    @PostMapping("/getMappingHistoryDetail")
    @NoNull(require = {Dict.ID}, parameter = LinkedHashMap.class)
    public JsonResult getMappingHistoryDetail(@RequestBody Map<String, Object> requestMap) {
        return JsonResultUtil.buildSuccess(historyDetailsService.getMappingHistoryDetail(requestMap));
    }

}
