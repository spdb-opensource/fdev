package com.spdb.fdev.component.service.impl;

import com.spdb.fdev.base.dict.Constants;
import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.dict.ErrorConstants;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.component.entity.Dependency;
import com.spdb.fdev.component.service.ICommonScanService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Service
/**
 * 骨架和组件扫描公共方法抽出
 */
@RefreshScope
public class CommonScanServiceImpl implements ICommonScanService {

    private static final Logger logger = LoggerFactory.getLogger(CommonScanServiceImpl.class);

    @Value("${nas.apps.path}")
    private String nas_apps_path;

    @Value("${python.version}")
    private String python_version;

    @Value("${python.path}")
    private String python_path;

    @Value("${nexus.search}")
    private String nexus_search;

    @Value("${nexus.version.resolve}")
    private String nexus_version_resolve;

    @Value("${nexus.version.detail}")
    private String nexus_version_detail;

    @Value("${multi.nexus.search}")
    private String multi_nexus_search;

    @Autowired
    private RestTemplate restTemplate;


    /**
     * 获取dir下的所有后端组件依赖列表
     *
     * @param dir
     * @return
     */
    @Override
    public List<Dependency> getDependencyList(String dir, String type) throws Exception {
        List<Dependency> result = null;
        String dependency = this.getPythonResult(dir, type);
        if (StringUtils.isNotBlank(dependency) && dependency.length() > 1) {
            // 测试时如果是3以上版本的python，调用脚本返回的是b'[]',2的版本返回[]
            if (Constants.PYTHON_VERSION_THREE.equals(python_version)) {
                dependency = dependency.substring(1).replace("'", "").trim();
            }
            ObjectMapper mapper = new ObjectMapper();// 定义 org.codehaus.jackson
            result = mapper.readValue(dependency, new TypeReference<List<Dependency>>() {
            });
        }
        return result;
    }

    /**
     * 获取dir下的所有mpass组件依赖列表
     *
     * @param dir
     * @return
     */
    @Override
    public List<Map> getDependencyMapList(String dir, String type) throws Exception {
        List<Map> result = null;
        String dependency = this.getPythonResult(dir, type);
        if (StringUtils.isNotBlank(dependency) && dependency.length() > 1) {
            // 测试时如果是3以上版本的python，调用脚本返回的是b'[]',2的版本返回[]
            if (Constants.PYTHON_VERSION_THREE.equals(python_version)) {
                dependency = dependency.substring(1).replace("'", "").trim();
            }
            ObjectMapper mapper = new ObjectMapper();// 定义 org.codehaus.jackson
            result = mapper.readValue(dependency, new TypeReference<List<Map>>() {
            });
        }
        return result;
    }

    /**
     * 调用maven接口获取当前骨架的版本记录
     */
    @Override
    public JSONArray getNexus(String groupId, String artifactId) {
        String url = nexus_search + "?g=" + groupId + "&a=" + artifactId;
        String body = Util.httpMethodGetExchange(url, restTemplate);
        JSONArray jsonArray = null;
        if (StringUtils.isNotBlank(body)) {
            JSONObject jsonObject = JSONObject.fromObject(body);
            jsonArray = jsonObject.getJSONArray("data");
        }
        return jsonArray;
    }

    @Override
    public JSONArray getMultiNexus(String groupId, String artifactId) throws Exception{
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json;charset=UTF-8");
        httpHeaders.add("Accept", "*/*");
        httpHeaders.add("Accept-Encoding", "gzip, deflate");
        httpHeaders.add("Accept-Language", "zh-CN,zh;q=0.9");
        httpHeaders.add("Connection", "keep-alive");
        httpHeaders.add("Cache-Control", "no-cache");
        httpHeaders.add("Pragma", "no-cache");
        httpHeaders.add("X-Nexus-UI", "true");
        httpHeaders.add("X-Requested-With", "XMLHttpRequest");
        httpHeaders.add("Host", "xxx:8081");
        httpHeaders.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
        //http body配置
        Map map = new HashMap();
        map.put("action","coreui_Search");
        map.put("method","read");
        List<Map> list = new ArrayList<>();
        Map search = new HashMap();
        search.put("page",1);
        search.put("start",0);
        search.put("limit",1000);
        List<Map> filters = new ArrayList();
        //assembleFiliter(filters, "repository_name", repositoryName);
        assembleFiliter(filters, "group", groupId);
        assembleFiliter(filters, "attributes.maven2.artifactId", artifactId);
        search.put("filter",filters);
        list.add(search);
        map.put("data",list);
        map.put("type","rpc");
        map.put("tid",15);
        JSONObject jsonObject = JSONObject.fromObject(map);
        HttpEntity httpEntity = new HttpEntity(jsonObject, httpHeaders);
        ResponseEntity<String> result;
        String body;
        JSONArray jsonArray = null;
        try {
            result = restTemplate.exchange(multi_nexus_search, HttpMethod.POST, httpEntity, String.class);
            body = result.getBody();
            if (StringUtils.isNotBlank(body)) {
                JSONObject json = JSONObject.fromObject(body);
                JSONObject result1 = json.getJSONObject("result");
                jsonArray = result1.getJSONArray("data");
            }
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return jsonArray;
    }

    private void assembleFiliter(List<Map> filters, String key, String value) throws Exception{
        Map property = new HashMap();
        property.put("property",key);
        property.put("value",value);
        filters.add(property);
    }

    /**
     * 调用maven接口获取当前组件推送的最新Release版本
     */
    @Override
    public String getLatestVersion(String groupId, String artifactId) {
        String url = nexus_search + "?g=" + groupId + "&a=" + artifactId;
        String body = Util.httpMethodGetExchange(url, restTemplate);
        if (StringUtils.isNotBlank(body)) {
            JSONObject jsonObject = JSONObject.fromObject(body);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    String version = jsonArray.getJSONObject(i).getString(Dict.VERSION);
                    if (StringUtils.isNotBlank(version) && version.endsWith(Dict.RELEASE)) {
                        return version;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取maven仓库最大版本
     *
     * @param groupId
     * @param artifactId
     * @return
     */
    @Override
    public String getMaxVersion(String groupId, String artifactId) {
        String url = nexus_search + "?g=" + groupId + "&a=" + artifactId;
        String body = Util.httpMethodGetExchange(url, restTemplate);
        if (StringUtils.isNotBlank(body)) {
            net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(body);
            JSONArray jsonArray = jsonObject.getJSONArray("data");
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    String version = jsonArray.getJSONObject(i).getString(Dict.VERSION);
                    if (StringUtils.isNotBlank(version)) {
                        return version;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 调用maven接口获取当前版本的推送时间
     * xxx/nexus/service/local/artifact/maven/resolve?_dc=1575509325941
     * &r=releases&g=com.csii.pe&a=pe-redis-core&v=1.0.8-RELEASE&c=&e=jar&isLocal=true
     * <p>
     * xxx/nexus/service/local/repositories/releases/content/
     * com/csii/pe/pe-redis-core/1.0.8-RELEASE/pe-redis-core-1.0.8-RELEASE.jar?
     * describe=maven2&isLocal=true&_dc=1575509317141
     */
    @Override
    public Date getLastChangeDate(JSONObject jsonObject) {
        Date date = new Date();
        String groupId = jsonObject.getString(Dict.GROUPID);
        String artifactId = jsonObject.getString(Dict.ARTIFACTID);
        String version = jsonObject.getString(Dict.VERSION);
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("artifactHits");
            if (jsonArray != null && jsonArray.size() > 0) {
                JSONObject object = jsonArray.getJSONObject(0);
                String repositoryId = object.getString("repositoryId");
                String resovleUrl = nexus_version_resolve + "?r=" + repositoryId + "&g=" + groupId +
                        "&a=" + artifactId + "&v=" + version + "&e=jar&isLocal=true";
                String resolve_Body = Util.httpMethodGetExchange(resovleUrl, restTemplate);
                if (StringUtils.isNotBlank(resolve_Body)) {
                    JSONObject resolveObject = JSONObject.fromObject(resolve_Body);
                    JSONObject resolveDataObject = resolveObject.getJSONObject("data");
                    String repositoryPath = resolveDataObject.getString("repositoryPath");
                    if (StringUtils.isNotBlank(repositoryPath)) {
                        String detailUrl = nexus_version_detail + "/" + repositoryId + "/content" + repositoryPath + "?describe=info&isLocal=true";
                        String detailBody = Util.httpMethodGetExchange(detailUrl, restTemplate);
                        if (StringUtils.isNotBlank(detailBody)) {
                            JSONObject detailObject = JSONObject.fromObject(detailBody);
                            JSONObject dataObject = detailObject.getJSONObject("data");
                            Long lastChanged = dataObject.getLong("lastChanged");
                            date.setTime(lastChanged);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("{}获取{}版本时间错误,{}", artifactId, version, e.getMessage());
        }
        return date;
    }

    /**
     * 调用python脚本 返回mvn dependency:list在dir路径下的执行结果
     *
     * @param dir
     * @param type 组件或骨架（component,archetype）
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public String getPythonResult(String dir, String type) throws IOException, InterruptedException {
        String script;
        switch (type) {
            case Constants.COMPONENT_COMPONENT:
                script = "scannerScript.py";
                break;
            case Constants.COMPONENT_ARCHETYPE:
                script = "scanArchetype.py";
                break;
            case Constants.COMPONENT_MPASS_COMPONENT:
                script = "scanVue.py";
                break;
            default:
                throw new FdevException(ErrorConstants.PARAM_ERROR, new String[]{type, "扫描组件和骨架脚本传参状态type值异常"});
        }
        String result = null;
        String[] cmdA = {"python", python_path + script, dir};
        Process pr = Runtime.getRuntime().exec(cmdA);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));) {
            result = in.readLine();
            in.close();
            pr.waitFor();
        } catch (Exception e) {
            logger.info("调用python脚本 返回mvn dependency:list在dir路径下的执行结果", e);
        }

        return result;
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
