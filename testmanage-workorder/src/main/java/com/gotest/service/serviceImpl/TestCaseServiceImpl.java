package com.gotest.service.serviceImpl;

import com.gotest.dao.PlanListTestCaseMapper;
import com.gotest.dict.Dict;
import com.gotest.dict.ErrorConstants;
import com.gotest.service.TestCaseService;
import com.test.testmanagecommon.exception.FtmsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 *  案例相关接口
 */
@SuppressWarnings("all")
@Service
public class TestCaseServiceImpl implements TestCaseService {

    @Autowired
    private PlanListTestCaseMapper planListTestCaseMapper;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 根据工单号查案例状态判断可否进uat
     * @param workNo
     * @throws Exception
     */
    public String judgeByCaseBeforeUat(String workNo) throws Exception{
        List<Map<String,String>> resultList = null;
        try {
            resultList = planListTestCaseMapper.countCase(workNo);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new FtmsException(ErrorConstants.DATA_QUERY_ERROR,new String[]{"根据工单查询案例失败！"});
        }
        for(Map m : resultList){
            String status = m.get(Dict.STATUS).toString();
            if("0".equals(status)){
                return "未执行";
            }
            if("2".equals(status)){
                return "阻塞";
            }
            if("3".equals(status)){
                return "失败";
            }
        }
        return null;
    }
}
