package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.entity.ProdRecord;
import com.spdb.fdev.release.entity.RelDevopsRecord;
import com.spdb.fdev.release.service.IProdRecordService;
import com.spdb.fdev.release.service.IRelDevopsRecordService;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devopsrecord")
public class RelDevopsRecordController {

	@Autowired
	private IRelDevopsRecordService relDevopsRecordService;
	@Autowired
	private IProdRecordService prodRecordService;

	@RequestValidate(NotEmptyFields= {Dict.APPLICATION_ID,Dict.RELEASE_NODE_NAME})
	@PostMapping(value = "/query")
	public JsonResult queryDevopsRecord(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
		String release_node_name = (String) (requestParam.get(Dict.RELEASE_NODE_NAME));
		RelDevopsRecord relDevopsRecord =new RelDevopsRecord();
		relDevopsRecord.setApplication_id(application_id);
		relDevopsRecord.setRelease_node_name(release_node_name);
		List<RelDevopsRecord> list = relDevopsRecordService.query(relDevopsRecord);	
		return JsonResultUtil.buildSuccess(list);
	}
	
	@RequestValidate(NotEmptyFields= {Dict.APPLICATION_ID, Dict.RELEASE_NODE_NAME})
	@PostMapping(value = "/queryImageTags")
	public JsonResult queryImageTags(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
		String release_node_name = (String) (requestParam.get(Dict.RELEASE_NODE_NAME));
		RelDevopsRecord relDevopsRecord = new RelDevopsRecord();
		relDevopsRecord.setApplication_id(application_id);
		relDevopsRecord.setRelease_node_name(release_node_name);
		Map map = new HashMap();
		map.put(Dict.IMAGES, relDevopsRecordService.queryImageTags(relDevopsRecord).get(Dict.CAAS));
		map.put(Dict.SCC_IMAGES, relDevopsRecordService.queryImageTags(relDevopsRecord).get(Dict.SCC));
		return JsonResultUtil.buildSuccess(map);
	}

    @RequestValidate(NotEmptyFields = {Dict.RELEASE_NODE_NAME, Dict.APPLICATION_ID, Dict.PRO_IMAGE_URI, Dict.PROD_ID})
    @PostMapping(value = "/query_docker_directory")
    public JsonResult query_docker_directory(@RequestBody @ApiParam Map<String, String> requestParam) throws Exception {
        String application_id = requestParam.get(Dict.APPLICATION_ID);
        String release_node_name = requestParam.get(Dict.RELEASE_NODE_NAME);
        String pro_image_uri = requestParam.get(Dict.PRO_IMAGE_URI);
        String prod_id = requestParam.get(Dict.PROD_ID);
		ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        RelDevopsRecord relDevopsRecord = new RelDevopsRecord();
        relDevopsRecord.setApplication_id(application_id);
        relDevopsRecord.setRelease_node_name(release_node_name);
        boolean flag = relDevopsRecordService.queryDockerDir(relDevopsRecord, prodRecord, pro_image_uri);
        return JsonResultUtil.buildSuccess(flag);
    }

	@RequestValidate(NotEmptyFields= {Dict.APPLICATION_ID, Dict.RELEASE_NODE_NAME})
	@PostMapping(value = "/queryPackageTags")
	public JsonResult queryPackageTags(@RequestBody @ApiParam Map<String, Object> requestParam) throws Exception {
		String application_id = (String) requestParam.get(Dict.APPLICATION_ID);
		String release_node_name = (String) (requestParam.get(Dict.RELEASE_NODE_NAME));
		RelDevopsRecord relDevopsRecord = new RelDevopsRecord();
		relDevopsRecord.setApplication_id(application_id);
		relDevopsRecord.setRelease_node_name(release_node_name);
		List<String> list = relDevopsRecordService.queryPackageTags(relDevopsRecord);
		Map map = new HashMap();
		map.put(Dict.PACKAGES, list);
		return JsonResultUtil.buildSuccess(map);
	}
}
