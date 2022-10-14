package com.spdb.fdev.fdemand.spdb.unit;

import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.spdb.entity.FdevImplementUnit;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理任务相关组件
 */
@Component
public class DealTaskUnit {

    //获取最早开发时间
    public String getRealStartDate(List<FdevImplementUnit> collect) {

        List<String> list = collect.stream()
                .filter(unit -> !CommonUtils.isNullOrEmpty(unit.getReal_start_date()))
                .flatMap(unit -> Arrays.asList(unit.getReal_start_date()).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return list.get(0);
    }

    //获取最早内测时间
    public String getRealInnerTestDate(List<FdevImplementUnit> collect) {
        List<String> list = collect.stream()
                .filter(unit -> !CommonUtils.isNullOrEmpty(unit.getReal_inner_test_date()))
                .flatMap(unit -> Arrays.asList(unit.getReal_inner_test_date()).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return list.get(0);
    }

    //获取最早业测时间
    public String getRealTestDate(List<FdevImplementUnit> collect) {
        List<String> list = collect.stream()
                .filter(unit -> !CommonUtils.isNullOrEmpty(unit.getReal_test_date()))
                .flatMap(unit -> Arrays.asList(unit.getReal_test_date()).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return list.get(0);
    }

    // 获取测试完成时间
    public String getRealTestFinishDate(List<FdevImplementUnit> collect) {
        List<String> list = collect.stream()
                .filter(unit -> !CommonUtils.isNullOrEmpty(unit.getReal_test_finish_date()))
                .flatMap(unit -> Arrays.asList(unit.getReal_test_finish_date()).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return list.get(list.size() - 1);
    }

    // 获取投产时间
    public String getRealProductDate(List<FdevImplementUnit> collect) {
        List<String> list = collect.stream()
                .filter(unit -> !CommonUtils.isNullOrEmpty(unit.getReal_product_date()))
                .flatMap(unit -> Arrays.asList(unit.getReal_product_date()).stream())
                .collect(Collectors.toList());
        Collections.sort(list);
        return list.get(list.size() - 1);
    }
}
