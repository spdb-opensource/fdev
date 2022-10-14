package com.spdb.fdev.spdb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.util.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.spdb.dao.ICaasDeploymentDao;
import com.spdb.fdev.spdb.dao.ISccDeploymentDao;
import com.spdb.fdev.spdb.entity.CaasDeployment;
import com.spdb.fdev.spdb.entity.SccDeploy;
import com.spdb.fdev.spdb.service.IBatchModifyService;
import com.spdb.fdev.spdb.service.ICaasModifyKeyService;
import com.spdb.fdev.spdb.service.ISccModifyKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

/**
 * @Author:guanz2
 * @Date:2022/2/23-17:12
 * @Description:
 */
@Component
public class BatchModifyServiceImpl implements IBatchModifyService {

    @Autowired
    private ICaasDeploymentDao caasDeploymentDao;

    @Autowired
    private ISccDeploymentDao sccDeployDao;

    @Autowired
    private ICaasModifyKeyService caasModifyKeyService;

    @Autowired
    private ISccModifyKeyService sccModifyKeyService;

    private static final String commonStr = "{\"name\": \"RUN_TIME\", \"value\": \"1\"}, {\"name\": \"INT_POD_IP\",  \"valueFrom\": {\"fieldRef\": {\"fieldPath\": \"status.podIP\"}}}";

    /*
     * @author:guanz2
     * @Description:修改云上、云下esf应用的env
     * 入参：数组对象。对象：应用名、团队、网段
     */
    @Override
    public void updateEsfEnv(List<Map<String, Object>> list) throws Exception {
        List<Map> params =  new ArrayList();
        for (Map item : list){
            String deploy_name = (String) item.get(Dict.DEPLOYNAME);
            String vlan = (String) item.get(Dict.VLAN);
            String team = (String) item.get(Dict.TEAM);
            if (CommonUtils.isNullOrEmpty(deploy_name)){
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"缺少参数：应用名"});
            }
            if (CommonUtils.isNullOrEmpty(vlan)){
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"缺少参数：网段"});
            }
            if (!vlan.equals("dmz") && !vlan.equals("biz")){
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"网段可选：dmz,biz"});
            }
            if (CommonUtils.isNullOrEmpty(team)){
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"缺少参数：团队"});
            }
            if (!team.equals("per") && !team.equals("common") && !team.equals("pay") && !team.equals("ent")){
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"团队可选：per,common,pay,ent "});
            }

            params.add(item);
        }
        this.modifyESF_Caas_Scc_Env(params);
    }

    //批量修改云上副本数、内存、cpu
    @Override
    public void updateSccYamlBatch(List<Map<String, Object>> list) throws Exception {
        List<Map> params =  new ArrayList();
        for (Map item : list){
            String deploy_name = (String) item.get(Dict.DEPLOYNAME);
            String active_env = (String) item.get(Dict.ACTIVEENV);
            Integer replicas = (Integer) item.get(Dict.REPLICAS);
            String cpu_limits = (String) item.get(Dict.CPULIMITS);
            String cpu_requests = (String) item.get(Dict.CPUREQUESTS);
            String memory_limits = (String) item.get(Dict.MEMORYLIMITS);
            String memory_requests = (String) item.get(Dict.MEMORYREQUESTS);
            if (CommonUtils.isNullOrEmpty(deploy_name)){
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"缺少参数：应用名"});
            }
            if (CommonUtils.isNullOrEmpty(active_env)){
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"缺少参数：环境"});
            }
            if (!active_env.equals("pro") && !active_env.equals("gray")){
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"环境可选：pro,gray"});
            }
            if (CommonUtils.isNullOrEmpty(replicas) && CommonUtils.isNullOrEmpty(cpu_limits) && CommonUtils.isNullOrEmpty(cpu_requests)
                    && CommonUtils.isNullOrEmpty(memory_limits) && CommonUtils.isNullOrEmpty(memory_requests)){
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"缺少入参：replicas、cpu_limits、cpu_requests、memory_limits、memory_requests"});
            }
            params.add(item);
        }
        this.modifySccYamlBatch(params);
    }

    //云上、云下批量添加initContainers
    @Override
    public void addInitContainers(List<String> deploys) throws Exception {
        if(CommonUtils.isNullOrEmpty(deploys)){
            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{"应用名为空"});
        }
        List<Map<String, String>> caasModifyKeys = new ArrayList();
        List<LinkedHashMap<String, Object>> sccModifyKeys = new ArrayList();

        for (int i= 0; i < deploys.size(); i++){
            //修改云下
            String deploy_name = deploys.get(i);
            List<CaasDeployment> caasDeployments = caasDeploymentDao.queryCaasDeploymentCondition(deploy_name);
            if(!CommonUtils.isNullOrEmpty(caasDeployments)){
                for (CaasDeployment deployment : caasDeployments){
                    Map<String, String> caasModifyKey = new HashMap<>();
                    String image = (String) JSONObject.parseObject(deployment.getTag(), List.class).get(0);
                    String ip = image.split("/")[0];
                    String imgNamespace = image.split("/")[1];

                    List initContainers = new ArrayList();
                    LinkedHashMap initContainer = new LinkedHashMap();
                    initContainer.put(Dict.NAME, "sysctl-buddy");
                    initContainer.put(Dict.IMAGE, ip + "/" + imgNamespace + "/" + "centos:centos7.9.2009");
                    Map securityContext = new LinkedHashMap();
                    securityContext.put("privileged", true);
                    initContainer.put("securityContext", securityContext);
                    List command = new ArrayList();
                    command.add("/bin/sh");
                    initContainer.put("command", command);
                    List argsList = new ArrayList();
                    argsList.add("-c");
                    argsList.add("sysctl -w net.core.somaxconn=2048");
                    initContainer.put("args", argsList);
                    Map resources = new LinkedHashMap();
                    Map requests = new LinkedHashMap();
                    requests.put("cpu", "1m");
                    requests.put("memory", "1Mi");
                    resources.put("requests",requests);
                    initContainer.put("resources",resources);
                    initContainers.add(initContainer);

                    String modify = JSON.toJSONString(initContainers);
                    caasModifyKey.put(Dict.CLUSTER , deployment.getCluster());
                    caasModifyKey.put(Dict.NAMESPACE , deployment.getNamespace());
                    caasModifyKey.put(Dict.DEPLOYMENT , deploy_name);
                    caasModifyKey.put(Dict.INITCONTAINERS , modify);
                    caasModifyKeys.add(caasModifyKey);
                }
            }

            //修改云上
            List<SccDeploy> sccDeploys = sccDeployDao.getSccDeployCondition(deploy_name);
            if(!CommonUtils.isNullOrEmpty(sccDeploys)){
                for (SccDeploy sccDeploy : sccDeploys){
                    LinkedHashMap<String, Object> sccModifyKey = new LinkedHashMap<>();
                    String yaml = sccDeploy.getYaml();
                    Map yamlMap = yamlString2Map(yaml);
                    LinkedHashMap spec =((LinkedHashMap)((LinkedHashMap)((LinkedHashMap) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC));
                    LinkedHashMap newSpec = new LinkedHashMap();

                    Map securityContext = new LinkedHashMap();
                    List sysctls = new ArrayList();
                    LinkedHashMap temp = new LinkedHashMap();
                    temp.put(Dict.NAME, "net.core.somaxconn");
                    temp.put(Dict.VALUE, "2048");
                    sysctls.add(temp);
                    securityContext.put("sysctls", sysctls);
                    newSpec.put("securityContext", securityContext);
                    newSpec.putAll(spec);

                    ((Map)((Map)yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).put(Dict.SPEC, newSpec);

                    String newYaml = Map2yamString(yamlMap);

                    sccModifyKey.put(Dict.RESOURCECODE, deploy_name);
                    sccModifyKey.put(Dict.NAMESPACECODE, sccDeploy.getNamespace_code());
                    sccModifyKey.put(Dict.YAML, newYaml);
                    sccModifyKeys.add(sccModifyKey);
                }
            }

        }
        if (!CommonUtils.isNullOrEmpty(caasModifyKeys)){
            caasModifyKeyService.updateModifyKeys(caasModifyKeys);
        }
        if (!CommonUtils.isNullOrEmpty(sccModifyKeys)){
            sccModifyKeyService.updateModifyKeys(sccModifyKeys);
        }
    }

    //批量修改云上scc环境的滚动发布参数，skywalking-configmap， dnsConfig
    @Override
    public void updateSccRollingParams(List<String> proDeployments, List<String> grayDeployments) throws Exception {
        Set<String> deployList = new HashSet<>();
        //pro和gray应用列表取和，
        deployList.addAll(proDeployments);
        deployList.addAll(grayDeployments);
        List<LinkedHashMap<String, Object>> modifyKeys = new ArrayList<LinkedHashMap<String, Object>>();
        for(String deployName: deployList){
            if (!CommonUtils.isNullOrEmpty(deployName)){
                List<SccDeploy> sccDeploys = sccDeployDao.getSccDeployCondition(deployName);

                //云上生产修改
                if(proDeployments.contains(deployName)){
                    for (SccDeploy sccDeploy : sccDeploys){
                        LinkedHashMap modifyKey = new LinkedHashMap();
                        String namespaceCode = sccDeploy.getNamespace_code();
                        if (namespaceCode.equals(Dict.MBPERUZ11) || namespaceCode.equals(Dict.MBPERUZ12) ||
                                namespaceCode.equals(Dict.MBPERUZ31)  || namespaceCode.equals(Dict.MBPERUZ32)){
                            Map yamlMap = yamlString2Map(sccDeploy.getYaml());
                            LinkedHashMap rollingUpdate = new LinkedHashMap();
                            LinkedHashMap maxSurge = new LinkedHashMap();
                            LinkedHashMap maxUnavailable = new LinkedHashMap();
                            maxSurge.put("intVal", 1);
                            maxSurge.put("kind", 0);
                            maxSurge.put("strVal", "0%");
                            maxUnavailable.put("intVal", 0);
                            maxUnavailable.put("kind", 0);
                            maxUnavailable.put("strVal", "25%");
                            rollingUpdate.put("maxSurge", maxSurge);
                            rollingUpdate.put("maxUnavailable", maxUnavailable);
                            ((Map)((Map)(yamlMap.get("spec"))).get("strategy")).put("rollingUpdate",rollingUpdate);
                            LinkedHashMap skywalking = new LinkedHashMap();
                            skywalking.put("name", "skywalking-configmap");
                            LinkedHashMap configMap = new LinkedHashMap();
                            configMap.put("configMapRef", skywalking);
                            List<Map> envFrom =(List<Map>) ((Map)((List)((Map)((Map)((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).get(Dict.ENVFROMUP);
                            envFrom.add(configMap);
                            ((Map)((List)((Map)((Map)((Map)yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).put(Dict.ENVFROMUP, envFrom);
                            List nameservers = new ArrayList();
                            if (namespaceCode.equals(Dict.MBPERUZ11) || namespaceCode.equals(Dict.MBPERUZ12)){
                                nameservers.add("xxx");
                                nameservers.add("xxx");
                            }else {
                                nameservers.add("xxx");
                                nameservers.add("xxx");
                            }
                            Map dnsConfig = (Map) ((Map)((Map)((Map)yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get("dnsConfig");
                            if(CommonUtils.isNullOrEmpty(dnsConfig)){
                                LinkedHashMap DNSConfig = new LinkedHashMap();
                                DNSConfig.put("nameservers", nameservers);
                                LinkedHashMap newSpec = new LinkedHashMap();
                                newSpec.put(Dict.CONTAINERS, null);
                                newSpec.put("dnsConfig", DNSConfig);
                                LinkedHashMap spec =((LinkedHashMap)((LinkedHashMap)((LinkedHashMap) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC));
                                newSpec.putAll(spec);
                                ((Map)((Map)yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).put(Dict.SPEC, newSpec);
                            }else {
                                ((Map)((Map)((Map)((Map)yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get("dnsConfig")).put("nameservers", nameservers);
                            }

                            modifyKey.put(Dict.NAMESPACECODE, namespaceCode);
                            modifyKey.put(Dict.RESOURCECODE, deployName);
                            modifyKey.put(Dict.YAML, Map2yamString(yamlMap));
                            modifyKeys.add(modifyKey);
                        }
                    }
                }
                //云上灰度修改
                if(grayDeployments.contains(deployName)){
                    for (SccDeploy sccDeploy : sccDeploys){
                        LinkedHashMap modifyKey = new LinkedHashMap();
                        String namespaceCode = sccDeploy.getNamespace_code();
                        if (namespaceCode.equals(Dict.MBPERUZ10) || namespaceCode.equals(Dict.MBPERUZ30)){
                            Map yamlMap = yamlString2Map(sccDeploy.getYaml());
                            LinkedHashMap rollingUpdate = new LinkedHashMap();
                            LinkedHashMap maxSurge = new LinkedHashMap();
                            LinkedHashMap maxUnavailable = new LinkedHashMap();
                            maxSurge.put("intVal", 1);
                            maxSurge.put("kind", 0);
                            maxSurge.put("strVal", "0%");
                            maxUnavailable.put("intVal", 0);
                            maxUnavailable.put("kind", 0);
                            maxUnavailable.put("strVal", "25%");
                            rollingUpdate.put("maxSurge", maxSurge);
                            rollingUpdate.put("maxUnavailable", maxUnavailable);
                            ((Map)((Map)(yamlMap.get("spec"))).get("strategy")).put("rollingUpdate",rollingUpdate);
                            LinkedHashMap skywalking = new LinkedHashMap();
                            skywalking.put("name", "skywalking-configmap");
                            LinkedHashMap configMap = new LinkedHashMap();
                            configMap.put("configMapRef", skywalking);
                            List<Map> envFrom =(List<Map>) ((Map)((List)((Map)((Map)((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).get(Dict.ENVFROMUP);
                            envFrom.add(configMap);
                            ((Map)((List)((Map)((Map)((Map)yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).put(Dict.ENVFROMUP, envFrom);
                            List nameservers = new ArrayList();
                            if (namespaceCode.equals(Dict.MBPERUZ10)){
                                nameservers.add("xxx");
                                nameservers.add("xxx");
                            }else {
                                nameservers.add("xxx");
                                nameservers.add("xxx");
                            }
                            Map dnsConfig = (Map) ((Map)((Map)((Map)yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get("dnsConfig");
                            if(CommonUtils.isNullOrEmpty(dnsConfig)){
                                LinkedHashMap DNSConfig = new LinkedHashMap();
                                DNSConfig.put("nameservers", nameservers);
                                LinkedHashMap newSpec = new LinkedHashMap();
                                newSpec.put(Dict.CONTAINERS, null);
                                newSpec.put("dnsConfig", DNSConfig);
                                LinkedHashMap spec =((LinkedHashMap)((LinkedHashMap)((LinkedHashMap) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC));
                                newSpec.putAll(spec);
                                ((Map)((Map)yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).put(Dict.SPEC, newSpec);
                            }else {
                                ((Map)((Map)((Map)((Map)yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get("dnsConfig")).put("nameservers", nameservers);
                            }

                            modifyKey.put(Dict.NAMESPACECODE, namespaceCode);
                            modifyKey.put(Dict.RESOURCECODE, deployName);
                            modifyKey.put(Dict.YAML, Map2yamString(yamlMap));
                            modifyKeys.add(modifyKey);
                        }
                    }
                }
            }
        }

        if(!CommonUtils.isNullOrEmpty(modifyKeys)){
            sccModifyKeyService.updateModifyKeys(modifyKeys);
        }
    }


    //修改caas、scc环境下的esf应用的env，同时修改云上环境的cpu,内存，副本数
    private void modifyESF_Caas_Scc_Env(List<Map> params){

        List<Map<String, String>> caasModifyKeys = new ArrayList();
        List<LinkedHashMap<String, Object>> sccModifyKeys = new ArrayList();

        for (Map item : params){

            String deploy_name = (String) item.get(Dict.DEPLOYNAME);
            String vlan = (String) item.get(Dict.VLAN);
            String team = (String) item.get(Dict.TEAM);
            //可选参入：cpu ,内存，副本数。
            Integer replicas = (Integer) item.get(Dict.REPLICAS);
            String cpu_limits = (String) item.get(Dict.CPULIMITS);
            String cpu_requests = (String) item.get(Dict.CPUREQUESTS);
            String memory_limits = (String) item.get(Dict.MEMORYLIMITS);
            String memory_requests = (String) item.get(Dict.MEMORYREQUESTS);

            String SDKGK = this.getSDKGK((String) item.get(Dict.DEPLOYNAME));

            if (CommonUtils.isNullOrEmpty(SDKGK)){
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{deploy_name+": 当前应用的灰度，没查到SDK-GK"});
            }

            //修改云下
            List<CaasDeployment> caasDeployments = caasDeploymentDao.queryCaasDeploymentCondition(deploy_name);

            if (CommonUtils.isNullOrEmpty(caasDeployments)) {
                throw new FdevException(ErrorConstants.DEPLOYMENT_NULL);
            }

            for (CaasDeployment caasDeployment : caasDeployments){

                    String namespace = caasDeployment.getNamespace();

                    if(namespace.indexOf("gray") < 0){
                        //所有生产环境
                        Map<String, String> caasModifyKey = new HashMap<>();
                        String CFG_SVR_URL = this.getCaasCFG_SVR_URL(vlan, team, caasDeployment.getCluster());

                        if (CommonUtils.isNullOrEmpty(CFG_SVR_URL)){
                            throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{deploy_name+": 没查到CFG_SVR_URL"});
                        }
                        String modifyEnv ="[" + "{\"name\": \"CFG_SVR_URL\", \"value\": \"" + CFG_SVR_URL + "\"}" + "," + "{\"name\": \"SDK-GK\", \"value\": \"" + SDKGK + "\"}" + "," + commonStr + "]";
                        String oldEnv = caasDeployment.getEnv();
                        String resEnv = null;
                        if (CommonUtils.isNullOrEmpty(oldEnv) || oldEnv.equals("[null]")){
                            resEnv = "[[" + modifyEnv + "]]";
                        }else {

                            List<Map> modifyEnvList = JSONObject.parseObject(modifyEnv, List.class);
                            List<Map> oldEnvList = JSONObject.parseObject(oldEnv.substring(1, oldEnv.length()-1), List.class, Feature.OrderedField);

                            //如果modify的name，原本env就存在，以modify的覆盖掉env的对应内容
                            Set<String> envNameSet = new HashSet<>();

                            for (Map temp : oldEnvList){
                                if (temp.containsKey(Dict.NAME)){
                                    envNameSet.add((String) temp.get(Dict.NAME));
                                }
                            }
                            for (Map itemp : modifyEnvList){
                                String name = (String) itemp.get(Dict.NAME);
                                if (!envNameSet.contains(name)){
                                    //如果env不包含modify的name,则直接加入env
                                    oldEnvList.add(itemp);
                                }else {
                                    //如果env包含modify的name,则进行替换
                                    for (int i = 0; i < oldEnvList.size(); i++){
                                        Map ele = oldEnvList.get(i);
                                        if (ele.containsKey(Dict.NAME) && ele.get(Dict.NAME).equals(name)){
                                            oldEnvList.set(i, itemp);
                                        }
                                    }
                                }
                            }

                            resEnv = "["+ JSON.toJSONString(oldEnvList)+"]";
                        }

                        caasModifyKey.put(Dict.CLUSTER , caasDeployment.getCluster());
                        caasModifyKey.put(Dict.NAMESPACE , namespace);
                        caasModifyKey.put(Dict.DEPLOYMENT , deploy_name);
                        caasModifyKey.put(Dict.ENV , resEnv);
                        caasModifyKeys.add(caasModifyKey);
                    }
                }


            //修改云上
            List<SccDeploy> sccDeploys = sccDeployDao.getSccDeployCondition(deploy_name);

            if (CommonUtils.isNullOrEmpty(sccDeploys)) {
                throw new FdevException(ErrorConstants.DEPLOYMENT_NULL);
            }

            for (SccDeploy sccDeploy : sccDeploys){

                    String namespace = sccDeploy.getNamespace_code();
                    String yaml = sccDeploy.getYaml();
                    if (!namespace.equals(Dict.MBPERUZ10) && !namespace.equals(Dict.MBPERUZ30) && namespace.indexOf("gray") < 0 ){
                        //所有生产环境
                        LinkedHashMap<String, Object> sccModifyKey = new LinkedHashMap<>();

                        String modifyStr = "[" + "{\"name\": \"SDK-GK\", \"value\": \"" + SDKGK + "\"}" + ',' + commonStr + "]";

                        List<Map> modifyEnv = JSONObject.parseObject(modifyStr, List.class);

                        //将yaml字符串转换成Map
                        Map yamlMap = this.yamlString2Map(yaml);

                        List<Map> env =(List<Map>) ((Map)((List)((Map)((Map)((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).get(Dict.ENV);

                        Set envNameSet = new HashSet();
                        if (!CommonUtils.isNullOrEmpty(env)){
                            //如果modify的name，原本env就存在，以modify的覆盖掉env对应内容
                            for (Map temp : env){
                                if (temp.containsKey(Dict.NAME)){
                                    envNameSet.add(temp.get(Dict.NAME));
                                }
                            }

                            for (Map itemp : modifyEnv){
                                String name = (String) itemp.get(Dict.NAME);
                                if (!envNameSet.contains(name)){
                                    //如果env不包含modify的name，则直接加入
                                    env.add(itemp);
                                }else {
                                    //如果env包含modify的name，则以modify的为准
                                    for (int i = 0; i < env.size(); i++){
                                        Map ele = env.get(i);
                                        if (ele.containsKey(Dict.NAME) && ele.get(Dict.NAME).equals(name)){
                                            env.set(i, itemp);
                                        }
                                    }
                                }
                            }
                            ((Map)((List)((Map)((Map)((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).put(Dict.ENV, env);
                        } else {
                            ((Map)((List)((Map)((Map)((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).put(Dict.ENV, modifyEnv);
                        }

                        // 加入cpu ,memroy，replicas的修改
                        if (!CommonUtils.isNullOrEmpty(replicas)){
                            ((Map)yamlMap.get(Dict.SPEC)).put(Dict.REPLICAS, replicas.intValue());
                        }

                        if (!CommonUtils.isNullOrEmpty(cpu_limits)){
                            Map cpuLimitsMap = new LinkedHashMap();
                            cpuLimitsMap.put("amount", cpu_limits);
                            cpuLimitsMap.put("format", "");
                            ((Map)((Map)((Map)((List)((Map) ((Map) ((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).get(Dict.RESOURCES)).get(Dict.LIMITS)).put(Dict.CPU, cpuLimitsMap);
                        }

                        if (!CommonUtils.isNullOrEmpty(cpu_requests)){
                            Map cpuRequestsMap = new LinkedHashMap();
                            cpuRequestsMap.put("amount", cpu_requests);
                            cpuRequestsMap.put("format", "");
                            ((Map)((Map)((Map)((List)((Map) ((Map) ((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).get(Dict.RESOURCES)).get(Dict.REQUESTS)).put(Dict.CPU, cpuRequestsMap);
                        }

                        if (!CommonUtils.isNullOrEmpty(memory_limits)){
                            Map memoryLimitsMap = new LinkedHashMap();
                            memoryLimitsMap.put("amount", memory_limits);
                            memoryLimitsMap.put("format", "Gi");
                            ((Map)((Map)((Map)((List)((Map) ((Map) ((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).get(Dict.RESOURCES)).get(Dict.LIMITS)).put(Dict.MEMORY, memoryLimitsMap);
                        }

                        if (!CommonUtils.isNullOrEmpty(memory_requests)){
                            Map memoryRequestsMap = new LinkedHashMap();
                            memoryRequestsMap.put("amount", memory_requests);
                            memoryRequestsMap.put("format", "Gi");
                            ((Map)((Map)((Map)((List)((Map) ((Map) ((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).get(Dict.RESOURCES)).get(Dict.REQUESTS)).put(Dict.MEMORY, memoryRequestsMap);
                        }

                        //Map转String
                        String modifyYaml = this.Map2yamString(yamlMap);

                        sccModifyKey.put(Dict.RESOURCECODE, deploy_name);
                        sccModifyKey.put(Dict.NAMESPACECODE, namespace);
                        sccModifyKey.put(Dict.YAML, modifyYaml);
                        sccModifyKeys.add(sccModifyKey);
                    }

                }


        }

        caasModifyKeyService.updateModifyKeys(caasModifyKeys);
        sccModifyKeyService.updateModifyKeys(sccModifyKeys);

    }

    //获取SDK-GK值，一个应用，云上，云下，所有环境的GK值都是一样的
    private String getSDKGK(String deploy_name){
        List<CaasDeployment> caasDeployments = caasDeploymentDao.queryCaasDeploymentCondition(deploy_name);
        List<SccDeploy> sccDeploys = sccDeployDao.getSccDeployCondition(deploy_name);


        if (!CommonUtils.isNullOrEmpty(caasDeployments)){
            for (CaasDeployment caasDeployment : caasDeployments){
                String namespace = caasDeployment.getNamespace();
                String envStr = caasDeployment.getEnv();
                if(namespace.indexOf("gray") >= 0 && envStr.indexOf("SDK-GK") >= 0){

                    List<Map> env = (List) JSONObject.parseObject(envStr, List.class).get(0);
                    for (Map item : env){
                        if (item.get(Dict.NAME).equals("SDK-GK")){
                            return (String) item.get(Dict.VALUE);
                        }
                    }
                }
            }
        }

        if(!CommonUtils.isNullOrEmpty(sccDeploys)){
            for (SccDeploy sccDeploy : sccDeploys){
                String namespace = sccDeploy.getNamespace_code();
                String yaml = sccDeploy.getYaml();
                if ((namespace.equals(Dict.MBPERUZ10) || namespace.equals(Dict.MBPERUZ30) || namespace.indexOf("gray") >= 0) && yaml.indexOf("SDK-GK") >= 0){
                    //将yaml字符串转换成Map
                    Map yamlMap = this.yamlString2Map(yaml);

                    List<Map> env =(List<Map>) ((Map)((List)((Map)((Map)((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).get(Dict.ENV);

                    for (Map item : env){
                        if (item.containsKey("SDK-GK")){
                            return (String) item.get("SDK-GK");
                        }
                    }
                }
            }
        }
        return null;
    }

    //caas环境，获取CFG_SVR_URL
    private String getCaasCFG_SVR_URL(String vlan, String team, String cluster){
        if (vlan.equals("dmz")){
            if (team.equals("per")){
                switch (cluster){
                    case Dict.SHK1:
                        return "http://esfconfdmzshnbper1.spdb.com:2222,http://esfconfdmzshnbper2.spdb.com:2222";
                    case Dict.SHK2:
                        return "http://esfconfdmzsh2nbper1.spdb.com:2222,http://esfconfdmzsh2nbper2.spdb.com:2222";
                    case Dict.HFK1:
                        return "http://esfconfdmzhfnbper1.spdb.com:2222,http://esfconfdmzhfnbper2.spdb.com:2222";
                    case Dict.HFK2:
                        return "http://esfconfdmzhf2nbper1.spdb.com:2222,http://esfconfdmzhf2nbper2.spdb.com:2222";
                }
            }
            if (team.equals("ent")){
                switch (cluster){
                    case Dict.SHK1:
                        return "http://esfconfdmzshnbent1.spdb.com:2222,http://esfconfdmzshnbent2.spdb.com:2222";
                    case Dict.SHK2:
                        return "http://esfconfdmzsh2nbent1.spdb.com:2222,http://esfconfdmzsh2nbent2.spdb.com:2222";
                    case Dict.HFK1:
                        return "http://esfconfdmzhfnbent1.spdb.com:2222,http://esfconfdmzhfnbent2.spdb.com:2222";
                    case Dict.HFK2:
                        return "http://esfconfdmzhf2nbent1.spdb.com:2222,http://esfconfdmzhf2nbent2.spdb.com:2222";
                }
            }
            if (team.equals("pay")){
                switch (cluster){
                    case Dict.SHK1:
                        return "http://esfconfdmzshepay1.spdb.com:2222,http://esfconfdmzshepay2.spdb.com:2222";
                    case Dict.SHK2:
                        return "http://esfconfdmzsh2epay1.spdb.com:2222,http://esfconfdmzsh2epay2.spdb.com:2222";
                    case Dict.HFK1:
                        return "http://esfconfdmzhfepay1.spdb.com:2222,http://esfconfdmzhfepay2.spdb.com:2222";
                    case Dict.HFK2:
                        return "http://esfconfdmzhf2epay1.spdb.com:2222,http://esfconfdmzhf2epay2.spdb.com:2222";
                }
            }
            if (team.equals("common")){
                switch (cluster){
                    case Dict.SHK1:
                        return "http://esfconfdmzshebankoth1.spdb.com:2222,http://esfconfdmzshebankoth2.spdb.com:2222";
                    case Dict.SHK2:
                        return "http://esfconfdmzsh2ebankoth1.spdb.com:2222,http://esfconfdmzsh2ebankoth2.spdb.com:2222";
                    case Dict.HFK1:
                        return "http://esfconfdmzhfebankoth1.spdb.com:2222,http://esfconfdmzhfebankoth2.spdb.com:2222";
                    case Dict.HFK2:
                        return "http://esfconfdmzhf2ebankoth1.spdb.com:2222,http://esfconfdmzhf2ebankoth2.spdb.com:2222";
                }
            }
            return  null;
        }

        if (vlan.equals("biz")){
            switch (cluster){
                case Dict.SHK1:
                    return "http://esfconfbizshnbh1.spdb.com:2222,http://esfconfbizshnbh2.spdb.com:2222";
                case Dict.SHK2:
                    return "http://esfconfbizsh2nbh1.spdb.com:2222,http://esfconfbizsh2nbh2.spdb.com:2222";
                case Dict.HFK1:
                    return "http://esfconfbizhfnbh1.spdb.com:2222,http://esfconfbizhfnbh2.spdb.com:2222";
                case Dict.HFK2:
                    return "http://esfconfbizhf2nbh1.spdb.com:2222,http://esfconfbizhf2nbh2.spdb.com:2222";
            }
        }
        return null;
    }

    //修改云上应用的yaml文件
    private void modifySccYamlBatch(List<Map> params){
        List<LinkedHashMap<String, Object>> sccModifyKeys = new ArrayList();
        for (Map item : params){
            String deploy_name = (String) item.get(Dict.DEPLOYNAME);
            String active_env = (String) item.get(Dict.ACTIVEENV);
            Integer replicas = (Integer) item.get(Dict.REPLICAS);
            String cpu_limits = (String) item.get(Dict.CPULIMITS);
            String cpu_requests = (String) item.get(Dict.CPUREQUESTS);
            String memory_limits = (String) item.get(Dict.MEMORYLIMITS);
            String memory_requests = (String) item.get(Dict.MEMORYREQUESTS);

            List<SccDeploy> sccDeployAll = this.sccDeployDao.getSccDeployCondition(deploy_name);

            if (CommonUtils.isNullOrEmpty(sccDeployAll)) {
                throw new FdevException(ErrorConstants.DEPLOYMENT_NULL);
            }
            //生产环境
            List<SccDeploy>  sccDeploys= new ArrayList<>();
            if (active_env.equals(Dict.PRO)){
                sccDeploys = this.getSccDeployByEnv(sccDeployAll, Dict.PRO);
            }
            //灰度环境
            if (active_env.equals(Dict.GRAY)){
                sccDeploys = this.getSccDeployByEnv(sccDeployAll, Dict.GRAY);
            }

            if (CommonUtils.isNullOrEmpty(sccDeploys)) {
                throw new FdevException(ErrorConstants.DEPLOYMENT_NULL);
            }

            for (SccDeploy sccDeploy : sccDeploys){
                LinkedHashMap<String, Object> sccModifyKey = new LinkedHashMap<>();

                String namespace_code = sccDeploy.getNamespace_code();
                String yaml = sccDeploy.getYaml();
                Map yamlMap = this.yamlString2Map(yaml);

                if (!CommonUtils.isNullOrEmpty(replicas)){
                    ((Map)yamlMap.get(Dict.SPEC)).put(Dict.REPLICAS, replicas.intValue());
                }

                if (!CommonUtils.isNullOrEmpty(cpu_limits)){
                    Map cpuLimitsMap = new LinkedHashMap();
                    cpuLimitsMap.put("amount", cpu_limits);
                    cpuLimitsMap.put("format", "");
                    ((Map)((Map)((Map)((List)((Map) ((Map) ((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).get(Dict.RESOURCES)).get(Dict.LIMITS)).put(Dict.CPU, cpuLimitsMap);
                }

                if (!CommonUtils.isNullOrEmpty(cpu_requests)){
                    Map cpuRequestsMap = new LinkedHashMap();
                    cpuRequestsMap.put("amount", cpu_requests);
                    cpuRequestsMap.put("format", "");
                    ((Map)((Map)((Map)((List)((Map) ((Map) ((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).get(Dict.RESOURCES)).get(Dict.REQUESTS)).put(Dict.CPU, cpuRequestsMap);
                }

                if (!CommonUtils.isNullOrEmpty(memory_limits)){
                    Map memoryLimitsMap = new LinkedHashMap();
                    memoryLimitsMap.put("amount", memory_limits);
                    memoryLimitsMap.put("format", "Gi");
                    ((Map)((Map)((Map)((List)((Map) ((Map) ((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).get(Dict.RESOURCES)).get(Dict.LIMITS)).put(Dict.MEMORY, memoryLimitsMap);
                }

                if (!CommonUtils.isNullOrEmpty(memory_requests)){
                    Map memoryRequestsMap = new LinkedHashMap();
                    memoryRequestsMap.put("amount", memory_requests);
                    memoryRequestsMap.put("format", "Gi");
                    ((Map)((Map)((Map)((List)((Map) ((Map) ((Map) yamlMap.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0)).get(Dict.RESOURCES)).get(Dict.REQUESTS)).put(Dict.MEMORY, memoryRequestsMap);
                }

                String modifyYaml = this.Map2yamString(yamlMap);

                sccModifyKey.put(Dict.NAMESPACECODE, namespace_code);
                sccModifyKey.put(Dict.RESOURCECODE, deploy_name);
                sccModifyKey.put(Dict.YAML, modifyYaml);
                sccModifyKeys.add(sccModifyKey);
            }
        }
        this.sccModifyKeyService.updateModifyKeys(sccModifyKeys);
    }

    private List getSccDeployByEnv(List<SccDeploy> list, String active_env){
        List<SccDeploy> res = new ArrayList<>();
        //灰度
        if (active_env.equals(Dict.GRAY)){
            for (SccDeploy sccDeploy : list){
                String namespace = sccDeploy.getNamespace_code();
                if (namespace.equals(Dict.MBPERUZ10) || namespace.equals(Dict.MBPERUZ30) || namespace.indexOf("gray") >= 0){
                    res.add(sccDeploy);
                }
            }
        }

        //生产
        if (active_env.equals(Dict.PRO)){
            for (SccDeploy sccDeploy : list){
                String namespace = sccDeploy.getNamespace_code();
                if ( !namespace.equals(Dict.MBPERUZ10) && !namespace.equals(Dict.MBPERUZ30) && namespace.indexOf("gray") < 0){
                    res.add(sccDeploy);
                }
            }
        }
        return res;
    }

    private Map yamlString2Map(String yaml){
        //将yaml字符串转换成json输出
        LinkedHashMap<String, Object> yamlMap = new LinkedHashMap<>();
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml wYaml = new Yaml();
        yamlMap = wYaml.load(yaml);
        return yamlMap;
    }

    private String Map2yamString(Map map){
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml();
        String res = yaml.dumpAsMap(map);
        return res;
    }
}
