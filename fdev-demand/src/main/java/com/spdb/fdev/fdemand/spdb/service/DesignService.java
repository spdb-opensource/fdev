package com.spdb.fdev.fdemand.spdb.service;

import com.spdb.fdev.fdemand.spdb.entity.DemandBaseInfo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


public interface DesignService {

    Map getDesignStateAndData(String rqrId) throws Exception;

    Long updateDesignState(String rqrId, Map<String,String> param) throws Exception;

    Long updateDesignRemark(String rqrId,String remark);

    DemandBaseInfo uploadDesignFile(MultipartFile file, String fileName, String taskId, String fileType, String uploadStage, String remark) throws Exception;

    String batchUpdateTaskDoc(String moduleName) throws Exception;
    
    List<Map<String,Object>> queryReviewDetailList(String reviewer, List<String> group, String startDate,
                                                   String endDate, String internetChildGroupId) throws Exception;

    void downLoadReviewList(HttpServletResponse response, String reviewer, List<String> group, String startDate, String endDate,
                            Map<String,String> columnMap, String internetChildGroupId) throws Exception;
}
