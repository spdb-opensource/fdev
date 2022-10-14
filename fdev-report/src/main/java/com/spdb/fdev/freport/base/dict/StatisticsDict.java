package com.spdb.fdev.freport.base.dict;

import java.util.HashMap;
import java.util.Map;

public class StatisticsDict {

    public static final Map<String, String> retailStatisticsGroupMap = new HashMap<String, String>() {{
        put("group2", "板块一");
        put("group3", "板块三");
        put("group6", "板块六");
        put("group7", "板块七");
        put("group8", "板块八");
    }};

    public static final String TOTAL = "合计";

    public static final String PRO_BUS_DEMAND = "投产业务需求";

    public static final String PRO_TECH_DEMAND = "投产科技需求";

    public static final String PRO_DAILY_DEMAND = "投产日常需求";

    public static final String TOTAL_PRO_DEMAND = "总投产需求";

    public static final String GROUP = "组/板块";
}
