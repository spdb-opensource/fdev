package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.DeployTypeEnum;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.annoation.OperateRecord;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.ProdAppScale;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.service.IProdAppScaleService;
import com.spdb.fdev.release.service.IProdApplicationService;
import com.spdb.fdev.release.service.IProdRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


@Api(tags = "变更-应用扩展表")
@RequestMapping("/api/appscale")
@RestController
public class ProdAppScaleController {

    @Autowired
    private IProdAppScaleService prodAppScaleService;

    @Autowired
    private IProdApplicationService prodApplicationService;

    @Autowired
    private IProdRecordService prodRecordService;

    /**
     * 新增应用扩展
     *
     * @param map
     * @return
     * @throws Exception
     */
    @OperateRecord(operateDiscribe="投产模块-新增弹性扩展")
    @RequestValidate(NotEmptyFields = {Dict.PROD_ID, Dict.APPLICATION_ID, Dict.ENV_SCALES})
    @PostMapping(value = "/add")
    public JsonResult add(@RequestBody @ApiParam Map<String, Object> map) throws Exception {
        String prod_id = (String) map.get(Dict.PROD_ID);
        String application_id = (String) map.get(Dict.APPLICATION_ID);
        List<String> deploy_type = (List<String>) map.get("deploy_type");
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        String tagName = null;
        Map<String,String> lastTagMap = new HashMap<>();
        for(String type : deploy_type){
            String lastTag = prodApplicationService.findLastReleaseUri(application_id, prodRecord, type);
            if(!CommonUtils.isNullOrEmpty(lastTag)){
                lastTagMap.put(type,lastTag);
                tagName = lastTag.split(":")[1];
            }
        }
        // 未查找到上次投产的镜像表示未曾投产，即无法对该应用进行弹性扩展
        if(CommonUtils.isNullOrEmpty(lastTagMap)) {
            throw new FdevException(ErrorConstants.RELEASE_LACK_MIRROR_URI, new String[]{"不能对此应用进行弹性扩展！"});
        }
        HashSet<String> allDeployedType = prodApplicationService.findAllDeployedType(application_id, prodRecord); // 查询所有部署过的平台，未部署无法进行弹性扩展
        deploy_type.forEach(deploy -> {
        	if(!allDeployedType.contains(deploy)){
        		throw new FdevException(ErrorConstants.RELEASE_DEPLOY_TYPE_ERROR, new String[]{deploy, "不能对此应用进行弹性扩展！"});
        	}
        });
        ProdAppScale prodAppScale = new ProdAppScale(prod_id, application_id,
                (List<Map<String, Object>>) map.get(Dict.ENV_SCALES),deploy_type);
        prodAppScale.setPro_image_uri(lastTagMap.get(DeployTypeEnum.CAAS.getType()));
        prodAppScale.setPro_scc_image_uri(lastTagMap.get(DeployTypeEnum.SCC.getType()));
        prodAppScale.setTag_name(tagName);
        prodAppScale.setCreate_time(TimeUtils.formatDate("yyyy-MM-dd HH:mm:ss"));
        return JsonResultUtil.buildSuccess(this.prodAppScaleService.addAppScale(prodAppScale));
    }

    /**
     * 通过prod_id、application_id修改应用扩展
     * @param map
     * @return
     * @throws Exception
     */
    @OperateRecord(operateDiscribe="投产模块-修改")
    @RequestValidate(NotEmptyFields = {Dict.PROD_ID, Dict.APPLICATION_ID, Dict.ENV_SCALES})
    @PostMapping(value = "/update")
    public JsonResult update(@RequestBody @ApiParam Map<String, Object> map) throws Exception {
        this.prodAppScaleService.updateAppScale(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 通过prod_id、application_id删除应用扩展
     * @param map
     * @return
     * @throws Exception
     */
    @OperateRecord(operateDiscribe="投产模块-删除")
    @RequestValidate(NotEmptyFields = {Dict.PROD_ID, Dict.APPLICATION_ID})
    @PostMapping(value = "/delete")
    public JsonResult delete(@RequestBody @ApiParam Map<String, Object> map) throws Exception {
        this.prodAppScaleService.deleteAppScale(map);
        return JsonResultUtil.buildSuccess();
    }

    /**
     * 通过prod_id查询应用扩展,并将环境对应改为map
     * @param map
     * @return
     * @throws Exception
     */
    @RequestValidate(NotEmptyFields = {Dict.PROD_ID})
    @PostMapping(value = "/query")
    public JsonResult query(@RequestBody @ApiParam Map<String, Object> map) throws Exception {
        return JsonResultUtil.buildSuccess(this.prodAppScaleService.queryAppScale(map));
    }
    
}
