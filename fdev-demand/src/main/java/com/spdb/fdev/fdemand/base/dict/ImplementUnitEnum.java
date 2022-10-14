package com.spdb.fdev.fdemand.base.dict;

public class ImplementUnitEnum {

    /**
     * 研发单元状态类型
     */
    public enum ImplementUnitStatusEnum {
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

        ImplementUnitStatusEnum(String name, Integer value) {
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
     * 研发单元暂缓相关枚举类型
     */
    public enum ImplementUnitDeferStatus {
        ROLLBACK("异常回滚",-1),
        DEFER("暂缓中", 1),
        RECOVER("恢复中", 2),
        FINISTH("恢复完成", 3);
        private String name;
        private Integer value;

        ImplementUnitDeferStatus(String name, Integer value) {
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
