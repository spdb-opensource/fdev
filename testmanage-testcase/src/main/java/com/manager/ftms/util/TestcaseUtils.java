package com.manager.ftms.util;

import com.manager.ftms.controller.TestCaseController;
import com.manager.ftms.dao.PlanlistTestcaseRelationMapper;
import com.manager.ftms.entity.PlanlistTestcaseRelation;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TestcaseUtils {
    @Autowired
    private RedisUtils redisUtils;

    @Resource
    private RestTransport restTransport;

    @Autowired
    private PlanlistTestcaseRelationMapper planlistTestcaseRelationMapper;

    private static Logger logger = LoggerFactory.getLogger(TestCaseController.class);

    /**
     * 检查关系id是否执行过
     * @param planlistTestcaseId
     * @throws Exception
     */
    public void checkCaseExe(String planlistTestcaseId) throws Exception {
        PlanlistTestcaseRelation planlistTestcaseRelation =
                planlistTestcaseRelationMapper.queryRelation(planlistTestcaseId);
        if(Util.isNullOrEmpty(planlistTestcaseRelation)){
            logger.error("planlistTestcaseId not exist");
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST_ERROR);
        }
        if(!"0".equals(planlistTestcaseRelation.getTestcaseExecuteResult())){
            logger.error("case is not allowed to modify after execute");
            throw new FtmsException(ErrorConstants.EXE_CASE_MODIFY_FORBID);
        }
    }

    /**
     * 根据任务id查询任务详细信息
     * @param taskNo
     * @return
     * @throws Exception
     */
    public Map queryTaskDetail(String taskNo) throws Exception {
        Map send = new HashMap();
        send.put(Dict.ID, taskNo);
        send.put(Dict.REST_CODE, "fdev.ftask.queryTaskDetail");
        try {
            return (Map)restTransport.submitSourceBack(send);
        } catch (Exception e) {
            logger.error("fail to query task" + taskNo + e);
            return null;
        }
    }

    /**
     * 从任务信息中提取各角色英文名
     * @param target
     * @param source
     * @param role
     */
    public void addUserNameEnByRoleName(Set<String> target, Map source, String role) {
        if(!Util.isNullOrEmpty(source.get(role))){
            List<Map<String, String>> list = (List<Map<String, String>>)source.get(role);
            for(Map m : list){
                target.add(String.valueOf(m.get(Dict.USER_NAME_EN)));
            }
        }
    }

    /**
     * 根据用户英文名查fdev用户核心数据
     * @param userNameEn
     * @return
     * @throws Exception
     */
    public Map queryUserCoreDataByNameEn(String userNameEn) throws Exception {
        Map send = new HashMap();
        send.put(Dict.USER_NAME_EN, userNameEn);
        send.put(Dict.REST_CODE, "queryByUserCoreData");
        try {
            return ((List<Map>)restTransport.submitSourceBack(send)).get(0);
        } catch (Exception e) {
            logger.error("fail to query user" + userNameEn + e);
            return  null;
        }
    }
}
