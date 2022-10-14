package com.gotest.domain;

/**
 * 发送给fdev的 状态数据bean类
 */
public class PlanListTestCase {


    //任务案例名称
    private String testplanName;
    /**
     *  案例总和，
     *    首先，根据传递过来的workOrderNo工单编号主键，从plan_list表中查询plan_id（1对多）
     *      根据plan_id 从planlist_testcase_rekatuib 中间表中获取testcase表中的testcase_id数据
     *        然后根据testcase_id主键查询表testcase表中 案例数据数据详情，得出：
     *          根据testcase_status的具体值的判断，
     *          统计
     *          allCount 案例总数(无需判断，统计数量和即可)、
     *          allPassed，通过数
     *          allFailed，失败数
     *          allBlocked, 堵塞数
     */
    //案例总数
    private Integer allCount;
    //通过数 1
    private Integer allPassed;
    //失败数  3
    private Integer allFailed;
    //堵塞数  2
    private Integer allBlocked;


    @Override
    public String toString() {
        return "PlanListTestCase{" +
                "testplanName='" + testplanName + '\'' +
                ", allCount=" + allCount +
                ", allPassed=" + allPassed +
                ", allFailed=" + allFailed +
                ", allBlocked=" + allBlocked +
                '}';
    }

    public String getTestplanName() {
        return testplanName;
    }

    public void setTestplanName(String testplanName) {
        this.testplanName = testplanName;
    }

    public Integer getAllCount() {
        return allCount;
    }

    public void setAllCount(Integer allCount) {
        this.allCount = allCount;
    }

    public Integer getAllPassed() {
        return allPassed;
    }

    public void setAllPassed(Integer allPassed) {
        this.allPassed = allPassed;
    }

    public Integer getAllFailed() {
        return allFailed;
    }

    public void setAllFailed(Integer allFailed) {
        this.allFailed = allFailed;
    }

    public Integer getAllBlocked() {
        return allBlocked;
    }

    public void setAllBlocked(Integer allBlocked) {
        this.allBlocked = allBlocked;
    }

}
