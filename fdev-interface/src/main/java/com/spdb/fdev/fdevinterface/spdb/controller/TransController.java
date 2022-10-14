package com.spdb.fdev.fdevinterface.spdb.controller;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.util.UserVerifyUtil;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.spdb.entity.Trans;
import com.spdb.fdev.fdevinterface.spdb.service.TransRelationService;
import com.spdb.fdev.fdevinterface.spdb.service.TransService;
import com.spdb.fdev.fdevinterface.spdb.vo.TransParamShow;

/**
 * @author xxx
 * @description 用于处理交易管理板块所有功能点
 * @creatDate 2019-08-05
 */
@RequestMapping("/api/interface")
@RestController
@RefreshScope
public class TransController {

    @Value("${max.batch.num}")
    private Integer maxNum;

    @Value("${runmaintenance.service.id}")
    private String runmaintenanceServiceId;

    @Resource
    private TransService transService;
    @Resource
    private TransRelationService transRelationService;
    @Autowired
    UserVerifyUtil userVerifyUtil;

    /**
     * @param paramShow
     * @return
     * @description 根据传入的条件, 查询交易列表
     * @author xxx
     * @creatDate 2019-08-05
     */
    @PostMapping("/queryTransList")
    public JsonResult queryTransList(@RequestBody TransParamShow paramShow) {
        if (StringUtils.isEmpty(paramShow.getBranchDefault())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        List<String> transIdList = paramShow.getTransId();
        if (CollectionUtils.isNotEmpty(transIdList) && transIdList.size() > maxNum) {
            throw new FdevException(ErrorConstants.TRANS_ID_LIST_OVER);
        }
        return JsonResultUtil.buildSuccess(transService.showTrans(paramShow));
    }
    
    
    /**
     * @param paramShow
     * @return
     * @description 根据传入的条件, 导出交易列表
     * @author t-lik5
     * @creatDate 2022-02-16
     */
    @PostMapping("/exportTransList")
    public void exportTransList(@RequestBody TransParamShow paramShow, HttpServletResponse resp) throws Exception {
        if (StringUtils.isEmpty(paramShow.getBranchDefault())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        Map map = transService.queryTrans(paramShow);
        transService.exprotTrans(map,resp);
    }
    

    @PostMapping("/queryAllTrans")
    public JsonResult queryAllTrans(@RequestBody Map<String, String> requestParam) {
        String id = requestParam.get(Dict.ID);
        if(StringUtils.isEmpty(id) || !id.equals(runmaintenanceServiceId)) {
            throw new FdevException(ErrorConstants.USR_AUTH_FAIL, new String[]{"id校验错误"});
        }
        return JsonResultUtil.buildSuccess(transService.showAllTrans());
    }

    /**
     * @param params
     * @return
     * @description 通过id查询交易详情
     * @author xxx
     * @creatDate 2019-08-05
     */
    @PostMapping("/queryTransDetailById")
    public JsonResult queryTransDetailById(@RequestBody Map params) {
        String id = (String) params.get(Dict.ID);
        if (StringUtils.isEmpty(id)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        return JsonResultUtil.buildSuccess(transService.getTransDetailById(id));
    }

    /**
     * @param params
     * @return
     * @description 更新交易标签, 需要做权限校验
     * @author xxx
     * @creatDate 2019-08-05
     */
    @PostMapping("/updateTransTags")
    public JsonResult updateTransTags(@RequestBody Map params) throws Exception {
        String id = (String) params.get(Dict.ID);
        List<String> tags = (List<String>) params.get(Dict.TAGS);
        if (StringUtils.isEmpty(id)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        transService.updateTransTags(id, tags);
        return JsonResultUtil.buildSuccess();
    }

    @PostMapping("/queryTransRelation")
    public JsonResult queryTransRelation(@RequestBody TransParamShow paramShow) {
        if (StringUtils.isEmpty(paramShow.getBranchDefault())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        List<String> transIdList = paramShow.getTransId();
        if (CollectionUtils.isNotEmpty(transIdList) && transIdList.size() > maxNum) {
            throw new FdevException(ErrorConstants.TRANS_ID_LIST_OVER);
        }
        return JsonResultUtil.buildSuccess(transRelationService.showTransRelation(paramShow));
    }

    @PostMapping("/modifyParamDescription")
    public JsonResult modifyParamDescription(@RequestBody Trans trans) throws Exception {
        if (StringUtils.isEmpty(trans.getTransId())||StringUtils.isEmpty(trans.getServiceId())
        ||StringUtils.isEmpty(trans.getChannel())) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY);
        }
        transService.updateParamDescription(trans);
        return JsonResultUtil.buildSuccess();
    }

}
