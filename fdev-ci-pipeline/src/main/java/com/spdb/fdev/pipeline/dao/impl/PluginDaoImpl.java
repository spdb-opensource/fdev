package com.spdb.fdev.pipeline.dao.impl;

import com.alibaba.fastjson.JSON;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.common.User;
import com.spdb.fdev.pipeline.dao.IPluginDao;
import com.spdb.fdev.pipeline.entity.*;
import com.spdb.fdev.pipeline.service.IFileService;
import okhttp3.*;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;

@Repository
public class PluginDaoImpl implements IPluginDao {
    @Resource
    private MongoTemplate mongoTemplate;
    @Value("${mioBuket}")
    private String mioBuket;
    @Autowired
    IFileService fileService;

    @Override
    public Map queryPlugin(List<String> regex, Map param, User user) throws Exception {
        Query query = new Query(Criteria.where(Dict.STATUS).is(Constants.STATUS_OPEN));
       //列表查询条件
        Criteria resultC = new Criteria();
        if (!CommonUtils.isNullOrEmpty(param)) {
            String pluginType = (String) param.get(Dict.PLUGINTYPE);//查询类型：0-全部1-我的
            List<String> platform = (List<String>) param.get(Dict.PLATFORM);  //运行平台信息
            Criteria listConditions = new Criteria();
            if (!CommonUtils.isNullOrEmpty(platform)){
                listConditions.and(Dict.PLATFORM).in(platform);
            }
            if(Constants.pluginType_1.equals(pluginType)){ //我的插件列表
                listConditions.and(Dict.AUTHOR__ID).is(user.getId());
                listConditions.and(Dict.AUTHOR_NAMECN).is(user.getUser_name_cn());
            }
            if(Constants.pluginType_0.equals(pluginType)){ //所有我可见插件列表(作者是我 或者 作者不是我但是是公开的插件)
                if(!user.getRole_id().contains(Constants.ENVIRONMENT_ADMIN_ID)){ //环境配置管理员可见真正的全部插件
                    Criteria andC = new Criteria();
                    andC.and(Dict.AUTHOR__ID).is(user.getId()).and(Dict.AUTHOR_NAMECN).is(user.getUser_name_cn());
                    Criteria orC = new Criteria();
                    orC.and(Dict.AUTHOR__ID).ne(user.getId()).and(Dict.OPEN).is(true);
                    listConditions.orOperator(andC,orC);
                }
            }

            resultC.andOperator(listConditions);
        }

        //模糊查条件：根据插件名称，和作者模糊查询，可同时输入多个模糊查询数据
        Criteria resultand2 = new Criteria();
        Criteria[] resultArr = new Criteria[regex.size()];
        if (!CommonUtils.isNullOrEmpty(regex)) {
            for (int i = 0; i < regex.size(); i++) {
                Criteria resultC2 = new Criteria();
                Criteria publicC = new Criteria();
                publicC.and(Dict.PLUGIN_NAME).regex(regex.get(i), "i");
                Criteria publicNameCn = new Criteria();
                publicNameCn.and(Dict.AUTHOR_NAMECN).regex(regex.get(i), "i");
                Criteria publicNameEn = new Criteria();
                publicNameEn.and(Dict.AUTHOR_NAMEEN).regex(regex.get(i), "i");
                resultC2.orOperator(publicC,publicNameCn,publicNameEn);
                resultArr[i] = resultC2;
            }
            resultand2.orOperator(resultArr);
        }
        query.addCriteria(new Criteria().andOperator(resultC,resultand2));

        Map result = new HashMap();
//        int count = (int) mongoTemplate.count(query, Plugin.class);
//        result.put(Dict.TOTAL, count);
//        if (pageSize != 0) {
//            query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME)).with(PageRequest.of(page - 1, pageSize));
//        }
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATE_TIME));
        //查询插件列表要去除script、params、artifacts
//        query.fields().exclude(Dict.SCRIPT).exclude("params").exclude(Dict.ARTIFACTS).exclude(Dict.RELEASE_LOG);
        result.put(Dict.LIST, mongoTemplate.find(query, Plugin.class, Dict.PLUGIN));
        return result;
    }

    @Override
    public void savePlugin(Plugin plugin) throws Exception {
         mongoTemplate.save(plugin);
    }

    @Override
    public void delPlugin(String pluginCode) throws Exception {
        Query query = new Query(Criteria.where(Dict.PLUGINCODE).is(pluginCode));
        Plugin plugin = mongoTemplate.findOne(query,Plugin.class);
        if(!CommonUtils.isNullOrEmpty(plugin)){
            query.addCriteria(Criteria.where(Dict.AUTHOR__ID).is(plugin.getAuthor().getId()));
            query.addCriteria(Criteria.where(Dict.AUTHOR_NAMECN).is(plugin.getAuthor().getNameEn()));
        }
        mongoTemplate.findAndRemove(query, Plugin.class);
    }

    @Override
    public List<Plugin> queryPluginHistory(String nameId) throws Exception {
        Query query = new Query(Criteria.where(Dict.NAMEID).is(nameId));
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATETIME));
        List<Plugin> pluginList = mongoTemplate.find(query, Plugin.class);
        return pluginList;
    }

    /*@Override
    public Plugin queryPluginDetail(String pluginCode) throws Exception {
        Query query = new Query(Criteria.where(Dict.PLUGINCODE).is(pluginCode));
        return mongoTemplate.findOne(query, Plugin.class);
    }*/

    @Override
    public Plugin queryPluginDetail(String pluginCode) throws Exception {
        Query query = new Query(Criteria.where(Dict.PLUGINCODE).is(pluginCode));
        Plugin plugin = mongoTemplate.findOne(query, Plugin.class);
        //插件中存量的实体模版参数
        if (!CommonUtils.isNullOrEmpty(plugin) && !CommonUtils.isNullOrEmpty(plugin.getEntityTemplateList())){
            List<Map> entityTemplateList = plugin.getEntityTemplateList();
            for(Map map : entityTemplateList){
                Map entityTemplateParam = new HashMap();
                entityTemplateParam.put(Dict.KEY,map.get(Dict.NAMEEN));
                entityTemplateParam.put(Dict.LABEL,map.get(Dict.NAMECN));
                entityTemplateParam.put(Dict.TYPE,Dict.ENTITYTYPE);
                List entityList = new ArrayList();
                entityList.add(map);
                entityTemplateParam.put(Dict.ENTITYTEMPLATEPARAMS,entityList);
                entityTemplateParam.put(Dict.REQUIRED,true);
                plugin.getParams().add(entityTemplateParam);
            }
        }
        /*//插件中存量的脚本参数
        if (!CommonUtils.isNullOrEmpty(plugin) && !CommonUtils.isNullOrEmpty(plugin.getScript())){
            Map script = plugin.getScript();
            if (!CommonUtils.isNullOrEmpty(script.get(Dict.MINIO_OBJECT_NAME))){
                String mioPath = (String)script.get(Dict.MINIO_OBJECT_NAME);
                String scriptCmd  = fileService.downloadDocumentFile(mioBuket, mioPath);
                Map scriptParam = new HashMap();
                scriptParam.put(Dict.KEY,Dict.SCRIPT);
                scriptParam.put(Dict.DEFAULT,scriptCmd);
                scriptParam.put(Dict.LABEL,Constants.SCRIPT_LABEL);
                scriptParam.put(Dict.TYPE,Dict.SCRIPT);
                scriptParam.put(Dict.FILETEMPLATEMINIOURL,mioPath);
                scriptParam.put(Dict.REQUIRED,true);
                scriptParam.put(Dict.HIDDEN,false);
                plugin.getParams().add(scriptParam);
            }
        }*/
        perparePlubinParam(plugin);
        return plugin;
    }
    private void perparePlubinParam(Plugin plugin) {
        plugin.getParams().forEach(params -> {
            /*处理http-select选项*/
            if (Objects.equals(params.get("type"), "http-select")) {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url((String) params.get("url"))
                        .headers(Headers.of((Map<String, String>) params.getOrDefault("requestHeader", new HashMap<>())))
                        .method(
                                (String) params.getOrDefault("method", "GET"),
                                RequestBody.create(
                                        MediaType.parse("application/json; charset=utf-8"),
                                        JSON.toJSONString(params.getOrDefault("requestBody", new HashMap<>()))
                                )
                        ).build();
                try {
                    List<Map> itemValue = (List<Map>) params.getOrDefault("itemValue", new ArrayList<Map>());
                    Response response = okHttpClient.newCall(request).execute();
                    Map map = JSON.parseObject(response.body().string(), Map.class);
                    Object root = map;
                    if(params.containsKey("mappingRoot")){
                        String s = (String) params.get("mappingRoot");
                        String[] rootPaths = s.split("\\.");
                        for (String rootPath : rootPaths) {
                            root = ((Map) root).get(rootPath);
                        }
                    }
                    if(!(root instanceof List)){
                        throw new RuntimeException("http-select mappingRoot不是列表");
                    }
                    if(!params.containsKey("mapping")){
                        params.put("itemValue",root);
                        return;
                    }
                    Map<String, String> mapping = (Map<String, String>) params.get("mapping");
                    for (Map rootElem : ((List<Map>) root)) {
                        HashMap<String, String> selection = new HashMap<>();
                        for (Map.Entry<String, String> entry : mapping.entrySet()) {
                            String[] mapPaths = entry.getValue().split("\\.");
                            Object mappedValue = rootElem;
                            for (String mapPath : mapPaths) {
                                mappedValue = ((Map) mappedValue).get(mapPath);
                            }
                            selection.put(entry.getKey(), (String) mappedValue);
                        }
                        itemValue.add(selection);
                    }
                    params.put("itemValue",itemValue);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    @Override
    public Plugin queryPluginDetailById(String id) throws Exception {
        Query query = new Query(Criteria.where(Dict.PLUGINCODE).is(id));
        return mongoTemplate.findOne(query, Plugin.class);
    }

    @Override
    public List<Plugin> queryPluginListByNameId(String nameId) throws Exception {
        Query query = new Query(Criteria.where(Dict.NAMEID).is(nameId));
        return mongoTemplate.find(query, Plugin.class);
    }

    @Override
    public List<Plugin> queryPluginDetailByNameId(String nameId) throws Exception {
        Criteria criteria = Criteria.where(Dict.NAMEID).is(nameId);
        criteria.and(Dict.STATUS).is(Constants.ONE);
        Query query = new Query(criteria);
        query.with(new Sort(Sort.Direction.DESC, Dict.CREATETIME));
        return mongoTemplate.find(query, Plugin.class);
    }

    @Override
    public void setStatusClose(String id,User user){
        Query query = new Query(Criteria.where(Dict.PLUGINCODE).is(id));
        query.addCriteria(Criteria.where(Dict.AUTHOR__ID).is(user.getId()));
        query.addCriteria(Criteria.where(Dict.AUTHOR_NAMECN).is(user.getUser_name_cn()));
        Update update = Update.update(Dict.STATUS, Constants.STATUS_CLOSE);
        mongoTemplate.updateFirst(query,update,Plugin.class);
    }

    @Override
    public List<Plugin> haveDuplicateNameFile() throws Exception{
        Query query = new Query();
        List<Plugin> pluginList = mongoTemplate.find(query,Plugin.class);
        return pluginList;
    }

    @Override
    public List<Plugin> queryByIdList(List<String> ids) {
        Query query = new Query(Criteria.where((Dict.PLUGINCODE)).in(ids));
        return mongoTemplate.find(query, Plugin.class);
    }

    @Override
    public List<PluginUseCount> statisticsPluginUseCount(String pipelineId) throws Exception {
        List<PluginUseCount> pluginUseCountList = new ArrayList<>();
        Criteria criteria = new Criteria();
        //pipelineId不为空则根据pipelineId查，否则根据status查全部
        if (!CommonUtils.isNullOrEmpty(pipelineId)) {
            criteria.and(Dict.PIPELINEID).is(pipelineId);
        } else {
            criteria.and(Dict.STATUS).is(Constants.ONE);
            this.removeAllPluginUseCount();
        }
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.unwind(Dict.STAGES),
                Aggregation.unwind(Dict.STAGESJOBS),
                Aggregation.unwind(Dict.STAGESJOBSSTEPS),
                Aggregation.group(Fields.fields().and(Dict.PLUGINCODE,Dict.STAGESJOBSSTEPSPLUGININFOPLUGINCODEVALUE).and(Dict.PROJECTID,Dict.BINDPROJECTPROJECTIDVALUE)).count().as(Dict.USECOUNT)
        );
        AggregationResults<Map> docs = mongoTemplate.aggregate(aggregation, Dict.PIPELINE, Map.class);
        Iterator<Map> iterator = docs.iterator();
        Map<String,String> nameIdMap = new HashMap<>();  //用于存放查询plugin的NameId，减少访问数据库次数
        while (iterator.hasNext()) {
            Map next = iterator.next();
            PluginUseCount pluginUseCount = new PluginUseCount();
            String pluginCode = (String) next.get(Dict.PLUGINCODE);
            String nameId = nameIdMap.get(pluginCode);
            if (CommonUtils.isNullOrEmpty(nameId)) {
                nameId = this.queryPluginDetail(pluginCode).getNameId();
                nameIdMap.put(pluginCode,nameId);
            }
            pluginUseCount.setPluginCode(pluginCode);
            pluginUseCount.setNameId(nameId);

            BindProject bindProject = new BindProject();
            bindProject.setProjectId((String) next.get(Dict.PROJECTID));
            pluginUseCount.setBindProject(bindProject);

            pluginUseCount.setUseCount(next.get(Dict.USECOUNT).toString());

            pluginUseCountList.add(pluginUseCount);
        }
        return pluginUseCountList;
    }

    @Override
    public void savePluginUseCount(PluginUseCount pluginUseCount) throws Exception {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(Dict.PLUGINCODE).is(pluginUseCount.getPluginCode());
        criteria.and(Dict.BINDPROJECT_PROJECTID).is(pluginUseCount.getBindProject().getProjectId());
        query.addCriteria(criteria);
        Update update = new Update();
        update.set(Dict.NAMEID,pluginUseCount.getNameId());
        update.set(Dict.BINDPROJECT,pluginUseCount.getBindProject());
        update.set(Dict.USECOUNT,pluginUseCount.getUseCount());
        mongoTemplate.upsert(query,update,PluginUseCount.class);
    }

    @Override
    public PluginUseCount queryPluginUseCountByPluginCodeAndBindProjectId(String pluginCode, String bindProjectId) throws Exception {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(Dict.PLUGINCODE).is(pluginCode);
        criteria.and(Dict.BINDPROJECT_PROJECTID).is(bindProjectId);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query,PluginUseCount.class);
    }

    @Override
    public void removeAllPluginUseCount() throws Exception {
        Query query = Query.query(Criteria.where(Dict.USECOUNT).ne(Constants.ZERO));
        Update update = Update.update(Dict.USECOUNT,Constants.ZERO);
        mongoTemplate.updateMulti(query, update,PluginUseCount.class);
    }

    @Override
    public void upsertPluginEvaluate(PluginEvaluate pluginEvaluate) {
        Author author = pluginEvaluate.getAuthor();
        String userId = author.getId();
        String nameId = pluginEvaluate.getNameId();
        Query query = new Query(Criteria.where(Dict.AUTHORID).is(userId).and(Dict.NAMEID).is(nameId));
        Update update = new Update();
        update.set(Dict.PLUGINEVALUATEID, pluginEvaluate.getPluginEvaluateId());
        update.set(Dict.NAMEID, pluginEvaluate.getNameId());
        update.set(Dict.OPERATIONDIFFICULTY, pluginEvaluate.getOperationDifficulty());
        update.set(Dict.ROBUSTNESS, pluginEvaluate.getRobustness());
        update.set(Dict.LOGCLARITY, pluginEvaluate.getLogClarity());
        update.set(Dict.AVERAGE, pluginEvaluate.getAverage());
        if (!CommonUtils.isNullOrEmpty(pluginEvaluate.getDesc())) {
            update.set(Dict.DESC, pluginEvaluate.getDesc());
        }
        update.set(Dict.AUTHOR, author);
        mongoTemplate.upsert(query, update, PluginEvaluate.class);
    }

    @Override
    public PluginEvaluate queryPluginEvaluate(String userId, String nameId) {
        Query query = new Query(Criteria.where(Dict.AUTHORID).is(userId).and(Dict.NAMEID).is(nameId));
        //在查询的时候排除author属性集合
        query.fields().exclude(Dict.AUTHOR);
        PluginEvaluate pluginEvaluate=mongoTemplate.findOne(query, PluginEvaluate.class);
        return pluginEvaluate;
    }

    @Override
    public Double queryPluginEvaluateByNameId(String nameId) {
        //根据插件唯一标识nameId来进行分组查询，求插件的平均分
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where(Dict.NAMEID).is(nameId)),
                Aggregation.group(Dict.NAMEID).avg(Dict.AVERAGE).as(Dict.AVERAGE),
                Aggregation.project(Dict.AVERAGE)
                        .andExclude(Dict._ID)
        );
        Map result = mongoTemplate.aggregate(aggregation, Dict.PLUGINEVALUATE, Map.class).getUniqueMappedResult();
        Double average = 0.0;
        if (!CommonUtils.isNullOrEmpty(result)) {
            //结果保留一位小数
            average = Double.parseDouble(String.format("%.1f", result.get(Dict.AVERAGE)));

        }
        return average;
    }

}
