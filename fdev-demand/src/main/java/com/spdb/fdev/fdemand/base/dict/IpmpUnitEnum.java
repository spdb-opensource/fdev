package com.spdb.fdev.fdemand.base.dict;

public class IpmpUnitEnum {

    /**
     * 实施单元状态类型
     */
    public enum IpmpUnitStatusEnum {
        TEMPORARY_STORAGE("暂存", "01"),
        EVALUATE("评估中","02"),
        PRE_IMPLEMENT( "待实施", "03"),
        DEVELOP("开发中", "05"),
        UAT("业务测试中" , "04"),
        REL("业务测试完成" , "09"),
        PRODUCT("已投产", "06"),
        DEFER("暂缓", "07"),
        IS_CANCELED("已撤销", "08");


        private String name;
        private String value;

        IpmpUnitStatusEnum(String name, String value) {
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
     * 研发单元对应实施单元状态
     TEMPORARY_STORAGE("暂存", "01"),
     EVALUATE("评估中","02"),
     PRE_IMPLEMENT( "待实施", "03"),
     DEVELOP("开发中", "05"),
     UAT("业务测试中" , "04"),
     REL("业务测试完成" , "09"),
     PRODUCT("已投产", "06"),
     DEFER("暂缓", "07"),
     IS_CANCELED("已撤销", "08");
     */
    public enum IpmpUnitConFdevUnitStatus {
        EVALUATE("评估中", 1),
        PRE_IMPLEMENT("待实施",2),
        DEVELOP( "开发中", 3),
        SIT("开发中", 4),
        UAT("业务测试中" , 5),
        REL("业务测试完成" , 6),
        PRODUCT("已投产", 7),
        IS_CANCELED("已撤销", 9);

        private String name;
        private Integer value;

        IpmpUnitConFdevUnitStatus(String name, Integer value) {
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
