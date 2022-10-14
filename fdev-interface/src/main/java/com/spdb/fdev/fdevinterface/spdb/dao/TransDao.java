package com.spdb.fdev.fdevinterface.spdb.dao;

import com.spdb.fdev.fdevinterface.spdb.entity.Trans;
import com.spdb.fdev.fdevinterface.spdb.entity.TransParamDesciption;
import com.spdb.fdev.fdevinterface.spdb.vo.TransParamShow;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

public interface TransDao {

    void saveTrans(List<Trans> transList);

    void deleteTrans(String appServiceId, String branchName);

    void updateTransTags(List<String> ids, List<String> tags);

    List<Trans> getTransList(@NotNull String serviceId, @NotNull String branch);

    Map showTrans(TransParamShow paramShow);

    Trans getTransDetailById(String id);

    Trans getTrans(String transId, String serviceId, String branch, String channel);

    /**
     * 根据一条交易找到这个交易的其他所有渠道的交易信息
     *
     * @param trans
     * @return
     */
    List<Trans> getTrans(Trans trans);

    void saveParamDescription(List<TransParamDesciption> transParamDesciptionList);

    TransParamDesciption getParamDescription(String transId, String serviceId, String channel);

    void updateParamDescription(Trans trans);

    void deleteTrans(Map params);

    List<Trans> showAllTrans();
    
    Map queryTrans(TransParamShow paramShow);
}
