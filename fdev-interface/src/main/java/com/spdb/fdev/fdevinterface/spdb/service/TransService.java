package com.spdb.fdev.fdevinterface.spdb.service;

import com.spdb.fdev.fdevinterface.spdb.entity.Trans;
import com.spdb.fdev.fdevinterface.spdb.vo.TransParamShow;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface TransService {

    void saveTrans(List<Trans> transList, String appServiceId, String branchName);

    List<Trans> getTransList(@NotNull String serviceId, @NotNull String branch);

    Map showTrans(TransParamShow paramShow);

    Trans getTransDetailById(String id);

    void updateTransTags(String id, List<String> tags);

    void updateParamDescription(Trans trans);

    void deleteTransData(Map params);

    Map showAllTrans();
    
    Map queryTrans(TransParamShow paramShow);

	public void exprotTrans(Map map,HttpServletResponse resp) throws Exception ;
}
