package com.spdb.fdev.freport.spdb.service.impl;

import com.spdb.fdev.freport.base.dict.DashboardDict;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.base.utils.DashboardUtils;
import com.spdb.fdev.freport.base.utils.TimeUtils;
import com.spdb.fdev.freport.spdb.dao.ReleaseDao;
import com.spdb.fdev.freport.spdb.dao.ReportDao;
import com.spdb.fdev.freport.spdb.dao.TaskDao;
import com.spdb.fdev.freport.spdb.entity.release.ReleaseNode;
import com.spdb.fdev.freport.spdb.entity.release.ReleaseTask;
import com.spdb.fdev.freport.spdb.entity.task.Task;
import com.spdb.fdev.freport.spdb.service.PublishManageService;
import com.spdb.fdev.freport.spdb.vo.Product;
import com.spdb.fdev.freport.spdb.vo.ProductVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PublishManageServiceImpl implements PublishManageService {

    @Autowired
    private TaskDao taskDao;

    @Autowired
    private ReleaseDao releaseDao;

    @Autowired
    private ReportDao reportDao;

    @Override
    public ProductVo queryPublishCountTrend(Integer cycle) throws Exception {
        List<Product> result = new ArrayList<>();//定义结果集
        Map<String, Date[]> periodMap = DashboardUtils.handleCycle(cycle);//获取仪表盘基本数据结构
        String[] startAndEnd = DashboardUtils.getStartAndEndByPeriodMap(periodMap);//获取整体周期时间条件
        List<Task> taskList = taskDao.find(new Task() {{
            setGroup(CommonUtils.getSessionUser().getGroup_id());
        }});
        //数据源准备
        List<ReleaseNode> releaseList = new ArrayList<>();
        if (!CommonUtils.isNullOrEmpty(taskList)) {
            List<ReleaseTask> releaseTaskList = releaseDao.findReleaseTaskByTaskId(taskList.stream().map(Task::getId).collect(Collectors.toSet()));
            Set<String> releaseNodeNameSet = releaseTaskList.stream().map(ReleaseTask::getReleaseNodeName).collect(Collectors.toSet());
            if (!CommonUtils.isNullOrEmpty(releaseNodeNameSet)) {
                releaseList.addAll(releaseDao.find(new ReleaseNode() {{//数据源准备完毕
                    setNodeStatus("1");
                }}, releaseNodeNameSet, startAndEnd[0], startAndEnd[1]));
            }
        }
        //组装填入最终数据
        Map<String, Integer> countMap = new LinkedHashMap<>();//构建map便于计算
        for (String period : periodMap.keySet()) {
            countMap.put(period, 0);
            Date[] dateArray = periodMap.get(period);//按周期分类拼接数据结构 - 准备判断起止时间
            for (ReleaseNode item : releaseList) {
                if (!CommonUtils.isNullOrEmpty(item.getReleaseDate())) {
                    Date condition = TimeUtils.FORMAT_DATESTAMP.parse(item.getReleaseDate());
                    //真正意义上实际投产日期需加1天
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(condition);
                    calendar.add(Calendar.DATE, 1);
                    condition = calendar.getTime();
                    if (0 <= condition.compareTo(dateArray[0]) && 0 >= condition.compareTo(dateArray[1])) {
                        countMap.put(period, countMap.get(period) + 1);
                    }
                }
            }
        }
        for (String period : countMap.keySet()) {
            result.add(new Product(period, countMap.get(period)));
        }
        return new ProductVo(reportDao.findDashboardTips(DashboardDict.PUBLISH_COUNT_TREND).getContent(), result);
    }

}
