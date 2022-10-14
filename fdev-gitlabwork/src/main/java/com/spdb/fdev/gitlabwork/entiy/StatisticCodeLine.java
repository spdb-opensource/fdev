package com.spdb.fdev.gitlabwork.entiy;

/**
 * @Author: c-jiangjk
 * @Date: 2021/3/1 14:20
 */
public class StatisticCodeLine {

    // 当前时间月份
    private String month;
    // 当月总行数
    private Integer codeLineTotal;
    // 当月在职总人数
    private Integer personTotal;
    // 当月人均代码行数
    private double average;

    public Integer getCodeLineTotal() {
        return codeLineTotal;
    }

    public void setCodeLineTotal(Integer codeLineTotal) {
        this.codeLineTotal = codeLineTotal;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Integer getPersonTotal() {
        return personTotal;
    }

    public void setPersonTotal(Integer personTotal) {
        this.personTotal = personTotal;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }
}
