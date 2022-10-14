package com.spdb.fdev.fdevfootprint.spdb.entity;

import java.io.Serializable;

public class SwitchModel implements Serializable {

    private String channelNo;

    private String channelName;

    private String status;

    private String autoPercent;

    public SwitchModel() {
    }

    public SwitchModel(String channelNo, String channelName, String status, String autoPercent) {
        this.channelNo = channelNo;
        this.channelName = channelName;
        this.status = status;
        this.autoPercent = autoPercent;
    }

    public String getChannelNo() {
        return channelNo;
    }

    public void setChannelNo(String channelNo) {
        this.channelNo = channelNo;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAutoPercent() {
        return autoPercent;
    }

    public void setAutoPercent(String autoPercent) {
        this.autoPercent = autoPercent;
    }
}
