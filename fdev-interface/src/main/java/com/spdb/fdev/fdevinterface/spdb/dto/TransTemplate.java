package com.spdb.fdev.fdevinterface.spdb.dto;

public class TransTemplate {

    private String templateId;
    private String chainId;
    private Integer writeJnl;
    private Integer needLogin;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getChainId() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public Integer getWriteJnl() {
        return writeJnl;
    }

    public void setWriteJnl(Integer writeJnl) {
        this.writeJnl = writeJnl;
    }

    public Integer getNeedLogin() {
        return needLogin;
    }

    public void setNeedLogin(Integer needLogin) {
        this.needLogin = needLogin;
    }
}
