package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.CommonUtil;
import com.spdb.fdev.fdevinterface.spdb.dao.TransDao;
import com.spdb.fdev.fdevinterface.spdb.dao.TransRelationDao;
import com.spdb.fdev.fdevinterface.spdb.entity.Trans;
import com.spdb.fdev.fdevinterface.spdb.entity.TransRelation;
import com.spdb.fdev.fdevinterface.spdb.service.InterfaceLazyInitService;
import com.spdb.fdev.fdevinterface.spdb.service.TransRelationService;
import com.spdb.fdev.fdevinterface.spdb.vo.TransParamShow;
import com.spdb.fdev.fdevinterface.spdb.vo.TransRelationShow;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransRelationServiceImpl implements TransRelationService {

    @Resource
    private TransRelationDao transRelationDao;
    @Resource
    private TransDao transDao;
    @Resource
    private InterfaceLazyInitService interfaceLazyInitService;

    @Override
    public void save(List<TransRelation> transRelationList, String appServiceId, String branchName, String channel) {
        transRelationDao.delete(appServiceId, branchName, channel);
        transRelationDao.save(transRelationList);
    }

    @Override
    public Map showTransRelation(TransParamShow paramShow) {
        Map returnMap = new HashMap();
        Map transMap = transRelationDao.showTransRelation(paramShow);
        List<TransRelation> transRelationList = (List<TransRelation>) transMap.get(Dict.LIST);
        List<TransRelationShow> transRelationShowList = new ArrayList<>();
        // 组装交易名称、交易详情ID、调用方ID、服务方ID
        for (TransRelation transRelation : transRelationList) {
            TransRelationShow transRelationShow = CommonUtil.convert(transRelation, TransRelationShow.class);
            String branch = transRelationShow.getBranch();
            // 除了master分支的其他分支，调的都是SIT的接口
            if (!Dict.MASTER.equals(branch)) {
                branch = Dict.SIT;
            }
            Trans trans = transDao.getTrans(transRelationShow.getTransId(), transRelationShow.getServiceId(), branch, transRelationShow.getChannel());
            // 关联交易中文名称及交易详情ID
            if (trans != null) {
                transRelationShow.setTransName(trans.getTransName());
                transRelationShow.setTransDetailId(trans.getId());
            }
            // 获取serviceProvider的应用Id
            transRelationShow.setServiceProviderAppId(interfaceLazyInitService.getAppIdByName(transRelationShow.getServiceId()));
            // 获取callingParty的应用Id
            transRelationShow.setCallingAppId(interfaceLazyInitService.getAppIdByName(transRelationShow.getServiceCalling()));
            transRelationShowList.add(transRelationShow);

        }
        returnMap.put(Dict.TOTAL, transMap.get(Dict.TOTAL));
        returnMap.put(Dict.LIST, transRelationShowList);
        return returnMap;
    }

    @Override
    public void deleteTransRelationData(Map params) {
        transRelationDao.deleteTransRelation(params);
    }


}
