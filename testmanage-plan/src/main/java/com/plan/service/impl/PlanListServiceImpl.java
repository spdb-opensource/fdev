package com.plan.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.plan.dao.TestcaseMapper;
import com.plan.dict.Dict;
import com.plan.dict.ErrorConstants;
import com.test.testmanagecommon.exception.FtmsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.plan.dao.PlanListMapper;
import com.plan.datamodel.PlanList;
import com.plan.service.PlanListService;
@Service
public class PlanListServiceImpl implements PlanListService {

    @Autowired
    private PlanListMapper planListMapper;
    @Autowired
    private TestcaseMapper testcaseMapper;

    /**
     * 根据计划ID和工单编号精准删除计划
     *
     * @param map
     * @return 受影响行数
     * @throws Exception
     */
    @Override
    public int deleteByPlanId(Map map) throws Exception {
        Integer planId = Integer.valueOf(map.get(Dict.PLANID).toString());
        String workNo = map.get(Dict.WORKNO).toString();
        //根据planId查询案例数
        Integer count;
        try {
            count = testcaseMapper.countCaseByPlanID(planId, workNo);
        } catch (Exception e) {
            throw new FtmsException(ErrorConstants.DATA_NOT_EXIST, new String[]{"根据planId查询案例失败"});
        }
        //若案例数不为零，则抛出异常
        if(!"0".equals(count.toString())){
            throw new FtmsException(ErrorConstants.DATA_DELETE_ERROR, new String[]{"计划下存在案例，"});
        }
        return planListMapper.deleteByPlanId(planId);
    }

    /**
     * 新增一个计划
     * @param record
     * @return 受影响行数
     * @throws Exception 
     */
    @Override
    public int insert(PlanList record) throws Exception {
        long nowDate =new Date().getTime();
        Integer sid =((int)nowDate);
        Integer planId=Math.abs(sid); //当前时间的绝对值
        record.setPlanId(planId);
        return this.planListMapper.insert(record);
    }
    /**
     * 根据主键查询
     * @param planId
     * @return 返回一个条数据
     * @throws Exception 
     */
    @Override
    public PlanList selectByPlanId(Integer planId) throws Exception {
        return this.planListMapper.selectByPlanId(planId);
    }
    /**
     * 查询全部计划
     * @return 返回所有计划
     * @throws Exception 
     */
    @Override
    public List<PlanList> selectAll() throws Exception {
        return this.planListMapper.selectAll();
    }

    /**
     * 根据主键修改一条复合的数据
     * @param planList
     * @return 受影响行数
     * @throws Exception 
     */
    @Override
    public int updateByPrimaryKey(PlanList planList) throws Exception {
        return this.planListMapper.updateByPrimaryKey(planList);
    }


    /**
     * 根据条件查询总数
     * @param work_no 工单编号
     * @param planName 计划名称
     * @return 总数
     * @throws Exception 
     */
    @Override
    public int selectByWorkNoByPlanNameCount(String work_no, String planName) throws Exception {
        return planListMapper.selectByWorkNoByPlanNameCount(work_no,planName);
    }
    /**
     * 根据工单编号查询当前工单下的计划
     * @param workNo
     * @return
     * @throws Exception 
     */
    @Override
    public List<PlanList> queryByworkNo(String workNo) throws Exception {
        return this.planListMapper.queryByworkNo(workNo);
    }
    /**
     * 根据执行计划id修改工单编号
     * @param workNo
     * @param planId
     * @return
     */
    @Override
    public Integer updateWorkNoByPlanId(String workNo, Integer planId) throws Exception {
        PlanList planList = planListMapper.selectByPlanId(planId);
        planList.setWorkNo(workNo);
        return this.planListMapper.updateByPrimaryKey(planList);
    }
}
