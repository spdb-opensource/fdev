package com.plan.util;

public class DicConstants {


    //交易失败返回码
    public static final String I_FAILED = "00000";
    /***************************** 案例状态 **********************************/
    //案例新建
    public static final String TESTCASE_NEW = "0";
    //待审核
    public static final String TESTCASE_AUDITING = "10";
    //审核通过
    public static final String TESTCASE_AUDITING_THROUGH = "11";
    //审核拒绝
    public static final String TESTCASE_AUDITING_REJECT = "12";
    //待生效
    public static final String TESTCASE_EFFECT = "20";
    //生效通过
    public static final String TESTCASE_EFFECT_THROUGH = "21";
    //生效拒绝
    public static final String TESTCASE_EFFECT_REJECT = "22";
    //通用(针对已生效案例)
    public static final String TESTCASE_EFFECT_GENERAL = "30";
    //废弃(针对已生效案例)
    public static final String TESTCASE_EFFECT_DISCARD = "40";
    // 反案例
    public static final String IS_CaseS="反案例";
    //正案例
    public static final String THE_CaseS="正案例";


}
