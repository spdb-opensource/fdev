package com.spdb.fdev.spdb.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.CommonUtil;
import com.spdb.fdev.base.utils.TimeUtils;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.spdb.service.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RefreshScope
public class SonarServiceImpl implements ISonarService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    GitLabService gitLabService;
    @Autowired
    IAppService appService;
    @Autowired
    ITaskService taskService;
    @Autowired
    IKafkaService kafkaService;

    @Value("${git.clone.user}")
    private String gitCloneUser;
    @Value("${git.clone.password}")
    private String gitClonePwd;
    @Value("${git.token}")
    private String token;
    @Value("${metricKey}")
    private String metricKey;
    @Value("${getProjectInfo}")
    private String getProjectInfo;
    @Value("${sonar.api}")
    private String sonarApi;
    @Value("${kafka.topic}")
    private String kafkaTopic;

    @Value("${dsonar.host}")
    private String dSonarHost;
    @Value("${dsonar.scm.provider}")
    private String dSonarScmProvider;
    @Value("${dsonar.login}")
    private String dSonarLogin;
    @Value("${sonar.image.name}")
    private String sonarImageName;
    @Value("${fdev.env}")
    private String fdevEnv;
    @Value("${sonar.environment.slug}")
    private String sonarEnvironmentSlug;
    @Value("${branch.specific.symbol}")
    private String branchSpecificSymbol;
    @Value("${sonar.file}")
    private String sonarFile;
    @Value("${sonar.not.scan.id}")
    private String noSonarId;

    private static String jsonString;

    static {
        jsonString = CommonUtil.getTemplateJson("kafka-template/sonar.json");
    }

    @Override
    public Map projectAnalyses(Map<String, Object> app, String branch_name) {
        String project = (String) app.get(Dict.NAME_EN);
        if (!CommonUtil.isNullOrEmpty(branch_name)) {
            branch_name = this.replaceSpecificSymbol(branch_name);
            project = project + "::" + branch_name;
        }
        String url = sonarApi + "project_analyses/search?project=" + project;
        ResponseEntity<String> result;
        HttpEntity<Object> request = setHttpHeader();
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            Object returnData = result.getBody();
            Map resultMap = (Map) JSONObject.parse((String) returnData);
            resultMap.remove("paging");
            JSONArray analyses = (JSONArray) resultMap.get("analyses");
            Map analysesMap = (Map) analyses.get(0);
            resultMap.put(Dict.VERSION, analysesMap.get("projectVersion"));
            return resultMap;
        } catch (Exception e) {
            logger.error("project analyses error,project=" + project);
            return null;
        }
    }

    @Override
    public Map searchProject() throws Exception {
        List<Map> projects = getProjects();
        List<Map> measures = getMeasures(projects);
        List<Map> bugs = new ArrayList<>();
        List<Map> vulnerabilities = new ArrayList<>();
        List<Map> duplicated_lines_density = new ArrayList<>();
        List<Map> code_smells = new ArrayList<>();
        Map ncloc = new HashMap();
        for (Map measure : measures) {
            HashMap tmp = new HashMap();
            switch ((String) measure.get("metric")) {
                case "bugs":
                    tmp.put("name", measure.get("component"));
                    tmp.put("id", getAppId((String) measure.get("component")));
                    tmp.put("value", measure.get("value"));
                    bugs.add(tmp);
                    break;
                case "vulnerabilities":
                    tmp.put("name", measure.get("component"));
                    tmp.put("id", getAppId((String) measure.get("component")));
                    tmp.put("value", measure.get("value"));
                    vulnerabilities.add(tmp);
                    break;
                case "duplicated_lines_density":
                    tmp.put("name", measure.get("component"));
                    tmp.put("id", getAppId((String) measure.get("component")));
                    Float f = Float.valueOf((String) measure.get("value"));
                    int value = (int) (f * 100);
                    tmp.put("value", value + "");
                    duplicated_lines_density.add(tmp);
                    break;
                case "code_smells":
                    tmp.put("name", measure.get("component"));
                    tmp.put("id", getAppId((String) measure.get("component")));
                    tmp.put("value", measure.get("value"));
                    code_smells.add(tmp);
                    break;
                case "ncloc":
                    ncloc.put(measure.get("component"), measure.get("value"));
                    break;
                default:
            }
        }
        Comparator comparator_int = new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                //降序
                Integer value2 = Integer.valueOf((String) o2.get("value"));
                Integer value1 = Integer.valueOf((String) o1.get("value"));
                return value2.compareTo(value1);
            }
        };
        Comparator comparator_float = new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                //降序
                Float value2 = Float.valueOf((String) o2.get("value"));
                Float value1 = Float.valueOf((String) o1.get("value"));
                return value2.compareTo(value1);
            }
        };
        Collections.sort(bugs, comparator_int);
        Collections.sort(duplicated_lines_density, comparator_int);
        Collections.sort(code_smells, comparator_int);
        Collections.sort(vulnerabilities, comparator_int);
        Map result = new HashMap();
        result.put("bugs", bugs.size() > 10 ? bugs.subList(0, 10) : bugs);
        result.put("duplicated_lines_density", duplicated_lines_density.size() > 10 ? duplicated_lines_density.subList(0, 10) : duplicated_lines_density);
        result.put("code_smells", code_smells.size() > 10 ? code_smells.subList(0, 10) : code_smells);
        result.put("vulnerabilities", vulnerabilities.size() > 10 ? vulnerabilities.subList(0, 10) : vulnerabilities);

        List bugs_radio = ratio(bugs, ncloc);
        List code_smells_radio = ratio(code_smells, ncloc);
        List vulnerabilities_radio = ratio(vulnerabilities, ncloc);
        Collections.sort(bugs_radio, comparator_float);
        Collections.sort(code_smells_radio, comparator_float);
        Collections.sort(vulnerabilities_radio, comparator_float);
        result.put("bugs_radio", bugs_radio.size() > 10 ? bugs_radio.subList(0, 10) : bugs_radio);
        result.put("code_smells_radio", code_smells_radio.size() > 10 ? code_smells_radio.subList(0, 10) : code_smells_radio);
        result.put("vulnerabilities_radio", vulnerabilities_radio.size() > 10 ? vulnerabilities_radio.subList(0, 10) : vulnerabilities_radio);
        return result;
    }

    @Override
    public List<Map<String, String>> searchTotalProjectMeasures(Map<String, Object> requestMap) throws Exception {
        List<Map> projects = getProjects();
        List<Map<String, String>> result = new ArrayList<>();
        //过滤key包含“::”
        List<String> keys = projects.stream().filter(x -> !((String)x.get("key")).contains("::")).map(x -> ((String)x.get("key"))).collect(Collectors.toList());
        List<String> measureSearchsUrls = new ArrayList<>();
        int batchCount = 100;//上限100
        int keysSize = keys.size();//预存 节省i循环性能
        String measureSearchsUrl = sonarApi + "measures/search?&metricKeys=bugs,vulnerabilities,code_smells,duplicated_lines_density&projectKeys=";
        StringBuilder keysStr = new StringBuilder();
        int index = 0;
        for (int i = 0; i < keysSize; i++) {
            keysStr.append(keys.get(i)).append(",");
            index++;
            if (index == batchCount) {
                measureSearchsUrls.add(measureSearchsUrl + keysStr.deleteCharAt(keysStr.length() - 1).toString());
                keysStr = new StringBuilder();
                index = 0;
            }
            if (i == keysSize - 1) {
                measureSearchsUrls.add(measureSearchsUrl + keysStr.deleteCharAt(keysStr.length() - 1).toString());
            }
        }
        for (String url : measureSearchsUrls) {
            result.addAll((List<Map<String, String>>)doGet(url).get("measures"));
        }
        return result;
    }

    /**
     * 获取全量的应用列表
     *
     * @return
     */
    private List<Map> getProjects() {
        String url = sonarApi + "components/search_projects?ps=500";
        Map data = doGet(url);
        List<Map> projects = (List<Map>) data.get("components");
        Map paging = (Map) data.get("paging");
        int pageSize = (int) paging.get("pageSize");
        int total = (int) paging.get("total");
        int i;
        if (10000 < total) {//如果大于10000则设置为10000，因为sonar只能查询10000条数据
            i = 10000 / pageSize;
        } else {
            i = total / pageSize + 1;
        }
        if (i != 0) {
            for (int j = 2; j <= i; j++) {
                data = doGet(url + "&p=" + j);
                projects.addAll((List<Map>) data.get("components"));
            }
        }
        return projects;
    }

    /**
     * 获取project对应的分析结果
     *
     * @param projects
     * @return
     */
    private List<Map> getMeasures(List<Map> projects) {
        List<Map> measures = new ArrayList<>();
        List<String> measuresUrls = getMeasuresUrl(projects, 50);
        for (String measuresUrl : measuresUrls) {
            HttpEntity<Object> request = setHttpHeader();
            URI uri = getURI(measuresUrl);
            ResponseEntity<String> result = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
            Map data = (Map) JSONObject.parse(result.getBody());
            measures.addAll(JSONObject.parseArray(data.get("measures").toString(), Map.class));
        }
        return measures;
    }

    /**
     * 根据扫描出的项目名称，获取应用实体的id
     *
     * @param componentName
     * @return
     */
    private String getAppId(String componentName) throws Exception {
        String name_en = componentName.substring(componentName.indexOf(":") + 1);
        if (name_en.contains("parent")) {
            name_en = name_en.substring(0, name_en.indexOf("-parent"));
        }
        Map<String, Object> app = appService.queryByNameEn(name_en);
        if (!CommonUtil.isNullOrEmpty(app)) {
            return (String) app.get(Dict.ID);
        }
        return "";
    }

    /**
     * 将所有项目分为每50个一次请求
     *
     * @param sourList
     * @param batchCount
     * @return
     */
    private List<String> getMeasuresUrl(List<Map> sourList, int batchCount) {
        ArrayList measuresUrls = new ArrayList();
        int sourListSize = sourList.size();
        int subCount = sourListSize % batchCount == 0 ? sourListSize / batchCount : sourListSize / batchCount + 1;
        int startIndext = 0;
        int stopIndext = 0;
        for (int i = 0; i < subCount; i++) {
            String url = sonarApi + "measures/search?&metricKeys=bugs,vulnerabilities,code_smells,duplicated_lines_density,ncloc&projectKeys=";
            stopIndext = (i == subCount - 1 && sourListSize % batchCount != 0) ? stopIndext + sourListSize % batchCount : stopIndext + batchCount;
            List<Map> tempList = new ArrayList<Map>(sourList.subList(startIndext, stopIndext));
            String str = "";
            for (Map project : tempList) {
                String projectName = (String) project.get("key");
                if (projectName.contains("::"))
                    continue;
                str = str + project.get("key") + ",";
            }
            startIndext = stopIndext;
            if (str.length() > 1) {
                url = url + str.substring(0, str.length() - 1);
                measuresUrls.add(url);
            }
        }
        return measuresUrls;
    }

    /**
     * 处理url
     *
     * @param url
     * @return
     */
    private URI getURI(String url) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        return builder.build(true).toUri();
    }

    /**
     * 整理请求头，发送get请求
     *
     * @param url
     * @return
     */
    private Map doGet(String url) {
        HttpEntity<Object> request = setHttpHeader();
        ResponseEntity<String> result = restTemplate.exchange(getURI(url), HttpMethod.GET, request, String.class);
        return (Map) JSONObject.parse(result.getBody());
    }

    /**
     * 根据行数计算比例
     *
     * @param src
     * @param ncloc
     * @return
     */
    private List ratio(List<Map> src, Map ncloc) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00000");
        ArrayList result = new ArrayList();
        for (Map map : src) {
            try {
                Map radio = new HashMap();
                Integer value = Integer.valueOf((String) map.get("value"));
                Integer wc = Integer.valueOf((String) ncloc.get(map.get("name")));
                String p = decimalFormat.format((float) value / wc);
                radio.put("name", map.get("name"));
                radio.put("id", map.get("id"));
                radio.put("value", p);
                result.add(radio);
            } catch (Exception e) {
                logger.error(map.get("name") + "应用不包含任何内容,请删除!!!");
            }
        }
        return result;
    }

    @Override
    public Map getProjectInfo(Map<String, Object> app, String branch_name) throws Exception {
        String projectKeys = (String) app.get(Dict.NAME_EN);
        if (!CommonUtil.isNullOrEmpty(branch_name)) {
            branch_name = this.replaceSpecificSymbol(branch_name);
            projectKeys = projectKeys + "::" + branch_name;
        }
        String url = sonarApi + "measures/search?projectKeys=" + projectKeys + "&metricKeys=" + getProjectInfo;
        try {
            Map data = doGet(url);
            List<Map> list = (List<Map>) data.get("measures");
            Map returnmap = new HashMap();
            for (Map map : list) {
                Map newmap = new HashMap();
                String metric = (String) map.get("metric");
                String value = (String) map.get(Dict.VALUE);
                String s = splicingUrl(projectKeys, metric);
                if (!CommonUtil.isNullOrEmpty(s)) {
                    newmap.put(Dict.VALUE, value);
                    newmap.put(Dict.URL, s);
                    returnmap.put(metric, newmap);
                } else {
                    returnmap.put(metric, value);
                }
            }
            return returnmap;
        } catch (HttpClientErrorException e) {
            logger.error("project analyses error,project=" + projectKeys);
            return null;
        }
    }

    @Override
    public Map<String, Object> getAnalysesHistory(Map<String, Object> app, String branch_name) throws Exception {
        String project = (String) app.get(Dict.NAME_EN);
        if (!CommonUtil.isNullOrEmpty(branch_name)) {
            branch_name = this.replaceSpecificSymbol(branch_name);
            project = project + "::" + branch_name;
        }
        String url = sonarApi + "measures/search_history?metrics=bugs,code_smells,vulnerabilities&component=" + project;
        //组装数据
        Map<String, Object> map;
        try {
            Map data = doGet(url);
            List<Map> measures = (List<Map>) data.get("measures");
            //组装数据
            map = splicingData(measures);
        } catch (HttpClientErrorException e) {
            logger.error("project analyses error,project=" + project);
            return null;
        }
        return map;
    }

    @Override
    public List<Map> componentTree(Map<String, Object> app, String branch_name) {
        String project = (String) app.get(Dict.NAME_EN);
        if (!CommonUtil.isNullOrEmpty(branch_name)) {
            branch_name = this.replaceSpecificSymbol(branch_name);
            project = project + "::" + branch_name;
        }
        String url = sonarApi + "measures/component_tree?component=" + project + "&ps=500&metricKeys=" + metricKey;
        try {
            Map data = doGet(url);
            List<Map> components = (List<Map>) data.get("components");
            if (!CommonUtil.isNullOrEmpty(components)) {
                for (Map<String, String> map : components) {
                    String key = map.get(Dict.KEY);
                    String path = map.get(Dict.PATH);
                    String sonarUrl = dSonarHost + "/code?id=" + project + "&selected=" + key;
                    map.put(Dict.KEY, path);
                    map.put(Dict.PATH, sonarUrl);
                    map.remove(Dict.ID);
                }
            }
            return components;
        } catch (HttpClientErrorException e) {
            logger.error("project analyses error,project=" + project);
            return null;
        }
    }

    @Override
    public String scanningFeatureBranch(Map app, String branch_name) {
        if (checkAppType((String) app.get(Dict.TYPE_NAME))) {
            logger.info("非Java微服务不扫描，应用名：{}", app.get(Dict.NAME_EN));
            throw new FdevException(ErrorConstants.SONAR_REJECTED, new String[]{"应用[" + app.get(Dict.NAME_EN)
                    + "]的类型为:" + app.get(Dict.TYPE_NAME)});
        }
        Map project = gitLabService.getProjectInfo(String.valueOf(app.get(Dict.GITLAB_PROJECT_ID)), token);
        String application_name = (String) app.get(Dict.NAME_EN);
        String time = TimeUtils.formatToday();
        try {
            taskService.updateTaskSonarTime(application_name, branch_name, time);
            logger.info("nameEn:{},branch:{},scanTime:{}", application_name, branch_name, time);
        } catch (Exception e) {
            logger.error("更新sonar时间异常：{}", e.getMessage());
            return "更新saonar扫描时间异常,请稍后重试!";
        }
        String result = sonarScan((Integer) app.get(Dict.GITLAB_PROJECT_ID), (String) project.get(Dict.NAME),
                application_name, branch_name, (String) app.get(Dict.GIT));
        return result;
    }

    /**
     * 判断该应用是否为容器化项目
     *
     * @param typeName
     * @return
     */
    private boolean checkAppType(String typeName) {
        boolean flag = true;
        if (Constants.APP_TYPE_JAVA.equals(typeName)) {
            flag = false;
        }
        return flag;
    }

    @Override
    public String getSonarStatus(String sonar_id) {
        String url = sonarApi + "ce/task?id=" + sonar_id;
        ResponseEntity<String> result;
        HttpEntity<Object> request = setHttpHeader();
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            Object returnData = result.getBody();
            Map resultMap = (Map) JSONObject.parse((String) returnData);
            Map map = (Map) resultMap.get("task");
            return (String) map.get(Dict.STATUS);
        } catch (Exception e) {
            return "failed";
        }
    }

    /**
     * 扫描指定分支
     *
     * @param projectId   应用GitLab的 project id
     * @param projectName 应用GitLab的 project name
     * @param appNameEn   应用英文名
     * @param branch      扫描分支
     * @param gitHttpUrl  git clone地址
     */
    private String sonarScan(Integer projectId, String projectName, String appNameEn, String branch, String gitHttpUrl) {
        // 根据projectId获取应用英文名
        String projectKey = appNameEn;
        String sonarProjectName = appNameEn;
        String projectDir = " /ebank/devops/" + projectName;
        String diffFile = "";
        String logDir = "";
        StringBuilder sonarTaskIdCmd = new StringBuilder();
        if (!Dict.SIT_UP.equals(branch)) {
            String specificBranch = this.replaceSpecificSymbol(branch);
            projectKey = appNameEn + "::" + specificBranch;
            sonarProjectName = appNameEn + "::" + specificBranch;
            // 增量扫描，只扫与master分支的差异文件
            diffFile = this.getDiffFile(projectId, Dict.MASTER, branch);
            if (StringUtils.isEmpty(diffFile)) {
                try {
                    taskService.updateTaskSonarId(appNameEn, branch, noSonarId);
                    logger.info("nameEn:{},branch:{},sonarId:{}", appNameEn, branch, noSonarId);
                } catch (Exception e) {
                    logger.error("更新sonarId异常：{}", e.getMessage());
                }
                return "当前分支变更的代码不涉及sonar扫描文件，无需扫描！";
            }
            // feature分支日志重定向
            logDir = "/fwebhook/sonar/" + appNameEn + "-" + specificBranch + ".log";
            // 组装获取sonar task id的命令
            String sonarReportPath = projectDir + "/target/sonar/report-task.txt";
            sonarTaskIdCmd.append(" ; sonar.py ")
                    .append(fdevEnv).append(" ")
                    .append(appNameEn).append(" ").append(branch).append(" ").append(sonarReportPath);
        }
        StringBuilder sonarCommand = new StringBuilder();
        sonarCommand.append("git clone -b \\\"").append(branch).append("\\\"")
                .append(gitHttpUrl.replace("http://", " http://" + gitCloneUser + ":" + gitClonePwd + "@"))
                .append(" ").append(projectDir).append(" && cd ").append(projectDir);
        sonarCommand.append(" && mvn clean package -U -Dmaven.test.skip=true");
        if (!Dict.SIT_UP.equals(branch)) {
            sonarCommand.append(" > ").append(logDir);
        }
        sonarCommand.append(" && mvn sonar:sonar -Dsonar.host.url=").append(dSonarHost)
                .append(" -Dsonar.login=").append(dSonarLogin)
                .append(" -Dsonar.scm.provider=").append(dSonarScmProvider)
                .append(" -Dsonar.projectKey=").append(projectKey)
                .append(" -Dsonar.projectName=").append(sonarProjectName);
        if (StringUtils.isNotEmpty(diffFile)) {
            sonarCommand.append(" -Dsonar.inclusions=").append(diffFile);
        }
        long currentTimeMillis = System.currentTimeMillis();
        String logDirCmd = " >> " + logDir;
        if (StringUtils.isNotEmpty(sonarTaskIdCmd.toString())) {
            sonarCommand.append(logDirCmd).append(sonarTaskIdCmd).append(" ").append(logDir);
        }
        String sonarData = jsonString.replace(Constants.SONAR_NAME, Dict.SONAR + "-" + projectName)
                .replace(Constants.TIMESTAMP, currentTimeMillis + "")
                .replace(Constants.SONAR_IMAGE_NAME, sonarImageName)
                .replace(Constants.SONAR_COMMAND, sonarCommand)
                .replace(Constants.SONAR_ENVIRONMENT_SLUG, sonarEnvironmentSlug)
                .replace(Constants.PROIECT_ID, projectId + "")
                .replace(Constants.PROJECT_NAME, appNameEn)
                .replace(Constants.BRANCH, branch);
        // 发送数据
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(Dict.PROJECT_ID, projectId);
        kafkaService.sendData(kafkaTopic, sonarData.getBytes(), logMap);
        return "后台正在执行sonar扫描，请耐心等待！执行结束后，可点击下方的\"代码分析\"查看扫描结果。";
    }

    /**
     * 获取变更文件路径
     *
     * @param projectId
     * @param from
     * @param to
     * @return
     */
    private String getDiffFile(Integer projectId, String from, String to) {
        Map<String, Object> compare = gitLabService.compare(projectId, from, to);
        List<Map<String, Object>> diffList = (List<Map<String, Object>>) compare.get(Dict.DIFFS);
        // 无代码变更
        if (CollectionUtils.isEmpty(diffList)) {
            return "";
        }
        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 0; i < diffList.size(); i++) {
            Map<String, Object> diffMap = diffList.get(i);
            String newPath = (String) diffMap.get(Dict.NEW_PATH);
            // 判断是否为指定扫描的文件类型
            String[] sonarFileList = sonarFile.split(";");
            for (String fileSuffix : sonarFileList) {
                if (newPath.endsWith(fileSuffix)) {
                    pathBuilder.append(newPath);
                    if (i != diffList.size() - 1) {
                        pathBuilder.append(",");
                    }
                    break;
                }
            }
        }
        return pathBuilder.toString();
    }

    /**
     * 拼接sonar扫描关于  bugs  vulnerabilities  code_smells 的url
     *
     * @param projectKey
     * @param metric
     * @return
     * @throws Exception
     */
    public String splicingUrl(String projectKey, String metric) {
        String str = dSonarHost + "/project/issues?id=" + projectKey + "&resolved=false&types=";
        String url = null;
        if (metric.equals(Dict.BUGS)) {
            url = str + "BUG";
        }
        if (metric.equals(Dict.VULNERABILITIES)) {
            url = str + "VULNERABILITY";
        }
        if (metric.equals(Dict.CODE_SEMLLS)) {
            url = str + "CODE_SMELL";
        }
        return url;
    }


    /**
     * 对bugs，漏洞，异味三者的数据组装
     *
     * @param list
     * @return
     * @throws Exception
     */
    public Map splicingData(List<Map> list) throws Exception {
        Map projectmap = new HashMap();
        for (Map<String, Object> map : list) {
            List integers = new ArrayList<>();
            List<String> dateList = new ArrayList<>();
            String metric = (String) map.get("metric");
            List<Map> history = (List<Map>) map.get("history");
            for (Map<String, String> mapq : history) {
                String date = mapq.get("date");
                //将日期转成 xxxx年xx月xx日
                String nowdate = TimeUtils.getDate(date, "yyyy-HH-dd", "yyyy年MM月dd日");
                dateList.add(nowdate);
                String value = mapq.get(Dict.VALUE);
                integers.add(value);
            }
            projectmap.put(metric, integers);
            projectmap.put("xAxis", dateList);
        }
        return projectmap;
    }

    /**
     * 设置请求头
     *
     * @return
     */
    public HttpEntity setHttpHeader() {
        //对账号密码进行base64编码
        String encode = CommonUtil.encode("admin:admin");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Basic " + encode);
        HttpEntity<Object> request = new HttpEntity<>(httpHeaders);
        return request;
    }

    /**
     * 替换分支中的特殊符号
     *
     * @param branch
     * @return
     */
    private String replaceSpecificSymbol(String branch) {
        String specificBranch = branch;
        String[] specificSymbolSplit = branchSpecificSymbol.split(";");
        for (String specificSymbol : specificSymbolSplit) {
            if (specificBranch.contains(specificSymbol)) {
                specificBranch = specificBranch.replace(specificSymbol, "_");
            }
        }
        return specificBranch;
    }

}
