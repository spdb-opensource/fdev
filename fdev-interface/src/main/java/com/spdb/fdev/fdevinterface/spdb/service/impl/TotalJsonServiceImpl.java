package com.spdb.fdev.fdevinterface.spdb.service.impl;

import com.spdb.fdev.fdevinterface.base.dict.Constants;
import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.base.utils.FileUtil;
import com.spdb.fdev.fdevinterface.spdb.dao.TotalJsonDao;
import com.spdb.fdev.fdevinterface.spdb.entity.TotalDat;
import com.spdb.fdev.fdevinterface.spdb.entity.TotalJson;
import com.spdb.fdev.fdevinterface.spdb.service.TotalJsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author xxx
 * @date 2020/7/30 16:52
 */
@Service
@RefreshScope
public class TotalJsonServiceImpl implements TotalJsonService {

    @Value("${finterface.nas}")
    private String finterfaceNas;
    @Autowired
    private TotalJsonDao totalDatDao;

    @Override
    public void save(TotalJson totalJson) {
        String env = totalJson.getEnv();
        // 将库里isNew为1的先更新为0
        totalDatDao.updateIsNew(env);
        if (Dict.GRAY.equals(env) || Dict.PRO.equals(env)) {
            // 判断当前变更是否有准备过介质，若准备过，则更新，没准备过，则新增
            TotalJson totalJsonByProdId = totalDatDao.getTotalDatByProdId(env, totalJson.getProdId());
            if (totalJsonByProdId != null) {
                totalDatDao.updateByProdId(totalJson);
            } else {
                totalDatDao.save(totalJson);
            }
        } else {
            totalDatDao.save(totalJson);
        }
        // 只保留最新20个版本
        deleteTotalDao(env);
    }

    @Override
    public List<TotalJson> getTotalJsonList(Map<String, Object> requestMap) {
        String projectName = (String) requestMap.get(Dict.PROJECT_NAME);
        return totalDatDao.getTotalJsonList(projectName);
    }

    @Override
    public List<TotalJson> queryTotalJsonHistory(Map<String, Object> requestMap) {
        String env = (String) requestMap.get(Dict.ENV);
        return totalDatDao.queryTotalJsonHistory(env);
    }

    @Override
    public TotalDat getNewTotalJson(String env) {
        return totalDatDao.getNewTotalJson(env);
    }

    @Override
    public Integer haveNewApp(String env, String prodId) {
        Integer haveNewApp = 0;
        TotalJson totalJsonByProdId = totalDatDao.getTotalDatByProdId(env, prodId);
        if (totalJsonByProdId != null) {
            Integer flag = totalJsonByProdId.getFlag();
            if (flag != null) {
                haveNewApp = flag;
            }
        }
        return haveNewApp;
    }

    @Override
    public List<TotalJson> getTotalJsonListByEnv(Map<String, Object> envMap) {
        String env = (String) envMap.get(Dict.ENV);
        return totalDatDao.getTotalJsonListByEnv(env);
    }

    @Async
    private void deleteTotalDao(String env) {
        // 获取最新20个版本
        List<TotalJson> totalJsonList = totalDatDao.getNewTwentyTotalDat(env);
        if (totalJsonList.size() == 20) {
            TotalJson totalJson = totalJsonList.get(19);
            List<TotalJson> delTotalJsonList = totalDatDao.deleteTotalTar(env, totalJson.getDatTime());
            for (TotalJson json : delTotalJsonList) {
                FileUtil.deleteDir(finterfaceNas + Dict.TOTAL + Constants.SLASH + json.getTotalTarName());
            }
        }
    }

}
