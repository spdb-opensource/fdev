package com.spdb.fdev.fdevinterface.spdb.callable;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.entity.TransRelation;
import com.spdb.fdev.fdevinterface.spdb.service.TransRelationService;

@Component(Dict.TRANS_RELATION_VUE_CALLABLE)
public class TransRelationVueCallable extends BaseScanCallable {

	private Logger logger = LoggerFactory.getLogger(TransRelationVueCallable.class);

	@Autowired
	private TransRelationService transRelationService;
	@Value("${path.project.json}")
	private String projectJsonPath;

	@Value("${path.git.clone}")
	private String gitClonePath;

	@Override
	public Object call() {
		logger.info(Constants.TRANS_RELATION_SCAN_START);
		Map returnMap = new HashMap();
		// 获取配置文件路径
		String projectJsonPath = getProjectJsonPath();
		if (StringUtils.isEmpty(projectJsonPath)) {
			return returnMap;
		}
		try {
			List<TransRelation> list = analysisJson(projectJsonPath);
			transRelationService.save(list, super.getAppServiceId(), super.getBranchName(), Dict.AJSON);
			logger.info(Constants.TRANS_RELATION_SCAN_END);
			returnMap.put(Dict.SUCCESS, Constants.TRANS_RELATION_SCAN_SUCCESS);
			return returnMap;
		} catch (FdevException e) {
			returnMap.put(Dict.ERROR, errorMessageUtil.get(e));
            logger.error("{}", errorMessageUtil.get(e));
            return returnMap;
		}
	}

	/**
	 * 获取客户端vue项目交易配置文件路径
	 *
	 * @return
	 */
	private String getProjectJsonPath() {
		String transConfigPath = gitClonePath + projectJsonPath;
		File file = new File(transConfigPath);
		if (file.exists()) {
			return transConfigPath;
		}
		return null;
	}

	/**
	 * 解析json文件
	 * 
	 * @param projectJsonPath
	 * @return
	 */
	private List<TransRelation> analysisJson(String projectJsonPath) {
		JsonParser jsonParser = new JsonParser();
		List<TransRelation> transRelationList = new ArrayList<>();
		Map map = new HashMap();
		try (FileReader fileReader = new FileReader(projectJsonPath)) {
			JsonObject jsonObject = (JsonObject) jsonParser.parse(fileReader);
			// 解析deps获取调用交易信息
			JsonObject deps = jsonObject.getAsJsonObject(Dict.DEPS);
			if (deps == null) {
				throw new FdevException(ErrorConstants.TRANS_RELATION_SCAN_ERROR,
						new String[] { projectJsonPath + "，不存在deps！" });
			}
			JsonArray jsonArray = deps.getAsJsonArray(Dict.TRANS);
			if (!FileUtil.isNullOrEmpty(jsonArray)) {
				for (JsonElement json : jsonArray) {
					JsonObject jsonObject1 = json.getAsJsonObject();
					String project = jsonObject1.get(Dict.APP).getAsString();
					JsonArray jsonArray1 = jsonObject1.getAsJsonArray(Dict.TRANS);
					if (!FileUtil.isNullOrEmpty(jsonArray1)) {
						for (JsonElement transName : jsonArray1) {
							TransRelation transRelation = new TransRelation();
							transRelation.setTransId(transName.getAsString());
							transRelation.setServiceCalling(super.getAppServiceId());
							transRelation.setServiceId(project);
							transRelation.setBranch(super.getBranchName());
							transRelation.setChannel(Dict.AJSON); 
							transRelation.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
							transRelationList.add(transRelation);
						}
					}
				}
			}
		}catch (FdevException e) {
			throw new FdevException(ErrorConstants.TRANS_RELATION_SCAN_ERROR, new String[]{errorMessageUtil.get(e)});
		}catch (JsonSyntaxException e) {
			throw new FdevException(ErrorConstants.TRANS_RELATION_SCAN_ERROR, new String[]{"project.json文件格式错误！"});
		}catch (Exception e) {
			logger.error("解析文件project.json出错！{}！", e.getMessage());
			throw new FdevException(ErrorConstants.TRANS_RELATION_SCAN_ERROR, new String[] { "解析文件project.json出错！" });
		}
		return transRelationList;
	}

}
