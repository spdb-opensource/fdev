package com.spdb.fdev.fdemand.base.dict;

public class DemandEnum {

    /**
     * 需求状态类型
     */
    public enum DemandStatusEnum {
        PRE_EVALUATE("预评估", 0),
        EVALUATE("评估中", 1),
        PRE_IMPLEMENT("待实施",2),
        DEVELOP( "开发中", 3),
        SIT("sit", 4),
        UAT("uat" , 5),
        REL("rel" , 6),
        PRODUCT("已投产", 7),
        PLACE_ON_FILE("已归档", 8),
        IS_CANCELED("已撤销", 9);

        private String name;
        private Integer value;

        DemandStatusEnum(String name, Integer value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Integer getValue() {
            return value;
        }

    }

    /**
     * 需求暂缓相关枚举类型
     */
    public enum DemandDeferStatus {
        ROLLBACK("异常回滚",-1),DEFER("暂缓中", 1), RECOVER("恢复中", 2), FINISTH("恢复完成", 3);
        private String name;
        private Integer value;

        DemandDeferStatus(String name, Integer value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Integer getValue() {
            return value;
        }
    }
    /**
     * 涉及板块相关状态          "assess_status": "评估状态（预评估、评估中、评估完成）
     */
    public enum DemandAssess_status {
    	PRE_EVALUATE("预评估","0"),EVALUATE("评估中", "1"), EVALUATE_OVER("评估完成","2");
        private String name;
        private String value;

        DemandAssess_status(String name, String value) {
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
    /**
     * 紧急需求标识         "demand_flag": 需求标识（紧急、正常同步）
     */
    public enum DemandDemand_flag {
        URGENT("紧急",0),NORMAL_SYNCHRONIZATION("正常同步", 1);
        private String name;
        private Integer value;

        DemandDemand_flag(String name, Integer value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Integer getValue() {
            return value;
        }
    }
}



