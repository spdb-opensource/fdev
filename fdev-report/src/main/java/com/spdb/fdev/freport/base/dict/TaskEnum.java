package com.spdb.fdev.freport.base.dict;

public class TaskEnum {

    /**
     * 任务状态
     */
    public enum TaskStage{
        ABORT("abort", "已删除"),
        DISCARD("discard", "废弃"),
        CREATE_INFO("create-info", "创建信息"),
        CREATE_APP("create-app", "创建应用"),
        CREATE_FEATURE("create-feature", "创建分支"),
        DEVELOP("develop", "开发中"),
        SIT("sit", "sit"),
        UAT("uat", "uat"),
        REL("rel", "rel"),
        PRODUCTION("production", "已投产"),
        FILE("file", "已归档");

        private String name;
        private String value;

        TaskStage(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public static TaskStage getByName(String name){
            for (TaskStage value : TaskStage.values()) {
                if (value.getName().equals(name)) {
                    return value;
                }
            }
            return null;
        }
    }

    /**
     * 需求状态类型
     */
    public enum TaskDiffEnum {
        A1("A1", "页面纯样式调整"),
        A2("A2", "页面路由和接口配套改造等"),
        A3("A3", "页面样式联动和控制等"),
        A4("A4", "新增功能，如：UI组件、页面等"),
        A5("A5", "涉及前端页面大改版"),
        A6("A6", "前端框架迁移或替换"),
        B1("B1", "现有交易涉及一般逻辑改造"),
        B2("B2", "现有交易涉及接口改造、组件升级"),
        B3("B3", "涉及数据库维护更新"),
        B4("B4", "新增交易"),
        B5("B5", "涉及逻辑改造和数据库维护更新"),
        B6("B6", "涉及服务大改造、数据库大改造(特殊)"),
        C1("C1", "管理端交易"),
        C2("C2", "仅对批量/host的小部分修改"),
        C3("C3", "管理端交易且涉及大量数据维护"),
        C4("C4", "批量/host新增交易或搬迁多个交易"),
        C5("C5", "涉及数据仓库开发并导入数据"),
        C6("C6", "涉及迁移或替换(特殊)"),
        D1("D1", "难度系数1(最简单)"),
        D2("D2", "难度系数2"),
        D3("D3", "难度系数3"),
        D4("D4", "难度系数4"),
        D5("D5", "难度系数5"),
        D6("D6", "难度系数6(最困难)");

        private String name;
        private String value;

        TaskDiffEnum(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

    }

    public enum DevelopWayEnum {
        A("A", "前端"),
        B("B", "服务端"),
        C("C", "辅助应用"),
        D("D", "管理端交易");

        private String name;
        private String value;

        DevelopWayEnum(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

    }
}

