package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.dto.DemandAssessDto;
import com.spdb.fdev.fdemand.spdb.entity.DemandAssess;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface IDemandAssessService {

    /**
     * 新增
     *
     * @param demandAssess
     * @throws Exception
     */
    void save(DemandAssess demandAssess) throws Exception;

    /**
     * 根据需求id查询
     *
     * @param id
     * @return
     * @throws Exception
     */
    DemandAssess queryById(String id) throws Exception;

    /**
     * 更新
     *
     * @param demandAssess
     * @return
     * @throws Exception
     */
    DemandAssess update(DemandAssess demandAssess) throws Exception;

    /**
     * 根据需求id撤销
     *
     * @param id
     * @return
     * @throws Exception
     */
    DemandAssess delete(String id) throws Exception;

    /**
     * 需求列表查询
     *
     * @param dto
     * @return
     * @throws Exception
     */
    Map<String, Object> query(DemandAssessDto dto) throws Exception;

    /**
     * 批量计算需求评估时长
     */
    void calcDemandAssessDays();

    /**
     * 根据用户id获取牵头人邮箱
     *
     * @param demandLeader
     * @return
     */
    List<String> getDemandLeaderEmail(HashSet<String> demandLeader);

    XSSFWorkbook export(DemandAssessDto dto) throws Exception;

    void updateAssessOver(String oaContactNo);

    void syncConfState() throws Exception;

    /**
     * 通过传入需求评估id，手动确认需求评估完成
     *
     * @param id 需求评估id
     * @return 操作结果
     */
    DemandAssess confirmFinish(String id, String endAccessDate) throws Exception;

    /**
     * 依据传递的需求评估id 及 暂缓状态 确定是否暂缓
     *
     * @param id           需求评估id
     * @param demandStatus 暂缓状态
     * @return
     */
    void deferOperate(MultipartFile[] files, String fileType, String id, Integer demandStatus,String UserGroupCn) throws Exception;

    /**
     * 进行定稿日期修改
     * @param demandAssess
     * @throws Exception
     */
    void updateFinalDate(DemandAssess demandAssess) throws Exception;
}
