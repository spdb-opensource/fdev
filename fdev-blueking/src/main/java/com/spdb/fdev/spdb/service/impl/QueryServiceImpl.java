package com.spdb.fdev.spdb.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.util.CommonUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.spdb.dao.ICaasDeploymentDao;
import com.spdb.fdev.spdb.dao.ICaasPodDao;
import com.spdb.fdev.spdb.dao.ISccDeploymentDao;
import com.spdb.fdev.spdb.dao.ISccPodDao;
import com.spdb.fdev.spdb.entity.CaasDeployment;
import com.spdb.fdev.spdb.entity.CaasPod;
import com.spdb.fdev.spdb.entity.SccDeploy;
import com.spdb.fdev.spdb.entity.SccPod;
import com.spdb.fdev.spdb.service.ICaasYamlService;
import com.spdb.fdev.spdb.service.IQueryService;
import com.spdb.fdev.spdb.service.ISccDeploymentService;
import net.sf.json.util.JSONUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author:guanz2
 * @Date:2021/12/10-16:58
 * @Description:
 */
@Component
public class QueryServiceImpl implements IQueryService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private ICaasDeploymentDao caasDeploymentDao;

    @Autowired
    private ICaasPodDao caasPodDao;

    @Autowired
    private ISccDeploymentDao sccDeploymentDao;

    @Autowired
    private ISccPodDao sccPodDao;

    @Value("${fdevUrl}")
    private String fdevUrl;

    private CaasYamlServiceImpl caasYamlService = new CaasYamlServiceImpl();

    private Logger logger = LoggerFactory.getLogger(QueryServiceImpl.class);

    @Override
    public Map queryAllCount() {
        //CAAS
        long shk1CaasDeploymentCount = caasDeploymentDao.queryCountByConditon(Dict.SHK1);
        long shk2CaasDeploymentCount = caasDeploymentDao.queryCountByConditon(Dict.SHK2);
        long hfk1CaasDeploymentCount = caasDeploymentDao.queryCountByConditon(Dict.HFK1);
        long hfk2CaasDeploymentCount = caasDeploymentDao.queryCountByConditon(Dict.HFK2);
        long caasDeploymentCount = shk1CaasDeploymentCount + shk2CaasDeploymentCount + hfk1CaasDeploymentCount + hfk2CaasDeploymentCount;

        long shk1CaasPodCount = caasPodDao.queryCountByCondition(Dict.SHK1) + caasPodDao.queryCountByCondition("sh00d01");
        long shk2CaasPodCount = caasPodDao.queryCountByCondition(Dict.SHK2);
        long hfk1CaasPodCount = caasPodDao.queryCountByCondition(Dict.HFK1) + caasPodDao.queryCountByCondition("hf00d01");
        long hfk2CaasPodCount = caasPodDao.queryCountByCondition(Dict.HFK2);
        long caasPodCount = shk1CaasPodCount + shk2CaasPodCount + hfk1CaasPodCount + hfk2CaasPodCount;

        //SCC
        long shk1SccDeployCount = sccPodDao.queryClusterCount(Arrays.asList(Dict.SCCSHK1CLUSTER));
        long shk2SccDeployCount = sccPodDao.queryClusterCount(Arrays.asList(Dict.SCCSHK2CLUSTER));
        long hfk1SccDeployCount = sccPodDao.queryClusterCount(Arrays.asList(Dict.SCCHFK1CLUSTER));
        long hfk2SccDeployCount = sccPodDao.queryClusterCount(Arrays.asList(Dict.SCCHFK2CLUSTER));

        long sccDeployCount = shk1SccDeployCount + shk2SccDeployCount + hfk1SccDeployCount + hfk2SccDeployCount;

        long shk1SccPodCount = sccPodDao.queryCountByCondition(Arrays.asList(Dict.SCCSHK1CLUSTER));
        long shk2SccPodCount = sccPodDao.queryCountByCondition(Arrays.asList(Dict.SCCSHK2CLUSTER));
        long hfk1SccPodCount = sccPodDao.queryCountByCondition(Arrays.asList(Dict.SCCHFK1CLUSTER));
        long hfk2SccPodCount = sccPodDao.queryCountByCondition(Arrays.asList(Dict.SCCHFK2CLUSTER));
        long sccPodCount = shk1SccPodCount + shk2SccPodCount + hfk1SccPodCount + hfk2SccPodCount;

        List caasDeployList = new ArrayList();
        caasDeployList.add(this.labelUtil(Dict.CAASSHK1, Dict.CNSHK1, shk1CaasDeploymentCount));
        caasDeployList.add(this.labelUtil(Dict.CAASSHK2, Dict.CNSHK2, shk2CaasDeploymentCount));
        caasDeployList.add(this.labelUtil(Dict.CAASHFK1, Dict.CNHFK1, hfk1CaasDeploymentCount));
        caasDeployList.add(this.labelUtil(Dict.CAASHFK2, Dict.CNHFK2, hfk2CaasDeploymentCount));

        List caasPodList = new ArrayList();
        caasPodList.add(this.labelUtil(Dict.CAASSHK1, Dict.CNSHK1, shk1CaasPodCount));
        caasPodList.add(this.labelUtil(Dict.CAASSHK2, Dict.CNSHK2, shk2CaasPodCount));
        caasPodList.add(this.labelUtil(Dict.CAASHFK2, Dict.CNHFK1, hfk1CaasPodCount));
        caasPodList.add(this.labelUtil(Dict.CAASHFK2, Dict.CNHFK2, hfk2CaasPodCount));

        List sccDeployList = new ArrayList();
        sccDeployList.add(this.labelUtil(Dict.SCCSHK1, Dict.CNSHK1, shk1SccDeployCount));
        sccDeployList.add(this.labelUtil(Dict.SCCSHK2, Dict.CNSHK2, shk2SccDeployCount));
        sccDeployList.add(this.labelUtil(Dict.SCCHFK1, Dict.CNHFK1, hfk1SccDeployCount));
        sccDeployList.add(this.labelUtil(Dict.SCCHFK2, Dict.CNHFK2, hfk2SccDeployCount));

        List sccPodList = new ArrayList();
        sccPodList.add(this.labelUtil(Dict.SCCSHK1, Dict.CNSHK1, shk1SccPodCount));
        sccPodList.add(this.labelUtil(Dict.SCCSHK2, Dict.CNSHK2, shk2SccPodCount));
        sccPodList.add(this.labelUtil(Dict.SCCHFK1, Dict.CNHFK1, hfk1SccPodCount));
        sccPodList.add(this.labelUtil(Dict.SCCHFK2, Dict.CNHFK2, hfk2SccPodCount));

        LinkedHashMap res = new LinkedHashMap();
        res.put(Dict.CAASDEPLOYCOUNT, caasDeploymentCount);
        res.put(Dict.CAASPODCOUNT, caasPodCount);
        res.put(Dict.SCCDEPLOYCOUNT, sccDeployCount);
        res.put(Dict.SCCPODCOUNT, sccPodCount);
        res.put(Dict.CAASDEPLOYDETAIL, caasDeployList);
        res.put(Dict.CAASPODDETAIL, caasPodList);
        res.put(Dict.SCCDEPLOYDETAIL, sccDeployList);
        res.put(Dict.SCCPODDETAIL, sccPodList);
        return res;
    }

    @Override
    public Map queryClusterInfo(String deploy_name) throws Exception {
        List<CaasDeployment> caasDeployments = caasDeploymentDao.queryCaasDeploymentCondition(deploy_name);
        List<SccDeploy> sccDeploys = sccDeploymentDao.getSccDeployCondition(deploy_name);

        if(CommonUtils.isNullOrEmpty(caasDeployments) && CommonUtils.isNullOrEmpty(sccDeploys)){
            throw new FdevException(ErrorConstants.DEPLOYMENT_NULL);
        }
        Map caas = new HashMap();
        Map scc = new HashMap();
        if(!CommonUtils.isNullOrEmpty(caasDeployments)){
            Set<String> clusters = new HashSet<>();
            Set<String> vlans = new HashSet<>();
            for(CaasDeployment deployment : caasDeployments){
                if(deployment.getNamespace().indexOf("gray") >= 0){
                    clusters.add(deployment.getCluster()+"-gray");
                }else{
                    clusters.add(deployment.getCluster());
                }
                caas.put(Dict.CLUSTERS, this.handleCaasCluster(clusters));
                if(deployment.getArea().indexOf("biz") >= 0){
                    vlans.add("biz");
                }else {
                    vlans.add("ebk");
                }
                caas.put(Dict.VLANS, this.handleVlans(vlans));
            }
        }
        if(!CommonUtils.isNullOrEmpty(sccDeploys)){
            Set<String> namespaces = new HashSet<>();
            for (SccDeploy deploy : sccDeploys){
                Set<String> vlans = new HashSet<>();
                List<SccPod> pods = this.sccPodDao.queryPodsByCondition(deploy.getNamespace_code(),deploy.getResource_code());
                if(!CommonUtils.isNullOrEmpty(pods)){
                    namespaces.add(deploy.getNamespace_code());
                    vlans = this.getSccVlans(pods);
                    scc.put(Dict.NAMESPACES, this.handleSccCluster(namespaces));
                    scc.put(Dict.VLANS, this.handleVlans(vlans));
                }

            }
        }
        LinkedHashMap res = new LinkedHashMap();
        res.put(Dict.CAAS, caas);
        res.put(Dict.SCC, scc);
        return res;
    }

    @Override
    public Map getCaasDeploymentDetail(String deploy_name, String cluster, String vlan ) throws Exception {
        CaasDeployment deployment = this.caasDeploymentDao.queryCaasDeploymentCondition(deploy_name, cluster, vlan);
        if(CommonUtils.isNullOrEmpty(deployment)){
            return null;
        }
        String deployName = deploy_name;
        String clusterName = deployment.getCluster();
        String namespaceName = deployment.getNamespace();

        List<CaasPod> pods = this.caasPodDao.queryCaasPodCondition(deployName, clusterName, namespaceName);

        LinkedHashMap res = new LinkedHashMap();
        Map deploy = this.bulidCassDeploy(deployment);
        List<Map> run = this.buildCaasRun(pods);
        List<Map> storage  = this.buildCaasStorage(deployment);
        Map config  = this.buildCaasConfig(deployment);
        String yaml = this.buildCaasYaml(deployment);

        res.put(Dict.DEPLOY, deploy);
        res.put(Dict.RUN, run);
        res.put(Dict.STORAGE, storage);
        res.put(Dict.CONFIG, config);
        res.put(Dict.YAML, yaml);
        return res;
    }

    @Override
    public Map getSccDeploymentDetail(String deploy_name, String namespace, String vlan) throws Exception {

        SccDeploy sccDeploy = this.sccDeploymentDao.getSccDeployCondition(namespace, deploy_name);
        if (CommonUtils.isNullOrEmpty(sccDeploy)){
            return null;
        }
        List<SccPod> pods = this.sccPodDao.queryPodsByCondition(sccDeploy.getNamespace_code(), sccDeploy.getResource_code());

        if(CommonUtils.isNullOrEmpty(pods)){
            return null;
        }

        Set vlans = this.getSccVlans(pods);

        if (CommonUtils.isNullOrEmpty(vlans) || !vlans.contains(vlan)){
           return null;
        }

        Map deployment = this.yamlString2Map(sccDeploy.getYaml(), sccDeploy.getResource_code());

        Map deploy = new LinkedHashMap();
        List<Map> storage = new LinkedList<Map>();
        Map config = new LinkedHashMap();
        if(!CommonUtils.isNullOrEmpty(deployment)){
            deploy = this.buildSccDeploy(deployment, sccDeploy);
            storage = this.buildSccStorage(deployment);
            config = this.buildSccConfig(deployment);
        }
        List<Map> run = this.buildSccRun(pods);

        LinkedHashMap res = new LinkedHashMap();
        res.put(Dict.DEPLOY, deploy);
        res.put(Dict.RUN, run);
        res.put(Dict.STORAGE, storage);
        res.put(Dict.CONFIG, config);
        res.put(Dict.YAML, sccDeploy.getYaml());
       return res;
    }

    @Override
    public Map getDeployment(String page, String pageSize, Map params) throws Exception {
        int pagestart = Integer.parseInt(page);
        int pagesize = Integer.parseInt(pageSize);
        String key = (String) params.get(Dict.TYPE);
        String value = (String) params.get(Dict.VALUE);

        //CAAS模糊查询，并且去重
        Set<String> caas = new TreeSet<>();
        List<CaasDeployment> caasDeployments = caasDeploymentDao.fuzzyQueryCaasDeployment(key, value);
        if (!CommonUtils.isNullOrEmpty(caasDeployments)){
            for (CaasDeployment item : caasDeployments){
                caas.add(item.getDeployment());
            }
        }
        //SCC模糊查询，并且去重
        Set<String> scc = new TreeSet<>();
        List<SccDeploy> sccDeploys = this.fuzzySccDeploys(key, value);
        if (!CommonUtils.isNullOrEmpty(sccDeploys)){
            List deploys = this.sccPodDao.getDistinctField(Dict.OWNERCODE);
            for (SccDeploy item : sccDeploys){
                //判断pod是否启动
                if(deploys.contains(item.getResource_code())){
                    scc.add(item.getResource_code());
                }
            }
        }

        //获取caas,scc所有模糊查询后,添加对应的平台信息
        LinkedHashMap<String, String> deployments  = this.addPlatInfo(caas, scc);

        //所有应用的名字列表
        List<String> deploymentNames = new ArrayList<>();
        for (String item : deployments.keySet()){
            deploymentNames.add(item);
        }

        //获取人员信息
        Map userInfo = this.getUserInfo(deploymentNames);

        //封装所有的应用名、平台、人员信息
        ArrayList allDeployments = new ArrayList();
        for (String deployName : deployments.keySet()){
            LinkedHashMap deploymentInfo = new LinkedHashMap();
            deploymentInfo.put(Dict.DEPLOYNAME, deployName);
            String gruop = "";
            List dev_managers = new ArrayList();
            List spdb_managers = new ArrayList();
            if(!CommonUtils.isNullOrEmpty(userInfo)){
                if(userInfo.containsKey(deployName)){
                   Map user = (Map) userInfo.get(deployName);
                   gruop = (String) user.get(Dict.GROUP);
                   dev_managers = (List) user.get(Dict.DEVMANAGERS);
                   spdb_managers = (List) user.get(Dict.SPDBMANAGERS);
                }
            }
            deploymentInfo.put(Dict.GROUP, gruop);
            deploymentInfo.put(Dict.DEVMANAGERS, dev_managers);
            deploymentInfo.put(Dict.SPDBMANAGERS, spdb_managers);
            deploymentInfo.put(Dict.PLATFORM, deployments.get(deployName));
            allDeployments.add(deploymentInfo);
        }

        LinkedHashMap res = new LinkedHashMap();

        //分页
        int total = allDeployments.size();
        int startIndex = (pagestart-1) * pagesize;
        int endIndex = startIndex + pagesize;
        List deploy_list = new ArrayList<>();
        for (int i = startIndex ; i < endIndex ; i++){
            if(i <= allDeployments.size()-1){
                deploy_list.add(allDeployments.get(i));
            }
        }
        res.put(Dict.DEPLOYLIST, deploy_list);
        res.put(Dict.TOTAL, total);
        return res;
    }


    private List handleCaasCluster(Set clusters){
        List res = new ArrayList();
        for(Object item :clusters){
            LinkedHashMap temp = new LinkedHashMap();
            switch (item.toString()){
                case Dict.MBPERUZ11:
                case Dict.SHK1:
                    temp.put(Dict.LABEl, "上海K1");
                    temp.put(Dict.VALUE, item.toString());
                    break;
                case Dict.MBPERUZ12:
                case Dict.SHK2:
                    temp.put(Dict.LABEl, "上海K2");
                    temp.put(Dict.VALUE, item.toString());
                    break;
                case Dict.MBPERUZ10:
                case Dict.SHK1GRAY :
                    temp.put(Dict.LABEl, "上海K1灰度");
                    temp.put(Dict.VALUE, item.toString());
                    break;
                case Dict.SHK2GRAY :
                    temp.put(Dict.LABEl, "上海K2灰度");
                    temp.put(Dict.VALUE, item.toString());
                    break;
                case Dict.MBPERUZ30:
                case Dict.HFK1GRAY:
                    temp.put(Dict.LABEl, "合肥K1灰度");
                    temp.put(Dict.VALUE, item.toString());
                    break;
                case Dict.HFK2GRAY:
                    temp.put(Dict.LABEl, "合肥K2灰度");
                    temp.put(Dict.VALUE, item.toString());
                    break;
                case Dict.MBPERUZ31:
                case Dict.HFK1:
                    temp.put(Dict.LABEl, "合肥K1");
                    temp.put(Dict.VALUE, item.toString());
                    break;
                case Dict.MBPERUZ32:
                case Dict.HFK2:
                    temp.put(Dict.LABEl, "合肥K2");
                    temp.put(Dict.VALUE, item.toString());
                    break;
            }
            if (item.toString().equals(Dict.SHK1)){
                res.add(0, temp);
            }else{
                res.add(temp);
            }
        }
        return res;
    };

    private List handleSccCluster(Set namespaces){
        List res = new ArrayList();
        for(Object item :namespaces){
            LinkedHashMap temp = new LinkedHashMap();
            temp.put(Dict.LABEl, item.toString());
            temp.put(Dict.VALUE, item.toString());
            res.add(temp);
        }
        return res;
    }

    private Map labelUtil(String en, String cn, Long value){
        Map res = new LinkedHashMap();
        res.put(Dict.NAME, cn);
        res.put(Dict.NAMEEN, en);
        res.put(Dict.VALUE, value);
        return res;
    }

    private List handleVlans(Set vlans){
        List res = new ArrayList();
        for(Object item :vlans){
            LinkedHashMap temp = new LinkedHashMap();
            switch (item.toString()) {
                case "ebk":
                    temp.put(Dict.LABEl, "网银网段");
                    temp.put(Dict.VALUE, item.toString());
                    break;
                case "biz":
                    temp.put(Dict.LABEl, "业务网段");
                    temp.put(Dict.VALUE, item.toString());
            }
            res.add(temp);
        }
        return res;
    }

    private Set getSccVlans(List<SccPod> pods){
        Set res = new HashSet();
        if(!CommonUtils.isNullOrEmpty(pods)){
            for (SccPod item : pods){
                if(Arrays.asList(Dict.SCCBIZ).contains(item.getCluster_code())){
                   res.add("biz");
                }
                if(Arrays.asList(Dict.SCCDMZ).contains(item.getCluster_code())){
                    res.add("ebk"); //ebk, dmz都表示网银
                }
            }
        }
        return res;
    }

    private Map bulidCassDeploy(CaasDeployment deployment){
        Map res = new LinkedHashMap();
        res.put(Dict.NAMESPACE, deployment.getNamespace());
        res.put(Dict.LAST_MODIFIED_DATE, deployment.getLast_modified_date());
        String tag = deployment.getTag().substring(2,deployment.getTag().length()-2);
        res.put(Dict.TAG, tag);
        res.put(Dict.CPULIMITS, deployment.getCpu_limits().substring(2, deployment.getCpu_limits().length()-2));
        res.put(Dict.CPUREQUESTS, deployment.getCpu_requests().substring(2, deployment.getCpu_requests().length()-2));
        res.put(Dict.MEMORYLIMITS, deployment.getMemory_limits().substring(2, deployment.getMemory_limits().length()-2));
        res.put(Dict.MEMORYREQUESTS, deployment.getMemory_requests().substring(2, deployment.getMemory_requests().length()-2));
        res.put(Dict.REPLICAS, deployment.getReplicas());
        return  res;
    }

    private List buildCaasRun(List<CaasPod> caasPods){
        List<Map> res = new ArrayList<>();
        if(!CommonUtils.isNullOrEmpty(caasPods)){
            for(CaasPod item : caasPods){
                LinkedHashMap temp = new LinkedHashMap();
                temp.put(Dict.HOSTNAME, item.getHostname());
                temp.put(Dict.NAME, item.getName());
                temp.put(Dict.IP, item.getIp());
                res.add(temp);
            }
        }
        return res;
    }

    private List buildCaasStorage(CaasDeployment deployment){
        //挂载点
        String volumemounts = deployment.getVolumemounts().substring(1, deployment.getVolumemounts().length()-1);
        //挂载卷
        String volumes = deployment.getVolumes();
        Map<String, Object> volumesMap = new HashMap<>();
        //获取挂载卷
        List<Map<String, Object>> storgeList = new ArrayList<>();
        if (StringUtils.isNotBlank(volumes)) {
            JSONArray volumesList = JSONObject.parseArray(volumes);
            for (int k = 0; k < volumesList.size(); k++) {
                String getVolumes = (String) volumesList.get(k);
                String[] slipVolumes = getVolumes.split("\"");
                if (Dict.NAME.equals(slipVolumes[1])) {
                    volumesMap.put(slipVolumes[3], slipVolumes[9]);
                } else if (Dict.NAME.equals(slipVolumes[11])) {
                    volumesMap.put(slipVolumes[13], slipVolumes[5]);
                }
            }
        }
        //获取挂载点
        if (StringUtils.isNotBlank(volumemounts)) {
            JSONArray volumemountsList = JSONObject.parseArray(volumemounts);
            for (int k = 0; k < volumemountsList.size(); k++) {
                JSONObject volumemountsObject = volumemountsList.getJSONObject(k);
                Map<String, Object> storgeMap = new HashMap<>();//存储信息
                String name = (String) volumemountsObject.get(Dict.NAME);
                storgeMap.put(Dict.NAME, name);//名称
                if(volumemountsObject.containsKey(Dict.SUBPATH)){ //subPath
                    String subPath = (String) volumemountsObject.get(Dict.SUBPATH);
                    storgeMap.put(Dict.SUBPATH, subPath);
                }else{
                    storgeMap.put(Dict.SUBPATH, "");
                }
                if (volumesMap.containsKey(name)) {
                    storgeMap.put(Dict.VOLUMES, volumesMap.get(name));//挂载卷
                }
                storgeMap.put(Dict.VOLUMEMOUNTS, volumemountsObject.get(Dict.MOUNTPATH));//容器内挂载点
                storgeList.add(storgeMap);
            }
        }
        return storgeList;
    }

    private Map buildCaasConfig(CaasDeployment deployment){
        Map<String, Object> configMap = new LinkedHashMap<>();
        //配置中心
        List<String> configCentorList = null;
        //注册中心
        List<String> eurekaList = null;
        //所有的环境变量
        String env = deployment.getEnv().substring(1, deployment.getEnv().length()-1);
        List<Map<String, Object>> envList = new ArrayList();
        if (!CommonUtils.isNullOrEmpty(env) && StringUtils.isNotBlank(env) && !env.equals("[null]")) {
            JSONArray getEnvList = JSONObject.parseArray(env);
            for (int k = 0; k < getEnvList.size(); k++) {
                Map<String, Object> envMap = new HashMap<>();
                JSONObject envObject = getEnvList.getJSONObject(k);
                String name = (String) envObject.get(Dict.NAME);
                String value = (String) envObject.get(Dict.VALUE);
                if (!Dict.SPRING_CLOUD_CONFIG_URI.equals(name) && !Dict.EUREKA_CLIENT_SERVICEURL_DEFAULTZONE.equals(name)) {
                    envMap.put(Dict.NAME, name);
                    envMap.put(Dict.VALUE, value);
                    envList.add(envMap);
                } else if (Dict.SPRING_CLOUD_CONFIG_URI.equals(name)) {
                    configCentorList = Arrays.asList(value.split(","));
                } else if (Dict.EUREKA_CLIENT_SERVICEURL_DEFAULTZONE.equals(name)) {
                    eurekaList = Arrays.asList(value.split(","));
                }
            }
        }
        String envfrom = deployment.getEnvfrom().substring(1, deployment.getEnvfrom().length()-1);
        List<Map<String, Object>> envfromList = new ArrayList();
        if (!CommonUtils.isNullOrEmpty(envfrom) && StringUtils.isNotBlank(envfrom) && !envfrom.equals("[null]") && !envfrom.equals("[null, null]")) {
            JSONArray getEnvFromList = JSON.parseArray(envfrom);
            for(int k = 0; k < getEnvFromList.size(); k++){
                String item = getEnvFromList.getString(k);
                envfromList.add(JSON.parseObject(item, Map.class));
            }
        }
        configMap.put(Dict.CONFIGCENTOR, configCentorList);
        configMap.put(Dict.EUREKA, eurekaList);
        configMap.put(Dict.ENV, envList);
        configMap.put(Dict.ENVFROM, envfromList);

        //DNS配置
        List<String> DNSconfigList = new ArrayList<String>();
        String DNSconfig = deployment.getDnsconfig();
        if (StringUtils.isNotBlank(DNSconfig)) {
            JSONArray DNSconfigListOne = JSONObject.parseArray(DNSconfig);
            for (int k = 0; k < DNSconfigListOne.size(); k++) {
                DNSconfigList.add((String) DNSconfigListOne.get(k));
            }
        }
        configMap.put(Dict.DNSPOLICY, deployment.getDnspolicy().substring(1, deployment.getDnspolicy().length()-1));
        configMap.put(Dict.DNSCONFIG, DNSconfigList);

        //预留IP
        List<String> allocatedIpSegmentList = new ArrayList<String>();
        String allocatedIpSegment = deployment.getAllocated_ip_segment();
        if (StringUtils.isNotBlank(allocatedIpSegment)) {
            String[] allocatedIpSegmentString = allocatedIpSegment.split("/");
            for (int k = 0; k < allocatedIpSegmentString.length; k++) {
                allocatedIpSegmentList.add(allocatedIpSegmentString[k]);
            }
        }
        configMap.put(Dict.ALLOCATED_IP_SEGMENT, allocatedIpSegmentList);
        //预停止策略
        String prestop =deployment.getPrestop();
        String prestopString = "";
        if (StringUtils.isNotBlank(prestop) && !prestop.equals("\"\"") && !prestop.equals("[null]")) {
            JSONObject prestopOne = JSONObject.parseObject(prestop.substring(1, prestop.length()-1));
            Map<String, Object> execMap = (Map) prestopOne.get("exec");
            List<String> commandList = (List) execMap.get("command");
            for (int k = 0; k < commandList.size(); k++) {
                prestopString = prestopString + commandList.get(k);
                if (k != commandList.size() - 1) {
                    prestopString = prestopString + " ";
                }
            }
        }
        configMap.put(Dict.PRESTOP, prestopString);//预停止策略
        configMap.put(Dict.STRATEGYTYPE, deployment.getStrategytype());//发布策略

        List<Map> imagePulList = new ArrayList();
        if (!CommonUtils.isNullOrEmpty(deployment.getImagepullsecrets()) && StringUtils.isNotBlank(deployment.getImagepullsecrets())) {
            JSONArray imagePullSecrets = JSON.parseArray(deployment.getImagepullsecrets());
            for(int k = 0; k < imagePullSecrets.size(); k++){
                String item = imagePullSecrets.getString(k);
                imagePulList.add(JSON.parseObject(item, Map.class));
            }
        }
        configMap.put(Dict.IMAGEPULLSECRETS, imagePulList);//镜像拉取策略

        //域名关联
        List<Map<String, String>> hostalias = new ArrayList<>();
        String hostaliasString = deployment.getHostalias();
        if (StringUtils.isNotBlank(hostaliasString)) {
            JSONArray hostaliasList = JSONObject.parseArray(hostaliasString);
            for (int j = 0; j < hostaliasList.size(); j++) {
                //获取对应的ip和域名
                JSONObject ip_host = JSONObject.parseObject(hostaliasList.get(j).toString());
                Map<String, String> hostaliasMap = new HashMap<>();
                hostaliasMap.put(Dict.IP, ip_host.get(Dict.IP).toString());
                //域名去掉多余的符号
                hostaliasMap.put(Dict.HOSTNAME, ip_host.get(Dict.HOSTNAMES).toString().replace("[\"", "").replace("\"]", ""));
                hostalias.add(hostaliasMap);
            }
        }
        configMap.put(Dict.HOSTALIAS, hostalias);
        return configMap;
    }

    private String buildCaasYaml(CaasDeployment deployment){
        Map<String, Object> deploymentMap = JSON.parseObject(JSON.toJSONString(deployment, SerializerFeature.WriteMapNullValue, SerializerFeature.QuoteFieldNames), Map.class);
        //1.判断每个字段是否为空含义，是就让它的值为null
        for(String key : deploymentMap.keySet()){
            Object o = deploymentMap.get(key);
            if(CommonUtils.isNullOrEmpty(o) || o.equals("[null]")){
                deploymentMap.put(key, null);
            }
        }
        //2.去中括号
        Map tempData = this.caasYamlService.cutMidCurly(deploymentMap);
        //3.去双引号
        Map data = this.caasYamlService.cutDoubleQuatation(tempData);
        //4.生成yaml的Map格式
        Map resObjMap = this.caasYamlService.generateYaml(data, deployment.getDeployment());

        Map spec= (Map) ((Map) ((Map) resObjMap.get("spec")).get("template")).get("spec");
        if(spec.get("hostAliases").equals("######")){
            spec.remove("hostAliases");
            ((Map)((Map)resObjMap.get("spec")).get("template")).put("spec",spec);
        }
        String res = this.Map2yamString(resObjMap);


        return res;
    }

    private Map yamlString2Map(String yaml, String deploy_name){
        //将yaml字符串转换成json输出
        LinkedHashMap<String, Object> yamlMap = new LinkedHashMap<>();
        try {
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml wYaml = new Yaml();
            yamlMap = wYaml.load(yaml);
        }catch (Exception e){
            logger.error(deploy_name+":yaml字符串转map失败" + e.getMessage());
        }
        return yamlMap;
    }

    private String Map2yamString(Map map){
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml();
        String res = yaml.dumpAsMap(map);
        return res;
    }

    private Map buildSccDeploy(Map deployment, SccDeploy deploy){
        Map res = new LinkedHashMap();
        res.put(Dict.NAMESPACE, deploy.getNamespace_code());
        res.put(Dict.LAST_MODIFIED_DATE, deploy.getLast_modified_date());

        Map container = (Map) ((List)((Map) ((Map) ((Map) deployment.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0);
        String tag = (String) container.get(Dict.IMAGE);
        res.put(Dict.TAG, tag);
        Map resources = (Map) container.get(Dict.RESOURCES);
        Map limits = (Map) resources.get(Dict.LIMITS);
        Map requests = (Map) resources.get(Dict.REQUESTS);


        if (!(limits.get(Dict.CPU) instanceof  String)){
            Map cpuLimits = (Map) limits.get(Dict.CPU);
            Map memoryLimits = (Map) limits.get(Dict.MEMORY);
            Map cpuRequests = (Map) requests.get(Dict.CPU);
            Map memoryRequests = (Map) requests.get(Dict.MEMORY);
            res.put(Dict.CPULIMITS, (String)cpuLimits.get("amount") + (String) cpuLimits.get("format"));
            res.put(Dict.MEMORYLIMITS, (String)memoryLimits.get("amount") + (String) memoryLimits.get("format"));
            res.put(Dict.CPUREQUESTS, (String)cpuRequests.get("amount") + (String) cpuRequests.get("format"));
            res.put(Dict.MEMORYREQUESTS, (String)memoryRequests.get("amount") + (String) memoryRequests.get("format"));
        }else {
            res.put(Dict.CPULIMITS, limits.get(Dict.CPU));
            res.put(Dict.MEMORYLIMITS, limits.get(Dict.MEMORY));
            res.put(Dict.CPUREQUESTS, requests.get(Dict.CPU));
            res.put(Dict.MEMORYREQUESTS, requests.get(Dict.MEMORY));
        }
        res.put(Dict.REPLICAS, ((Map)deployment.get(Dict.SPEC)).get(Dict.REPLICAS).toString());
        return  res;
    }

    private List buildSccRun(List<SccPod> sccPods){
        List<Map> res = new ArrayList<>();
        if(!CommonUtils.isNullOrEmpty(sccPods)){
            for(SccPod item : sccPods){
                LinkedHashMap temp = new LinkedHashMap();
                temp.put(Dict.HOSTNAME, item.getNode_name());
                temp.put(Dict.NAME, item.getPod_code());
                temp.put(Dict.IP, item.getPod_ip());
                res.add(temp);
            }
        }
        return res;
    }

    private List<Map> buildSccStorage(Map deployment){
        Map container = (Map) ((List)((Map) ((Map) ((Map) deployment.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0);
        List volumes = (List) ((Map)((Map)((Map) deployment.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.VOLUMES);
        List volumemounts = (List) container.get(Dict.VOLUMEMOUNTSUP);
        ArrayList<Map> res = new ArrayList<>();
        for(Object item : volumemounts){
            Map itemMap = (Map) item;
            for(Object iitem : volumes){
               Map iitemMap = (Map) iitem;
               if(itemMap.get(Dict.NAME).equals(iitemMap.get(Dict.NAME))){
                   Map temp = new LinkedHashMap();
                   temp.put(Dict.NAME, itemMap.get(Dict.NAME));
                   temp.put(Dict.VOLUMEMOUNTS, itemMap.get(Dict.MOUNTPATH));
                   if(iitemMap.containsKey(Dict.PERSISTENTVOLUMECLAIM)){
                       temp.put(Dict.VOLUMES, ((Map)iitemMap.get(Dict.PERSISTENTVOLUMECLAIM)).get(Dict.CLAIMNAME));
                   }
                   if(iitemMap.containsKey(Dict.HOSTPATH)){
                       temp.put(Dict.VOLUMES, ((Map)iitemMap.get(Dict.HOSTPATH)).get(Dict.PATH));
                   }
                   if(itemMap.containsKey(Dict.SUBPATH)){
                       temp.put(Dict.SUBPATH, itemMap.get(Dict.SUBPATH));
                   }
                   res.add(temp);
               }
            }
        }
        return res;
    }

    private Map buildSccConfig(Map deployment){
        Map res = new LinkedHashMap();
        Map container = (Map) ((List)((Map) ((Map) ((Map) deployment.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC)).get(Dict.CONTAINERS)).get(0);
        List env = new ArrayList<>();
        List envfrom = new ArrayList<>();
        if (container.containsKey(Dict.ENV)){
            env = (List) container.get(Dict.ENV);
        }
        if (container.containsKey(Dict.ENVFROMUP)){
            envfrom = (List) container.get(Dict.ENVFROMUP);
        }
        Map templateSpec = (Map) ((Map) ((Map) deployment.get(Dict.SPEC)).get(Dict.TEMPLATE)).get(Dict.SPEC);
        String dnspolicy = "";
        if (templateSpec.containsKey(Dict.DNSPOLICYUP)){
            dnspolicy = (String) templateSpec.get(Dict.DNSPOLICYUP);
        }
        Map strtegytype = new LinkedHashMap();

        Map spec = (Map) deployment.get(Dict.SPEC);
        if (spec.containsKey(Dict.STRATEGY)){
            strtegytype = (Map) spec.get(Dict.STRATEGY);
        }
        res.put(Dict.ENV, env);
        res.put(Dict.ENVFROM, envfrom);
        res.put(Dict.DNSPOLICY, dnspolicy);
        res.put(Dict.STRATEGYTYPE, strtegytype);
        return res;
    }

    //scc所有应用进行模糊查询
    private List fuzzySccDeploys(String key, String value){
        //key为空或者value为空返回所有数据,key为空，或应用名，或租户可以直接数据库模糊查询，不用走全量
        if(CommonUtils.isNullOrEmpty(key) || CommonUtils.isNullOrEmpty(value) || key.equals(Dict.DEPLOYNAME) || key.equals(Dict.NAMESPACE)){
            if(key.equals(Dict.DEPLOYNAME)){
                key = Dict.RESOURCECODE;
            }
            if(key.equals(Dict.NAMESPACE)){
                key = Dict.NAMESPACECODE;
            }
            List res = this.sccDeploymentDao.fuzzyQueryCaasDeployment(key, value);
            return res;
        }

        //耗时的地方
        List<SccDeploy> sccDeploys = sccDeploymentDao.getAllSccDeploy();
        Map<String, Map>  deployObjectAndMap= new LinkedHashMap();
        for (SccDeploy sccDeploy : sccDeploys){
            Map sccDeployMap = this.yamlString2Map(sccDeploy.getYaml(), sccDeploy.getResource_code());
            if (!CommonUtils.isNullOrEmpty(sccDeployMap) && sccDeployMap.get(Dict.KIND).equals("Deployment")){
                String primaryKeyScc = sccDeploy.getNamespace_code()+"###"+sccDeploy.getResource_code(); //scc_deploy以namespace_code+resource_code为主键
                Map temp = new LinkedHashMap();
                temp.put("sccDeploy", sccDeploy);
                temp.put("sccMap", sccDeployMap);
                deployObjectAndMap.put(primaryKeyScc,temp);
            }
        }

        List res = new ArrayList();

        if (!CommonUtils.isNullOrEmpty(deployObjectAndMap)){
            //cpu_limits
            if(key.equals(Dict.CPULIMITS)){
                for (String primaryKeyScc : deployObjectAndMap.keySet()){
                    SccDeploy sccDeploy = (SccDeploy) deployObjectAndMap.get(primaryKeyScc).get("sccDeploy");
                    Map sccDeployMap = (Map) deployObjectAndMap.get(primaryKeyScc).get("sccMap");
                    Map deployInfo = this.buildSccDeploy(sccDeployMap, sccDeploy);
                    String cpu_limits = (String) deployInfo.get(Dict.CPULIMITS);
                    if(cpu_limits.indexOf(value) >= 0){
                        res.add(sccDeploy);
                    }
                }
            }
            //cpu_requests
            if(key.equals(Dict.CPUREQUESTS)){
                for (String primaryKeyScc: deployObjectAndMap.keySet()){
                    SccDeploy sccDeploy = (SccDeploy) deployObjectAndMap.get(primaryKeyScc).get("sccDeploy");
                    Map sccDeployMap = (Map) deployObjectAndMap.get(primaryKeyScc).get("sccMap");
                    Map deployInfo = this.buildSccDeploy(sccDeployMap, sccDeploy);
                    String memory_limits = (String) deployInfo.get(Dict.CPUREQUESTS);
                    if(memory_limits.indexOf(value) >= 0){
                        res.add(sccDeploy);
                    }
                }
            }
            //memory_limits
            if(key.equals(Dict.MEMORYLIMITS)){
                for (String primaryKeyScc: deployObjectAndMap.keySet()){
                    SccDeploy sccDeploy = (SccDeploy) deployObjectAndMap.get(primaryKeyScc).get("sccDeploy");
                    Map sccDeployMap = (Map) deployObjectAndMap.get(primaryKeyScc).get("sccMap");
                    Map deployInfo = this.buildSccDeploy(sccDeployMap, sccDeploy);
                    String memory_limits = (String) deployInfo.get(Dict.MEMORYLIMITS);
                    if(memory_limits.indexOf(value) >= 0){
                        res.add(sccDeploy);
                    }
                }
            }
            //memory_requests
            if(key.equals(Dict.MEMORYREQUESTS)){
                for (String primaryKeyScc: deployObjectAndMap.keySet()){
                    SccDeploy sccDeploy = (SccDeploy) deployObjectAndMap.get(primaryKeyScc).get("sccDeploy");
                    Map sccDeployMap = (Map) deployObjectAndMap.get(primaryKeyScc).get("sccMap");
                    Map deployInfo = this.buildSccDeploy(sccDeployMap, sccDeploy);
                    String memory_requests = (String) deployInfo.get(Dict.MEMORYREQUESTS);
                    if(memory_requests.indexOf(value) >= 0){
                        res.add(sccDeploy);
                    }
                }
            }
            //env
            if(key.equals(Dict.ENV)){
                for (String primaryKeyScc: deployObjectAndMap.keySet()){
                    SccDeploy sccDeploy = (SccDeploy) deployObjectAndMap.get(primaryKeyScc).get("sccDeploy");
                    Map sccDeployMap = (Map) deployObjectAndMap.get(primaryKeyScc).get("sccMap");
                    Map configInfo = this.buildSccConfig(sccDeployMap);
                    List env = (List)configInfo.get(Dict.ENV);
                    String envStr = JSON.toJSONString(env);
                    if(envStr.indexOf(value) >= 0){
                        res.add(sccDeploy);
                    }
                }
            }
            //envfrom
            if(key.equals(Dict.ENVFROM)){
                for (String primaryKeyScc: deployObjectAndMap.keySet()){
                    SccDeploy sccDeploy = (SccDeploy) deployObjectAndMap.get(primaryKeyScc).get("sccDeploy");
                    Map sccDeployMap = (Map) deployObjectAndMap.get(primaryKeyScc).get("sccMap");
                    Map configInfo = this.buildSccConfig(sccDeployMap);
                    List envfrom = (List) configInfo.get(Dict.ENVFROM);
                    String envfromStr = JSON.toJSONString(envfrom);
                    if(envfromStr.indexOf(value) >=0){
                        res.add(sccDeploy);
                    }
                }
            }
            //dnspolicy
            if (key.equals(Dict.DNSPOLICY)){
                for (String primaryKeyScc: deployObjectAndMap.keySet()){
                    SccDeploy sccDeploy = (SccDeploy) deployObjectAndMap.get(primaryKeyScc).get("sccDeploy");
                    Map sccDeployMap = (Map) deployObjectAndMap.get(primaryKeyScc).get("sccMap");
                    Map configInfo = this.buildSccConfig(sccDeployMap);
                    String dnspolicy = (String) configInfo.get(Dict.DNSPOLICY);
                    if(dnspolicy.indexOf(value) >= 0){
                        res.add(sccDeploy);
                    }
                }
            }
            //volumes
            if(key.equals(Dict.VOLUMES)){
                for (String primaryKeyScc: deployObjectAndMap.keySet()){
                    SccDeploy sccDeploy = (SccDeploy) deployObjectAndMap.get(primaryKeyScc).get("sccDeploy");
                    Map sccDeployMap = (Map) deployObjectAndMap.get(primaryKeyScc).get("sccMap");
                    List<Map> storageInfo = this.buildSccStorage(sccDeployMap);
                    if(!CommonUtils.isNullOrEmpty(storageInfo)){
                        for (Map item : storageInfo){
                            String volumes = (String) item.get(Dict.VOLUMES);
                            String volumesStr = JSON.toJSONString(volumes);
                            if(volumesStr.indexOf(value) >=0){
                                res.add(sccDeploy);
                                break;
                            }
                        }
                    }
                }
            }
            //volumemounts
            if(key.equals(Dict.VOLUMEMOUNTS)){
                for (String primaryKeyScc: deployObjectAndMap.keySet()){
                    SccDeploy sccDeploy = (SccDeploy) deployObjectAndMap.get(primaryKeyScc).get("sccDeploy");
                    Map sccDeployMap = (Map) deployObjectAndMap.get(primaryKeyScc).get("sccMap");
                    List<Map> storageInfo = this.buildSccStorage(sccDeployMap);
                    if(!CommonUtils.isNullOrEmpty(storageInfo)){
                        for (Map item : storageInfo){
                            String volumemounts = (String) item.get(Dict.VOLUMEMOUNTS);
                            String volumemountsStr = JSON.toJSONString(volumemounts);
                            if(volumemountsStr.indexOf(value) >=0){
                                res.add(sccDeploy);
                                break;
                            }
                        }
                    }
                }
            }
            //allocated_ip_segment,scc没有预留IP,直接给空
        }
        return res;
    }

    //获取应用的人员信息
    private Map getUserInfo(List deploy_list){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(Dict.SOURCE,Dict.BACK);
        HttpMethod method = HttpMethod.POST;
        String url = this.fdevUrl+Dict.FDEVUSERPATH;
        if (!CommonUtils.isNullOrEmpty(deploy_list)){
            Map parms = new HashMap();
            parms.put("nameEns", deploy_list);
            HttpEntity requestEntity= new HttpEntity<>(parms, headers);
            ResponseEntity<Map> responseMap = this.restTemplate.exchange(url, method, requestEntity, Map.class);
            Map res = (Map) responseMap.getBody().get(Dict.DATA);
            return res;
        }
        return null;
    }

    //添加平台信息
    private LinkedHashMap<String, String> addPlatInfo(Set<String> caas, Set<String> scc){
        LinkedHashMap<String, String> res = new LinkedHashMap();
        List<String> caasList = caasDeploymentDao.getDistinctField(Dict.DEPLOYMENT);
        List<String> sccList = sccPodDao.getDistinctField(Dict.OWNERCODE);
        for (String item : caas){
            if(sccList.contains(item)){
                res.put(item, Dict.CAAS + "," + Dict.SCC);
            }else{
                res.put(item, Dict.CAAS);
            }
        }
        for (String item : scc){
            if(caasList.contains(item)){
                res.put(item, Dict.CAAS + "," + Dict.SCC);
            }else{
                res.put(item, Dict.SCC);
            }
        }
        return res;
    }
}
