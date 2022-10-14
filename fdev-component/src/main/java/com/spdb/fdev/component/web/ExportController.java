package com.spdb.fdev.component.web;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.base.utils.CommonUtils;
import com.spdb.fdev.component.entity.ComponentApplication;
import com.spdb.fdev.component.entity.ComponentInfo;
import com.spdb.fdev.component.entity.ComponentRecord;
import com.spdb.fdev.component.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private IAppService appService;
    @Autowired
    private IComponentApplicationService componentApplicationService;
    @Autowired
    private IComponentRecordService componentRecordService;
    @Autowired
    private IComponentInfoService componentInfoService;
    @Autowired
    private IExportFileService exportFileService;

    /**
     * 导出后端组件使用应用汇总
     *
     * @param requestMap
     * @param resp
     * @throws Exception
     */
    @PostMapping("/exportExcelByComponent")
    public void exportExcelByComponent(@RequestBody Map requestMap, HttpServletResponse resp) throws Exception {
        //获取组件信息
        String componentId = (String) requestMap.get(Dict.COMPONENT_ID);
        ComponentInfo componentItem = componentInfoService.queryById(componentId);
        List<Map> result = new ArrayList<>();
        if (null != componentItem) {
            //根据组件id 获取组件关联的 应用信息
            ComponentApplication componentApplication = new ComponentApplication();
            componentApplication.setComponent_id(componentId);
            List<ComponentApplication> componentApplicationList = componentApplicationService.query(componentApplication);
            for (ComponentApplication application : componentApplicationList) {
                //获取单条应用信息
                Map appInfo = appService.queryAPPbyid(application.getApplication_id());
                if (appInfo != null) {
                    Map group = (Map) appInfo.get(Dict.GROUP);
                    String group_name = (String) group.get(Dict.NAME);
                    ArrayList<Map<String, String>> dev_managers = (ArrayList<Map<String, String>>) appInfo.get("dev_managers");
                    String dev = getName(dev_managers);
                    ArrayList<Map<String, String>> spdb_managers = (ArrayList<Map<String, String>>) appInfo.get("spdb_managers");
                    String spdb = getName(spdb_managers);
                    Map dateMap = CommonUtils.object2Map(application);
                    dateMap.put(Dict.APP_NAME_EN, appInfo.get(Dict.NAME_EN));
                    dateMap.put(Dict.APP_NAME_CN, appInfo.get(Dict.NAME_ZH));
                    dateMap.put(Dict.GROUP_NAME, group_name);
                    dateMap.put(Dict.DEV, dev);
                    dateMap.put(Dict.SPDB, spdb);
                    dateMap.put(Dict.MANAGER, getName(componentItem.getManager_id()));
                    dateMap.put(Dict.NAME_EN, componentItem.getName_en());
                    dateMap.put(Dict.NAME_CN, componentItem.getName_cn());
                    ComponentRecord record = componentRecordService.queryByComponentIdAndVersion(application.getComponent_id(), application.getComponent_version());
                    if (!CommonUtils.isNullOrEmpty(record)) {
                        dateMap.put(Dict.TYPE, record.getType());
                        putTypeName(dateMap, record.getType());
                    }
                    result.add(dateMap);
                }
            }
        }
        //导出Excel头信息
        Map<String, String> titleMap = new LinkedHashMap<>();
        titleMap.put(Dict.APP_NAME_CN, "应用中文名");
        titleMap.put(Dict.APP_NAME_EN, "应用英文名");
        titleMap.put(Dict.DEV, "应用负责人");
        titleMap.put(Dict.SPDB, "应用行内负责人");
        titleMap.put(Dict.NAME_CN, "组件中文名");
        titleMap.put(Dict.NAME_EN, "组件英文名");
        titleMap.put(Dict.MANAGER, "组件负责人");
        titleMap.put(Dict.COMPONENT_VERSION, "组件版本");
        titleMap.put(Dict.TYPE_NAME, "组件版本类型");
        titleMap.put(Dict.GROUP_NAME, "项目所属小组");
        String sheetName = "组件使用应用汇总";
        exportFileService.exportExcelByComponent(result, titleMap, sheetName, resp);
    }

    @GetMapping("/exportExcel")
    public void exportExcel(HttpServletResponse resp) throws Exception {
        //获取组件信息
        String componentId = "5def0a7a14c5780015e2178a";
        ComponentInfo componentItem = componentInfoService.queryById(componentId);
        List<Map> result = new ArrayList<>();
        if (null != componentItem) {
            //根据组件id 获取组件关联的 应用信息
            ComponentApplication componentApplication = new ComponentApplication();
            componentApplication.setComponent_id(componentId);
            List<ComponentApplication> componentApplicationList = componentApplicationService.query(componentApplication);
            for (ComponentApplication application : componentApplicationList) {
                //获取单条应用信息
                Map appInfo = appService.queryAPPbyid(application.getApplication_id());
                if (appInfo != null) {
                    Map group = (Map) appInfo.get(Dict.GROUP);
                    String group_name = (String) group.get(Dict.NAME);
                    ArrayList<Map<String, String>> dev_managers = (ArrayList<Map<String, String>>) appInfo.get("dev_managers");
                    String dev = getName(dev_managers);
                    ArrayList<Map<String, String>> spdb_managers = (ArrayList<Map<String, String>>) appInfo.get("spdb_managers");
                    String spdb = getName(spdb_managers);
                    Map dateMap = CommonUtils.object2Map(application);
                    dateMap.put(Dict.APP_NAME_EN, appInfo.get(Dict.NAME_EN));
                    dateMap.put(Dict.APP_NAME_CN, appInfo.get(Dict.NAME_ZH));
                    dateMap.put(Dict.GROUP_NAME, group_name);
                    dateMap.put(Dict.DEV, dev);
                    dateMap.put(Dict.SPDB, spdb);
                    dateMap.put(Dict.MANAGER, getName(componentItem.getManager_id()));
                    dateMap.put(Dict.NAME_EN, componentItem.getName_en());
                    dateMap.put(Dict.NAME_CN, componentItem.getName_cn());
                    ComponentRecord record = componentRecordService.queryByComponentIdAndVersion(application.getComponent_id(), application.getComponent_version());
                    dateMap.put(Dict.TYPE, record.getType());
                    putTypeName(dateMap, record.getType());
                    result.add(dateMap);
                }
            }
        }
        //导出Excel头信息
        Map<Integer, String> titleMap = new LinkedHashMap<>();
        titleMap.put(0, Dict.NAME_CN + "--组件中文名");
        titleMap.put(1, Dict.NAME_EN + "--组件英文名");
        titleMap.put(2, Dict.MANAGER + "--组件负责人");
        titleMap.put(3, Dict.COMPONENT_VERSION + "--组件版本");
        titleMap.put(4, Dict.TYPE_NAME + "--组件版本类型");
        titleMap.put(5, Dict.APP_NAME_CN + "--应用中文名");
        titleMap.put(6, Dict.APP_NAME_EN + "--应用英文名");
        titleMap.put(7, Dict.DEV + "--应用负责人");
        titleMap.put(8, Dict.SPDB + "--应用行内负责人");
        titleMap.put(9, Dict.GROUP_NAME + "--项目所属小组");
        String sheetName = "组件使用应用汇总";
        exportFileService.exportExcel(result, titleMap, sheetName, resp, "componentApplication");
    }


    private String getName(HashSet<Map<String, String>> manager) {
        StringBuffer stringBuffer = new StringBuffer();
        if (!CommonUtils.isNullOrEmpty(manager)) {
            for (Map map : manager) {
                stringBuffer.append(map.get("user_name_cn")).append(";");
            }
        }
        return stringBuffer.length() > 0 ? stringBuffer.substring(0, stringBuffer.length() - 1) : stringBuffer.toString();
    }

    private String getName(ArrayList<Map<String, String>> manager) {
        StringBuffer stringBuffer = new StringBuffer();
        if (!CommonUtils.isNullOrEmpty(manager)) {
            for (Map map : manager) {
                stringBuffer.append(map.get("user_name_cn")).append(";");
            }
        }
        return stringBuffer.length() > 0 ? stringBuffer.substring(0, stringBuffer.length() - 1) : stringBuffer.toString();
    }


    private void putTypeName(Map map, String type) {
        if (CommonUtils.isNullOrEmpty(type)) {
            map.put(Dict.TYPE_NAME, "");
        }
        if ("0".equals(type)) {
            map.put(Dict.TYPE_NAME, "正式版本");
        }
        if ("1".equals(type)) {
            map.put(Dict.TYPE_NAME, "推荐版本");
        }
        if ("2".equals(type)) {
            map.put(Dict.TYPE_NAME, "废弃版本");
        }
        if ("3".equals(type)) {
            map.put(Dict.TYPE_NAME, "测试版本");
        }
    }
}
