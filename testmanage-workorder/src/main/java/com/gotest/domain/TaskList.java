package com.gotest.domain;

/**
 * 任务列表实体类
 *
 */
public class TaskList {
    //任务编号
    private String taskno;
    //工单编号。。。。外键，工单表的主键
    private String workno;
    //任务名称
    private String taskname;
    //施工单位
    private String taskunit;
    //spdb管理人员
    private String spdbmanager;
    //施工人员
    private String developer;
    //状态值
    private Integer flag;
    //备用字段
    private String field1;
    private String field2;
    private String field3;
    private String field4;
    private String field5;

    @Override
    public String toString() {
        return "TaskList{" +
                "taskno='" + taskno + '\'' +
                ", workno='" + workno + '\'' +
                ", taskname='" + taskname + '\'' +
                ", taskunit='" + taskunit + '\'' +
                ", spdbmanager='" + spdbmanager + '\'' +
                ", developer='" + developer + '\'' +
                ", flag=" + flag +
                ", field1='" + field1 + '\'' +
                ", field2='" + field2 + '\'' +
                ", field3='" + field3 + '\'' +
                ", field4='" + field4 + '\'' +
                ", field5='" + field5 + '\'' +
                '}';
    }

    public String getTaskno() {
        return taskno;
    }

    public void setTaskno(String taskno) {
        this.taskno = taskno;
    }

    public String getWorkno() {
        return workno;
    }

    public void setWorkno(String workno) {
        this.workno = workno;
    }

    public String getTaskname() {
        return taskname;
    }

    public void setTaskname(String taskname) {
        this.taskname = taskname;
    }

    public String getTaskunit() {
        return taskunit;
    }

    public void setTaskunit(String taskunit) {
        this.taskunit = taskunit;
    }

    public String getSpdbmanager() {
        return spdbmanager;
    }

    public void setSpdbmanager(String spdbmanager) {
        this.spdbmanager = spdbmanager;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public String getField4() {
        return field4;
    }

    public void setField4(String field4) {
        this.field4 = field4;
    }

    public String getField5() {
        return field5;
    }

    public void setField5(String field5) {
        this.field5 = field5;
    }
}
