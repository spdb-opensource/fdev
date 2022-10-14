package com.manager.ftms.service.impl;

import com.manager.ftms.controller.TestCaseController;
import com.manager.ftms.entity.PlanlistTestcaseRelation;
import com.manager.ftms.entity.TestcaseExeRecord;
import com.manager.ftms.service.IMantisService;
import com.manager.ftms.service.IPlanlistTestcaseRelationServier;
import com.manager.ftms.service.IUserService;
import com.manager.ftms.service.TestCaseService;
import com.manager.ftms.util.Constants;
import com.manager.ftms.util.Dict;
import com.manager.ftms.util.ErrorConstants;
import com.manager.ftms.util.Utils;
import com.test.testmanagecommon.exception.FtmsException;
import com.test.testmanagecommon.rediscluster.RedisUtils;
import com.test.testmanagecommon.transport.RestTransport;
import com.test.testmanagecommon.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PlanlistTestcaseRelationServierImpl implements IPlanlistTestcaseRelationServier {
	@Autowired
	private com.manager.ftms.dao.PlanlistTestcaseRelationMapper planlistTestcaseRelationMapper;

	@Autowired
	private TestCaseService testCaseService;
	@Autowired
	private RedisUtils redisUtils;

	@Autowired
	private RestTransport restTransport;

	@Autowired
	private IUserService iUserService;

	@Autowired
	private IMantisService mantisService;

	private static Logger logger = LoggerFactory.getLogger(TestCaseController.class);

	@Override
	public PlanlistTestcaseRelation queryRelationByTestcaseNoAndPlanId(String testcaseNo, Integer planId)
			throws Exception {
		return planlistTestcaseRelationMapper.selectRelationByTestcaseNoAndPlanId(testcaseNo, planId);
	}

	@Override
	public int savePlanlistTsetcaseRelation(PlanlistTestcaseRelation planlistTestcase) throws Exception {
		try {
			return planlistTestcaseRelationMapper.insert(planlistTestcase);
		} catch (Exception e) {
			throw new FtmsException(ErrorConstants.UPDATE_ERROR, new String[] { "数据插入失败!" });
		}
	}

	@Override
	public int batchRepeatedRelation(List testcaseNos, Integer planId, String workNo) throws Exception {
		try {
			// 当前时间戳
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String createTm = sdf.format(new Date());
			return planlistTestcaseRelationMapper.batchAddRelation(testcaseNos, planId, workNo, createTm);
		} catch (Exception e) {
			throw new FtmsException(ErrorConstants.UPDATE_ERROR, new String[] { "案例批量复用失败!" });
		}
    }

    @Override
    public List<PlanlistTestcaseRelation> selectByPlanId(Map map) throws Exception{

        return this.planlistTestcaseRelationMapper.selectByPlanId(map);
    }

    @Override
    public int deleteByPlanId(Integer planId)throws Exception {
        return this.planlistTestcaseRelationMapper.deleteByPlanId(planId);
    }

    /**
     * 添加案例计划关系
     *
     * @param map
     * @return int
     */
    @Override
    public int addPlanlistTsetcaseRelation(Map map)throws Exception {
        return planlistTestcaseRelationMapper.addPlanlistTsetcaseRelation(map);
    }

    /**
     * 根据流水id删除案例计划关系表关系
     *
     * @param planlistTsetcaseId
     * @return 受影响行数
     */
    @Override
    public int deletePlanlistTsetcaseRelationById(String planlistTsetcaseId)throws Exception {
    	//删除案例关系的同时要删除 案例执行记录
		planlistTestcaseRelationMapper.deleteTestcaseExeRecord(planlistTsetcaseId);
		//新增案例删除记录
		addRelationDeleteRecord(planlistTsetcaseId);
        //物理删除关系
		return planlistTestcaseRelationMapper.deletePlanlistTsetcaseRelationById(planlistTsetcaseId);
    }

	/**
	 * 新增案例删除记录
	 * @param planlistTsetcaseId
	 * @throws Exception
	 */
	@Override
	public void addRelationDeleteRecord(String planlistTsetcaseId) throws Exception {
		//删除案例关系的同时在执行表中新增一条执行删除记录
		PlanlistTestcaseRelation fpr = planlistTestcaseRelationMapper.queryRelation(planlistTsetcaseId);
		Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
		if(Util.isNullOrEmpty(user)){
			throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
		}
		// 当前时间戳
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String executeDate = sdf.format(new Date());
		//案例执行独立统计
		TestcaseExeRecord testcaseExeRecord = new TestcaseExeRecord();
		testcaseExeRecord.setDate(executeDate);
		testcaseExeRecord.setFprId(String.valueOf(planlistTsetcaseId));
		testcaseExeRecord.setOpr((String)user.get(Dict.USER_NAME_EN));
		testcaseExeRecord.setWorkNo(fpr.getWorkNo());
		testcaseExeRecord.setStatus(fpr.getTestcaseExecuteResult());
		testcaseExeRecord.setPlanId(String.valueOf(fpr.getPlanId()));
		testcaseExeRecord.setTestcaseNo(String.valueOf(fpr.getTestcaseNo()));
		testcaseExeRecord.setOprType(Constants.DEFAULT_1);
		planlistTestcaseRelationMapper.addTestcaseExeRecord(testcaseExeRecord);
	}

    /**
     * 根据案例编号和计划编号查询关系表数据数量
     *
     * @param testcaseNo
     * @return 数量
     */
    @Override
    public int queryCountByTestcaseNo(String testcaseNo) throws Exception{
        return planlistTestcaseRelationMapper.queryCountByTestcaseNo(testcaseNo);
    }

    /**
     * 根据planId 删除 案例关系表
     *
     * @param planid
     * @return
     */
    @Override
    public int delPlanlistTestcaseIdByPlanId(Integer planid) throws Exception {
        return this.planlistTestcaseRelationMapper.delPlanlistTestcaseIdByPlanId(planid);
    }

    @Override
    public int delBatchRelationCase(Map map) throws Exception{
        int row = 1;
        List<String> testcaseNoList =new ArrayList<String>();
        List<Map> mapa= (List<Map>) map.get(Dict.TESTCASENOS);
        Integer planId = (Integer) map.get(Dict.PLANID);
        StringBuilder error = new StringBuilder();
        for (Map item : mapa) {
			String testcaseNo = String.valueOf(item.get(Dict.TESTCASENO));
			int count = this.planlistTestcaseRelationMapper.queryPlanlistTestcaseRelationCountBytestcaseNo(testcaseNo);
			//通过planId和testcaseNo获取关系id
			PlanlistTestcaseRelation ptr = planlistTestcaseRelationMapper.selectRelationByTestcaseNoAndPlanId(testcaseNo, planId);
			if(Util.isNullOrEmpty(ptr)){
				continue;
			}
			String relationId = ptr.getPlanlistTestcaseId();

			if(!"0".equals(String.valueOf(ptr.getExeNum()))){
				//查询当前案例关系下是否存在缺陷，若有缺陷则不允许删除,记录该条数据最后统一抛异常
				List<Map> issues = mantisService.queryIssuesByFprId(relationId);
				if(!Util.isNullOrEmpty(issues) && issues.size() != 0){
					error.append(relationId+"|");
					continue;
				}
			}
			if (count >= 1) {
				int delNum = deletePlanlistTsetcaseRelationById(relationId);
				if (1 > delNum) {
					logger.error("案例关系删除失败!");
					throw new FtmsException(ErrorConstants.DATA_DEL_ERROR);
				}
			}
        }
        if(!Util.isNullOrEmpty(error.toString())){
			throw new FtmsException(ErrorConstants.BATCH_DELETE_CONTAIN_ISSUE_ERROR, new String[]{error.toString()});
		}
        return row;
    }

    @Override
    public int batchCopyTestcaseToOtherPlan(List<String> testcaseNos, Integer planId, String workNo, String createTm) throws Exception{
        	return planlistTestcaseRelationMapper.batchAddRelation(testcaseNos, planId, workNo, createTm);
	}

	/**
	 * 根据案例关系表主键进行修改案例执行状态
	 * @return
	 *
	 *
	 */
	@Override
	public int updateTestCaseExecuteStatus(String id, String testcaseExecuteResult, String remark, String userNameEn) throws Exception {
		int row = -1;
		PlanlistTestcaseRelation fpr = planlistTestcaseRelationMapper.queryRelation(id);
		String testcaseNo = fpr.getTestcaseNo();
		// 执行结果相同提示已经执行过
		if (testcaseExecuteResult.equals(fpr.getTestcaseExecuteResult())) {
			throw new FtmsException(ErrorConstants.EXECUTE_RESULT_STATUS_EXIST);
		}
		// 若是上次执行结果是失败并且matins状态是已解决或已关闭或确认拒绝则可进行再次执行
		if (fpr.getTestcaseExecuteResult().equals("3")) {
			Map m = new HashMap();
			m.put("id", id);
			m.put(Dict.REST_CODE, "mantis.queryIssueByPlanResultId");
			List<Map> list = (List<Map>) restTransport.submit(m);
			for (Map map2 : list) {
				if (!(map2.get("status").equals("80") || map2.get("status").equals("90") || map2.get("status").equals("30"))) {
					throw new FtmsException(Constants.HAS_ERROR);
				}
			}
		}
		row += this.testCaseService.updateByPrimaryKey(testcaseNo, remark);
		// 根据关系id查询上次执行结果 若是0变成其他存第一次时间fstTm存第一次操作人fstOpr
		// 若是其他变成其他存第二次时间testcaseExecuteDate

		// 案例点击执行数
		Integer exeNum = fpr.getExeNum();
		if (0 == exeNum) {
			exeNum = 1;
		} else {
			exeNum += 1;
		}
		// 当前时间戳
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String executeDate = sdf.format(new Date());
		//如果执行状态为阻塞或者失败，则分别独立记录次数
		Integer blockExeNum = null;
		Integer failExeNum = null;
		if("2".equals(testcaseExecuteResult)){
			blockExeNum = fpr.getBlockExeNum() == null ? 0 : fpr.getBlockExeNum();
			blockExeNum += 1;
		}
        if("3".equals(testcaseExecuteResult)){
            failExeNum = fpr.getFailExeNum() == null ? 0 : fpr.getFailExeNum();
            failExeNum += 1;
        }
		try {
			if (fpr.getTestcaseExecuteResult().equals("0")) {
				row += this.planlistTestcaseRelationMapper.updateTestCaseExecuteStatus(id,
						testcaseExecuteResult, executeDate, userNameEn, userNameEn, executeDate, exeNum, blockExeNum, failExeNum);
			} else {
				row += this.planlistTestcaseRelationMapper.updateTestCaseExecuteStatus(id,
						testcaseExecuteResult, executeDate, userNameEn, null, null, exeNum, blockExeNum, failExeNum);
			}
			//案例执行独立统计
			TestcaseExeRecord testcaseExeRecord = new TestcaseExeRecord();
			testcaseExeRecord.setDate(executeDate);
			testcaseExeRecord.setFprId(id);
			testcaseExeRecord.setOpr(userNameEn);
			testcaseExeRecord.setWorkNo(fpr.getWorkNo());
			testcaseExeRecord.setStatus(testcaseExecuteResult);
			testcaseExeRecord.setPlanId(String.valueOf(fpr.getPlanId()));
			testcaseExeRecord.setTestcaseNo(String.valueOf(fpr.getTestcaseNo()));
			testcaseExeRecord.setOprType(Constants.TESTCASE_NEW);
			planlistTestcaseRelationMapper.addTestcaseExeRecord(testcaseExeRecord);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new FtmsException(ErrorConstants.UPDATE_ERROR, new String[] { ",案例执行异常!" });
		}
		return row;
	}

	@Override
	public int queryCountByPlanIdandTestcaseNo(String testcaseNo, Integer planId) throws Exception {
		return planlistTestcaseRelationMapper.queryCountByPlanIdandTestcaseNo(testcaseNo, planId);
	}

	@Override
	public String queryPlanIdByPlanlistTestcaseId(String planlistTestcaseId, String workOrderNo) throws Exception {
		//根据redis缓存判断用户角色是否为master
		boolean master = iUserService.isMaster();
		//判断当前登录人是否属于工单号涉及人员
		boolean isRelated = iUserService.isRelated(workOrderNo);
		//如果登录人是master权限或者与所选工单有关,则允许查planID
		if(master||isRelated){
			Integer planId = 0;
			try {
				planId = planlistTestcaseRelationMapper.queryPlanIdByPlanlistTestcaseId(planlistTestcaseId);
			} catch (Exception e) {
				logger.error(e.toString());
				throw new FtmsException(ErrorConstants.TESTCASE_CAN_NOT_FOUND);
			}
			return planId.toString();
		}else{
			//否则抛出权限不足异常
			throw new FtmsException(ErrorConstants.ROLE_ERROR);
		}
	}

	/**
	 * 根据关系id该案例编号
	 * @param testcaseNo
	 * @param PlanlistTestcaseId
	 * @return
	 * @throws Exception
	 */
	@Override
	public Integer updateTestcaseByPlanlistTestcaseId(String testcaseNo, String PlanlistTestcaseId) throws Exception {
		return planlistTestcaseRelationMapper.updateTestcaseByPlanlistTestcaseId(testcaseNo, PlanlistTestcaseId);
	}

	/**
	 * 新增案例修改记录
	 * @param planlistTsetcaseId
	 * @throws Exception
	 */
	@Override
	public void addRelationModifyRecord(String planlistTsetcaseId) throws Exception {
		//修改案例关系的同时在执行表中新增一条执行修改记录
		PlanlistTestcaseRelation fpr = planlistTestcaseRelationMapper.queryRelation(planlistTsetcaseId);
		// 当前时间戳
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String executeDate = sdf.format(new Date());
		//案例执行独立统计
		Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
		if(Util.isNullOrEmpty(user)){
			throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
		}
		String userEnName = (String)user.get(Dict.USER_NAME_EN);
		TestcaseExeRecord testcaseExeRecord = new TestcaseExeRecord();
		testcaseExeRecord.setDate(executeDate);
		testcaseExeRecord.setFprId(String.valueOf(planlistTsetcaseId));
		testcaseExeRecord.setOpr(userEnName);
		testcaseExeRecord.setWorkNo(fpr.getWorkNo());
		testcaseExeRecord.setStatus(fpr.getTestcaseExecuteResult());
		testcaseExeRecord.setPlanId(String.valueOf(fpr.getPlanId()));
		testcaseExeRecord.setTestcaseNo(String.valueOf(fpr.getTestcaseNo()));
		testcaseExeRecord.setOprType(Constants.DEFAULT_2);
		planlistTestcaseRelationMapper.addTestcaseExeRecord(testcaseExeRecord);
	}

	@Override
	public void batchExecuteTestcase(List<String> ids, String testcaseExecuteResult) throws Exception {
		Map<String, Object> user = redisUtils.getCurrentUserInfoMap();
		if(Util.isNullOrEmpty(user)){
			throw new FtmsException(ErrorConstants.GET_CURRENT_USER_INFO_ERROR);
		}
		String userEnName = (String)user.get(Dict.USER_NAME_EN);
		for (String id : ids) {
			updateTestCaseExecuteStatus(id, testcaseExecuteResult,"", userEnName);
		}
	}
}
