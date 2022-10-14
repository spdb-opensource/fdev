package com.spdb.fdev.component;

import com.spdb.fdev.base.utils.GitUtilsNew;
import com.spdb.fdev.base.utils.Util;
import com.spdb.fdev.component.dao.IComponentRecordDao;
import com.spdb.fdev.component.entity.ComponentApplication;
import com.spdb.fdev.component.entity.ComponentInfo;
import com.spdb.fdev.component.entity.ComponentRecord;
import com.spdb.fdev.component.service.IAppService;
import com.spdb.fdev.component.service.IComponentInfoService;
import com.spdb.fdev.component.service.IComponentScanService;
import com.spdb.fdev.component.service.IGitlabSerice;
import com.spdb.fdev.component.service.impl.ComponnetScanServiceImpl;
import com.spdb.fdev.transport.RestTransport;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest()
public class ComponentInfoControllerTest {

    @Autowired
    private IComponentInfoService componentInfoService;

    @Autowired
    private IAppService appService;

    @Autowired
    private RestTransport restTransport;

    @Autowired
    @Lazy
    private IComponentScanService componentScanService;

    @Autowired
    private IGitlabSerice gitlabSerice;

    @Value("${nas.apps.path}")
    private String nas_apps_path;

    @Value("${nexus.search}")
    private String nexus_search;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IComponentRecordDao componentRecordDao;

    @Autowired
    private IComponentScanService componnetScanService;

    /**
     * 遍历所有应用模块的应用，进行gitclone或git pull操作
     */
    @Test
    public void gitOpereation() throws Exception {
        List<Map<String, Object>> appList = appService.getAppList();
        for (int i = 0; i < appList.size(); i++) {
            Map map = appList.get(i);
            try {
                String name_en = (String) map.get("name_en");
                String git = (String) map.get("git");
                // 本地仓库的 地址
                String localRepository = nas_apps_path + name_en;
                File partDirGit = new File(localRepository + "/.git");
                // 首次本地没有项目的时候进行克隆
                if (!partDirGit.exists()) {
                    // git clone
                    GitUtilsNew.gitCloneFromGitlab(git, localRepository);
                }
                GitUtilsNew.gitPullFromGitlab(localRepository);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 调用maven接口获取所有历史版本
     */
    @Test
    public void getComponentHistory() {
        String groupId = "com.fdev";
        String artifactId = "fdev-common";
        String body = Util.httpMethodGetExchange(nexus_search + "?g=" + groupId + "&a=" + artifactId, restTemplate);
        JSONObject jsonObject = JSONObject.fromObject(body);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject job = jsonArray.getJSONObject(i);
            System.out.println(job.getString("groupId"));
            System.out.println(job.getString("artifactId"));
            System.out.println(job.getString("version"));
        }
    }

    /**
     * 调用maven接口获取组件历史版本并入库
     */
    @Test
    public void initComponentHistory() throws Exception {
//        String id = "5da98880673c360f8cc84vq";
//        ComponentInfo componentInfo = componentInfoService.queryById(id);
//        componentScanService.initComponentHistory(componentInfo);
        ComponentRecord record = componentRecordDao.queryByComponentIdAndVersion("5db8f495673c3618f0f85db2", "10.0.3-RELEASE");
        record.setDate(Util.simdateFormat(new Date()));
        componentRecordDao.update(record);
    }

    /**
     * 添加组件应用关系
     */
    @Test
    public void initComponentApplication() throws Exception {
        String id = "5da98880673c360f8cc84vq";
        ComponentInfo componentInfo = componentInfoService.queryById(id);
        componentScanService.initComponentApplication(componentInfo);
    }

    /**
     * 获取组件关联关系
     */
    @Test
    public void getComponentApplication() throws Exception {
        ComponentInfo componentInfo = componentInfoService.queryById("5dcbd7887cedb10013adbcfb");
//        String id = "5dae9e3c17a68800097526aa";
//        String name_en = "spdb-netbank-newent";
        String id = "5cd0de93c11d450006125487";
        String name_en = "nbh-param-sso";
        ComponentApplication componentApplication = componentScanService.getComponentApplication(id, nas_apps_path + name_en,
                componentInfo);
    }

    @Test
    public void chekcoutBranch() {
        String name_en = "fdev-prototype";
        String git = "xxx/ebank/devops/testGroupss/fdev-prototype.git";
        String version = "1.1.3-SNAPSHOT";
        componnetScanService.gitOperation(name_en, git, version);
    }
}
