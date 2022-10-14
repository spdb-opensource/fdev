package com.spdb.fdev.fdevtask.base.unit;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevtask.base.dict.Constants;
import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.base.dict.ErrorConstants;
import com.spdb.fdev.fdevtask.base.utils.CommonUtils;
import com.spdb.fdev.fdevtask.base.utils.Tuple;
import com.spdb.fdev.fdevtask.spdb.entity.FdevTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 处理任务相关组件
 */
@Component
public class DealTaskUnit {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean flag = false;

    //    获取当前需求应该处于的阶段,返回当前正确的需求阶段和需要更新的实际时间。
    public Tuple.Tuple2<String, HashMap<String, String>> getStageAndDate(List<FdevTask> taskList, List<FdevTask> todoTasks) {
        HashMap<String, String> param = new HashMap<>();
        // 1.根据不同stage分组
//        Map<String, List<FdevTask>> collect = taskList.stream()
//                .collect(Collectors.groupingBy(task -> task.getStage()));
//        logger.info("打印分组后的参数:{}",JSON.toJSONString(collect,true));
        //只存在归档则从归档中选择时间更新，需求阶段为已投产
//        if (collect.size() == 1 && collect.containsKey("file") && todoTasks.size() == 0) {
//            logger.info("只存在归档状态");
//            List<FdevTask> file = collect.get("file");
//            //实际启动日期
//            param.put("real_start_date", getEstInitRealDate(file));
//            //实际进入uat日期
//            param.put("real_test_date", getEstUatRealDate(file));
//            //实际投产日期
//            param.put("real_product_date", getEstRelComplRealDate(file));
//            //实际提交内测信息sit
//            param.put("real_inner_test_date", getSitRealDate(file));
//            //实际测试完成时间rel
//            param.put("real_test_finish_date", getRelRealDate(file));
//            return Tuple.tuple("production", param);
//        }
        //最小任务阶段
        String stage = taskList.stream()
                .flatMap(task -> Arrays.asList(task.getStage()).stream())
                .sorted(Comparator.comparingInt(CommonUtils::pareStage))
                .limit(1)
                .collect(Collectors.joining());
        //最大任务阶段
        String maxStage = taskList.stream()
                .flatMap(task -> Arrays.asList(task.getStage()).stream())
                .sorted(Comparator.comparingInt(CommonUtils::pareStage).reversed())
                .limit(1)
                .collect(Collectors.joining());
        //非日常任务最大阶段
        String maxStage2 = taskList.stream()
                .filter(task -> !"2".equals(String.valueOf(task.getTaskType())))
                .flatMap(task -> Arrays.asList(task.getStage()).stream())
                .sorted(Comparator.comparingInt(CommonUtils::pareStage).reversed())
                .limit(1)
                .collect(Collectors.joining());
        //是否日常需求
        boolean dailyDemand = taskList.size() == taskList.stream().filter(task -> "2".equals(String.valueOf(task.getTaskType()))).count();
        if(dailyDemand) {
            //日常需求只推开发中和已投产2个阶段
            if (Dict.TASK_STAGE_DEVELOP.equals(maxStage)
                    || (Dict.TASK_STAGE_PRODUCTION.equals(maxStage) && Dict.TASK_STAGE_DEVELOP.equals(stage))) {
                param.put(Dict.REAL_START_DATE, getEstInitRealDate(taskList));
                return Tuple.tuple(Dict.TASK_STAGE_DEVELOP, param);
            }
            if (Dict.TASK_STAGE_PRODUCTION.equals(stage)) {
                logger.info("进入production阶段");
                param.put(Dict.REAL_START_DATE, getEstInitRealDate(taskList));
                param.put(Dict.REAL_PRODUCT_DATE, getEstRelComplRealDate(taskList));
                return Tuple.tuple(Dict.TASK_STAGE_PRODUCTION, param);
            }
            return null;
        }
        // 3 根据状态包装参数
        if (Dict.TASK_STAGE_DEVELOP.equals(maxStage) || Dict.TASK_STAGE_DEVELOP.equals(maxStage2)) {
            param.put(Dict.REAL_START_DATE, getEstInitRealDate(taskList));
            return Tuple.tuple(Dict.TASK_STAGE_DEVELOP, param);
        }
        if (Dict.TASK_STAGE_SIT.equals(maxStage2)) {
            logger.info("进入sit阶段");
            param.put(Dict.REAL_START_DATE, getEstInitRealDate(taskList));
            //实际提交内测信息sit
            param.put(Dict.REAL_INNER_TEST_DATE, getSitRealDate(taskList));
            //2021-08实施单元字段拆分需求，进入UAT和REL通过编辑任务改变，不再通过uatTestTime字段判断内业测并行
//            //判断sit中的任务是否都有uatTestTime
//            if (collect.get("sit").stream().allMatch(task -> CommonUtils.isNotNullOrEmpty(task.getUat_test_time()))) {
//                // 都有
//                logger.info("进入sit全部");
//                param.put("real_test_date", getUatTestTime(taskList));
//                return Tuple.tuple("uat", param);
//            }
            // 部分有或者没有
            return Tuple.tuple(Dict.TASK_STAGE_SIT, param);
        }
        if (Dict.TASK_STAGE_UAT.equals(maxStage2)
                || ((Dict.TASK_STAGE_REL.equals(maxStage2) || Dict.TASK_STAGE_PRODUCTION.equals(maxStage2) || Dict.TASK_STAGE_FILE.equals(maxStage2))
                && (Dict.TASK_STAGE_DEVELOP.equals(stage) || Dict.TASK_STAGE_SIT.equals(stage) || Dict.TASK_STAGE_UAT.equals(stage) || todoTasks.size() > 0))) {
            logger.info("进入uat阶段");
            param.put(Dict.REAL_START_DATE, getEstInitRealDate(taskList));
            //实际提交内测信息sit
            param.put(Dict.REAL_INNER_TEST_DATE, getSitRealDate(taskList));
            param.put(Dict.REAL_TEST_DATE, getEstUatRealDate(taskList));
            return Tuple.tuple(Dict.TASK_STAGE_UAT, param);
        }
        if (Dict.TASK_STAGE_REL.equals(stage)) {
            logger.info("进入rel阶段");
            param.put(Dict.REAL_START_DATE, getEstInitRealDate(taskList));
            //实际提交内测信息sit
            param.put(Dict.REAL_INNER_TEST_DATE, getSitRealDate(taskList));
            param.put(Dict.REAL_TEST_DATE, getEstUatRealDate(taskList));
            //实际测试完成时间rel
            param.put(Dict.REAL_TEST_FINISH_DATE, getRelRealDate(taskList));
            return Tuple.tuple(Dict.TASK_STAGE_REL, param);
        }
        if (Dict.TASK_STAGE_PRODUCTION.equals(stage) || Dict.TASK_STAGE_FILE.equals(stage)) {
            logger.info("进入production阶段");
            param.put(Dict.REAL_START_DATE, getEstInitRealDate(taskList));
            //实际提交内测信息sit
            param.put(Dict.REAL_INNER_TEST_DATE, getSitRealDate(taskList));
            param.put(Dict.REAL_TEST_DATE, getEstUatRealDate(taskList));
            //实际测试完成时间rel
            param.put(Dict.REAL_TEST_FINISH_DATE, getRelRealDate(taskList));
            param.put(Dict.REAL_PRODUCT_DATE, getEstRelComplRealDate(taskList));
            return Tuple.tuple(Dict.TASK_STAGE_PRODUCTION, param);
        }
        if (Dict.TASK_STAGE_CREATE_INFO.equals(maxStage)) {
            logger.info("进入create-info阶段");
            param.put(Dict.REAL_START_DATE, "");
            param.put(Dict.REAL_INNER_TEST_DATE, "");
            param.put(Dict.REAL_TEST_DATE, "");
            param.put(Dict.REAL_TEST_FINISH_DATE, "");
            param.put(Dict.REAL_PRODUCT_DATE, "");
            return Tuple.tuple(Dict.TASK_STAGE_CREATE_INFO, param);
        }
        return null;
    }

    //获取最早开发时间
    public String getEstInitRealDate(List<FdevTask> collect) {
        List<String> list = collect.stream()
                .filter(task -> !CommonUtils.isNullOrEmpty(task.getStart_time()))
                .flatMap(task -> Arrays.asList(task.getStart_time()).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return CommonUtils.dateParse2(list.get(0));
    }

    //获取非日常任务最早uat提测时间
    public String getEstUatRealDate(List<FdevTask> collect) {
        List<String> list = collect.stream()
                .filter(task -> !CommonUtils.isNullOrEmpty(task.getStart_uat_test_time()) && !"2".equals(String.valueOf(task.getTaskType())))
                .flatMap(task -> Arrays.asList(task.getStart_uat_test_time()).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return CommonUtils.dateParse2(list.get(0));
    }

    //获取非日常任务最早提交sit内测时间
    public String getSitRealDate(List<FdevTask> collect) {
        List<String> list = collect.stream()
                .filter(task -> !CommonUtils.isNullOrEmpty(task.getStart_inner_test_time()) && !"2".equals(String.valueOf(task.getTaskType())))
                .flatMap(task -> Arrays.asList(task.getStart_inner_test_time()).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return CommonUtils.dateParse2(list.get(0));
    }

    //获取最晚进入rel测试时间
    public String getRelRealDate(List<FdevTask> collect) {
        List<String> list = collect.stream()
                .filter(task -> !CommonUtils.isNullOrEmpty(task.getStart_rel_test_time()) && !"2".equals(String.valueOf(task.getTaskType())))
                .flatMap(task -> Arrays.asList(task.getStart_rel_test_time()).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return CommonUtils.dateParse2(list.get(list.size() - 1));
    }

    //获取最晚投产时间
    public String getEstRelComplRealDate(List<FdevTask> collect) {
        List<String> list = collect.stream()
                .flatMap(task -> Arrays.asList(task.getFire_time()).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return CommonUtils.dateParse2(list.get(list.size() - 1));
    }

    // 获取uat业务测试时间
    public String getUatTestTime(List<FdevTask> collect) {
        // 特殊情况 业务测试 当所有sit均有业务测试时间
        logger.info("进入getUatTestTime");
        List<String> uatList = collect.stream()
                .flatMap(task -> Arrays.asList(task.getUat_test_time()).stream())
                .filter(str -> CommonUtils.isNotNullOrEmpty(str) && "null" != str)
                .collect(Collectors.toList());
        logger.info("进入getUatTestTime");
        List<String> starList = collect.stream()
                .flatMap(task -> Arrays.asList(task.getStart_uat_test_time()).stream())
                .filter(str -> CommonUtils.isNotNullOrEmpty(str) && "null" != str)
                .collect(Collectors.toList());
        uatList.addAll(starList);
        Collections.sort(uatList);
        return CommonUtils.dateParse2(uatList.get(uatList.size() - 1));
    }


    /**
     * 当任务中存在有待实施的时候，将推研发单元状态改为develop
     *
     * @param param
     * @return
     */
    public Tuple.Tuple2<String, HashMap<String, String>> changeToDevelop(Tuple.Tuple2<String, HashMap<String, String>> param, List<FdevTask> todoList) {
        if (CommonUtils.isNullOrEmpty(param))
            throw new FdevException(ErrorConstants.DATA_NOT_EXIST, new String[]{"获取需求状态失败"});
        HashMap<String, String> timeData = param.second();
        //去掉其他阶段的时间数据
        timeData.remove(Dict.REAL_INNER_TEST_DATE);
        timeData.remove(Dict.REAL_TEST_DATE);
        timeData.remove(Dict.REAL_TEST_FINISH_DATE);
        timeData.remove(Dict.REAL_PRODUCT_DATE);
        String real_start_time = timeData.get(Dict.REAL_START_DATE);
        //取到待实施的任务列表中的最早的start时间
        Tuple.Tuple2<String, HashMap<String, String>> todoTuple = getTodoMinStageAndDate(todoList);
        HashMap<String, String> todoTimeData = todoTuple.second();
        String todo_real_start_time = todoTimeData.get(Dict.REAL_START_DATE);
        //对比那个时间更早
        String sendDate = minDate(todo_real_start_time, real_start_time);
        timeData.put(Dict.REAL_START_DATE, sendDate);
        return Tuple.tuple(Dict.TASK_STAGE_DEVELOP, timeData);
    }


    /**
     * 对比待实施的时间和开发的时间，取最早的时间
     *
     * @param todoDate
     * @param date
     * @return
     */
    private String minDate(String todoDate, String date){
        long todoTime = 0;
        long time = 0;
        try {
            todoTime = CommonUtils.dateParse(todoDate, CommonUtils.DATE_PATTERN).getTime();
            time = CommonUtils.dateParse(date, CommonUtils.DATE_PATTERN).getTime();
        } catch (Exception e) {
            logger.error("时间yyyy/MM/dd转换成date出错");
            return "";
        }
        return todoTime - time > 0 ? date : todoDate;
    }

    /**
     * 取出待实施的最小阶段和实际启动日期
     *
     * @param taskList
     * @return
     */
    private Tuple.Tuple2<String, HashMap<String, String>> getTodoMinStageAndDate(List<FdevTask> taskList) {
        HashMap<String, String> param = new HashMap<>();
        // 1.根据不同stage分组
        Map<String, List<FdevTask>> collect = taskList.stream()
                .collect(Collectors.groupingBy(task -> task.getStage()));

        //取出最小时间
        String stage = taskList.stream()
                .flatMap(task -> Arrays.asList(task.getStage()).stream())
                .sorted(Comparator.comparingInt(CommonUtils::pareStage))
                .limit(1)
                .collect(Collectors.joining());
        logger.info("进入create-info阶段");
        //取出待实施的实际启动日期
        param.put(Dict.REAL_START_DATE, getEstInitRealDate(taskList));
        return Tuple.tuple(Dict.TASK_STAGE_CREATE_INFO, param);
    }
}
