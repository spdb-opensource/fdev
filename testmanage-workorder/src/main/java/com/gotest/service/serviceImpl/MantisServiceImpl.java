package com.gotest.service.serviceImpl;

import com.gotest.dao.WorkOrderMapper;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.service.MantisService;
import com.gotest.utils.CommonUtils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@SuppressWarnings("all")
@Service
public class MantisServiceImpl implements MantisService {

    private Logger logger = LoggerFactory.getLogger(MantisServiceImpl.class);

    @Autowired
    private WorkOrderMapper workOrderMapper;
    @Autowired
    private RestTransport restTransport;

    @Override
    public Map<String, String> queryWorkNoByTaskNo(List<String> list) throws Exception {
        StringBuffer sb = new StringBuffer();
        Map<String, String> resMap = new HashMap<>();
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()){
            String taskNo = iterator.next();
            sb.append(taskNo);
            resMap.put(taskNo, null);
            if (iterator.hasNext()){
                sb.append(",");
            }
        }
        List<String> res = null;
        try {
            List<Map<String, String>> workNos = workOrderMapper.queryWorkNoByTaskNo(sb.toString());
            workNos.remove(null);
            if (!Util.isNullOrEmpty(workNos))
                workNos.forEach(ele ->{
                    resMap.put(ele.get(Dict.MAIN_TASK_NO), ele.get(Dict.WORK_NO));
                });
        }catch (Exception e){
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR, new String[]{list.toString()});
        }
        return resMap;
    }

    /**
     * 根据工单号查缺陷情况
     * @param workNo
     * @return
     * @throws Exception
     */
    public String judgeByMantisBeforeUat(String workNo) throws Exception {
        Map sendMap = new HashMap();
        sendMap.put(Dict.WORKNO, workNo);
        sendMap.put(Dict.REST_CODE, "mantis.countMantisByWorkNo");
        List<Map<String, String>> resultList = null;
        try {
            resultList = (List<Map<String, String>>)restTransport.submit(sendMap);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"根据工单查询缺陷失败！"});
        }
        for(Map m : resultList){
            String status = m.get(Dict.STATUS).toString();
            if("10".equals(status)){
                return "未处理";
            }
            if("20".equals(status)){
                return "被拒绝";
            }
            if("40".equals(status)||"50".equals(status)){
                return "未修复";
            }
            if("80".equals(status)){
                return "未关闭";
            }
        }
        return null;
    }

    @Override
    public List<Map<String, String>> queryQualityAll() throws Exception {
        Map send = new HashMap();
        send.put(Dict.REST_CODE, "ftms.mantis.qualityReportAll");
        return (List<Map<String, String>>)restTransport.submit(send);
    }

    @Override
    public Map<String, Integer> countMantisByGroup(String startDate, String endDate, List<String> groupIds) {
        Map<String, Object> param = new HashMap<String, Object>(){{
            put(Dict.STARTDATE, startDate);
            put(Dict.ENDDATE, endDate);
            put(Dict.GROUPIDS, groupIds);
            put(Dict.REST_CODE, "countMantisByGroup");
        }};
        Map<String, Integer> result = new HashMap<>();
        try {
            List<Map> mantisCountList = (List<Map>) restTransport.submitSourceBack(param);
            if (!CommonUtils.isNullOrEmpty(mantisCountList)) {
                for (Map mantisCount : mantisCountList) {
                    result.put(String.valueOf(mantisCount.get(Dict.GROUPID)), (Integer) mantisCount.get(Dict.COUNT_MANTIS));
                }
            }
            return result;
        } catch (Exception e) {
            return result;
        }
    }
}
