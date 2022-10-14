package com.spdb.fdev.component;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.dao.IComponentRecordDao;
import com.spdb.fdev.component.entity.ComponentApplication;
import com.spdb.fdev.component.entity.ComponentInfo;
import com.spdb.fdev.component.entity.ComponentRecord;
import com.spdb.fdev.component.service.IAppService;
import com.spdb.fdev.component.service.IComponentApplicationService;
import com.spdb.fdev.component.service.IComponentInfoService;
import com.spdb.fdev.component.service.IComponentRecordService;
import com.spdb.fdev.component.service.ExportExcelPip;
import com.spdb.fdev.transport.RestTransport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest()
public class ComponentApplicationTest {

    @Autowired
    private ComponentInfo componentInfo;

    @Autowired
    private IAppService appService;

    @Autowired
    private IComponentApplicationService componentApplicationService;

    @Autowired
    private IComponentRecordService componentRecordService;

    @Autowired
    private IComponentInfoService componentInfoService;

    @Autowired
    private IComponentRecordDao componentRecordDao;

    @Autowired
    private RestTransport restTransport;

    @Test
    public void exportExcel() throws Exception {
        List<Map<String, Object>> appList = appService.getAppList();
        List<Map> result = new ArrayList<>();
        for (Map app : appList) {
            String id = (String) app.get(Dict.ID);
            String app_name_en = (String) app.get(Dict.NAME_EN);
            String app_name_cn = (String) app.get(Dict.NAME_ZH);
            Map group = (Map) app.get(Dict.GROUP);
            String group_name = (String) group.get(Dict.NAME);
            ArrayList<Map<String, String>> spdb_managers = (ArrayList<Map<String, String>>) app.get("spdb_managers");
            String spdb = getName(spdb_managers);
            ArrayList<Map<String, String>> dev_managers = (ArrayList<Map<String, String>>) app.get("dev_managers");
            String dev = getName(dev_managers);
            ComponentApplication componentApplication = new ComponentApplication();
            componentApplication.setApplication_id(id);
            //componentApplication.setComponent_id("5edd91da946e7c000ea46c7c");
            List<ComponentApplication> componentApplicationList = componentApplicationService.query(componentApplication);
            List<Map> appResult = new ArrayList<>();
            for (ComponentApplication application : componentApplicationList) {
                ComponentRecord record = componentRecordService.queryByComponentIdAndVersion(application.getComponent_id(), application.getComponent_version());
                if (record != null) {
                    ComponentInfo info = componentInfoService.queryById(record.getComponent_id());
                    ArrayList typeList = new ArrayList(Arrays.asList(new String[]{"0", "1", "2", "3"}));
                    if (info != null && typeList.contains(record.getType())) {
                        Map map = CommonUtils.object2Map(application);
                        map.put("app_name_en", app_name_en);
                        map.put("app_name_cn", app_name_cn);
                        map.put("group_name", group_name);
                        map.put("dev", dev);
                        map.put("spdb", spdb);
                        map.put("manager", getName(info.getManager_id()));
                        map.put(Dict.NAME_EN, info.getName_en());
                        map.put(Dict.NAME_CN, info.getName_cn());

                        map.put(Dict.TYPE, record.getType());
                        if ("0".equals(record.getType())) {
                            map.put(Dict.TYPE_NAME, "正式版本");
                        }
                        if ("1".equals(record.getType())) {
                            map.put(Dict.TYPE_NAME, "推荐版本");
                        }
                        if ("2".equals(record.getType())) {
                            map.put(Dict.TYPE_NAME, "废弃版本");
                        }
                        if ("3".equals(record.getType())) {
                            map.put(Dict.TYPE_NAME, "测试版本");
                        }
                        appResult.add(map);
                    }
                } else {
                    componentApplicationService.delete(application);
                }
            }
            result.addAll(appResult);
        }

        Map<String, String> titleMap = new LinkedHashMap<>();
        titleMap.put("app_name_cn", "应用中文名");
        titleMap.put("app_name_en", "应用英文名");
        titleMap.put("dev", "应用负责人");
        titleMap.put("spdb", "应用行内负责人");
        titleMap.put("name_cn", "组件中文名");
        titleMap.put("name_en", "组件英文名");
        titleMap.put("manager", "组件负责人");
        titleMap.put("component_version", "组件版本");
        titleMap.put("type_name", "组件版本类型");
        titleMap.put("group_name", "项目所属小组");
        String sheetName = "应用使用组件汇总";
        ExportExcelPip.excelExport(result, titleMap, sheetName);
    }

    public String getName(ArrayList<Map<String, String>> manager) {
        StringBuffer stringBuffer = new StringBuffer();
        if (!CommonUtils.isNullOrEmpty(manager)) {
            for (Map map : manager) {
                stringBuffer.append(map.get("user_name_cn")).append(";");
            }
        }
        return stringBuffer.length() > 0 ? stringBuffer.substring(0, stringBuffer.length() - 1) : stringBuffer.toString();
    }

    public String getName(HashSet<Map<String, String>> manager) {
        StringBuffer stringBuffer = new StringBuffer();
        if (!CommonUtils.isNullOrEmpty(manager)) {
            for (Map map : manager) {
                stringBuffer.append(map.get("user_name_cn")).append(";");
            }
        }
        return stringBuffer.length() > 0 ? stringBuffer.substring(0, stringBuffer.length() - 1) : stringBuffer.toString();
    }

}