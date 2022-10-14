package com.spdb.fdev.release.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.base.utils.zipUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.release.dao.IDbReviewDao;
import com.spdb.fdev.release.dao.IProdAssetDao;
import com.spdb.fdev.release.entity.*;
import com.spdb.fdev.release.service.*;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DbReviewServiceImpl implements IDbReviewService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());// 日志打印

	@Autowired
	private RestTransport restTransport;
	@Autowired
	private IDbReviewDao dbReviewDao;
	@Autowired
	private IReleaseTaskService releaseTaskService;
	@Autowired
	private ITaskService taskService;
	@Autowired
	private IFileService fileService;
	@Autowired
	private IGroupAbbrService groupAbbrService;
    @Autowired
    private IProdRecordService prodRecordService;
    @Autowired
    private IReleaseCatalogService catalogService;
    @Autowired
    private IReleaseCatalogService releaseCatalogService;
    @Autowired
    IReleaseNodeService releaseNodeService;
    @Autowired
    private IRoleService roleService;
    @Autowired
    private IAppService appService;
    @Resource
    private IProdAssetDao prodAssetDao;
    @Value("${prod.record.minio.url}")
    private String recordAssetsUrl;// /testassets-sit/
    
    @Autowired
	private IUserService userService;


	@Override
	public DbReview query(String task_id) {
		return dbReviewDao.query(task_id);
	}

	@Override
	public void update(DbReview dbReview1) {
		dbReviewDao.update(dbReview1);
	}

	@Override
	public void dbReviewUpload(String task_id) {
		try {
			Map<String, Object> releaseInfo = releaseTaskService.queryDetailByTaskId(task_id, "");
			Map fdevTask = taskService.queryInfoById(task_id);
			String groupId = (String) fdevTask.get("group");
			GroupAbbr group = groupAbbrService.queryGroupAbbr(groupId);
			//当前任务关联用户小组为空时，查询用户所属第三层级组
			if(Util.isNullOrEmpty(group)) {
				Map<String,Object> userGroup = userService.getThreeLevelGroup(groupId);
				String threeLevelId = (String) userGroup.get(Dict.ID);
				group = groupAbbrService.queryGroupAbbr(threeLevelId);
			}
			
			//已挂载
			if(!CommonUtils.isNullOrEmpty(releaseInfo)){
				String release_node_name = (String) releaseInfo.get("release_node_name");   //投产窗口
				if(!CommonUtils.isNullOrEmpty(fdevTask)){
					Map reviewRecordInfo = (Map) fdevTask.get("reviewRecord");
					List<Map<String, String>> newDoc = (List<Map<String, String>>) fdevTask.get("newDoc");
					String project_id = (String) fdevTask.get("project_id"); //应用id
					String releaseDate = (String) releaseInfo.get(Dict.RELEASE_DATE);
					String minioPath = "/db-review/" + releaseDate + "/" + group.getGroup_abbr() + "/";
					//审核通过
					if ("通过".equals(reviewRecordInfo.get("reviewStatus").toString())) {
						if (!CommonUtils.isNullOrEmpty(newDoc)) {
							List<String> minIo_path = new ArrayList<>();
							List<String> zip_path = new ArrayList<>();
							Boolean isflag = false;
							for (Map map : newDoc) {
								if ("审核类-数据库审核材料".equals((String) map.get("type")) && ((String) map.get("name")).contains(".zip")) {
									isflag = true;
									//获取文件并解压文件，获取sql和sh文件上传到投产模块的文件中
									List<String> paths = filesDownload("fdev-task", (String) map.get("path"), (String) map.get("name"), minioPath, task_id);
									minIo_path.addAll(paths);
									zip_path.add((String) map.get("path"));
								}
							}
							//同步数据到库中
							if(isflag){
								DbReview dbReview1 = new DbReview(task_id, release_node_name, project_id, zip_path, minIo_path, "1");
								dbReview1.setGroup_abbr(group.getGroup_abbr());//小组标识
								dbReview1.setRelease_date(releaseDate.replace("-","/"));//投产日期
								dbReviewDao.infoUpsert(dbReview1);
							}
						}
					} else {   // 挂载 、审核未通过
						if (!CommonUtils.isNullOrEmpty(newDoc)) {
							List<String> minIo_path = new ArrayList<>();
							List<String> zip_path = new ArrayList<>();
							Boolean isflag = false;
							for (Map map : newDoc) {
								if ("审核类-数据库审核材料".equals((String) map.get("type")) && ((String) map.get("name")).contains(".zip")) {
									isflag = true;
									zip_path.add((String) map.get("path"));
								}
							}
							if(isflag){
								DbReview dbReview1 = new DbReview(task_id, release_node_name, project_id, zip_path, minIo_path, "0");
								dbReview1.setGroup_abbr(group.getGroup_abbr());//小组标识
								dbReview1.setRelease_date(releaseDate.replace("-","/"));//投产日期
								dbReviewDao.infoUpsert(dbReview1);
							}
						}
					}
				}
			} else {   //未挂载 、审核通过
				if(!CommonUtils.isNullOrEmpty(fdevTask)){
					Map reviewRecordInfo = (Map) fdevTask.get("reviewRecord");
					String plan_fire_time = (String) fdevTask.get("plan_fire_time");
                    String minioPath = "/db-review/" + plan_fire_time.replace("/","-") + "/" + group.getGroup_abbr() + "/";
					if("通过".equals(reviewRecordInfo.get("reviewStatus").toString())){
						List<Map<String, String>> newDoc = (List<Map<String, String>>) fdevTask.get("newDoc");
						String project_id = (String) fdevTask.get("project_id"); //应用id
						if (!CommonUtils.isNullOrEmpty(newDoc)) {
							List<String> minIo_path = new ArrayList<>();
							List<String> zip_path = new ArrayList<>();
							Boolean isflag = false;
							for (Map map : newDoc) {
								if ("审核类-数据库审核材料".equals((String) map.get("type")) && ((String) map.get("name")).contains(".zip")) {
									isflag = true;
                                    //获取文件并解压文件，获取sql和sh文件上传到投产模块的文件中
                                    List<String> paths = filesDownload("fdev-task", (String) map.get("path"), (String) map.get("name"), minioPath, task_id);
                                    minIo_path.addAll(paths);
                                    zip_path.add((String) map.get("path"));
								}
							}
							if(isflag){
								DbReview dbReview1 = new DbReview(task_id, "", project_id, zip_path, minIo_path, "1");
								dbReview1.setGroup_abbr(group.getGroup_abbr());//小组标识
                                dbReview1.setRelease_date(plan_fire_time);//投产日期
								dbReviewDao.infoUpsert(dbReview1);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("上传数据库审核文件失败");
		}
	}

	public List<String> filesDownload(String moduleName, String path, String fileName, String minioPath, String task_id) {
		byte[] result = fileService.downloadDocumentFileToByte(moduleName, path);
		//zip文件临时存储路径
		String zippath = "/minio/dbreview/"+task_id+"/";
		//将byte数组转成文件存储在本地
        zipUtils.byteToFile(result,zippath,fileName);
		//解压文件到本地指点路径下
		File file = new File(zippath + fileName);
		//解压文件临时存储路径
		String filepath = "/minio/dbreview/"+task_id+"/zip/";
		zipUtils.upZipFiles(file, filepath);
		//获取路径下的sql和sh文件，并上传投产模块的minio
		List<String> paths = new ArrayList<>();

		scannGetFile(new File(filepath), minioPath, paths);
		//删除本地存储文件
		zipUtils.deleteFile(new File(zippath));
		return  paths;
	}

	//获取路径下的sql和sh文件，并上传投产模块的minio
	private List<String> scannGetFile(File file, String path, List<String> paths) {
		File[] tempLists = file.listFiles();
		for (File file1 : tempLists) {
			if (file1.isDirectory()) {
				scannGetFile(file1, path, paths);
			} else if(file1.isFile()){
				String pathSql = "";
				if(file1.getName().contains(".sql") || file1.getName().contains(".sh")){
					if(file1.getParent().split("/zip/").length > 1){
						pathSql = file1.getParent().split("/zip/")[1] + "/";
					}
					uploadFile(path+pathSql+file1.getName(), file1, "fdev-release");
					paths.add(path+pathSql+file1.getName());
				}
			}
		}
		return paths;
	}

	/**
	 * 根据路径上传文件
	 * @param path
	 * @param file
	 * @param moduleName
	 */
	public void uploadFile(String path, File file, String moduleName){
		Map<String, Object> param = new HashMap<>();
		param.put("moduleName", moduleName);
		param.put("path", path);
		param.put("files", new FileSystemResource(file));
		try {
			restTransport.submitUpload("filesUpload", param);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<String> queryDbPath(String release_node_name, List<String> project_ids) {
		List<String> result = new ArrayList<>();
		List<DbReview> dbReviews = dbReviewDao.queryDbPath(release_node_name, project_ids,"1");
		if(!CommonUtils.isNullOrEmpty(dbReviews)){
			for(DbReview dbReview : dbReviews){
				result.addAll(dbReview.getMinIo_path());
			}
		}
		return result;
	}


	@Override
	public Object queryPath(String releaseDate, String group_id,String reviewStatus) {
		List<String> result = new ArrayList<>();
		List<DbReview> dbReviews = dbReviewDao.queryPath(releaseDate, group_id,reviewStatus);
		if(!CommonUtils.isNullOrEmpty(dbReviews)){
			for(DbReview dbReview : dbReviews){
				result.addAll(dbReview.getMinIo_path());
			}
		}
		return result;
	}

    @Override
    public Map uploadAssets(String prod_id, String asset_catalog_name, String child_catalog, String file) throws Exception {
        // 参数校验
        if (CommonUtils.isNullOrEmpty(prod_id)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.PROD_ID});
        }
        //获取文件名称
		int index = file.lastIndexOf("/");
		String filename = file.substring(index+1);
        // 变更记录日期小于当前日期,不能操作
        ProdRecord prodRecord = prodRecordService.queryDetail(prod_id);
        AssetCatalog assetCatalog = catalogService.queryAssetCatalogByName(prodRecord.getTemplate_id(), asset_catalog_name);
        ReleaseNode releaseNode = releaseNodeService.queryDetail(prodRecord.getRelease_node_name());
        if(CommonUtils.isNullOrEmpty(releaseNode)) {
            throw new FdevException(ErrorConstants.RELEASE_NODE_NOT_EXIST, new String[]{prodRecord.getRelease_node_name()});
        }
        if (CommonUtils.isNullOrEmpty(asset_catalog_name)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.ASSET_CATALOG_NAME});
        }
        if (CommonUtils.isNullOrEmpty(file)) {
            throw new FdevException(ErrorConstants.PARAM_CANNOT_BE_EMPTY, new String[]{Dict.FILE});
        }
		if (filename.contains(" ")) {
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"文件名不能有空格"});
		}
		if (CommonUtils.isNullOrEmpty(prodRecord)) {
			throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"变更记录不存在"});
		}
		// 检查当前窗口下配置文件名唯一性
		ProdAsset savedProdAsset = queryAssetByName(prod_id, asset_catalog_name, filename, null);
		if (!CommonUtils.isNullOrEmpty(savedProdAsset)) {
			throw new FdevException(ErrorConstants.REPET_INSERT_REEOR, new String[]{"文件名已存在,请更换"});
		}
        ProdAsset prodAsset = new ProdAsset();
		// 变更级别上传： 该小组投产管理员,当前投产窗口投产负责人、本次投产窗口科技负责人
		if (!(roleService.isGroupReleaseManager(CommonUtils.getSessionUser().getGroup_id())
				|| releaseNode.getRelease_manager().equals(CommonUtils.getSessionUser().getUser_name_en())
				|| releaseNode.getRelease_spdb_manager()
				.equals(CommonUtils.getSessionUser().getUser_name_en()))) {
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"此操作为变更级别上传,没有操作权限"});
		}
        prodAsset.setProd_id(prod_id);
        prodAsset.setAsset_catalog_name(asset_catalog_name);
        prodAsset.setChild_catalog(child_catalog);
        // 有序文件,系统查询介质目录有值则size+1,没有值则从1开始
		List<ProdAsset> assetsWithSeqno = queryAssetsWithSeqno(prod_id, asset_catalog_name);
		if (!CommonUtils.isNullOrEmpty(assetsWithSeqno)) {
			prodAsset.setSeq_no(String.valueOf(assetsWithSeqno.size() + 1));
		} else {
			prodAsset.setSeq_no("1");
		}
		User user = CommonUtils.getSessionUser();
		Map prodAssetMap = new HashMap<>();
		// 根据介质目录名查询目录
		//目录类型为1或2时,不能上传文件
		if ("1".equals(assetCatalog.getCatalog_type()) || "2".equals(assetCatalog.getCatalog_type())) {
			throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"当前目录类型不能上传文件,请更换目录类型"});
		}
		prodAsset.setRelease_node_name(prodRecord.getRelease_node_name());
		// 方式为上传文件
		prodAsset.setSource("1");
		String upload_time = CommonUtils.formatDate("yyyy-MM-dd HH:mm:ss");
		prodAsset.setUpload_user(user.getId());
		prodAsset.setUpload_time(upload_time);
		prodAsset.setFileName(filename);
		prodAsset.setFile_giturl(file);
		prodAsset.setWrite_flag("0");
		ProdAsset saveProdAsset = prodAssetDao.save(prodAsset);
		ProdAsset queryAssetsOne = prodAssetDao.queryAssetsOne(saveProdAsset.getId());
		prodAssetMap.put(saveProdAsset.getId(), queryAssetsOne);
		return prodAssetMap;
    }

    @Override
    public ProdAsset queryAssetByName(String prod_id, String asset_catalog_name, String file_name, String runtime_env) throws Exception {
        return prodAssetDao.queryAssetByName(prod_id, asset_catalog_name, file_name, runtime_env);
    }

    @Override
    public List<ProdAsset> queryAssetsWithSeqno(String prod_id, String asset_catalog_name) throws Exception {
        return prodAssetDao.queryAssetsWithSeqno(prod_id, asset_catalog_name);
    }

}
