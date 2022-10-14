package com.mantis.service.impl;

import com.mantis.dict.Dict;
import com.mantis.dict.ErrorConstants;
import com.mantis.service.ApiService;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ApiServiceImpl implements ApiService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;

    @Override
    public List<String> queryTaskNoByWorkNos(List<String> workNos) throws Exception {
        Map sendData = new HashMap();
        sendData.put(Dict.WORKNOS, workNos);
        sendData.put(Dict.REST_CODE, "queryTaskNoByWorkNos");
        return (List<String>) restTransport.submit(sendData);
    }

    @Override
    public Map queryInfoByTaskNo(String taskNo) throws Exception {
        try  {
            Map sendMap = new HashMap();
            List list = new ArrayList();
            list.add(taskNo);
            sendMap.put(Dict.IDS, list);
            sendMap.put(Dict.REST_CODE, "fdev.task.ids.query");
            List<Map> result = (List<Map>) restTransport.submitSourceBack(sendMap);
            if (Util.isNullOrEmpty(result)) {
                return null;
            }
            return result.get(0);
        } catch (Exception e) {
            logger.error("fail to query old task info"+taskNo);
        }
        return null;
    }

    @Override
    public String querymainTaskNoByWorkNo(String workNo) throws Exception {
        Map sendData = new HashMap();
        sendData.put(Dict.WORKNO, workNo);
        sendData.put(Dict.REST_CODE, "querymainTaskNoByWorkNo");
        return (String) restTransport.submit(sendData);
    }

    /**
     * 根据任务id查fdev重构后任务
     *
     * @param taskNo
     * @return
     * @throws Exception
     */
    @Override
    public Map queryNewTaskInfoByTaskNo(String taskNo) throws Exception {
        Map send = new HashMap();
        send.put(Dict.ID, taskNo);
        send.put(Dict.REST_CODE, "getTaskById");
        try {
            Map result = (Map) restTransport.submitSourceBack(send);
            if (Util.isNullOrEmpty(result)) {
                logger.error("fail to get task info");
                throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"任务信息不存在"});
            }
            return result;
        } catch (Exception e) {
            logger.error("fail to get task info");
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"任务信息不存在"});
        }
    }

    @Override
    public Map queryFdevGroupInfo(String groupId) throws Exception {
        Map send = new HashMap();
        send.put(Dict.ID, groupId);
        send.put(Dict.REST_CODE, "queryGroup");
        try {
            List<Map> result = (List<Map>) restTransport.submitSourceBack(send);
            if (Util.isNullOrEmpty(result)) {
                logger.error("fail to get group info");
                throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"组信息不存在"});
            }
            return result.get(0);
        } catch (Exception e) {
            logger.error("fail to get group info" + e);
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"组信息不存在"});
        }
    }

    /**
     * 根据应用id查新老应用模块应用名
     *
     * @param appId
     * @return
     * @throws Exception
     */
    @Override
    public String queryAppNameByAppid(String appId) throws Exception {
        try {
            //查新应用模块
            Map send = new HashMap();
            send.put(Dict.IDS, Arrays.asList(appId));
            send.put(Dict.REST_CODE, "new.fdev.queryApps");
            Map result = (Map) restTransport.submitSourceBack(send);
            List<Map> apps = (List<Map>) result.get("serviceAppList");
            if (Util.isNullOrEmpty(apps)) {
                //新应用模块不存在，查老应用模块
                send.put(Dict.ID, appId);
                send.put(Dict.REST_CODE, "fdev.findbyid");
                result = (Map) restTransport.submitSourceBack(send);
                return (String) result.get("name_en");
            } else {
                return (String) apps.get(0).get("nameEN");
            }
        } catch (Exception e) {
            logger.error("fail to find app info");
        }
        return "";
    }

    @Override
    public List<Map> queryTaskBaseInfoByIds(Set<String> taskIds) {
        Map<String, Object> param = new HashMap<String, Object>(){{
            put(Dict.IDS, taskIds);
            put(Dict.REST_CODE, "queryTaskBaseInfoByIds");
        }};
        try {
            return (List<Map>) restTransport.submitSourceBack(param);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map> queryWorkOrderByNos(Set<String> workNos, List<String> fields) {
        Map<String, Object> param = new HashMap<String, Object>(){{
            put(Dict.WORKNOS, workNos);
            put(Dict.FIELDS, fields);
            put(Dict.REST_CODE, "queryWorkOrderByNos");
        }};
        try {
            return (List<Map>) restTransport.submitSourceBack(param);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
