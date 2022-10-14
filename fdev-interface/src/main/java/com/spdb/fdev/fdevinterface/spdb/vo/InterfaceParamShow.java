package com.spdb.fdev.fdevinterface.spdb.vo;

import java.util.List;

/**
 * 接口列表/调用关系 查询请求参数对象
 */
public class InterfaceParamShow {

    private String branchDefault;
    private String interfaceType;
    private List<String> transId;
    private String serviceCalling;
    private String serviceId;
    private String interfaceName;
    private String branch;
    private Integer page;
    private Integer pageNum;
    private List<String> transOrServiceOrOperation;

    public String getBranchDefault() {
        return branchDefault;
    }

    public void setBranchDefault(String branchDefault) {
        this.branchDefault = branchDefault;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public List<String> getTransId() {
        return transId;
    }

    public void setTransId(List<String> transId) {
        this.transId = transId;
    }

    public String getServiceCalling() {
        return serviceCalling;
    }

    public void setServiceCalling(String serviceCalling) {
        this.serviceCalling = serviceCalling;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }


    public List<String> getTransOrServiceOrOperation() {
        return transOrServiceOrOperation;
    }

    public void setTransOrServiceOrOperation(List<String> transOrServiceOrOperation) {
        this.transOrServiceOrOperation = transOrServiceOrOperation;
    }
}
