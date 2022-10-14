package com.spdb.fdev.fdevinterface.spdb.callable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.MD5Util;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.entity.RoutesRelation;
import com.spdb.fdev.fdevinterface.spdb.service.RoutesService;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(Dict.VUE_ROUTER_CALLABLE)
@RefreshScope
public class VueRouterCallable extends BaseScanCallable {

    private Logger logger = LoggerFactory.getLogger(VueRouterCallable.class);

    @Autowired
    private RoutesService routesService;
    @Value("${path.project.json}")
    private String projectJsonPath;
    @Value("${path.git.clone}")
    private String gitClonePath;

    @Override
    public Object call() {
        logger.info(Constants.VUE_ROUTER_SCAN_START);
        Map returnMap = new HashMap();
        // 获取配置文件路径
        String projectJsonPath = getProjectJsonPath();
        if (StringUtils.isEmpty(projectJsonPath)) {
            return returnMap;
        }
        try {
            JsonObject jsonObject = this.getJsonObject();
            // 去空格，再MD5
            String newMD5Str = MD5Util.encoder("", jsonObject.toString().replace(" ", ""));
            if (Constants.AUTO_SCAN.equals(super.getScanType())) {
                // 自动扫描只扫master分支的路由提供信息
                if (Dict.MASTER.equals(super.getBranchName())) {
                    routesService.analysisRoutesApi(super.getAppServiceId(), super.getBranchName(), newMD5Str, jsonObject, Constants.AUTO_SCAN);
                }
            } else {
                // 解析路由提供信息
                routesService.analysisRoutesApi(super.getAppServiceId(), super.getBranchName(), newMD5Str, jsonObject, Constants.HAND_SCAN);
            }
            // 解析路由调用信息
            analysisRoutesRelation(newMD5Str, jsonObject);
            logger.info(Constants.VUE_ROUTER_SCAN_END);
            returnMap.put(Dict.SUCCESS, Constants.VUE_ROUTER_SCAN_SUCCESS);
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

    private JsonObject getJsonObject() {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject;
        // 获取配置文件路径
        String projectJsonPath = getProjectJsonPath();
        try (FileReader fileReader = new FileReader(projectJsonPath)) {
            jsonObject = (JsonObject) jsonParser.parse(fileReader);
        } catch (Exception e) {
            logger.error("解析project.json文件出错：{}！", e.getMessage());
            throw new FdevException(ErrorConstants.ROUTES_SCAN_ERROR, new String[]{"解析project.json文件出错！"});
        }
        return jsonObject;
    }

    /**
     * 解析路由调用信息
     *
     * @param newMD5Str
     */
    private void analysisRoutesRelation(String newMD5Str, JsonObject jsonObject) {
        JsonObject deps = jsonObject.getAsJsonObject(Dict.DEPS);
        if (deps == null) {
            throw new FdevException(ErrorConstants.ROUTES_SCAN_ERROR, new String[]{projectJsonPath + "，不存在deps！"});
        }
        JsonArray jsonArray = deps.getAsJsonArray(Dict.ROUTES);
        List<RoutesRelation> oldRoutesRelationList = routesService.getRoutes(super.getAppServiceId(), super.getBranchName());
        if (jsonArray == null || jsonArray.size() == 0) {
            if (CollectionUtils.isNotEmpty(oldRoutesRelationList)) {
                // 删除原有的路由调用关系
                routesService.romoveRoutesRelation(super.getAppServiceId(), super.getBranchName());
            }
        }
        // 去空格，再MD5
        String newRoutesMD5 = MD5Util.encoder("", jsonArray.toString().replace(" ", ""));
        if (CollectionUtils.isEmpty(oldRoutesRelationList)) {
            scanAllRoutesRelation(jsonArray, newMD5Str, newRoutesMD5);
        } else {
            // 从数据库中获取project.json文件的md5值，如果有变化则扫描，没有变化则跳过
            RoutesRelation routesRelation = oldRoutesRelationList.get(0);
            if (newMD5Str.equals(routesRelation.getMd5Str())) {
                return;
            }
            // 从数据库中获取project.json文件里deps字段下routes字段的md5值，如果有变化则扫描，没有变化则跳过
            if (newRoutesMD5.equals(routesRelation.getRoutesMD5())) {
                return;
            }
            // 删除原有的路由调用关系
            routesService.romoveRoutesRelation(super.getAppServiceId(), super.getBranchName());
            // 扫描最新数据入库
            scanAllRoutesRelation(jsonArray, newMD5Str, newRoutesMD5);
        }

    }

    private void scanAllRoutesRelation(JsonArray jsonArray, String newMD5Str, String newRoutesMD5) {
        List<RoutesRelation> routesRelationList = new ArrayList<>();
        try {
            for (JsonElement json : jsonArray) {
                JsonObject jsonObject1 = json.getAsJsonObject();
                String project = jsonObject1.get(Dict.PROJECT).getAsString();
                JsonArray jsonArray1 = jsonObject1.getAsJsonArray(Dict.ROUTES);
                if (!FileUtil.isNullOrEmpty(jsonArray1)) {
                    for (JsonElement routesName : jsonArray1) {
                        RoutesRelation routesRelation = new RoutesRelation();
                        routesRelation.setTargetProject(project);
                        routesRelation.setSourceProject(super.getAppServiceId());
                        routesRelation.setBranch(super.getBranchName());
                        routesRelation.setName(routesName.getAsString());
                        routesRelation.setMd5Str(newMD5Str);
                        routesRelation.setRoutesMD5(newRoutesMD5);
                        routesRelation.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                        routesRelationList.add(routesRelation);
                    }
                }
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.ROUTES_SCAN_ERROR, new String[]{projectJsonPath + "，解析调用路由信息配置错误！"});
        }
        routesService.insertRoutesRelation(routesRelationList);
    }

}
