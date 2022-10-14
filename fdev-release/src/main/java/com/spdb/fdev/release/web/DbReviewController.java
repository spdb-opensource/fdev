package com.spdb.fdev.release.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.JsonResult;
import com.spdb.fdev.common.util.JsonResultUtil;
import com.spdb.fdev.common.validate.RequestValidate;
import com.spdb.fdev.release.dao.IProdAssetDao;
import com.spdb.fdev.release.entity.GroupAbbr;
import com.spdb.fdev.release.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

@Api(tags = "数据库审核文件管理接口")
@RequestMapping("/api/dbreview")
@RestController
public class DbReviewController {

	@Autowired
	private IDbReviewService dbReviewService;
	@Autowired
	private IProdAssetService prodAssetService;
	@Autowired
	private IFileService fileService;
	@Autowired
	private IGroupAbbrService groupAbbrService;
	@Resource
    private IProdAssetDao prodAssetDao;

	@PostMapping(value = "/dbReviewUpload")
	public JsonResult dbReviewUpload(@RequestBody Map request) {
		String task_id = (String) request.get("id");
		dbReviewService.dbReviewUpload(task_id);
		return JsonResultUtil.buildSuccess(null);
	}

	@PostMapping(value = "/queryDbPath")
	@RequestValidate(NotEmptyFields = {Dict.RELEASE_DATE, Dict.GROUP_ID})
	public JsonResult queryDbPath(@RequestBody Map request) throws Exception {
//		String release_node_name = (String) request.get("release_node_name");
//		List<String> project_ids = (List<String>) request.get("ids");
		String release_date = (String) request.get("release_date");
		String group_id = (String) request.get("group_id");
		GroupAbbr group = groupAbbrService.queryGroupAbbr(group_id);
		return JsonResultUtil.buildSuccess(dbReviewService.queryPath(release_date,group.getGroup_abbr(),"1"));
	}

	@ApiOperation(value = "变更文件上传")
	@PostMapping(value = "/uploadAssets")
	public JsonResult uploadAssets(@RequestBody Map request) throws Exception {
		String prod_id = (String) request.get("prod_id");
		String asset_catalog_name = (String) request.get("asset_catalog_name");
		String child_catalog = (String) request.get("child_catalog");
		List<String> filePaths = (List<String>) request.get("filePaths");
		for(String file : filePaths) {
			dbReviewService.uploadAssets(prod_id, asset_catalog_name, child_catalog, file);
		}
		return JsonResultUtil.buildSuccess();
	}
	
	@ApiOperation(value = "是否写入order")
	@PostMapping(value = "/whetherWriteOrder")
	public JsonResult whetherWriteOrder(@RequestBody Map request) throws Exception {
		String prodId = (String) request.get(Dict.PROD_ID);
		String writeFlag = (String) request.get(Dict.WRITE_FLAG);
		String fileName = (String) request.get(Dict.FILENAME);
		String asset_catalog_name = (String) request.get(Dict.ASSET_CATALOG_NAME);
		prodAssetDao.updateAssetWriteFlag(prodId, writeFlag, fileName, asset_catalog_name);
		return JsonResultUtil.buildSuccess();
	}
	
	// 所有变更文件新增write_flag字段
    @PostMapping(value = "/addWriteFlagField")
    public JsonResult addWriteFlagField(@RequestBody Map<String, String> requestParam) throws Exception {
    	prodAssetDao.addWriteFlagField();
        return JsonResultUtil.buildSuccess();
    }

}
