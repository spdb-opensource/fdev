package com.spdb.fdev.freport.base.dict;

import com.spdb.fdev.freport.base.utils.TimeUtils;

import java.util.*;

public class DashboardDict {

    /**
     * 研发协作
     */
    public static final String DEMAND_NEW_TREND = "demandNewTrend";

    public static final String DEMAND_PRO_TREND = "demandProTrend";

    public static final String DEMAND_THROUGHPUT_TREND = "demandThroughputTrend";

    public static final String TASK_TREND = "taskTrend";

    public static final String APP_NEW_TREND = "appNewTrend";

    /**
     * 质量管理
     */
    public static final String PRO_ISSUE_TREND = "proIssueTrend";

    //投产管理
    public static final String PUBLISH_COUNT_TREND = "publishCountTrend";

    //排行榜
    public static final String USER_COMMIT = "userCommit";

    public static final String SONAR_PROJECT_RANK = "sonar_project_rank";//这里code是下划线格式 这里就算了 同sonar api格式

    //bugs
    public static final String BUGS = "bugs";

    //codeSmells
    public static final String CODE_SMELLS = "code_smells";//这里接口数据为下划线则以下划线格式为主

    //duplicatedLinesDensity
    public static final String DUPLICATED_LINES_DENSITY = "duplicated_lines_density";//这里接口数据为下划线则以下划线格式为主

    //vulnerabilities
    public static final String VULNERABILITIES = "vulnerabilities";

    public static final List<Map<String, Object>> defaultConfig = new ArrayList<Map<String, Object>>() {{
        add(new HashMap<String, Object>() {{
            put("w", 4);
            put("h", 3);
            put("x", 0);
            put("y", 0);
            put("i", 1632823867463L);
            put("params", new HashMap<String, Object>() {{
                put("graphType", new HashMap<String, Object>() {{
                    put("value", "LineG");
                    put("label", "折线图");
                }});
                put("demandType", new HashMap<String, Object>() {{
                    put("value", "");
                    put("label", "总和");
                }});
                put("period", new HashMap<String, Object>() {{
                    put("value", "1");
                    put("label", "每月");
                }});
            }});
            put("nameEn", "addDemandNum");
        }});
        add(new HashMap<String, Object>() {{
            put("w", 4);
            put("h", 3);
            put("x", 4);
            put("y", 0);
            put("i", 1632823869096L);
            put("params", new HashMap<String, Object>() {{
                put("graphType", new HashMap<String, Object>() {{
                    put("value", "LineG");
                    put("label", "折线图");
                }});
                put("period", new HashMap<String, Object>() {{
                    put("value", "1");
                    put("label", "每月");
                }});
            }});
            put("nameEn", "publishNum");
        }});
        add(new HashMap<String, Object>() {{
            put("w", 4);
            put("h", 3);
            put("x", 0);
            put("y", 3);
            put("i", 1632823872019L);
            put("params", new HashMap<String, Object>() {{
                put("graphType", new HashMap<String, Object>() {{
                    put("value", "Table");
                    put("label", "表格");
                }});
                put("rankingType", new HashMap<String, Object>() {{
                    put("value", "topTen");
                    put("label", "前十名");
                }});
                Calendar c = Calendar.getInstance();
                put("endDate", TimeUtils.FORMAT_DATESTAMP.format(c.getTime()));
                c.add(Calendar.DATE, -7);
                put("startDate", TimeUtils.FORMAT_DATESTAMP.format(c.getTime()));
            }});
            put("nameEn", "CodeSubmitionRank");
        }});
        add(new HashMap<String, Object>() {{
            put("w", 4);
            put("h", 3);
            put("x", 8);
            put("y", 0);
            put("i", 1632823870467L);
            put("params", new HashMap<String, Object>() {{
                put("graphType", new HashMap<String, Object>() {{
                    put("value", "LineG");
                    put("label", "折线图");
                }});
                put("period", new HashMap<String, Object>() {{
                    put("value", "1");
                    put("label", "每月");
                }});
            }});
            put("nameEn", "publishProblemNum");
        }});
    }};

}