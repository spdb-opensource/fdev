package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import com.spdb.fdev.fdemand.spdb.entity.IpmpUnit;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface IIpmpUnitService {
    /**
     * 查看实施单元
     *
     * @param params
     * @return Map<String, Object>
     * @throws Exception
     */
    Map<String, Object> queryIpmpUnitByDemandId(Map<String, Object> params) throws Exception ;
    /**
     * 查看实施单元详情
     *
     * @param params
     * @return IpmpUnit
     * @throws Exception
     */
    IpmpUnit queryIpmpUnitById(Map<String, Object> params) throws Exception ;
    /**
     * 修改实施单元
     *
     * @param params
     * @return String
     * @throws Exception
     */
    Map<String,String> updateIpmpUnit(Map<String,Object> params) throws Exception ;
    /**
     * 向IPMP同步实施单元
     *
     * @param params
     * @return String
     * @throws Exception
     */
    Map<String,String> syncIpmpUnit(Map<String,Object> params) throws Exception ;

    /**
     * 更新研发单元、实施单元、需求状态
     *
     * @param params
     * @throws Exception
     */
    void updateStatus(Map<String, Object> params) throws Exception;

    /**
     * 更新需求状态,与实际日期
     *
     * @param demandBaseInfo
     * @throws Exception
     */
    void updateDemandStatus(DemandBaseInfo demandBaseInfo) throws Exception;

    /**
     * 更新实施单元状态,与实际日期
     *
     * @param ipmpUnit
     * @throws Exception
     */
    void updateIpmpUnitStatus(IpmpUnit ipmpUnit,String userNameEn) throws Exception;
    /**
     * 定时任务发送邮件提醒用户挂载研发单元
     *
     * @param params
     * @throws Exception
     */
    void sendUserMountDevUnit(Map<String, Object> params) throws Exception;

    /**
     * 根据实施单元编号查询任务列表
     *
     * @param params
     * @throws Exception
     */
    List<Map> queryTaskByIpmpUnitNo(Map<String, Object> params) throws Exception;
    /**
     * 从IPMP定时全量同步实施单元
     *
     * @param params
     * @throws Exception
     */
    void syncAllIpmpInfo(Map<String, Object> params) throws Exception;

    /**
     * 根据实施单元编号\研发单元编号、需求id查实施单元\研发单元信息及需求信息
     * @param demandId
     * @param unitNo
     * @return
     */
    Map<String,Object> queryByUnitNoAndDemandId(String demandId, String unitNo) throws Exception;

    /**
     * 根据实施单元编号\研发单元编号、需求id查实施单元\研发单元信息及需求信息
     * @param param
     * @return Map<String,Object>
     */
    Map<String,Object> queryIpmpUnitIsCheck(Map<String, Object> param) throws Exception;

    /**
     * 查询实施单元列表
     * @param params
     * @return Map<String,Object>
     */
    Map<String,Object> queryIpmpUnitList(Map<String, Object> params) throws Exception;

    /**
     * 导出实施单元列表
     * @return
     */
    void exportIpmpUnitList(Map<String, Object> params, HttpServletResponse resp) throws Exception;

    Object getTestFinishDateByUpdateTime(Map<String, Object> params) throws Exception;

    void getTestFinishDate(Map<String, Object> params) throws Exception;

    /**
     * 查询技术方案编号
     * @return List<Map<String,Object>>
     */
    List<Map<String,Object>> getSchemeReview(Map<String, Object> params) throws Exception;

    /**
     * 查询审核人
     * @return List<Map<String,Object>>
     */
    List<Map<String,Object>> getCloudCheckers(Map<String, Object> params) throws Exception;

    String adjustDate(IpmpUnit ipmpUnit, MultipartFile[] businessEmail) throws Exception;

    void confirmDelay(Map<String, Object> params) throws Exception;

    void deleteEmailFile(Map<String, Object> params) throws Exception;

    Boolean getImplUnitRelatSpFlag(String implUnitNum, String prjNum) throws Exception;
}
