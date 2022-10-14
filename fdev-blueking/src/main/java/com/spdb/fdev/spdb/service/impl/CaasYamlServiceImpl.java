package com.spdb.fdev.spdb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.gson.Gson;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.util.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.spdb.dao.impl.CaasDeploymentDaoImpl;
import com.spdb.fdev.spdb.entity.CaasDeployment;
import com.spdb.fdev.spdb.service.ICaasYamlService;
import com.spdb.fdev.spdb.yamlEntity.Container;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @Author:guanz2
 * @Date:2021/10/2-22:02
 * @Description:
 */
@Component
public class CaasYamlServiceImpl implements ICaasYamlService {

    @Autowired
    private CaasDeploymentDaoImpl caasDeploymentDao;

    @Autowired
    private CaasModifyKeyServiceImpl caasModifyKeyService;

    @Value("${caas_old_yaml_path}")
    private String path;

    @Override
    public Map<String, Object> getCaasOldYaml(String deploy_name) throws Exception {
        String deploymentName = deploy_name;
        Map resultMap = new HashMap();

        List<CaasDeployment> deployments = caasDeploymentDao.queryCaasDeploymentCondition(deploymentName);

        if (CommonUtils.isNullOrEmpty(deployments)) {
            throw new FdevException(ErrorConstants.DEPLOYMENT_NULL);
        }

        for (CaasDeployment deployment : deployments) {
            Map<String, Object> deploymentMap = JSON.parseObject(JSON.toJSONString(deployment, SerializerFeature.WriteMapNullValue, SerializerFeature.QuoteFieldNames), Map.class);

            //1.判断每个字段是否为空含义，是就让它的值为null
            for(String key : deploymentMap.keySet()){
                Object o = deploymentMap.get(key);
                if(CommonUtils.isNullOrEmpty(o) || o.equals("[null]")){
                    deploymentMap.put(key, null);
                }
            }
            //2.去中括号
            Map tempData = this.cutMidCurly(deploymentMap);
            //3.去双引号
            Map data = this.cutDoubleQuatation(tempData);
            //4.生成yaml
            Map resObjMap = this.generateYaml(data, deploymentName);

            //判断gray biz
            String namespace = (String) data.get("namespace");
            String cluster = (String) data.get("cluster");
            if (!CommonUtils.isNullOrEmpty(namespace)) {
                if (namespace.length() >= 2) {
                    if ("sh".equals(namespace.substring(0, 2))||"hf".equals(namespace.substring(0, 2))) {
                        cluster = cluster + "-biz";
                    }
                    if (namespace.contains("gray")||namespace.contains("grey")) {
                        cluster = cluster + "-gray";
                    }
                }
            }
            this.generageFile(this.path, cluster, deploymentName, resObjMap);
            resultMap.put(cluster, resObjMap);

        }
        return  resultMap;
    }

    @Override
    public List<Map> getCaasNewYaml(String deploy_name, String tag) throws Exception {

        //读取entity.properties里的配置，修改字段
        ClassPathResource classPathResource = new ClassPathResource("entity.properties");
        Properties properties = new Properties();
        InputStream in = classPathResource.getInputStream();
        properties.load(in);

        //读取请求的参数
        String deploymentName = deploy_name;
        String imagVersion = tag;

        List<Map> resultList = new ArrayList<>();
        List<CaasDeployment> deployments = caasDeploymentDao.queryCaasDeploymentCondition(deploymentName);

        if (CommonUtils.isNullOrEmpty(deployments)) {
            throw new FdevException(ErrorConstants.DEPLOYMENT_NULL);
        }

        for (CaasDeployment deployment : deployments) {

            Map<String, Object> deploymentMap = JSON.parseObject(JSON.toJSONString(deployment, SerializerFeature.WriteMapNullValue, SerializerFeature.QuoteFieldNames), Map.class);

            //1.判断每个字段是否为空含义，是就让它的值为null
            for(String key : deploymentMap.keySet()){
                Object o = deploymentMap.get(key);
                if(CommonUtils.isNullOrEmpty(o) || o.equals("[null]")){
                    deploymentMap.put(key, null);
                }
            }

            //2.获取modifyKey
            Map<String, Object> modifyKeyMap1 = new HashMap<>();
            Map changeMap = new HashMap();
            Map resultMap = new HashMap();
            String namespace = (String) deploymentMap.get("namespace");
            String cluster = (String) deploymentMap.get("cluster");

            if (!CommonUtils.isNullOrEmpty(caasModifyKeyService.queryCommonModifyKeys())) {
                modifyKeyMap1.putAll(caasModifyKeyService.queryCommonModifyKeys());
            }
            if (!CommonUtils.isNullOrEmpty(caasModifyKeyService.queryPersonalModifyKeys(deploymentName, cluster, namespace))) {
                modifyKeyMap1.putAll(caasModifyKeyService.queryPersonalModifyKeys(deploymentName, cluster, namespace));
            }

            //3.对data和modifyKey都去中括号
            Map<String, Object> tempData2 = this.cutMidCurly(deploymentMap);
            Map<String, Object> modifyKeyMap2 = this.cutMidCurly(modifyKeyMap1);

            //4.去data和modifyKey都去双引号
            Map<String, Object> data = this.cutDoubleQuatation(tempData2);
            Map<String, Object> modifyKeyMap = this.cutDoubleQuatation(modifyKeyMap2);

            //5.根据modifyKey里的字段对应修改 caas的deployment数据
            for (String key : modifyKeyMap.keySet()) {
                if(key.equals("hostalias")){
                    //对hostalias做特别处理
                    if(!CommonUtils.isNullOrEmpty(data.get(key))){
                        changeMap.put(key, modifyKeyMap.get(key));
                    }
                    data.put(key, modifyKeyMap.get(key));
                } else if(!modifyKeyMap.get(key).equals(data.get(key))){
                    data.put(key, modifyKeyMap.get(key));
                    changeMap.put(key, modifyKeyMap.get(key));
                }else{
                    continue;
                }
            }

            //6.对replicas做处理
            if ("0".equals(data.get("replicas"))) {
                data.put("replicas", "1");
                changeMap.put("replicas", "1");
            }
            //7.对imageVersion处理 对版本单独判断是否相等,imagVersion
            String TAG = (String) data.get("tag");
            String[] tagList = TAG.split(":");
            if(tagList[tagList.length-1].hashCode() != imagVersion.hashCode()){
                tagList[tagList.length-1]=imagVersion;
                changeMap.put("tag", imagVersion);
            }
            String resultTag="";
            for (int i = 0; i < tagList.length; i++) {
                resultTag=resultTag+":"+tagList[i];
            }
            data.put("tag",resultTag.substring(1, resultTag.length()));

            // 8. 生成新yaml
            Map resObjMap = this.generateYaml(data, deploymentName);


            Map<String, Object> change = new HashMap<>();
            for (String key : (Set<String>)changeMap.keySet()) {
                if (!CommonUtils.isNullOrEmpty(properties.get(key))) {
                    change.put((String) properties.get(key), 0);
                }
            }
            resultMap.put("cluster", cluster);
            resultMap.put("change", change);
            resultMap.put("namespace", namespace);
            resultMap.put("deployment", deploymentName);
            resultMap.put("yaml", resObjMap);
            resultList.add(resultMap);
        }

        return resultList;
    }

    /**
     * 输入一个生成yaml需要的jsonMap数据
     * 生成一个标准的json格式Map数据
     */
    protected Map generateYaml(Map data, String deploymentName){
        Map<String, Object> targetMap = new LinkedHashMap<>();
        targetMap.put("kind","Deployment");
        targetMap.put("apiVersion","apps/v1");
        Map labels = new LinkedHashMap();
        labels.put("app", deploymentName);
        labels.put("dce.daocloud.io/app", deploymentName);
        String namespace = (String) data.get("namespace");

        //metadata
        Map<String, Object> metadata = new LinkedHashMap();

        metadata.put("labels", labels);
        metadata.put("name", deploymentName);
        metadata.put("namespace", namespace);
        targetMap.put("metadata",metadata);

        //spec-->replicas
        Map<String, Object> spec = new LinkedHashMap();
        if(data.get("replicas") != null){
            spec.put("replicas", Integer.valueOf((String) data.get("replicas")));
        }

        //spec--->selector
        Map selector = new LinkedHashMap();
        Gson gson1 = new Gson();
        LinkedHashMap tempLabels = gson1.fromJson(gson1.toJson(labels), LinkedHashMap.class);
        selector.put("matchLabels", tempLabels);
        spec.put("selector", selector);

        //spec--->strategy
        if(data.get("strategytype") != null){
            Map strategy = JSONObject.parseObject((String) data.get("strategytype"), LinkedHashMap.class);
            spec.put("strategy", strategy);
        }

        //spec--->template
        Map<String, Object> template = new LinkedHashMap();

        //template-->metadata
        Gson gson = new Gson();
        LinkedHashMap<String, Object> metaDataMap = gson.fromJson(gson.toJson(metadata),LinkedHashMap.class);
        metaDataMap.remove("namespace");
        template.put("metadata", metaDataMap);

        //template--->spec
        Map<String, Object> template_spec = new LinkedHashMap();

        //template--->dnsPolicy
        if(data.get("dnspolicy") != null){
            String dnsPolicy = (String) data.get("dnspolicy");
            template_spec.put("dnsPolicy", dnsPolicy);
        }

        //template--->dnsConfig
        if(data.get("dnsconfig") != null){
            Map<String, Object> dnsConfig = new LinkedHashMap<>();
            dnsConfig.put("nameservers", JSONObject.parseObject((String)data.get("dnsconfig"), List.class));
            template_spec.put("dnsConfig", dnsConfig);
        }

        //template--->spec--->affinity
        if(data.get("nodeselectorterms") != null && data.get("area") != null){
            Map<String, Object> affinity = new LinkedHashMap();
            Map<String, Object> nodeAffinity = new LinkedHashMap();
            Map<String, Object> requiredDuringSchedulingIgnoredDuringExecution = new LinkedHashMap();
            List nodeSelectorTerms = new ArrayList();
            Map nodeSelector = new LinkedHashMap();
            List matchExpressions = new ArrayList();
            Map<String, Object> expressions = new LinkedHashMap<>();
            expressions.put("key", data.get("nodeselectorterms"));
            expressions.put("operator","In");
            expressions.put("values", JSON.parseObject((String)data.get("area"), List.class));
            matchExpressions.add(expressions);
            nodeSelector.put("matchExpressions", matchExpressions);
            nodeSelectorTerms.add(nodeSelector);
            requiredDuringSchedulingIgnoredDuringExecution.put("nodeSelectorTerms", nodeSelectorTerms);
            nodeAffinity.put("requiredDuringSchedulingIgnoredDuringExecution", requiredDuringSchedulingIgnoredDuringExecution);
            affinity.put("nodeAffinity",nodeAffinity);
            template_spec.put("affinity", affinity);
        }

        //todo template-->spec--->initContainers
        if(data.get("initContainers") != null){
            template_spec.put("initContainers",  JSONObject.parseObject((String) data.get("initContainers"), List.class, Feature.OrderedField));
        }

        //template-->spec--->containers
        List containers = new ArrayList();
        Container container = new Container();
        container.setName(deploymentName);
        //envFrom
        container.setEnvFrom(JSONObject.parseObject((String) data.get("envfrom"), List.class));
        //env
        container.setEnv(JSONObject.parseObject((String) data.get("env"), List.class));
        //ports
        container.setPorts(JSONObject.parseObject((String) data.get("postserver"), List.class));
        //image
        container.setImage((String) data.get("tag"));
        //拉取策略固定死的
        container.setImagePullPolicy("IfNotPresent");
        //prestop
        if("\"\"".equals(data.get("prestop"))){
            Map prestop = new LinkedHashMap();
            prestop.put("preStop", "\"\"");
            container.setLifecycle(prestop);
        }else{
            Map temp = JSONObject.parseObject((String) data.get("prestop"), LinkedHashMap.class);
            container.setLifecycle(JSONObject.parseObject((String) data.get("prestop"), LinkedHashMap.class));
        }
        //ports
        container.setPorts(JSONObject.parseObject((String) data.get("portserver"), List.class));
        container.setResources((String) data.get("cpu_limits"), (String) data.get("memory_limits"),
                (String) data.get("cpu_requests"), (String) data.get("memory_requests"));
        container.setVolumeMounts(JSONObject.parseObject((String) data.get("volumemounts"), List.class));
        //todo
        container.setReadinessProbe(JSONObject.parseObject((String) data.get("readinessprobe"), LinkedHashMap.class));
        containers.add(JSONObject.parseObject(JSON.toJSONString(container), LinkedHashMap.class, Feature.OrderedField));
        template_spec.put("containers",containers);

        //template--->spec--->volumes
        if(data.get("volumes") != null){
            template_spec.put("volumes", complexString2Map((String) data.get("volumes"), "volumes"));
        }

        //template ---> spec -->hostAliases
        //hostAliases的处理比较特殊，单独处理
        if(CommonUtils.isNullOrEmpty(data.get("hostalias")) || "DELETE".equals(data.get("hostalias"))){
            template_spec.put("hostAliases", "######");
        }else{
            template_spec.put("hostAliases", complexString2Map((String) data.get("hostalias"),"hostalias"));
        }

        //imagePullSecrets
        if(data.get("imagepullsecrets") != null){
            template_spec.put("imagePullSecrets",complexString2Map((String) data.get("imagepullsecrets"), "imagepullsecrets"));
        }
        template.put("spec",template_spec);
        spec.put("template", template);
        targetMap.put("spec",spec);
        return targetMap;
    }

    //生成文件
    private void generageFile(String path, String cluster, String deployment, Map data){
        String midPath = cluster;
        String targetPath = path+"/"+deployment+"/"+midPath+"/"+deployment+".yaml";
        for(int i = 0; i < targetPath.length() ; i++){
            if(targetPath.charAt(i) == '/'){
                File temp = new File(targetPath.substring(0, i));
                if(!temp.exists()){
                    temp.mkdir();
                }
            }
        }
        File file = new File(targetPath);

        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(dumperOptions);
        try (FileWriter writer = new FileWriter(file);){
            yaml.dump(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //去中括号
    protected Map cutMidCurly(Map data){
        /**
         * volumemounts,containerport,tag,area,prestop,portserver,envfrom
         * cpu_limits,memory_requests,cpu_requests,memory_limits
         */
        ArrayList<String> fields = new ArrayList<String>(){{
            add("volumemounts");add("containerport");add("tag");
            add("prestop");add("portserver");add("envfrom");
            add("env");add("cpu_limits"); add("memory_requests");
            add("cpu_requests");add("memory_limits");add("readinessprobe");
        }};
        for(Object key : data.keySet()){
            if(fields.contains(key)){
                if(!CommonUtils.isNullOrEmpty(data.get(key))){
                    String temp = (String) data.get(key);
                    if("[null]".equals(temp)){
                        data.put(key,null);
                    }else{
                        String tempString = temp.substring(1,temp.length()-1);
                        data.put(key,tempString);
                    }
                    continue;
                }
            }
        }
        return  data;
    }

    //去掉双引号“”,
    protected Map cutDoubleQuatation(Map data){
        for(Object key : data.keySet()){
            if(!CommonUtils.isNullOrEmpty(data.get(key)) && ("java.lang.String".equals(data.get(key).getClass().getName()))){
                String temp = (String) data.get(key);
                if(("\"".charAt(0) == temp.charAt(0)) && ("\"".charAt(0) == temp.charAt(temp.length()-1))){
                    temp = temp.substring(1,temp.length()-1);
                }
                data.put(key,temp);
            }
        }
        return  data;
    }

    //复杂json字符串转map，之限外层是List，内层只包含一个JSONObject的对象，hostAliases，imagePullSecret，volumes
    protected List complexString2Map(String str ,String type){
        if(!CommonUtils.isNullOrEmpty(str)){
            List listObject = JSONObject.parseObject(str,List.class);
            List resObjList = new ArrayList();
            for (Object item : listObject){
                Map<String, Object> temp = new LinkedHashMap();
                temp = JSONObject.parseObject((String) item, LinkedHashMap.class);
                resObjList.add(temp);
            }
            return resObjList;
        }else{
            return  null;
        }
    }
}
