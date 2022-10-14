package com.spdb.fdev.fdemand.spdb.dto.testorder;

import com.spdb.fdev.fdemand.spdb.dto.PageDto;

import java.util.List;

public class TestOrderDto extends PageDto {

    private String testOrder;//测试单编号

    private String implKey;//研发单元关键字

    private String ipmpKey;//实施单元关键字

    private String demandKey;//需求关键字

    private String group;//小组

    private List<String> status;//状态

    private String creatorId;//创建人

    public String getIpmpKey() {
        return ipmpKey;
    }

    public void setIpmpKey(String ipmpKey) {
        this.ipmpKey = ipmpKey;
    }

    public String getImplKey() {
        return implKey;
    }

    public void setImplKey(String implKey) {
        this.implKey = implKey;
    }

    public String getDemandKey() {
        return demandKey;
    }

    public void setDemandKey(String demandKey) {
        this.demandKey = demandKey;
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getTestOrder() {
        return testOrder;
    }

    public void setTestOrder(String testOrder) {
        this.testOrder = testOrder;
    }
}
