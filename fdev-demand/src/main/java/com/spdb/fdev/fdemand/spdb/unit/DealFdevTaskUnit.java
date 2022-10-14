package com.spdb.fdev.fdemand.spdb.unit;

import com.spdb.fdev.fdemand.base.dict.DemandEnum;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.ImplementUnitEnum;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.TimeUtil;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import com.spdb.fdev.fdemand.spdb.service.ITaskService;
import javassist.bytecode.stackmap.TypeData;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DealFdevTaskUnit {

    @Autowired
    private ITaskService taskService;

    /**
     * 将任务的阶段和时间进行转换
     *
     * @param taskList
     * @return
     */
    public List<Map> getTrueTaskInfo(List<Map> taskList) {
        List<Map> result = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(taskList)) {
            List<Map> collect = taskList.stream().filter(task -> CommonUtils.isNullOrEmpty(task.get("fdev_implement_unit_no"))).collect(Collectors.toList());
            if (!CommonUtils.isNullOrEmpty(collect)) {
                for (int i = 0; i < collect.size(); i++) {
                    Map task = collect.get(i);
                    Map map = new HashMap();
                    Integer stage = CommonUtils.getStage((String) task.get("stage"));
                    map.put("stage", stage);
                    String startTime = (String) task.get("start_time");
                    String sitTime = (String) task.get("start_inner_test_time");
                    String uatTime = (String) task.get("start_uat_test_time");
                    String relTime = (String) task.get("start_rel_test_time");
                    String productionTime = (String) task.get("fire_time");
                    map.put("start_time", null != startTime ? TimeUtil.timeConvert(startTime) : null);
                    map.put("start_inner_test_time", null != sitTime ? TimeUtil.timeConvert(startTime) : null);
                    map.put("start_uat_test_time", null != uatTime ? TimeUtil.timeConvert(startTime) : null);
                    map.put("start_rel_test_time", null != relTime ? TimeUtil.timeConvert(startTime) : null);
                    map.put("fire_time", null != productionTime ? TimeUtil.timeConvert(startTime) : null);
                    //如果当前在sit阶段，且uat_test_time不为空，则任务进入到uat阶段
                    if (stage.equals(4) && StringUtils.isNotBlank((String) task.get("uat_test_time"))) {
                        map.put("stage", 5);
                        map.put("start_uat_test_time", task.get("uat_test_time"));
                    }
                    result.add(map);
                }
            }
        }
        return result;
    }


    //获取最早开发时间
    public String getRealStartDate(List<Map> collect) {
        List<String> list = collect.stream()
                .filter(task -> !CommonUtils.isNullOrEmpty(task.get("start_time")))
                .flatMap(task -> Arrays.asList((String) task.get("start_time")).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return list.get(0);
    }

    //获取最晚内测时间
    public String getRealInnerTestDate(List<Map> collect) {
        List<String> list = collect.stream()
                .filter(task -> !CommonUtils.isNullOrEmpty(task.get("start_inner_test_time")))
                .flatMap(task -> Arrays.asList((String) task.get("start_inner_test_time")).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return list.get(list.size() - 1);
    }

    //获取最晚业测时间
    public String getRealTestDate(List<Map> collect) {
        List<String> list = collect.stream()
                .filter(task -> !CommonUtils.isNullOrEmpty(task.get("start_uat_test_time")))
                .flatMap(task -> Arrays.asList((String) task.get("start_uat_test_time")).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return list.get(list.size() - 1);
    }

    // 获取测试完成时间
    public String getRealTestFinishDate(List<Map> collect) {
        List<String> list = collect.stream()
                .filter(task -> !CommonUtils.isNullOrEmpty(task.get("start_rel_test_time")))
                .flatMap(task -> Arrays.asList((String) task.get("start_rel_test_time")).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return list.get(list.size() - 1);
    }

    // 获取投产时间
    public String getRealProductDate(List<Map> collect) {
        List<String> list = collect.stream()
                .filter(task -> !CommonUtils.isNullOrEmpty(task.get("fire_time")))
                .flatMap(task -> Arrays.asList((String) task.get("fire_time")).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return list.get(list.size() - 1);
    }
}
