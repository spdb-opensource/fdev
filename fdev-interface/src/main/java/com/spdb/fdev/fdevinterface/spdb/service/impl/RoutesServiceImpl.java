package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.CommonUtil;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.MD5Util;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.dao.RoutesDao;
import com.spdb.fdev.fdevinterface.spdb.entity.RoutesApi;
import com.spdb.fdev.fdevinterface.spdb.entity.RoutesRelation;
import com.spdb.fdev.fdevinterface.spdb.service.GitLabService;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceLazyInitService;
import com.spdb.fdev.fdevinterface.spdb.service.RoutesService;
import com.spdb.fdev.fdevinterface.spdb.vo.RoutesRelationShow;
import com.spdb.fdev.fdevinterface.spdb.vo.RoutesShow;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoutesServiceImpl implements RoutesService {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Resource
    private RoutesDao routesDao;
    @Autowired
    private InterfaceLazyInitService interfaceLazyInitService;
    @Autowired
    private GitLabService gitLabService;

    @Override
    public void insertRoutesApi(List<RoutesApi> routesApiList) {
        routesDao.saveRoutesApi(routesApiList);
    }

    @Override
    public void insertRoutesRelation(List<RoutesRelation> routesRelationList) {
        routesDao.saveRoutesRelation(routesRelationList);
    }

    @Override
    public Map getRoutesApi(Map params) {
        Map resultMap = new HashMap();
        Map map = routesDao.queryRoutesApi(params);
        List<RoutesApi> routesApiList = (List<RoutesApi>) map.get("list");
        List<RoutesShow> routesShowList = new ArrayList<>();
        for (RoutesApi routesApi : routesApiList) {
            RoutesShow routesShow = CommonUtil.convert(routesApi, RoutesShow.class);
            String appId = interfaceLazyInitService.getAppIdByName(routesShow.getProjectName());
            routesShow.setAppId(appId);
            routesShowList.add(routesShow);
        }
        resultMap.put("list", routesShowList);
        resultMap.put("total", map.get("total"));
        return resultMap;
    }

    @Override
    public Map getRoutesRelation(Map params) {
        Map resultMap = new HashMap();
        Map map = routesDao.queryRoutesRelation(params);
        List<RoutesRelation> routesRelationList = (List<RoutesRelation>) map.get("list");
        List<RoutesRelationShow> routesRelationShowList = new ArrayList<>();
        for (RoutesRelation routesRelation : routesRelationList) {
            RoutesRelationShow routesRelationShow = CommonUtil.convert(routesRelation, RoutesRelationShow.class);
            //根据应用名去查appid
            String targetId = interfaceLazyInitService.getAppIdByName(routesRelationShow.getTargetProject());
            routesRelationShow.setTargetId(targetId);
            String sourceId = interfaceLazyInitService.getAppIdByName(routesRelation.getSourceProject());
            routesRelationShow.setSourceId(sourceId);
            // 除了master分支的其他分支，调的都是SIT的接口
            String branch = routesRelationShow.getBranch();
            if (!Dict.MASTER.equals(branch)) {
                branch = Dict.SIT;
            }
            RoutesApi routesApi = routesDao.queryRoutesDetail(routesRelationShow.getName(), routesRelationShow.getTargetProject(), branch);
            // 关联路由id
            if (routesApi != null) {
                routesRelationShow.setRouteId(routesApi.getId());
            }
            routesRelationShowList.add(routesRelationShow);
        }
        resultMap.put("list", routesRelationShowList);
        resultMap.put("total", map.get("total"));
        return resultMap;
    }

    @Override
    public List<RoutesApi> getRoutesDetailVer(Map params) {
        String id = (String) params.get(Dict.ID);
        RoutesApi routesApi = routesDao.queryRoutesById(id);
        if (routesApi == null) {
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"查询路由详情失败"});
        }
        return routesDao.queryRoutesDetailVer(routesApi.getName(), routesApi.getProjectName(), routesApi.getBranch());
    }

    @Override
    public List<RoutesRelation> getRoutes(String appName, String branch) {
        return routesDao.queryRoutes(appName, branch);
    }

    @Override
    public void removeRoutes(String appName, String branch) {
        routesDao.deleteRoutes(appName, branch);
    }

    @Override
    public void romoveRoutesRelation(String appName, String branch) {
        routesDao.deleteRoutesRelation(appName, branch);
    }

    @Override
    public List<RoutesApi> getRoutesApiList(String projectName, String branchName) {
        return routesDao.queryRoutesApiList(projectName, branchName);
    }

    @Override
    public void updateIsNew(String projectName, String branchName) {
        routesDao.updateIsNew(projectName, branchName);
    }

    @Override
    public void deleteRoutesApi(String projectName, String branchName, int ver) {
        routesDao.deleteRoutesApi(projectName, branchName, ver);
    }

    /**
     * 解析路由提供信息
     *
     * @param appNameEn
     * @param branch
     * @param newMD5Str
     * @param jsonObject
     * @param type
     */
    @Override
    public void analysisRoutesApi(String appNameEn, String branch, String newMD5Str, JsonObject jsonObject, String type) {
        List<RoutesApi> oldRoutesApiList = this.getRoutesApiList(appNameEn, branch);
        JsonArray routesArray = jsonObject.getAsJsonArray(Dict.ROUTES);
        // 去空格，再MD5
        String newRoutesMD5 = MD5Util.encoder("", routesArray.toString().replace(" ", ""));
        if (CollectionUtils.isEmpty(oldRoutesApiList)) {
            this.scanAllRoutesApi(appNameEn, branch, jsonObject, newMD5Str, newRoutesMD5, 0, type);
            return;
        } else {
            // 从数据库中获取project.json文件的md5值，如果有变化则扫描，没有变化则跳过
            RoutesApi oldRoutesApi = oldRoutesApiList.get(0);
            if (newMD5Str.equals(oldRoutesApi.getMd5Str())) {
                return;
            }
            // 从数据库中获取project.json文件里routes字段的md5值，如果有变化则扫描，没有变化则跳过
            if (newRoutesMD5.equals(oldRoutesApi.getRoutesMD5())) {
                return;
            }
            // 将原有数据的is_new改为0
            this.updateIsNew(appNameEn, branch);
            // 扫描新的版本入库
            Integer newVer = oldRoutesApi.getVer() + 1;
            this.scanAllRoutesApi(appNameEn, branch, jsonObject, newMD5Str, newRoutesMD5, newVer, type);
            // 异步删除老数据，只保留最新5个版本
            this.deleteRoutesApi(appNameEn, branch, newVer - 5);
        }
    }

    /**
     * 判断路由是否有变化，是否需要生成介质
     *
     * @param appNameEn
     * @param lastBranch
     * @param newMD5Str
     * @param jsonObject
     * @return returnMap中的flag 0：不需要扫描，且不需要生成介质     1:需要扫描，且需要生成介质      2：不需要扫描，但需要生成介质
     */
    public Map<String, Integer> getMD5AndVer(String appNameEn, Integer gitLabId, String lastBranch, String newMD5Str, JsonObject jsonObject) {
        Map<String, Integer> returnMap = new HashMap<>();
        // 若获取上一次的branch为空，说明本次是第一次生成介质
        if (StringUtils.isEmpty(lastBranch)) {
            returnMap.put(Dict.FLAG, 1);
            returnMap.put(Dict.VER, 0);
            return returnMap;
        }
        JsonArray routesArray = jsonObject.getAsJsonArray(Dict.ROUTES);
        // 去空格，再MD5
        String newRoutesMD5 = MD5Util.encoder("", routesArray.toString().replace(" ", ""));
        List<RoutesApi> oldRoutesApiList = this.getRoutesApiList(appNameEn, lastBranch);
        // 若为tag，则需要实时获取上次投产tag的md5
        Map<String, Integer> tagMd5Map = this.tagMd5Map(appNameEn, gitLabId, lastBranch, newMD5Str, newRoutesMD5, oldRoutesApiList);
        if (tagMd5Map.size() != 0) {
            return tagMd5Map;
        }
        if (CollectionUtils.isEmpty(oldRoutesApiList)) {
            // 记录MD5是否变化
            returnMap.put(Dict.FLAG, 1);
            returnMap.put(Dict.VER, 0);
            return returnMap;
        } else {
            // 判断project.json文件的md5值是否有变化
            RoutesApi oldRoutesApi = oldRoutesApiList.get(0);
            if (newMD5Str.equals(oldRoutesApi.getMd5Str())) {
                // 没有变化，则需判断上一次扫描是否为自动扫描，若为自动扫描，则本次不扫，且不生成介质
                String type = oldRoutesApi.getType();
                if (Constants.AUTO_SCAN.equals(type)) {
                    returnMap.put(Dict.FLAG, 0);
                } else {
                    // 若为手动扫描，则本次不扫(需要将手动改为自动)，但需要生成介质
                    returnMap.put(Dict.FLAG, 2);
                    returnMap.put(Dict.VER, oldRoutesApi.getVer());
                }
                return returnMap;
            }
            // 从数据库中获取project.json文件里routes字段的md5值
            if (newRoutesMD5.equals(oldRoutesApi.getRoutesMD5())) {
                // 没有变化，则需判断上一次扫描是否为自动扫描，若为自动扫描，则本次不扫，且不生成介质
                String type = oldRoutesApi.getType();
                if (Constants.AUTO_SCAN.equals(type)) {
                    returnMap.put(Dict.FLAG, 0);
                } else {
                    // 若为手动扫描，则本次不扫(需要将手动改为自动)，但需要生成介质
                    returnMap.put(Dict.FLAG, 2);
                    returnMap.put(Dict.VER, oldRoutesApi.getVer());
                }
                return returnMap;
            }
            returnMap.put(Dict.FLAG, 1);
            returnMap.put(Dict.VER, oldRoutesApi.getVer() + 1);
            return returnMap;
        }
    }

    /**
     * 若为tag，则需要实时获取上次投产tag的md5
     *
     * @param appNameEn
     * @param gitLabId
     * @param lastBranch
     * @param newMD5Str
     * @param newRoutesMD5
     * @param oldRoutesApiList
     * @return
     */
    private Map<String, Integer> tagMd5Map(String appNameEn, Integer gitLabId, String lastBranch, String newMD5Str, String newRoutesMD5, List<RoutesApi> oldRoutesApiList) {
        Map<String, Integer> returnMap = new HashMap<>();
        if (lastBranch.startsWith(Dict.PRO) || lastBranch.startsWith(Dict.PRO_LOWER)) {
            // 当前tag的版本只记录最新的，若库里存在之前手动扫描的数据，直接删掉
            if (CollectionUtils.isNotEmpty(oldRoutesApiList)) {
                routesDao.deleteRoutes(appNameEn, lastBranch);
            }
            String fileContent = gitLabService.getFileContent(gitLabId, lastBranch, "project.json");
            String lastTagMd5Str = MD5Util.encoder("", fileContent.replace(" ", ""));
            if (newMD5Str.equals(lastTagMd5Str)) {
                returnMap.put(Dict.FLAG, 0);
                return returnMap;
            } else {
                if (StringUtils.isEmpty(fileContent)) {
                    returnMap.put(Dict.FLAG, 1);
                    returnMap.put(Dict.VER, 0);
                    return returnMap;
                }
                JsonObject lastTagProjectJson = new JsonParser().parse(fileContent).getAsJsonObject();
                JsonArray lastTagRoutesArray = lastTagProjectJson.getAsJsonArray(Dict.ROUTES);
                if (lastTagRoutesArray == null) {
                    returnMap.put(Dict.FLAG, 1);
                    returnMap.put(Dict.VER, 0);
                    return returnMap;
                }
                // 去空格，再MD5
                String lastTagRoutesMD5 = MD5Util.encoder("", lastTagRoutesArray.toString().replace(" ", ""));
                if (newRoutesMD5.equals(lastTagRoutesMD5)) {
                    returnMap.put(Dict.FLAG, 0);
                    return returnMap;
                } else {
                    returnMap.put(Dict.FLAG, 1);
                    returnMap.put(Dict.VER, 0);
                    return returnMap;
                }
            }
        } else {
            return returnMap;
        }
    }

    @Override
    public void analysisRoutesApi(Map<String, Integer> md5AndVer, String appNameEn, String branch, String newMD5Str, JsonObject jsonObject, String type) {
        Integer flag = md5AndVer.get(Dict.FLAG);
        Integer ver = md5AndVer.get(Dict.VER);
        if (flag == 0) {
            return;
        } else if (flag == 1) {
            JsonArray routesArray = jsonObject.getAsJsonArray(Dict.ROUTES);
            // 去空格，再MD5
            String newRoutesMD5 = MD5Util.encoder("", routesArray.toString().replace(" ", ""));
            if (ver == 0) {
                this.scanAllRoutesApi(appNameEn, branch, jsonObject, newMD5Str, newRoutesMD5, 0, type);
            } else {
                // 将原有数据的is_new改为0
                this.updateIsNew(appNameEn, branch);
                this.scanAllRoutesApi(appNameEn, branch, jsonObject, newMD5Str, newRoutesMD5, ver, type);
                // 异步删除老数据，只保留最新5个版本
                this.deleteRoutesApi(appNameEn, branch, ver - 5);
            }
        } else {
            // 将手动更新为自动
            this.updateScanType(appNameEn, branch, ver);
        }
    }

    private void scanAllRoutesApi(String appNameEn, String branch, JsonObject jsonObject, String newMD5Str, String newRoutesMD5, Integer ver, String type) {
        List<RoutesApi> routesApiList = new ArrayList<>();
        JsonArray routesArray = jsonObject.getAsJsonArray(Dict.ROUTES);
        try {
            if (!FileUtil.isNullOrEmpty(routesArray)) {
                // 解析具体路由信息
                for (JsonElement json : routesArray) {
                    RoutesApi routesApi = new RoutesApi();
                    routesApi.setType(type);
                    JsonObject jsonObject1 = json.getAsJsonObject();
                    String routesName = jsonObject1.get(Dict.NAME).getAsString();
                    routesApi.setName(routesName);
                    routesApi.setProjectName(appNameEn);
                    routesApi.setMd5Str(newMD5Str);
                    routesApi.setRoutesMD5(newRoutesMD5);
                    routesApi.setVer(ver);
                    routesApi.setIsNew(1);
                    routesApi.setBranch(branch);
                    routesApi.setModule(jsonObject1.get(Dict.MODULE).getAsString());
                    routesApi.setPath(jsonObject1.get(Dict.PATH).getAsString());
                    JsonObject paramsObject = jsonObject1.getAsJsonObject(Dict.PARAMS);
                    Map paramsMap = new HashMap<>();
                    if (!FileUtil.isNullOrEmpty(paramsObject)) {
                        paramsObject.entrySet()
                                .forEach(s -> paramsMap.put(s.getKey(), s.getValue().getAsString()));
                        routesApi.setParams(paramsMap);
                    }
                    JsonArray jsonArray1 = jsonObject1.getAsJsonArray(Dict.AUTH_CHECK);
                    List<String> authorityList = new ArrayList<>();
                    if (!FileUtil.isNullOrEmpty(jsonArray1)) {
                        for (JsonElement authority : jsonArray1) {
                            authorityList.add(authority.getAsString());
                        }
                    }
                    routesApi.setAuthCheck(authorityList);
                    if (!FileUtil.isNullOrEmpty(jsonObject1.get(Dict.QUERY))) {
                        JsonObject query = jsonObject1.get(Dict.QUERY).getAsJsonObject();
                        Map map1 = new HashMap();
                        for (Map.Entry<String, JsonElement> queryEntry : query.entrySet()) {
                            JsonObject value = queryEntry.getValue().getAsJsonObject();
                            String key = queryEntry.getKey();
                            for (Map.Entry<String, JsonElement> valueEntry : value.entrySet()) {
                                Map map2 = new HashMap();
                                map2.put(valueEntry.getKey(), valueEntry.getValue().getAsBoolean());
                                map1.put(key, map2);
                            }
                        }
                        routesApi.setQuery(map1);
                    }
                    JsonElement extraElement = jsonObject1.get(Dict.EXTRA);
                    if (!FileUtil.isNullOrEmpty(extraElement)) {
                        Map extraMap1 = new HashMap<>();
                        List<Object> extraList = new ArrayList<>();
                        if (extraElement.isJsonArray()) {
                            JsonArray extraArray = extraElement.getAsJsonArray();
                            extraArray.forEach(s -> {
                                Map extraMap2 = new HashMap<>();
                                JsonObject extraObject = s.getAsJsonObject();
                                extraObject.entrySet().forEach(r -> extraMap2.put(r.getKey(), r.getValue().toString()));
                                extraList.add(extraMap2);
                            });
                            routesApi.setExtra(extraList);
                        } else {
                            JsonObject extraObject = extraElement.getAsJsonObject();
                            extraObject.entrySet().forEach(s -> extraMap1.put(s.getKey(), s.getValue().toString()));
                            extraList.add(extraMap1);
                            routesApi.setExtra(extraList);
                        }
                    }
                    routesApi.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                    routesApiList.add(routesApi);
                }
            }
        } catch (Exception e) {
            throw new FdevException(ErrorConstants.ROUTES_SCAN_ERROR, new String[]{"解析project.json文件出错！"});
        }
        this.insertRoutesApi(routesApiList);
    }

    /**
     * 更新扫描类型
     *
     * @param appNameEn
     * @param branch
     * @param ver
     */
    private void updateScanType(String appNameEn, String branch, Integer ver) {
        routesDao.updateScanType(appNameEn, branch, ver);
    }

}
