package com.spdb.fdev.fdemand.spdb.service;

import java.util.List;
import java.util.Map;

public interface IIpmpUtilsService {
    /**
     * 更新指定实施单元里程碑日期和延期原因分类
     *
     * @param params
     * @return
     * @throws Exception
     */
    Map updateImplUnit(Map<String, String> params) throws Exception ;
    /**
     * 更新指定实施单元其他信息
     *实施单元编号，实施牵头人域账号，实施牵头团队，测试牵头人中文姓名，测试牵头人域账号，
     * 测试牵头人邮箱，预期行内人员工作量，预期公司人员工作量，项目编号（项目/任务集编号）
     * @param params
     * @return
     * @throws Exception
     */
    void updateImplUnitOther(Map<String, Object> params) throws Exception ;
    /**
     * 根据实施单元编号查询是否需要功能点核算
     *
     * @param params
     * @return
     * @throws Exception
     */
    boolean getUfpCountFlag(String implUnitNum) throws Exception ;
    /**
     * 查询实施单元上云审核人（条线负责人或处长）
     *
     * @param params
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> getCloudCheckers() throws Exception ;
    /**
     * 查询技术方案编号
     *
     * @param params
     * @return
     * @throws Exception
     */
    List<Map<String, Object>> getSchemeReview() throws Exception ;
    /**
     * 更新指定实施单元云标识和技术方案
     *
     * @param params
     * @return
     * @throws Exception
     */
    Object updateCloudData(String implUnitNum,String planCloudFlag ,String planTechSchemeKey
            ,String prjNum ,String lineChargers,String userId,String userType) throws Exception ;
    /**
     * 根据实施单元编号和任务集编号校验是否可以挂接
     *
     * @return
     * @throws Exception
     */
    Boolean getImplUnitRelatSpFlag(String implUnitNum,String prjNum ) throws Exception ;

}
