package com.spdb.fdev.fdemand.spdb.notify;

import java.util.Map;

import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;

public interface INotifyStrategy {

    void doNotify(Map<String,Object> param);
    
}
