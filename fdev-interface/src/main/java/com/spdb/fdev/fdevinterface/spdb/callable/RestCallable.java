package com.spdb.fdev.fdevinterface.spdb.callable;

import com.esotericsoftware.minlog.Log;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.common.util.Util;
import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.dict.ErrorConstants;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.base.utils.TimeUtils;
import com.spdb.fdev.fdevinterface.spdb.entity.RestApi;
import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component(Dict.REST_CALLABLE)
public class RestCallable extends BaseScanCallable {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    // 路径相关
    @Value(value = "${path.rest.server.schema}")
    private String restSchema;
    @Value(value = "${path.rest.server.header}")
    private String restHeader;
    @Value(value = "${path.rest.server.header.other}")
    private String restHeaderOther;
    @Value(value = "${path.rest.server.schemaconfig}")
    private String restSchemaConfig;

    @Override
    public Map call() {
        logger.info(Constants.REST_PROVIDER_SCAN_START);
        Map returnMap = new HashMap();
        try {
            // 获取报文头路径
            String headerPath = getRestHeaderPath(super.getSrcPathList());
            // schema-config.xml文件路径
            String schemaConfigPath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, restSchemaConfig);
            // 获取Rest接口调用方schema文件夹路径
            String schemaPath = FileUtil.getFilePath(super.getSrcPathList(), mainResources, restSchema);
            if (StringUtils.isEmpty(headerPath) || StringUtils.isEmpty(schemaConfigPath) || StringUtils.isEmpty(schemaPath)) {
            	logger.info("请检查应用rest相关配置文件路径");
            	logger.info("报文头路径:"+headerPath);
            	logger.info("schema-config.xml文件路径:"+schemaConfigPath);
            	logger.info("Rest接口调用方schema文件夹路径:"+schemaPath);
                return returnMap;
            }
            // 获取json文件对应的Repository ID，Header Repository ID，schema-config Repository ID
            List<RestApi> repoIdList = getRepoId(schemaConfigPath, schemaPath);
            List<RestApi> restApiList;
            // 获取数据库数据
            List<RestApi> oldRestApiList = interfaceService.getRestApiList(super.getAppServiceId(), super.getBranchName());
            if (CollectionUtils.isEmpty(oldRestApiList)) {
                // 根据解析出的sop接口别名，去扫描对应xml文件
                restApiList = scanAll(repoIdList, headerPath, schemaConfigPath, schemaPath);
            } else {
                // 只扫描有变化的xml文件
                restApiList = scanChange(repoIdList, oldRestApiList, headerPath, schemaConfigPath, schemaPath);
            }
            interfaceService.saveRestApiList(restApiList);
            // 将没有在schema-config.xml中注册的接口交易码返给前端
            returnMap.put(Dict.SUCCESS, returnMsg());
            logger.info(Constants.REST_PROVIDER_SCAN_END);
            return returnMap;
        } catch (FdevException e) {
            returnMap.put(Dict.ERROR, errorMessageUtil.get(e));
            logger.error("{}", errorMessageUtil.get(e));
            return returnMap;
        }
    }

    /**
     * 获取Rest接口提供方报文头的文件路径
     *
     * @param srcPath
     */
    private String getRestHeaderPath(List<String> srcPath) {
        String headerPath = FileUtil.getFilePath(srcPath, mainResources, restHeader);
        if (!StringUtils.isEmpty(headerPath)) {
            return headerPath;
        }
        headerPath = FileUtil.getFilePath(srcPath, mainResources, restHeaderOther);
        return headerPath;
    }

    /**
     * 获取json文件对应的Repository ID，Header Repository ID，schema-config Repository ID
     *
     * @param schemaConfigPath
     * @param schemaPath
     * @return
     */
    private List<RestApi> getRepoId(String schemaConfigPath, String schemaPath) {
        List<RestApi> restApiList = new ArrayList<>();
        // 拼接GitLab上schema-config.xml文件所在文件夹路径
        String configAfterResourcesPath = schemaConfigPath.split(Dict.RESOURCES)[1];
        String configBeforeSchemaPath = configAfterResourcesPath.split("/schema-config")[0];
        String configRepoPath = gitSrcMainResources + configBeforeSchemaPath;
        // 拼接GitLab上schema文件夹路径
        String schemaAfterResourcesPath = schemaPath.split(Dict.RESOURCES)[1];
        String schemaRepoPath = gitSrcMainResources + schemaAfterResourcesPath;
        // 如果interfacePathArray的长度为3，说明src文件夹外面还有一层以appServiceId(应用英文名)命名的文件夹
        if (schemaPath.contains(super.getAppServiceId())) {
            configRepoPath = super.getAppServiceId() + pathJoin + configRepoPath;
            schemaRepoPath = super.getAppServiceId() + pathJoin + schemaRepoPath;
        }
        List<Map> configFilesRepoList = gitLabService.getProjectsRepository(super.getProjectId(), super.getAppServiceId(), super.getBranchName(), configRepoPath);
        Map headerMap = new HashMap();
        Map configMap = new HashMap();
        for (Map map : configFilesRepoList) {
            if (map.get(Dict.NAME).equals(Constants.REST_HEADER)) {
                headerMap = map;
            }
            if (map.get(Dict.NAME).equals(Constants.SCHEMA_CONFIG)) {
                configMap = map;
            }
        }
        // 获取GitLab上interface文件夹下所有子文件的Repository ID
        List<Map> interfaceRepositoryList = gitLabService.getProjectsRepository(super.getProjectId(), super.getAppServiceId(), super.getBranchName(), schemaRepoPath);
        // RestHeader.json文件不跟schema-config.xml同一目录的话，就一定在schema文件夹下
        if (Util.isNullOrEmpty(headerMap)) {
            for (Map map : interfaceRepositoryList) {
                if (map.get(Dict.NAME).equals(Constants.REST_HEADER)) {
                    headerMap = map;
                }
            }
        }
        if (headerMap.size() == 0) {
            throw new FdevException(ErrorConstants.REST_PROVIDER_SCAN_ERROR, new String[]{"获取头文件的Repository ID失败！"});
        }
        for (Map map : interfaceRepositoryList) {
            String transId = (String) map.get(Dict.NAME);
            if (!map.get(Dict.NAME).equals(Constants.REST_HEADER)) {
                RestApi restApi = new RestApi();
                restApi.setTransId(transId.replaceAll(Constants.JSON_FILE, ""));
                restApi.setRepositoryId((String) map.get(Dict.ID));
                restApi.setHeaderRepositoryId((String) headerMap.get(Dict.ID));
                restApi.setSchemaConfigRepositoryId((String) configMap.get(Dict.ID));
                restApiList.add(restApi);
            }
        }
        return restApiList;
    }

    /**
     * 根据Repository ID去扫描对应json文件
     *
     * @param mappingAndRepoIdList
     * @param headerPath
     * @param schemaConfigPath
     * @param schemaPath
     * @return
     */

    private List<RestApi> scanAll(List<RestApi> mappingAndRepoIdList, String headerPath, String schemaConfigPath, String schemaPath) {
        List<RestApi> restApiList = new ArrayList<>();
        RestApi header = getRestHeader(headerPath);
        List<Map<String, String>> registerList = getSchemaConfig(schemaConfigPath);
        for (RestApi repo : mappingAndRepoIdList) {
            String jsonPath = schemaPath + pathJoin + repo.getTransId() + Constants.JSON_FILE;
            // 获取RestApi信息
            if (!jsonPath.contains(Constants.EXAMPLE_JSON)) {
                RestApi restApi = getRestApi(jsonPath, header, repo, registerList);
                restApiList.add(restApi);
            }
        }
        return restApiList;
    }

    /**
     * 只扫描有变化的
     *
     * @param repoIdList
     * @param oldRestApiList
     * @param headerPath
     * @param schemaConfigPath
     * @param schemaPath
     * @return
     */
    private List<RestApi> scanChange(List<RestApi> repoIdList, List<RestApi> oldRestApiList, String headerPath, String schemaConfigPath, String schemaPath) {
        List<RestApi> restApiList = new ArrayList<>();
        List<String> notNewIdList = new ArrayList<>();
        List<RestApi> updateRegisterList = new ArrayList<>();
        RestApi header = getRestHeader(headerPath);
        List<Map<String, String>> registerList = getSchemaConfig(schemaConfigPath);
        for (RestApi repo : repoIdList) {
            String transId = repo.getTransId();
            String jsonPath = schemaPath + pathJoin + transId + Constants.JSON_FILE;
            String repositoryId = repo.getRepositoryId();
            // 标记是否存在对应的老数据
            boolean flag = false;
            for (RestApi oldRest : oldRestApiList) {
                if (transId.equals(oldRest.getTransId())) {
                    flag = true;
                    if (repo.getHeaderRepositoryId().equals(oldRest.getHeaderRepositoryId())) {
                        // 如果本次扫描的RepositoryID与数据库里面的不一致，则需要扫描xml文件
                        if (!repositoryId.equals(oldRest.getRepositoryId())) {
                            notNewIdList.add(oldRest.getId());
                            oldRest.setVer(oldRest.getVer() + 1);
                            oldRest.setId(null);
                            // 更新接口消息体
                            updateRestApi(jsonPath, oldRest, repositoryId);
                            if (!repo.getSchemaConfigRepositoryId().equals(oldRest.getSchemaConfigRepositoryId())) {
                                // 更新注册状态
                                updateRegister(repo, oldRest, registerList);
                            }
                            oldRest.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                            oldRest.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                            restApiList.add(oldRest);
                        } else {
                            if (!repo.getSchemaConfigRepositoryId().equals(oldRest.getSchemaConfigRepositoryId())) {
                                // 更新注册状态
                                updateRegisterList.add(updateRegister(repo, oldRest, registerList));
                            }
                        }
                        // 删除后，剩下此次扫描相对上次扫描删除的接口
                        oldRestApiList.remove(oldRest);
                        break;
                    } else {
                        // 如果本次扫描的HeaderRepositoryID与数据库里面的不一致，则需要更新消息头信息
                        notNewIdList.add(oldRest.getId());
                        oldRest.setId(null);
                        oldRest.setReqHeader(header.getReqHeader());
                        oldRest.setRspHeader(header.getRspHeader());
                        oldRest.setHeaderRepositoryId(repo.getHeaderRepositoryId());
                        oldRest.setVer(oldRest.getVer() + 1);
                        if (!repo.getSchemaConfigRepositoryId().equals(oldRest.getSchemaConfigRepositoryId())) {
                            // 更新注册状态
                            updateRegister(repo, oldRest, registerList);
                        }
                        if (!repositoryId.equals(oldRest.getRepositoryId())) {
                            // 更新接口消息体
                            updateRestApi(jsonPath, oldRest, repositoryId);
                        }
                        oldRest.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                        oldRest.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
                        restApiList.add(oldRest);
                        // 删除后，剩下此次扫描相对上次扫描删除的接口
                        oldRestApiList.remove(oldRest);
                        break;
                    }
                }
            }
            if (!flag && !jsonPath.contains(Constants.EXAMPLE_JSON)) {
                RestApi restApi = getRestApi(jsonPath, header, repo, registerList);
                restApiList.add(restApi);
            }
        }
        if (CollectionUtils.isNotEmpty(oldRestApiList)) {
            List<String> idList = oldRestApiList.stream().map(RestApi::getId).collect(Collectors.toList());
            notNewIdList.addAll(idList);
        }
        interfaceService.updateRestApiIsNewByIds(notNewIdList);
        interfaceService.updateRestApiRegister(updateRegisterList);
        return restApiList;
    }

    /**
     * 获取RestApi信息
     *
     * @param jsonPath
     * @param header
     * @param repo
     * @param registerList
     * @return
     */
    private RestApi getRestApi(String jsonPath, RestApi header, RestApi repo, List<Map<String, String>> registerList) {
        RestApi restApi = analysisJson(jsonPath);
        // 默认是最新版本的接口
        restApi.setIsNew(1);
        // 默认版本为0
        restApi.setVer(0);
        // 默认没有在mapping文件配置过
        restApi.setRegister(0);
        // 设置消息头
        restApi.setReqHeader(header.getReqHeader());
        restApi.setRspHeader(header.getRspHeader());
        String transId = repo.getTransId();
        restApi.setTransId(transId);
        // 判断是否在schema-config.xml文件中注册过
        for (Map<String, String> registerMap : registerList) {
            if (registerMap.containsKey(transId)) {
                restApi.setTransId(registerMap.get(transId));
                restApi.setRegister(1);
                break;
            }
        }
        restApi.setRepositoryId(repo.getRepositoryId());
        restApi.setHeaderRepositoryId(repo.getHeaderRepositoryId());
        restApi.setSchemaConfigRepositoryId(repo.getSchemaConfigRepositoryId());
        // 设置接口类型
        restApi.setInterfaceType(Dict.REST);
        restApi.setCreateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        restApi.setUpdateTime(TimeUtils.getFormat(TimeUtils.FORMAT_DATE_TIME));
        return restApi;
    }


    /**
     * 如果本次扫描的RepositoryId与数据库里面的不一致，则需要更新接口消息体和元数据
     *
     * @param jsonPath
     * @param oldRest
     * @param repositoryId
     */
    private void updateRestApi(String jsonPath, RestApi oldRest, String repositoryId) {
        // 解析json文件
        if (!jsonPath.contains(Constants.EXAMPLE_JSON)) {
            RestApi restApi = analysisJson(jsonPath);
            oldRest.setInterfaceName(restApi.getInterfaceName());
            oldRest.setUri(restApi.getUri());
            oldRest.setRequestType(restApi.getRequestType());
            oldRest.setRequestProtocol(restApi.getRequestProtocol());
            oldRest.setDescription(restApi.getDescription());
            oldRest.setRequest(restApi.getRequest());
            oldRest.setResponse(restApi.getResponse());
            oldRest.setRepositoryId(repositoryId);
        }
    }

    /**
     * 如果本次扫描的SchemaConfigRepositoryId与数据库里面的不一致，则需要更新注册状态
     *
     * @param repo
     * @param oldRest
     * @param registerList
     */
    private RestApi updateRegister(RestApi repo, RestApi oldRest, List<Map<String, String>> registerList) {
        oldRest.setSchemaConfigRepositoryId(repo.getSchemaConfigRepositoryId());
        oldRest.setRegister(0);
        // 判断是否在schema-config.xml文件中注册过
        for (Map<String, String> registerMap : registerList) {
            String oldTransId = oldRest.getTransId();
            if (registerMap.containsKey(oldTransId)) {
                oldRest.setTransId(registerMap.get(oldTransId));
                oldRest.setRegister(1);
                break;
            }
        }
        return oldRest;
    }

    /**
     * 解析RestHeader.json信息
     *
     * @param headerPath
     * @return
     */
    private RestApi getRestHeader(String headerPath) {
        RestApi restApi = new RestApi();
        try (FileReader fileReader = new FileReader(headerPath)) {
            // 获取RestHeader.json的内容
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(fileReader);
            JsonObject reqHeader = jsonObject.getAsJsonObject(Dict.REQHEADER);
            JsonObject rspHeader = jsonObject.getAsJsonObject(Dict.RSPHEADER);
            // 解析请求头参数列表
            restApi.setReqHeader(FileUtil.analysisParams(reqHeader, 3));
            // 解析响应头参数列表
            restApi.setRspHeader(FileUtil.analysisParams(rspHeader, 3));
        } catch (Exception e) {
            logger.error("{}", Constants.ANALYSIS_FILE_ERROR + headerPath + "，" + e.getMessage());
            throw new FdevException(ErrorConstants.REST_PROVIDER_SCAN_ERROR, new String[]{Constants.ANALYSIS_FILE_ERROR + headerPath + "！"});
        }
        return restApi;
    }

    /**
     * 解析schema-config.xml文件信息
     *
     * @param schemaConfigPath
     * @return
     */
    private List<Map<String, String>> getSchemaConfig(String schemaConfigPath) {
        List<Map<String, String>> registerList = new ArrayList<>();
        // 获取根元素
        Element root = FileUtil.getXmlRootElement(schemaConfigPath, ErrorConstants.REST_PROVIDER_SCAN_ERROR);
        List<Element> beanList = root.elements(Dict.BEAN);
        if (CollectionUtils.isEmpty(beanList)) {
            return registerList;
        }
        // 获取元素信息
        for (Element bean : beanList) {
            // 获取beanid 判断是不是jsonSchemaManager或者serverJsonSchemaManager
            String beanId = bean.attributeValue(Dict.ID);
            if (Dict.JSONSCHEMAMANAGER.equals(beanId) || Dict.SERVERJSONSCHEMAMANAGER.equals(beanId)) {
                List<Element> propertyList = bean.elements(Dict.PROPERTY);
                if (CollectionUtils.isEmpty(propertyList)) {
                    continue;
                }
                for (Element property : propertyList) {
                    // 获取property的孩子节点
                    Element mapElement = property.element(Dict.MAP);
                    if (mapElement == null) {
                        continue;
                    }
                    List<Element> entryList = mapElement.elements(Dict.ENTRY);
                    if (CollectionUtils.isEmpty(entryList)) {
                        continue;
                    }
                    // 获取entry的属性值
                    for (Element entry : entryList) {
                        String key = entry.attributeValue(Dict.KEY);
                        String value = entry.attributeValue(Dict.VALUE);
                        if (StringUtils.isEmpty(value)) {
                            continue;
                        }
                        String[] values = value.split("/");
                        String jsonName = values[values.length - 1].replaceAll(Constants.JSON_FILE, "");
                        Map<String, String> entryMap = new HashMap<>();
                        // map的key值为json文件名，value值为transId
                        entryMap.put(jsonName, key);
                        registerList.add(entryMap);
                    }
                }
            }
        }
        return registerList;
    }

    /**
     * 解析json文件
     *
     * @param jsonPath
     * @return
     */
    public RestApi analysisJson(String jsonPath) {
        JsonParser jsonParser = new JsonParser();
        RestApi restApi = new RestApi();
        try (FileReader fileReader = new FileReader(jsonPath)) {
            JsonObject jsonObject = (JsonObject) jsonParser.parse(fileReader);
            // 解析元数据
            JsonObject metadata = jsonObject.getAsJsonObject(Dict.METADATA);
            if (metadata == null) {
                throw new FdevException(ErrorConstants.REST_PROVIDER_SCAN_ERROR, new String[]{jsonPath + "，不存在Metadata！"});
            }
            String interfaceName = metadata.get(Dict.INTERFACENAME).getAsString();
            String serviceId = metadata.get(Dict.SERVICEID).getAsString();
            if (!super.getAppServiceId().equalsIgnoreCase(serviceId)) {
                throw new FdevException(ErrorConstants.REST_PROVIDER_SCAN_ERROR, new String[]{jsonPath + "，ServiceId不一致！"});
            }
            restApi.setServiceId(super.getAppServiceId());
            restApi.setBranch(super.getBranchName());
            restApi.setUri(metadata.get(Dict.URI).getAsString());
            restApi.setRequestType(metadata.get(Dict.REQUESTTYPE).getAsString());
            restApi.setRequestProtocol(metadata.get(Dict.REQUESTPROTOCOL).getAsString());
            restApi.setDescription(metadata.get(Dict.INTERFACEDESCRIPTION).getAsString());
            restApi.setInterfaceName(interfaceName);

            // 解析请求体
            JsonObject request = jsonObject.getAsJsonObject(Dict.REQUEST);
            if (request == null) {
                throw new FdevException(ErrorConstants.REST_PROVIDER_SCAN_ERROR, new String[]{jsonPath + "，不存在Request！"});
            }
            String requestSchemaValue = request.get("$schema").getAsString();
            JsonObject requestProperties = request.getAsJsonObject(Dict.PROPERTIES);
            if (requestProperties != null) {
                JsonObject reqBody = requestProperties.getAsJsonObject(Dict.REQBODY);
                if (requestSchemaValue.contains("draft-04")) {
                    restApi.setRequest(FileUtil.analysisParams(reqBody, 4));
                } else {
                    restApi.setRequest(FileUtil.analysisParams(reqBody, 3));
                }
            } else {
                restApi.setRequest(Lists.newArrayList());
            }

            // 解析响应体
            JsonObject response = jsonObject.getAsJsonObject(Dict.RESPONSE);
            if (response == null) {
                throw new FdevException(ErrorConstants.REST_PROVIDER_SCAN_ERROR, new String[]{jsonPath + "，不存在Response！"});
            }
            String responseSchemaValue = response.get("$schema").getAsString();
            JsonObject responseProperties = response.getAsJsonObject(Dict.PROPERTIES);
            if (responseProperties != null) {
                JsonObject rspBody = responseProperties.getAsJsonObject(Dict.RSPBODY);
                if (responseSchemaValue.contains("draft-04")) {
                    restApi.setResponse(FileUtil.analysisParams(rspBody, 4));
                } else {
                    restApi.setResponse(FileUtil.analysisParams(rspBody, 3));
                }
            } else {
                restApi.setResponse(Lists.newArrayList());
            }
        } catch (FdevException e) {
            throw new FdevException(ErrorConstants.REST_PROVIDER_SCAN_ERROR, new String[]{errorMessageUtil.get(e)});
        } catch (Exception e) {
            logger.error("{}", Constants.ANALYSIS_FILE_ERROR + jsonPath + "，" + e.getMessage());
            throw new FdevException(ErrorConstants.REST_PROVIDER_SCAN_ERROR, new String[]{Constants.ANALYSIS_FILE_ERROR + jsonPath + "！"});
        }
        return restApi;
    }


    /**
     * 将没有在schema-config.xml中注册的接口交易码返给前端
     *
     * @return
     */
    private String returnMsg() {
        StringBuilder msg = new StringBuilder();
        msg.append(Constants.REST_PROVIDER_SCAN_SUCCESS);
        List<String> notRegisterList = interfaceService.getNotRegister(super.getAppServiceId(), super.getBranchName());
        if (CollectionUtils.isNotEmpty(notRegisterList)) {
            msg.append("：没有在schema-config.xml文件中注册的接口有");
            msg.append(notRegisterList);
        }
        msg.append("！");
        return msg.toString();
    }

}
