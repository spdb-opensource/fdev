package com.spdb.fdev.freport.spdb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.spdb.fdev.common.dict.Dict;
import com.spdb.fdev.freport.base.dict.DashboardDict;
import com.spdb.fdev.freport.base.utils.CommonUtils;
import com.spdb.fdev.freport.base.utils.DashboardUtils;
import com.spdb.fdev.freport.base.utils.TimeUtils;
import com.spdb.fdev.freport.spdb.dao.ReportDao;
import com.spdb.fdev.freport.spdb.dto.ProIssuesDto;
import com.spdb.fdev.freport.spdb.service.QualityManageService;
import com.spdb.fdev.freport.spdb.vo.Product;
import com.spdb.fdev.freport.spdb.vo.ProductVo;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class QualityManageServiceImpl implements QualityManageService {

    @Autowired
    private ReportDao reportDao;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestTransport restTransport;

    @Override
    public ProductVo queryProIssueTrend(Integer cycle) throws Exception {
        List<Product> result = new ArrayList<>();//定义结果集
        Map<String, Date[]> periodMap = DashboardUtils.handleCycle(cycle);//获取仪表盘基本数据结构
        String[] startAndEnd = DashboardUtils.getStartAndEndByPeriodMap(periodMap, TimeUtils.FORMAT_DATESTAMP);//获取整体周期时间条件
        //数据源准备完毕
        List<ProIssuesDto> proIssuesList = JSONObject.parseArray(JSONObject.toJSONString(restTransport.submit(
                new HashMap<String, Object>() {{
                    put(Dict.REST_CODE, "queryProIssues");
                    put("current_page", 1);
                    put("page_size", 0);//默认查全部
                    put("responsible_type", "0");//传0保险走全部的条件逻辑
                    put("module", new ArrayList<String>() {{
                        add(CommonUtils.getSessionUser().getGroup_id());
                    }});
                    put("isIncludeChildren", false);//只查当前组
                    put("start_time", startAndEnd[0]);
                    put("end_time", startAndEnd[1]);
                }}
        )), ProIssuesDto.class);
        //组装填入最终数据
        Map<String, Integer> countMap = new LinkedHashMap<>();//构建map便于计算
        for (String period : periodMap.keySet()) {
            countMap.put(period, 0);
            Date[] dateArray = periodMap.get(period);//按周期分类拼接数据结构 - 准备判断起止时间
            for (ProIssuesDto item : proIssuesList) {
                if (!CommonUtils.isNullOrEmpty(item.getOccurred_time())) {
                    Date condition = TimeUtils.FORMAT_DATESTAMP_2.parse(item.getOccurred_time());
                    if (0 <= condition.compareTo(dateArray[0]) && 0 >= condition.compareTo(dateArray[1])) {
                        countMap.put(period, countMap.get(period) + 1);
                    }
                }
            }
        }
        for (String period : countMap.keySet()) {
            result.add(new Product(period, countMap.get(period)));
        }
        return new ProductVo(reportDao.findDashboardTips(DashboardDict.PRO_ISSUE_TREND).getContent(), result);
    }

}
