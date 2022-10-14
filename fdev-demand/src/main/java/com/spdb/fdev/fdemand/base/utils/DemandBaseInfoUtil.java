package com.spdb.fdev.fdemand.base.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.crypto.Data;

import com.spdb.fdev.fdemand.base.dict.DemandDocEnum;
import com.spdb.fdev.fdemand.spdb.entity.DictEntity;
import org.apache.kafka.common.protocol.types.Field.Str;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators.In;

import com.spdb.fdev.fdemand.base.dict.DemandEnum;
import com.spdb.fdev.fdemand.base.dict.Dict;
import com.spdb.fdev.fdemand.base.dict.UIEnum;
import com.spdb.fdev.fdemand.base.utils.CommonUtils;
import com.spdb.fdev.fdemand.base.utils.MyUtils;
import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.RelatePartDetail;
import com.spdb.fdev.fdemand.spdb.entity.UserInfo;

public class DemandBaseInfoUtil {
    @Autowired
    private DemandEnum demandEnum;
    /**
     * 计算 状态的日期过滤
     *  计算规则，当状态为
     *      预评估，
     *      需求评估
     *      开发中
     *      uat
     *      已投产
     *  计算出 今天 减 天数的 date
     *  实际状态日期 + 天数  <  今天(最大时间)
     */
    public static List<HashMap> getStateDate(List<String> stateList, Integer num){
        List<HashMap> resList = new ArrayList<>();
        if (CommonUtils.isNullOrEmpty(stateList)||CommonUtils.isNullOrEmpty(num)) {
            return resList;
        }

        SimpleDateFormat sdfDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar =Calendar.getInstance();
        calendar.setTime(new Date());
        Date today = calendar.getTime();
        String lteDate = sdfDateFormat.format(today);
        calendar.add(Calendar.DATE, -num);
        Date date=calendar.getTime();
        String calDate =sdfDateFormat.format(date);
        //Date calDate =  MyUtils.getDateAdd(MyUtils.todayLastDate(), num * -24*60*60*1000L);
        HashMap resMap;
        Integer state = null;
        for (String State : stateList) {
            if ("defer".equals(State)) {
                continue;
            }
            else {
                state = Integer.valueOf(State);
            }
            resMap = new HashMap();
            resMap.put("state", state);
            resMap.put("stateDate", calDate);
            switch (state){
                case 2:
                    resMap.put("stateDateType", "plan_start_date");
                    break;
                case 4:
                    resMap.put("stateDateType", "plan_inner_test_date");
                    break;
                case 5:
                    resMap.put("stateDateType", "plan_test_finish_date");
                    break;
                case 6:
                    resMap.put("stateDateType", "plan_test_date");
                    break;
                case 7:
                    resMap.put("stateDateType", "plan_product_date");
                    break;
            }
            resList.add(resMap);
        }
        return resList;
    }

    /**
     * 过滤 预进行状态
     *  计算规则：
     *      计划启动：estInitDate
     * 	    计划提测：estSitDate
     * 	    计划投产：estRelComplDate
     *
     * 	     今天(最小时间) < 计划天数  < 今天(最大时间) + 数字天数
     */
    public static Map getFeatureDate(String featureType, Integer num) {
        Map resMap = new HashMap<>();
        if (CommonUtils.isNullOrEmpty(featureType)) {
            return resMap;
        }
        if (CommonUtils.isNullOrEmpty(num)) {
            num = 9999;
        }
        resMap.put(Dict.DATETYPE, featureType);
        SimpleDateFormat sdfDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar =Calendar.getInstance();
        calendar.setTime(new Date());
        Date today = calendar.getTime();
        String lteDate = sdfDateFormat.format(today);
        calendar.add(Calendar.DATE, +num);
        Date date=calendar.getTime();

        String gteDate =sdfDateFormat.format(date);
        // Date gltDate =  MyUtils.getDateAdd(MyUtils.todayLastDate(), num * -24*60*60*1000L);
        resMap.put(Dict.GTEDATE, gteDate);
        resMap.put(Dict.LTEDATE, lteDate);
        return resMap;
    }

    /**
     * 过滤 延期状态时间
     *  启动延期： estInitDate
     * 	提测延期： estSitDate
     * 	投产延期： estRelComplDate

     * 	今天（最大时间）  》  计划  + 天数
     * 	---》 今天（最大时间） - 天数 》 计划
     */
    public static Map getDelayDate(String datetype, Integer num){
        Map resMap = new HashMap<>();
        if (CommonUtils.isNullOrEmpty(datetype)) {
            return resMap;
        }
        if (CommonUtils.isNullOrEmpty(num)) {
            num = 0;
        }
        resMap.put(Dict.DATETYPE, datetype);
        Calendar calendar =Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -num);
        Date date=calendar.getTime();
        SimpleDateFormat sdfDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String gteDate =sdfDateFormat.format(date);
        // Date gltDate =  MyUtils.getDateAdd(MyUtils.todayLastDate(), num * -24*60*60*1000L);
        resMap.put(Dict.GTEDATE, gteDate);
        return resMap;
    }

    /**
     * 根据计划日期的key
     *      获取实际日期的key
     */
    public static String getRelDateKey(String planDateKey){
        if (CommonUtils.isNullOrEmpty(planDateKey)) {
            return null;
        }
        switch (planDateKey){
            case Dict.PLAN_START_DATE:
                return Dict.REAL_START_DATE;
            case Dict.PLAN_INNER_TEST_DATE:
                return Dict.REAL_INNER_TEST_DATE;
            case Dict.PLAN_TEST_DATE:
                return Dict.REAL_TEST_DATE;
            case Dict.PLAN_TEST_FINISH_DATE:
                return Dict.REAL_TEST_FINISH_DATE;
            case Dict.PLAN_PRODUCT_DATE:
                return Dict.REAL_PRODUCT_DATE;
        }
        return null;
    }
    /**
     * 组装实际时间的过滤
     */
    public static Map getRelDate(List<String> relDateType, String relStartDate, String relEndDate) {
        Map resMap = new HashMap<>();
        if (CommonUtils.isNullOrEmpty(relDateType) || CommonUtils.isNullOrEmpty(relStartDate))
            return resMap;
        resMap.put(Dict.RELDATETYPE, relDateType);
        resMap.put(Dict.RELSTARTDATE, relStartDate);
        resMap.put(Dict.RELENDDATE, relEndDate);
        return resMap;
    }

    public static String changeStateCn(Integer state) {
        String result = null;
        if (CommonUtils.isNullOrEmpty(state)) {
            return result;
        }
        switch (state) {
            case 0:
                return DemandEnum.DemandStatusEnum.PRE_EVALUATE.getName();
            case 1:
                return DemandEnum.DemandStatusEnum.EVALUATE.getName();
            case 2:
                return DemandEnum.DemandStatusEnum.PRE_IMPLEMENT.getName();
            case 3:
                return DemandEnum.DemandStatusEnum.DEVELOP.getName();
            case 4:
                return DemandEnum.DemandStatusEnum.SIT.getName();
            case 5:
                return DemandEnum.DemandStatusEnum.UAT.getName();
            case 6:
                return DemandEnum.DemandStatusEnum.REL.getName();
            case 7:
                return DemandEnum.DemandStatusEnum.PRODUCT.getName();
            case 8:
                return DemandEnum.DemandStatusEnum.PLACE_ON_FILE.getName();
            case 9:
                return DemandEnum.DemandStatusEnum.IS_CANCELED.getName();
            default:
                return "";
        }

    }
    public static String changeSpeStateCn(Integer state) {
        if (CommonUtils.isNullOrEmpty(state)) {
            return "";
        }
        switch (state) {
            case 1:
                return DemandEnum.DemandDeferStatus.DEFER.getName();
            case 2:
                return DemandEnum.DemandDeferStatus.RECOVER.getName();
            case 3:
                return DemandEnum.DemandDeferStatus.FINISTH.getName();

            default:
                return "";
        }


    }
    public static String changeDemandType(String type) {
        if (CommonUtils.isNullOrEmpty(type)) {
            return null;
        }
        switch (type) {
            case "business":
                return "业务需求";
            case "tech":
                return "科技需求";
            default:
                return "其他需求类型";
        }
    }

	public static String changeDemandLabelList(Set<String> demandLabels,List<DictEntity> demand_label_list) {
		if (CommonUtils.isNullOrEmpty(demandLabels)) {
			return "";
		}
		String demandLabelStr = "" ;
		for (DictEntity demandLabel : demand_label_list) {
			if( demandLabels.contains(demandLabel.getCode()) ){
				if(CommonUtils.isNullOrEmpty(demandLabelStr)){
					demandLabelStr = demandLabel.getValue();
				}else{
					demandLabelStr += "," + demandLabel.getValue();
				}
			}
		}
		return demandLabelStr ;
	}

	public static String changeDemandProperty(String type) {
		if (CommonUtils.isNullOrEmpty(type)) {
			return null;
		}
		switch (type) {
			case "advancedResearch":
				return "预研";
			case "keyPoint":
				return "重点";
			default:
				return "常规";
		}
	}

    public static String changeAvaliable(String avaliable) {
        if (CommonUtils.isNullOrEmpty(avaliable)) {
            return null;
        }
        switch (avaliable) {
            case "0":
                return "可行";
            case "1":
                return "部分可行";
            case "2":
                return "不可行";
            default:
                return "";
        }
    }
    public static String changeAssessWay(String assessway) {
        if (CommonUtils.isNullOrEmpty(assessway)) {
            return null;
        }

        switch (assessway) {
            case "0":
                return "业务需求评审";
            case "1":
                return "科技需求评审";
            default:
                return "";
        }
    }
    public static String changeFutureAssess(String futureAssess) {
        if (CommonUtils.isNullOrEmpty(futureAssess)) {
            return null;
        }

        switch (futureAssess) {
            case "0":
                return "纳入后评估";
            case "1":
                return "不纳入后评估";
            default:
                return "";
        }
    }
    public static String changeDesignStatus(String designStatus) {
        if (CommonUtils.isNullOrEmpty(designStatus)) {
            return null;
        }
        switch (designStatus) {
            case "noRelate":
                return UIEnum.UIDesignEnum.NORELATE.getName();
            case "uploadNot":
                return UIEnum.UIDesignEnum.UPLOADED.getName();
            case "uploaded":
                return UIEnum.UIDesignEnum.UPLOADED.getName();
            case "auditWait":
                return UIEnum.UIDesignEnum.AUDITWAIT.getName();
            case "auditIn":
                return UIEnum.UIDesignEnum.AUDITIN.getName();
            case "auditPass":
                return UIEnum.UIDesignEnum.AUDITPASS.getName();
            case "auditPassNot":
                return UIEnum.UIDesignEnum.AUDITPASSNOT.getName();
            case "completedAudit":
                return UIEnum.UIDesignEnum.COMPLETEAUDIT.getName();
            case "abnormalShutdown":
                return UIEnum.UIDesignEnum.ABNORMALSHUTDOWN.getName();


            default:
                return "";
        }
    }
    public static String changePriority(String priority) {
        if (CommonUtils.isNullOrEmpty(priority)) {
            return null;
        }
        switch (priority) {
            case "0":
                return "高";
            case "1":
                return "中";
            case "2":
                return "一般";
            case "3":
                return "低";
            default:
                return "";
        }

    }
    public static String demandLeader(DemandBaseInfo demandBaseInfo) {
        List<UserInfo> leaderAll = demandBaseInfo.getDemand_leader_all();
        String result = "";
        if (CommonUtils.isNullOrEmpty(leaderAll)) {
            return result;
        }
        for (UserInfo userInfo :leaderAll) {
            result = result + userInfo.getUser_name_cn()+"，";
        }
        if (CommonUtils.isNullOrEmpty(result)) {
            return result;
        }
        result = result.substring(0,result.length()-1);
        return result;
    }
    //涉及板块信息格式化整合
    public static String relatePart(DemandBaseInfo demandBaseInfo) {
        String result = "";
        List<RelatePartDetail> relatePart= demandBaseInfo.getRelate_part_detail();
        if (CommonUtils.isNullOrEmpty(relatePart)) {
            return null;
        }
        for (RelatePartDetail relatePartDetail : relatePart) {
            String person = "";
            List<UserInfo> users = relatePartDetail.getAssess_user_all();
            if(CommonUtils.isNullOrEmpty(users)) {
                continue;
            }
            for (UserInfo user:users) {
                person+=user.getUser_name_cn()+"，";
            }
            person = person.substring(0,person.length()-1);
            result +="(" + relatePartDetail.getPart_name()+"："+DemandBaseInfoUtil.changeAssessStatus(relatePartDetail.getAssess_status())+"；评估人："+person + ")"+",";
        }
        if (CommonUtils.isNullOrEmpty(result)) {
            return result;
        }
        result = result.substring(0,result.length()-1);
        return result;

    }

    public static String getDocType(String doctype) {
        if (CommonUtils.isNullOrEmpty(doctype)) {
            return null;
        }
        switch (doctype) {
            case "demandInstruction":
                return DemandDocEnum.DemandDocTypeEnum.DEMAND_INSTRUCTION.getName();
            case "techPlan":
                return DemandDocEnum.DemandDocTypeEnum.TECH_PLAN.getName();
            case "demandReviewResult":
                return DemandDocEnum.DemandDocTypeEnum.DEMAND_REVIEW_RESULT.getName();
            case "meetMinutes":
                return DemandDocEnum.DemandDocTypeEnum.MEETMINUTES.getName();
            case "otherRelatedFile":
                return DemandDocEnum.DemandDocTypeEnum.OTHER_RELATED_FILE.getName();
            case "confluenceFile":
                return DemandDocEnum.DemandDocTypeEnum.CONFLUENCE_FILE.getName();
            case "demandPlanInstruction":
                return DemandDocEnum.DemandDocTypeEnum.DEMAND_PLAN_INSTRUCTION.getName();
            case "innerTestReport":
                return DemandDocEnum.DemandDocTypeEnum.INNER_TEST_REPORT.getName();
            case "deferEmail":
                return DemandDocEnum.DemandDocTypeEnum.DEFER_EMAIL.getName();
            case "businessTestReport":
                return DemandDocEnum.DemandDocTypeEnum.BUSINESS_TEST_REPORT.getName();
            case "launchConfirm":
                return DemandDocEnum.DemandDocTypeEnum.LAUNCH_CONFIRM.getName();
            case "demandAssessReport":
                return DemandDocEnum.DemandDocTypeEnum.DEMAND_ASSESS_REPORT.getName();
            case "demandPlanConfirm":
                return DemandDocEnum.DemandDocTypeEnum.DEMAND_PLAN_CONFIRM.getName();
            default:
                return null;
        }

    }

    public static String changeAssessStatus(String assessStatus) {
        if (CommonUtils.isNullOrEmpty(assessStatus)) {
            return "";
        }
        switch (assessStatus) {
            case "0":
                return DemandEnum.DemandAssess_status.PRE_EVALUATE.getName();
            case "1":
                return DemandEnum.DemandAssess_status.EVALUATE.getName();
            case "2":
                return DemandEnum.DemandAssess_status.EVALUATE_OVER.getName();

            default:
                return "其他状态";
            //return "";
        }
    }
}
