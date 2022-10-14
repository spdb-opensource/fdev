package com.gotest.controller;

import com.gotest.dict.Constants;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.service.RequireService;
import com.test.testmanagecommon.JsonResult;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.util.JsonResultUtil;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@SuppressWarnings("all")
@RestController
@RequestMapping(value="/require")
public class RequireController {

    @Autowired
    private RequireService requireService;

    private Logger logger = LoggerFactory.getLogger(RequireController.class);



    /**
     * fdev任务添加到工单下的任务列表----------------------------
     */
    @RequestMapping("/addRequireImplementNo")
    public JsonResult createFdevOrder(@RequestBody Map map){
        try {
            map.put(Dict.WORKORDERNO, requireService.createFdevOrder(map));
        }catch (Exception e){
            logger.error(e.toString());
            throw new FtmsException(ErrorConstants.DATA_INSERT_ERROR,new String[]{"添加任务到工单列表失败！"});
        }
        return JsonResultUtil.buildSuccess(map);
    }

    /**
     * fdev查询工单状态
     */
    @RequestMapping("/testPlanQuery")
    public JsonResult queryFdevOrderState(@RequestBody Map map) throws Exception {
        return JsonResultUtil.buildSuccess(requireService.queryFdevOrderState((String)map.get(Dict.TASKNO),
                (String)map.get(Dict.ORDERTYPE),(Integer)map.get(Dict.PAGE),(Integer)map.get(Dict.PAGESIZE)));
    }

    /**
     * 删除工单
     */
    @RequestMapping("/deleteOrder")
    public JsonResult deleteOrder(@RequestBody Map map){
        Integer dropTask;
        try{
            dropTask = requireService.deleteOrder(map);
            if(Util.isNullOrEmpty(dropTask) || dropTask == 0){
                return JsonResultUtil.buildSuccess("删除任务失败");
            }
        }catch (Exception e){
            logger.error(e.toString());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{"删除任务失败"});
        }
        return JsonResultUtil.buildSuccess("删除任务成功");
    }

    /**
     * 修改工单
     * */
    @RequestMapping("/updateOrder")
    public JsonResult updateWorkOrder(@RequestBody Map map){
        Integer count = null;
        try {
            count = requireService.updateWorkOrder(map);
        }catch (Exception e){
            logger.error(e.toString());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{e.getMessage()});
        }
        if ( count == 1 ){
            return JsonResultUtil.buildSuccess("修改工单成功！");
        }
        return JsonResultUtil.buildError(Constants.I_FAILED,"修改工单失败！");
    }

    /**
     * fdev任务修改实施单元编号
     * */
    @RequestMapping("/updateUnitNo")
    public JsonResult updateUnitNo(@RequestBody Map map){
        Integer count = null;
        try {
            count=requireService.updateUnitNo(map);
        }catch (Exception e){
            logger.error(e.toString());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{e.getMessage()});
        }
        if(count==1){
            return JsonResultUtil.buildSuccess("修改任务对应的实施单元成功！");
        }
        return JsonResultUtil.buildError(Constants.I_FAILED,"修改任务对应的实施单元失败！");
    }
}
