package com.spdb.fdev.fdevtask.base.utils;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import com.spdb.fdev.fdevtask.spdb.service.IAppApi;
import com.spdb.fdev.fdevtask.spdb.service.IFdevTaskService;
import com.spdb.fdev.fdevtask.spdb.service.IRedmineApi;
import com.spdb.fdev.fdevtask.spdb.service.IUserApi;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

@Component
public class ExcelUtils {

    private HSSFWorkbook workbook = null;

    @Autowired
    public IRedmineApi iRedmineApi;

    @Autowired
    public IFdevTaskService iFdevTaskService;

    @Autowired
    public IUserApi iUserApi;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 默认 Excel2003
     *
     * @return
     */
    public void createWorkbook(List sheets) {
        workbook = new HSSFWorkbook();
        sheets.forEach(n -> workbook.createSheet((String) n));
    }

    public void fillData(String sheetName, List title, List dataList) {
        if (CommonUtils.isNullOrEmpty(workbook)) {
            createWorkbook(new ArrayList());
        }
        HSSFSheet sheet = workbook.getSheet(sheetName);
        HSSFRow rowTmp = sheet.createRow(0);
        HSSFCellStyle titleStyle = (this.setTitleStyle());
//        title.forEach( n-> rowTmp.createCell(title.indexOf(n)).setCellValue((String) n));
        for (Object t : title) {
            HSSFCell cell = rowTmp.createCell(title.indexOf(t));
            cell.setCellValue((String) t);
            cell.setCellStyle(titleStyle);
        }
        for (int i = 0; i < dataList.size(); i++) {
            HSSFRow row = sheet.createRow(i + 1);
            row.setRowStyle(this.setContentStyle());
            List groupRow = (List) dataList.get(i);
            //序号
            row.createCell(0).setCellValue(i + 1);
            for (int j = 0; j < groupRow.size(); j++) {
                HSSFCell cell = row.createCell(j + 1);
                Object item = groupRow.get(j);
                if (item instanceof String) {
                    cell.setCellValue((String) item);
                } else if (item instanceof Integer) {
                    cell.setCellValue((Integer) item);
                }
            }
        }

    }

    public HSSFCellStyle setContentStyle() {
        if (CommonUtils.isNullOrEmpty(workbook)) {
            this.createWorkbook(new ArrayList());
        }
        HSSFCellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public HSSFCellStyle setTitleStyle() {
        if (CommonUtils.isNullOrEmpty(workbook)) {
            this.createWorkbook(new ArrayList());
        }
        HSSFCellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public void write(HttpServletResponse resp) throws IOException {
        OutputStream out = resp.getOutputStream();
        workbook.write(out);
        out.close();
    }

    public Map childTitleInAll() {
        Map result = new LinkedHashMap();
        result.put(Dict.NAME, "任务名称");
        result.put(Dict.SPDB_MANAGERS, "行内项目负责人");
        result.put(Dict.DEV_MANAGERS, "应用负责人");
        result.put(Dict.PLAN_START_TIME, "计划启动日期");
        result.put(Dict.PLAN_INNER_TEST_TIME, "计划内测启动日期");
        result.put(Dict.PLAN_UAT_TEST_START_TIME, "计划uat测试启动日期");
        result.put(Dict.PLAN_UAT_TEST_STOP_TIME, "计划uat测试完成日期");
        result.put(Dict.PLAN_REL_TEST_TIME, "计划rel测试启动日期");
        result.put(Dict.PLAN_FIRE_TIME, "计划投产日期");
        result.put(Dict.DESC, "描述");
        result.put(Dict.REQUIRE_INFO_NO, "需求信息单编号");
        result.put(Dict.REQUIRE_INFO_TITLE, "需求信息单标题");
        result.put(Dict.REDMINE_CONTENT, "研发单元内容");
        result.put(Dict.REFER_SYSTEM, "涉及系统");
        result.put(Dict.REDMINE_FOLLOW, "研发单元跟踪人");
        result.put(Dict.SPDB_WORKTIME, "行内工作量");
        result.put(Dict.COMPANY_WORKTIME, "厂商工作量");
        result.put(Dict.REDMINE_ID, "研发单元编号");
        result.put(Dict.WORK_LEADER, "实施牵头人");
        result.put(Dict.REQUIRE_DOC, "需求说明书名称");
        return result;

    }

    public List childDataInAll(List ids) throws Exception {

        List result = new ArrayList();
        if (CommonUtils.isNullOrEmpty(ids)) {
            return result;
        }
        List<FdevTask> tasks = iFdevTaskService.queryTasksByIds((ArrayList) ids);
        if (CommonUtils.isNullOrEmpty(tasks)) {
            return result;
        }
        for (FdevTask task : tasks) {
            Map dataMap = new LinkedHashMap();
            dataMap.put(Dict.NAME, task.getName());
            dataMap.put(Dict.SPDB_MANAGERS, conactUserNames(task.getSpdb_master()));
            dataMap.put(Dict.DEV_MANAGERS, conactUserNames(task.getMaster()));
            dataMap.put(Dict.PLAN_START_TIME, task.getPlan_start_time());
            dataMap.put(Dict.PLAN_INNER_TEST_TIME, task.getPlan_inner_test_time());
            dataMap.put(Dict.PLAN_UAT_TEST_START_TIME, task.getPlan_uat_test_start_time());
            dataMap.put(Dict.PLAN_UAT_TEST_STOP_TIME, task.getPlan_uat_test_stop_time());
            dataMap.put(Dict.PLAN_REL_TEST_TIME, task.getPlan_rel_test_time());
            dataMap.put(Dict.PLAN_FIRE_TIME, task.getFire_time());
            dataMap.put(Dict.DESC, task.getDesc());
            if (!CommonUtils.isNullOrEmpty(task.getRedmine_id()) && null != queryDataFromRedis(task.getRedmine_id())) {
//                dataMap.putAll(iRedmineApi.getRedmineInfoForExcel(task.getRedmine_id()));
                dataMap.putAll(this.queryDataFromRedis(task.getRedmine_id()));
            } else {
                dataMap.put(Dict.REDMINE_ID, "");
                dataMap.put(Dict.WORK_LEADER, "");
                dataMap.put(Dict.REQUIRE_DOC, "");
            }
            result.add(dataMap);
        }
        return result;
    }

    public String conactUserNames(String[] ids) {
        List names = new ArrayList();
        for (int i = 0; i < ids.length; i++) {
            Map param = new HashMap();
            param.put(Dict.ID, ids[i]);
            try {
                Map map = iUserApi.queryUser(param);
                names.add(map.get(Dict.USER_NAME_CN).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return StringUtils.join(names, "、");
    }

    public Collection childTitleTodoAndDevelop(Collection result) {
        result.remove(Dict.PLAN_INNER_TEST_TIME);
        result.remove(Dict.PLAN_UAT_TEST_START_TIME);
        result.remove(Dict.PLAN_UAT_TEST_STOP_TIME);
        result.remove(Dict.PLAN_REL_TEST_TIME);
        result.remove(Dict.PLAN_FIRE_TIME);
        return result;
    }

    public Collection childTitleSit(Collection result) {
        result.remove(Dict.PLAN_UAT_TEST_STOP_TIME);
        result.remove(Dict.PLAN_REL_TEST_TIME);
        result.remove(Dict.PLAN_FIRE_TIME);
        return result;
    }

    public Collection childTitleUat(Collection result) {
        result.remove(Dict.PLAN_START_TIME);
        result.remove(Dict.PLAN_INNER_TEST_TIME);
        result.remove(Dict.PLAN_REL_TEST_TIME);
        result.remove(Dict.PLAN_FIRE_TIME);
        return result;
    }

    public Collection childTitleRel(Collection result) {
        result.remove(Dict.PLAN_INNER_TEST_TIME);
        result.remove(Dict.PLAN_START_TIME);
        result.remove(Dict.PLAN_UAT_TEST_START_TIME);
        result.remove(Dict.PLAN_UAT_TEST_STOP_TIME);
        result.remove(Dict.PLAN_FIRE_TIME);
        return result;
    }

    public Collection childTitlePro(Collection result) {
        result.remove(Dict.PLAN_INNER_TEST_TIME);
        result.remove(Dict.PLAN_UAT_TEST_START_TIME);
        result.remove(Dict.PLAN_UAT_TEST_STOP_TIME);
        result.remove(Dict.PLAN_REL_TEST_TIME);
        result.remove(Dict.PLAN_START_TIME);
        return result;
    }

    public List sortChildData(Collection collection) {
        List result = new ArrayList();
        for (Object c : collection) {
            if (c instanceof Map.Entry) {
                result.add(((Map.Entry) c).getValue());
            } else {
                result.add(c);
            }
        }
        return result;
    }

    public List childTitle() {
        List result = new ArrayList();
        result.add(Dict.NAME);
        result.add(Dict.SPDB_MANAGERS);
        result.add(Dict.DEV_MANAGERS);
        result.add(Dict.PLAN_START_TIME);
        result.add(Dict.PLAN_INNER_TEST_TIME);
        result.add(Dict.PLAN_UAT_TEST_START_TIME);
        result.add(Dict.PLAN_UAT_TEST_STOP_TIME);
        result.add(Dict.PLAN_REL_TEST_TIME);
        result.add(Dict.PLAN_FIRE_TIME);
        result.add(Dict.DESC);
//      REQUIRE_INFO_NO    需求信息单编号:信息单2017科技224  41
//      REQUIRE_INFO_TITLE 需求信息单标题:网银证书系统优化 120
//      REDMINE_CONTENT    研发单元内容 43
//      REFER_SYSTEM       涉及系统 60
//      REDMINE_FOLLOW     研发单元跟踪人 126
//      SPDB_WORKTIME      行内工作量 235
//      COMPANY_WORKTIME   厂商工作量 236
        result.add(Dict.REQUIRE_INFO_NO);
        result.add(Dict.REQUIRE_INFO_TITLE);
        result.add(Dict.REDMINE_CONTENT);
        result.add(Dict.REFER_SYSTEM);
        result.add(Dict.REDMINE_FOLLOW);
        result.add(Dict.SPDB_WORKTIME);
        result.add(Dict.COMPANY_WORKTIME);
        result.add(Dict.REDMINE_ID);
        result.add(Dict.WORK_LEADER);
        result.add(Dict.REQUIRE_DOC);
        return result;
    }

    public List generatechildData(List titles, List childDataList, int index) throws Exception {
        List childData = new ArrayList();
        for (Object item : childDataList) {
            List tmp = (ArrayList) item;
            String groupName = (String) tmp.get(0);
            List taskIds = (ArrayList) tmp.get(index + 1);
            List idsData = this.childDataInAll(taskIds);
            if (CommonUtils.isNullOrEmpty(idsData)) break;
            for (Object idData : idsData) {
                List data = new ArrayList();
                for (Object cell : titles) {
                    data.add(((Map) idData).get(cell));
                }
                data.add(0, groupName);
                childData.add(data);
            }
        }
        return childData;
    }

    public List translateTitle(List title) {
        List titleCN = new ArrayList();
        title.forEach(n -> titleCN.add(this.childTitleInAll().get(n)));
        return titleCN;
    }

    public Map queryDataFromRedis(String redminId) {
        Map result = redisTemplate.opsForHash().entries("redmine." + redminId);
        if (CommonUtils.isNullOrEmpty(result)) {
            result = iRedmineApi.getRedmineInfoForExcel(redminId);
        }
        return result;
    }
}
