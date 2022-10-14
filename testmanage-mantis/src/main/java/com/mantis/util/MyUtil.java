package com.mantis.util;

import com.mantis.dict.Dict;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

@Component
@RefreshScope
public class MyUtil {

    private static final Logger logger = LoggerFactory.getLogger(MyUtil.class);

    @Value("${spring.profiles.active}")
    private String active;

    @Value("${ftms.ip}")
    private String ftmsIp;


    @Autowired
    WebApplicationContext applicationContext;


    /**
     * 根据环境不同，组装不同的访问地址
     */
    public String getActiveIp(String ip, String address){
        StringBuffer url = new StringBuffer();
        url.append(ip).append(":");
        if (active.equals("sit")){
            url.append("9093");
        }else if (active.equals("uat")){
            url.append("9092");
        }else if (active.equals("rel")){
            url.append("9091");
        }else if (active.equals("pro")){
            url.append("8080");
        }else if (active.equals("sit-new")){
            url.append("9093");
        }else if (active.equals("rel-new")){
            url.append("9091");
        }else if (active.equals("pro-new")){
            url.append("8080");
        }
        url.append(address);
        return url.toString();
    }

    /**
     * 根据环境
     *  获取 ftms 接口地址
     */
    public String getFtmsActiveIp(String address){
        return getActiveIp(ftmsIp, address);
    }

    //发送 mantis平台接口 数据组装
    public Map<String,Object> assemblyParamMap(String type , Object value) {
        Map<String,Object> projectMap = new HashMap<String,Object>();
        projectMap.put(type, value);
        return projectMap;
    }

    //发送 mantis平台接口 数据组装
    public Map<String,Object> assemblyCustomMap(Integer id,Object value) {
        Map<String,Object> customMap = new HashMap<String,Object>();
        Map<String,Integer> custom_item = new HashMap<String,Integer>();
        custom_item.put(Dict.ID, id);
        customMap.put(Dict.FIELD, custom_item);
        customMap.put(Dict.VALUE, value);
        return customMap;
    }


    /**
     * 将状态值 status 转为中文
     */
    public static String getChStatus(String priority) {
        if (priority == null)
            return "";
        switch (priority) {
            case "10":
                return "新建";
            case "20":
                return "拒绝";
            case "30":
                return "确认拒绝";
            case "40":
                return "延迟修复";
            case "50":
                return "打开";
            case "80":
                return "已修复";
            case "90":
                return "关闭";
        }
        return priority;

    }

    public String getProIssueProjectId(){
        String projectId = null ;
        switch (active){
            case "sit":projectId = "21";
                break;
            case "rel":projectId = "24";
                break;
            case "pro":projectId = "20";
                break;
            case "sit-new":projectId = "29";
                break;
            case "rel-new":projectId = "31";
                break;
            case "pro-new":projectId = "32";
                break;
        }
        return projectId;
    }

    public static String transformString(Object str){
        if(Util.isNullOrEmpty(str)){
            return  "";
        };
        return String.valueOf(str);
    }

    public static String getPercentage(int child, int parent){
        if(Double.compare(parent,0) == 0){
            return "0";
        }
        Double rate = Double.valueOf(child) / Double.valueOf(parent);
        NumberFormat format = NumberFormat.getPercentInstance();
        format.setMaximumFractionDigits(2);
        return format.format(rate);
    }

    public  String isNewFtms() {
        if (applicationContext.getServletContext().getContextPath().contains("-new")) {
            return "1";
        }
        return "0";
    }

    /**
     * 获取严重性文本
     * @param severity
     * @return
     */
    public static String getSeverityText(String severity) {
        switch (severity) {
            case "10" :
                return "新功能";
            case "20" :
                return "细节";
            case "30" :
                return "文字";
            case "40" :
                return "小调整";
            case "50" :
                return "小错误";
            case "60" :
                return "很严重";
            case "70" :
                return "崩溃";
            case "80" :
                return "宕机";
            default:
                return "其它";
        }
    }
}
