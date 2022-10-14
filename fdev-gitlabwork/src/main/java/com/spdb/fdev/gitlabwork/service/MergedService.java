package com.spdb.fdev.gitlabwork.service;


import com.spdb.fdev.gitlabwork.entiy.MergedInfo;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;
import java.util.Map;

public interface MergedService {

    /**
     * 添加merged信息
     *
     * @param mergedInfo
     * @return
     */
    MergedInfo addMergedInfo(MergedInfo mergedInfo);

    /**
     * 获取merged信息，分页查询
     * @param params
     * @return
     */
    Map getMergedInfo(Map params);

    XSSFWorkbook exportMergedInfo(Map<String, Object> request);

    /**
     *
     * 通过gitlab_id来查询fdev用户信息和gitlab的用户信息
     * @param creator
     * @return
     */
    Map getCreatorInfo(Integer creator);


    /**
     * 确认merged请求的projectId在fdev上是否存在，若存在返回fdev的appId、group，否则返回null
     * @param projectId
     * @return
     */
    Map checkFapp(Integer projectId);

    /**
     * 调用用户模块接口来获取所有组的fullName并进行缓存
     *
     */
    List<Map> getFullNameGroup();

    /**
     * 校验用户是否为白名单组用户
     *
     * @param memberInfo
     * @return
     */
    Boolean checkGroupIsWList(Map memberInfo);
}
