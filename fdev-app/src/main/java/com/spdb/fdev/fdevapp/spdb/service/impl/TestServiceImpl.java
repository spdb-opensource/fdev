package com.spdb.fdev.fdevapp.spdb.service.impl;

import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.fdevapp.base.dict.Dict;
import com.spdb.fdev.fdevapp.base.dict.ErrorConstants;
import com.spdb.fdev.fdevapp.base.utils.CommonUtils;
import com.spdb.fdev.fdevapp.spdb.service.TestService;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    private RestTransport restTransport;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public List queryMantisByTaskList(List taskList) throws Exception {
        Object resp = null;
        List result = new ArrayList();
        try {
            Map param = new HashMap();
            param.put(Dict.TASKLIST,taskList);
            param.put(Dict.REST_CODE,"queryMantisByTaskList");
            resp = this.restTransport.submit(param);
            List mantisList = (List)resp;
            Map tmpResult = new HashMap();
            for(Object item:mantisList){
                Map mantis = new HashMap();
                Map tmp = (Map) item;
                List tmpList = new ArrayList();
                String taskId = (String) tmp.get("main_task_no");
                if(CommonUtils.isNullOrEmpty(taskId)) throw new FdevException(ErrorConstants.DATA_NOT_EXIST,new String[]{"缺陷数据异常"});
                if (tmpResult.containsKey(taskId)) {
                    mantis = (Map) tmpResult.get(taskId);
                    tmpList = (List) mantis.get("mantis");
                }else {
                    mantis.put(Dict.ID,taskId);
                    Map task = queryTaskDetail(taskId);
                    mantis.put(Dict.NAME,CommonUtils.isNullOrEmpty(task)?"":task.get(Dict.NAME));
                }
                tmpList.add(tmp);
                mantis.put("mantis",tmpList);
                tmpResult.put(taskId,mantis);
            }
            tmpResult.keySet().forEach(n->result.add(tmpResult.get(n)));
        } catch (Exception e) {
            logger.error("组装mantis数据出错"+e.getMessage());
            throw new FdevException(ErrorConstants.MANTIS_ERROR,new String[]{e.getMessage()});
        }
        return result;
    }

    private Map queryTaskDetail(String id) throws Exception{
        Map param = new HashMap();
        List ids = new ArrayList();
        ids.add(id);
        param.put(Dict.IDS,ids);
        param.put(Dict.REST_CODE,Dict.QUERYTASKS);
        Object result = restTransport.submit(param);
        if(!CommonUtils.isNullOrEmpty(result)){
            List tmpList = (List)result;
            if(tmpList.size()==1){
                return (Map)tmpList.get(0);
            }
        }
        return new HashMap();
    }
}
