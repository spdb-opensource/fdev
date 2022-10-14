package com.gotest.dict;

public class WorkOrderStage {
    /**
     * 开发中=1
     * sit=2
     * uat=3
     * 已投产=4
     * uat(含风险)=6
     * 撤销=8
     * #零售组专属阶段
     * 分包测试（内测完成）=9
     * 分包测试（含风险）=10
     * 安全测试（内测完成）=12
     * 安全测试（含风险）=13
     * 安全测试（不涉及）=14
     */
    public static final String stage1= "开发中";
    public static final String stage2= "SIT";
    public static final String stage3= "UAT";
    public static final String stage4= "已投产";
    public static final String stage6= "UAT(含风险)";
    public static final String stage8= "废弃";
    public static final String stage9= "分包测试（内测完成）";
    public static final String stage10= "分包测试（含风险）";
    public static final String stage12= "安全测试（内测完成）";
    public static final String stage13= "安全测试（含风险）";
    public static final String stage14= "安全测试（不涉及）";

    public static String getStage(String i){
        switch (i){
            case "1":
               return stage1;
            case "2":
                return stage2;
            case "3":
                return stage3;
            case "4":
                return stage4;
            case "6":
                return stage6;
            case "8":
                return stage8;
            case "9":
                return stage9;
            case "10":
                return stage10;
            case "12":
                return stage12;
            case "13":
                return stage13;
            case "14":
                return stage14;
                default:
                    return "待分配";
        }
    }


}
