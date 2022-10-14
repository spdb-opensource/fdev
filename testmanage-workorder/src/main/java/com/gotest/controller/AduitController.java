package com.gotest.controller;

import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.domain.WorkOrderUser;
import com.gotest.service.AduitService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import com.test.testmanagecommon.vaildate.RequestValidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 审核工单 控制器层
 */
@SuppressWarnings("all")
@RestController
@RequestMapping(value="/aduit")
public class AduitController {

    private Logger logger = LoggerFactory.getLogger(InformController.class);

    @Autowired
    private AduitService aduitService;



    /**
     * 根据用户查询待审核工单
     */
    @RequestMapping("/queryAduitOrder")
    public JsonResult queryAduitOrder(@RequestBody Map map){
        String userEnName = (String) map.getOrDefault(Dict.USER_EN_NAME, Constants.FAIL_GET);
        if (!Util.isNullOrEmpty(userEnName)){
            try {
                List<WorkOrderUser> result = aduitService.queryAduitOrder(map);
                return JsonResultUtil.buildSuccess(result);
            }catch (Exception e){
                logger.error(e.getMessage());
                throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"审核工单查询异常！"});
            }
        }
        return JsonResultUtil.buildError(Constants.I_FAILED, "审核工单查询失败，请检查是否登录");
    }

    /**
     * 根据用户查询待审核工单 总数--分页用
     */
    @RequestMapping("/queryAduitOrderCount")
    public JsonResult queryAduitOrderCount(@RequestBody Map map){
        String userEnName = (String) map.getOrDefault(Dict.USER_EN_NAME, Constants.FAIL_GET);
        if (!Util.isNullOrEmpty(userEnName)){
            try {
                Integer count = aduitService.queryAduitOrderCount(map);
                map.put(Dict.TOTAL, count);
                return JsonResultUtil.buildSuccess(map);
            }catch (Exception e){
                logger.error(e.getMessage());
                throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"审核工单查询异常！"});
            }
        }
        return JsonResultUtil.buildError(Constants.I_FAILED, "审核工单查询失败，请检查是否登录");
    }


    /**
     * 通过审核
     */
    @RequestValidate(NotEmptyFields = {Dict.WORKORDERNO})
    @RequestMapping("/passAduit")
    public JsonResult passAduit(@RequestBody Map map) throws Exception{
        aduitService.passAduit(map);
        return JsonResultUtil.buildSuccess(null);
    }



    /**
     * 拒绝审核
     */
    @RequestMapping("/refuseAduit")
    public JsonResult refuseAduit(@RequestBody Map map){
        String workOrderNo = (String) map.getOrDefault(Dict.WORKORDERNO, Constants.FAIL_GET);
        if(!Util.isNullOrEmpty(workOrderNo)){
            Integer count = null;
            try {
                count = aduitService.refuseAduit(map);
            }catch (Exception e){
                logger.error(e.getMessage());
                return JsonResultUtil.buildError(ErrorConstants.SERVER_ERROR,"审核拒绝失败！");
            }
            if (null == count || count == 0){
                return JsonResultUtil.buildError(Constants.I_FAILED, "审核拒绝失败");
            }else if(count != 1){
                return JsonResultUtil.buildError(Constants.I_FAILED, "工单修改状态异常");
            } else {
                return JsonResultUtil.buildSuccess(count);
            }
        }
        return JsonResultUtil.buildError(Constants.I_FAILED, "工单号不能为空");
    }

}
