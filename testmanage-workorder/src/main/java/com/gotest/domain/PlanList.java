package com.gotest.domain;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
public class PlanList implements Serializable {

    private Integer planId;

    private String workNo;

    private String planName;

    private String planStartDate;

    private String planEndDate;
    
    private String deviceInfo;

    private String field1;

    private String field2;

    private String field3;

    private String field4;

    private String field5;

    private Long allCase;

    private Long exeNum ;

    private  Long sumFail ;

    private Long sumBlock ;

    private  Long sumUnExe ;

    private Long sumSucc;

    private String unitNo;

    private String testers ;

    private Map testResult;

    private List<String> testernames;

    public List<String> getTesternames() {
        return testernames;
    }

    public void setTesternames(List<String> testernames) {
        this.testernames = testernames;
    }

    public String getTesters() {
        return testers;
    }

    public void setTesters(String testers) {
        this.testers = testers;
    }

    public Map getTestResult() {
        return testResult;
    }

    public void setTestResult(Map testResult) {
        this.testResult = testResult;
    }

    public String getUnitNo() {
        return unitNo;
    }

    public void setUnitNo(String unitNo) {
        this.unitNo = unitNo;
    }

    public Long getSumSucc() {
        return sumSucc;
    }

    public void setSumSucc(Long sumSucc) {
        this.sumSucc = sumSucc;
    }

    public Long getAllCase() {
        return allCase;
    }

    public void setAllCase(Long allCase) {
        this.allCase = allCase;
    }

    public Long getExeNum() {
        return exeNum;
    }

    public void setExeNum(Long exeNum) {
        this.exeNum = exeNum;
    }

    public Long getSumFail() {
        return sumFail;
    }

    public void setSumFail(Long sumFail) {
        this.sumFail = sumFail;
    }

    public Long getSumBlock() {
        return sumBlock;
    }

    public void setSumBlock(Long sumBlock) {
        this.sumBlock = sumBlock;
    }

    public Long getSumUnExe() {
        return sumUnExe;
    }

    public void setSumUnExe(Long sumUnExe) {
        this.sumUnExe = sumUnExe;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    private static final long serialVersionUID = 1L;

    public Integer getPlanId() {
        return planId;
    }

    public void setPlanId(Integer planId) {
        this.planId = planId;
    }

    public String getWorkNo() {
        return workNo;
    }

    public void setWorkNo(String workNo) {
        this.workNo = workNo == null ? null : workNo.trim();
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName == null ? null : planName.trim();
    }

    public String getPlanStartDate() {
		return planStartDate;
	}

	public void setPlanStartDate(String planStartDate) {
		this.planStartDate = planStartDate;
	}

	public String getPlanEndDate() {
		return planEndDate;
	}

	public void setPlanEndDate(String planEndDate) {
		this.planEndDate = planEndDate;
	}

	public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1 == null ? null : field1.trim();
    }

    public String getField2() {
        return field2;
    }

    public void setField2(String field2) {
        this.field2 = field2 == null ? null : field2.trim();
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3 == null ? null : field3.trim();
    }

    public String getField4() {
        return field4;
    }

    public void setField4(String field4) {
        this.field4 = field4 == null ? null : field4.trim();
    }

    public String getField5() {
        return field5;
    }

    public void setField5(String field5) {
        this.field5 = field5 == null ? null : field5.trim();
    }
    

    public String getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

    @Override
    public String toString() {
        return "PlanList{" +
                "planId=" + planId +
                ", workNo='" + workNo + '\'' +
                ", planName='" + planName + '\'' +
                ", planStartDate='" + planStartDate + '\'' +
                ", planEndDate='" + planEndDate + '\'' +
                ", deviceInfo='" + deviceInfo + '\'' +
                ", field1='" + field1 + '\'' +
                ", field2='" + field2 + '\'' +
                ", field3='" + field3 + '\'' +
                ", field4='" + field4 + '\'' +
                ", field5='" + field5 + '\'' +
                ", allCase=" + allCase +
                ", exeNum=" + exeNum +
                ", sumFail=" + sumFail +
                ", sumBlock=" + sumBlock +
                ", sumUnExe=" + sumUnExe +
                ", sumSucc=" + sumSucc +
                ", unitNo='" + unitNo + '\'' +
                ", testers='" + testers + '\'' +
                ", testResult=" + testResult +
                ", testernames=" + testernames +
                '}';
    }
}