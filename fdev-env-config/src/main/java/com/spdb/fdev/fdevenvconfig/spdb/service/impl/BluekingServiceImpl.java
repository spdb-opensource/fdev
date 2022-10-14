package com.spdb.fdev.fdevenvconfig.spdb.service.impl;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IBluekingDao;
import com.spdb.fdev.fdevenvconfig.spdb.dao.IModelDao;
import com.spdb.fdev.fdevenvconfig.spdb.entity.Blueking;
import com.spdb.fdev.fdevenvconfig.spdb.service.IBluekingService;
import com.spdb.fdev.fdevenvconfig.spdb.service.IRequestService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * @Author: lisy26
 * @date: 2021/1/4 15:42
 * @ClassName: BluekingServiceImpl
 * @Description:
 */
@Service
@RefreshScope
public class BluekingServiceImpl implements IBluekingService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    private IModelDao modelDao;

    @Autowired
    private IBluekingDao bluekingDao;

    @Autowired
    private IRequestService requestService;

    @Value("${bk_app_secret}")
    private String bk_app_secret;
    @Value("${searchInst}")
    private String searchInst;
    @Value("${findInstanceAssociation}")
    private String findInstanceAssociation;

    private Logger logger = LoggerFactory.getLogger(ModelServiceImpl.class);

    /**
     * @Author： lisy26
     * @Description： 同步蓝鲸生产信息.
     * @Date： 2021/1/4 16:41
     * @Param: []
     * @return: java.util.Map
     **/
    @Override
    public void updateBlueking() throws Exception {
        logger.info("开始同步蓝鲸接口数据");
        bluekingDao.dropCollection("blueking");
        logger.info("删除数据库中已有blueking数据成功");

        //调用蓝鲸平台接口，查询网银系统群下所有系统，获取每个系统的bk_inst_id值。
        List<Integer> bkInstIdList = findBkInstId();
        logger.info("获取网银系统群所属系统成功");
        //调用蓝鲸平台接口查询系统的全量deployment，获取每个deployment的bk_asst_inst_id值
        List<Integer> bkAsstInstIdList = findBkAsstInstId(bkInstIdList);
        logger.info("获取网银系统群deployment id成功");
        //调取蓝鲸平台接口，获取deployment的全量详情（去重之后）。
        Set<String> deploymentSet = deploymentDetail(bkAsstInstIdList);
        logger.info("获取网银系统群deployment详细信息成功");
        //用蓝鲸平台接口查询deployment关联的pod；
        podDetail(deploymentSet);
        logger.info("获取网银系统群pod详细信息成功");
        //调用蓝鲸平台接口查询deployment关联的container；
        containerDetail(deploymentSet);
        logger.info("获取网银系统群container详细信息成功");
    }

    /**
     * @Author： lisy26
     * @Description： 查询蓝鲸应用（输入系统名、应用名，进行模糊查询）
     * @Date： 2021/1/18 20:11
     * @Param: [map]
     * @return: java.util.Map
     **/
    @Override
    public Map getAllDeployments(Map map) {
        Map<String, Object> responseMap = new HashMap<>();
        int page = Integer.parseInt((String) map.get(Dict.PAGE));
        int perPage = Integer.parseInt((String) map.get(Dict.PERPAGE));
        int startPage = (page - 1) * perPage;
        String name = (String) map.get(Dict.NAME);
        //模糊查询所有
        List<Blueking> deploymentList = bluekingDao.queryDeploymentList(name);
        //根据应用名进行去重
        List<String> deploymentSet = new ArrayList<>();
        for (int i = 0; i < deploymentList.size(); i++) {
            Blueking blueking = deploymentList.get(i);
            Map dataMap = blueking.getData();
            if (!deploymentSet.contains(dataMap.get(Dict.DEPLOYMENT)))
                deploymentSet.add((String) dataMap.get(Dict.DEPLOYMENT));
        }
        int total = deploymentSet.size();
        responseMap.put(Dict.TOTAL, total);
        //查询CPU和内存相关信息
        List<Blueking> limitList = bluekingDao.queryLimits(deploymentSet);
        List<Map> resultList = new ArrayList<>();
        //分页参数为全部时
        if (perPage == 0) {
            perPage = total;
        }
        for (int i = startPage; i < startPage + perPage && i < deploymentSet.size(); i++) {
            Map<String, Object> resultMap = new HashMap<>();
            //从deployment的信息中查询应用名和系统名
            for (int j = 0; j < deploymentList.size(); j++) {
                Blueking bluekingMap = deploymentList.get(j);
                Map dataMap = bluekingMap.getData();
                //应用名、系统信息、最后修改时间从所有deployment中取，没必要去除灰度租户
                if (dataMap.get(Dict.DEPLOYMENT).equals(deploymentSet.get(i))) {
                    resultMap.put(Dict.DEPLOYMENT, dataMap.get(Dict.DEPLOYMENT));
                    resultMap.put(Dict.SYSNAMEUP, dataMap.get(Dict.SYSNAME));
                    //resultMap.put("replicas", dataMap.get("replicas"));
                    resultMap.put(Dict.LASTMODIFIEDDATE, dataMap.get(Dict.LAST_MODIFIED_DATE));
                    //找到一个即可
                    break;
                }
            }
            //从container的信息中查询cpulimit和内存limit
            for (int j = 0; j < limitList.size(); j++) {
                Blueking containerMap = limitList.get(j);
                Map dataMap = (Map) containerMap.getData();
                //去掉灰度租户
                if (dataMap.get(Dict.DEPLOYMENT).equals(deploymentSet.get(i)) && !dataMap.get(Dict.NAMESPACE).toString().contains("gray")) {
                    resultMap.put(Dict.CPULIMITS, dataMap.get(Dict.CPU_LIMITS));
                    resultMap.put(Dict.MEMORYLIMITS, dataMap.get(Dict.MEMORY_LIMITS));
                    resultMap.put(Dict.MEMORY_REQUESTSUP, dataMap.get(Dict.MEMORY_REQUESTS));
                    resultMap.put(Dict.CPU_REQUESTSUP, dataMap.get(Dict.CPU_REQUESTS));
                    //找到一个即可
                    break;
                }
            }
            resultList.add(resultMap);
        }
        responseMap.put(Dict.LIST, resultList);
        return responseMap;
    }

    /**
     * @Author： lisy26
     * @Description： 查询应用详细
     * @Date： 2021/1/18 20:13
     * @Param: [map]
     * @return: java.util.List
     **/
    @Override
    public List listDeploymentDetail(Map map) {
        String deployment = (String) map.get(Dict.DEPLOYMENT);
        String cluster = (String) map.get(Dict.CLUSTER);
        String area = "[\"" + (String) map.get(Dict.AREA) + "\"]";

        //取出相关集群和环境中的各类信息
        List<Map<String, Object>> podList = bluekingDao.queryBluekingDetail(deployment, area, cluster, Dict.POD);
        List<Map<String, Object>> containerList = bluekingDao.queryBluekingDetail(deployment, area, cluster, Dict.CONTAINER);
        List<Map<String, Object>> deploymentList = bluekingDao.queryBluekingDetail(deployment, area, cluster, Dict.DEPLOYMENT);
        List<Map<String, Object>> responseList = new ArrayList<>();
        //如果应用没有对应的pod和contianer，表示应用没有启动，直接返回空
        if (podList.size() == 0 || containerList.size() == 0) {
            return responseList;
        }
        //获取当前集群和区域的租户信息
        List<String> nameSpaceList = new ArrayList<>();
        for (int i = 0; i < deploymentList.size(); i++) {
            Map<String, Object> deploymentMap = deploymentList.get(i);
            String nameSpace = (String) deploymentMap.get(Dict.NAMESPACE);
            if (!nameSpaceList.contains(nameSpace)) {
                nameSpaceList.add(nameSpace);
            }
        }
        for (int i = 0; i < nameSpaceList.size(); i++) {
            //单个租户下面的pod信息
            List<Map<String, Object>> podOneNamespace = new ArrayList<Map<String, Object>>();
            //单个租户下面的container信息
            Map<String, Object> containerOneNamespace = new HashMap<String, Object>();
            //单个租户下面的deployment信息
            Map<String, Object> deploymentOneNamespace = new HashMap<String, Object>();
            //从全量数据中提取出当前租户的pod信息
            for (int j = 0; j < podList.size(); j++) {
                Map<String, Object> podOneMap = podList.get(j);
                String nameSpace = (String) podOneMap.get(Dict.NAMESPACE);
                if (nameSpace.equals(nameSpaceList.get(i))) {
                    podOneNamespace.add(podOneMap);
                }
            }
            //从全量数据中提取出当前租户的container信息
            for (int j = 0; j < containerList.size(); j++) {
                Map<String, Object> containerOneMap = containerList.get(j);
                String nameSpace = (String) containerOneMap.get(Dict.NAMESPACE);
                if (nameSpace.equals(nameSpaceList.get(i))) {
                    containerOneNamespace = containerOneMap;
                }
            }
            //从全量数据中提取出当前租户的deployment信息
            for (int j = 0; j < deploymentList.size(); j++) {
                Map<String, Object> deploymentOneMap = deploymentList.get(j);
                String nameSpace = (String) deploymentOneMap.get(Dict.NAMESPACE);
                if (nameSpace.equals(nameSpaceList.get(i))) {
                    deploymentOneNamespace = deploymentOneMap;
                }
            }
            Map<String, Object> responseMap = new HashMap<>();
            //保证当前租户下容器存在，否则不返回数据
            if (podOneNamespace.size() != 0 && containerList.size() != 0 && deploymentOneNamespace.size() != 0){
                //运行信息
                responseMap.put(Dict.RUN, buildRun(podOneNamespace));
                //部署信息
                responseMap.put(Dict.DEPLOY, buildDeploy(containerOneNamespace, deploymentOneNamespace));
                //存储信息
                responseMap.put(Dict.STORGE, buildStorge(containerOneNamespace, deploymentOneNamespace));
                //配置信息
                responseMap.put(Dict.CONFIG, buildConfig(containerOneNamespace, deploymentOneNamespace));
                //当前租户的数据返回
                responseList.add(responseMap);
            }
        }
        return responseList;
    }

    private List<Map<String, Object>> buildRun(List<Map<String, Object>> podOneNamespace) {
        //pod信息有多个
        List<Map<String, Object>> runList = new ArrayList<>();
        for (int j = 0; j < podOneNamespace.size(); j++) {
            Map<String, Object> podMap = podOneNamespace.get(j);
            Map<String, Object> podinfo = new HashMap<>();//运行信息
            podinfo.put(Dict.NAME, podMap.get(Dict.NAME));//pod名称
            podinfo.put(Dict.IP, podMap.get(Dict.IP));//IP地址
            podinfo.put(Dict.HOSTNAMEUP, podMap.get(Dict.HOSTNAME));//宿主机名称
            runList.add(podinfo);
        }
        return runList;
    }

    private Map<String, Object> buildDeploy(Map<String, Object> containerOneNamespace, Map<String, Object> deploymentOneNamespace) {
        Map<String, Object> deployMap = new HashMap<>();
        deployMap.put(Dict.DCEIPUP, deploymentOneNamespace.get(Dict.DCEIP));//dce管理地址
        deployMap.put(Dict.NAMESPACEUP, containerOneNamespace.get(Dict.NAMESPACE));//租户
        deployMap.put(Dict.TAG, containerOneNamespace.get(Dict.TAG));//镜像标签
        deployMap.put(Dict.IMAGESPACEUP, containerOneNamespace.get(Dict.IMAGESPACE));//镜像空间
        deployMap.put(Dict.IMAGESPACEIPUP, containerOneNamespace.get(Dict.IMAGESPACEIP));//镜像空间ip
        deployMap.put(Dict.REPLICAS, deploymentOneNamespace.get(Dict.REPLICAS));//容器组数
        String portServer = (String) containerOneNamespace.get(Dict.PORTSERVER);
        if (StringUtils.isNotBlank(portServer)) {
            JSONArray portServerList = JSONObject.parseArray(portServer);
            JSONObject portServerObject = (JSONObject) portServerList.get(0);
            deployMap.put(Dict.PORTSERVERUP, portServerObject.get(Dict.CONTAINERPORT));//容器端口
        }
        return deployMap;
    }

    private List<Map<String, Object>> buildStorge(Map<String, Object> containerOneNamespace, Map<String, Object> deploymentOneNamespace) {
        //挂载点
        String volumemounts = (String) containerOneNamespace.get(Dict.VOLUMEMOUNTS);
        //挂载卷
        String volumes = (String) deploymentOneNamespace.get(Dict.VOLUMES);
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
                if (volumesMap.containsKey(name)) {
                    storgeMap.put(Dict.VOLUMES, volumesMap.get(name));//挂载卷
                }
                storgeMap.put(Dict.VOLUMEMOUNTSUP, volumemountsObject.get(Dict.MOUNTPATH));//容器内挂载点
                storgeList.add(storgeMap);
            }
        }
        return storgeList;
    }

    private Map<String, Object> buildConfig(Map<String, Object> containerOneNamespace, Map<String, Object> deploymentOneNamespace) {
        Map<String, Object> configMap = new HashMap<>();
        //配置中心
        List<String> configCentorList = null;
        //注册中心
        List<String> eurekaList = null;
        //所有的环境变量
        String env = (String) containerOneNamespace.get(Dict.ENV);
        List<Map<String, Object>> envList = new ArrayList<Map<String, Object>>();
        if (StringUtils.isNotBlank(env)) {
            JSONArray getEnvList = JSONObject.parseArray(env);
            for (int k = 0; k < getEnvList.size(); k++) {
                Map<String, Object> envMap = new HashMap<>();
                String getEnvString = (String) getEnvList.get(k);
                String[] envString = getEnvString.split("\"");
                String name = envString[3];
                String value = envString[7];
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
        configMap.put(Dict.CONFIGCENTOR, configCentorList);
        configMap.put(Dict.EUREKA, eurekaList);
        configMap.put(Dict.ENV, envList);
        //域名关联
        List<Map<String, String>> hostalias = new ArrayList<>();
        String hostaliasString = (String) deploymentOneNamespace.get(Dict.HOSTALIAS);
        if (StringUtils.isNotBlank(hostaliasString)) {
            JSONArray hostaliasList = JSONObject.parseArray(hostaliasString);
            for (int j = 0; j < hostaliasList.size(); j++) {
                //获取对应的ip和域名
                JSONObject ip_host = JSONObject.parseObject(hostaliasList.get(j).toString());
                Map<String, String> hostaliasMap = new HashMap<>();
                hostaliasMap.put(Dict.IP, ip_host.get(Dict.IP).toString());
                //域名去掉多余的符号
                hostaliasMap.put(Dict.HOSTNAMEUP, ip_host.get(Dict.HOSTNAMES).toString().replace("[\"", "").replace("\"]", ""));
                hostalias.add(hostaliasMap);
            }
        }
        configMap.put(Dict.HOSTALIAS, hostalias);
        //DNS配置
        List<String> DNSconfigList = new ArrayList<String>();
        String DNSconfig = (String) deploymentOneNamespace.get(Dict.DNSCONFIG);
        if (StringUtils.isNotBlank(DNSconfig)) {
            JSONArray DNSconfigListOne = JSONObject.parseArray(DNSconfig);
            for (int k = 0; k < DNSconfigListOne.size(); k++) {
                DNSconfigList.add((String) DNSconfigListOne.get(k));
            }
        }
        configMap.put(Dict.DNSCONFIGUP, DNSconfigList);
        //预留IP
        List<String> allocatedIpSegmentList = new ArrayList<String>();
        String allocatedIpSegment = (String) deploymentOneNamespace.get(Dict.ALLOCATED_IP_SEGMENT);
        if (StringUtils.isNotBlank(allocatedIpSegment)) {
            String[] allocatedIpSegmentString = allocatedIpSegment.split("/");
            for (int k = 0; k < allocatedIpSegmentString.length; k++) {
                allocatedIpSegmentList.add(allocatedIpSegmentString[k]);
            }
        }
        configMap.put(Dict.ALLOCATEDIPSEGMENT, allocatedIpSegmentList);
        //预停止策略
        String prestop = (String) containerOneNamespace.get(Dict.PRESTOP);
        if (StringUtils.isNotBlank(prestop) && !prestop.equals("\"\"")) {
            JSONObject prestopOne = JSONObject.parseObject(prestop);
            Map<String, Object> execMap = (Map) prestopOne.get("exec");
            List<String> commandList = (List) execMap.get("command");
            String prestopString = "";
            for (int k = 0; k < commandList.size(); k++) {
                prestopString = prestopString + commandList.get(k);
                if (k != commandList.size() - 1) {
                    prestopString = prestopString + " ";
                }
            }
            configMap.put(Dict.PRESTOPUP, prestopString);//预停止策略
        }
        configMap.put(Dict.STRATEGYTYPEUP, deploymentOneNamespace.get(Dict.STRATEGYTYPE));//发布策略
        configMap.put(Dict.NODESELECTORTERMSUP, deploymentOneNamespace.get(Dict.NODESELECTORTERMS));//节点选择策略
        return configMap;
    }

    //连接蓝鲸接口所需参数
    private Map<String, Object> baseParams() {
        Map<String, Object> baseParams = new HashMap<>();
        baseParams.put("bk_app_code", "cc-automation_saas");
        baseParams.put("bk_app_secret", bk_app_secret);
        baseParams.put("bk_username", "admin");
        baseParams.put("bk_supplier_account", "0");
        return baseParams;
    }

    //查询fdev二级实体blueking下面所有实体的属性字段，确定需要保存的蓝鲸数据。
    private List findBluekingDetail(String type) {
        //查询蓝鲸实体详情
        Map requestMap = new HashMap<String, Object>();
        requestMap.put(Dict.NAME_EN, Dict.BLUEKING);
        requestMap.put(Dict.PAGE, 1);
        requestMap.put(Dict.PER_PAGE, 50);
        //获取蓝鲸实体详情
        Map responseMap = this.modelDao.pageQuery(requestMap);
        List<Map> responseList = (List<Map>) responseMap.get(Dict.LIST);

        List returnList = new ArrayList();
        for (int i = 0; i < responseList.size(); i++) {
            Map bluekingMap = responseList.get(i);
            if (type.equals(bluekingMap.get(Dict.SUFFIX_NAME))) {
                List envList = (List) bluekingMap.get(Dict.ENV_KEY);
                for (int j = 0; j < envList.size(); j++) {
                    Map envMap = (Map) envList.get(j);
                    returnList.add(envMap.get(Dict.NAME_EN));
                }
            }
        }
        return returnList;
    }

    //调用蓝鲸平台接口，查询网银系统群下所有系统，获取每个系统的bk_inst_id值。
    private List findBkInstId() {
        Map<String, Object> requestMap = baseParams();
        List<Integer> returnList = new ArrayList<>();
        requestMap.put(Dict.BK_OBJ_ID, Dict.APP);
        Map<String, Object> conditionMap = new HashMap<>();
        List<Map> appList = new ArrayList<>();
        Map<String, Object> appListMap = new HashMap<>();
        appListMap.put(Dict.FIELD, Dict.BUSINESS_NAME);
        appListMap.put(Dict.OPERATOR, "$eq");
        appListMap.put(Dict.VALUE, "网银系统群");
        appList.add(appListMap);
        conditionMap.put(Dict.APP, appList);
        requestMap.put(Dict.CONDITION, conditionMap);
        try {
            Map responseMap = (Map) this.restTemplate.postForObject(searchInst, requestMap, Map.class, new Object[0]);
            Map data = (Map) responseMap.get(Dict.DATA);
            int count = (int) data.get(Dict.COUNT);
            List<Map> infoMap = (List) data.get(Dict.INFO);
            for (int i = 0; i < count; i++) {
                int bkInstIdNum = (int) infoMap.get(i).get(Dict.BK_INST_ID);
                if (bkInstIdNum != 276856 && bkInstIdNum != 282382) {
                    returnList.add(bkInstIdNum);
                }
            }
        } catch (Exception e) {
            logger.error("获取网银系统群所属系统失败" + e.getMessage());
            throw new FdevException("ENV0024");
        }
        return returnList;
    }

    //调用蓝鲸平台接口查询系统的全量deployment，获取每个deployment的bk_asst_inst_id值
    private List findBkAsstInstId(List bkInstIdList) {
        List<Integer> returnList = new ArrayList<>();
        Map<String, Object> requestMap = baseParams();
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put(Dict.BK_OBJ_ASST_ID, Dict.APP_CONTAIN_DEPLOYMENT);
        conditionMap.put(Dict.BK_ASST_OBJ_ID, Dict.DEPLOYMENT);
        conditionMap.put(Dict.BK_OBJ_ID, Dict.APP);
        Map<String, Object> bkInstIdListMap = new HashMap<>();
        bkInstIdListMap.put("$in", bkInstIdList);
        conditionMap.put(Dict.BK_INST_ID, bkInstIdListMap);
        requestMap.put(Dict.CONDITION, conditionMap);
        try {
            Map responseMap = (Map) this.restTemplate.postForObject(findInstanceAssociation, requestMap, Map.class, new Object[0]);
            List<Map> dataMapList = (List) responseMap.get(Dict.DATA);
            int count = dataMapList.size();
            for (int j = 0; j < count; j++) {
                int bkAsstInstId = (int) dataMapList.get(j).get(Dict.BK_ASST_INST_ID);
                returnList.add(bkAsstInstId);
            }
        } catch (Exception e) {
            logger.error("获取网银系统群deployment id失败" + e.getMessage());
            throw new FdevException("ENV0024");
        }
        return returnList;
    }

    //调取蓝鲸平台接口，获取deployment的全量详情。
    private Set deploymentDetail(List bkAsstInstIdList) {
        Set<String> returnSet = new HashSet<>();
        List<Map> depList = new ArrayList<>();
        Map<String, Object> findDeploymentMap = baseParams();
        findDeploymentMap.put(Dict.BK_OBJ_ID, Dict.DEPLOYMENT);
        Map<String, Object> conditionMap = new HashMap<>();
        List<Map> deploymentList = new ArrayList<>();
        Map<String, Object> deploymentFieldMap = new HashMap<>();
        deploymentFieldMap.put(Dict.FIELD, Dict.BK_INST_ID);
        deploymentFieldMap.put(Dict.OPERATOR, "$in");
        deploymentFieldMap.put(Dict.VALUE, bkAsstInstIdList);
        deploymentList.add(deploymentFieldMap);
        conditionMap.put(Dict.DEPLOYMENT, deploymentList);
        findDeploymentMap.put(Dict.CONDITION, conditionMap);
        List deploymentFieldList = findBluekingDetail(Dict.DEPLOYMENT);
        List<Blueking> bluekingdata = new ArrayList<>();
        try {
            Map responseMap = (Map) this.restTemplate.postForObject(searchInst, findDeploymentMap, Map.class, new Object[0]);
            Map data = (Map) responseMap.get(Dict.DATA);
            int count = (int) data.get(Dict.COUNT);
            List<Map> infoMap = (List) data.get(Dict.INFO);
            for (int j = 0; j < count; j++) {
                depList.add(infoMap.get(j));
                returnSet.add((String) infoMap.get(j).get(Dict.DEPLOYMENT));
                Blueking blueking = new Blueking();
                blueking.setEntityType(Dict.DEPLOYMENT);
                Map<String, Object> deploymentOneMap = new HashMap<>();
                for (int k = 0; k < deploymentFieldList.size(); k++) {
                    deploymentOneMap.put((String) deploymentFieldList.get(k), infoMap.get(j).get(deploymentFieldList.get(k)));
                }
                blueking.setData(deploymentOneMap);
                //数据先放list
                bluekingdata.add(blueking);
            }
        } catch (Exception e) {
            logger.error("获取网银系统群deployment详细信息失败" + e.getMessage());
            throw new FdevException("ENV0024");
        }
        //保存list
        bluekingDao.addAll(bluekingdata);
        return returnSet;
    }

    //调用蓝鲸平台接口查询deployment关联的pod；
    private void podDetail(Set<String> deploymentSet) {
        Map<String, Object> findPodMap = baseParams();
        List<Map> podMapList = new ArrayList<>();
        findPodMap.put(Dict.BK_OBJ_ID, Dict.POD);
        Map<String, Object> conditionMap = new HashMap<>();
        List<Map> podList = new ArrayList<>();
        Map<String, Object> podFieldMap = new HashMap<>();
        podFieldMap.put(Dict.FIELD, Dict.DEPLOYMENT);
        podFieldMap.put(Dict.OPERATOR, "$in");
        podFieldMap.put(Dict.VALUE, deploymentSet);
        podList.add(podFieldMap);
        conditionMap.put(Dict.POD, podList);
        findPodMap.put(Dict.CONDITION, conditionMap);
        List podFieldList = findBluekingDetail(Dict.POD);
        List<Blueking> bluekingdata = new ArrayList<>();
        try {
            Map responseMap = (Map) this.restTemplate.postForObject(searchInst, findPodMap, Map.class, new Object[0]);
            Map data = (Map) responseMap.get(Dict.DATA);
            int count = (int) data.get(Dict.COUNT);
            List<Map> infoMap = (List) data.get(Dict.INFO);
            podMapList.addAll(infoMap);
            for (int j = 0; j < count; j++) {
                Blueking blueking = new Blueking();
                blueking.setEntityType(Dict.POD);
                Map<String, Object> podOneMap = new HashMap<>();
                for (int k = 0; k < podFieldList.size(); k++) {
                    podOneMap.put((String) podFieldList.get(k), infoMap.get(j).get(podFieldList.get(k)));
                }
                blueking.setData(podOneMap);
                //数据先放list
                bluekingdata.add(blueking);
            }
        } catch (Exception e) {
            logger.error("获取网银系统群pod详细信息失败" + e.getMessage());
            throw new FdevException("ENV0024");
        }
        //保存list
        bluekingDao.addAll(bluekingdata);
    }

    //调用蓝鲸平台接口查询deployment关联的container；
    private void containerDetail(Set<String> deploymentSet) {
        Map<String, Object> findContainerMap = baseParams();
        List<Map> containerMapList = new ArrayList<>();
        findContainerMap.put(Dict.BK_OBJ_ID, Dict.CONTAINER);
        Map<String, Object> conditionMap = new HashMap<>();
        List<Map> containerList = new ArrayList<>();
        Map<String, Object> containerFieldMap = new HashMap<>();
        containerFieldMap.put(Dict.FIELD, Dict.DEPLOYMENT);
        containerFieldMap.put(Dict.OPERATOR, "$in");
        containerFieldMap.put(Dict.VALUE, deploymentSet);
        containerList.add(containerFieldMap);
        conditionMap.put(Dict.CONTAINER, containerList);
        findContainerMap.put(Dict.CONDITION, conditionMap);
        List containerFieldList = findBluekingDetail(Dict.CONTAINER);
        List<Blueking> bluekingdata = new ArrayList<>();
        try {
            Map responseMap = (Map) this.restTemplate.postForObject(searchInst, findContainerMap, Map.class, new Object[0]);
            Map data = (Map) responseMap.get(Dict.DATA);
            int count = (int) data.get(Dict.COUNT);
            List<Map> infoMap = (List) data.get(Dict.INFO);
            containerMapList.addAll(infoMap);
            for (int j = 0; j < count; j++) {
                Blueking blueking = new Blueking();
                blueking.setEntityType(Dict.CONTAINER);
                Map<String, Object> containerOneMap = new HashMap<>();
                for (int k = 0; k < containerFieldList.size(); k++) {
                    containerOneMap.put((String) containerFieldList.get(k), infoMap.get(j).get(containerFieldList.get(k)));
                }
                blueking.setData(containerOneMap);
                //数据先放list
                bluekingdata.add(blueking);
            }
        } catch (Exception e) {
            logger.error("获取网银系统群container详细信息失败" + e.getMessage());
            throw new FdevException("ENV0024");
        }
        //保存list
        bluekingDao.addAll(bluekingdata);
    }

}